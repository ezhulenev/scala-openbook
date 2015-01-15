package com.scalafi.openbook

import java.io.InputStream


final class ByteArrayIterator(is : InputStream, len : Int) extends Iterator[Array[Byte]] {

  assume(len > 0, s"Message length must be positive. Found: ${len}")
  
  private val buf = new Array[Byte](len);
  private var done, full = false : Boolean;

  def next() = {
    if (full)
      // next message already buffered
    {
      full = false;
      buf
    }
    else
    {
      // see if we can buffer next message, if so, return that one
      if (hasNext()) next()
      else throw new NoSuchElementException("next on empty iterator")
    }
  }

  def hasNext() = {
    if (done) false
    else if (full) true
    else {
      val nBytesRead = is.read(buf)
      if (nBytesRead == len)
        // cool, read a whole message
      {
        full = true
        full
      }
      else if (nBytesRead == -1)
      // end of buffer has been reached
      {
        is.close()
        done = true
        false
      }
      else
      {
        throw new NoSuchElementException(s"Expected message length of ${len}, but could read only ${nBytesRead}.")
      }
    }
  }
}
