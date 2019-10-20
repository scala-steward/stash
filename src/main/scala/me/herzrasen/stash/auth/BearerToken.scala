package me.herzrasen.stash.auth

case class BearerToken(bearer: String) {

  val token: Option[String] =
    if (bearer.startsWith("Bearer "))
      Some(bearer.replace("Bearer ", ""))
    else None

}
