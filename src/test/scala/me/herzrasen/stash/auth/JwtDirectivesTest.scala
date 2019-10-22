package me.herzrasen.stash.auth

import java.time.ZonedDateTime
import java.util.Date

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.{PathDirectives, RouteDirectives}
import akka.http.scaladsl.server.{
  AuthorizationFailedRejection,
  Route,
  RouteConcatenation
}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import me.herzrasen.stash.domain.{Roles, User}
import org.scalatest.{FlatSpec, Matchers}

class JwtDirectivesTest extends FlatSpec with Matchers with ScalatestRouteTest {

  object TestRoute
      extends JwtDirectives
      with PathDirectives
      with RouteDirectives
      with RouteConcatenation {

    val route: Route =
      path("test") {
        authorize { id =>
          complete(s"$id")
        }
      } ~ path("admin") {
        authorizeAdmin { id =>
          complete(s"$id")
        }
      }

  }

  "An admin endpoint" should "complete for an Admin" in {
    val user = User(42, "Test", JwtUtil.hash("mypassword"), Roles.Admin)
    val token = JwtUtil.create(user)

    Get("/admin") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "42"
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
    val user = User(41, "Test", JwtUtil.hash("mypassword"), Roles.User)
    val token = JwtUtil.create(user)

    Get("/test") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "41"
    }
  }

  it should "be completed for an admin" in {
    val user = User(42, "Test", JwtUtil.hash("mypassword"), Roles.Admin)
    val token = JwtUtil.create(user)

    Get("/test") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[String] shouldEqual "42"
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

  "Access" should "be rejected when a token does not contain an id" in {
    val token = JWT
      .create()
      .withClaim("role", "user")
      .withExpiresAt(Date.from(ZonedDateTime.now().plusDays(7).toInstant))
      .sign(Algorithm.HMAC256("test"))

    Get("/test") ~> addHeader("Authorization", s"Bearer $token") ~> TestRoute.route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

}
