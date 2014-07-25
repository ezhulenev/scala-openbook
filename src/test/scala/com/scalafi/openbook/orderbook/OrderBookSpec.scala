package com.scalafi.openbook.orderbook

import org.scalatest.{GivenWhenThen, FlatSpec}
import com.scalafi.openbook._
import scalaz.concurrent.Task

class OrderBookSpec extends FlatSpec with GivenWhenThen {

  "OrderBook" should "build correct order book from order flow" in {

    Given("three orders")
    val order1 = orderMsg(0, 0, 10000, 10, Side.Buy)
    val order2 = orderMsg(100, 0, 10000, 15, Side.Buy)
    val order3 = orderMsg(200, 0, 11000, 20, Side.Sell)

    Then("correct order books should be constructed")
    val orderBook = OrderBook(Symbol).update(order1).update(order2).update(order3)

    assert(orderBook.buy.size == 1)
    assert(orderBook.sell.size == 1)

    Given("next order")
    val order4 = orderMsg(1001, 0, 12000, 20, Side.Sell)

    Then("first order should be evicted by time")
    val orderBookUpd = orderBook.update(order4)

    assert(orderBookUpd.buy.size == 1)
    assert(orderBookUpd.sell.size == 2)
  }

  it should "build valid stream of order books" in {

    import scalaz.stream._

    Given("three orders")
    val order1 = orderMsg(0, 0, 10000, 10, Side.Buy)
    val order2 = orderMsg(100, 0, 10000, 15, Side.Buy)
    val order3 = orderMsg(200, 0, 11000, 20, Side.Sell)

    val orders = Process.emitSeq[Task, OpenBookMsg](Seq(order1, order2, order3))

    Then("stream of three order books should be created")
    val orderBooks = orders.zipWith(OrderBook.fromOrders(Symbol, orders))((o, ob) => (o, ob))

    val trail = orderBooks.runLog.run

    assert(trail.size == 3)

    assert(trail(0)._1 == order1)
    assert(trail(1)._1 == order2)
    assert(trail(2)._1 == order3)
  }
}