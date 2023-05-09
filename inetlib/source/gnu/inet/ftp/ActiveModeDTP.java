/*
 * ActiveModeDTP.java
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
import java.net.ServerSocket;
import java.net.Socket;

/**
 * An active mode FTP data transfer process.
 * This starts a server on the specified port listening for a data
 * connection. It converts the socket input into a file stream for reading.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
final class ActiveModeDTP
  implements DTP, Runnable
{

  ServerSocket server;
  Socket socket;
  DTPInputStream in;
  DTPOutputStream out;
  boolean completed;
  boolean inProgress;
  int transferMode;
  IOException exception;
  Thread acceptThread;
  int connectionTimeout;

  ActiveModeDTP(InetAddress localhost, int port,
                int connectionTimeout, int timeout)
    throws IOException
  {
    completed = false;
    inProgress = false;
    server = new ServerSocket(port, 1, localhost);
    if (timeout > 0)
      {
        server.setSoTimeout(timeout);
      }
    if (connectionTimeout <= 0)
      {
        connectionTimeout = 20000;
      }
    this.connectionTimeout = connectionTimeout;
    acceptThread = new Thread(this, "ActiveModeDTP");
    acceptThread.setDaemon(true);
    acceptThread.start();
  }

  /**
   * Start listening.
   */
  public void run()
  {
    try
      {
        socket = server.accept();
        //System.err.println("Accepted connection from "+socket.getInetAddress()+":"+socket.getPort());
      }
    catch (IOException e)
      {
        exception = e;
      }
  }

  /**
   * Waits until a client has connected.
   */
  public void waitFor()
    throws IOException
  {
    try
      {
        acceptThread.join(connectionTimeout);
      }
    catch (InterruptedException e)
      {
      }
    if (exception != null)
      {
        throw exception;
      }
    if (socket == null)
      {
        server.close();
        throw new IOException("client did not connect before timeout");
      }
    acceptThread = null;
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
    if (acceptThread != null)
      {
        waitFor();
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
        throw new IllegalStateException("invalid transfer mode");
      }
    in.setTransferComplete(false);
    return in;
  }

  /**
   * Returns an output stream to which a local file can be written for
   * upload.
   */
  public OutputStream getOutputStream() throws IOException
  {
    if (inProgress)
      {
        throw new IOException("Transfer in progress");
      }
    if (acceptThread != null)
      {
        waitFor();
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
        throw new IllegalStateException("invalid transfer mode");
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

  public void transferComplete()
  {
    if (socket == null)
      {
        return;
      }
    if (in != null)
      {
        in.setTransferComplete(true);
      }
    if (out != null)
      {
        out.setTransferComplete(true);
      }
    completed = completed || (transferMode == FTPConnection.MODE_STREAM);
    if (completed && socket != null)
      {
        try
          {
            socket.close();
          }
        catch (IOException e)
          {
          }
        try
          {
            server.close();
          }
        catch (IOException e)
          {
          }
      }
  }

}

