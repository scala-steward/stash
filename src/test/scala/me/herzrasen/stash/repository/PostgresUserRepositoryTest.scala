package me.herzrasen.stash.repository

import java.sql.{Connection, DriverManager}

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import com.typesafe.config.{Config, ConfigFactory}
import io.getquill.context.monix.Runner
import io.getquill.{PostgresMonixJdbcContext, SnakeCase}
import me.herzrasen.stash.auth.JwtUtil
import me.herzrasen.stash.domain.Roles.{
  Unknown,
  Admin => AdminRole,
  User => UserRole
}
import me.herzrasen.stash.domain.{Roles, User}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PostgresUserRepositoryTest
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

  "An initial Admin user" should "be created" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    val adminPassword =
      Await.result(repository.initializeAdminUser(), Duration.Inf)
    val adminUser = Await.result(repository.find("admin"), Duration.Inf)

    adminUser shouldBe defined
    adminPassword shouldBe defined
    adminUser.get.password shouldEqual JwtUtil.hash(adminPassword.get)
  }

  it should "not create an user when one exists" in {
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
      repository.create(
        User(0, "root", JwtUtil.hash("root's password"), Roles.Admin)
      ),
      Duration.Inf
    )

    Await.result(repository.initializeAdminUser(), Duration.Inf) shouldEqual None
  }

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

  "The password" should "be updated" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()
    repository.createTable()

    val user = Await.result(
      repository.create(User(0, "test", JwtUtil.hash("test123"), Roles.User)),
      Duration.Inf
    )

    val newPassword = "foobar"

    Await.result(repository.updatePassword(user, JwtUtil.hash(newPassword)), Duration.Inf)

    val updatedUser = Await.result(repository.find(user.id), Duration.Inf)

    updatedUser shouldBe defined
    updatedUser.get.password shouldEqual JwtUtil.hash(newPassword)
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

    val user = Await.result(repository.find(9), Duration.Inf)
    user shouldEqual None
  }

  "Searching for a non-existant User by name" should "return None" in {
    dropTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: UserRepository = new PostgresUserRepository()

    repository.createTable()

    val user = Await.result(repository.find("not a real user"), Duration.Inf)
    user shouldEqual None
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
