/*
 * QInputStream.java
 * Copyright (C) 2002, 2013 The Free Software Foundation
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

import java.io.InputStream;
import java.io.IOException;

/**
 * Provides RFC 2047 "B" transfer encoding.
 * See section 4.2:
 * <p>
 * The "Q" encoding is similar to the "Quoted-Printable" content-
 * transfer-encoding defined in RFC 2045.  It is designed to allow text
 * containing mostly ASCII characters to be decipherable on an ASCII
 * terminal without decoding.
 * <ol>
 * <li>Any 8-bit value may be represented by a "=" followed by two
 * hexadecimal digits.  For example, if the character set in use
 * were ISO-8859-1, the "=" character would thus be encoded as
 * "=3D", and a SPACE by "=20". (Upper case should be used for
 * hexadecimal digits "A" through "F".)
 * <li>The 8-bit hexadecimal value 20(e.g., ISO-8859-1 SPACE) may be
 * represented as "_"(underscore, ASCII 95.). (This character may
 * not pass through some internetwork mail gateways, but its use
 * will greatly enhance readability of "Q" encoded data with mail
 * readers that do not support this encoding.)  Note that the "_"
 * always represents hexadecimal 20, even if the SPACE character
 * occupies a different code position in the character set in use.
 * <li>8-bit values which correspond to printable ASCII characters other
 * than "=", "?", and "_"(underscore), MAY be represented as those
 * characters. (But see section 5 for restrictions.)  In
 * particular, SPACE and TAB MUST NOT be represented as themselves
 * within encoded words.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.5
 */
public class QInputStream
  extends QPInputStream
{

  private static final int SPACE = 32;
  private static final int EQ = 61;
  private static final int UNDERSCORE = 95;

  /**
   * Constructor.
   * @param in the underlying input stream.
   */
  public QInputStream(InputStream in)
  {
    super(in);
  }

  /**
   * Read a character.
   */
  public int read()
    throws IOException
  {
    int c = in.read();
    if (c == UNDERSCORE)
      {
        return SPACE;
      }
    if (c == EQ)
      {
        int hi = in.read();
        int lo = in.read();
        if (hi < 0 || lo < 0)
          {
            return -1;
          }
        return (Character.digit(hi, 16) << 4) | Character.digit(lo, 16);
      }
    return c;
  }

}
