package com.scalafi.openbook.orderbook

import scalaz.stream._
import scalaz.concurrent.Task
import com.scalafi.openbook.{Side, OpenBookMsg}
import scala.concurrent.duration.FiniteDuration

object TimeSensitiveSet {

  def apply(symbol: String, orders: Process[Task, OpenBookMsg])(implicit config: TimeSensitiveSet.Config): TimeSensitiveSet =
    apply(BasicSet(symbol, orders))

  def apply(basicSet: BasicSet)(implicit config: TimeSensitiveSet.Config): TimeSensitiveSet =
    new TimeSensitiveSet(basicSet)

  trait Config extends BasicSet.Config {
    def duration: FiniteDuration
  }

  object Config {
    implicit val default = new Config {
      import scala.concurrent.duration._
      val duration: FiniteDuration = 1.second
      val orderBookDepth: Int = BasicSet.Config.default.orderBookDepth
    }
  }
}

class TimeSensitiveSet private[orderbook](basicSet: BasicSet)(implicit config: TimeSensitiveSet.Config) {

  case class OrdersTrail(orders: Vector[OpenBookMsg])

  import scalaz.syntax.applicative._
  import scalaz.std.option._

  private[orderbook] val ordersTrail = basicSet.orders.scan(Vector.empty[OpenBookMsg]) {
    (acc, order) =>
      val sourceTimeBound = order.sourceTime - config.duration.toMillis
      acc.dropWhile(_.sourceTime < sourceTimeBound) :+ order
  } map OrdersTrail


  private[orderbook] type Extractor[T] = OrderBook => Int => Option[T]

  private[orderbook] def spread(orderBook: OrderBook)(i: Int, l: Extractor[Int], r: Extractor[Int]): Option[Int] = {
    ^(l(orderBook)(i), r(orderBook)(i))((lv, rv) => lv - rv)
  }

  private[orderbook] def arrivalRate(f: OpenBookMsg => Boolean): Process[Task, Option[Double]] =
    ordersTrail map { trail =>
        ^(trail.orders.headOption, trail.orders.lastOption) {
          case (head, last) =>
            val diff = last.sourceTime - head.sourceTime
            if (diff == 0) None else Some(trail.orders.count(f).toDouble / diff)
        }.flatten
    }

  def bidArrivalRate: Process[Task, Option[Double]] =
    arrivalRate(_.side == Side.Buy)

  def askArrivalRate: Process[Task, Option[Double]] =
    arrivalRate(_.side == Side.Sell)
}