/*
 * StringToken.java
 * Copyright (C) 2013 The Free Software Foundation
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
package gnu.inet.imap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * String token.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class StringToken
  extends Token
{

  private final byte[] string;

  StringToken(int type, byte[] string)
  {
    super(type);
    this.string = string;
  }

  int intValue()
  {
    return Integer.parseInt(stringValue());
  }

  long longValue()
  {
    return Long.parseLong(stringValue());
  }

  String stringValue()
  {
    return new String(string, IMAPConnection.US_ASCII);
  }

  Literal literalValue()
  {
    return new Literal()
    {
      public OutputStream getOutputStream()
      {
        throw new UnsupportedOperationException();
      }

      public InputStream getInputStream()
      {
        return new ByteArrayInputStream(string);
      }
    };
  }

  public String toString()
  {
    return new StringBuilder(super.toString())
      .append(':')
      .append(stringValue())
      .toString();
  }

}
