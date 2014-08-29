package com.scalafi.openbook.orderbook

import org.scalatest.FlatSpec
import com.scalafi.openbook.Side
import scalaz.stream.Process

class TimeInsensitiveSetSpec extends FlatSpec {

  val order1 = orderMsg(0, 0, 10000, 10, Side.Buy)
  val order2 = orderMsg(100, 0, 10000, 15, Side.Buy)
  val order3 = orderMsg(200, 0, 11000, 20, Side.Sell)
  val order4 = orderMsg(200, 0, 12000, 30, Side.Sell)
  val order5 = orderMsg(200, 0, 14000, 40, Side.Sell)

  val orders = Process.emitAll(Seq(order1, order2, order3, order4, order5))
  
  val timeInsensitiveSet = TimeInsensitiveSet(Symbol, orders)

  "TimeInsensitiveSet features" should "build valid price spreads stream" in {
    val priceSpread1 = timeInsensitiveSet.priceSpread(1)
    val priceSpreads1 = priceSpread1.runLog.run

    assert(priceSpreads1.last == Some(1000))
  }

  it should "build valid volume spreads stream" in {
    val volumeSpread1 = timeInsensitiveSet.volumeSpread(1)
    val volumeSpreads1 = volumeSpread1.runLog.run

    assert(volumeSpreads1.last == Some(5))
  }

  it should "build valid mid price stream" in {
    val midPrice1 = timeInsensitiveSet.midPrice(1)
    val midPrices1 = midPrice1.runLog.run

    assert(midPrices1.last == Some(10500))
  }

  it should "build valid ask step stream" in {
    val askStep1 = timeInsensitiveSet.askStep(1)
    val askSteps1 = askStep1.runLog.run

    assert(askSteps1.last == Some(1000))
  }

  it should "build valid mean ask price stream" in {
    val meanAsk = timeInsensitiveSet.meanAsk
    val meanAsks = meanAsk.runLog.run

    val expectedMean = (order3.priceNumerator.toDouble + order4.priceNumerator.toDouble + order5.priceNumerator.toDouble) / 3
    assert(meanAsks.last == Some(expectedMean))
  }

  it should "build valid mean bid price stream" in {
    val meanBid = timeInsensitiveSet.meanBid
    val meanBids = meanBid.runLog.run

    val expectedMean = (order1.priceNumerator.toDouble + order2.priceNumerator.toDouble) / 2
    assert(meanBids.last == Some(expectedMean))
  }

  it should "build valid price & volume accumulators" in {
    val accumulatedPrice = timeInsensitiveSet.accumulatedPriceSpread
    val accumulatedVolume = timeInsensitiveSet.accumulatedVolumeSpread

    val acc = (accumulatedPrice zip accumulatedVolume).runLog.run

    val expectedAcc = (Some(1000), Some(5))
    assert(acc.last == expectedAcc)
  }
}
