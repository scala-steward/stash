package me.herzrasen.stash.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.herzrasen.stash.auth.JwtUtil
import me.herzrasen.stash.domain.{Roles, User}
import me.herzrasen.stash.repository.{InMemoryUserRepository, UserRepository}
import org.scalatest.{FlatSpec, Matchers}

class RouteTest extends FlatSpec with Matchers with ScalatestRouteTest {

  val adminPassword: String = "test123"
  val admin: User = User(1, "Admin", JwtUtil.hash(adminPassword), Roles.Admin)

  implicit val repository: UserRepository = new InMemoryUserRepository()
  repository.create(admin)

  "The route" should "contain the Auth endpoints" in {
    Get("/v1/token") ~> addCredentials(BasicHttpCredentials(admin.name, adminPassword)) ~> Routes() ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "contain the User endpoints" in {
    val token = JwtUtil.create(admin)
    Get("/v1/users") ~> addHeader("Authorization", s"Bearer $token") ~> Routes() ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

}
