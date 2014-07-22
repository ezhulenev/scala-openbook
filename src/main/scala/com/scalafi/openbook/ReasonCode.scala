package com.scalafi.openbook

sealed trait ReasonCode

object ReasonCode {
  case object Order extends ReasonCode
  case object Cancel extends ReasonCode
  case object Execution extends ReasonCode
  case object MultipleEvents extends ReasonCode
  case object NA extends ReasonCode

  def apply(c: Char) = c match {
    case 'O' => Order
    case 'C' => Cancel
    case 'E' => Execution
    case 'X' => MultipleEvents
    case _ if c.toByte == 0 => NA
    case _ => sys.error(s"Unknown reason code: '$c'")
  }
}