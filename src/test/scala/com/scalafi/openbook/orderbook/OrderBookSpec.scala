package com.scalafi.openbook.orderbook

import org.scalatest.{GivenWhenThen, FlatSpec}
import com.scalafi.openbook._
import scala.concurrent.duration._

class OrderBookSpec extends FlatSpec with GivenWhenThen {

  import FeatureSet._

  private val Symbol = "APL"

  private def orderMsg(sourceTime: Int, sourceTimeMicroSecs: Short, price: Int, volume: Int, side: Side) =
    OpenBookMsg(
      msgSeqNum = 0,
      msgType = MsgType.DeltaUpdate,
      sendTime = 0,
      symbol = Symbol,
      msgSize = 46,
      securityIndex = 0,
      sourceTime = sourceTime,
      sourceTimeMicroSecs = sourceTimeMicroSecs,
      quoteCondition = QuoteCondition.Normal,
      tradingStatus = TradingStatus.Opened,
      sourceSeqNum = 0,
      sourceSessionId = 0,
      priceScaleCode = 4,
      priceNumerator = price,
      volume = volume,
      chgQty = 0,
      numOrders = 0,
      side = side,
      reasonCode = ReasonCode.Order,
      linkID1 = 0,
      linkID2 = 0,
      linkID3 = 0)

  "OrderBook" should "build correct order book from order flow" in {

    Given("three orders")
    val order1 = orderMsg(0, 0, 10000, 10, Side.Buy)
    val order2 = orderMsg(100, 0, 10000, 15, Side.Buy)
    val order3 = orderMsg(200, 0, 11000, 20, Side.Sell)

    Then("correct order books should be constructed")
    val orderBook = OrderBook(Symbol, 1.second).update(order1).update(order2).update(order3)

    assert(orderBook.buy.size == 1)
    assert(orderBook.sell.size == 1)
    assert(orderBook.orders.size == 3)

    And("basic set features correctly calculated")
    assert(orderBook.askPrice(1).map(_.value) == Some(11000))
    assert(orderBook.askVolume(1).map(_.value) == Some(20))

    assert(orderBook.bidPrice(1).map(_.value) == Some(10000))
    assert(orderBook.bidVolume(1).map(_.value) == Some(15))

    Given("next order")
    val order4 = orderMsg(1001, 0, 12000, 20, Side.Sell)

    Then("first order should be evicted by time")
    val orderBookUpd = orderBook.update(order4)

    assert(orderBookUpd.buy.size == 1)
    assert(orderBookUpd.sell.size == 2)
    assert(orderBookUpd.orders.size == 3)

    And("basic set features correctly updated")
    assert(orderBookUpd.askPrice(1).map(_.value) == Some(11000))
    assert(orderBookUpd.askPrice(2).map(_.value) == Some(12000))
    assert(orderBookUpd.bidPrice(1).map(_.value) == Some(10000))
  }
}
