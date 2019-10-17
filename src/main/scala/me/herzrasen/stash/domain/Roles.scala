package me.herzrasen.stash.domain

case object Roles {
  sealed trait Role
  case object Admin extends Role
  case object User extends Role
}
