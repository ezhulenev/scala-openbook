package com.scalafi.openbook.orderbook

import scala.collection.immutable.TreeMap
import com.scalafi.openbook.{Side, OpenBookMsg}


object OrderBook {

  def empty(symbol: String): OrderBook = new OrderBook(symbol)

  /**
   * Time series of OrderBook for a stream of order messages for a single symbol
   */
  def fromOrders(symbol: String, orders: Iterator[OpenBookMsg]): Iterator[OrderBook] = {
    orders.
      filter(_.symbol == symbol). // filter by symbol
      scanLeft(OrderBook.empty(symbol))((ob, o) => ob.update(o)). // update orderbook
      drop(1) // drop empty order book used for initialization
  }

  /**
   * Time series of OrderBooks for a stream of order messages for a multiple symbols.
   * The iterator will always return the order book corresponding to the most recent message.
   */
  def fromOrders(orders: Iterator[OpenBookMsg]): Iterator[OrderBook] = {
    val orderBooks = collection.mutable.Map.empty[String, OrderBook]
    orders map { order =>
      // Get OrderBook for current symbol
      val orderBook = orderBooks.getOrElseUpdate(order.symbol, OrderBook.empty(order.symbol))

      // Update OrderBook state with new order
      val updated = orderBook.update(order)
      orderBooks.update(order.symbol, updated)

      // Return updated state
      updated
    }
  }
}

case class OrderBook(symbol: String,
    buy: TreeMap[Int, Int] = TreeMap.empty,
    sell: TreeMap[Int, Int] = TreeMap.empty,
    lastMsg : OpenBookMsg = null
    ) {

  def update(order: OpenBookMsg): OrderBook = {
    assume(order.symbol == symbol, s"Unexpected order symbol: ${order.symbol}. In Order Book for: $symbol")

    order match {
      case _ if order.side == Side.Buy & order.volume > 0 =>
        copy(buy = buy + (order.priceNumerator -> order.volume), lastMsg = order)

      case _ if order.side == Side.Buy & order.volume == 0 =>
        copy(buy = buy - order.priceNumerator, lastMsg = order)

      case _ if order.side == Side.Sell & order.volume > 0 =>
        copy(sell = sell + (order.priceNumerator -> order.volume), lastMsg = order)

      case _ if order.side == Side.Sell & order.volume == 0 =>
        copy(sell = sell - order.priceNumerator, lastMsg = order)

      case _ if order.side == Side.NA => copy(lastMsg = order)
    }
  }

  def printOrderBook(depth: Int): String = {

      val bid = buy.keySet.drop(buy.size - depth).map(price => s"$price : ${buy(price)}");
      val ask = sell.keySet.take(depth).map(price => s"$price : ${sell(price)}");

      s"""|Bid
      |${bid.mkString(System.lineSeparator())}
      |- - - - - - - - - -
      |Ask
      |${ask.mkString(System.lineSeparator())}
      |""".stripMargin.trim
  }
}