package me.herzrasen.stash.auth

import java.time.ZonedDateTime
import java.util.Date

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import me.herzrasen.stash.domain.Roles.Admin
import me.herzrasen.stash.domain.{Roles, User}
import org.scalatest.{FlatSpec, Matchers}

class JwtUtilTest extends FlatSpec with Matchers {

  implicit val hmacSecret: HmacSecret = HmacSecret("jwt-util")

  "An invalid token" should "return Roles.Unknown" in {
    val role = JwtUtil.role("foobarnotvalid")
    role shouldEqual Roles.Unknown
  }

  "A Token" should "be created" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    val decoded = JWT.decode(token)
    decoded.getIssuer shouldEqual "stash"
    decoded.getClaim("id").asInt shouldEqual 42
    decoded.getClaim("role").asString shouldEqual "admin"
    decoded.getClaim("user").asString shouldEqual "Test"
  }

  it should "have a valid id claim" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    JwtUtil.id(token) shouldEqual Some(42)
  }

  it should "return None when no id is found" in {
    val token = JWT
      .create()
      .withClaim("role", "user")
      .withExpiresAt(Date.from(ZonedDateTime.now().plusDays(7).toInstant))
      .sign(Algorithm.HMAC256("test"))

    JwtUtil.id(token) shouldEqual None
  }

  it should "have a valid role claim" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    JwtUtil.role(token) shouldEqual Admin
  }

  it should "have a valid user claim" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    JwtUtil.user(token) shouldEqual Some("Test")
  }

  "A non-existent claim" should "fallback to the default" in {
    val token = JwtUtil.create(User(42, null, "foo", Roles.Unknown))
    JwtUtil.user(token) shouldEqual None
  }

  "A Hash" should "be generated" in {
    val hash = JwtUtil.hash("test")
    hash shouldEqual "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="
  }
}
