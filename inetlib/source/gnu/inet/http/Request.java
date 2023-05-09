/*
 * Request.java
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

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import gnu.inet.http.event.RequestEvent;
import gnu.inet.util.BASE64;
import gnu.inet.util.LineInputStream;

/**
 * A single HTTP request.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class Request
{

  /**
   * The connection context in which this request is invoked.
   */
  protected final HTTPConnection connection;

  /**
   * The HTTP method to invoke.
   */
  protected final String method;

  /**
   * The path identifying the resource.
   * This string must conform to the abs_path definition given in RFC2396,
   * with an optional "?query" part, and must be URI-escaped by the caller.
   */
  protected final String path;

  /**
   * The headers in this request.
   */
  protected final Headers requestHeaders;

  /**
   * The request body provider.
   */
  protected RequestBodyWriter requestBodyWriter;

  /**
   * Request body negotiation threshold for 100-continue expectations.
   */
  protected long requestBodyNegotiationThreshold;

  /**
   * The response body reader.
   */
  protected ResponseBodyReader responseBodyReader;

  /**
   * Map of response header handlers.
   */
  protected Map responseHeaderHandlers;

  /**
   * The authenticator.
   */
  protected Authenticator authenticator;

  /**
   * Whether this request has been dispatched yet.
   */
  private boolean dispatched;

  /**
   * Constructor for a new request.
   * @param connection the connection context
   * @param method the HTTP method
   * @param path the resource path including query part
   */
  protected Request(HTTPConnection connection, String method,
                    String path)
  {
    this.connection = connection;
    this.method = method;
    this.path = path;
    requestHeaders = new Headers();
    responseHeaderHandlers = new HashMap();
    requestBodyNegotiationThreshold = 4096L;
  }

  /**
   * Returns the connection associated with this request.
   * @see #connection
   */
  public HTTPConnection getConnection()
  {
    return connection;
  }

  /**
   * Returns the HTTP method to invoke.
   * @see #method
   */
  public String getMethod()
  {
    return method;
  }

  /**
   * Returns the resource path.
   * @see #path
   */
  public String getPath()
  {
    return path;
  }

  /**
   * Returns the full request-URI represented by this request, as specified
   * by HTTP/1.1.
   */
  public String getRequestURI()
  {
    return connection.getURI() + path;
  }

  /**
   * Returns the headers in this request.
   */
  public Headers getHeaders()
  {
    return requestHeaders;
  }

  /**
   * Returns the value of the specified header in this request.
   * @param name the header name
   */
  public String getHeader(String name)
  {
    return requestHeaders.getValue(name);
  }

  /**
   * Returns the value of the specified header in this request as an integer.
   * @param name the header name
   */
  public int getIntHeader(String name)
  {
    return requestHeaders.getIntValue(name);
  }

  /**
   * Returns the value of the specified header in this request as a date.
   * @param name the header name
   */
  public Date getDateHeader(String name)
  {
    return requestHeaders.getDateValue(name);
  }

  /**
   * Sets the specified header in this request.
   * @param name the header name
   * @param value the header value
   */
  public void setHeader(String name, String value)
  {
    requestHeaders.put(name, value);
  }

  /**
   * Convenience method to set the entire request body.
   * @param requestBody the request body content
   */
  public void setRequestBody(byte[] requestBody)
  {
    setRequestBodyWriter(new ByteArrayRequestBodyWriter(requestBody));
  }

  /**
   * Sets the request body provider.
   * @param requestBodyWriter the handler used to obtain the request body
   */
  public void setRequestBodyWriter(RequestBodyWriter requestBodyWriter)
  {
    this.requestBodyWriter = requestBodyWriter;
  }

  /**
   * Sets the response body reader.
   * @param responseBodyReader the handler to receive notifications of
   * response body content
   */
  public void setResponseBodyReader(ResponseBodyReader responseBodyReader)
  {
    this.responseBodyReader = responseBodyReader;
  }

  /**
   * Sets a callback handler to be invoked for the specified header name.
   * @param name the header name
   * @param handler the handler to receive the value for the header
   */
  public void setResponseHeaderHandler(String name,
                                       ResponseHeaderHandler handler)
  {
    responseHeaderHandlers.put(name, handler);
  }

  /**
   * Sets an authenticator that can be used to handle authentication
   * automatically.
   * @param authenticator the authenticator
   */
  public void setAuthenticator(Authenticator authenticator)
  {
    this.authenticator = authenticator;
  }

  /**
   * Sets the request body negotiation threshold.
   * If this is set, it determines the maximum size that the request body
   * may be before body negotiation occurs(via the
   * <code>100-continue</code> expectation). This ensures that a large
   * request body is not sent when the server wouldn't have accepted it
   * anyway.
   * @param threshold the body negotiation threshold, or &lt;=0 to disable
   * request body negotation entirely
   */
  public void setRequestBodyNegotiationThreshold(long threshold)
  {
    requestBodyNegotiationThreshold = threshold;
  }

  /**
   * Dispatches this request.
   * A request can only be dispatched once; calling this method a second
   * time results in a protocol exception.
   * @exception IOException if an I/O error occurred
   * @return an HTTP response object representing the result of the operation
   */
  public Response dispatch()
    throws IOException
  {
    if (dispatched)
      {
        String message =
          HTTPConnection.L10N.getString("err.request_already_dispatched");
        throw new ProtocolException(message);
      }
    final String CRLF = "\r\n";
    final String HEADER_SEP = ": ";
    final String US_ASCII = "US-ASCII";
    final String version = connection.getVersion();
    Response response;
    long contentLength = -1;
    boolean retry = false;
    int attempts = 0;
    boolean expectingContinue = false;
    if (requestBodyWriter != null)
      {
        contentLength = requestBodyWriter.getContentLength();
        if (requestBodyNegotiationThreshold > 0L &&
                contentLength > requestBodyNegotiationThreshold &&
                (connection.minorVersion > 0 || connection.majorVersion > 1))
          {
            expectingContinue = true;
            setHeader("Expect", "100-continue");
          }
        else
          {
            setHeader("Content-Length", Long.toString(contentLength));
          }
      }

    try
      {
        // Loop while authentication fails or continue
        do
          {
            retry = false;
            // Send request
            connection.fireRequestEvent(RequestEvent.REQUEST_SENDING, this);

            // Get socket output and input streams
            OutputStream out = connection.getOutputStream();
            LineInputStream in =
              new LineInputStream(connection.getInputStream());
            // Request line
            String requestUri = path;
            if (connection.isUsingProxy() &&
                !"*".equals(requestUri) &&
                !"CONNECT".equals(method))
              {
                requestUri = getRequestURI();
              }
            String line = method + ' ' + requestUri + ' ' + version + CRLF;
            out.write(line.getBytes(US_ASCII));
            // Request headers
            for (Iterator i = requestHeaders.keySet().iterator();
                 i.hasNext(); )
              {
                String name =(String) i.next();
                String value =(String) requestHeaders.get(name);
                line = name + HEADER_SEP + value + CRLF;
                out.write(line.getBytes(US_ASCII));
              }
            out.write(CRLF.getBytes(US_ASCII));
            // Request body
            if (requestBodyWriter != null && !expectingContinue)
              {
                byte[] buffer = new byte[4096];
                int len;
                long count = 0L;

                requestBodyWriter.reset();
                do
                  {
                    len = requestBodyWriter.write(buffer);
                    if (len > 0)
                      {
                        out.write(buffer, 0, len);
                        count += (long) len;
                      }
                  }
                while (len > -1 && count < contentLength);
                out.write(CRLF.getBytes(US_ASCII));
              }
            out.flush();
            // Sent event
            connection.fireRequestEvent(RequestEvent.REQUEST_SENT, this);
            // Get response
            response = readResponse(in);
            int sc = response.getCode();
            if (sc == 401 && authenticator != null)
              {
                if (authenticate(response, attempts++))
                  {
                    retry = true;
                  }
              }
            else if (sc == 100)
              {
                requestHeaders.remove("Expect");
                setHeader("Content-Length", Long.toString(contentLength));
                expectingContinue = false;
                retry = true;
              }
          }
        while (retry);
      }
    catch (IOException e)
      {
        connection.close();
        throw e;
      }
    return response;
  }

  Response readResponse(LineInputStream in)
    throws IOException
  {
    String line;
    int len;

    // Read response status line
    line = in.readLine();
    if (line == null)
      {
        throw new EOFException();
      }
    if (!line.startsWith("HTTP/"))
      {
        throw new ProtocolException(line);
      }
    len = line.length();
    int start = 5, end = 6;
    while (line.charAt(end) != '.')
      {
        end++;
      }
    int majorVersion = Integer.parseInt(line.substring(start, end));
    start = end + 1;
    end = start + 1;
    while (line.charAt(end) != ' ')
      {
        end++;
      }
    int minorVersion = Integer.parseInt(line.substring(start, end));
    start = end + 1;
    end = start + 3;
    int code = Integer.parseInt(line.substring(start, end));
    String message = line.substring(end + 1, len - 1);
    // Read response headers
    Headers responseHeaders = new Headers();
    responseHeaders.parse(in);
    // Check for upgrade
    if (connection.http20 == HTTPConnection.HTTP20_UNKNOWN)
      {
        connection.http20 = HTTPConnection.HTTP20_NO;
        if (code == 101)
          {
            if ("Upgrade".equals(responseHeaders.getValue("Connection")) &&
                "HTTP/2.0".equals(responseHeaders.getValue("Upgrade")))
              {
                connection.http20 = HTTPConnection.HTTP20_OK;
                // TODO parse new response
              }
          }
      }
    notifyHeaderHandlers(responseHeaders);
    // Construct response
    int codeClass = code / 100;
    Response ret = new Response(majorVersion, minorVersion, code,
                                codeClass, message, responseHeaders);
    if (!"HEAD".equals(method) && !"OPTIONS".equals(method))
      {
        switch (code)
          {
          case 204:
          case 205:
            break;
          default:
            // Does response body reader want body?
            boolean notify = (responseBodyReader != null);
            if (notify)
              {
                if (!responseBodyReader.accept(this, ret))
                  {
                    notify = false;
                  }
              }
            readResponseBody(ret, in, notify);
          }
      }
    return ret;
  }

  void notifyHeaderHandlers(Headers headers)
  {
    for (Iterator i = headers.entrySet().iterator(); i.hasNext(); )
      {
        Map.Entry entry = (Map.Entry) i.next();
        String name = (String) entry.getKey();
        // Handle Set-Cookie
        if ("Set-Cookie".equalsIgnoreCase(name))
          {
            String value = (String) entry.getValue();
            handleSetCookie(value);
          }
        ResponseHeaderHandler handler =
          (ResponseHeaderHandler) responseHeaderHandlers.get(name);
        if (handler != null)
          {
            String value = (String) entry.getValue();
            handler.setValue(value);
          }
      }
  }

  void readResponseBody(Response response, InputStream in,
                        boolean notify)
    throws IOException
  {
    byte[] buffer = new byte[4096];
    int contentLength = -1;
    Headers trailer = null;

    String transferCoding = response.getHeader("Transfer-Encoding");
    if ("chunked".equalsIgnoreCase(transferCoding))
      {
        trailer = new Headers();
        in = new ChunkedInputStream(in, trailer);
      }
    else
      {
        contentLength = response.getIntHeader("Content-Length");
      }
    String contentCoding = response.getHeader("Content-Encoding");
    if (contentCoding != null && !"identity".equals(contentCoding))
      {
        if ("gzip".equals(contentCoding))
          {
            in = new GZIPInputStream(in);
          }
        else if ("deflate".equals(contentCoding))
          {
            in = new InflaterInputStream(in);
          }
        else
          {
            String message =
              HTTPConnection.L10N.getString("err.unsupported_content_encoding");
            message = MessageFormat.format(message, contentCoding);
            throw new ProtocolException(message);
          }
        response.headers.remove("Content-Encoding");
      }

    // Persistent connections are the default in HTTP/1.1
    boolean doClose = "close".equalsIgnoreCase(getHeader("Connection")) ||
      "close".equalsIgnoreCase(response.getHeader("Connection")) ||
      (connection.majorVersion == 1 && connection.minorVersion == 0) ||
      (response.majorVersion == 1 && response.minorVersion == 0) ||
      contentLength == -1;

    if (contentLength == 0)
      {
        if (doClose)
          {
            connection.closeConnection();
          }
      }
    else
      {
        int count = contentLength;
        int len = (count > -1) ? count : buffer.length;
        len = (len > buffer.length) ? buffer.length : len;
        while (len > -1)
          {
            len = in.read(buffer, 0, len);
            if (len < 0)
              {
                // EOF
                connection.closeConnection();
                break;
              }
            if (notify)
              {
                responseBodyReader.read(buffer, 0, len);
              }
            if (count > -1)
              {
                count -= len;
                if (count < 1)
                  {
                    if (doClose)
                      {
                        connection.closeConnection();
                      }
                    break;
                  }
              }
          }
      }
    if (notify)
      {
        responseBodyReader.close();
      }
    if (trailer != null)
      {
        response.getHeaders().putAll(trailer);
        notifyHeaderHandlers(trailer);
      }
  }

  boolean authenticate(Response response, int attempts)
    throws IOException
  {
    String challenge = response.getHeader("WWW-Authenticate");
    if (challenge == null)
      {
        challenge = response.getHeader("Proxy-Authenticate");
      }
    int si = challenge.indexOf(' ');
    String scheme = challenge;
    Properties authParams = null;
    if (si > 0) {
        scheme = challenge.substring(0, si);
        authParams = parseAuthParams(challenge.substring(si + 1));
    }
    return authenticate(scheme, authParams, attempts);
  }

  public void preauthenticate(String scheme)
    throws IOException
  {
    if (!authenticate(scheme, null, 0))
      {
        String message =
          HTTPConnection.L10N.getString("err.bad_preauth_scheme");
        message = MessageFormat.format(message, scheme);
        throw new IOException(message);
      }
  }

  boolean authenticate(String scheme, Properties authParams, int attempts)
    throws IOException
  {
    if ("Basic".equalsIgnoreCase(scheme))
      {
        String realm = authParams.getProperty("realm");
        Credentials creds = authenticator.getCredentials(realm, attempts);
        String userPass = creds.getUsername() + ':' + creds.getPassword();
        byte[] b_userPass = userPass.getBytes("US-ASCII");
        byte[] b_encoded = BASE64.encode(b_userPass);
        String authorization =
          scheme + " " + new String(b_encoded, "US-ASCII");
        setHeader("Authorization", authorization);
        return true;
      }
    else if ("Digest".equalsIgnoreCase(scheme))
      {
        String realm = authParams.getProperty("realm");
        String nonceParam = authParams.getProperty("nonce");
        String qop = authParams.getProperty("qop");
        String algorithm = authParams.getProperty("algorithm");
        String digestUri = getRequestURI();
        Credentials creds = authenticator.getCredentials(realm, attempts);
        String username = creds.getUsername();
        String password = creds.getPassword();
        connection.incrementNonce(nonceParam);
        try
          {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            final byte[] COLON = { 0x3a };

            // Calculate H(A1)
            md5.reset();
            md5.update(username.getBytes("US-ASCII"));
            md5.update(COLON);
            md5.update(realm.getBytes("US-ASCII"));
            md5.update(COLON);
            md5.update(password.getBytes("US-ASCII"));
            byte[] ha1 = md5.digest();
            if ("md5-sess".equals(algorithm))
              {
                byte[] cnonce = generateNonce();
                md5.reset();
                md5.update(ha1);
                md5.update(COLON);
                md5.update(nonceParam.getBytes("US-ASCII"));
                md5.update(COLON);
                md5.update(cnonce);
                ha1 = md5.digest();
              }
            String ha1Hex = toHexString(ha1);

            // Calculate H(A2)
            md5.reset();
            md5.update(method.getBytes("US-ASCII"));
            md5.update(COLON);
            md5.update(digestUri.getBytes("US-ASCII"));
            if ("auth-int".equals(qop))
              {
                byte[] hEntity = null; // TODO hash of entity body
                md5.update(COLON);
                md5.update(hEntity);
              }
            byte[] ha2 = md5.digest();
            String ha2Hex = toHexString(ha2);

            // Calculate response
            md5.reset();
            md5.update(ha1Hex.getBytes("US-ASCII"));
            md5.update(COLON);
            md5.update(nonceParam.getBytes("US-ASCII"));
            if ("auth".equals(qop) || "auth-int".equals(qop))
              {
                String nc = getNonceCount(nonceParam);
                byte[] cnonce = generateNonce();
                md5.update(COLON);
                md5.update(nc.getBytes("US-ASCII"));
                md5.update(COLON);
                md5.update(cnonce);
                md5.update(COLON);
                md5.update(qop.getBytes("US-ASCII"));
              }
            md5.update(COLON);
            md5.update(ha2Hex.getBytes("US-ASCII"));
            String digestResponse = toHexString(md5.digest());

            String authorization = scheme +
              " username=\"" + username + "\"" +
              " realm=\"" + realm + "\"" +
              " nonce=\"" + nonceParam + "\"" +
              " uri=\"" + digestUri + "\"" +
              " response=\"" + digestResponse + "\"";
            setHeader("Authorization", authorization);
            return true;
          }
        catch (NoSuchAlgorithmException e)
          {
            return false;
          }
      }
    else if ("AWS".equalsIgnoreCase(scheme))
      {
        Credentials creds = authenticator.getCredentials(null, attempts);
        String awsAccessKeyId = creds.getUsername();
        String yourSecretAccessKeyID = creds.getPassword();
        StringBuilder stringToSign = new StringBuilder(method);
        stringToSign.append('\n');
        String contentMD5 = requestHeaders.getValue("Content-MD5");
        if (contentMD5 != null)
          {
            stringToSign.append(contentMD5);
          }
        stringToSign.append('\n');
        String contentType = requestHeaders.getValue("Content-Type");
        if (contentType != null)
          {
            stringToSign.append(contentType);
          }
        stringToSign.append('\n');
        String date = requestHeaders.getValue("Date");
        if (date != null)
          {
            stringToSign.append(date);
          }
        stringToSign.append('\n');
        stringToSign.append(getCanonicalizedAmzHeaders());
        stringToSign.append(getCanonicalizedResource());
        byte[] b_secret = yourSecretAccessKeyID.getBytes("UTF-8");
        byte[] b_string = stringToSign.toString().getBytes("UTF-8");
        try
          {
            byte[] b_encoded = BASE64.encode(hmac_sha1(b_secret, b_string));
            String signature = new String(b_encoded, "US-ASCII");
            String authorization =
                scheme + " " + awsAccessKeyId + ":" + signature;
            setHeader("Authorization", authorization);
            return true;
          }
        catch (NoSuchAlgorithmException e) {
          {
            return false;
          }
        }
      }
    // Scheme not recognised
    return false;
  }

  Properties parseAuthParams(String text)
  {
    int len = text.length();
    String key = null;
    StringBuffer buf = new StringBuffer();
    Properties ret = new Properties();
    boolean inQuote = false;
    for (int i = 0; i < len; i++)
      {
        char c = text.charAt(i);
        if (c == '"')
          {
            inQuote = !inQuote;
          }
        else if (c == '=' && key == null)
          {
            key = buf.toString().trim();
            buf.setLength(0);
          }
        else if (c == ' ' && !inQuote)
          {
            String value = unquote(buf.toString().trim());
            ret.put(key, value);
            key = null;
            buf.setLength(0);
          }
        else if (c != ',' || (i <(len - 1) && text.charAt(i + 1) != ' '))
          {
            buf.append(c);
          }
      }
    if (key != null)
      {
        String value = unquote(buf.toString().trim());
        ret.put(key, value);
      }
    return ret;
  }

  String unquote(String text)
  {
    int len = text.length();
    if (len > 0 && text.charAt(0) == '"' && text.charAt(len - 1) == '"')
      {
        return text.substring(1, len - 1);
      }
    return text;
  }

  String getCanonicalizedAmzHeaders()
  {
    List headerNames = new ArrayList(requestHeaders.size());
    for (Iterator i = requestHeaders.keySet().iterator(); i.hasNext(); )
      {
        String key = i.next().toString().toLowerCase();
        if (key.startsWith("x-amz-"))
          {
            headerNames.add(key);
          }
     }
    Collections.sort(headerNames);
    StringBuilder buf = new StringBuilder();
    for (Iterator i = headerNames.iterator(); i.hasNext(); )
      {
        String key = (String) i.next();
        String value = requestHeaders.getValue(key);
        buf.append(key);
        buf.append(':');
        buf.append(value);
        buf.append('\n');
      }
    return buf.toString();
  }

  String getCanonicalizedResource()
  {
    StringBuilder buf = new StringBuilder();
    String host = connection.getHostName();
    if (host.endsWith(".s3.amazonaws.com"))
      {
        String bucket = host.substring(0, host.length() - ".s3.amazonaws.com".length());
        buf.append('/');
        buf.append(bucket);
      }
    buf.append(path);
    return buf.toString();
  }

  byte[] hmac_sha1(byte[] key, byte[] value)
      throws NoSuchAlgorithmException
  {
    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    int B = 64;
    byte[] k1 = new byte[B];
    System.arraycopy(key, 0, k1, 0, key.length);
    byte[] k2 = new byte[B];
    System.arraycopy(key, 0, k2, 0, key.length);
    for (int i = 0; i < B; i++)
      {
        int k = (i >= key.length) ? 0 : (int) key[i];
        k1[i] = (byte) (k ^ 0x36);
        k2[i] = (byte) (k ^ 0x5c);
      }
    sha1.update(k1);
    sha1.update(value);
    byte[] h2 = sha1.digest();
    sha1.reset();
    sha1.update(k2);
    sha1.update(h2);
    return sha1.digest();
  }

  /**
   * Returns the number of times the specified nonce value has been seen.
   * This always returns an 8-byte 0-padded hexadecimal string.
   */
  String getNonceCount(String nonceParam)
  {
    int nc = connection.getNonceCount(nonceParam);
    String hex = Integer.toHexString(nc);
    StringBuffer buf = new StringBuffer();
    for (int i = 8 - hex.length(); i > 0; i--)
      {
        buf.append('0');
      }
    buf.append(hex);
    return buf.toString();
  }

  /**
   * Client nonce value.
   */
  byte[] nonce;

  /**
   * Generates a new client nonce value.
   */
  byte[] generateNonce()
    throws IOException, NoSuchAlgorithmException
  {
    if (nonce == null)
      {
        long time = System.currentTimeMillis();
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(Long.toString(time).getBytes("US-ASCII"));
        nonce = md5.digest();
      }
    return nonce;
  }

  String toHexString(byte[] bytes)
  {
    char[] ret = new char[bytes.length * 2];
    for (int i = 0, j = 0; i < bytes.length; i++)
      {
        int c =(int) bytes[i];
        if (c < 0)
          {
            c += 0x100;
          }
        ret[j++] = Character.forDigit(c / 0x10, 0x10);
        ret[j++] = Character.forDigit(c % 0x10, 0x10);
      }
    return new String(ret);
  }

  /**
   * Parse the specified cookie list and notify the cookie manager.
   */
  void handleSetCookie(String text)
  {
    CookieManager cookieManager = connection.getCookieManager();
    if (cookieManager == null)
      {
        return;
      }
    String name = null;
    String value = null;
    String comment = null;
    String domain = connection.getHostName();
    String p = path;
    int lsi = p.lastIndexOf('/');
    if (lsi != -1)
      {
        p = p.substring(0, lsi);
      }
    boolean secure = false;
    Date expires = null;

    int len = text.length();
    String attr = null;
    StringBuffer buf = new StringBuffer();
    boolean inQuote = false;
    for (int i = 0; i <= len; i++)
      {
        char c =(i == len) ? '\u0000' : text.charAt(i);
        if (c == '"')
          {
            inQuote = !inQuote;
          }
        else if (!inQuote)
          {
            if (c == '=' && attr == null)
              {
                attr = buf.toString().trim();
                buf.setLength(0);
              }
            else if (c == ';' || i == len || c == ',')
              {
                String val = unquote(buf.toString().trim());
                if (name == null)
                  {
                    name = attr;
                    value = val;
                  }
                else if ("Comment".equalsIgnoreCase(attr))
                  {
                    comment = val;
                  }
                else if ("Domain".equalsIgnoreCase(attr))
                  {
                    domain = val;
                  }
                else if ("Path".equalsIgnoreCase(attr))
                  {
                    p = val;
                  }
                else if ("Secure".equalsIgnoreCase(val))
                  {
                    secure = true;
                  }
                else if ("Max-Age".equalsIgnoreCase(attr))
                  {
                    int delta = Integer.parseInt(val);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());
                    cal.add(Calendar.SECOND, delta);
                    expires = cal.getTime();
                  }
                else if ("Expires".equalsIgnoreCase(attr))
                  {
                    DateFormat dateFormat = new HTTPDateFormat();
                    try
                      {
                        expires = dateFormat.parse(val);
                      }
                    catch (ParseException e)
                      {
                        // if this isn't a valid date, it may be that
                        // the value was returned unquoted; in that case, we
                        // want to continue buffering the value
                        buf.append(c);
                        continue;
                      }
                  }
                attr = null;
                buf.setLength(0);
                // case EOL
                if (i == len || c == ',')
                  {
                    Cookie cookie = new Cookie(name, value, comment, domain,
                                               p, secure, expires);
                    cookieManager.setCookie(cookie);
                  }
                if (c == ',')
                  {
                    // Reset cookie fields
                    name = null;
                    value = null;
                    comment = null;
                    domain = connection.getHostName();
                    p = path;
                    if (lsi != -1)
                      {
                        p = p.substring(0, lsi);
                      }
                    secure = false;
                    expires = null;
                  }
              }
            else
              {
                buf.append(c);
              }
          }
        else
          {
            buf.append(c);
          }
      }
  }

}

