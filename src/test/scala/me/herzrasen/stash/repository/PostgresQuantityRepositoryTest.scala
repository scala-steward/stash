package me.herzrasen.stash.repository

import java.sql.{Connection, DriverManager}

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import io.getquill.{PostgresMonixJdbcContext, SnakeCase}
import io.getquill.context.monix.Runner
import me.herzrasen.stash.domain.Quantity
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

import scala.concurrent.ExecutionContext.Implicits.global

class PostgresQuantityRepositoryTest
    extends FlatSpec
    with Matchers
    with ForAllTestContainer {

  val databaseName: String = "stash-quantity-test"
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
    val dropTable = connection.prepareStatement(Quantity.dropTableStatement)
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

  "A Quantity" should "be inserted" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: QuantityRepository = new PostgresQuantityRepository()

    repository.createTable()

    val foo = Await.result(
      repository.create(Quantity(0, "Foobar", Some("Foo"))),
      Duration.Inf
    )
    foo.id should not equal 0
    foo.name shouldEqual "Foobar"
    foo.abbreviation shouldEqual Some("Foo")
  }

  it should "be found" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: QuantityRepository = new PostgresQuantityRepository()

    repository.createTable()

    val foo =
      Await.result(repository.create(Quantity(0, "Foo", None)), Duration.Inf)

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

    val repository: QuantityRepository = new PostgresQuantityRepository()

    repository.createTable()

    val foo = Await.result(
      repository.create(Quantity(0, "Foobar", Some("Foo"))),
      Duration.Inf
    )

    Await.result(repository.delete(foo), Duration.Inf)

    val shops = Await.result(repository.findAll(), Duration.Inf)

    shops shouldBe empty
  }

  "Searching for a non-existant Quantity" should "not cause failure" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: QuantityRepository = new PostgresQuantityRepository()

    repository.createTable()

    val quantity = Await.result(repository.find(9), Duration.Inf)
    quantity shouldEqual None
  }

  "All Quantities" should "be found" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: QuantityRepository = new PostgresQuantityRepository()

    repository.createTable()

    Await.result(
      Future.sequence(
        List(
          repository.create(Quantity(0, "Foo", None)),
          repository.create(Quantity(0, "Bar", None)),
          repository.create(Quantity(0, "Baz", None))
        )
      ),
      Duration.Inf
    )

    val quantities = Await.result(repository.findAll(), Duration.Inf)
    quantities should have size 3

    quantities.find(_.name == "Foo") shouldBe defined
    quantities.find(_.name == "Bar") shouldBe defined
    quantities.find(_.name == "Baz") shouldBe defined
  }

}
