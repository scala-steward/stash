package me.herzrasen.stash.json

import me.herzrasen.stash.domain.Roles.Role
import me.herzrasen.stash.domain.{Item, NewItem, NewQuantity, NewUser, Quantity, Roles, Shop, User}
import me.herzrasen.stash.json.JsonSupport._
import spray.json._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JsonSupportTest extends AnyFlatSpec with Matchers {

  "A Role" should "be serialized / deserialized correctly for an Admin" in {
    val role: Role = Roles.Admin
    val json = role.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Role]
    fromJson shouldEqual role
  }

  it should "be serialized / deserialized correctly for an User" in {
    val role: Role = Roles.User
    val json = role.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Role]
    fromJson shouldEqual role
  }

  it should "be serialized / deserialized correctly for an unknown User" in {
    val role: Role = Roles.Unknown
    val json = role.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Role]
    fromJson shouldEqual role
  }

  "Parsing an invalid Role" should "return Unknown" in {
    val json = 42.toJson.prettyPrint
    val fromJson = json.parseJson.convertTo[Role]
    fromJson shouldEqual Roles.Unknown
  }

  "A Shop" should "be serialized / deserialized correctly" in {
    val shop = Shop(42, "My Shop")
    val json = shop.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Shop]
    fromJson shouldEqual shop
  }

  "A Quantity" should "be serialized / deserialized correctly" in {
    val quantity = Quantity(1000, "Foobar", Some("Foo"))
    val json = quantity.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Quantity]
    fromJson shouldEqual quantity
  }

  "A NewQuantity" should "be serialized / deserialized correctly" in {
    val newQuantity = NewQuantity("Foobar", Some("Foo"))
    val json = newQuantity.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[NewQuantity]
    fromJson shouldEqual newQuantity
  }

  "A User" should "be serialized / deserialized correctly" in {
    val user = User(42, "Test", "mypassword", Roles.User)
    val json = user.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[User]
    fromJson shouldEqual user
  }

  "A NewUser" should "be serialized / deserialized correctly" in {
    val newUser = NewUser("Test", "mytest")
    val json = newUser.toJson.prettyPrint

    json.parseJson.convertTo[NewUser] shouldEqual newUser
  }

  "An Item" should "be serialized / deseerialized corrrectly" in {
    val item = Item(1000,"Item", 1001, 5, 1, 1002)
    val json = item.toJson.prettyPrint

    json.parseJson.convertTo[Item] shouldEqual item
  }

  "A NewItem" should "be serialized / deseerialized corrrectly" in {
    val newItem = NewItem("Item", 1001, 5, 1, 1002)
    val json = newItem.toJson.prettyPrint

    json.parseJson.convertTo[NewItem] shouldEqual newItem
  }

}
