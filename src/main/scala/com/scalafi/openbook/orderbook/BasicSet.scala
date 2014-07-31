package com.scalafi.openbook.orderbook

import scalaz.concurrent.Task
import scalaz.stream._
import com.scalafi.openbook.OpenBookMsg

object BasicSet {

  def apply(symbol: String, orders: Process[Task, OpenBookMsg])(implicit config: BasicSet.Config): BasicSet =
    new BasicSet(symbol, orders)

  trait Config {
    def orderBookDepth: Int

    def checkLevel(i: Int) = {
      assume(i > 0, s"Level index should be greater then 0")
      assume(i <= orderBookDepth, s"Level index should be less then $orderBookDepth")
    }
  }

  object Config {
    implicit val default = new Config {
      val orderBookDepth = 10
    }
  }
}

class BasicSet private[orderbook](val symbol: String, val orders: Process[Task, OpenBookMsg])(implicit config: BasicSet.Config) {

  private[orderbook] val orderBooks = OrderBook.fromOrders(symbol, orders)

  private[orderbook] def askPrice(orderBook: OrderBook)(i: Int): Option[Int] = {
    orderBook.sell.keySet.drop(i - 1).headOption
  }

  private[orderbook] def askVolume(orderBook: OrderBook)(i: Int) = {
    askPrice(orderBook)(i).map(orderBook.sell)
  }

  private[orderbook] def bidPrice(orderBook: OrderBook)(i: Int): Option[Int] = {
    val bidPrices = orderBook.buy.keySet
    if (bidPrices.size >= i) {
      bidPrices.drop(bidPrices.size - i).headOption
    } else None
  }

  private[orderbook] def bidVolume(orderBook: OrderBook)(i: Int) = {
    bidPrice(orderBook)(i).map(orderBook.buy)
  }

  private def checkLevel[T](i: Int)(f: =>T): T = {
    config.checkLevel(i)
    f
  }

  def askPrice(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    orderBooks.map(askPrice(_)(i))
  }

  def bidPrice(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    orderBooks.map(bidPrice(_)(i))
  }

  def askVolume(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    orderBooks.map(askVolume(_)(i))
  }

  def bidVolume(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    orderBooks.map(bidVolume(_)(i))
  }
}