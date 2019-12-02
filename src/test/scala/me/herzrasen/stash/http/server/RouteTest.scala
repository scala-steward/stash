package me.herzrasen.stash.http.server

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.testkit.ScalatestRouteTest
import me.herzrasen.stash.auth.{HmacSecret, JwtUtil}
import me.herzrasen.stash.domain.{Roles, User}
import me.herzrasen.stash.repository.{
  InMemoryItemRepository,
  InMemoryQuantityRepository,
  InMemoryShopRepository,
  InMemoryUserRepository,
  ItemRepository,
  QuantityRepository,
  ShopRepository,
  UserRepository
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RouteTest extends AnyFlatSpec with Matchers with ScalatestRouteTest {

  implicit val hmacSecret: HmacSecret = HmacSecret("route-test")

  val adminPassword: String = "test123"
  val admin: User = User(1, "Admin", JwtUtil.hash(adminPassword), Roles.Admin)

  implicit val repository: UserRepository = new InMemoryUserRepository()
  repository.create(admin)

  implicit val shopRepository: ShopRepository = new InMemoryShopRepository()

  implicit val quantityRepository: QuantityRepository =
    new InMemoryQuantityRepository()

  implicit val itemRepository: ItemRepository = new InMemoryItemRepository()

  "The route" should "contain the Auth endpoints" in {
    Get("/v1/token") ~> addCredentials(
      BasicHttpCredentials(admin.name, adminPassword)
    ) ~> Routes() ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "contain the User endpoints" in {
    val token = JwtUtil.create(admin)
    Get("/v1/users") ~> addHeader("Authorization", s"Bearer $token") ~> Routes() ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "contain the Shop endpoints" in {
    val token = JwtUtil.create(admin)
    Get("/v1/shops") ~> addHeader("Authorization", s"Bearer $token") ~> Routes() ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "contain the Quantity endpoints" in {
    val token = JwtUtil.create(admin)
    Get("/v1/quantities") ~> addHeader("Authorization", s"Bearer $token") ~> Routes() ~> check {
      status shouldEqual StatusCodes.OK
    }
  }

  it should "contain the Item endpoints" in {
    val token = JwtUtil.create(admin)
    Get("/v1/items") ~> addHeader("Authorization", s"Bearer $token") ~> Routes() ~> check {
      status shouldEqual StatusCodes.OK
    }
  }
}
