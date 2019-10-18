package me.herzrasen.stash.domain

case object Roles {
  sealed trait Role
  case object Admin extends Role
  case object User extends Role
  case object Unknown extends Role

  def parse(str: String): Role =
    str match {
      case "admin" => Admin
      case "user"  => User
      case _       => Unknown
    }
}
