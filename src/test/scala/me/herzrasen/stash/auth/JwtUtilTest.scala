package me.herzrasen.stash.auth
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import java.time.ZonedDateTime

class JwtUtilTest extends FlatSpec with Matchers {

  "A Token" should "be created" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    val decoded = JWT.decode(token)
    decoded.getIssuer() shouldEqual "stash"
    decoded.getClaim("role").asString shouldEqual "admin"
    decoded.getClaim("user").asString shouldEqual "Test"
  }

  it should "have a valid role claim" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    JwtUtil.role(token) shouldEqual Some("admin")
  }

  it should "have a valid user claim" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    JwtUtil.user(token) shouldEqual Some("Test")
  }

  it should "not be expired" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    JwtUtil.isExpired(token) shouldBe false
  }

  it should "be expired" in {
    val token =
      JWT
        .create()
        .withExpiresAt(
          Date.from(ZonedDateTime.now().minusMinutes(1).toInstant())
        )
        .sign(Algorithm.HMAC256("test"))
    JwtUtil.isExpired(token) shouldBe true
  }

  it should "be never expire when no expiration is in token" in {
    val token =
      JWT
        .create()
        .sign(Algorithm.HMAC256("test"))
    JwtUtil.isExpired(token) shouldBe false
  }

  "A Hash" should "be generated" in {
    val hash = JwtUtil.hash("test")
    hash shouldEqual "n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg="
  }
}
