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

  private[orderbook] def spread(orderBook: OrderBook)(i: Int, l: Extractor[Int], r: Extractor[Int]) = {
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

  /*
  def accumulatedPriceSpread(n: Int): Option[Feature[Int]] =
    acc(n, s"accPriceSpread_$n", _.priceSpread)

  def accumulatedVolumeSpread(n: Int): Option[Feature[Int]] =
    acc(n, s"accVolumeSpread_$n", _.priceSpread)

  private def spread(i: Int, name: String, l: FeatureF[Int], r: FeatureF[Int]) = {
    ^(l(orderBook)(i), r(orderBook)(i)) {
      (ask, bid) => Feature(name, ask.value - bid.value)
    }
  }

  private def mean(n: Int, name: String, f: FeatureF[Int]) =
    acc(n, name, f) map (feature => feature.copy(value = feature.value / n))

  private def acc(n: Int, name: String, f: FeatureF[Int]) = {
    import scalaz.syntax.foldable._
    import scalaz.std.indexedSeq._

    (1 to n).map(i => f(orderBook)(i)).map(_.map(_.value)).suml.map(Feature(name, _))
  }
   */

}
