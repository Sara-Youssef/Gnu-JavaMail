/*
 * CRLFOutputStream.java
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * An output stream that filters LFs into CR/LF pairs.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class CRLFOutputStream
  extends FilterOutputStream
{

  static final String US_ASCII = "US-ASCII";

  /**
   * The CR octet.
   */
  public static final int CR = '\r';

  /**
   * The LF octet.
   */
  public static final int LF = '\n';

  /**
   * The CR/LF pair.
   */
  public static final byte[] CRLF = { CR, LF };

  /**
   * At beginning of line.
   */
  protected boolean atBOL;

  /**
   * The last byte read.
   */
  protected int last;

  /**
   * Constructs a CR/LF output stream connected to the specified output stream.
   */
  public CRLFOutputStream(OutputStream out)
  {
    super(out);
    atBOL = true;
    last = -1;
  }

  /**
   * Constructs a CR/LF output stream connected to the specified output stream.
   */
  protected CRLFOutputStream(CRLFOutputStream out)
  {
    super(out);
    atBOL = out.atBOL;
    last = out.last;
  }

  /**
   * Writes newline to the underlying stream if ! atBOL.
   * @exception IOException if an I/O error occurred
   */
  public void ensureBOL() throws IOException
  {
    if (! atBOL)
      {
        writeln();
      }
  }

  /**
   * Writes a character to the underlying stream.
   * @exception IOException if an I/O error occurred
   */
  public void write(int ch) throws IOException
  {
    switch (ch)
      {
      case CR:
        writeln();
        break;
      case LF:
        if (last != CR)
          {
            writeln();
          }
        break;
      default:
        if (atBOL)
          {
            writeAtBOL(ch);
          }
        out.write(ch);
        atBOL = false;
        break;
      }
    last = ch;
  }

  /**
   * Writes a portion of a byte array to the underlying stream.
   * @exception IOException if an I/O error occurred
   */
  public void write(byte[] b, int off, int len)
    throws IOException
  {
    int d = off;
    len += off;
    for (int i = off; i < len; i++)
      {
        switch (b[i])
          {
          case CR:
            writeln(b, d, i - d);
            d = i + 1;
            break;
          case LF:
            if (last != CR)
              {
                writeln(b, d, i - d);
              }
            d = i + 1;
            break;
          }
        last = b[i];
      }
    if ((len -= d) > 0)
      {
        if (atBOL)
          {
            writeAtBOL(b, d, len);
          }
        out.write(b, d, len);
        atBOL = false;
      }
  }

  /**
   * Writes the specified ASCII string to the underlying stream.
   * @exception IOException if an I/O error occurred
   */
  public void write(String text)
    throws IOException
  {
    try
      {
        byte[] bytes = text.getBytes(US_ASCII);
        write(bytes, 0, bytes.length);
        }
    catch (UnsupportedEncodingException e)
      {
        throw new IOException("The US-ASCII encoding is not supported " +
                              "on this system");
      }
  }

  /**
   * Write At Beginning Of Line event; notify subclass, if any.
   * @exception IOException if an I/O error occurred
   */
  protected void writeAtBOL(int ch)
    throws IOException
  {
  }

  /**
   * Write At Beginning Of Line event; notify subclass, if any.
   * @exception IOException if an I/O error occurred
   */
  protected void writeAtBOL(byte[] b, int off, int len)
    throws IOException
  {
  }

  /**
   * Writes a newline to the underlying stream.
   * @exception IOException if an I/O error occurred
   */
  public void writeln()
    throws IOException
  {
    out.write(CRLF, 0, CRLF.length);
    atBOL = true;
  }

  /**
   * DANGER - private method for internal use only - DANGER
   * Private method for public void write(byte[] b, int off, int len)
   * Writes a portion of a byte array and newline to underlying stream.
   * @exception IOException if an I/O error occurred
   */
  private void writeln(byte[] b, int off, int len)
    throws IOException
  {
    // Leading newlines in buffer have d == i therefore len == 0 here
    if (len > 0)
    {
      if (atBOL)
        {
          writeAtBOL(b, off, len);
        }
      out.write(b, off, len);
    }
    writeln();
  }

}
