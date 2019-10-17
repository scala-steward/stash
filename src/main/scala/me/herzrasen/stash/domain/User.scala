package me.herzrasen.stash.domain

import me.herzrasen.stash.domain.Roles._

case class User(id: Long, name: String, password: String, role: Role)
