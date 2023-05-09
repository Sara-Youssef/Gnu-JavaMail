/*
 * BlockInputStream.java
 * Copyright (C) 2003 The Free Software Foundation
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

package gnu.inet.ftp;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * A DTP input stream that implements the FTP block transfer mode.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class BlockInputStream
  extends DTPInputStream
{

  static final int EOF = 64;

  int descriptor;
  int max = -1;
  int count = -1;

  BlockInputStream(DTP dtp, InputStream in)
  {
    super(dtp, in);
  }

  public int read()
    throws IOException
  {
    if (transferComplete)
      {
        return -1;
      }
    if (count == -1)
      {
        readHeader();
      }
    if (max < 1)
      {
        close();
        return -1;
      }
    int c = in.read();
    if (c == -1)
      {
        dtp.transferComplete();
      }
    count++;
    if (count >= max)
      {
        count = -1;
        if (descriptor == EOF)
          {
            close();
          }
      }
    return c;
  }

  public int read(byte[] buf)
    throws IOException
  {
    return read(buf, 0, buf.length);
  }

  public int read(byte[] buf, int off, int len)
    throws IOException
  {
    if (transferComplete)
      {
        return -1;
      }
    if (count == -1)
      {
        readHeader();
      }
    if (max < 1)
      {
        close();
        return -1;
      }
    int l = in.read(buf, off, len);
    if (l == -1)
      {
        dtp.transferComplete();
      }
    count += l;
    if (count >= max)
      {
        count = -1;
        if (descriptor == EOF)
          {
            close();
          }
      }
    return l;
  }

  /**
   * Reads the block header.
   */
  void readHeader()
    throws IOException
  {
    descriptor = in.read();
    int max_hi = in.read();
    int max_lo = in.read();
    max = (max_hi << 8) | max_lo;
    count = 0;
  }

}

