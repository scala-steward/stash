package me.herzrasen.stash.repository

import java.sql.{Connection, DriverManager}

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import io.getquill.context.monix.Runner
import io.getquill.{PostgresMonixJdbcContext, SnakeCase}
import me.herzrasen.stash.domain.Shop

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PostgresShopRepositoryTest
    extends AnyFlatSpec
    with Matchers
    with ForAllTestContainer {

  val databaseName: String = "stash-test"
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

  def dropTable(): Unit = {
    val dropTable = connection.prepareStatement(Shop.dropTableStatement)
    dropTable.execute()
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

  "A Shop" should "be inserted" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: ShopRepository = new PostgresShopRepository()

    repository.createTable()

    val foo = Await.result(repository.create(Shop(0, "Foo")), Duration.Inf)
    foo.id should not equal 0
    foo.name shouldEqual "Foo"
  }

  it should "be found" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: ShopRepository = new PostgresShopRepository()

    repository.createTable()

    val foo = Await.result(repository.create(Shop(0, "Foo")), Duration.Inf)

    val other = Await.result(repository.find(foo.id), Duration.Inf)

    other shouldBe defined
    other.get shouldEqual foo
  }

  it should "be deleted" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: ShopRepository = new PostgresShopRepository()

    repository.createTable()

    val foo = Await.result(repository.create(Shop(0, "Foo")), Duration.Inf)

    Await.result(repository.delete(foo), Duration.Inf)

    val shops = Await.result(repository.findAll(), Duration.Inf)

    shops shouldBe empty
  }

  "Searching for a non-existant Shop" should "not cause failure" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: ShopRepository = new PostgresShopRepository()

    repository.createTable()

    val shop = Await.result(repository.find(9), Duration.Inf)
    shop shouldEqual None
  }

  "All Shops" should "be found" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: ShopRepository = new PostgresShopRepository()

    repository.createTable()

    Await.result(
      Future.sequence(
        List(
          repository.create(Shop(0, "Foo")),
          repository.create(Shop(0, "Bar")),
          repository.create(Shop(0, "Baz"))
        )
      ),
      Duration.Inf
    )

    val shops = Await.result(repository.findAll(), Duration.Inf)
    shops should have size 3

    shops.find(_.name == "Foo") shouldBe defined
    shops.find(_.name == "Bar") shouldBe defined
    shops.find(_.name == "Baz") shouldBe defined
  }

}
