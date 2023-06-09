/*
 * URLDataSource.java
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
package javax.activation;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * DataSource implementation that retrieves its data from a URL.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public class URLDataSource
    implements DataSource
{

  private URL url;
  private URLConnection connection;

  /**
   * Constructor.
   * This will not open the connection to the URL.
   */
  public URLDataSource(URL url)
  {
    this.url = url;
  }

  /**
   * Returns the Content-Type header for the URL.
   * In the case of failure or lack of such a header,
   * returns "application/octet-stream".
   */
  public String getContentType()
  {
    try
      {
        if (connection == null)
          {
            connection = url.openConnection();
          }
      }
    catch (IOException e)
      {
      }
    String contentType = null;
    if (connection != null)
      {
        contentType = connection.getContentType();
      }
    if (contentType == null)
      {
        contentType = "application/octet-stream";
      }
    return contentType;
  }

  /**
   * Returns the result of <code>getFile</code> of the underlying URL.
   */
  public String getName()
  {
    return url.getFile();
  }

  public InputStream getInputStream()
    throws IOException
  {
    connection = url.openConnection();
    if (connection != null)
      {
        connection.setDoInput(true);
        return connection.getInputStream();
      }
    return null;
  }

  public OutputStream getOutputStream()
    throws IOException
  {
    connection = url.openConnection();
    if (connection != null)
      {
        connection.setDoOutput(true);
        return connection.getOutputStream();
      }
    return null;
  }

  /**
   * Returns the underlying URL.
   */
  public URL getURL()
  {
    return url;
  }

}

