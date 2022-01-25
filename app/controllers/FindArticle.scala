package controllers

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.mvc.{AbstractController, ControllerComponents}
import slick.jdbc.JdbcProfile
import models.ArticleTable
import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class FindArticle @Inject()(val AT: ArticleTable,
                            protected val dbConfigProvider: DatabaseConfigProvider,
                            cc: ControllerComponents)(
                             implicit ec: ExecutionContext
                           ) extends AbstractController(cc)
  with HasDatabaseConfigProvider[JdbcProfile] {

  import dbConfig.profile.api._
  val wikiTable = TableQuery[AT.WikiTable]

  def search(title: String) = {
    val name = title.replace("_", " ").toLowerCase
    val articleSearch = Await.result(db.run(wikiTable.filter(_.title.toLowerCase === name).result), Duration.Inf)
    if (articleSearch.nonEmpty) {
      val fArticle = articleSearch.head
      Action {
        Ok(views.html.findArticles
          .render(fArticle.create_timestamp.toString,
            fArticle.timestamp.getTime,
            fArticle.language,
            fArticle.category,
            fArticle.wiki,
            fArticle.title,
            fArticle.auxiliary_text
          )
        )
      }
    } else Action {
      Ok(views.html.SimplePage("Article not found"))
    }
  }
}

