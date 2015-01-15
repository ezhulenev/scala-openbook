package com.scalafi.openbook.orderbook

import scala.collection.immutable.TreeMap
import com.scalafi.openbook.{Side, OpenBookMsg}
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

object OrderBook {

	def empty(symbol: String): OrderBook = new OrderBook(symbol)

	/**
	 * Time series of OrderBook for a stream of order messages for a single symbol:
	 */
	def fromOrders(symbol: String, orders: Iterator[OpenBookMsg]): Iterator[OrderBook] = {
		val ob = empty(symbol);
		orders filter(_.symbol.equals(symbol)) map { ob.update(_) }
	}

	/**
	 * Time series of OrderBooks for a stream of order messages for a multiple symbols:
	 */
	def fromOrders(orders: Iterator[OpenBookMsg]): Iterator[OrderBook] = {
		val obs = new HashMap[String, OrderBook];
		orders map { 
			msg => {
				val sym = msg.symbol;

				// get order book for this symbol, if it exists, else create new one 
        // (un-idiomatic old people style):
				val ob : OrderBook = 
						if (obs.contains(sym)) {
							obs.get(sym).get
						}
						else {
							val ob1 = empty(sym);
							obs.put(sym, ob1)
							ob1
						};

						ob.update(msg)
			}
		}
	}
}

case class OrderBook(symbol: String,
		buy: TreeMap[Int, Int] = TreeMap.empty,
		sell: TreeMap[Int, Int] = TreeMap.empty,
    lastMsg : OpenBookMsg = null // old people style, crusty and fast
    ) {

	def update(order: OpenBookMsg): OrderBook = {
			assume(order.symbol == symbol, s"Unexpected order symbol: ${order.symbol}. In Order Book for: $symbol")

			order match {
			case _ if order.side == Side.Buy & order.volume > 0 =>
			copy(buy = buy + (order.priceNumerator -> order.volume), lastMsg = order)

			case _ if order.side == Side.Buy & order.volume == 0 =>
			copy(buy = buy - order.priceNumerator, lastMsg = order)

			case _ if order.side == Side.Sell & order.volume > 0 =>
			copy(sell = sell + (order.priceNumerator -> order.volume), lastMsg = order)

			case _ if order.side == Side.Sell & order.volume == 0 =>
			copy(sell = sell - order.priceNumerator, lastMsg = order)

			case _ if order.side == Side.NA => copy(lastMsg = order)
			}
	}

	def printOrderBook(depth: Int): String = {

			val bid = buy.keySet.drop(buy.size - depth).map(price => s"$price : ${buy(price)}");
			val ask = sell.keySet.take(depth).map(price => s"$price : ${sell(price)}");

			s"""|Bid
			|${bid.mkString(System.lineSeparator())}
			|- - - - - - - - - -
			|Ask
			|${ask.mkString(System.lineSeparator())}
			|""".stripMargin.trim
	}
}