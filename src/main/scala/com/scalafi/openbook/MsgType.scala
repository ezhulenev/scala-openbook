package com.scalafi.openbook

sealed trait MsgType

object MsgType {

  case object FullUpdate extends MsgType

  case object DeltaUpdate extends MsgType

  case class UnknownMsgType(code: Int) extends MsgType

  def apply(code: Int): MsgType = code match {
    case 230 => FullUpdate
    case 231 => DeltaUpdate
    case _ => UnknownMsgType(code)
  }
}