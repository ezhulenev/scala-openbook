package com.scalafi.openbook

import org.scalatest.FlatSpec

class OpenBookParserSpec extends FlatSpec {

  implicit val codec = io.Codec.ISO8859

  "OpenBook parser" should "parse all OpenBook Ultra messages" in {

    val is = this.getClass.getResourceAsStream("/openbookultraAA_N20130403_1_of_1")

    val messages = io.Source.fromInputStream(is).
      map(_.toByte).grouped(69).map(_.toArray)

    val parsed = messages.map(OpenBookMsg.apply)
    assert(parsed.size == 1000)
  }

  it should "stream it from InputStream" in {
    val is = this.getClass.getResourceAsStream("/openbookultraAA_N20130403_1_of_1")

    val orders = OpenBookMsg.stream(is)
    val parsed = orders.runLog.run
    assert(parsed.size == 1000)
  }
}
