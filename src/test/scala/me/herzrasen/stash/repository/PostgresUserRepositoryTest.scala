package me.herzrasen.stash.repository

import java.sql.{Connection, DriverManager}

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import io.getquill.context.monix.Runner
import io.getquill.{PostgresMonixJdbcContext, SnakeCase}
import me.herzrasen.stash.domain.Roles.{Unknown, Admin => AdminRole, User => UserRole}
import me.herzrasen.stash.domain.User
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class PostgresUserRepositoryTest
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
    val dropTable = connection.prepareStatement(User.dropTableStatement)
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

  "A User" should "be inserted" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    val foo = Await.result(
      repository.create(
        User(0, "A User", "apassword", UserRole)
      ),
      Duration.Inf
    )
    foo.id should not equal 0
    foo.name shouldEqual "A User"
    foo.password shouldEqual "apassword"
    foo.role shouldEqual UserRole
  }

  it should "be found by id" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    val aUser = Await.result(
      repository.create(User(0, "A User", "auser", UserRole)),
      Duration.Inf
    )

    val other = Await.result(repository.find(aUser.id), Duration.Inf)

    other shouldBe defined
    other.get.name shouldEqual aUser.name
    other.get.password.sameElements(aUser.password) shouldBe true
  }

  it should "be found by name" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    val aUser = Await.result(
      repository.create(User(0, "A User", "auser", UserRole)),
      Duration.Inf
    )

    val other = Await.result(repository.find(aUser.name), Duration.Inf)

    other shouldBe defined
    other.get.name shouldEqual aUser.name
    other.get.password.sameElements(aUser.password) shouldBe true
  }

  it should "be deleted" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    val foo = Await.result(
      repository.create(User(0, "A User", "auser", Unknown)),
      Duration.Inf
    )

    Await.result(repository.delete(foo), Duration.Inf)

    val Users = Await.result(repository.findAll(), Duration.Inf)

    Users shouldBe empty
  }

  "Searching for a non-existant User by id" should "not cause failure" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    val User = Await.result(repository.find(9), Duration.Inf)
    println(s"$User")
  }

  "Searching for a non-existant User by name" should "not cause failure" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    val User = Await.result(repository.find("not a real user"), Duration.Inf)
    println(s"$User")
  }

  "All Users" should "be found" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    Await.result(
      Future.sequence(
        List(
          repository.create(User(0, "A User", "auser", UserRole)),
          repository.create(
            User(0, "An Admin", "anadmin", AdminRole)
          )
        )
      ),
      Duration.Inf
    )

    val Users = Await.result(repository.findAll(), Duration.Inf)
    Users should have size 2

    Users.find(_.name == "A User") shouldBe defined
    Users.find(_.name == "An Admin") shouldBe defined
  }

}
