package com.scalafi.openbook

sealed trait QuoteCondition

object QuoteCondition {
  case object Normal extends QuoteCondition
  case object SlowBid extends QuoteCondition
  case object SlowAsk extends QuoteCondition
  case object SlowBidAsk extends QuoteCondition
  case object SetSlow extends QuoteCondition

  def apply(c: Char) = c match {
    case ' ' => Normal
    case 'E' => SlowBid
    case 'F' => SlowAsk
    case 'U' => SlowBidAsk
    case 'W' => SetSlow
    case _ => sys.error(s"Unknown quote condition code: '$c'")
  }
}
