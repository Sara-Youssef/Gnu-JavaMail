/*
 * ByteArrayRequestBodyWriter.java
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
 * A simple request body writer using a byte array.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class ByteArrayRequestBodyWriter
  implements RequestBodyWriter
{

  /**
   * The content.
   */
  protected byte[] content;

  /**
   * The position within the content at which the next read will occur.
   */
  protected int pos;

  /**
   * Constructs a new byte array request body writer with the specified
   * content.
   * @param content the content buffer
   */
  public ByteArrayRequestBodyWriter(byte[] content)
  {
    this.content = content;
    pos = 0;
  }

  /**
   * Returns the total number of bytes that will be written in a single pass
   * by this writer.
   */
  public long getContentLength()
  {
    return (long) content.length;
  }

  /**
   * Initialises the writer.
   * This will be called before each pass.
   */
  public void reset()
  {
    pos = 0;
  }

  /**
   * Writes body content to the supplied buffer.
   * @param buffer the content buffer
   * @return the number of bytes written
   */
  public int write(byte[] buffer)
  {
    int len = content.length - pos;
    len = (buffer.length < len) ? buffer.length : len;
    if (len > -1)
      {
        System.arraycopy(content, pos, buffer, 0, len);
        pos += len;
      }
    return len;
  }

}

