package me.herzrasen.stash.json

import me.herzrasen.stash.domain.Roles.{Role, Unknown}
import me.herzrasen.stash.domain.{
  NewQuantity,
  NewUser,
  Quantity,
  Roles,
  Shop,
  User
}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}

trait JsonSupport extends DefaultJsonProtocol {

  implicit object RoleFormat extends RootJsonFormat[Role] {

    def write(role: Role): JsValue =
      JsString(role.mkString())

    def read(json: JsValue): Role =
      json match {
        case JsString(value) => Roles.parse(value)
        case _ => Unknown
      }
  }

  implicit val shopFormat: RootJsonFormat[Shop] = jsonFormat2(Shop.apply)

  implicit val quantityFormat: RootJsonFormat[Quantity] = jsonFormat3(
    Quantity.apply
  )

  implicit val newQuantityFormat: RootJsonFormat[NewQuantity] = jsonFormat2(
    NewQuantity.apply
  )

  implicit val newUserFormat: RootJsonFormat[NewUser] = jsonFormat2(
    NewUser.apply
  )

  implicit val userFormat: RootJsonFormat[User] = jsonFormat4(User.apply)

}

object JsonSupport extends JsonSupport
