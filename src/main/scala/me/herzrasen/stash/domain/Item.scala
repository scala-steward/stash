package me.herzrasen.stash.domain

case class Item(
    id: Int,
    name: String,
    shopId: Int,
    quantityId: Int,
    inStock: Float,
    warnAt: Float
)

object Item extends CreateTableSupport with DropTableSupport {

  def createTableStatement: String =
    """CREATE TABLE IF NOT EXISTS item (
      |  id SERIAL PRIMARY KEY,
      |  name TEXT NOT NULL,
      |  shop_id INT NOT NULL REFERENCES shop(id),
      |  quantity_id INT NOT NULL REFERENCES quantity(id),
      |  in_stock NUMERIC NOT NULL,
      |  warn_at NUMERIC
      |)""".stripMargin

  def dropTableStatement: String =
    "DROP TABLE IF EXISTS item"
}
