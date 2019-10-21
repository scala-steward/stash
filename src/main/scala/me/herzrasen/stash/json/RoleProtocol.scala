package me.herzrasen.stash.json

import me.herzrasen.stash.domain.Roles._
import spray.json._

object RoleProtocol extends DefaultJsonProtocol {

  implicit object RoleFormat extends RootJsonFormat[Role] {

    def write(role: Role): JsValue =
      JsString(role.mkString)

    def read(json: JsValue): Role =
      json match {
        case JsString(value) => parse(value)
        case _ => Unknown
      }
  }
}
