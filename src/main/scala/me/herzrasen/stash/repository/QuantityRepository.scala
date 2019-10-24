package me.herzrasen.stash.repository

import me.herzrasen.stash.domain.Quantity

trait QuantityRepository extends StashRepository[Quantity, Int] with CreateTable
