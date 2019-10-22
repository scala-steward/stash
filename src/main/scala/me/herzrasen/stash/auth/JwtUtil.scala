package me.herzrasen.stash.auth

import java.security.MessageDigest
import java.time.ZonedDateTime
import java.util.{Base64, Date}

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.{Claim, DecodedJWT}
import me.herzrasen.stash.domain.{Roles, User}

object JwtUtil {

  private val validDays: Long = 7

  def create(user: User): String = {
    val algorithm = Algorithm.HMAC256(user.password)
    JWT
      .create()
      .withIssuer("stash")
      .withExpiresAt(
        Date.from(ZonedDateTime.now.plusDays(validDays).toInstant)
      )
      .withClaim("id", Integer.valueOf(user.id))
      .withClaim("user", user.name)
      .withClaim("role", user.role.mkString())
      .sign(algorithm)
  }
  private def decode(jwt: String): Option[DecodedJWT] =
    try {
      Some(JWT.decode(jwt))
    } catch {
      case _: JWTDecodeException =>
        None
    }

  def isExpired(jwt: String): Boolean =
    decode(jwt) match {
      case Some(decoded) =>
        Option(decoded.getExpiresAt) match {
          case Some(expiresAt) =>
            expiresAt.toInstant.isBefore(ZonedDateTime.now.toInstant)
          case None =>
            false
        }
      case None => false
    }

  private def extractClaimOr[T](
      jwt: String,
      claimName: String,
      f: Claim => T,
      default: T
  ) = {
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
  }

  def role(jwt: String): Roles.Role =
    extractClaimOr(jwt, "role", c => Roles.parse(c.asString), Roles.Unknown)

  def id(jwt: String): Option[Int] =
    extractClaimOr(jwt, "id", c => Some(Int.unbox(c.asInt)), None)

  def user(jwt: String): Option[String] =
    extractClaimOr(jwt, "user", c => Some(c.asString), None)

  def hash(password: String): String = {
    val bytes =
      MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(bytes)
  }

}
