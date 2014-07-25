package com.scalafi.openbook.orderbook

import org.scalatest.FlatSpec
import com.scalafi.openbook.{OpenBookMsg, Side}
import scalaz.stream.Process
import scalaz.concurrent.Task

class BasicSetSpec extends FlatSpec {

  val order1 = orderMsg(0, 0, 10000, 10, Side.Buy)
  val order2 = orderMsg(100, 0, 10000, 15, Side.Buy)
  val order3 = orderMsg(200, 0, 11000, 20, Side.Sell)

  val orders = Process.emitSeq[Task, OpenBookMsg](Seq(order1, order2, order3))
  val orderBooks = OrderBook.fromOrders(Symbol, orders)


  "BasicSet features" should "build valid bid prices stream" in {
  val bid1 = BasicSet.bidPrice(orderBooks)(1)
    val bid1Volume = BasicSet.bidVolume(orderBooks)(1)

    val bids1 = bid1.zipWith(bid1Volume)((p, v) => (p, v)).runLog.run
    assert(bids1.last == (Some(10000), Some(15)))
  }
}
