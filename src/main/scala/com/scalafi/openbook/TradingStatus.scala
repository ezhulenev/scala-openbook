package com.scalafi.openbook

sealed trait TradingStatus

object TradingStatus {
  case object PreOpening extends TradingStatus
  case object Opened extends TradingStatus
  case object Closed extends TradingStatus
  case object Halted extends TradingStatus

  def apply(c: Char) = c match {
    case 'P' => PreOpening
    case 'O' => Opened
    case 'C' => Closed
    case 'H' => Halted
    case _ => sys.error(s"Unknown trading status: '$c'")
  }
}