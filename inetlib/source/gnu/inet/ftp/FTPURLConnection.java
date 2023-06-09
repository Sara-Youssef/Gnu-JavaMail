/*
 * FTPURLConnection.java
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

package gnu.inet.ftp;

import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import gnu.inet.util.GetLocalHostAction;
import gnu.inet.util.GetSystemPropertyAction;

/**
 * An FTP URL connection.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class FTPURLConnection
  extends URLConnection
{

  /**
   * The connection managing the protocol exchange.
   */
  protected FTPConnection connection;

  protected boolean passive;
  protected int representationType;
  protected int fileStructure;
  protected int transferMode;

  /**
   * Constructs an FTP connection to the specified URL.
   * @param url the URL
   */
  public FTPURLConnection(URL url)
  {
    super(url);
    passive = true;
    representationType = FTPConnection.TYPE_BINARY;
    fileStructure = -1;
    transferMode = -1;
  }

  /**
   * Establishes the connection.
   */
  public void connect()
    throws IOException
  {
    if (connected)
      {
        return;
      }
    String host = url.getHost();
    int port = url.getPort();
    String username = url.getUserInfo();
    String password = null;
    if (username != null)
      {
        int ci = username.indexOf(':');
        if (ci != -1)
          {
            password = username.substring(ci + 1);
            username = username.substring(0, ci);
          }
      }
    else
      {
        username = "anonymous";
        PrivilegedAction a = new GetSystemPropertyAction("user.name");
        String systemUsername =(String) AccessController.doPrivileged(a);
        a = new GetLocalHostAction();
        InetAddress localhost =(InetAddress) AccessController.doPrivileged(a);
        password = systemUsername + "@" +
          ((localhost == null) ? "localhost" : localhost.getHostName());
      }
    connection = new FTPConnection(host, port);
    if (!connection.authenticate(username, password))
      {
        throw new SecurityException("Authentication failed");
      }
    connection.setPassive(passive);
    if (representationType != -1)
      {
        connection.setRepresentationType(representationType);
      }
    if (fileStructure != -1)
      {
        connection.setFileStructure(fileStructure);
      }
    if (transferMode != -1)
      {
        connection.setTransferMode(transferMode);
      }
  }

  /**
   * This connection supports doInput.
   */
  public void setDoInput(boolean doinput)
  {
    doInput = doinput;
  }

  /**
   * This connection supports doOutput.
   */
  public void setDoOutput(boolean dooutput)
  {
    doOutput = dooutput;
  }

  /**
   * Returns an input stream that reads from this open connection.
   */
  public InputStream getInputStream()
    throws IOException
  {
    if (!connected)
      {
        connect();
      }
    String path = url.getPath();
    if (path.startsWith("/"))
      {
        path = path.substring(1);
      }
    if (connection.changeWorkingDirectory(path))
      {
        return this.new ClosingInputStream(connection.list(null));
      }
    else
      {
        return this.new ClosingInputStream(connection.retrieve(path));
      }
  }

  /**
   * Returns an output stream that writes to this connection.
   */
  public OutputStream getOutputStream()
    throws IOException
  {
    if (!connected)
      {
        connect();
      }
    String path = url.getPath();
    return this.new ClosingOutputStream(connection.store(path));
  }

  public String getRequestProperty(String key)
  {
    if ("passive".equals(key))
      {
        return Boolean.toString(passive);
      }
    else if ("representationType".equals(key))
      {
        switch (representationType)
          {
          case FTPConnection.TYPE_ASCII:
            return "ASCII";
          case FTPConnection.TYPE_EBCDIC:
            return "EBCDIC";
          case FTPConnection.TYPE_BINARY:
            return "BINARY";
          }
      }
    else if ("fileStructure".equals(key))
      {
        switch (fileStructure)
          {
          case FTPConnection.STRUCTURE_FILE:
            return "FILE";
          case FTPConnection.STRUCTURE_RECORD:
            return "RECORD";
          case FTPConnection.STRUCTURE_PAGE:
            return "PAGE";
          }
      }
    else if ("transferMode".equals(key))
      {
        switch (transferMode)
          {
          case FTPConnection.MODE_STREAM:
            return "STREAM";
          case FTPConnection.MODE_BLOCK:
            return "BLOCK";
          case FTPConnection.MODE_COMPRESSED:
            return "COMPRESSED";
          }
      }
    return null;
  }

  public Map getRequestProperties()
  {
    Map map = new HashMap();
    addRequestPropertyValue(map, "passive");
    addRequestPropertyValue(map, "representationType");
    addRequestPropertyValue(map, "fileStructure");
    addRequestPropertyValue(map, "transferMode");
    return map;
  }

  private void addRequestPropertyValue(Map map, String key)
  {
    String value = getRequestProperty(key);
    map.put(key, value);
  }

  public void setRequestProperty(String key, String value)
  {
    if (connected)
      {
        throw new IllegalStateException();
      }
    if ("passive".equals(key))
      {
        passive = Boolean.valueOf(value).booleanValue();
      }
    else if ("representationType".equals(key))
      {
        if ("A".equalsIgnoreCase(value) ||
            "ASCII".equalsIgnoreCase(value))
          {
            representationType = FTPConnection.TYPE_ASCII;
          }
        else if ("E".equalsIgnoreCase(value) ||
                 "EBCDIC".equalsIgnoreCase(value))
          {
            representationType = FTPConnection.TYPE_EBCDIC;
          }
        else if ("I".equalsIgnoreCase(value) ||
                 "BINARY".equalsIgnoreCase(value))
          {
            representationType = FTPConnection.TYPE_BINARY;
          }
        else
          {
            throw new IllegalArgumentException(value);
          }
      }
    else if ("fileStructure".equals(key))
      {
        if ("F".equalsIgnoreCase(value) ||
            "FILE".equalsIgnoreCase(value))
          {
            fileStructure = FTPConnection.STRUCTURE_FILE;
          }
        else if ("R".equalsIgnoreCase(value) ||
                 "RECORD".equalsIgnoreCase(value))
          {
            fileStructure = FTPConnection.STRUCTURE_RECORD;
          }
        else if ("P".equalsIgnoreCase(value) ||
                 "PAGE".equalsIgnoreCase(value))
          {
            fileStructure = FTPConnection.STRUCTURE_PAGE;
          }
        else
          {
            throw new IllegalArgumentException(value);
          }
      }
    else if ("transferMode".equals(key))
      {
        if ("S".equalsIgnoreCase(value) ||
            "STREAM".equalsIgnoreCase(value))
          {
            transferMode = FTPConnection.MODE_STREAM;
          }
        else if ("B".equalsIgnoreCase(value) ||
                 "BLOCK".equalsIgnoreCase(value))
          {
            transferMode = FTPConnection.MODE_BLOCK;
          }
        else if ("C".equalsIgnoreCase(value) ||
                 "COMPRESSED".equalsIgnoreCase(value))
          {
            transferMode = FTPConnection.MODE_COMPRESSED;
          }
        else
          {
            throw new IllegalArgumentException(value);
          }
      }
  }

  public void addRequestProperty(String key, String value)
  {
    setRequestProperty(key, value);
  }

  class ClosingInputStream
    extends FilterInputStream
  {

    ClosingInputStream(InputStream in)
    {
      super(in);
    }

    public void close()
      throws IOException
    {
      super.close();
      connection.logout();
    }

  }

  class ClosingOutputStream
    extends FilterOutputStream
  {

    ClosingOutputStream(OutputStream out)
    {
      super(out);
    }

    public void close()
      throws IOException
    {
      super.close();
      connection.logout();
    }

  }

}

