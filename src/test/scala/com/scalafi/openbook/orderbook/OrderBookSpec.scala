package com.scalafi.openbook.orderbook

import org.scalatest.{GivenWhenThen, FlatSpec}
import com.scalafi.openbook._

class OrderBookSpec extends FlatSpec with GivenWhenThen {

  "OrderBook" should "build correct order book from order flow" in {

    Given("three orders")
    val order1 = orderMsg(0, 0, 10000, 10, Side.Buy)
    val order2 = orderMsg(100, 0, 10000, 15, Side.Buy)
    val order3 = orderMsg(200, 0, 11000, 20, Side.Sell)

    Then("correct order books should be constructed")
    val orderBook = OrderBook(SYMBOL_APL).update(order1).update(order2).update(order3)

    assert(orderBook.buy.size == 1)
    assert(orderBook.sell.size == 1)

    Given("next order")
    val order4 = orderMsg(1001, 0, 12000, 20, Side.Sell)

    Then("first order should be evicted by time")
    val orderBookUpd = orderBook.update(order4)

    assert(orderBookUpd.buy.size == 1)
    assert(orderBookUpd.sell.size == 2)
  }

  it should "build valid stream of order books either for a single symbol " in {

    Given("three orders")
    val order1 = orderMsg(0, 0, 10000, 10, Side.Buy)
    val order2 = orderMsg(100, 0, 10000, 15, Side.Buy)
    val order3 = orderMsg(200, 0, 11000, 20, Side.Sell)

    val orders: Iterator[OpenBookMsg] = Seq(order1, order2, order3).iterator

    Then("stream of three order books should be created")
    val orderBooks = OrderBook.fromOrders(SYMBOL_APL, orders)

    assert(orderBooks.next().buy.get(order1.priceNumerator).get == order1.volume)
    assert(orderBooks.next().buy.get(order2.priceNumerator).get == order2.volume)
    assert(orderBooks.next().sell.get(order3.priceNumerator).get == order3.volume)
    
    assert(!orderBooks.hasNext)

  }
  
  it should "build valid stream of order books either a stream of messages corresponding to different symbols " in {
    val is = this.getClass.getResourceAsStream("/openbookultraAA_N20130403_1_of_1")
    val symbolsInMessages = OpenBookMsg.iterate(is).map(_.symbol).toSet
    val symbolsInOrderBooks = OrderBook.fromOrders(OpenBookMsg.iterate(is)).map(_.lastMsg.symbol).toSet

    assert(symbolsInMessages.intersect(symbolsInOrderBooks).size == symbolsInMessages.size)
  }
}