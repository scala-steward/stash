package me.herzrasen.stash.json

import me.herzrasen.stash.domain.Roles.{User => UserRole}
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.json.UserProtocol._
import org.scalatest.{FlatSpec, Matchers}
import spray.json._

class UserProtocolTest extends FlatSpec with Matchers {

  "Serializing / deserializing" should "be correct" in {
    val user = User(42, "Test", "mypassword", UserRole)
    val json = user.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[User]
    fromJson shouldEqual user
  }
}
