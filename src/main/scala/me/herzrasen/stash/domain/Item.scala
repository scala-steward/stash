package me.herzrasen.stash.domain

case class Item(
    id: Int,
    name: String,
    inStock: Int,
    warnAt: Int,
    shopId: Int
)
