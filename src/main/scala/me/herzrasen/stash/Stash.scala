package me.herzrasen.stash
import com.typesafe.scalalogging.StrictLogging

import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import me.herzrasen.stash.db.Users
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles.{User => UserRole}

object Stash extends App with StrictLogging {
  logger.info("Stash server starting...")

  import scala.concurrent.ExecutionContext.Implicits.global

  val users = TableQuery[Users]

  val db = Database.forConfig("postgres")
  try {
    Await.result(
      db.run(
        DBIO.seq(
          users.schema.createIfNotExists,
          users += User(0, "Jessica", "fooooooo", UserRole),
          users.result.map(println)
        )
      ),
      Duration.Inf
    )
  } finally db.close()
}
