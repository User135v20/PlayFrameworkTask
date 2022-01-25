package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{Future, ExecutionContext}

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class CategoryTable @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

  import dbConfig._
  import profile.api._

  val NAMETABLECATEGORY = "CategoryTable"
  val CATEGORY = "category"

  case class Category(category: String)

  class CategoryTable(tag: Tag) extends Table[String](tag, Some("public"), NAMETABLECATEGORY) {
    def category = column[String](CATEGORY)

    def * = (category)
  }

  val categoryTable = TableQuery[CategoryTable]
}