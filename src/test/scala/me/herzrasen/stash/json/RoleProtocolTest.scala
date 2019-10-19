package me.herzrasen.stash.json

import me.herzrasen.stash.json.RoleProtocol._
import spray.json._
import me.herzrasen.stash.domain.Roles._
import org.scalatest.FlatSpec
import org.scalatest.Matchers

class RoleProtocolTest extends FlatSpec with Matchers {

  "Serializing / deserializing" should "be correct for Admin" in {
    val role: Role = Admin
    val json = role.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Role]
    fromJson shouldEqual role
  }

  it should "be correct for User" in {
    val role: Role = User
    val json = role.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Role]
    fromJson shouldEqual role
  }

  it should "be correct for Unknown" in {
    val role: Role = Unknown
    val json = role.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Role]
    fromJson shouldEqual role
  }

  "Parsing an invalid JSON" should "return Unknown" in {
    val json = 42.toJson.prettyPrint
    val fromJson = json.parseJson.convertTo[Role]
    fromJson shouldEqual Unknown
  }
}
