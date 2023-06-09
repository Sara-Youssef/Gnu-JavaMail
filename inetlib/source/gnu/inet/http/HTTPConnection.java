/*
 * HTTPConnection.java
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import gnu.inet.http.event.ConnectionEvent;
import gnu.inet.http.event.ConnectionListener;
import gnu.inet.http.event.RequestEvent;
import gnu.inet.http.event.RequestListener;
import gnu.inet.util.GetSystemPropertyAction;
import gnu.inet.util.EmptyX509TrustManager;

/**
 * A connection to an HTTP server.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class HTTPConnection
{

  static final ResourceBundle L10N =
    ResourceBundle.getBundle("gnu.inet.http.L10N");

  /**
   * The default HTTP port.
   */
  public static final int HTTP_PORT = 80;

  /**
   * The default HTTPS port.
   */
  public static final int HTTPS_PORT = 443;

  private static final String userAgent;
  static
  {
    String version = "inetlib/1.2";
    String osName = (String) AccessController
      .doPrivileged(new GetSystemPropertyAction("os.name"));
    String osArch = (String) AccessController
      .doPrivileged(new GetSystemPropertyAction("os.arch"));
    String userLanguage = (String) AccessController
      .doPrivileged(new GetSystemPropertyAction("user.language"));
    if (osName != null && osArch != null && userLanguage != null)
      {
        userAgent = MessageFormat.format("{0} ({1}; {2}; {3})", version,
                                         osName, osArch, userLanguage);
      }
    else
      {
        userAgent = version;
      }
  }

  private static final String acceptEncoding =
    "chunked;q=1.0, gzip;q=0.9, deflate;q=0.8, identity;q=0.6, *;q=0";

  /**
   * The host name of the server to connect to.
   */
  protected final String hostname;

  /**
   * The port to connect to.
   */
  protected final int port;

  /**
   * Whether the connection should use transport level security (HTTPS).
   */
  protected final boolean secure;

  /**
   * The connection timeout for connecting the underlying socket.
   */
  protected final int connectionTimeout;

  /**
   * The read timeout for reads on the underlying socket.
   */
  protected final int timeout;

  /**
   * The host name of the proxy to connect to.
   */
  protected String proxyHostname;

  /**
   * The port on the proxy to connect to.
   */
  protected int proxyPort;

  /**
   * The major version of HTTP supported by this client.
   */
  protected int majorVersion;

  /**
   * The minor version of HTTP supported by this client.
   */
  protected int minorVersion;

  private final List<ConnectionListener> connectionListeners;
  private final List<RequestListener> requestListeners;

  /**
   * The socket this connection communicates on.
   */
  protected Socket socket;

  /**
   * The socket input stream.
   */
  protected InputStream in;

  /**
   * The socket output stream.
   */
  protected OutputStream out;

  /**
   * Nonce values seen by this connection.
   */
  private Map nonceCounts;

  /**
   * The cookie manager for this connection.
   */
  protected CookieManager cookieManager;

  static final int HTTP20_UNKNOWN = 0;
  static final int HTTP20_OK = 1;
  static final int HTTP20_NO = -1;

  /**
   * Negotiate HTTP/2.0.
   */
  int http20 = HTTP20_NO; // TODO make UNKNOWN when we support

  /**
   * Creates a new HTTP connection.
   * @param hostname the name of the host to connect to
   */
  public HTTPConnection(String hostname)
  {
    this(hostname, HTTP_PORT, false, 0, 0);
  }

  /**
   * Creates a new HTTP or HTTPS connection.
   * @param hostname the name of the host to connect to
   * @param secure whether to use a secure connection
   */
  public HTTPConnection(String hostname, boolean secure)
  {
    this(hostname, secure ? HTTPS_PORT : HTTP_PORT, secure, 0, 0);
  }

  /**
   * Creates a new HTTP or HTTPS connection on the specified port.
   * @param hostname the name of the host to connect to
   * @param secure whether to use a secure connection
   * @param connectionTimeout the connection timeout
   * @param timeout the socket read timeout
   */
  public HTTPConnection(String hostname, boolean secure,
                        int connectionTimeout, int timeout)
  {
    this(hostname, secure ? HTTPS_PORT : HTTP_PORT, secure,
         connectionTimeout, timeout);
  }

  /**
   * Creates a new HTTP connection on the specified port.
   * @param hostname the name of the host to connect to
   * @param port the port on the host to connect to
   */
  public HTTPConnection(String hostname, int port)
  {
    this(hostname, port, false, 0, 0);
  }

  /**
   * Creates a new HTTP or HTTPS connection on the specified port.
   * @param hostname the name of the host to connect to
   * @param port the port on the host to connect to
   * @param secure whether to use a secure connection
   */
  public HTTPConnection(String hostname, int port, boolean secure)
  {
    this(hostname, port, secure, 0, 0);
  }

  /**
   * Creates a new HTTP or HTTPS connection on the specified port.
   * @param hostname the name of the host to connect to
   * @param port the port on the host to connect to
   * @param secure whether to use a secure connection
   * @param connectionTimeout the connection timeout
   * @param timeout the socket read timeout
   */
  public HTTPConnection(String hostname, int port, boolean secure,
                        int connectionTimeout, int timeout)
  {
    this.hostname = hostname;
    this.port = port;
    this.secure = secure;
    this.connectionTimeout = connectionTimeout;
    this.timeout = timeout;
    majorVersion = minorVersion = 1;
    connectionListeners = new ArrayList<ConnectionListener>(4);
    requestListeners = new ArrayList<RequestListener>(4);
  }

  /**
   * Returns the name of the host to connect to.
   */
  public String getHostName()
  {
    return hostname;
  }

  /**
   * Returns the port on the host to connect to.
   */
  public int getPort()
  {
    return port;
  }

  /**
   * Indicates whether to use a secure connection or not.
   */
  public boolean isSecure()
  {
    return secure;
  }

  /**
   * Returns the HTTP version string supported by this connection.
   * @see #version
   */
  public String getVersion()
  {
    return "HTTP/" + majorVersion + '.' + minorVersion;
  }

  /**
   * Sets the HTTP version supported by this connection.
   * @param major the major version
   * @param minor the minor version
   */
  public void setVersion(int major, int minor)
  {
    if ((major == 1 && minor >= 0 && minor <= 1) ||
        (major == 2 && minor == 0))
      {
        this.majorVersion = major;
        this.minorVersion = minor;
        if (major == 1)
          {
            http20 = HTTP20_NO;
          }
      }
    else
      {
        String message = L10N.getString("err.unsupported_version");
        message = MessageFormat.format(message, major, minor);
        throw new IllegalArgumentException(message);
      }
  }

  /**
   * Directs this connection to use the specified proxy.
   * @param hostname the proxy host name
   * @param port the port on the proxy to connect to
   */
  public void setProxy(String hostname, int port)
  {
    proxyHostname = hostname;
    proxyPort = port;
  }

  /**
   * Indicates whether this connection is using an HTTP proxy.
   */
  public boolean isUsingProxy()
  {
    return (proxyHostname != null && proxyPort > 0);
  }

  /**
   * Sets the cookie manager to use for this connection.
   * @param cookieManager the cookie manager
   */
  public void setCookieManager(CookieManager cookieManager)
  {
    this.cookieManager = cookieManager;
  }

  /**
   * Returns the cookie manager in use for this connection.
   */
  public CookieManager getCookieManager()
  {
    return cookieManager;
  }

  /**
   * Creates a new request using this connection.
   * @param method the HTTP method to invoke
   * @param path the URI-escaped RFC2396 <code>abs_path</code> with
   * optional query part
   */
  public Request newRequest(String method, String path)
  {
    if (method == null || method.length() == 0)
      {
        String message = L10N.getString("err.bad_method");
        message = MessageFormat.format(message, method);
        throw new IllegalArgumentException(message);
      }
    if (path == null || path.length() == 0)
      {
        path = "/";
      }
    Request ret = new Request(this, method, path);
    switch (http20)
      {
      case HTTP20_UNKNOWN:
        ret.setHeader("Connection", "Upgrade, HTTP2-Settings");
        ret.setHeader("Upgrade", "HTTP/2.0");
        ret.setHeader("HTTP2-Settings", null);; // TODO
        // fall through
      case HTTP20_NO:
        if ((secure && port != HTTPS_PORT) ||
            (!secure && port != HTTP_PORT))
          {
            ret.setHeader("Host", hostname + ":" + port);
          }
        else
          {
            ret.setHeader("Host", hostname);
          }
        ret.setHeader("User-Agent", userAgent);
        ret.setHeader("Accept-Encoding", acceptEncoding);
        if (http20 == HTTP20_NO)
          {
            ret.setHeader("Connection", "keep-alive");
          }
      }
    if (cookieManager != null)
      {
        Cookie[] cookies = cookieManager.getCookies(hostname, secure, path);
        if (cookies != null && cookies.length > 0)
          {
            StringBuffer buf = new StringBuffer();
            buf.append("$Version=1");
            for (int i = 0; i < cookies.length; i++)
              {
                buf.append(',');
                buf.append(' ');
                buf.append(cookies[i].toString());
              }
            ret.setHeader("Cookie", buf.toString());
          }
      }
    fireRequestEvent(RequestEvent.REQUEST_CREATED, ret);
    return ret;
  }

  /**
   * Closes this connection.
   */
  public void close()
    throws IOException
  {
    try
      {
        closeConnection();
      }
    finally
      {
        fireConnectionEvent(ConnectionEvent.CONNECTION_CLOSED);
      }
  }

  /**
   * Retrieves the socket associated with this connection.
   * This creates the socket if necessary.
   */
  protected Socket getSocket()
    throws IOException
  {
    if (socket == null)
      {
        String connectHostname = hostname;
        int connectPort = port;
        if (isUsingProxy())
          {
            connectHostname = proxyHostname;
            connectPort = proxyPort;
          }
        socket = new Socket();
        InetAddress address = InetAddress.getByName(connectHostname);
        InetSocketAddress socketAddress =
          new InetSocketAddress(address, connectPort);
        if (connectionTimeout > 0)
          {
            socket.connect(socketAddress, connectionTimeout);
          }
        else
          {
            socket.connect(socketAddress);
          }
        if (timeout > 0)
          {
            socket.setSoTimeout(timeout);
          }
        if (secure)
          {
            try
              {
                TrustManager tm = new EmptyX509TrustManager();
                SSLContext context = SSLContext.getInstance("SSL");
                TrustManager[] trust = new TrustManager[] { tm };
                context.init(null, trust, null);
                SSLSocketFactory factory =  context.getSocketFactory();
                SSLSocket ss =
                  (SSLSocket) factory.createSocket(socket, connectHostname,
                                                   connectPort, true);
                String[] protocols = { "TLSv1", "SSLv3" };
                ss.setEnabledProtocols(protocols);
                ss.setUseClientMode(true);
                ss.startHandshake();
                socket = ss;
              }
            catch (GeneralSecurityException e)
              {
                throw new IOException(e.getMessage());
              }
          }
        in = socket.getInputStream();
        in = new BufferedInputStream(in);
        out = socket.getOutputStream();
        out = new BufferedOutputStream(out);
      }
    return socket;
  }

  protected InputStream getInputStream()
    throws IOException
  {
    if (socket == null)
      {
        getSocket();
      }
    return in;
  }

  protected OutputStream getOutputStream()
    throws IOException
  {
    if (socket == null)
      {
        getSocket();
      }
    return out;
  }

  /**
   * Closes the underlying socket, if any.
   */
  protected void closeConnection()
    throws IOException
  {
    if (socket != null)
      {
        try
          {
            socket.close();
          }
        finally
          {
            socket = null;
          }
      }
  }

  /**
   * Returns a URI representing the connection.
   * This does not include any request path component.
   */
  protected String getURI()
  {
    StringBuffer buf = new StringBuffer();
    buf.append(secure ? "https://" : "http://");
    buf.append(hostname);
    if (secure)
      {
        if (port != HTTPConnection.HTTPS_PORT)
          {
            buf.append(':');
            buf.append(port);
          }
      }
    else
      {
        if (port != HTTPConnection.HTTP_PORT)
          {
            buf.append(':');
            buf.append(port);
          }
      }
    return buf.toString();
  }

  /**
   * Get the number of times the specified nonce has been seen by this
   * connection.
   */
  int getNonceCount(String nonce)
  {
    if (nonceCounts == null)
      {
        return 0;
      }
    return((Integer) nonceCounts.get(nonce)).intValue();
  }

  /**
   * Increment the number of times the specified nonce has been seen.
   */
  void incrementNonce(String nonce)
  {
    int current = getNonceCount(nonce);
    if (nonceCounts == null)
      {
        nonceCounts = new HashMap();
      }
    nonceCounts.put(nonce, new Integer(current + 1));
  }

  // -- Events --

  public void addConnectionListener(ConnectionListener l)
  {
    synchronized (connectionListeners)
      {
        connectionListeners.add(l);
      }
  }

  public void removeConnectionListener(ConnectionListener l)
  {
    synchronized (connectionListeners)
      {
        connectionListeners.remove(l);
      }
  }

  protected void fireConnectionEvent(int type)
  {
    ConnectionEvent event = new ConnectionEvent(this, type);
    ConnectionListener[] l = null;
    synchronized (connectionListeners)
      {
        l = new ConnectionListener[connectionListeners.size()];
        connectionListeners.toArray(l);
      }
    for (int i = 0; i < l.length; i++)
      {
        switch (type)
          {
          case ConnectionEvent.CONNECTION_CLOSED:
            l[i].connectionClosed(event);
            break;
          }
      }
  }

  public void addRequestListener(RequestListener l)
  {
    synchronized (requestListeners)
      {
        requestListeners.add(l);
      }
  }

  public void removeRequestListener(RequestListener l)
  {
    synchronized (requestListeners)
      {
        requestListeners.remove(l);
      }
  }

  protected void fireRequestEvent(int type, Request request)
  {
    RequestEvent event = new RequestEvent(this, type, request);
    RequestListener[] l = null;
    synchronized (requestListeners)
      {
        l = new RequestListener[requestListeners.size()];
        requestListeners.toArray(l);
      }
    for (int i = 0; i < l.length; i++)
      {
        switch (type)
          {
          case RequestEvent.REQUEST_CREATED:
            l[i].requestCreated(event);
            break;
          case RequestEvent.REQUEST_SENDING:
            l[i].requestSent(event);
            break;
          case RequestEvent.REQUEST_SENT:
            l[i].requestSent(event);
            break;
          }
      }
  }

}

