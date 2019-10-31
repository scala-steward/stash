package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthorizationFailedRejection
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.herzrasen.stash.auth.{HmacSecret, JwtUtil}
import me.herzrasen.stash.domain._
import me.herzrasen.stash.json.JsonSupport._
import me.herzrasen.stash.repository._
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class ItemRouteTest
    extends FlatSpec
    with Matchers
    with SprayJsonSupport
    with ScalatestRouteTest {

  implicit val hmacSecret: HmacSecret = HmacSecret("item-route-test")

  val shopRepository: ShopRepository = new InMemoryShopRepository
  val shop = Shop(1, "My Shop")
  shopRepository.create(shop)

  val quantityRepository: QuantityRepository = new InMemoryQuantityRepository
  val quantity = Quantity(1, "Quantity", Some("qty"))
  quantityRepository.create(quantity)

  implicit val repository: ItemRepository = new InMemoryItemRepository
  val item1 = Item(1, "Testitem", shop.id, quantity.id, 10.0f, 1.0f)
  val item2 = Item(2, "Another Item", shop.id, quantity.id, 1f, 2f)
  repository.create(item1)
  repository.create(item2)

  val user = User(1000, "A User", JwtUtil.hash("user"), Roles.User)

  "GET /v1/items" should "return the list of items to an user" in {
    val token = JwtUtil.create(user)

    Get("/v1/items") ~> addHeader("Authorization", s"Bearer $token") ~> new ItemRoute().route ~> check {
      status shouldEqual StatusCodes.OK
      val json = responseAs[String]
      val items = json.parseJson.convertTo[List[Item]]
      items should contain allOf (item1, item2)
    }
  }

  it should "reject when calling unauthorized" in {
    Get("/v1/items") ~> new ItemRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

  "POST /v1/items" should "register a new Item" in {
    val token = JwtUtil.create(user)

    Post(
      "/v1/items",
      NewItem("new testitem", shop.id, quantity.id, 5f, 1f)
    ) ~> addHeader("Authorization", s"Bearer $token") ~> new ItemRoute().route ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "reject when calling unauthorized" in {
    Post("/v1/items", NewItem("new testitem", shop.id, quantity.id, 5f, 1f)) ~> new ItemRoute().route ~> check {
      rejection shouldEqual AuthorizationFailedRejection
    }
  }

}
