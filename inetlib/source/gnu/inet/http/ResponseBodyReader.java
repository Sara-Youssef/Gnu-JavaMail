/*
 * ResponseBodyReader.java
 * Copyright (C) 2004 The Free Software Foundation
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

package gnu.inet.http;

/**
 * Callback interface for receiving notification of response body content.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public interface ResponseBodyReader
{

  /**
   * Indicate whether this reader is interested in the specified response.
   * If it returns false, it will not receive body content notifications for
   * that response.
   */
  boolean accept(Request request, Response response);

  /**
   * Receive notification of body content.
   * @param buffer the content buffer
   * @param offset the offset within the buffer that content starts
   * @param length the length of the content
   */
  void read(byte[] buffer, int offset, int length);

  /**
   * Notifies the reader that the end of the content was reached.
   */
  void close();

}

