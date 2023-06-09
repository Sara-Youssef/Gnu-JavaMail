/*
 * AddressException.java
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

package javax.mail.internet;

/**
 * An exception thrown when an incorrectly formatted address is encountered.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.5
 */
public class AddressException
  extends ParseException
{

  /**
   * The address(es) being parsed.
   */
  protected String ref;

  /**
   * The index in <code>ref</code> where the error occurred, or -1 if not known.
   */
  protected int pos;

  /**
   * Constructor with no detail message.
   */
  public AddressException()
  {
    this(null, null, -1);
  }

  /**
   * Constructor with the specified detail message.
   * @param s the detail message
   */
  public AddressException(String s)
  {
    this(s, null, -1);
  }

  /**
   * Constructor with the specified detail message and address being parsed.
   * @param s the detail message
   * @param ref the address being parsed
   */
  public AddressException(String s, String ref)
  {
    this(s, ref, -1);
  }

  /**
   * Constructor with the specified detail message and address being parsed.
   * @param s the detail message
   * @param ref the address being parsed
   * @param pos the index in <code>ref</code> where the error occurred
   */
  public AddressException(String s, String ref, int pos)
  {
    super(s);
    this.ref = ref;
    this.pos = pos;
  }

  /**
   * Returns the address(es) being parsed when the error was detected.
   */
  public String getRef()
  {
    return ref;
  }

  /**
   * Returns the position within <code>ref</code> where the error was detected,
   * or -1 if <code>ref</code> is null.
   */
  public int getPos()
  {
    return pos;
  }

  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(super.toString());
    if (ref != null)
      {
        buffer.append(" in string ");
        buffer.append(ref);
        if (pos > -1)
          {
            buffer.append(" at position ");
            buffer.append(pos);
          }
      }
    return buffer.toString();
  }

}

