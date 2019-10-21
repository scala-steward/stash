package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.herzrasen.stash.auth.JwtUtil
import me.herzrasen.stash.domain.{NewUser, Roles, User}
import me.herzrasen.stash.json.JsonSupport._
import me.herzrasen.stash.repository.{InMemoryUserRepository, UserRepository}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.Future

class UserRouteTest
    extends FlatSpec
    with Matchers
    with SprayJsonSupport
    with ScalatestRouteTest {

  val admin = User(1, "Admin", JwtUtil.hash("test123"), Roles.Admin)
  val user = User(2, "User", JwtUtil.hash("test123"), Roles.User)
  val unknown = User(3, "Unknown", JwtUtil.hash("test123"), Roles.Unknown)

  implicit val repository: UserRepository = new InMemoryUserRepository()
  repository.create(admin)
  repository.create(user)
  repository.create(unknown)

  "GET /v1/users" should "complete successfully for an admin" in {
    val token = JwtUtil.create(admin)

    Get("/v1/users") ~> addHeader("Authorization", s"Bearer $token") ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      val json = responseAs[String]
      val users = json.parseJson.convertTo[List[User]]
      users should have size (3)
      users should contain allOf (admin, user, unknown)
    }
  }

  it should "be rejected for users" in {
    val token = JwtUtil.create(user)

    Get("/v1/users") ~> addHeader("Authorization", s"Bearer $token") ~> new UserRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "be rejected for unknown users" in {
    val token = JwtUtil.create(unknown)

    Get("/v1/users") ~> addHeader("Authorization", s"Bearer $token") ~> new UserRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "be rejected for anonymous" in {
    Get("/v1/users") ~> new UserRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "return 500 when failing finding users" in {
    implicit val repository: UserRepository =
      new FailingFindInMemoryUserRepository
    val token = JwtUtil.create(admin)
    Get("/v1/users") ~> addHeader("Authorization", s"Bearer $token") ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  "POST /v1/users" should "create a new user" in {
    val token = JwtUtil.create(admin)
    val newUser = NewUser("foo", "bar")
    Post("/v1/users", newUser) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      val user = responseAs[User]
      user.name shouldEqual newUser.name
      user.password shouldEqual JwtUtil.hash(newUser.password)
    }
  }

  it should "fail when the new user already exists" in {
    val token = JwtUtil.create(admin)
    val newUser = NewUser("User", "myuserpassword")
    Post("/v1/users", newUser) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.Conflict
      responseAs[String] shouldEqual "User User already exists"
    }
  }

  it should "fail when finding the user by name fails" in {
    implicit val repository: UserRepository =
      new FailingFindInMemoryUserRepository
    val token = JwtUtil.create(admin)
    val newUser = NewUser("User", "myuserpassword")
    Post("/v1/users", newUser) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  it should "fail when creating the user fails" in {
    implicit val repository: UserRepository =
      new FailingCreateInMemoryUserRepository
    val token = JwtUtil.create(admin)
    val newUser = NewUser("New", "myuserpassword")
    Post("/v1/users", newUser) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  class FailingCreateInMemoryUserRepository extends InMemoryUserRepository {
    override def create(user: User): Future[User] =
      Future.failed(new IllegalArgumentException("create failed"))
  }

  class FailingFindInMemoryUserRepository extends InMemoryUserRepository {
    override def findAll(): Future[List[User]] =
      Future.failed(new IllegalArgumentException("findAll failed"))

    override def find(name: String): Future[Option[User]] =
      Future.failed(new IllegalArgumentException("find failed"))
  }

}
