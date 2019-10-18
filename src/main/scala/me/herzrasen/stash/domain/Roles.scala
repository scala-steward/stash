package me.herzrasen.stash.domain

case object Roles {
  sealed trait Role {
    def mkString(): String
  }
  case object Admin extends Role {
    override def mkString(): String = "admin"
  }
  case object User extends Role {
    override def mkString(): String = "user"
  }
  case object Unknown extends Role {
    override def mkString(): String = "unknown"
  }

  def parse(str: String): Role =
    str match {
      case "admin" => Admin
      case "user"  => User
      case _       => Unknown
    }
}
