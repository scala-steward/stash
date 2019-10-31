package me.herzrasen.stash.repository

import java.sql.{Connection, DriverManager}

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import io.getquill.context.monix.Runner
import io.getquill.{PostgresMonixJdbcContext, SnakeCase}
import me.herzrasen.stash.domain.{Item, Quantity, Shop}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PostgresItemRepositoryTest
    extends FlatSpec
    with Matchers
    with ForAllTestContainer {

  val databaseName: String = "stash-item-test"
  val username: String = "stash"
  val password: String = ""

  override val container = PostgreSQLContainer(
    databaseName = databaseName,
    username = username,
    password = password
  )

  lazy val connection: Connection = {
    Class.forName(container.driverClassName)
    DriverManager.getConnection(
      container.jdbcUrl,
      container.username,
      container.password
    )
  }

  def dropTables(): Unit = {
    connection.prepareStatement(Item.dropTableStatement).execute()
    connection.prepareStatement(Quantity.dropTableStatement).execute()
    connection.prepareStatement(Shop.dropTableStatement).execute()
    ()
  }

  lazy val config: Config = ConfigFactory.parseString(s"""
       |postgres {
       |  dataSourceClassName="org.postgresql.ds.PGSimpleDataSource"
       |  dataSource {
       |    databaseName=$databaseName
       |    portNumber=${container.mappedPort(5432)}
       |    serverName=${container.containerIpAddress}
       |    user=$username
       |  }
       |  connectionTimeout=30000
       | }""".stripMargin)

  val quantity = Quantity(0, "Quantity", Some("qty"))
  val shop = Shop(0, "My special store")

  "An Item" should "be inserted" in {
    dropTables()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val shopRepository = new PostgresShopRepository()
    val quantityRepository = new PostgresQuantityRepository()
    val repository = new PostgresItemRepository()

    shopRepository.createTable()
    quantityRepository.createTable()
    repository.createTable()

    val newShop = Await.result(shopRepository.create(shop), Duration.Inf)
    val newQuantity =
      Await.result(quantityRepository.create(quantity), Duration.Inf)

    val newItem = Await.result(
      repository.create(Item(0, "Testitem", newShop.id, newQuantity.id, 5, 1)),
      Duration.Inf
    )

    newItem.id should not equal 0
    newItem.shopId shouldEqual newShop.id
  }

  it should "be found" in {
    dropTables()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val shopRepository = new PostgresShopRepository()
    val quantityRepository = new PostgresQuantityRepository()
    val repository = new PostgresItemRepository()

    shopRepository.createTable()
    quantityRepository.createTable()
    repository.createTable()

    val newShop = Await.result(shopRepository.create(shop), Duration.Inf)
    val newQuantity =
      Await.result(quantityRepository.create(quantity), Duration.Inf)

    val newItem = Await.result(
      repository.create(Item(0, "Testitem", newShop.id, newQuantity.id, 5, 1)),
      Duration.Inf
    )

    val foundItem = Await.result(repository.find(newItem.id), Duration.Inf)
    foundItem shouldBe defined
    foundItem.get shouldEqual newItem
  }

  it should "be deleted" in {
    dropTables()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val shopRepository = new PostgresShopRepository()
    val quantityRepository = new PostgresQuantityRepository()
    val repository = new PostgresItemRepository()

    shopRepository.createTable()
    quantityRepository.createTable()
    repository.createTable()

    val newShop = Await.result(shopRepository.create(shop), Duration.Inf)
    val newQuantity =
      Await.result(quantityRepository.create(quantity), Duration.Inf)

    val newItem = Await.result(
      repository.create(Item(0, "Testitem", newShop.id, newQuantity.id, 5, 1)),
      Duration.Inf
    )

    Await.result(repository.delete(newItem), Duration.Inf)

    val foundItems = Await.result(repository.findAll(), Duration.Inf)

    foundItems should have size 0
  }

  "All Items" should "be found" in {
    dropTables()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val shopRepository = new PostgresShopRepository()
    val quantityRepository = new PostgresQuantityRepository()
    val repository = new PostgresItemRepository()

    shopRepository.createTable()
    quantityRepository.createTable()
    repository.createTable()

    val newShop = Await.result(shopRepository.create(shop), Duration.Inf)
    val newQuantity =
      Await.result(quantityRepository.create(quantity), Duration.Inf)

    Await.result(
      repository.create(
        Item(0, "Testitem", newShop.id, newQuantity.id, 5, 1)
      ),
      Duration.Inf
    )

    val items = Await.result(repository.findAll(), Duration.Inf)
    items should have size 1

    Await.result(
      repository.create(
        Item(0, "new testitem", newShop.id, newQuantity.id, 2, 1)
      ),
      Duration.Inf
    )

    val moreItems = Await.result(repository.findAll(), Duration.Inf)
    moreItems should have size 2
  }

}
