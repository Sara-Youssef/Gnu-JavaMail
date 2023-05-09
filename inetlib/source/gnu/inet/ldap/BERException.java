/*
 * BERException.java
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

package gnu.inet.ldap;

import java.io.IOException;

/**
 * Exception thrown to indicate a BER encoding error.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class BERException
  extends IOException
{

  /**
   * Constructs a BER encoding exception with no message.
   */
  public BERException()
  {
  }

  /**
   * Constructs a BER encoding exception with the specified message.
   */
  public BERException(String message)
  {
    super(message);
  }

}

