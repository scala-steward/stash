package me.herzrasen.stash.domain

case class NewItem(
    name: String,
    shopId: Int,
    quantityId: Int,
    inStock: Float,
    warnAt: Float
)
