package me.herzrasen.stash.repository
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.dimafeng.testcontainers.ForAllTestContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import java.sql.DriverManager
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import io.getquill.PostgresMonixJdbcContext
import io.getquill.SnakeCase
import io.getquill.context.monix.Runner
import me.herzrasen.stash.domain.Shop
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import java.sql.Connection

import scala.concurrent.ExecutionContext.Implicits.global

class PostgresShopRepositoryTest
    extends FlatSpec
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
    println(s"$shop")
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
