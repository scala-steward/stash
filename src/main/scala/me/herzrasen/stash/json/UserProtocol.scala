package me.herzrasen.stash.json
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

import me.herzrasen.stash.domain.User

object UserProtocol extends DefaultJsonProtocol {

  import RoleProtocol._

  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User.apply)

}
