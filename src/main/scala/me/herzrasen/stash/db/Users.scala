package me.herzrasen.stash.db
import slick.jdbc.PostgresProfile.api._
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles.Role
import me.herzrasen.stash.domain.Roles

class Users(tag: Tag) extends Table[User](tag, "user") {

  implicit val roleTypeMapper = MappedColumnType.base[Role, String]({
    case Roles.Admin   => "admin"
    case Roles.User    => "user"
    case Roles.Unknown => "unknown"
  }, { str =>
    Roles.parse(str)
  })

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.Unique)
  def password = column[String]("password")
  def role = column[Role]("role")

  def * = (id, name, password, role).mapTo[User]
}
