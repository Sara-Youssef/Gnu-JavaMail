/*
 * ComsatClient.java
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

package gnu.inet.comsat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.StringTokenizer;

/**
 * Simple comsat client.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class ComsatClient
{

  /**
   * The default comsat port.
   */
  public static final int DEFAULT_PORT = 512;

  protected DatagramSocket socket;

  public ComsatClient()
    throws IOException
  {
    this(DEFAULT_PORT, 0);
  }

  public ComsatClient(int port, int timeout)
    throws IOException
  {
    if (port < 0)
      {
        port = DEFAULT_PORT;
      }

    socket = new DatagramSocket(port);
    if (timeout > 0)
      {
        socket.setSoTimeout(timeout);
      }
    socket.setReceiveBufferSize(1024);
  }

  public void close()
    throws IOException
  {
    socket.close();
  }

  public ComsatInfo read()
    throws IOException
  {
    byte[] buf = new byte[socket.getReceiveBufferSize()];
    int len = buf.length;
    DatagramPacket packet = new DatagramPacket(buf, len);
    socket.receive(packet);
    buf = packet.getData();
    len = packet.getLength();
    String data = new String(buf, 0, len, "ISO-8859-1");

    ComsatInfo info = new ComsatInfo();
    StringTokenizer st = new StringTokenizer(data, "\n");
    String mailbox = st.nextToken();
    info.setMailbox(mailbox);
    boolean inBody = false;
    String lastHeader = null;
    while (st.hasMoreTokens())
      {
        String line = st.nextToken();
        if (inBody)
          {
            String body = info.getBody();
            if (body == null)
              {
                body = line;
              }
            else
              {
                body += "\n" + line;
              }
            info.setBody(body);
          }
        else
          {
            if (line.length() == 0)
              {
                inBody = true;
              }
            else
              {
                int ci = line.indexOf(':');
                if (ci != -1)
                  {
                    lastHeader = line.substring(0, ci);
                    info.setHeader(lastHeader,
                                   line.substring(ci + 1).trim());
                  }
                else
                  {
                    String val = info.getHeader(lastHeader);
                    val += "\n" + line;
                    info.setHeader(lastHeader, val);
                  }
              }
          }
      }
    return info;
  }

}

