package me.herzrasen.stash.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.herzrasen.stash.auth.{HmacSecret, JwtUtil}
import me.herzrasen.stash.domain.{Roles, Shop, User}
import me.herzrasen.stash.repository.{InMemoryShopRepository, ShopRepository}
import org.scalatest.{FlatSpec, Matchers}
import me.herzrasen.stash.json.JsonSupport._
import spray.json._

import scala.concurrent.Future

class ShopRouteTest extends FlatSpec with Matchers with ScalatestRouteTest {

  implicit val hmacSecret: HmacSecret = HmacSecret("shop-route-test")

  implicit val repository: ShopRepository = new InMemoryShopRepository
  val shop1 = Shop(1, "Shop 1")
  val shop2 = Shop(2, "Shop 2")
  repository.create(shop1)
  repository.create(shop2)

  val user = User(1000, "A User", JwtUtil.hash("user"), Roles.User)
  val admin = User(1001, "An Admin", JwtUtil.hash("admin"), Roles.Admin)

  "GET /v1/shops" should "return the list of shops to an unprivileged user" in {
    val token = JwtUtil.create(user)

    Get("/v1/shops") ~> addHeader("Authorization", s"Bearer $token") ~> new ShopRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      val json = responseAs[String]
      val shops = json.parseJson.convertTo[List[Shop]]
      shops should contain allOf (shop1, shop2)
    }
  }

  it should "return the list of shops to an admin" in {
    val token = JwtUtil.create(admin)

    Get("/v1/shops") ~> addHeader("Authorization", s"Bearer $token") ~> new ShopRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      val json = responseAs[String]
      val shops = json.parseJson.convertTo[List[Shop]]
      shops should contain allOf (shop1, shop2)
    }
  }

  it should "reject when calling unauthorized" in {
    Get("/v1/shops") ~> new ShopRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  it should "fail when finding shops fails" in {
    implicit val repository: ShopRepository = new FailingFindShopRepository
    val token = JwtUtil.create(admin)

    Get("/v1/shops") ~> addHeader("Authorization", s"Bearer $token") ~> new ShopRoute().route ~> check {
      status shouldEqual StatusCodes.InternalServerError
    }
  }

  class FailingFindShopRepository extends InMemoryShopRepository {
    override def findAll(): Future[List[Shop]] =
      Future.failed(new IllegalArgumentException("findAll failed"))
  }

}
