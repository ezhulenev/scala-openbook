package com.scalafi.openbook.orderbook

import scala.collection.immutable.TreeMap
import com.scalafi.openbook.{Side, OpenBookMsg}
import scala.concurrent.duration._

case class OrderBook(symbol: String,
                     d: FiniteDuration = 1.second,
                     buy: TreeMap[Int, Int] = TreeMap.empty,
                     sell: TreeMap[Int, Int] = TreeMap.empty,
                     orders: Vector[OpenBookMsg] = Vector.empty) {

  private def recentOrders(order: OpenBookMsg): Vector[OpenBookMsg] = {
    val MicrosInMillis = 1000

    def orderT(o: OpenBookMsg): Long =
      o.sourceTime * MicrosInMillis + o.sourceTimeMicroSecs

    val lastT = orderT(order)

    orders.dropWhile(o => (lastT - orderT(o)) > d.toMicros) :+ order
  }

  def update(order: OpenBookMsg): OrderBook = {
    assume(order.symbol == symbol, s"Unexpected order symbol: ${order.symbol}. In Order Book for: $symbol")

    order match {
      case _ if order.side == Side.Buy & order.volume > 0 =>
        copy(
          buy = buy + (order.priceNumerator -> order.volume),
          orders = recentOrders(order)
        )

      case _ if order.side == Side.Buy & order.volume == 0 =>
        copy(
          buy = buy - order.priceNumerator,
          orders = recentOrders(order)
        )

      case _ if order.side == Side.Sell & order.volume > 0 =>
        copy(
          sell = sell + (order.priceNumerator -> order.volume),
          orders = recentOrders(order)
        )

      case _ if order.side == Side.Sell & order.volume == 0 =>
        copy(
          sell = sell - order.priceNumerator,
          orders = recentOrders(order)
        )

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