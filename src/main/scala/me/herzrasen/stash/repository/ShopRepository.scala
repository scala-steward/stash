package me.herzrasen.stash.repository

import me.herzrasen.stash.domain.Shop

trait ShopRepository extends StashRepository[Shop, Int] with CreateTable
