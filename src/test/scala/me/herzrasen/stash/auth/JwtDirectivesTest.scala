package me.herzrasen.stash.auth
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.PathDirectives
import akka.http.scaladsl.server.directives.RouteDirectives
import akka.http.scaladsl.server.AuthorizationFailedRejection
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles
import akka.http.scaladsl.model.StatusCodes
import com.auth0.jwt.JWT
import java.util.Date
import java.time.ZonedDateTime
import com.auth0.jwt.algorithms.Algorithm
import akka.http.scaladsl.server.RouteConcatenation

class JwtDirectivesTest extends FlatSpec with Matchers with ScalatestRouteTest {

  object TestRoute
      extends JwtDirectives
      with PathDirectives
      with RouteDirectives
      with RouteConcatenation {

    val route: Route =
      path("test") {
        authorize {
          complete("Hello, Authorized")
        }
      } ~ path("admin") {
        authorizeAdmin {
          complete("Hello, Admin")
        }
      }

  }

  "An admin endpoint" should "complete for an Admin" in {
    val user = User(42, "Test", JwtUtil.hash("mypassword"), Roles.Admin)
    val token = JwtUtil.create(user)

    Get("/admin") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Hello, Admin"
    }
  }

  it should "be rejected for an User" in {
    val user = User(42, "Test", JwtUtil.hash("mypassword"), Roles.User)
    val token = JwtUtil.create(user)

    Get("/admin") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "be rejected for an Unknown user" in {
    val user = User(42, "Test", JwtUtil.hash("mypassword"), Roles.Unknown)
    val token = JwtUtil.create(user)

    Get("/admin") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "be rejected for an unauthorized user" in {
    Get("/admin") ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  "An unauthorized request" should "be rejected" in {
    Get("/test") ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  "An authorized request" should "be completed" in {
    val user = User(42, "Test", JwtUtil.hash("mypassword"), Roles.User)
    val token = JwtUtil.create(user)

    Get("/test") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Hello, Authorized"
    }
  }

  it should "be completed for an admin" in {
    val user = User(42, "Test", JwtUtil.hash("mypassword"), Roles.Admin)
    val token = JwtUtil.create(user)

    Get("/test") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "Hello, Authorized"
    }
  }

  it should "be rejected for an Unknown user" in {
    val user = User(42, "Test", JwtUtil.hash("mypassword"), Roles.Unknown)
    val token = JwtUtil.create(user)

    Get("/test") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "be rejected for an expired token" in {
    val token =
      JWT
        .create()
        .withClaim("role", "admin")
        .withExpiresAt(
          Date.from(ZonedDateTime.now().minusMinutes(1).toInstant())
        )
        .sign(Algorithm.HMAC256("test"))

    Get("/test") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "be rejected for a token without role claim" in {
    val token =
      JWT
        .create()
        .withExpiresAt(
          Date.from(ZonedDateTime.now().plusDays(7).toInstant())
        )
        .sign(Algorithm.HMAC256("test"))

    Get("/test") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "be rejected for an Non-Bearer token" in {
    Get("/test") ~> addHeader(
      "Authorization",
      s"Basic c29tZSB1c2VyOnRlc3QxMjM="
    ) ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

}
