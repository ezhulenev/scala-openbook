package com.scalafi.openbook

import org.scalatest.FlatSpec
import java.io.FileInputStream
import com.scalafi.openbook.orderbook.{BasicSet, OrderBook}

class OpenBookParserSpec extends FlatSpec {

  "OpenBook parser" should "parse all OpenBook Ultra messages" in {

    //val is = this.getClass.getResourceAsStream("/openbookultraAA_N20130403_1_of_1")

    val is = new FileInputStream("/Users/ezhulenev/workstuff/NYSE/EQY_US_NYSE_BOOK_20130403/openbookultraAA_N20130403_1_of_1")

    val messages = io.Source.fromInputStream(is)(io.Codec.ISO8859).
      map(_.toByte).grouped(69).map(_.toArray)

    val parsed = messages.map(OpenBookMsg.apply).filter(_.symbol == "APL").take(100)

    parsed.foldLeft(OrderBook("APL")) {
      (orderBook, order) =>

        println(orderBook.printOrderBook(10))
        println(new BasicSet(orderBook).bidPrice(1) + ", " + new BasicSet(orderBook).bidPrice(2))
        println(new BasicSet(orderBook).askPrice(1) + ", " + new BasicSet(orderBook).askPrice(2))
        println(order)


      orderBook.update(order)
    }

    //assert(parsed.size == 1000)
  }

}
