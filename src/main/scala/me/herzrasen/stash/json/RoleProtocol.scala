package me.herzrasen.stash.json
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

import me.herzrasen.stash.domain.Roles._
import spray.json.JsValue
import spray.json.JsString

object RoleProtocol extends DefaultJsonProtocol {

  implicit object RoleFormat extends RootJsonFormat[Role] {

    def write(role: Role): JsValue =
      JsString(role.mkString)

    def read(json: JsValue): Role =
      json match {
        case JsString(value) => parse(value)
        case _               => Unknown
      }
  }
}
