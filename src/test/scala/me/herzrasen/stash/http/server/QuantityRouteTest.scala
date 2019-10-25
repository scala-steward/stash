package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.herzrasen.stash.auth.{HmacSecret, JwtUtil}
import me.herzrasen.stash.domain.{NewQuantity, Quantity, Roles, User}
import me.herzrasen.stash.json.JsonSupport._
import me.herzrasen.stash.repository.{InMemoryQuantityRepository, QuantityRepository}
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

import scala.concurrent.Future

class QuantityRouteTest
    extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with SprayJsonSupport {

  implicit val hmacSecret: HmacSecret = HmacSecret("quantity-route-test")

  implicit val repository: QuantityRepository = new InMemoryQuantityRepository
  val quantity1 = Quantity(1, "Quantity1", Some("qt1."))
  val quantity2 = Quantity(1, "Quantity2", Some("qt2."))
  repository.create(quantity1)
  repository.create(quantity2)

  val user = User(1000, "A User", JwtUtil.hash("user"), Roles.User)

  "GET /v1/quantities" should "return the list of quantities to an unprivileged user" in {
    val token = JwtUtil.create(user)

    Get("/v1/quantities") ~> addHeader("Authorization", s"Bearer $token") ~> new QuantityRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      val json = responseAs[String]
      val quantities = json.parseJson.convertTo[List[Quantity]]
      quantities should contain allOf (quantity1, quantity2)
    }
  }

  it should "reject when calling unauthorized" in {
    Get("/v1/quantities") ~> new QuantityRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "fail when finding quantities fails" in {
    implicit val repository: QuantityRepository =
      new FailingFindQuantityRepository
    val token = JwtUtil.create(user)

    Get("/v1/quantities") ~> addHeader("Authorization", s"Bearer $token") ~> new QuantityRoute().route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  "POST /v1/quantities" should "create a new Quantity" in {
    val token = JwtUtil.create(user)

    Post("/v1/quantities", NewQuantity("Foo", None)) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new QuantityRoute().route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "fail when the Quantity already exists" in {
    val token = JwtUtil.create(user)

    Post("/v1/quantities", NewQuantity(quantity1.name, quantity1.abbreviation)) ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new QuantityRoute().route ~> check {
      status shouldEqual StatusCodes.NotModified
    }
  }

  "DELETE /v1/quantities/<id>" should "delete an Quantity" in {
    val token = JwtUtil.create(user)

    Delete(s"/v1/quantities/${quantity1.id}") ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new QuantityRoute().route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "return NotFound when deleting a Quantity that does not exist" in {
    val token = JwtUtil.create(user)

    Delete(s"/v1/quantities/999") ~> addHeader(
      "Authorization",
      s"Bearer $token"
    ) ~> new QuantityRoute().route ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }

  it should "fail when finding the Quantity to delete fails" in {
    implicit val repository: QuantityRepository =
      new FailingFindQuantityRepository
    val token = JwtUtil.create(user)

    Delete("/v1/quantities/1") ~> addHeader("Authorization", s"Bearer $token") ~> new QuantityRoute().route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  it should "return NotModified when deleting the Quantity fails" in {
    implicit val repository: QuantityRepository =
      new FailingDeletQuantityRepository
    val token = JwtUtil.create(user)
    repository.create(quantity1)

    Delete("/v1/quantities/1") ~> addHeader("Authorization", s"Bearer $token") ~> new QuantityRoute().route ~> check {
      status shouldEqual StatusCodes.NotModified
    }
  }

  class FailingFindQuantityRepository extends InMemoryQuantityRepository {
    override def findAll(): Future[List[Quantity]] =
      Future.failed(new IllegalArgumentException("findAll failed"))

    override def find(id: Int): Future[Option[Quantity]] =
      Future.failed(new IllegalArgumentException("find failed"))
  }

  class FailingDeletQuantityRepository extends InMemoryQuantityRepository {
    override def delete(quantity: Quantity): Future[Unit] =
      Future.failed(new IllegalArgumentException("delete failed"))
  }

}
