package com.scalafi.openbook.orderbook

case class Feature[T](name: String, value: T)

object FeatureSet {
  implicit def basicSet(orderBook: OrderBook) =
    new BasicSet(orderBook)
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