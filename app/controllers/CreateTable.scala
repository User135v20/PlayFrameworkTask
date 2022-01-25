package controllers

import com.typesafe.scalalogging.Logger
import io.circe._
import models._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import java.sql
import java.text.SimpleDateFormat
import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.io.Source
import doobie.implicits._

class CreateTable @Inject()(
                             val AT: ArticleTable,
                             val CT: CategoryTable,
                             protected val dbConfigProvider: DatabaseConfigProvider,
                             cc: ControllerComponents)(implicit ec: ExecutionContext
                           ) extends AbstractController(cc)
  with HasDatabaseConfigProvider[JdbcProfile] {

  private val logger = Logger("Wiki service")

  import dbConfig.profile.api._

  private val DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"

  val wikiTable = TableQuery[AT.WikiTable]
  val categoryTable = TableQuery[CT.CategoryTable]

  def checkDB(name: String): Boolean = {
    val tables = Await.result(db.run(MTable.getTables), Duration.Inf).toList
    tables.exists(_.name.name == name)
  }

  def createDB(): Unit = {
    val setup = (categoryTable.schema ++ wikiTable.schema).create
    Await.result(db.run(setup), Duration.Inf)
  }
  /*
    def add = Action.async( parse.json(userReads) ) { request =>
      val results = userRepo.insert(  UserData( None, request.body.name, request.body.note ) )
      results.map(_ => Ok("done") )*/
  /*def addToDB(map: Map[String, String]): AT.Article = {

    AT.Article(
      convertToDate(map(AT.CREATETIMESTAMP)),
      convertToDate(map(AT.TIMESTAMP)),
      map(AT.LANGUAGE),
      map(AT.CATEGORY),
      map(AT.WIKI),
      map(AT.TITLE),
      map(AT.AUXILIARYTEXT)
    )
  }*/
  def addToDB(map: Map[String, String]): Unit = {
    val setup = wikiTable += AT.Article(
      convertToDate(map(AT.CREATETIMESTAMP)),
      convertToDate(map(AT.TIMESTAMP)),
      map(AT.LANGUAGE),
      map(AT.CATEGORY),
      map(AT.WIKI),
      map(AT.TITLE),
      map(AT.AUXILIARYTEXT)
    )
    Await.result(db.run(setup), Duration.Inf)
  }

  def addToCategoryTable(newCategory: String): Unit = {

    val transaction =
      categoryTable.filter(_.category === newCategory.trim).exists.result.flatMap { exists =>
        if (!exists) {
          categoryTable += newCategory.trim
        } else {
          DBIO.successful(None)
        }
      }.transactionally
    Await.result(db.run(transaction), Duration.Inf)
  }

  def splitCategoryString(map: Map[String, String]): List[String] = {
    map(AT.CATEGORY).split(',').toList
  }

  def addCategoryList(list: List[String]): Unit = {
    list match {
      case Nil => logger.debug("create category list")
      case List(x) => addToCategoryTable(x)
      case x :: xs =>
        addToCategoryTable(x)
        addCategoryList(xs)
    }
  }

  def dropArticleDB(): Unit = Await.result(db.run(wikiTable.schema.drop), Duration.Inf)

  def dropCategoryDB(): Unit = Await.result(db.run(categoryTable.schema.drop), Duration.Inf)

  def convertToDate(dateString: String): java.sql.Date = {
    val formatter = new SimpleDateFormat(DATEFORMAT)
    //formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    new sql.Date(formatter.parse(dateString).getTime)
  }

  def takingValueFromJson(json: Json, key: String): String =
    json.\\(key).head.toString
      .replaceAll("[\"\\[\\]\n]", "")
      .trim

  def parseArticle(dataString: String): Map[String, String] = {

    val parseResult: Either[ParsingFailure, Json] = parser.parse(dataString)
    parseResult match {
      case Left(parsingError) =>
        throw new IllegalArgumentException(s"Invalid JSON object: ${parsingError.message}")
      case Right(json) =>
        Map(
          AT.CREATETIMESTAMP -> takingValueFromJson(json, AT.CREATETIMESTAMP),
          AT.TIMESTAMP -> takingValueFromJson(json, AT.TIMESTAMP),
          AT.LANGUAGE -> takingValueFromJson(json, AT.LANGUAGE),
          AT.WIKI -> takingValueFromJson(json, AT.WIKI),
          AT.CATEGORY -> takingValueFromJson(json, AT.CATEGORY),
          AT.TITLE -> takingValueFromJson(json, AT.TITLE),
          AT.AUXILIARYTEXT -> takingValueFromJson(json, AT.AUXILIARYTEXT)
        )
    }
  }

  def create(): Action[AnyContent] = {
    //val wikiData: Iterator[String] = Source.fromResource("enwikiquote-20220110-cirrussearch-general.json").getLines()
    val wikiData: Iterator[String] = Source.fromResource("ruwikiquote-20211220-cirrussearch-general.json").getLines()

    val dataIterator: Iterator[String] =
      wikiData.filter(
        x =>
          x.contains(AT.CREATETIMESTAMP) &&
            x.contains(AT.TIMESTAMP) &&
            x.contains(AT.LANGUAGE) &&
            x.contains(AT.WIKI) &&
            x.contains(AT.CATEGORY) &&
            x.contains(AT.TITLE) &&
            x.contains(AT.AUXILIARYTEXT)
      )

    Action {
      if (checkDB(AT.NAMETABLEARTICLES)) dropArticleDB()
      if (checkDB(CT.NAMETABLECATEGORY)) dropCategoryDB()
      createDB()


      for (elem <- dataIterator) {
        addCategoryList(splitCategoryString(parseArticle(elem)))
        addToDB(parseArticle(elem))
      }
      db.close()
      Ok(views.html.SimplePage("Tables was created"))
    }
  }
}
