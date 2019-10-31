package me.herzrasen.stash.repository

import me.herzrasen.stash.domain.Item

trait ItemRepository extends StashRepository[Item, Int] with CreateTable