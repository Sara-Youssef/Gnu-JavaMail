/*
 * SaslOutputStream.java
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.security.sasl.SaslClient;

/**
 * A filter output stream that encodes data written to it using a SASL
 * client.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class SaslOutputStream
  extends FilterOutputStream
{

  /*
   * The SASL client used for encoding data.
   */
  private final SaslClient sasl;

  /**
   * Constructor.
   * @param sasl the SASL client
   * @param out the target output stream
   */
  public SaslOutputStream(SaslClient sasl, OutputStream out)
  {
    super(out);
    this.sasl = sasl;
  }

  /**
   * Character write.
   */
  public void write(int c)
    throws IOException
  {
    byte[] bytes = new byte[1];
    bytes[0] = (byte) c;
    write(bytes, 0, 1);
  }

  public void write(byte[] bytes)
    throws IOException
  {
    write(bytes, 0, bytes.length);
  }

  /**
   * Block write.
   */
  public void write(byte[] bytes, int off, int len)
    throws IOException
  {
    byte[] wrapped = sasl.wrap(bytes, off, len);
    super.write(wrapped, 0, wrapped.length);
  }

}

