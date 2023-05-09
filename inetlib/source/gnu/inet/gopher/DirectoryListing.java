/*
 * DirectoryListing.java
 * Copyright (C) 2003 The Free Software Foundation
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

package gnu.inet.gopher;

import java.io.InputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import gnu.inet.util.LineInputStream;

/**
 * A gopher directory listing.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class DirectoryListing
  implements Iterator
{

  private static final String DOT = ".";

  private LineInputStream in;
  private boolean doneRead = false;
  private DirectoryEntry current;

  DirectoryListing(InputStream in)
  {
    this.in = new LineInputStream(in);
  }

  /**
   * Indicates whether this listing contains more entries.
   */
  public boolean hasNext()
  {
    try
      {
        fetch();
      }
    catch (IOException e)
      {
        return false;
      }
    return (current != null);
  }

  /**
   * @see #nextEntry
   */
  public Object next()
  {
    try
      {
        return nextEntry();
      }
    catch (IOException e)
      {
        throw new NoSuchElementException("I/O error: " + e.getMessage());
      }
  }

  /**
   * This iterator is read-only.
   */
  public void remove()
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the next entry in the directory listing.
   */
  public DirectoryEntry nextEntry()
    throws IOException
  {
    fetch();
    if (current == null)
      {
        throw new NoSuchElementException();
      }
    doneRead = false;
    return current;
  }

  void fetch()
    throws IOException
  {
    if (doneRead)
      {
        return;
      }
    String line = in.readLine();
    if (DOT.equals(line))
      {
        current = null;
      }
    else
      {
        // Parse line
        int type = DirectoryEntry.ERROR;
        switch(line.charAt(0))
          {
          case '0':
            type = DirectoryEntry.FILE;
            break;
          case '1':
            type = DirectoryEntry.DIRECTORY;
            break;
          case '2':
            type = DirectoryEntry.CSO_PHONE_BOOK;
            break;
          case '3':
            type = DirectoryEntry.ERROR;
            break;
          case '4':
            type = DirectoryEntry.BINHEX;
            break;
          case '5':
            type = DirectoryEntry.DOS_ARCHIVE;
            break;
          case '6':
            type = DirectoryEntry.UUENCODED;
            break;
          case '7':
            type = DirectoryEntry.INDEX_SEARCH;
            break;
          case '8':
            type = DirectoryEntry.TELNET;
            break;
          case '9':
            type = DirectoryEntry.BINARY;
            break;
          case '+':
            type = DirectoryEntry.REDUNDANT;
            break;
          case 'T':
            type = DirectoryEntry.TN3270;
            break;
          case 'g':
            type = DirectoryEntry.GIF;
            break;
          case 'I':
            type = DirectoryEntry.IMAGE;
            break;
          }
        int start = 1;
        int end = line.indexOf('\t', start);
        if (end == -1)
          {
            throw new ProtocolException("Invalid directory entry: " + line);
          }
        String title = line.substring(start, end);
        start = end + 1;
        end = line.indexOf('\t', start);
        if (end == -1)
          {
            throw new ProtocolException("Invalid directory entry: " + line);
          }
        String selector = line.substring(start, end);
        start = end + 1;
        end = line.indexOf('\t', start);
        if (end == -1)
          {
            throw new ProtocolException("Invalid directory entry: " + line);
          }
        String hostname = line.substring(start, end);
        start = end + 1;
        int port = Integer.parseInt(line.substring(start));
        current = new DirectoryEntry(type, title, selector, hostname, port);
      }
  }

}

