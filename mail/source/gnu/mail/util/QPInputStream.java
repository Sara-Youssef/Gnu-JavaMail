/*
 * QPInputStream.java
 * Copyright (C) 2002, 2013 The Free Software Foundation
 *
 * This file is part of GNU Classpath Extensions (classpathx).
 * For more information please visit https://www.gnu.org/software/classpathx/
 *
 * classpathx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * classpathx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with classpathx.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package gnu.mail.util;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

/**
 * A Quoted-Printable decoder stream.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.5
 */
public class QPInputStream
  extends FilterInputStream
{

  /**
   * The number of times read() will return a space.
   */
  protected int spaceCount;

  private static final int LF = 10;
  private static final int CR = 13;
  private static final int SPACE = 32;
  private static final int EQ = 61;

  /**
   * Constructor.
   * @param in the underlying input stream.
   */
  public QPInputStream(InputStream in)
  {
    super(new PushbackInputStream(in, 2));
  }

  /**
   * Read a character from the stream.
   */
  public int read()
    throws IOException
  {
    if (spaceCount > 0)
      {
        spaceCount--;
        return SPACE;
      }

    int c = in.read();
    if (c == SPACE)
      {
        while ((c = in.read()) == SPACE)
          {
            spaceCount++;
          }
        if (c == LF || c == CR || c == -1)
          {
            spaceCount = 0;
          }
        else
          {
            ((PushbackInputStream) in).unread(c);
            c = SPACE;
          }
        return c;
      }
    if (c == EQ)
      {
        int c2 = super.in.read();
        if (c2 == LF)
          {
            return read();
          }
        if (c2 == CR)
          {
            int peek = in.read();
            if (peek != LF)
              ((PushbackInputStream) in).unread(peek);
            return read();
          }
        int hi = c2;
        int lo = in.read();
        if (hi < 0 || lo < 0)
          {
            return -1;
          }
        return (Character.digit(hi, 16) << 4) | Character.digit(lo, 16);
      }
    return c;
  }

  /**
   * Reads from the underlying stream into the specified byte array.
   */
  public int read(byte[] bytes, int off, int len)
    throws IOException
  {
    int pos = 0;
    for (int c = read(); c != -1 && pos < len; c = read())
      {
        bytes[off + pos] = (byte) c;
        pos++;
      }
    return (pos < 1) ? -1 : pos;
  }

  /**
   * Mark is not supported.
   */
  public boolean markSupported()
  {
    return false;
  }

  /**
   * Returns the number of bytes that can be read without blocking.
   */
  public int available()
    throws IOException
  {
    return in.available();
  }

}
