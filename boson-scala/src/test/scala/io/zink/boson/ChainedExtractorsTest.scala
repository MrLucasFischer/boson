package io.zink.boson

import bsonLib.{BsonArray, BsonObject}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import org.junit.Assert.{assertArrayEquals, assertEquals, assertTrue}


case class Book(title: String, price: Double, edition: Int, forSale: Boolean, nPages: Long)
case class Book1(title: String, price: Double)

@RunWith(classOf[JUnitRunner])
class ChainedExtractorsTest extends FunSuite{

  private val _book1 = new BsonObject().put("Title", "Scala").put("Price", 25.6).put("Edition",10).put("ForSale", true).put("nPages", 750L)
  private val _store = new BsonObject().put("Book", _book1)
  private val _bson = new BsonObject().put("Store", _store)

  test("Extract Type class Book") {
    val expression: String = ".Store.Book"
    val boson: Boson = Boson.extractor(expression, (in: Book) => {
      assertEquals(Book("Scala",25.6,10,true,750L), in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Type class Book as byte[]") {
    val expression: String = ".Store.Book"
    val boson: Boson = Boson.extractor(expression, (in: Array[Byte]) => {
      assertArrayEquals(_book1.encodeToBarray(), in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Seq[Type class Book]") {

    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5)
    val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books)
    val bson = new BsonObject().put("Store", store)

      val expression: String = ".Store.Book[0 to 1]"
      val boson: Boson = Boson.extractor(expression, (in: Seq[Book1]) => {
        assertEquals(Seq(Book1("Java",15.5),Book1("Scala",21.5)), in)
        println("APPLIED")
      })
      val res = boson.go(bson.encode.getBytes)
      Await.result(res, Duration.Inf)
    }

  test("Extract Seq[Type class Book] as Seq[byte[]]") {

    val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6)
    val title2 = new BsonObject().put("Title", "Scala").put("Price", 21.5)
    val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5)
    val books = new BsonArray().add(title1).add(title2).add(title3)
    val store = new BsonObject().put("Book", books)
    val bson = new BsonObject().put("Store", store)

    val expression: String = ".Store.Book[0 to 1]"
    val expected: Seq[Array[Byte]] = Seq(title1.encodeToBarray(),title2.encodeToBarray())
    val boson: Boson = Boson.extractor(expression, (in: Seq[Array[Byte]]) => {
      assert(expected.size === in.size)
      assertTrue(expected.zip(in).forall(b => b._1.sameElements(b._2)))
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Long") {
    val expression: String = ".Store.Book.nPages"
    val boson: Boson = Boson.extractor(expression, (in: Long) => {
      assertEquals(750L, in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Boolean") {
    val expression: String = ".Store.Book.ForSale"
    val boson: Boson = Boson.extractor(expression, (in: Boolean) => {
      assertEquals(true, in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Int") {
    val expression: String = ".Store.Book.Edition"
    val boson: Boson = Boson.extractor(expression, (in: Int) => {
      assertEquals(10, in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract Double") {
    val expression: String = ".Store.Book.Price"
    val boson: Boson = Boson.extractor(expression, (in: Double) => {
      assert(25.6 === in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract String") {
    val expression: String = ".Store.Book.Title"
    val boson: Boson = Boson.extractor(expression, (in: String) => {
      assertEquals("Scala", in)
      println("APPLIED")
    })
    val res = boson.go(_bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract simple Seq[Boolean]") {
    val arr = new BsonArray().add(true).add(true).add(false)

    val expression: String = ".[all]"
    val boson: Boson = Boson.extractor(expression, (in: Seq[Boolean]) => {
      assertEquals(Seq(true,true,false), in)
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract simple Seq[Int]") {
    val arr = new BsonArray().add(1).add(2).add(3)

    val expression: String = ".[all]"
    val boson: Boson = Boson.extractor(expression, (in: Seq[Int]) => {
      assertEquals(Seq(1,2,3), in)
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract simple Seq[String]") {
    val arr = new BsonArray().add("one").add("two").add("three")

    val expression: String = ".[all]"
    val boson: Boson = Boson.extractor(expression, (in: Seq[String]) => {
      assertEquals(Seq("one","two","three"), in)
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract simple Seq[Long]") {
    val arr = new BsonArray().add(1000L).add(1001L).add(1002L)

    val expression: String = ".[all]"
    val boson: Boson = Boson.extractor(expression, (in:  Seq[Long]) => {
      assertEquals(Seq(1000L,1001L,1002L), in)
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract simple Seq[Double]") {
    val arr = new BsonArray().add(1.1).add(2.2).add(3.3)

    val expression: String = ".[all]"
    val boson: Boson = Boson.extractor(expression, (in: Seq[Double]) => {
      assertEquals(Seq(1.1,2.2,3.3), in)
      println("APPLIED")
    })
    val res = boson.go(arr.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

    private val hat3 = new BsonObject().put("Price", 38).put("Color", "Blue")
    private val hat2 = new BsonObject().put("Price", 35).put("Color", "White")
    private val hat1 = new BsonObject().put("Price", 48).put("Color", "Red")
    private val hats = new BsonArray().add(hat1).add(hat2).add(hat3)
    private val edition3 = new BsonObject().put("Title", "C++Machine").put("Price", 38)
    private val sEditions3 = new BsonArray().add(edition3)
    private val title3 = new BsonObject().put("Title", "C++").put("Price", 12.6).put("SpecialEditions", sEditions3)
    private val edition2 = new BsonObject().put("Title", "ScalaMachine").put("Price", 40)
    private val sEditions2 = new BsonArray().add(edition2)
    private val title2 = new BsonObject().put("Title", "Scala").put("Pri", 21.5).put("SpecialEditions", sEditions2)
    private val edition1 = new BsonObject().put("Title", "JavaMachine").put("Price", 39)
    private val sEditions1 = new BsonArray().add(edition1)
    private val title1 = new BsonObject().put("Title", "Java").put("Price", 15.5).put("SpecialEditions", sEditions1)
    private val books = new BsonArray().add(title1).add(title2).add(title3)
    private val store = new BsonObject().put("Book", books).put("Hat", hats)
    private val bson = new BsonObject().put("Store", store)

  test("Extract byte[]") {

    val obj = new BsonObject().put("byte[]","Scala".getBytes)

  }

  test("Extract complex Seq[String]") {
    val expression: String = ".Store.Book[1 to end].Title"
    val boson: Boson = Boson.extractor(expression, (in: Seq[String]) => {
      assertEquals(Seq("Scala","C++"), in)
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract complex Seq[Int]") {
    val expression: String = ".Store.Book[0 until end].SpecialEditions[all].Price"
    val boson: Boson = Boson.extractor(expression, (in: Seq[Int]) => {
      assertEquals(Seq(39,40), in)
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }

  test("Extract complex Seq[Double]") {
    val expression: String = ".Store.Book[1 to 3].Price"
    val boson: Boson = Boson.extractor(expression, (in: Seq[Double]) => {
      assertEquals(Seq(12.6), in)
      println("APPLIED")
    })
    val res = boson.go(bson.encode.getBytes)
    Await.result(res, Duration.Inf)
  }


}