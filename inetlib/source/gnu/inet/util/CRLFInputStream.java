/*
 * CRLFInputStream.java
 * Copyright (C) 2002,2006 The Free Software Foundation
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

package gnu.inet.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * An input stream that filters out CR/LF pairs into LFs.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class CRLFInputStream
  extends InputStream
{

  /**
   * The CR octet.
   */
  public static final int CR = 13;

  /**
   * The LF octet.
   */
  public static final int LF = 10;

  /**
   * The underlying input stream.
   */
  protected InputStream in;

  private boolean doReset;

  /**
   * Constructs a CR/LF input stream connected to the specified input
   * stream.
   */
  public CRLFInputStream(InputStream in)
  {
    this.in = in.markSupported() ? in : new BufferedInputStream(in);
  }

  /**
   * Reads the next byte of data from this input stream.
   * Returns -1 if the end of the stream has been reached.
   * @exception IOException if an I/O error occurs
   */
  public int read()
    throws IOException
  {
    int c = in.read();
    if (c == CR)
      {
        in.mark(1);
        int d = in.read();
        if (d == LF)
          {
            c = d;
          }
        else
          {
            in.reset();
          }
      }
    return c;
  }

  /**
   * Reads up to b.length bytes of data from this input stream into
   * an array of bytes.
   * Returns -1 if the end of the stream has been reached.
   * @exception IOException if an I/O error occurs
   */
  public int read(byte[] b)
    throws IOException
  {
    return read(b, 0, b.length);
  }

  /**
   * Reads up to len bytes of data from this input stream into an
   * array of bytes, starting at the specified offset.
   * Returns -1 if the end of the stream has been reached.
   * @exception IOException if an I/O error occurs
   */
  public int read(byte[] b, int off, int len)
    throws IOException
  {
    in.mark(len + 1);
    int l = in.read(b, off, len);
    if (l > 0)
      {
        int i = indexOfCRLF(b, off, l);
        if (doReset)
          {
            in.reset();
            if (i != -1)
              {
                l = in.read(b, off, (i + 1) - off); // read to CR
                in.read(); // skip LF
                b[i] = LF; // fix CR as LF
              }
            else
              {
                l = in.read(b, off, len); // CR(s) but no LF
              }
          }
      }
    return l;
  }

  private int indexOfCRLF(byte[] b, int off, int len)
    throws IOException
  {
    doReset = false;
    int end = off + len;
    int em1 = end - 1;
    for (int i = off; i < end; i++)
      {
        if (b[i] == CR)
          {
            int d;
            if (i == em1)
              {
                d = in.read();
                doReset = true;
              }
            else
              {
                d = b[i + 1];
              }
            if (d == LF)
              {
                doReset = true;
                return i;
              }
          }
      }
    return -1;
  }

}

