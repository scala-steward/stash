package me.herzrasen.stash.domain

import me.herzrasen.stash.domain.Roles._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RolesTest extends AnyFlatSpec with Matchers {

  "Parsing from String" should "be Admin" in {
    val admin = Roles.parse("admin")
    admin shouldBe Admin
  }

  it should "be Admin (ignorecase)" in {
    val admin = Roles.parse("ADMIN")
    admin shouldBe Admin
  }

  it should "be User" in {
    val user = Roles.parse("user")
    user shouldBe User
  }

  it should "be User (ignorecase)" in {
    val user = Roles.parse("USER")
    user shouldBe User
  }

  it should "be Unknown" in {
    val unknown = Roles.parse("FooBaR")
    unknown shouldBe Unknown
  }

  it should "be Unknown when passing null" in {
    val unknown = Roles.parse(null)
    unknown shouldBe Unknown
  }

  "String representation" should "be admin" in {
    Admin.mkString shouldEqual "admin"
  }

  it should "be user" in {
    User.mkString shouldEqual "user"
  }

  it should "be unknown" in {
    Unknown.mkString shouldEqual "unknown"
  }
}
