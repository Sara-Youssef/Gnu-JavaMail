/*
 * FingerURLConnection.java
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

package gnu.inet.finger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A URL connection that uses the finger protocol.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class FingerURLConnection
  extends URLConnection
{

  FingerConnection connection;
  String response;

  FingerURLConnection(URL url)
    throws IOException
  {
    super(url);
  }

  public void connect()
    throws IOException
  {
    if (connection != null)
      {
        return;
      }
    connection = new FingerConnection(url.getHost(), url.getPort());
    response = connection.finger(url.getUserInfo());
  }

  public InputStream getInputStream()
    throws IOException
  {
    if (!connected)
      {
        connect();
      }
    byte[] bytes = response.getBytes("US-ASCII");
    return new ByteArrayInputStream(bytes);
  }

}

