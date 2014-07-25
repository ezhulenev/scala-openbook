package com.scalafi.openbook.orderbook

import scala.language.implicitConversions

/*
case class Feature[T](name: String, value: T)

object FeatureSet {
  implicit def basicSet(orderBook: OrderBook) =
    new BasicSet(orderBook)

  implicit def timeInsensitiveSet(orderBook: OrderBook) =
    new TimeInsensitiveSet(orderBook)

  implicit def timeSensitiveSet(orderBook: OrderBook) =
    new TimeSensitiveSet(orderBook)

  private[orderbook] implicit object PriceM extends scalaz.Monoid[Option[Int]] {

    val zero: Option[Int] = Some(0)

    def append(f1: Option[Int], f2: => Option[Int]) = (f1, f2) match {
      case (Some(v1), Some(v2)) => Some(v1 + v2)
      case _ => None
    }
  }
}

class BasicSet(val orderBook: OrderBook) extends AnyVal {

  def askPrice(i: Int): Option[Feature[Int]] = {
    orderBook.sell.keySet.drop(i - 1).headOption.map(Feature(s"askP_$i", _))
  }

  def bidPrice(i: Int): Option[Feature[Int]] = {
    val bidPrices = orderBook.buy.keySet
    if (bidPrices.size >= i) {
      Some(Feature(s"bidP_$i", bidPrices.drop(bidPrices.size - i).head))
    } else None
  }

  def askVolume(i: Int): Option[Feature[Int]] =
    askPrice(i).map(p => Feature(s"askV_$i", orderBook.sell(p.value)))


  def bidVolume(i: Int): Option[Feature[Int]] =
    bidPrice(i).map(p => Feature(s"bidV_$i", orderBook.buy(p.value)))
}

class TimeInsensitiveSet(val orderBook: OrderBook) extends AnyVal {

  import FeatureSet._

  import scalaz.syntax.applicative._
  import scalaz.std.option._

  type FeatureF[T] = OrderBook => Int => Option[Feature[T]]

  def priceSpread(i: Int): Option[Feature[Int]] =
    spread(i, s"priceSpread_$i", _.askPrice, _.bidPrice)

  def volumeSpread(i: Int): Option[Feature[Int]] =
    spread(i, s"volumeSpread_$i", _.askPrice, _.bidPrice)

  def midPrice(i: Int): Option[Feature[Int]] =
    ^(orderBook.askPrice(i), orderBook.bidPrice(i)) {
      (ask, bid) => Feature(s"midPrice_$i", (ask.value + bid.value) / 2)
    }

  def bidStep(i: Int): Option[Feature[Int]] =
    ^(orderBook.bidPrice(i), orderBook.bidPrice(i + 1)) {
      (bid1, bid2) => Feature(s"bidStep_$i", math.abs(bid1.value - bid2.value))
    }

  def askStep(i: Int): Option[Feature[Int]] =
    ^(orderBook.askPrice(i), orderBook.askPrice(i + 1)) {
      (ask1, ask2) => Feature(s"askStep_$i", math.abs(ask1.value - ask2.value))
    }

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
}

class TimeSensitiveSet(val orderBook: OrderBook) extends AnyVal {

  def askArrivalRate(i: Int): Option[Feature[Double]] = {
    orderBook.trail.headOption.map {
      head =>
        val dMillis = head.sourceT.millis
        val orders = orderBook.trail.count(_.order.side == Side.Sell)
        Feature(s"askArrivalRate_$i", orders.toDouble / dMillis)
    }
  }

  def bidArrivalRate(i: Int): Option[Feature[Double]] = {
    orderBook.trail.headOption.map {
      head =>
        val dMillis = head.sourceT.millis
        val orders = orderBook.trail.count(_.order.side == Side.Buy)
        Feature(s"bidArrivalRate_$i", orders.toDouble / dMillis)
    }
  }
}*/
