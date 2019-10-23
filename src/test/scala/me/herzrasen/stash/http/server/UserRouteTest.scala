package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import me.herzrasen.stash.auth.{HmacSecret, JwtUtil}
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

  implicit val hmacSecret: HmacSecret = HmacSecret("user-route-test")

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

  "GET /v1/users/id" should "complete successfully for own id" in {
    val token = JwtUtil.create(user)
    Get(s"/v1/users/${user.id}") ~> addHeader("Authorization", s"Bearer $token") ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[User] shouldEqual user
    }
  }

  it should "complete successfully when an Admin queries an user" in {
    val token = JwtUtil.create(admin)
    Get(s"/v1/users/${user.id}") ~> addHeader("Authorization", s"Bearer $token") ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[User] shouldEqual user
    }
  }

  it should "fail when anonymous queries an user" in {
    Get(s"/v1/users/${user.id}") ~> new UserRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "return Forbidden when an user queries an unknown id" in {
    val token = JwtUtil.create(user)
    Get(s"/v1/users/99999") ~> addHeader("Authorization", s"Bearer $token") ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.Forbidden
    }
  }

  it should "return NotFound an admin queries an unknown id" in {
    val token = JwtUtil.create(admin)
    Get(s"/v1/users/99999") ~> addHeader("Authorization", s"Bearer $token") ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "fail when finding the users by id fails" in {
    implicit val repository: UserRepository =
      new FailingFindInMemoryUserRepository
    val token = JwtUtil.create(admin)
    Get("/v1/users/123") ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  "PUT /v1/user/<id>" should "update the password" in {
    val token = JwtUtil.create(user)
    val newPassword = "mynewpassword"
    Put(s"/v1/users/${user.id}", newPassword) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "fail when trying to update the password of another user" in {
    val token = JwtUtil.create(admin)
    val newPassword = "mynewpassword"
    Put(s"/v1/users/${user.id}", newPassword) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "fail when finding the user fails" in {
    implicit val repository: UserRepository =
      new FailingFindInMemoryUserRepository
    val token = JwtUtil.create(user)
    val newPassword = "mynewpassword"
    Put(s"/v1/users/${user.id}", newPassword) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  it should "fail when the user is not found" in {
    val id = 42
    val token = JWT
      .create()
      .withClaim("id", Integer.valueOf(id))
      .withClaim("role", "user")
      .withIssuer("stash")
      .sign(Algorithm.HMAC256(hmacSecret.value))
    val newPassword = "mynewpassword"
    Put(s"/v1/users/$id", newPassword) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "return NotModified when the update fails" in {
    implicit val repository: UserRepository = new FailingUpdatePasswordUserRepository
    repository.create(user)
    val token = JwtUtil.create(user)
    val newPassword = "mynewpassword"
    Put(s"/v1/users/${user.id}", newPassword) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new UserRoute().route ~> check {
      status shouldEqual StatusCodes.NotModified
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

    override def find(id: Int): Future[Option[User]] =
      Future.failed(new IllegalArgumentException("find failed"))
  }

  class FailingUpdatePasswordUserRepository extends InMemoryUserRepository {
    override def updatePassword(user: User, newPassword: String): Future[Unit] =
      Future.failed(new IllegalArgumentException("updatePassword failed"))
  }

}
