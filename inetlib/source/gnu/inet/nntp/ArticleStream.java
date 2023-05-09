/*
 * ArticleStream.java
 * Copyright (C) 2002 The Free Software Foundation
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

package gnu.inet.nntp;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * A stream that can be told to read to the end of its data.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class ArticleStream
  extends FilterInputStream
  implements PendingData
{

  private static final int LF = 0x0a;
  private static final int DOT = 0x2e;

  boolean eol;
  boolean eof;

  ArticleStream(InputStream in)
  {
    super(in.markSupported() ? in : new BufferedInputStream(in));
    eol = true;
    eof = false;
  }

  public int read()
    throws IOException
  {
    if (eof)
      {
        return -1;
      }
    int c = in.read();
    // Check for LF
    if (c == LF)
      {
        eol = true;
      }
    else if (eol)
      {
        if (c == DOT)
          {
            in.mark(1);
            int d = in.read();
            if (d == DOT)
              {
                // Not resetting here means that 2 dots are collapsed into 1
              }
            else if (d == LF)
              {
                // Check for LF
                eof = true;
                return -1;
              }
            else
              {
                in.reset();
              }
          }
        eol = false;
      }
    return c;
  }

  public int read(byte[] b)
    throws IOException
  {
    return read(b, 0, b.length);
  }

  public int read(byte[] b, int off, int len)
    throws IOException
  {
    if (eof)
      {
        return -1;
      }
    int l = in.read(b, off, len);
    if (l > 0)
      {
        if (eol)
          {
            if (b[off] == DOT && l > 1)
              {
                if (b[off + 1] == DOT)
                  {
                    // Truncate b
                    System.arraycopy(b, off + 1, b, off, l - off);
                    l--;
                  }
                else if (b[off + 1] == LF)
                  {
                    // EOF
                    eof = true;
                    return -1;
                  }
              }
          }
        eol = (b[(off + l) - 1] == LF);
      }
    return l;
  }

  /**
   * Read to the end of the article data.
   */
  public void readToEOF()
    throws IOException
  {
    if (available() == 0)
      {
        return;
      }
    byte[] buf = new byte[4096];
    int ret = 0;
    while (ret != -1)
      {
        ret = read(buf);
      }
  }

}

