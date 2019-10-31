package me.herzrasen.stash.domain

case class Item(
    id: Int,
    name: String,
    quantityId: Int,
    inStock: Int,
    warnAt: Int,
    shopId: Int
)

object Item extends CreateTableSupport with DropTableSupport {

  def createTableStatement: String =
    """CREATE TABLE IF NOT EXISTS item (
      |  id SERIAL PRIMARY KEY,
      |  name TEXT NOT NULL,
      |  quantity_id INT NOT NULL REFERENCES quantity(id),
      |  in_stock NUMERIC NOT NULL,
      |  warn_at NUMERIC,
      |  shop_id INT NOT NULL REFERENCES shop(id)
      |)""".stripMargin

  def dropTableStatement: String =
    "DROP TABLE IF EXISTS item"
}
