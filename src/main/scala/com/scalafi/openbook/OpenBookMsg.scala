package com.scalafi.openbook

import java.io.InputStream
import java.nio.ByteBuffer

import scala.io.{Codec, Source}
import scalaz.concurrent.Task
import scalaz.stream._


private[openbook] trait Parser {

  protected sealed trait Parser[T] { parser =>

    def parseInternal(byteBuffer: ByteBuffer): T

    def parse(bytes: Array[Byte], pos: (Int, Int)): T = {
      val (from, to) = pos
      parseInternal(ByteBuffer.wrap(bytes.slice(from - 1, to)))
    }

    def map[O](f: T => O): Parser[O] = new Parser[O] {
      def parseInternal(byteBuffer: ByteBuffer): O =
        f(parser.parseInternal(byteBuffer))
    }
  }

  protected implicit object IntParser extends Parser[Int] {
    def parseInternal(byteBuffer: ByteBuffer) =
      byteBuffer.getInt
  }

  protected implicit object ShortParser extends Parser[Short] {
    def parseInternal(byteBuffer: ByteBuffer) =
      byteBuffer.getShort
  }

  protected implicit object StringParser extends Parser[String] {
    def parseInternal(byteBuffer: ByteBuffer) =
      String.valueOf(byteBuffer.array().map(_.toChar)).trim
  }

  protected implicit object ByteParser extends Parser[Byte] {
    def parseInternal(byteBuffer: ByteBuffer) =
      byteBuffer.get()
  }

  protected implicit val MsgTypeParser: Parser[MsgType] =
    implicitly[Parser[Short]].map(MsgType.apply)

  protected implicit val QuoteConditionParser: Parser[QuoteCondition] =
    implicitly[Parser[Byte]].map(b => QuoteCondition(b.toChar))

  protected implicit val TradingStatusParser: Parser[TradingStatus] =
   implicitly[Parser[Byte]].map(b => TradingStatus(b.toChar))

  protected implicit val SideParser: Parser[Side] =
    implicitly[Parser[Byte]].map(b => Side(b.toChar))

  protected implicit val ReasonCodeParser: Parser[ReasonCode] =
    implicitly[Parser[Byte]].map(b => ReasonCode(b.toChar))

  protected def parse[T](pos: (Int, Int))(implicit bytes: Array[Byte], parser: Parser[T]) =
    parser.parse(bytes, pos)
}

object OpenBookMsg extends Parser {

  private object Layout {
    val MsgSeqNum           = 1  -> 4
    val MsgType             = 5  -> 6
    val SendTime            = 7  -> 10
    val Symbol              = 11 -> 21
    val MsgSize             = 22 -> 23
    val SecurityIndex       = 24 -> 25
    val SourceTime          = 26 -> 29
    val SourceTimeMicroSecs = 30 -> 31
    val QuoteCondition      = 32 -> 32
    val TradingStatus       = 33 -> 33
    val SourceSeqNum        = 34 -> 37
    val SourceSessionId     = 38 -> 38
    val PriceScaleCode      = 39 -> 39
    val PriceNumerator      = 40 -> 43
    val Volume              = 44 -> 47
    val ChgQty              = 48 -> 51
    val NumOrders           = 52 -> 53
    val Side                = 54 -> 54
    val Filler1             = 55 -> 55
    val ReasonCode          = 56 -> 56
    val Filler2             = 57 -> 57
    val LinkID1             = 58 -> 61
    val LinkID2             = 62 -> 65
    val LinkID3             = 66 -> 69
  }

  def apply(bytes: Array[Byte]): OpenBookMsg = {
    assume(bytes.length == 69, s"Unexpected message length: ${bytes.length}")

    implicit val b = bytes

    OpenBookMsg(
      parse[Int](Layout.MsgSeqNum),
      parse[MsgType](Layout.MsgType),
      parse[Int](Layout.SendTime),
      parse[String](Layout.Symbol),
      parse[Short](Layout.MsgSize),
      parse[Short](Layout.SecurityIndex),
      parse[Int](Layout.SourceTime),
      parse[Short](Layout.SourceTimeMicroSecs),
      parse[QuoteCondition](Layout.QuoteCondition),
      parse[TradingStatus](Layout.TradingStatus),
      parse[Int](Layout.SourceSeqNum),
      parse[Byte](Layout.SourceSessionId),
      parse[Byte](Layout.PriceScaleCode),
      parse[Int](Layout.PriceNumerator),
      parse[Int](Layout.Volume),
      parse[Int](Layout.ChgQty),
      parse[Short](Layout.NumOrders),
      parse[Side](Layout.Side),
      parse[ReasonCode](Layout.ReasonCode),
      parse[Int](Layout.LinkID1),
      parse[Int](Layout.LinkID2),
      parse[Int](Layout.LinkID3)
    )
  }
  
  def read(filename: String)(implicit codec: Codec): Process[Task, OpenBookMsg] =  
    read(Source.fromFile(filename)(codec))
  
  def read(is: InputStream)(implicit codec: Codec): Process[Task, OpenBookMsg] =
    read(Source.fromInputStream(is)(codec))
  
  def read(src: Source): Process[Task, OpenBookMsg] = {
    import scalaz.stream.io.resource
    resource(Task.delay(src))(src => Task.delay(src.close())) { src =>
      lazy val lines = src.map(_.toByte).grouped(69).map(_.toArray)
      Task.delay { if (lines.hasNext) OpenBookMsg(lines.next()) else throw Cause.Terminated(Cause.End) }
    }
  }

  def iterate(filename: String)(implicit codec: Codec): Iterator[OpenBookMsg] =
    iterate(Source.fromFile(filename)(codec))

  def iterate(is: InputStream)(implicit codec: Codec): Iterator[OpenBookMsg] =
    iterate(Source.fromInputStream(is)(codec))

  def iterate(src: Source): Iterator[OpenBookMsg] = {
    src.map(_.toByte).grouped(69).map(_.toArray).map(OpenBookMsg.apply)
  }
}

case class OpenBookMsg(msgSeqNum: Int,
                       msgType: MsgType,
                       sendTime: Int,
                       symbol: String,
                       msgSize: Short,
                       securityIndex: Short,
                       sourceTime: Int,
                       sourceTimeMicroSecs: Short,
                       quoteCondition: QuoteCondition,
                       tradingStatus: TradingStatus,
                       sourceSeqNum: Int,
                       sourceSessionId: Byte,
                       priceScaleCode: Byte,
                       priceNumerator: Int,
                       volume: Int,
                       chgQty: Int,
                       numOrders: Short,
                       side: Side,
                       reasonCode: ReasonCode,
                       linkID1: Int,
                       linkID2: Int,
                       linkID3: Int)