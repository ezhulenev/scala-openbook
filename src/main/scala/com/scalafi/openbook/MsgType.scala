package com.scalafi.openbook

sealed trait MsgType

object MsgType {

  case object FullUpdate extends MsgType

  case object DeltaUpdate extends MsgType

  def apply(code: Short): MsgType = code match {
    case 230 => FullUpdate
    case 231 => DeltaUpdate
    case _ => sys.error(s"Unknown message type code: '$code'")
  }
}