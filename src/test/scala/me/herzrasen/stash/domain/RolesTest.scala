package me.herzrasen.stash.domain
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import me.herzrasen.stash.domain.Roles._

class RolesTest extends FlatSpec with Matchers {

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
