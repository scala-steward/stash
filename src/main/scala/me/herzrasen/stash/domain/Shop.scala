package me.herzrasen.stash.domain

case class Shop(id: Int, name: String)

object Shop {

  def createTableStatement: String =
    """CREATE TABLE IF NOT EXISTS shop (
      | id SERIAL PRIMARY KEY,
      | name VARCHAR(255) NOT NULL UNIQUE
    )""".stripMargin

  def dropTableStatement: String =
    "DROP TABLE IF EXISTS shop"
}
