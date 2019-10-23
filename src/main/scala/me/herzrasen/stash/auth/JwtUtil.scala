package me.herzrasen.stash.auth

import java.security.MessageDigest
import java.time.ZonedDateTime
import java.util.{Base64, Date}

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.{
  JWTDecodeException,
  SignatureVerificationException,
  TokenExpiredException
}
import com.auth0.jwt.interfaces.{Claim, DecodedJWT}
import me.herzrasen.stash.domain.{Roles, User}

object JwtUtil {

  private val validDays: Long = 7
  private val issuer: String = "stash"

  def create(user: User)(implicit hmacSecret: HmacSecret): String =
    JWT
      .create()
      .withIssuer(issuer)
      .withExpiresAt(
        Date.from(ZonedDateTime.now.plusDays(validDays).toInstant)
      )
      .withClaim("id", Integer.valueOf(user.id))
      .withClaim("user", user.name)
      .withClaim("role", user.role.mkString())
      .sign(Algorithm.HMAC256(hmacSecret.value))

  private def decode(
      jwt: String
  )(implicit hmacSecret: HmacSecret): Option[DecodedJWT] =
    try {
      val verifier =
        JWT
          .require(Algorithm.HMAC256(hmacSecret.value))
          .withIssuer(issuer)
          .build()
      Some(verifier.verify(jwt))
    } catch {
      case _: JWTDecodeException =>
        None
      case _: SignatureVerificationException =>
        None
      case _: TokenExpiredException =>
        None
    }

  private def extractClaimOr[T](
      jwt: String,
      claimName: String,
      f: Claim => T,
      default: T
  )(implicit hmacSecret: HmacSecret): T =
    decode(jwt) match {
      case Some(decoded) =>
        val claim = decoded.getClaim(claimName)
        if (claim.isNull) {
          default
        } else {
          f(claim)
        }
      case None =>
        default
    }

  def role(jwt: String)(implicit hmacSecret: HmacSecret): Roles.Role =
    extractClaimOr(jwt, "role", c => Roles.parse(c.asString), Roles.Unknown)

  def id(jwt: String)(implicit hmacSecret: HmacSecret): Option[Int] =
    extractClaimOr(jwt, "id", c => Some(Int.unbox(c.asInt)), None)

  def user(jwt: String)(implicit hmacSecret: HmacSecret): Option[String] =
    extractClaimOr(jwt, "user", c => Some(c.asString), None)

  def hash(password: String): String = {
    val bytes =
      MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(bytes)
  }

}
