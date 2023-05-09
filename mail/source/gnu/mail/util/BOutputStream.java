/*
 * BOutputStream.java
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

package gnu.mail.util;

import java.io.OutputStream;

/**
 * Provides RFC 2047 "B" transfer encoding.
 * See section 4.1.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class BOutputStream
  extends Base64OutputStream
{

  public BOutputStream(OutputStream out)
  {
    super(out, 0x7fffffff);
  }

  public static int encodedLength(byte[] bytes)
  {
    return ((bytes.length+2)/3)*4;
  }

}
