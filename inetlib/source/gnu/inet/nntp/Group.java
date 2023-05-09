/*
 * Group.java
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

/**
 * An item in an NNTP newsgroup listing.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class Group
{

  String name;
  int last;
  int first;
  boolean canPost;

  Group(String name, int last, int first, boolean canPost)
  {
    this.name = name;
    this.last = last;
    this.first = first;
    this.canPost = canPost;
  }

  /**
   * The name of the newsgroup.
   */
  public String getName()
  {
    return name;
  }

  /**
   * The number of the last known article currently in the newsgroup.
   */
  public int getLast()
  {
    return last;
  }

  /**
   * The number of the first article currently in the newsgroup.
   */
  public int getFirst()
  {
    return first;
  }

  /**
   * True if posting to this newsgroup is allowed.
   */
  public boolean isCanPost()
  {
    return canPost;
  }

}

