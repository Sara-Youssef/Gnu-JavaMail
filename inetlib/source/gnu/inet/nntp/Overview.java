/*
 * Overview.java
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

import java.util.List;
import java.util.ArrayList;

/**
 * An overview entry.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class Overview
{

  int articleNumber;
  private List headers;

  Overview(int articleNumber)
  {
    this.articleNumber = articleNumber;
    headers = new ArrayList(8);
  }

  void add(String header)
  {
    headers.add(header);
  }

  /**
   * Returns the article number this overview entry is associated with.
   */
  public int getArticleNumber()
  {
    return articleNumber;
  }

  /**
   * Returns the header at the specified index.
   */
  public String getHeader(int index)
  {
    return (String) headers.get(index);
  }

}

