/*
 * HeaderEntry.java
 * Copyright (C) 2002 The free Software Foundation
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
 * An item in an NNTP newsgroup single-header listing.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public final class HeaderEntry
{

  String articleId;
  String header;

  HeaderEntry(String articleId, String header)
  {
    this.articleId = articleId;
    this.header = header;
  }

  /**
   * The article ID. This is either an article number, if a number or range
   * was used in the XHDR command, or a Message-ID.
   */
  public String getArticleId()
  {
    return articleId;
  }

  /**
   * The requested header value.
   */
  public String getHeader()
  {
    return header;
  }

}

