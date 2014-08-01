package com.scalafi.openbook.orderbook

import scalaz.concurrent.Task
import scalaz.stream._
import com.scalafi.openbook.OpenBookMsg

object TimeInsensitiveSet {

  def apply(symbol: String, orders: Process[Task, OpenBookMsg])(implicit config: TimeInsensitiveSet.Config): TimeInsensitiveSet =
    apply(BasicSet(symbol, orders))

  def apply(basicSet: BasicSet)(implicit config: TimeInsensitiveSet.Config): TimeInsensitiveSet =
    new TimeInsensitiveSet(basicSet)

  type Config = BasicSet.Config
}

class TimeInsensitiveSet private[orderbook](basicSet: BasicSet)(implicit config: TimeInsensitiveSet.Config) {

  import basicSet.orderBooks

  import scalaz.syntax.applicative._
  import scalaz.std.option._

  private[orderbook] type Extractor[T] = OrderBook => Int => Option[T]

  private[orderbook] def spread(orderBook: OrderBook)(i: Int, l: Extractor[Int], r: Extractor[Int]): Option[Int] = {
    ^(l(orderBook)(i), r(orderBook)(i))((lv, rv) => lv - rv)
  }

  private[orderbook] def mean(orderBook: OrderBook)(f: Extractor[Int]): Option[Double] = {
    val definedValues =
      (1 to config.orderBookDepth) map (i => f(orderBook)(i)) takeWhile(_.isDefined) map (_.get)

    if (!definedValues.isEmpty) {
      val sum = definedValues.sum.toDouble
      val n = definedValues.size

      Some(sum / n)
    } else None
  }

  private[orderbook] def acc(orderBook: OrderBook)(ask: Extractor[Int], bid: Extractor[Int]): Option[Int] = {
    val spreads =
      (1 to config.orderBookDepth) map(i => ^(ask(orderBook)(i), bid(orderBook)(i))((a, b) => a - b)) takeWhile(_.isDefined) map(_.get)

    if (spreads.isEmpty) None else Some(spreads.sum)
  }

  private def checkLevel[T](i: Int)(f: =>T): T = {
    config.checkLevel(i)
    f
  }

  def priceSpread(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    import basicSet.askPrice
    import basicSet.bidPrice
    orderBooks.map(ob => spread(ob)(i, askPrice, bidPrice))
  }

  def volumeSpread(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    import basicSet.askVolume
    import basicSet.bidVolume
    orderBooks.map(ob => spread(ob)(i, askVolume, bidVolume))
  }

  def midPrice(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    import basicSet.askPrice
    import basicSet.bidPrice
    orderBooks.map(ob => ^(askPrice(ob)(i), bidPrice(ob)(i))((ask, bid) => (ask + bid) / 2))
  }

  def bidStep(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    import basicSet.bidPrice
    orderBooks.map(ob => ^(bidPrice(ob)(i), bidPrice(ob)(i+1))((bid1, bid2) => math.abs(bid1 - bid2)))
  }

  def askStep(i: Int): Process[Task, Option[Int]] = checkLevel(i) {
    import basicSet.askPrice
    orderBooks.map(ob => ^(askPrice(ob)(i), askPrice(ob)(i+1))((ask1, ask2) => math.abs(ask1 - ask2)))
  }

  def meanAsk: Process[Task, Option[Double]] = {
    import basicSet.askPrice
    orderBooks.map(ob => mean(ob)(askPrice))
  }

  def meanBid: Process[Task, Option[Double]] = {
    import basicSet.bidPrice
    orderBooks.map(ob => mean(ob)(bidPrice))
  }

  def meanAskVolume: Process[Task, Option[Double]] = {
    import basicSet.askVolume
    orderBooks.map(ob => mean(ob)(askVolume))
  }

  def meanBidVolume: Process[Task, Option[Double]] = {
    import basicSet.bidVolume
    orderBooks.map(ob => mean(ob)(bidVolume))
  }

  def accumulatedPriceSpread: Process[Task, Option[Int]] = {
    import basicSet.askPrice
    import basicSet.bidPrice
    orderBooks.map(ob => acc(ob)(askPrice, bidPrice))
  }

  def accumulatedVolumeSpread: Process[Task, Option[Int]] = {
    import basicSet.askVolume
    import basicSet.bidVolume
    orderBooks.map(ob => acc(ob)(askVolume, bidVolume))
  }
}
