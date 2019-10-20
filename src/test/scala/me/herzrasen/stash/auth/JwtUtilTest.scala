package me.herzrasen.stash.auth
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles
import com.auth0.jwt.JWT

class JwtUtilTest extends FlatSpec with Matchers {

  "A Token" should "be created" in {
    val user = User(42, "Test", "mysecret123", Roles.Admin)
    val token = JwtUtil.create(user)

    val decoded = JWT.decode(token)
    decoded.getIssuer() shouldEqual "stash"
    decoded.getClaim("role").asString shouldEqual "admin"
    decoded.getClaim("user").asString shouldEqual "Test"
  }
}
