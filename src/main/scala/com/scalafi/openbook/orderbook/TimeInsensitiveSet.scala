package com.scalafi.openbook.orderbook

import scalaz.concurrent.Task
import scalaz.stream._

object TimeInsensitiveSet {

  import scalaz.syntax.applicative._
  import scalaz.std.option._

  private[orderbook] type Extractor[T] = OrderBook => Int => Option[T]

  private[orderbook] def spread(orderBook: OrderBook)(i: Int, l: Extractor[Int], r: Extractor[Int]) = {
    ^(l(orderBook)(i), r(orderBook)(i))((lv, rv) => lv - rv)
  }

  def priceSpread(orderBooks: Process[Task, OrderBook])(i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    import BasicSet.askPrice
    import BasicSet.bidPrice
    orderBooks.map(ob => spread(ob)(i, askPrice, bidPrice))
  }

  def volumeSpread(orderBooks: Process[Task, OrderBook])(i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    import BasicSet.askVolume
    import BasicSet.bidVolume
    orderBooks.map(ob => spread(ob)(i, askVolume, bidVolume))
  }

  def midPrice(orderBooks: Process[Task, OrderBook])(i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    import BasicSet.askPrice
    import BasicSet.bidPrice
    orderBooks.map(ob => ^(askPrice(ob)(i), bidPrice(ob)(i))((ask, bid) => (ask + bid) / 2))
  }

  def bidStep(orderBooks: Process[Task, OrderBook])(i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    import BasicSet.bidPrice
    orderBooks.map(ob => ^(bidPrice(ob)(i), bidPrice(ob)(i+1))((bid1, bid2) => math.abs(bid1 - bid2)))
  }

  def askStep(orderBooks: Process[Task, OrderBook])(i: Int): Process[Task, Option[Int]] = {
    assume(i > 0, s"Level index should be greater then 0")
    import BasicSet.askPrice
    orderBooks.map(ob => ^(askPrice(ob)(i), askPrice(ob)(i+1))((ask1, ask2) => math.abs(ask1 - ask2)))
  }

  /*

  def meanAsk(n: Int): Option[Feature[Int]] =
    mean(n, s"meanAsk_$n", _.askPrice)

  def meanBid(n: Int): Option[Feature[Int]] =
    mean(n, s"meanBid_$n", _.bidPrice)

  def meanAskVolume(n: Int): Option[Feature[Int]] =
    mean(n, s"meanAskVolume$n", _.askVolume)

  def meanBidVolume(n: Int): Option[Feature[Int]] =
    mean(n, s"meanBidVolume$n", _.bidVolume)

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
