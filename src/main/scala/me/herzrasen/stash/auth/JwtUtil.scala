package me.herzrasen.stash.auth

import java.security.MessageDigest
import java.time.ZonedDateTime
import java.util.{Base64, Date}

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import me.herzrasen.stash.domain.{Roles, User}

object JwtUtil {

  private val validDays: Long = 7

  def create(user: User): String = {
    val algorithm = Algorithm.HMAC256(user.password)
    JWT
      .create()
      .withIssuer("stash")
      .withExpiresAt(
        Date.from(ZonedDateTime.now.plusDays(validDays).toInstant())
      )
      .withClaim("user", user.name)
      .withClaim("role", user.role.mkString())
      .sign(algorithm)
  }

  def isExpired(jwt: String): Boolean =
    Option(JWT.decode(jwt).getExpiresAt) match {
      case Some(expiredAt) =>
        expiredAt.toInstant().isBefore(ZonedDateTime.now().toInstant())
      case None => false
    }

  def role(jwt: String): Roles.Role =
    Option(JWT.decode(jwt).getClaim("role").asString) match {
      case Some(role) => Roles.parse(role)
      case None => Roles.Unknown
    }

  def user(jwt: String): Option[String] =
    Option(JWT.decode(jwt).getClaim("user")).map(_.asString)

  def hash(password: String): String = {
    val bytes =
      MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(bytes)
  }

}
