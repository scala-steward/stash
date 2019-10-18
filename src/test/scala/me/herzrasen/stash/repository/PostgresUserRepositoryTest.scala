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
import me.herzrasen.stash.domain.User
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles.{Admin => AdminRole, User => UserRole}

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

  def createTable(): Unit = {
    Class.forName(container.driverClassName)
    val connection = DriverManager.getConnection(
      container.jdbcUrl,
      container.username,
      container.password
    )

    val createTableStatement: String =
      "CREATE TABLE stash_user (id SERIAL PRIMARY KEY, name VARCHAR UNIQUE, password VARCHAR, role VARCHAR(12))"

    val createTable = connection.prepareStatement(createTableStatement)
    createTable.execute()
    ()
  }

  def dropTable(): Unit = {
    Class.forName(container.driverClassName)
    val connection = DriverManager.getConnection(
      container.jdbcUrl,
      container.username,
      container.password
    )

    val dropTableStatement: String =
      "DROP TABLE IF EXISTS stash_user"

    val dropTable = connection.prepareStatement(dropTableStatement)
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
    createTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

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

  it should "be found" in {
    dropTable()
    createTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    val aUser = Await.result(
      repository.create(User(0, "A User", "auser", UserRole)),
      Duration.Inf
    )

    val other = Await.result(repository.find(aUser.id), Duration.Inf)

    other shouldBe defined
    other.get shouldEqual aUser
  }

  it should "be deleted" in {
    dropTable()
    createTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    val foo = Await.result(
      repository.create(User(0, "A User", "auser", UserRole)),
      Duration.Inf
    )

    Await.result(repository.delete(foo), Duration.Inf)

    val Users = Await.result(repository.findAll(), Duration.Inf)

    Users shouldBe empty
  }

  "Searching for a non-existant User" should "not cause failure" in {
    dropTable()
    createTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    val User = Await.result(repository.find(9), Duration.Inf)
    println(s"$User")
  }

  "All Users" should "be found" in {
    dropTable()
    createTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    import scala.concurrent.ExecutionContext.Implicits.global

    val repository: UserRepository = new PostgresUserRepository()

    Await.result(
      Future.sequence(
        List(
          repository.create(User(0, "A User", "auser", UserRole)),
          repository.create(User(0, "An Admin", "anadmin", AdminRole))
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
