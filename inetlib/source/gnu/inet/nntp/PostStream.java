/*
 * PostStream.java
 * Copyright (C) 2002, 2003 The free Software Foundation
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

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * A stream to which article contents should be written.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class PostStream
  extends FilterOutputStream
{

  private static final int LF = 0x0a;
  private static final int DOT = 0x2e;

  NNTPConnection connection;
  boolean isTakethis;
  byte last;

  PostStream(NNTPConnection connection, boolean isTakethis)
  {
    super(connection.out);
    this.connection = connection;
    this.isTakethis = isTakethis;
  }

  public void write(int c)
    throws IOException
  {
    super.write(c);
    if (c == DOT && last == LF)
      {
        super.write(c); // double up initial dot
      }
    last = (byte) c;
  }

  public void write(byte[] bytes)
    throws IOException
  {
    write(bytes, 0, bytes.length);
  }

  public void write(byte[] bytes, int pos, int len)
    throws IOException
  {
    int end = pos + len;
    for (int i = pos; i < end; i++)
      {
        byte c = bytes[i];
        if (c == DOT && last == LF)
          {
            // Double dot
            if (i > pos)
              {
                // Write everything up to i
                int l = i - pos;
                super.write(bytes, pos, l);
                pos += l;
                len -= l;
              }
            else
              {
                super.write(DOT);
              }
          }
        last = c;
      }
    if (len > 0)
      {
        super.write(bytes, pos, len);
      }
  }

  /**
   * Close the stream.
   * This calls NNTPConnection.postComplete().
   */
  public void close()
    throws IOException
  {
    if (last != 0x0d)
      {
        // Need to add LF
        write(0x0d);
      }
    if (isTakethis)
      {
        connection.takethisComplete();
      }
    else
      {
        connection.postComplete();
      }
  }

}

