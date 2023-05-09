/*
 * LiteralFactory.java
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

import java.io.IOException;

/**
 * Interface defining a mechanism that can handle large IMAP literals.
 * These may require a significant time to load from the
 * network, so any application that wishes to provide a progress bar,
 * or some strategy that can cache the data on disk instead of it all being
 * held in memory should implement this interface and call
 * {@link IMAPConnection#setLiteralHandler} with the size threshold before
 * {@link IMAPConnection#connect}.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public interface LiteralFactory
{

  /**
   * Returns a new literal of the specified size.
   * @param size the number of bytes that are required to store the literal
   * @exception IOException if the handler cannot store a literal of this
   * size
   */
  Literal newLiteral(int size)
    throws IOException;

}
