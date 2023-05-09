/*
 * LineIterator.java
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

package gnu.inet.nntp;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over an NNTP multi-line response.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class LineIterator
  implements Iterator, PendingData
{

  static final String DOT = ".";

  boolean doneRead = false;
  NNTPConnection connection;
  String current;

  LineIterator(NNTPConnection connection)
  {
    this.connection = connection;
  }

  void doRead()
    throws IOException
  {
    if (doneRead)
      {
        return;
      }
    String line = connection.read();
    if (DOT.equals(line))
      {
        current = null;
      }
    else
      {
        current = line;
      }
    doneRead = true;
  }

  /**
   * Indicates whether there are more lines to be read.
   */
  public boolean hasNext()
  {
    try
      {
        doRead();
      }
    catch (IOException e)
      {
        return false;
      }
    return(current != null);
  }

  /**
   * Returns the next line.
   */
  public Object next()
  {
    try
      {
        return nextLine();
      }
    catch (IOException e)
      {
        throw new NoSuchElementException("I/O error: " + e.getMessage());
      }
  }

  /**
   * Returns the next line.
   */
  public String nextLine()
    throws IOException
  {
    doRead();
    if (current == null)
      {
        throw new NoSuchElementException();
      }
    doneRead = false;
    return current;
  }

  /**
   * This iterator is read-only.
   */
  public void remove()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Read to the end of this iterator.
   */
  public void readToEOF()
    throws IOException
  {
    do
      {
        doRead();
      }
    while (current != null);
  }

}

