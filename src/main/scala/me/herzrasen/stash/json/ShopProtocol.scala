package me.herzrasen.stash.json

import me.herzrasen.stash.domain.Shop
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object ShopProtocol extends DefaultJsonProtocol {

  implicit val shopFormat: RootJsonFormat[Shop] = jsonFormat2(Shop.apply)

}
