/*
 * PassiveModeDTP.java
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A passive mode FTP data transfer process.
 * This connects to the host specified and proxies the resulting socket's
 * input and output streams.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
final class PassiveModeDTP
  implements DTP
{

  final String address;
  final int port;
  Socket socket;
  DTPInputStream in;
  DTPOutputStream out;
  boolean completed;
  boolean inProgress;
  int transferMode;

  PassiveModeDTP(String address, int port, InetAddress localhost,
                 int connectionTimeout, int timeout)
    throws IOException
  {
    this.address = address;
    this.port = port;
    completed = false;
    inProgress = false;
    socket = new Socket();
    InetSocketAddress remote = new InetSocketAddress(address, port);
    InetSocketAddress local = new InetSocketAddress(localhost, port + 1);
    socket.bind(local);
    if (connectionTimeout > 0)
      {
        socket.connect(remote, connectionTimeout);
      }
    else
      {
        socket.connect(remote);
      }
    if (timeout > 0)
      {
        socket.setSoTimeout(timeout);
      }
  }

  /**
   * Returns an input stream from which a remote file can be read.
   */
  public InputStream getInputStream()
    throws IOException
  {
    if (inProgress)
      {
        throw new IOException("Transfer in progress");
      }
    switch (transferMode)
      {
      case FTPConnection.MODE_STREAM:
        in = new StreamInputStream(this, socket.getInputStream());
        break;
      case FTPConnection.MODE_BLOCK:
        in = new BlockInputStream(this, socket.getInputStream());
        break;
      case FTPConnection.MODE_COMPRESSED:
        in = new CompressedInputStream(this, socket.getInputStream());
        break;
      default:
        throw new IllegalStateException("Invalid transfer mode");
      }
    in.setTransferComplete(false);
    return in;
  }

  /**
   * Returns an output stream to which a local file can be written for
   * upload.
   */
  public OutputStream getOutputStream()
    throws IOException
  {
    if (inProgress)
      {
        throw new IOException("Transfer in progress");
      }
    switch (transferMode)
      {
      case FTPConnection.MODE_STREAM:
        out = new StreamOutputStream(this, socket.getOutputStream());
        break;
      case FTPConnection.MODE_BLOCK:
        out = new BlockOutputStream(this, socket.getOutputStream());
        break;
      case FTPConnection.MODE_COMPRESSED:
        out = new CompressedOutputStream(this, socket.getOutputStream());
        break;
      default:
        throw new IllegalStateException("Invalid transfer mode");
      }
    out.setTransferComplete(false);
    return out;
  }

  public void setTransferMode(int mode)
  {
    transferMode = mode;
  }

  public void complete()
  {
    completed = true;
    if (!inProgress)
      {
        transferComplete();
      }
  }

  public boolean abort()
  {
    completed = true;
    transferComplete();
    return inProgress;
  }

  /*
   * Called by DTPInputStream or DTPOutputStream when end of
   * stream is reached.
   */
  public void transferComplete()
  {
    if (in != null)
      {
        in.setTransferComplete(true);
      }
    if (out != null)
      {
        out.setTransferComplete(true);
      }
    inProgress = false;
    completed = completed ||(transferMode == FTPConnection.MODE_STREAM);
    if (completed && socket != null)
      {
        try
          {
            socket.close();
          }
        catch (IOException e)
          {
          }
      }
  }

}

