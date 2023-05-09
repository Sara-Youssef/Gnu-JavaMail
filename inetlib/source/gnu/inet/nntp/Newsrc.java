/*
 * Newsrc.java
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

import java.util.Iterator;

/**
 * Interface for a .newsrc configuration.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public interface Newsrc
{

  /**
   * Returns an iterator over the names of the subscribed newsgroups.
   * Each item returned is a String.
   */
  public Iterator list();

  /**
   * Indicates whether a newsgroup is subscribed in this newsrc.
   */
  public boolean isSubscribed(String newsgroup);

  /**
   * Sets whether a newsgroup is subscribed in this newsrc.
   */
  public void setSubscribed(String newsgroup, boolean subs);

  /**
   * Indicates whether an article is marked as seen in the specified newsgroup.
   */
  public boolean isSeen(String newsgroup, int article);

  /**
   * Sets whether an article is marked as seen in the specified newsgroup.
   */
  public void setSeen(String newsgroup, int article, boolean seen);

  /**
   * Closes the configuration, potentially saving any changes.
   */
  public void close();

}

