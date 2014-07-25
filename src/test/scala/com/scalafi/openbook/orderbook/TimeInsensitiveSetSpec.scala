package com.scalafi.openbook.orderbook

import org.scalatest.FlatSpec
import com.scalafi.openbook.{OpenBookMsg, Side}
import scalaz.stream.Process
import scalaz.concurrent.Task

class TimeInsensitiveSetSpec extends FlatSpec {

  val order1 = orderMsg(0, 0, 10000, 10, Side.Buy)
  val order2 = orderMsg(100, 0, 10000, 15, Side.Buy)
  val order3 = orderMsg(200, 0, 11000, 20, Side.Sell)
  val order4 = orderMsg(200, 0, 12000, 30, Side.Sell)
  val order5 = orderMsg(200, 0, 14000, 40, Side.Sell)

  val orders = Process.emitSeq[Task, OpenBookMsg](Seq(order1, order2, order3, order4, order5))
  val orderBooks = OrderBook.fromOrders(Symbol, orders)

  "TimeInsensitiveSet features" should "build valid price spreads stream" in {
    val priceSpread1 = TimeInsensitiveSet.priceSpread(orderBooks)(1)
    val priceSpreads1 = priceSpread1.runLog.run

    assert(priceSpreads1.last == Some(1000))
  }

  it should "build valid volume spreads stream" in {
    val volumeSpread1 = TimeInsensitiveSet.volumeSpread(orderBooks)(1)
    val volumeSpreads1 = volumeSpread1.runLog.run

    assert(volumeSpreads1.last == Some(5))
  }

  it should "build valid mid price stream" in {
    val midPrice1 = TimeInsensitiveSet.midPrice(orderBooks)(1)
    val midPrices1 = midPrice1.runLog.run

    assert(midPrices1.last == Some(10500))
  }

  it should "build valid ask step stream" in {
    val askStep1 = TimeInsensitiveSet.askStep(orderBooks)(1)
    val askSteps1 = askStep1.runLog.run

    assert(askSteps1.last == Some(1000))
  }
}
