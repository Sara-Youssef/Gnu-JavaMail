/*
 * ByteArrayResponseBodyReader.java
 * Copyright (C) 2004 The Free Software Foundation
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

package gnu.inet.http;

/**
 * Simple response body reader that stores content in a byte array.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class ByteArrayResponseBodyReader
  implements ResponseBodyReader
{

  /**
   * The content.
   */
  protected byte[] content;

  /**
   * The position in the content at which the next write will occur.
   */
  protected int pos;

  /**
   * The length of the buffer.
   */
  protected int len;

  /**
   * Constructs a new byte array response body reader.
   */
  public ByteArrayResponseBodyReader()
  {
    this(4096);
  }

  /**
   * Constructs a new byte array response body reader with the specified
   * initial buffer size.
   * @param size the initial buffer size
   */
  public ByteArrayResponseBodyReader(int size)
  {
    content = new byte[size];
    pos = len = 0;
  }

  /**
   * This reader accepts all responses.
   */
  public boolean accept(Request request, Response response)
  {
    return true;
  }

  public void read(byte[] buffer, int offset, int length)
  {
    int l = length - offset;
    if (pos + l > content.length)
      {
        byte[] tmp = new byte[content.length * 2];
        System.arraycopy(content, 0, tmp, 0, pos);
        content = tmp;
      }
    System.arraycopy(buffer, offset, content, pos, l);
    pos += l;
    len = pos;
  }

  public void close()
  {
    pos = 0;
  }

  /**
   * Retrieves the content of this reader as a byte array.
   * The size of the returned array is the number of bytes read.
   */
  public byte[] toByteArray()
  {
    byte[] ret = new byte[len];
    System.arraycopy(content, 0, ret, 0, len);
    return ret;
  }

}

