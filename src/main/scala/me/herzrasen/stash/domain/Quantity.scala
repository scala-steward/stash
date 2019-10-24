package me.herzrasen.stash.domain

case class Quantity(id: Int, name: String, abbreviation: Option[String])

object Quantity {

  def createTableStatement: String =
    """CREATE TABLE IF NOT EXISTS quantity (
      | id SERIAL PRIMARY KEY,
      | name VARCHAR(255) NOT NULL UNIQUE,
      | abbreviation VARCHAR(128)
    )""".stripMargin

  def dropTableStatement: String =
    "DROP TABLE IF EXISTS quantity"

}
