package me.herzrasen.stash.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, HttpChallenge}
import akka.http.scaladsl.server.AuthenticationFailedRejection
import akka.http.scaladsl.server.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.herzrasen.stash.auth.{HmacSecret, JwtUtil}
import me.herzrasen.stash.domain.Roles.Admin
import me.herzrasen.stash.domain.{Roles, User}
import me.herzrasen.stash.repository.{InMemoryUserRepository, UserRepository}
import org.scalatest.{FlatSpec, Matchers}

class AuthRouteTest extends FlatSpec with Matchers with ScalatestRouteTest {

  implicit val hmacSecret: HmacSecret = HmacSecret("auth-route-test")

  val admin = User(1, "Admin", JwtUtil.hash("test123"), Roles.Admin)
  val user = User(2, "User", JwtUtil.hash("test123"), Roles.User)
  val unknown = User(3, "Unknown", JwtUtil.hash("test123"), Roles.Unknown)

  implicit val repository: UserRepository = new InMemoryUserRepository()
  repository.create(admin)
  repository.create(user)
  repository.create(unknown)

  "/v1/token" should "return a valid token for an admin" in {
    Get("/v1/token") ~> addCredentials(
      BasicHttpCredentials(admin.name, "test123")
    ) ~> new AuthRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      val token = responseAs[String]
      JwtUtil.role(token) shouldEqual Admin
    }
  }

  it should "return a valid token for an user" in {
    Get("/v1/token") ~> addCredentials(
      BasicHttpCredentials(user.name, "test123")
    ) ~> new AuthRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      val token = responseAs[String]
      JwtUtil.role(token) shouldEqual Roles.User
    }
  }

  it should "be rejected when providing the wrong password for a valid user" in {
    Get("/v1/token") ~> addCredentials(
      BasicHttpCredentials(user.name, "notcorrect")
    ) ~> new AuthRoute().route ~> check {
      rejection shouldEqual AuthenticationFailedRejection(
        CredentialsRejected,
        HttpChallenge("Basic", "secure site", Map("charset" -> "UTF-8"))
      )
    }
  }

  it should "not return a token for an unknown user" in {
    Get("/v1/token") ~> addCredentials(
      BasicHttpCredentials(unknown.name, "test123")
    ) ~> new AuthRoute().route ~> check {
      rejection shouldEqual AuthenticationFailedRejection(
        CredentialsRejected,
        HttpChallenge("Basic", "secure site", Map("charset" -> "UTF-8"))
      )
    }
  }

  it should "be rejected when omitting credentials" in {
    Get("/v1/token") ~> new AuthRoute().route ~> check {
      rejection shouldEqual AuthenticationFailedRejection(
        CredentialsMissing,
        HttpChallenge("Basic", "secure site", Map("charset" -> "UTF-8"))
      )
    }
  }

  it should "be rejected when providing unknown credentials" in {
    Get("/v1/token") ~> addCredentials(
      BasicHttpCredentials("Mr. Unknown", "test3233")
    ) ~> new AuthRoute().route ~> check {
      rejection shouldEqual AuthenticationFailedRejection(
        CredentialsRejected,
        HttpChallenge("Basic", "secure site", Map("charset" -> "UTF-8"))
      )
    }
  }
}
