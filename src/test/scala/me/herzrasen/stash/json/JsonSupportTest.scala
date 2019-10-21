package me.herzrasen.stash.json

import me.herzrasen.stash.domain.Roles.Role
import me.herzrasen.stash.domain.{NewUser, Roles, Shop, User}
import me.herzrasen.stash.json.JsonSupport._
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class JsonSupportTest extends FlatSpec with Matchers {

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
}
