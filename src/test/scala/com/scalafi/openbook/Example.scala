package com.scalafi.openbook

import com.scalafi.openbook.orderbook._

object Example extends App {

  import scalaz.std.option._
  import scalaz.syntax.applicative._

  implicit val codec = io.Codec.ISO8859

  val ticker = "APL"
  val source = this.getClass.getResource("/openbookultraAA_N20130403_1_of_1").getPath

  val orders = OpenBookMsg.read(source).
    filter(_.symbol == ticker)

  val basicSet = BasicSet(ticker, orders)
  val timeInsensitiveSet = TimeInsensitiveSet(ticker, orders)

  val ask1 = basicSet.askPrice(1)
  val bid1 = basicSet.bidPrice(1)
  val spread1 = timeInsensitiveSet.priceSpread(1)

  val features = ((ask1 zip bid1) zipWith spread1)((ab, s) => (ab._1, ab._2, s))
  
  features.runLog.run.foreach {
    case (ask, bid, spread) =>
      println(s"Ask: $ask. Bid: $bid. Spread: $spread")
      ^^(ask, bid, spread)((a, b, s) => assume(a - b == s))
  }
}
