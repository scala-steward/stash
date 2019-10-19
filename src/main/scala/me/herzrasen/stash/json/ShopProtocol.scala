package me.herzrasen.stash.json
import spray.json.DefaultJsonProtocol
import spray.json.RootJsonFormat

import me.herzrasen.stash.domain.Shop

object ShopProtocol extends DefaultJsonProtocol {

  implicit val shopFormat: RootJsonFormat[Shop] = jsonFormat2(Shop.apply)

}
