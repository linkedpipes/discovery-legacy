import org.apache.jena.rdf.model.Model
import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import services.RdfUtils

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Jena" should {
    "diff graphs" in {

      val data = """
        | <http://helmich.cz> <http://helmich.cz/relation/fake> [
        |   a <http://helmich.cz/resource/Person>
        | ] .
      """.stripMargin

      val g1 = RdfUtils.modelFromTtl(data)
      val g2 = RdfUtils.modelFromTtl(data)

      //g1.difference(g2).isEmpty mustBe true FALSE

      g2.isIsomorphicWith(g1) mustBe true
    }
  }

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HomeController" should {

    "render the index page" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include ("Your new application is ready.")
    }

  }

  "CountController" should {

    "return an increasing count" in {
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "0"
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "1"
      contentAsString(route(app, FakeRequest(GET, "/count")).get) mustBe "2"
    }

  }

}
