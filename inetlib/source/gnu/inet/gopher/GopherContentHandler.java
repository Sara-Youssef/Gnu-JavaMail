/*
 * GopherContentHandler.java
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

import java.io.IOException;
import java.net.ContentHandler;
import java.net.UnknownServiceException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A Gopher content handler.
 * This will return either directory listings or input streams.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class GopherContentHandler
  extends ContentHandler
{

  public Object getContent(URLConnection urlc)
    throws IOException
  {
    if (urlc instanceof GopherURLConnection)
      {
        GopherURLConnection gurlc = (GopherURLConnection) urlc;
        GopherConnection connection = gurlc.connection;
        URL url = gurlc.getURL();
        String dir = url.getPath();
        String file = url.getFile();
        if (dir == null && file == null)
          {
            return connection.list();
          }
        else
          {
            return gurlc.getInputStream();
          }
      }
    else
      {
        throw new UnknownServiceException();
      }
  }

}

