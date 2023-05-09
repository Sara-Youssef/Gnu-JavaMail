/*
 * MessageOutputStream.java
 * Copyright (C) 2002, 2014 The Free Software Foundation
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

import java.io.IOException;
import java.io.OutputStream;
import gnu.inet.util.CRLFOutputStream;

/**
 * An output stream that escapes any dots on a line by themself with
 * another dot, for the purposes of sending messages to SMTP and NNTP
 * servers.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class MessageOutputStream
  extends CRLFOutputStream
{

  /**
   * The stream termination octet.
   */
  public static final int END = '.';

  /**
   * Constructs a message output stream connected to the specified output
   * stream.
   * @param out the target output stream
   */
  public MessageOutputStream(OutputStream out)
  {
    super(out);
  }

  /**
   * Constructs a message output stream connected to the specified output
   * stream.
   * @param out the target output stream
   */
  public MessageOutputStream(CRLFOutputStream out)
  {
    super(out);
  }

  /**
   * Write At Beginning Of Line event
   * @exception IOException if an I/O error occurred
   */
  protected void writeAtBOL(int ch)
      throws IOException
  {
    if (ch == END)
    {
      // DANGER - avoid super.write(int) method - DANGER
      // Reset atBOL first before super.write(int) method
      // otherwise a recursive infinite loop shall occur.
      out.write(END); // Double At Beginning Of Line
    }
  }

  /**
   * Write At Beginning Of Line event
   * @exception IOException if an I/O error occurred
   */
  protected void writeAtBOL(byte[] b, int off, int len)
      throws IOException
  {
    writeAtBOL(b[off]);
  }

}
