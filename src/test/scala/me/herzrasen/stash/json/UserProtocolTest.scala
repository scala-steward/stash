package me.herzrasen.stash.json
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import UserProtocol._
import spray.json._
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles.{User => UserRole}

class UserProtocolTest extends FlatSpec with Matchers {

  "Serializing / deserializing" should "be correct" in {
    val user = User(42, "Test", "mypassword", UserRole)
    val json = user.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[User]
    fromJson shouldEqual user
  }
}
