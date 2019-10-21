package me.herzrasen.stash.json

import me.herzrasen.stash.domain.User
import me.herzrasen.stash.json.RoleProtocol._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object UserProtocol extends DefaultJsonProtocol {

  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User.apply)

}
