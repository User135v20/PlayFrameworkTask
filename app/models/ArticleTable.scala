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
class ArticleTable @Inject()(val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.

  import dbConfig._
  import profile.api._


  val NAMETABLEARTICLES = "ArticleTable"
  val CREATETIMESTAMP = "create_timestamp"
  val TIMESTAMP = "timestamp"
  val LANGUAGE = "language"
  val WIKI = "wiki"
  val CATEGORY = "category"
  val TITLE = "title"
  val AUXILIARYTEXT = "auxiliary_text"

  case class Article(
                      create_timestamp: java.sql.Date,
                      timestamp: java.sql.Date,
                      language: String,
                      wiki: String,
                      category: String,
                      title: String,
                      auxiliary_text: String)

  class WikiTable(tag: Tag) extends Table[Article](tag, Some("public"), NAMETABLEARTICLES) {

    def create_timestamp = column[java.sql.Date](CREATETIMESTAMP)

    def timestamp = column[java.sql.Date](TIMESTAMP)

    def language = column[String](LANGUAGE)

    def category = column[String](CATEGORY)

    def wiki = column[String](WIKI)

    def title = column[String](TITLE)

    def auxiliary_text = column[String](AUXILIARYTEXT)

    override def * = (create_timestamp, timestamp,
      language, category, wiki, title, auxiliary_text) <> ((Article.apply _).tupled, Article.unapply)
  }
  val wikiTable = TableQuery[WikiTable]
}