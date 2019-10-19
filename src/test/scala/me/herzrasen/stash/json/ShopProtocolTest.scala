package me.herzrasen.stash.json
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import me.herzrasen.stash.domain.Shop

import me.herzrasen.stash.json.ShopProtocol._
import spray.json._

class ShopProtocolTest extends FlatSpec with Matchers {

  "Serializing / deserializing" should "be correct" in {
    val shop = Shop(42, "My Shop")
    val json = shop.toJson.prettyPrint

    val fromJson = json.parseJson.convertTo[Shop]
    fromJson shouldEqual shop
  }
}
