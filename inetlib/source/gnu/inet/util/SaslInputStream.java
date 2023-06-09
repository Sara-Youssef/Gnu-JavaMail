/*
 * SaslInputStream.java
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

package gnu.inet.util;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.security.sasl.SaslClient;

/**
 * A filter input stream that decodes all its received input using a SASL
 * client.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class SaslInputStream
  extends FilterInputStream
{

  /*
   * The SASL client.
   */
  private final SaslClient sasl;

  /*
   * Overflow buffer.
   */
  private byte[] buf;

  /*
   * Offset in overflow buffer.
   */
  private int pos;

  /**
   * Constructor.
   * @param sasl the SASL client
   * @param in the underlying input stream
   */
  public SaslInputStream(SaslClient sasl, InputStream in)
  {
    super(in);
    this.sasl = sasl;
  }

  /**
   * Reads a single character.
   */
  public int read()
    throws IOException
  {
    if (buf != null)
      {
        // Return next characer in buffer
        int c = (int) buf[pos++];
        if (pos == buf.length)
          {
            buf = null;
          }
        return c;
      }
    int c = super.read();
    if (c == -1)
      {
        return c;
      }
    byte[] bytes = new byte[1];
    byte[] unwrapped = sasl.unwrap(bytes, 0, 1);
    // FIXME if we get 0 bytes, we have a problem
    c = (int) unwrapped[0];
    if (unwrapped.length > 1)
      {
        // Store in overflow buffer
        int l = unwrapped.length - 1;
        buf = new byte[l];
        System.arraycopy(unwrapped, 1, buf, 0, l);
        pos = 0;
      }
    return c;
  }

  public int read(byte[] bytes)
    throws IOException
  {
    return read(bytes, 0, bytes.length);
  }

  /**
   * Block read.
   */
  public int read(byte[] bytes, int off, int len)
    throws IOException
  {
    if (buf != null)
      {
        // Return bytes from buffer
        int l = buf.length;
        if (l - pos <= len)
          {
            System.arraycopy(buf, pos, bytes, off, l);
            buf = null;
            return l;
          }
        else
          {
            System.arraycopy(buf, pos, bytes, off, len);
            pos += len;
            return len;
          }
      }
    int l = super.read(bytes, off, len);
    if (l == -1)
      {
        return l;
      }
    byte[] unwrapped = sasl.unwrap(bytes, off, l);
    int l2 = unwrapped.length;
    if (l2 > len)
      {
        // Store excess bytes in buffer
        int d = l2 - len;
        buf = new byte[d];
        System.arraycopy(unwrapped, 0, bytes, off, len);
        System.arraycopy(unwrapped, len, buf, 0, d);
        pos = 0;
        return len;
      }
    else
      {
        System.arraycopy(unwrapped, 0, bytes, off, l2);
        // Zero bytes from l2..l to ensure none of the original
        // bytes received can be read by the caller
        for (int i = l2; i < l; i++)
          {
            bytes[off + l2] = 0;
          }
        return l2;
      }
  }

}

