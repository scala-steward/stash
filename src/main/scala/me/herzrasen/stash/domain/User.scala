package me.herzrasen.stash.domain

import me.herzrasen.stash.domain.Roles._

case class User(id: Int, name: String, password: String, role: Role)
