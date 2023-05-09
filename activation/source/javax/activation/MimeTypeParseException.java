/*
 * MimeTypeParseException.java
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
package javax.activation;

/**
 * Exception thrown to indicate a malformed MIME content type.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public class MimeTypeParseException
  extends Exception
{

  /**
   * Constructs a MimeTypeParseException with no detail message.
   */
  public MimeTypeParseException()
  {
  }

  /**
   * MimeTypeParseException with the specified detail message.
   * @param message the exception message
   */
  public MimeTypeParseException(String message)
  {
    super(message);
  }

}

