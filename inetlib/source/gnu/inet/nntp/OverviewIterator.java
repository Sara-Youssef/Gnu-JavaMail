/*
 * OverviewIterator.java
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
 * An iterator over an overview listing.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class OverviewIterator
  extends LineIterator
{

  OverviewIterator(NNTPConnection connection)
  {
    super(connection);
  }

  /**
   * Returns the next overview entry.
   */
  public Object next()
  {
    try
      {
        return nextOverview();
      }
    catch (IOException e)
      {
        throw new NoSuchElementException("I/O error: " + e.getMessage());
      }
  }

  /**
   * Returns the next overview entry.
   */
  public Overview nextOverview()
    throws IOException
  {
    String line = nextLine();

    try
      {
        // Parse line
        int start = 0, end;
        end = line.indexOf('\t', start);
        int articleNumber = Integer.parseInt(line.substring(start, end));
        start = end + 1;
        Overview overview = new Overview(articleNumber);
        end = line.indexOf('\t', start);
        while(end > -1)
          {
            String entry = line.substring(start, end);
            overview.add(entry);
            start = end + 1;
            end = line.indexOf('\t', start);
          }
        String entry = line.substring(start);
        overview.add(entry);

        return overview;
      }
    catch (StringIndexOutOfBoundsException e)
      {
        ProtocolException e2 =
          new ProtocolException("Invalid overview line: " + line);
        e2.initCause(e);
        throw e2;
      }
    catch (NumberFormatException e)
      {
        ProtocolException e2 =
          new ProtocolException("Invalid overview line: " + line);
        e2.initCause(e);
        throw e2;
      }
  }

}

