/*
 * ArticleResponse.java
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

import java.io.InputStream;

/**
 * An NNTP article status response.
 * This represents the status response associated with NNTP status codes
 * 220-223, including an article number and a message-id.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class ArticleResponse
  extends StatusResponse
{

  /*
   * The article number.
   */
  public int articleNumber;

  /*
   * The message-id.
   */
  public String messageId;

  /**
   * If the status code for this response is one of:
   * <ul>
   * <li>ARTICLE_FOLLOWS
   * <li>HEAD_FOLLOWS
   * <li>BODY_FOLLOWS
   * </ul>
   * then this stream can be used to retrieve the byte content of the article
   * retrieved. Otherwise, it will be null. If it is non-null, the stream
   * must be read in its entirety before further methods can be invoked on
   * the NNTPConnection.
   */
  public InputStream in;

  protected ArticleResponse(short status, String message)
  {
    super(status, message);
  }

}

