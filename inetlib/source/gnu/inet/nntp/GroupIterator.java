/*
 * GroupIterator.java
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
import java.net.ProtocolException;
import java.util.NoSuchElementException;

/**
 * An iterator over an NNTP newsgroup listing.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class GroupIterator
  extends LineIterator
{

  static final String CAN_POST = "y";

  GroupIterator(NNTPConnection connection)
  {
    super(connection);
  }

  /**
   * Returns the next group.
   */
  public Object next()
  {
    try
      {
        return nextGroup();
      }
    catch (IOException e)
      {
        throw new NoSuchElementException("I/O error: " + e.getMessage());
      }
  }

  /**
   * Returns the next group.
   */
  public Group nextGroup()
    throws IOException
  {
    String line = nextLine();

    // Parse line
    try
      {
        int start = 0, end;
        end = line.indexOf(' ', start);
        if (end == -1)
          return new Group(line, -1, -1, false);

        String name = line.substring(start, end);
        start = end + 1;
        end = line.indexOf(' ', start);
        int last = Integer.parseInt(line.substring(start, end));
        start = end + 1;
        end = line.indexOf(' ', start);
        int first = Integer.parseInt(line.substring(start, end));
        start = end + 1;
        boolean canPost = CAN_POST.equals(line.substring(start));

        return new Group(name, last, first, canPost);
      }
    catch (StringIndexOutOfBoundsException e)
      {
        ProtocolException e2 =
          new ProtocolException("Invalid group line: " + line);
        e2.initCause(e);
        throw e2;
      }
  }

}

