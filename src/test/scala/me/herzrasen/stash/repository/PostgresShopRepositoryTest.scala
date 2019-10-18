package me.herzrasen.stash.repository
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.dimafeng.testcontainers.ForAllTestContainer
import com.dimafeng.testcontainers.PostgreSQLContainer
import java.sql.DriverManager
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll
import io.getquill.PostgresMonixJdbcContext
import io.getquill.SnakeCase
import io.getquill.context.monix.Runner
import me.herzrasen.stash.domain.Shop
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future

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

  def createTable(): Unit = {
    Class.forName(container.driverClassName)
    val connection = DriverManager.getConnection(
      container.jdbcUrl,
      container.username,
      container.password
    )

    val createTableStatement: String =
      "CREATE TABLE shop (id SERIAL PRIMARY KEY, name VARCHAR UNIQUE)"

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
      "DROP TABLE IF EXISTS shop"

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

  "A Shop" should "be inserted" in {
    dropTable()
    createTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    val repository: ShopRepository = new PostgresShopRepository()

    val foo = Await.result(repository.create(Shop(0, "Foo")), Duration.Inf)
    foo.id should not equal 0
    foo.name shouldEqual "Foo"
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

    val repository: ShopRepository = new PostgresShopRepository()

    val foo = Await.result(repository.create(Shop(0, "Foo")), Duration.Inf)

    val other = Await.result(repository.find(foo.id), Duration.Inf)

    other shouldEqual foo
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

    val repository: ShopRepository = new PostgresShopRepository()

    val foo = Await.result(repository.create(Shop(0, "Foo")), Duration.Inf)

    Await.result(repository.delete(foo), Duration.Inf)

    val shops = Await.result(repository.findAll(), Duration.Inf)

    shops shouldBe empty
  }

  "All Shops" should "be found" in {
    dropTable()
    createTable()

    implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
      new PostgresMonixJdbcContext(
        SnakeCase,
        config.getConfig("postgres"),
        Runner.default
      )

    import scala.concurrent.ExecutionContext.Implicits.global

    val repository: ShopRepository = new PostgresShopRepository()

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
