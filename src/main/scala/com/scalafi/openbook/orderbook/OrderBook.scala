package com.scalafi.openbook.orderbook

import scala.collection.immutable.TreeMap
import com.scalafi.openbook.{Side, OpenBookMsg}
import scalaz.concurrent.Task

object OrderBook {

  import scalaz.stream.Process

  def fromOrders(symbol: String, orders: Process[Task, OpenBookMsg]): Process[Task, OrderBook] =
    orders.scan(OrderBook(symbol)) {
      (ob, order) => ob.update(order)
    }
}

case class OrderBook(symbol: String,
                     buy: TreeMap[Int, Int] = TreeMap.empty,
                     sell: TreeMap[Int, Int] = TreeMap.empty) {

  def update(order: OpenBookMsg): OrderBook = {
    assume(order.symbol == symbol, s"Unexpected order symbol: ${order.symbol}. In Order Book for: $symbol")

    order match {
      case _ if order.side == Side.Buy & order.volume > 0 =>
        copy(buy = buy + (order.priceNumerator -> order.volume))

      case _ if order.side == Side.Buy & order.volume == 0 =>
        copy(buy = buy - order.priceNumerator)

      case _ if order.side == Side.Sell & order.volume > 0 =>
        copy(sell = sell + (order.priceNumerator -> order.volume))

      case _ if order.side == Side.Sell & order.volume == 0 =>
        copy(sell = sell - order.priceNumerator)

      case _ if order.side == Side.NA => this
    }
  }

  def printOrderBook(depth: Int): String = {

    val bid = buy.keySet.drop(buy.size - depth).map(price => s"$price : ${buy(price)}")
    val ask = sell.keySet.take(depth).map(price => s"$price : ${sell(price)}")

    s"""|Bid
        |${bid.mkString(System.lineSeparator())}
        |- - - - - - - - - -
        |Ask
        |${ask.mkString(System.lineSeparator())}
        |""".stripMargin.trim
  }
}