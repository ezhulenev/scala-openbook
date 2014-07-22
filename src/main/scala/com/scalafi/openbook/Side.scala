package com.scalafi.openbook

sealed trait Side

object Side {
  case object Buy extends Side
  case object Sell extends Side
  case object NA extends Side

  def apply(c: Char) = c match {
    case 'B' => Buy
    case 'S' => Sell
    case _ if c.toByte == 0 => NA
    case _ => sys.error(s"Unknown Buy/Sell side: '$c'")
  }
}