package me.herzrasen.stash.json

import me.herzrasen.stash.domain.User
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object UserProtocol extends DefaultJsonProtocol {

  import RoleProtocol._

  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User.apply)

}
