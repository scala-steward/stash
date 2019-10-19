package me.herzrasen.stash.domain

import me.herzrasen.stash.domain.Roles._

case class User(id: Int, name: String, password: Array[Byte], role: Role)

object User {

  def createTableStatement: String =
    """CREATE TABLE IF NOT EXISTS stash_user (
    | id SERIAL PRIMARY KEY,
    | name VARCHAR NOT NULL UNIQUE,
    | password BYTEA NOT NULL,
    | role VARCHAR(16) NOT NULL
  )""".stripMargin

  def dropTableStatement: String =
    "DROP TABLE IF EXISTS stash_user"
}
