package com.scalafi.openbook.orderbook

import scalaz.concurrent.Task
import scalaz.stream._

object BasicSet {

  private[this] def askPrice(orderBook: OrderBook)(i: Int): Option[Int] = {
    orderBook.sell.keySet.drop(i - 1).headOption
  }

  private[this] def bidPrice(orderBook: OrderBook)(i: Int): Option[Int] = {
    val bidPrices = orderBook.buy.keySet
    if (bidPrices.size >= i) {
      bidPrices.drop(bidPrices.size - i).headOption
    } else None
  }

  def askPrice(orderBooks: Process[Task, OrderBook]) (i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    orderBooks.map(askPrice(_)(i))
  }

  def bidPrice(orderBooks: Process[Task, OrderBook])(i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    orderBooks.map(bidPrice(_)(i))
  }

  def askVolume(orderBooks: Process[Task, OrderBook])(i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    orderBooks.map(orderBook => askPrice(orderBook)(i).map(orderBook.sell))
  }

  def bidVolume(orderBooks: Process[Task, OrderBook])(i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    orderBooks.map(orderBook => bidPrice(orderBook)(i).map(orderBook.buy))
  }
}
