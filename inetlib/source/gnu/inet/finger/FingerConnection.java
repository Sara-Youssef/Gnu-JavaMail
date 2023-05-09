/*
 * FingerConnection.java
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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A client for the finger protocol described in RFC 1288.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class FingerConnection
{

  /**
   * The default finger port.
   */
  public static final int DEFAULT_PORT = 79;

  /*
   * The US-ASCII encoding.
   */
  private static final String US_ASCII = "US-ASCII";

  /**
   * The underlying socket used for communications.
   */
  protected Socket socket;

  /**
   * If we want a verbose response.
   */
  protected boolean verbose;

  /**
   * Creates a new finger connection.
   * @param host the name of the internet host to connect to
   */
  public FingerConnection(String host)
    throws IOException
  {
    this(host, DEFAULT_PORT);
  }

  /**
   * Creates a new finger connection.
   * @param host the name of the internet host to connect to
   * @param port the port to connect to
   */
  public FingerConnection(String host, int port)
    throws IOException
  {
    socket = new Socket(host, port);
  }

  /**
   * Retrieves the verbose flag.
   * If true, the server should provide more output.
   */
  public boolean isVerbose()
  {
    return verbose;
  }

  /**
   * Sets the verbose flag.
   * If true, the server should provide more output.
   * @param verbose true for more verbose, false otherwise
   */
  public void setVerbose(boolean verbose)
  {
    this.verbose = verbose;
  }

  /**
   * Lists the available users.
   */
  public String list()
    throws IOException
  {
    return finger(null, null);
  }

  /**
   * Fingers the specified user.
   * @param username the user to finger
   * @return information about all matching users
   */
  public String finger(String username)
    throws IOException
  {
    return finger(username, null);
  }

  /**
   * Fingers the specified user at the specified host.
   * @param username the user to finger (null for any user)
   * @param hostname the domain of the user (null for any domain)
   * @return information about all matching users
   */
  public String finger(String username, String hostname)
    throws IOException
  {
    // Send the command
    OutputStream out = socket.getOutputStream();
    out = new BufferedOutputStream(out);
    if (verbose)
      {
        out.write('/');
        out.write('W');
        if (username != null || hostname != null)
          {
            out.write(' ');
          }
      }
    if (username != null)
      {
        out.write(username.getBytes(US_ASCII));
      }
    if (hostname != null)
      {
        out.write('@');
        out.write(hostname.getBytes(US_ASCII));
      }
    out.write('\r');
    out.write('\n');
    out.flush();

    // Read the response
    InputStream in = socket.getInputStream();
    ByteArrayOutputStream acc = new ByteArrayOutputStream();
    byte[] buf = new byte[4096];
    for (int len = in.read(buf); len != -1; len = in.read(buf))
      {
        acc.write(buf, 0, len);
      }
    return acc.toString(US_ASCII);
  }

}

