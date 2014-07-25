package com.scalafi.openbook.orderbook

import scala.collection.immutable.TreeMap
import com.scalafi.openbook.{Side, OpenBookMsg}
import scala.concurrent.duration._


case class SourceT(sourceTime: Int, sourceTimeMicroSecs: Int) {
  val MicrosInMillis = 1000l

  def micros: Long =
    sourceTime * MicrosInMillis + sourceTimeMicroSecs

  def millis: Long =
    sourceTime * sourceTimeMicroSecs / MicrosInMillis
}

case class OrderBook(symbol: String,
                     d: FiniteDuration = 1.second,
                     buy: TreeMap[Int, Int] = TreeMap.empty,
                     sell: TreeMap[Int, Int] = TreeMap.empty,
                     sourceT: SourceT = SourceT(0, 0),
                     order: OpenBookMsg = null,
                     trail: Vector[OrderBook] = Vector.empty) {

  private implicit class RichOpenBookMsg(msg: OpenBookMsg) {
    def sourceT = SourceT(msg.sourceTime, msg.sourceTimeMicroSecs)
  }

  // Build new OrderBook trail & clean transitive trail history
  private def trail(order: OpenBookMsg): Vector[OrderBook] = {
    val lastT = order.sourceT.micros
    trail.dropWhile(o => (lastT - o.sourceT.micros) > d.toMicros).map(_.copy(trail = Vector.empty)) :+ this
  }

  def update(order: OpenBookMsg): OrderBook = {
    assume(order.symbol == symbol, s"Unexpected order symbol: ${order.symbol}. In Order Book for: $symbol")

    order match {
      case _ if order.side == Side.Buy & order.volume > 0 =>
        copy(
          buy = buy + (order.priceNumerator -> order.volume),
          sourceT = SourceT(order.sourceTime, order.sourceTimeMicroSecs),
          order = order,
          trail = trail(order)
        )

      case _ if order.side == Side.Buy & order.volume == 0 =>
        copy(
          buy = buy - order.priceNumerator,
          sourceT = order.sourceT,
          order = order,
          trail = trail(order)
        )

      case _ if order.side == Side.Sell & order.volume > 0 =>
        copy(
          sell = sell + (order.priceNumerator -> order.volume),
          sourceT = order.sourceT,
          order = order,
          trail = trail(order)
        )

      case _ if order.side == Side.Sell & order.volume == 0 =>
        copy(
          sell = sell - order.priceNumerator,
          sourceT = order.sourceT,
          order = order,
          trail = trail(order)
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