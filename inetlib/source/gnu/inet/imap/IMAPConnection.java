/*
 * IMAPConnection.java
 * Copyright (C) 2003,2004,2005,2013 The Free Software Foundation
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

package gnu.inet.imap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import javax.security.auth.callback.CallbackHandler;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

import gnu.inet.util.BASE64;
import gnu.inet.util.CRLFOutputStream;
import gnu.inet.util.EmptyX509TrustManager;
import gnu.inet.util.SaslCallbackHandler;
import gnu.inet.util.SaslCramMD5;
import gnu.inet.util.SaslInputStream;
import gnu.inet.util.SaslLogin;
import gnu.inet.util.SaslOutputStream;
import gnu.inet.util.SaslPlain;
import gnu.inet.util.TraceLevel;

/**
 * The protocol class implementing IMAP4rev1.
 * <p>
 * To use this connection, you must implement an
 * {@link IMAPCallback} either directly or by subclassing the
 * {@link IMAPAdapter} class. This callback will be notified of the various
 * IMAP events that occur during the processing of the command, since a
 * single command may result in multiple events possibly unrelated to the
 * intent of the caller (alerts, new message updates, etc).
 * @version 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class IMAPConnection
  implements IMAPConstants
{

  static final ResourceBundle L10N =
    ResourceBundle.getBundle("gnu.inet.imap.L10N");

  static final Charset US_ASCII = Charset.forName("US-ASCII");

  /**
   * The network trace level.
   */
  public static final Level IMAP_TRACE = new TraceLevel("imap");

  /**
   * Prefix for tags.
   */
  static final String TAG_PREFIX = "A";

  /**
   * The default IMAP port.
   */
  public static final int DEFAULT_PORT = 143;

  /**
   * The default IMAP-SSL port.
   */
  public static final int DEFAULT_SSL_PORT = 993;

  /**
   * The logger used for IMAP protocol traces.
   */
  private final Logger logger = Logger.getLogger("gnu.inet.imap");

  private String host;
  private int port;
  private int connectionTimeout;
  private int timeout;
  private boolean secure;
  private TrustManager tm;
  private String[] sslTlsProtocolsIfAny;

  /**
   * The socket used for communication with the server.
   */
  private Socket socket;

  /**
   * The tokenizer used to read IMAP stream tokens from.
   */
  private Tokenizer in;
  private LiteralFactory literalFactory;
  private int literalThreshold = 4096;

  /**
   * The output stream.
   */
  private CRLFOutputStream out;

  /*
   * Used to generate new tags for tagged commands.
   */
  private int tagIndex = 0;

  private List<String> capabilities = new ArrayList<String>();

  private static final SimpleDateFormat DATETIME_FORMAT =
    new SimpleDateFormat("'\"'dd-MMM-yyyy hh:mm:ss Z'\"'");

  /**
   * Creates a new connection to the default IMAP port.
   * @param host the name of the host to connect to
   */
  public IMAPConnection(String host)
  {
    this(host, -1, 0, 0, false, null);
  }

  /**
   * Creates a new connection.
   * @param host the name of the host to connect to
   * @param port the port to connect to, or -1 for the default
   */
  public IMAPConnection(String host, int port)
  {
    this(host, port, 0, 0, false, null);
  }

  /**
   * Creates a new connection.
   * @param host the name of the host to connect to
   * @param port the port to connect to, or -1 for the default
   * @param connectionTimeout the socket connection timeout
   * @param timeout the socket timeout
   */
  public IMAPConnection(String host, int port,
                        int connectionTimeout, int timeout)
  {
    this(host, port, connectionTimeout, timeout, false, null);
  }

  /**
   * Creates a new secure connection using the specified trust manager.
   * @param host the name of the host to connect to
   * @param port the port to connect to, or -1 for the default
   * @param tm a trust manager used to check SSL certificates, or null to
   * use the default
   */
  public IMAPConnection(String host, int port, TrustManager tm)
  {
    this(host, port, 0, 0, true, tm);
  }

  /**
   * Creates a new connection.
   * @param host the name of the host to connect to
   * @param port the port to connect to, or -1 for the default
   * @param connectionTimeout the socket connection timeout
   * @param timeout the socket timeout
   * @param secure if an IMAP-SSL connection should be made
   * @param tm a trust manager used to check SSL certificates, or null to
   * use the default
   */
  public IMAPConnection(String host, int port,
                        int connectionTimeout, int timeout,
                        boolean secure, TrustManager tm)
  {
    if (port < 0)
      {
        port = secure ? DEFAULT_SSL_PORT : DEFAULT_PORT;
      }
    this.host = host;
    this.port = port;
    this.connectionTimeout = connectionTimeout;
    this.timeout = timeout;
    this.secure = secure;
    this.tm = tm;
    this.sslTlsProtocolsIfAny = null;
  }

  /**
   * Connects this connection.
   */
  private void connect()
    throws IOException
  {
    // Set up socket
    try
      {
        socket = new Socket();
        InetSocketAddress address = new InetSocketAddress(host, port);
        if (connectionTimeout > 0)
          {
            socket.connect(address, connectionTimeout);
          }
        else
          {
            socket.connect(address);
          }
        if (timeout > 0)
          {
            socket.setSoTimeout(timeout);
          }
        if (secure)
          {
            SSLSocketFactory factory = getSSLSocketFactory(tm);
            SSLSocket ss =
              (SSLSocket) factory.createSocket(socket, host, port, true);

            if (sslTlsProtocolsIfAny != null)
              ss.setEnabledProtocols(sslTlsProtocolsIfAny);
            ss.setUseClientMode(true);
            ss.startHandshake();
            socket = ss;
          }
      }
    catch (GeneralSecurityException e)
      {
        IOException e2 = new IOException();
        e2.initCause(e);
        throw e2;
      }
    InputStream is = socket.getInputStream();
    is = new BufferedInputStream(is);
    in = new Tokenizer(is, this, literalFactory, literalThreshold);
    OutputStream os = socket.getOutputStream();
    os = new BufferedOutputStream(os);
    out = new CRLFOutputStream(os);
  }

  public void setSslTlsProtocolsIfAny(String[] sslTlsProtocolsIfAny) {
    if (sslTlsProtocolsIfAny == null || sslTlsProtocolsIfAny.length < 1) {
        this.sslTlsProtocolsIfAny = null;
        return;
    }

    this.sslTlsProtocolsIfAny = new String[sslTlsProtocolsIfAny.length];
    for (int i = 0; i < sslTlsProtocolsIfAny.length; i++)
        this.sslTlsProtocolsIfAny[i] = sslTlsProtocolsIfAny[i];
  }

  /**
   * Sets a literal factory for the connection.
   * This must be called before {@link #connect}
   * @see {@link LiteralFactory}
   * @param factory the factory
   * @param threshold the threshold over which literals are considered large
   */
  public void setLiteralFactory(LiteralFactory factory, int threshold)
  {
    literalFactory = factory;
    literalThreshold = threshold;
  }

  /**
   * Returns the logger used by this connection for debug output.
   */
  public Logger getLogger()
  {
    return logger;
  }

  /**
   * Returns a new tag for a command.
   */
  protected String newTag()
  {
    return TAG_PREFIX + (++tagIndex);
  }

  boolean isDebug()
  {
    return logger.getLevel() == IMAP_TRACE;
  }

  void debug(String message)
  {
    logger.log(IMAP_TRACE, message);
    Handler[] handlers = logger.getHandlers();
    for (Handler h : handlers)
      {
        h.flush();
      }
  }

  /**
   * Sends the specified IMAP tagged command to the server.
   */
  private void sendCommand(String tag, String command)
    throws IOException
  {
    if (socket == null)
      {
        connect();
      }
    if (isDebug())
      {
        debug(new StringBuilder("> ")
              .append(tag).append(' ').append(command).toString());
      }
    out.write(tag);
    out.write(' ');
    out.write(command);
    out.writeln();
    out.flush();
  }

  /**
   * Sends the specified IMAP command.
   * @param command the command
   * @return true if OK was received, or false if NO was received
   * @exception IOException if BAD was received or an I/O error occurred
   */
  private boolean invokeSimpleCommand(String command, IMAPCallback callback)
    throws IOException
  {
    String tag = newTag();
    sendCommand(tag, command);
    return handleSimpleResponse(tag, callback);
  }

  private boolean handleSimpleResponse(String tag, IMAPCallback callback)
    throws IOException
  {
    while (true)
      {
        Token token = in.next();
        switch (token.type)
          {
          case Token.TAG:
            boolean match = tag.equals(token.stringValue());
            boolean result = parseRespCondState(callback);
            in.collectToEOL();
            in.reset();
            if (match)
              {
                return result;
              }
            break;
          case Token.UNTAGGED_RESPONSE:
            token = in.next();
            switch (token.type)
              {
              case Token.NUMBER:
                int number = token.intValue();
                parseUntaggedNumber(number, callback);
                break;
              case Token.ATOM:
                parseUntaggedAtom(token, callback);
                break;
              }
            in.collectToEOL();
            in.reset();
            break;
          default:
            throw createException("err.unexpected_token", token);
          }
      }
  }

  private IOException createException(String key, Object... args)
  {
    in.reset();
    String message = L10N.getString(key);
    if (args != null)
      {
        message = MessageFormat.format(message, args);
      }
    return new IOException(message);
  }

  // -- start parsing --

  private String toString(Token token)
    throws IOException
  {
    switch (token.type)
      {
      case Token.NIL:
        return null;
      case Token.ATOM:
      case Token.QUOTED_STRING:
        return token.stringValue();
      default:
        throw createException("err.expected_string", token);
      }
  }

  private String toLowerCaseString(Token token)
    throws IOException
  {
    String ret = toString(token);
    return ret == null ? null : ret.toLowerCase();
  }

  private String toAString(Token token)
    throws IOException
  {
    String ret = toString(token);
    return (ret == null) ? "NIL" : ret;
  }

  private Token requireToken(int type, String error)
    throws IOException
  {
    Token token = in.next();
    if ((token.type & type) == 0)
      {
        throw createException(error, token);
      }
    return token;
  }

  private boolean parseRespCondState(IMAPCallback callback)
    throws IOException
  {
    boolean ret = false;
    Token token;
    token = requireToken(Token.ATOM, "err.expected_atom");
    String status = token.stringValue();
    String text = parseRespText(callback);
    if (OK.equals(status))
      {
        return true;
      }
    else if (NO.equals(status))
      {
        return false;
      }
    throw new IMAPException(status, text);
  }

  private String parseRespText(IMAPCallback callback)
    throws IOException
  {
    String code = null;
    Map params = null;
    Token token = in.peek();
    if (token.type == Token.LBRACKET)
      {
        in.next();
        token = requireToken(Token.ATOM, "err.expected_atom");
        code = token.stringValue();
        params = new HashMap();
        if (PERMANENTFLAGS.equals(code) ||
            BADCHARSET.equals(code))
          {
            List<String> data = parseFlags(BADCHARSET.equals(code));
            params.put(code, data);
          }
        else if (CAPABILITY.equals(code))
          {
            List<String> data = new ArrayList<String>();
            this.capabilities.clear();
            while (token.type != Token.RBRACKET)
              {
                token = requireToken(Token.ATOM |
                                     Token.RBRACKET,
                                     "err.expected_capability");
                if (token.type == Token.ATOM)
                  {
                    String cap = token.stringValue();
                    data.add(cap);
                    this.capabilities.add(cap);
                  }
              }
            params.put(code, data);
          }
        else if (UIDNEXT.equals(code) ||
                 UIDVALIDITY.equals(code) ||
                 UNSEEN.equals(code))
          {
            token = requireToken(Token.NUMBER, "err.expected_number");
            params.put(code, token.stringValue());
          }
        else if (APPENDUID.equals(code) ||
                 COPYUID.equals(code))
          {
            List<String> args = new ArrayList<String>();
            token = requireToken(Token.NUMBER, "err.expected_number");
            while (token.type != Token.RBRACKET)
              {
                args.add(token.stringValue());
                token = in.next();
              }
            params.put(code, args);
          }
        while (token.type != Token.RBRACKET)
          {
            token = in.next();
          }
      }
    String text = in.collectToEOL();
    if (ALERT.equals(code))
      {
        callback.alert(text);
      }
    else if (CAPABILITY.equals(code))
      {
        callback.capability((List<String>) params.get(code));
      }
    else if (PERMANENTFLAGS.equals(code))
      {
        callback.permanentflags((List<String>) params.get(code));
      }
    else if (UIDVALIDITY.equals(code))
      {
        callback.uidvalidity(Long.parseLong((String) params.get(code)));
      }
    else if (UIDNEXT.equals(code))
      {
        callback.uidnext(Long.parseLong((String) params.get(code)));
      }
    else if (UNSEEN.equals(code))
      {
        callback.firstUnseen(Integer.parseInt((String) params.get(code)));
      }
    else if (READ_ONLY.equals(code))
      {
        callback.readOnly();
      }
    else if (READ_WRITE.equals(code))
      {
        callback.readWrite();
      }
    else if (TRYCREATE.equals(code))
      {
        callback.tryCreate();
      }
    else if (APPENDUID.equals(code))
      {
        List<String> args = (List<String>) params.get(code);
        long uidvalidity = Long.parseLong(args.get(0));
        long uid = Long.parseLong(args.get(1));
        callback.appenduid(uidvalidity, uid);
      }
    else if (COPYUID.equals(code))
      {
        List<String> args = (List<String>) params.get(code);
        long uidvalidity = Long.parseLong(args.get(0));
        UIDSet source = new UIDSet(args.get(1));
        UIDSet destination = new UIDSet(args.get(2));
        callback.copyuid(uidvalidity, source, destination);
      }
    else if (UIDNOTSTICKY.equals(code))
      {
        callback.uidnotsticky();
      }
    return text;
  }

  /*
   * resp-cond-state | resp-cond-bye | mailbox-data | capability-data
   * token is an atom
   */
  void parseUntaggedAtom(Token token, IMAPCallback callback)
    throws IOException
  {
    String status = token.stringValue();
    if (OK.equals(token.stringValue()) ||
        NO.equals(token.stringValue()) ||
        BYE.equals(token.stringValue()))
      {
        parseRespText(callback);
      }
    else if (BAD.equals(token.stringValue()))
      {
        String text = parseRespText(callback);
        throw new IMAPException(token.stringValue(), text);
      }
    else if (FLAGS.equals(token.stringValue()))
      {
        callback.flags(parseFlags(false));
      }
    else if (LIST.equals(token.stringValue()) ||
             LSUB.equals(token.stringValue()))
      {
        List<String> flags = parseFlags(false);
        token = requireToken(Token.QUOTED_STRING |
                             Token.NIL, "err.expected_list_delim");
        String delim = toString(token);
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String mailbox = UTF7imap.decode(toAString(token));
        callback.list(flags, delim, mailbox);
      }
    else if (SEARCH.equals(token.stringValue()))
      {
        callback.search(parseNumberList());
      }
    else if (STATUS.equals(token.stringValue()))
      {
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String mailbox = UTF7imap.decode(toAString(token));
        token = requireToken(Token.LPAREN, "err.expected_lparen");
        Map<String,Integer> data = new LinkedHashMap<String,Integer>();
        do
          {
            token = requireToken(Token.ATOM | Token.RPAREN,
                                 "err.expected_status_item");
            if (token.type == Token.ATOM)
              {
                String item = toString(token);
                token = requireToken(Token.NUMBER,
                                     "err.expected_number");
                if (MESSAGES.equals(item))
                  {
                    callback.exists(token.intValue());
                  }
                else if (RECENT.equals(item))
                  {
                    callback.recent(token.intValue());
                  }
                else if (UIDNEXT.equals(item))
                  {
                    callback.uidnext(token.longValue());
                  }
                else if (UIDVALIDITY.equals(item))
                  {
                    callback.uidvalidity(token.longValue());
                  }
                else if (UNSEEN.equals(item))
                  {
                    callback.unseen(token.intValue());
                  }
              }
          }
        while (token.type != Token.RPAREN);
      }
    else if (CAPABILITY.equals(token.stringValue()))
      {
        List<String> data = new ArrayList<String>();
        this.capabilities.clear();
        do
          {
            token = requireToken(Token.ATOM | Token.EOL,
                                 "err.expected_capability");
            if (token.type == Token.ATOM)
              {
                String cap = token.stringValue();
                data.add(cap);
                this.capabilities.add(cap);
              }
          }
        while (token.type != Token.EOL);
        callback.capability(data);
      }
    else if (NAMESPACE.equals(token.stringValue()))
      {
        List<Namespace> personal = parseNamespaces();
        List<Namespace> otherUsers = parseNamespaces();
        List<Namespace> shared = parseNamespaces();
        callback.namespace(personal, otherUsers, shared);
      }
    else if (ACL.equals(token.stringValue()))
      {
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String mailbox = UTF7imap.decode(toAString(token));
        Map<String,String> rights = parseACL();
        callback.acl(mailbox, rights);
      }
    else if (LISTRIGHTS.equals(token.stringValue()))
      {
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String mailbox = UTF7imap.decode(toAString(token));
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String identifier = UTF7imap.decode(toAString(token));
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String required = toAString(token);
        List<String> optional = new ArrayList<String>();
        while (token.type != Token.EOL)
          {
            if ((token.type & Token.ASTRING) == 0)
              {
                throw new IOException("err.expected_astring");
              }
            optional.add(toAString(token));
            token = in.next();
          }
        callback.listrights(mailbox, identifier, required, optional);
      }
    else if (MYRIGHTS.equals(token.stringValue()))
      {
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String mailbox = UTF7imap.decode(toAString(token));
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String rights = toAString(token);
        callback.myrights(mailbox, rights);
      }
    else if (QUOTA.equals(token.stringValue()))
      {
        Map<String,Integer> currentUsage = new LinkedHashMap<String,Integer>();
        Map<String,Integer> limit = new LinkedHashMap<String,Integer>();
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String quotaRoot = UTF7imap.decode(toAString(token));
        token = requireToken(Token.LPAREN, "err.expected_lparam");
        while (token.type != Token.RPAREN)
          {
            if ((token.type & Token.ASTRING) == 0)
              {
                throw createException("err.expected_astring", token);
              }
            String resource = UTF7imap.decode(toAString(token));
            token = requireToken(Token.NUMBER, "err.expected_number");
            currentUsage.put(resource, new Integer(token.stringValue()));
            token = requireToken(Token.NUMBER, "err.expected_number");
            limit.put(resource, new Integer(token.stringValue()));
            token = in.next();
          }
        callback.quota(quotaRoot, currentUsage, limit);
      }
    else if (QUOTAROOT.equals(token.stringValue()))
      {
        List<String> roots = new ArrayList<String>();
        token = requireToken(Token.ASTRING, "err.expected_astring");
        String mailbox = UTF7imap.decode(toAString(token));
        while (token.type != Token.EOL)
          {
            if ((token.type & Token.ASTRING) == 0)
              {
                throw createException("err.expected_astring", token);
              }
            roots.add(UTF7imap.decode(toAString(token)));
            token = in.next();
          }
        callback.quotaroot(mailbox, roots);
      }
    else
      {
        String message = L10N.getString("err.unhandled_response");
        message = MessageFormat.format(message, token);
        logger.warning(message);
      }
  }

  /*
   * message-data | number SP "EXISTS" | number SP "RECENT"
   */
  void parseUntaggedNumber(int number, IMAPCallback callback)
    throws IOException
  {
    Token token;
    token = requireToken(Token.ATOM, "err.expected_atom");
    if (EXPUNGE.equals(token.stringValue()))
      {
        callback.expunge(number);
      }
    else if (FETCH.equals(token.stringValue()))
      {
        List<FetchDataItem> items = new ArrayList<FetchDataItem>();
        requireToken(Token.LPAREN, "err.expected_lparen");
        do
          {
            token = requireToken(Token.ATOM | Token.RPAREN,
                                 "err.unexpected_fetch_data_item");
            if (token.type == Token.ATOM)
              {
                items.add(parseFetchDataItem(token.stringValue()));
              }
          }
        while (token.type != Token.RPAREN);
        callback.fetch(number, items);
      }
    else if (EXISTS.equals(token.stringValue()))
      {
        callback.exists(number);
      }
    else if (RECENT.equals(token.stringValue()))
      {
        callback.recent(number);
      }
    else
      {
        throw new IMAPException(token.stringValue(), "err.unexpected_token");
      }
  }

  private FetchDataItem parseFetchDataItem(String code)
    throws IOException
  {
    Token token;
    if (IMAPConstants.FLAGS.equals(code))
      {
        return new FLAGS(parseFlags(false));
      }
    else if (IMAPConstants.UID.equals(code))
      {
        token = requireToken(Token.NUMBER, "err.expected_number");
        return new UID(token.longValue());
      }
    else if (IMAPConstants.INTERNALDATE.equals(code))
      {
        token = requireToken(Token.STRING, "err.expected_string");
        Date date = DATETIME_FORMAT.parse(toString(token),
                                          new ParsePosition(0));
        return new INTERNALDATE(date);
      }
    else if (IMAPConstants.RFC822_SIZE.equals(code))
      {
        token = requireToken(Token.NUMBER, "err.expected_number");
        return new RFC822_SIZE(token.intValue());
      }
    else if (IMAPConstants.BODYSTRUCTURE.equals(code))
      {
        return parseBodyStructure();
      }
    else if (IMAPConstants.ENVELOPE.equals(code))
      {
        return parseEnvelope();
      }
    else if (RFC822.equals(code))
      {
        token = requireToken(Token.STRING, "err.expected_string");
        return new BODY(null, -1, token.literalValue());
      }
    else if (RFC822_HEADER.equals(code))
      {
        token = requireToken(Token.STRING, "err.expected_string");
        return new BODY("HEADER", -1, token.literalValue());
      }
    else if (RFC822_TEXT.equals(code))
      {
        token = requireToken(Token.STRING, "err.expected_string");
        return new BODY("TEXT", -1, token.literalValue());
      }
    else if (IMAPConstants.BODY.equals(code))
      {
        token = in.peek();
        if (token.type == Token.LBRACKET)
          {
            token = in.next(); // consume it
            // optional section
            token = requireToken(Token.NUMBER | Token.ATOM |
                                 Token.RBRACKET,
                                 "err.expected_section");
            String section = null;
            if (token.type == Token.ATOM |
                token.type == Token.NUMBER)
              {
                section = token.stringValue();
                token = requireToken(Token.RBRACKET,
                                     "err.expected_rbracket");
              }
            // handle <n>
            int offset = -1;
            token = requireToken(Token.STRING | Token.ATOM,
                                 "err.expected_string");
            if (token.type == Token.ATOM)
              {
                int len = token.stringValue().length();
                if (len > 2 && token.stringValue().charAt(0) == '<' &&
                    token.stringValue().charAt(len - 1) == '>')
                  {
                    String so = token.stringValue().substring(1, len - 1);
                    offset = Integer.parseInt(so);
                    token = requireToken(Token.STRING,
                                 "err.expected_string");
                  }
                else
                  {
                    throw createException("err_invalid_offset", token);
                  }
              }
            // body literal
            return new BODY(section, offset, token.literalValue());
          }
        else
          {
            return parseBodyStructure();
          }
      }
    else
      {
        String message = L10N.getString("err.unexpected_fetch_data_item");
        message = MessageFormat.format(message, code);
        throw new IMAPException(code, message);
      }
  }

  private List<String> parseFlags(boolean allowNil)
    throws IOException
  {
    List<String> data = new ArrayList<String>();
    Token token;
    token = requireToken(Token.LPAREN, "err.expected_lparen");
    do
      {
        token = in.next();
        switch (token.type)
          {
          case Token.ATOM:
            data.add(token.stringValue());
            break;
          case Token.RPAREN:
            break;
          case Token.NIL:
            if (allowNil)
              {
                data.add("NIL");
                break;
              }
          default:
            throw createException("err.expected_atom_or_rparen", token);
          }
      }
    while (token.type != Token.RPAREN);
    return data;
  }

  private BODYSTRUCTURE parseBodyStructure()
    throws IOException
  {
    Token token;
    token = requireToken(Token.LPAREN, "err.expected_lparen");
    return new BODYSTRUCTURE(parsePart());
  }

  // (x y z)
  // ((x y z) (x y z) y z)
  // (((x y z) (x y z) y z) (x y z) y z)

  private BODYSTRUCTURE.Part parsePart()
    throws IOException
  {
    // We have seen the opening '(' of the part
    // The final ')' will be consumed by skipExtensionData
    Token token;
    token = requireToken(Token.LPAREN | Token.STRING,
                         "err.expected_part");
    return (token.type == Token.LPAREN) ?
        parseMultipart() :
        parseBodyPart(token);
  }

  private BODYSTRUCTURE.Part parseBodyPart(Token token)
    throws IOException
  {
    String primaryType = toLowerCaseString(token);
    token = requireToken(Token.NSTRING, "err.expected_nstring");
    String subtype = toLowerCaseString(token);
    Map<String,String> params = null;
    token = requireToken(Token.NIL | Token.LPAREN,
                         "err.expected_lparen");
    if (token.type == Token.LPAREN)
      {
        params = parseMIMEParameters();
      }
    token = requireToken(Token.NSTRING, "err.expected_nstring");
    String id = toLowerCaseString(token);
    token = requireToken(Token.NSTRING, "err.expected_nstring");
    String description = toLowerCaseString(token);
    token = requireToken(Token.NSTRING, "err.expected_nstring");
    String encoding = toLowerCaseString(token);
    token = requireToken(Token.NUMBER, "err.expected_number");
    int size = token.intValue();
    BODYSTRUCTURE.Part ret;
    if ("text".equals(primaryType))
      {
        token = requireToken(Token.NUMBER, "err.expected_number");
        int lines = token.intValue();
        ret = new BODYSTRUCTURE.TextPart(primaryType, subtype, params,
                                         id, description, encoding, size,
                                         lines);
      }
    else if ("message".equals(primaryType))
      {
        ENVELOPE envelope = parseEnvelope();
        BODYSTRUCTURE.Part bodystructure = parsePart();
        token = requireToken(Token.NUMBER, "err.expected_number");
        int lines = token.intValue();
        ret = new BODYSTRUCTURE.MessagePart(primaryType, subtype, params,
                                            id, description, encoding, size,
                                            envelope, bodystructure, lines);
      }
    else
      {
        ret = new BODYSTRUCTURE.BodyPart(primaryType, subtype, params,
                                         id, description, encoding, size);
      }
    skipExtensionData();
    return ret;
  }

  private BODYSTRUCTURE.Part parseMultipart()
    throws IOException
  {
    // We have seen the '(' of the first part
    List<BODYSTRUCTURE.Part> parts = new ArrayList<BODYSTRUCTURE.Part>();
    parts.add(parsePart());
    Token token;
    do
      {
        token = requireToken(Token.LPAREN | Token.STRING,
                             "err.expected_part");
        if (token.type == Token.LPAREN)
          {
            parts.add(parsePart());
          }
      }
    while (token.type == Token.LPAREN);
    String subtype = toLowerCaseString(token);
    Map<String,String> parameters = null;
    BODYSTRUCTURE.Disposition disposition = null;
    List<String> language = null;
    List<String> location = null;
    token = requireToken(Token.NIL | Token.RPAREN |
                         Token.LPAREN, "err.expected_plist");
    if (token.type != Token.RPAREN)
      {
        if (token.type == Token.LPAREN)
          {
            parameters = parseMIMEParameters();
          }
        token = requireToken(Token.NIL | Token.RPAREN |
                             Token.LPAREN, "err.expected_plist");
        if (token.type != Token.RPAREN)
          {
            if (token.type == Token.LPAREN)
              {
                disposition = parseDisposition();
              }
            token = requireToken(Token.NIL | Token.RPAREN |
                                 Token.LPAREN, "err.expected_plist");
            if (token.type != Token.RPAREN)
              {
                if (token.type == Token.LPAREN)
                  {
                    language = parseStringList();
                  }
                token = requireToken(Token.NIL | Token.RPAREN |
                                     Token.LPAREN, "err.expected_plist");
                if (token.type != Token.RPAREN)
                  {
                    if (token.type == Token.LPAREN)
                      {
                        location = parseStringList();
                      }
                    skipExtensionData();
                  }
              }
          }
      }
    return new BODYSTRUCTURE.Multipart(parts, subtype, parameters,
                                       disposition, language, location);
  }

  private void skipExtensionData()
    throws IOException
  {
    int depth = 1;
    while (depth > 0)
      {
        Token token = in.next();
        switch (token.type)
          {
          case Token.LPAREN:
            depth++;
            break;
          case Token.RPAREN:
            depth--;
            break;
          case Token.EOL:
            throw createException("err.unexpected_eol");
          }
      }
  }

  private Map<String,String> parseMIMEParameters()
    throws IOException
  {
    // We have seen '('
    Token token;
    Map<String,String> map = new LinkedHashMap<String,String>();
    do
      {
        token = requireToken(Token.STRING | Token.RPAREN,
                             "err.expected_string");
        if ((token.type & Token.STRING) != 0)
          {
            String key = toLowerCaseString(token);
            token = requireToken(Token.STRING,
                                 "err.expected_string");
            String val = toString(token);
            map.put(key, val);
          }
      }
    while (token.type != Token.RPAREN);
    return map;
  }

  private BODYSTRUCTURE.Disposition parseDisposition()
    throws IOException
  {
    // We have seen '('
    Token token;
    token = requireToken(Token.STRING, "err.expected_string");
    String type = toLowerCaseString(token);
    Map<String,String> params = null;
    token = requireToken(Token.NIL | Token.LPAREN,
                         "err.expected_lparen");
    if (token.type == Token.LPAREN)
      {
        params = parseMIMEParameters();
      }
    token = requireToken(Token.RPAREN, "err.expected_rparen");
    return new BODYSTRUCTURE.Disposition(type, params);
  }

  private ENVELOPE parseEnvelope()
    throws IOException
  {
    Token token = in.next();
    switch (token.type)
      {
      case Token.NIL:
        return null;
      case Token.LPAREN:
        token = requireToken(Token.STRING, "err.expected_envelope");
        String date = toString(token);
        token = requireToken(Token.NSTRING, "err.expected_envelope");
        String subject = toString(token);
        List<ENVELOPE.Address> from = parseAddressList();
        List<ENVELOPE.Address> sender = parseAddressList();
        List<ENVELOPE.Address> replyTo = parseAddressList();
        List<ENVELOPE.Address> to = parseAddressList();
        List<ENVELOPE.Address> cc = parseAddressList();
        List<ENVELOPE.Address> bcc = parseAddressList();
        token = requireToken(Token.NSTRING, "err.expected_envelope");
        String inReplyTo = toString(token);
        token = requireToken(Token.NSTRING, "err.expected_envelope");
        String messageId = toString(token);
        return new ENVELOPE(date, subject, from, sender, replyTo, to, cc, bcc,
                            inReplyTo, messageId);
      default:
        throw new IOException("err.expected_plist");
      }
  }

  private List<ENVELOPE.Address> parseAddressList()
    throws IOException
  {
    // We have NOT seen '('
    Token token = in.next();
    switch (token.type)
      {
      case Token.NIL:
        return null;
      case Token.LPAREN:
        List<ENVELOPE.Address> ret = new ArrayList<ENVELOPE.Address>();
        do
          {
            token = requireToken(Token.LPAREN | Token.RPAREN,
                                 "err.expected_address");
            if (token.type == Token.LPAREN)
              {
                token = requireToken(Token.NSTRING,
                                     "err.expected_string");
                String personal = toString(token);
                in.next();
                token = requireToken(Token.NSTRING,
                                     "err.expected_string");
                String address = toString(token);
                token = requireToken(Token.NSTRING,
                                     "err.expected_string");
                String host = toString(token);
                if (host != null)
                  {
                    address = new StringBuilder(address).append('@')
                      .append(host).toString();
                  }
                ret.add(new ENVELOPE.Address(personal, address));
                requireToken(Token.RPAREN, "err.expected_rparen");
              }
          }
        while (token.type != Token.RPAREN);
        return ret;
      default:
        throw new IOException("err.expected_plist");
      }
  }

  private List<String> parseStringList()
    throws IOException
  {
    // We have seen '('
    Token token;
    List<String> data = new ArrayList<String>();
    do
      {
        token = requireToken(Token.STRING | Token.RPAREN,
                             "err.expected_string");
        if ((token.type & Token.STRING) != 0)
          {
            data.add(token.stringValue());
          }
      }
    while (token.type != Token.RPAREN);
    return data;
  }

  private List<Integer> parseNumberList()
    throws IOException
  {
    List<Integer> data = new ArrayList<Integer>();
    Token token;
    do
      {
        token = requireToken(Token.NUMBER | Token.EOL,
                             "err.expected_number");
        if (token.type == Token.NUMBER)
          {
            data.add(new Integer(token.stringValue()));
          }
      }
    while (token.type != Token.EOL);
    return data;
  }

  private List<Namespace> parseNamespaces()
    throws IOException
  {
    Token token;
    token = requireToken(Token.NIL | Token.LPAREN, "err.expected_namespace");
    if (token.type == Token.NIL)
      {
        return null;
      }
    List<Namespace> ret = new ArrayList<Namespace>();
    do
      {
        token = requireToken(Token.LPAREN | Token.RPAREN,
                             "err.expected_namespace");
        if (token.type == Token.LPAREN)
          {
            ret.add(parseNamespace());
          }
      }
    while (token.type == Token.LPAREN);
    return ret;
  }

  private Namespace parseNamespace()
    throws IOException
  {
    Token token;
    token = requireToken(Token.STRING, "err.expected_string");
    String prefix = UTF7imap.decode(toString(token));
    token = requireToken(Token.STRING, "err.expected_string");
    String hierarchyDelimiter = toString(token);
    token = requireToken(Token.RPAREN, "err.expected_rparen");
    return new Namespace(prefix, hierarchyDelimiter);
  }

  private Map<String,String> parseACL()
    throws IOException
  {
    Map<String, String> acls = new LinkedHashMap<String,String>();
    Token token;
    do
      {
        token = requireToken(Token.ASTRING | Token.EOL,
                             "err.expected_acl");
        if ((token.type & Token.ASTRING) != 0)
          {
            String identifier = UTF7imap.decode(toString(token));
            token = requireToken(Token.ASTRING,
                                 "err.expected_astring");
            String rights = toString(token);
            acls.put(identifier, rights);
          }
      }
    while (token.type != Token.EOL);
    return acls;
  }

  // -- end parsing --

  // -- IMAP commands --

  /**
   * Returns a list of the capabilities of the IMAP server.
   */
  public boolean capability(IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(CAPABILITY, callback);
  }

  /**
   * Ping the server.
   * The callback will be notified of any changes in state.
   */
  public void noop(IMAPCallback callback)
    throws IOException
  {
    invokeSimpleCommand(NOOP, callback);
  }

  /**
   * Returns a configured SSLSocketFactory to use in creating new SSL
   * sockets.
   * @param tm an optional trust manager to use
   */
  protected SSLSocketFactory getSSLSocketFactory(TrustManager tm)
    throws GeneralSecurityException
  {
    if (tm == null)
      {
        tm = new EmptyX509TrustManager();
      }
    SSLContext context = SSLContext.getInstance("TLS");
    TrustManager[] trust = new TrustManager[] { tm };
    context.init(null, trust, null);
    return context.getSocketFactory();
  }

  /**
   * Attempts to start TLS on the specified connection.
   * See RFC 2595 for details.
   * @return true if successful, false otherwise
   */
  public boolean starttls(IMAPCallback callback)
    throws IOException
  {
    return starttls(callback, new EmptyX509TrustManager());
  }

  /**
   * Attempts to start TLS on the specified connection.
   * See RFC 2595 for details.
   * @param tm the custom trust manager to use
   * @return true if successful, false otherwise
   */
  public boolean starttls(IMAPCallback callback, TrustManager tm)
    throws IOException
  {
    try
      {
        SSLSocketFactory factory = getSSLSocketFactory(tm);
        String hostname = socket.getInetAddress().getHostName();
        int port = socket.getPort();

        if (!invokeSimpleCommand(STARTTLS, callback))
          {
            return false;
          }

        SSLSocket ss =
          (SSLSocket) factory.createSocket(socket, hostname, port, true);

        if (sslTlsProtocolsIfAny != null)
          ss.setEnabledProtocols(sslTlsProtocolsIfAny);
        ss.setUseClientMode(true);
        ss.startHandshake();

        InputStream is = ss.getInputStream();
        is = new BufferedInputStream(is);
        in = new Tokenizer(is, this, literalFactory, literalThreshold);
        OutputStream os = ss.getOutputStream();
        os = new BufferedOutputStream(os);
        out = new CRLFOutputStream(os);
        return true;
      }
    catch (GeneralSecurityException e)
      {
        e.printStackTrace();
        return false;
      }
  }

  /**
   * Login to the connection using the username and password method.
   * @param username the authentication principal
   * @param password the authentication credentials
   * @return true if authentication was successful, false otherwise
   */
  public boolean login(String username, String password, IMAPCallback callback)
    throws IOException
  {
    String cmd = new StringBuilder(LOGIN)
        .append(' ')
        .append(quote(username))
        .append(' ')
        .append(quote(password))
        .toString();
    return invokeSimpleCommand(cmd, callback);
  }

  /**
   * Authenticates the connection using the specified SASL mechanism,
   * username, and password.
   * @param mechanism a SASL authentication mechanism, e.g. LOGIN, PLAIN,
   * CRAM-MD5, GSSAPI
   * @param username the authentication principal
   * @param password the authentication credentials
   * @return true if authentication was successful, false otherwise
   */
  public boolean authenticate(String mechanism, String username,
                              String password, IMAPCallback callback)
    throws IOException
  {
    try
      {
        String[] m = new String[] { mechanism };
        CallbackHandler ch = new SaslCallbackHandler(username, password);
        // Avoid lengthy callback procedure for GNU Crypto
        HashMap p = new HashMap();
        p.put("gnu.crypto.sasl.username", username);
        p.put("gnu.crypto.sasl.password", password);
        SaslClient sasl = Sasl.createSaslClient(m, null, "imap",
                                                socket.getInetAddress().
                                                getHostName(), p, ch);
        if (sasl == null)
          {
            // Fall back to home-grown SASL clients
            if ("LOGIN".equalsIgnoreCase(mechanism))
              {
                sasl = new SaslLogin(username, password);
              }
            else if ("PLAIN".equalsIgnoreCase(mechanism))
              {
                sasl = new SaslPlain(username, password);
              }
            else if ("CRAM-MD5".equalsIgnoreCase(mechanism))
              {
                sasl = new SaslCramMD5(username, password);
              }
            else
              {
                String message = L10N.getString("warn.sasl_not_available");
                message = MessageFormat.format(message, mechanism);
                logger.log(Level.FINEST, message);
                return false;
              }
          }
        StringBuilder buf = new StringBuilder(AUTHENTICATE)
            .append(' ')
            .append(mechanism);
        if (capabilities.contains("SASL-IR") &&
            sasl.hasInitialResponse()) // see RFC 4959
          {
            byte[] ir = BASE64.encode(sasl.evaluateChallenge(new byte[0]));
            buf.append(' ');
            buf.append(new String(ir, US_ASCII));
          }
        String tag = newTag();
        sendCommand(tag, buf.toString());
        while (true)
          {
            Token token = in.next();
            switch (token.type)
              {
              case Token.CONTINUATION:
                String text = in.collectToEOL();
                in.reset();
                try
                  {
                    byte[] c0 = text.getBytes(US_ASCII);
                    byte[] c1 = BASE64.decode(c0); // challenge
                    byte[] r0 = sasl.evaluateChallenge(c1);
                    byte[] r1 = BASE64.encode(r0); // response
                    out.write(r1);
                    if (isDebug())
                      {
                        debug("> " + new String(r1, US_ASCII));
                      }
                  }
                catch (SaslException e)
                  {
                    // Error in SASL challenge evaluation - cancel exchange
                    out.write(0x2a);
                    if (isDebug())
                      {
                        debug("> *");
                      }
                  }
                out.writeln();
                out.flush();
                break;
              case Token.TAG:
                boolean match = tag.equals(token.stringValue());
                boolean result = parseRespCondState(callback);
                in.collectToEOL();
                in.reset();
                if (match)
                  {
                    if (result) // OK
                      {
                        String qop =
                          (String) sasl.getNegotiatedProperty(Sasl.QOP);
                        if ("auth-int".equalsIgnoreCase(qop) ||
                            "auth-conf".equalsIgnoreCase(qop))
                          {
                            InputStream is = socket.getInputStream();
                            is = new BufferedInputStream(is);
                            is = new SaslInputStream(sasl, is);
                            in = new Tokenizer(is, this, literalFactory, literalThreshold);
                            OutputStream os = socket.getOutputStream();
                            os = new BufferedOutputStream(os);
                            os = new SaslOutputStream(sasl, os);
                            out = new CRLFOutputStream(os);
                          }
                      }
                    return result;
                  }
                break;
              case Token.UNTAGGED_RESPONSE:
                token = in.next();
                switch (token.type)
                  {
                  case Token.NUMBER:
                    int number = token.intValue();
                    parseUntaggedNumber(number, callback);
                    break;
                  case Token.ATOM:
                    parseUntaggedAtom(token, callback);
                    break;
                  }
                in.collectToEOL();
                in.reset();
                break;
              default:
                throw createException("err.unexpected_token", token);
              }
          }
      }
    catch (SaslException e)
      {
        logger.log(IMAP_TRACE, e.getMessage(), e);
        return false; // No provider for mechanism
      }
    catch (RuntimeException e)
      {
        logger.log(IMAP_TRACE, e.getMessage(), e);
        return false; // No javax.security.sasl classes
      }
  }

  /**
   * Logout this connection.
   * Underlying network resources will be freed.
   */
  public void logout(IMAPCallback callback)
    throws IOException
  {
    invokeSimpleCommand(LOGOUT, callback);
    in.reset();
    socket.close();
  }

  /**
   * Selects the specified mailbox.
   * The mailbox is identified as read-write if writes are permitted.
   * @param mailbox the mailbox name
   * @param callback the callback to be notified of the state of the
   * selected mailbox
   */
  public boolean select(String mailbox, IMAPCallback callback)
    throws IOException
  {
    return selectImpl(mailbox, SELECT, callback);
  }

  /**
   * Selects the specified mailbox.
   * The mailbox is identified as read-only.
   * @param mailbox the mailbox name
   * @param callback the callback to be notified of the state of the
   * selected mailbox
   */
  public boolean examine(String mailbox, IMAPCallback callback)
    throws IOException
  {
    return selectImpl(mailbox, EXAMINE, callback);
  }

  private boolean selectImpl(String mailbox, String command,
                             IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(new StringBuilder(command)
                               .append(' ')
                               .append(quote(UTF7imap.encode(mailbox)))
                               .toString(), callback);
  }

  /**
   * Creates a mailbox with the specified name.
   * @param mailbox the mailbox name
   * @return true if the mailbox was successfully created, false otherwise
   */
  public boolean create(String mailbox, IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(new StringBuilder(CREATE)
                               .append(' ')
                               .append(quote(UTF7imap.encode(mailbox)))
                               .toString(), callback);
  }

  /**
   * Deletes the mailbox with the specified name.
   * @param mailbox the mailbox name
   * @return true if the mailbox was successfully deleted, false otherwise
   */
  public boolean delete(String mailbox, IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(new StringBuilder(DELETE)
                               .append(' ')
                               .append(quote(UTF7imap.encode(mailbox)))
                               .toString(), callback);
  }

  /**
   * Renames the source mailbox to the specified name.
   * @param source the source mailbox name
   * @param target the target mailbox name
   * @return true if the mailbox was successfully renamed, false otherwise
   */
  public boolean rename(String source, String target, IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(new StringBuilder(RENAME)
                               .append(' ')
                               .append(quote(UTF7imap.encode(source)))
                               .append(' ')
                               .append(quote(UTF7imap.encode(target)))
                               .toString(), callback);
  }

  /**
   * Adds the specified mailbox to the set of subscribed mailboxes as
   * returned by the LSUB command.
   * @param mailbox the mailbox name
   * @return true if the mailbox was successfully subscribed, false otherwise
   */
  public boolean subscribe(String mailbox, IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(new StringBuilder(SUBSCRIBE)
                               .append(' ')
                               .append(quote(UTF7imap.encode(mailbox)))
                               .toString(), callback);
  }

  /**
   * Removes the specified mailbox from the set of subscribed mailboxes as
   * returned by the LSUB command.
   * @param mailbox the mailbox name
   * @return true if the mailbox was successfully unsubscribed, false otherwise
   */
  public boolean unsubscribe(String mailbox, IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(new StringBuilder(UNSUBSCRIBE)
                               .append(' ')
                               .append(quote(UTF7imap.encode(mailbox)))
                               .toString(), callback);
  }

  /**
   * Returns a subset of names from the complete set of names available to
   * the client.
   * @param reference the context relative to which mailbox names are
   * defined
   * @param mailbox a mailbox name, possibly including IMAP wildcards
   */
  public boolean list(String reference, String mailbox,
                      IMAPCallback callback)
    throws IOException
  {
    return listImpl(LIST, reference, mailbox, callback);
  }

  /**
   * Returns a subset of subscribed names.
   * @param reference the context relative to which mailbox names are
   * defined
   * @param mailbox a mailbox name, possibly including IMAP wildcards
   * @see #list
   */
  public boolean lsub(String reference, String mailbox,
                      IMAPCallback callback)
    throws IOException
  {
    return listImpl(LSUB, reference, mailbox, callback);
  }

  private boolean listImpl(String command, String reference,
                           String mailbox, IMAPCallback callback)
    throws IOException
  {
    if (reference == null)
      {
        reference = "";
      }
    if (mailbox == null)
      {
        mailbox = "";
      }
    StringBuilder buf = new StringBuilder(command)
      .append(' ')
      .append(quote(UTF7imap.encode(reference)))
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)));
    return invokeSimpleCommand(buf.toString(), callback);
  }

  /**
   * Requests the status of the specified mailbox.
   */
  public boolean status(String mailbox, List<String> statusNames,
                        IMAPCallback callback)
    throws IOException
  {
    StringBuilder buf = new StringBuilder(STATUS)
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)))
      .append(' ')
      .append('(');
    int len = statusNames.size();
    for (int i = 0; i < len; i++)
      {
        if (i > 0)
          {
            buf.append(' ');
          }
        buf.append(statusNames.get(i));
      }
    buf.append(')');
    return invokeSimpleCommand(buf.toString(), callback);
  }

  /**
   * Append a message to the specified mailbox.
   * @param mailbox the mailbox name
   * @param flags optional list of flags to specify for the message
   * @param date optional date of the message
   * @param content the RFC822 message (including headers)
   * @return true if successful, false if error in flags/text
   */
  public boolean append(String mailbox, List<String> flags, Date date,
                        byte[] content, IMAPCallback callback)
    throws IOException
  {
    if (content == null || content.length == 0)
      {
        return false;
      }
    String tag = newTag();
    StringBuilder buf = new StringBuilder(APPEND)
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)))
      .append(' ');
    if (flags != null)
      {
        buf.append('(');
        int len = flags.size();
        for (int i = 0; i < len; i++)
          {
            if (i > 0)
              {
                buf.append(' ');
              }
            buf.append(flags.get(i));
          }
        buf.append(')');
        buf.append(' ');
      }
    if (date != null)
      {
        buf.append(DATETIME_FORMAT.format(date));
        buf.append(' ');
      }
    buf.append('{');
    buf.append(content.length);
    buf.append('}');
    sendCommand(tag, buf.toString());
    Token token = in.next();
    if (token.type != Token.CONTINUATION)
      {
        throw createException("err.expected_continuation", token);
      }
    in.collectToEOL();
    in.reset();
    out.write(content);         // write the message body
    out.writeln();
    out.flush();
    return handleSimpleResponse(tag, callback);
  }

  /**
   * Request a checkpoint of the currently selected mailbox.
   */
  public boolean check(IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(CHECK, callback);
  }

  /**
   * Permanently remove all messages that have the \Deleted flags set,
   * and close the mailbox.
   * @return true if successful, false if no mailbox was selected
   */
  public boolean close(IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(CLOSE, callback);
  }

  /**
   * Permanently removes all messages that have the \Delete flag set.
   */
  public boolean expunge(IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(EXPUNGE, callback);
  }

  /**
   * Permanently removes all messages in the specified UID-set that have
   * the \Delete flag set.
   * @param uids the non-null set of UIDs
   */
  public boolean uidExpunge(UIDSet uids, IMAPCallback callback)
    throws IOException
  {
    StringBuilder buf = new StringBuilder(UID_EXPUNGE)
      .append(' ')
      .append(uids.toString());
    return invokeSimpleCommand(buf.toString(), callback);
  }

  /**
   * Searches the currently selected mailbox for messages matching the
   * specified criteria.
   * @param charset optional charset
   * @param criteria the list of criteria
   */
  public boolean search(String charset, List<String> criteria,
                        IMAPCallback callback)
    throws IOException
  {
    StringBuilder buf = new StringBuilder(SEARCH);
    if (charset != null)
      {
        buf.append(' ');
        buf.append(charset);
      }
    if (criteria != null)
      {
        int len = criteria.size();
        for (int i = 0; i < len; i++)
          {
            buf.append(' ');
            buf.append(criteria.get(i));
          }
      }
    return invokeSimpleCommand(buf.toString(), callback);
  }

  /**
   * Retrieves data associated with the specified message in the mailbox.
   * @param message the message number, or -1 for all messages
   * @param fetchCommands the fetch commands, e.g. FLAGS
   */
  public boolean fetch(MessageSet messages, List<String> fetchCommands,
                       IMAPCallback callback)
    throws IOException
  {
    String ids = (messages == null) ? "*" : messages.toString();
    return fetchImpl(FETCH, ids, fetchCommands, callback);
  }

  /**
   * Retrieves data associated with the specified message in the mailbox.
   * @param uids the message UIDs, or null for all messages
   * @param fetchCommands the fetch commands, e.g. FLAGS
   */
  public boolean uidFetch(UIDSet uids, List<String> fetchCommands,
                          IMAPCallback callback)
    throws IOException
  {
    String ids = (uids == null) ? "*" : uids.toString();
    return fetchImpl(UID + ' ' + FETCH, ids, fetchCommands, callback);
  }

  private boolean fetchImpl(String cmd, String ids,
                            List<String> fetchCommands,
                            IMAPCallback callback)
    throws IOException
  {
    StringBuilder buf = new StringBuilder(cmd);
    buf.append(' ');
    buf.append(ids);
    buf.append(' ');
    buf.append('(');
    int len = fetchCommands.size();
    for (int i = 0; i < len; i++)
      {
        if (i > 0)
          {
            buf.append(' ');
          }
        buf.append(fetchCommands.get(i));
      }
    buf.append(')');
    return invokeSimpleCommand(buf.toString(), callback);
  }

  /**
   * Alters data associated with the specified messages in the mailbox.
   * @param messages the message set, or null for all messages
   * @param flagCommand FLAGS, +FLAGS, -FLAGS(or .SILENT versions)
   * @param flags message flags to set
   */
  public boolean store(MessageSet messages, String flagCommand,
                       List<String> flags, IMAPCallback callback)
    throws IOException
  {
    String ids = (messages == null) ? "*" : messages.toString();
    return storeImpl(STORE, ids, flagCommand, flags, callback);
  }

  /**
   * Alters data associated with the specified messages in the mailbox.
   * @param uids the message UIDs, or null for all messages
   * @param flagCommand FLAGS, +FLAGS, -FLAGS(or .SILENT versions)
   * @param flags message flags to set
   */
  public boolean uidStore(UIDSet uids, String flagCommand,
                          List<String> flags, IMAPCallback callback)
    throws IOException
  {
    String ids = (uids == null) ? "*" : uids.toString();
    return storeImpl(UID + ' ' + STORE, ids, flagCommand, flags, callback);
  }

  private boolean storeImpl(String cmd, String ids, String flagCommand,
                            List<String> flags, IMAPCallback callback)
    throws IOException
  {
    StringBuilder buf = new StringBuilder(cmd);
    buf.append(' ');
    buf.append(ids);
    buf.append(' ');
    buf.append(flagCommand);
    buf.append(' ');
    buf.append('(');
    int len = flags.size();
    for (int i = 0; i < len; i++)
      {
        if (i > 0)
          {
            buf.append(' ');
          }
        buf.append(flags.get(i));
      }
    buf.append(')');
    return invokeSimpleCommand(buf.toString(), callback);
  }

  /**
   * Copies the specified messages to the end of the destination mailbox.
   * @param messages the message-set
   * @param mailbox the destination mailbox
   */
  public boolean copy(MessageSet messages, String mailbox,
                      IMAPCallback callback)
    throws IOException
  {
    if (messages == null || messages.size() < 1)
      {
        return true;
      }
    StringBuilder buf = new StringBuilder(COPY)
      .append(' ')
      .append(messages.toString())
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)));
    return invokeSimpleCommand(buf.toString(), callback);
  }

  /**
   * Returns the namespaces available on the server.
   * @see RFC 2342
   */
  public boolean namespace(IMAPCallback callback)
    throws IOException
  {
    return invokeSimpleCommand(NAMESPACE, callback);
  }

  /**
   * Changes the access rights on the specified mailbox such that the
   * authentication principal is granted the specified permissions.
   * @param mailbox the mailbox name
   * @param principal the authentication identifier
   * @param rights the rights to assign
   * @see RFC 4314
   */
  public boolean setacl(String mailbox, String principal, int rights,
                        IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(SETACL)
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)))
      .append(' ')
      .append(UTF7imap.encode(principal))
      .append(' ')
      .append(rights);
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  /**
   * Removes any access rights for the given authentication principal on the
   * specified mailbox.
   * @param mailbox the mailbox name
   * @param principal the authentication identifier
   * @see RFC 4314
   */
  public boolean deleteacl(String mailbox, String principal,
                           IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(DELETEACL)
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)))
      .append(' ')
      .append(UTF7imap.encode(principal));
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  /**
   * Returns the access control list for the specified mailbox.
   * @param mailbox the mailbox name
   * @see RFC 4314
   */
  public boolean getacl(String mailbox, IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(GETACL)
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)));
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  /**
   * Returns the rights for the given principal for the specified mailbox.
   * The returned rights are a logical OR of RIGHTS_* bits.
   * @param mailbox the mailbox name
   * @param principal the authentication identity
   * @see RFC 4314
   */
  public boolean listrights(String mailbox, String principal,
                            IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(LISTRIGHTS)
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)))
      .append(' ')
      .append(UTF7imap.encode(principal));
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  /**
   * Returns the rights for the current principal for the specified mailbox.
   * The returned rights are a logical OR of RIGHTS_* bits.
   * @param mailbox the mailbox name
   * @see RFC 4314
   */
  public boolean myrights(String mailbox, IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(MYRIGHTS)
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)));
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  /**
   * Sets the quota for the specified quota root.
   * @param quotaRoot the quota root
   * @param resources the list of resources and associated limits to set
   */
  public boolean setquota(String quotaRoot, Map<String,Integer> resources,
                          IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(SETQUOTA)
      .append(' ')
      .append(quote(UTF7imap.encode(quotaRoot)));
    if (resources != null)
      {
        for (Iterator<String> i = resources.keySet().iterator(); i.hasNext(); )
          {
            String resource = i.next();
            int limit = resources.get(resource);
            cmd.append(' ');
            cmd.append('(');
            cmd.append(quote(UTF7imap.encode(resource)));
            cmd.append(' ');
            cmd.append(limit);
            cmd.append(')');
          }
      }
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  /**
   * Returns the specified quota root's resource usage and limits.
   * @param quotaRoot the quota root
   */
  public boolean getquota(String quotaRoot, IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(GETQUOTA)
      .append(' ')
      .append(quote(UTF7imap.encode(quotaRoot)));
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  /**
   * Returns the quotas for the given mailbox.
   * @param mailbox the mailbox name
   */
  public boolean getquotaroot(String mailbox, IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(GETQUOTAROOT)
      .append(' ')
      .append(quote(UTF7imap.encode(mailbox)));
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  /**
   * Expunges the specified range of messages.
   * See RFC 2359 for details.
   * @param start the UID of the first message to expunge
   * @param end the UID of the last message to expunge
   */
  public boolean uidExpunge(long start, long end, IMAPCallback callback)
    throws IOException
  {
    StringBuilder cmd = new StringBuilder(UID_EXPUNGE)
      .append(' ')
      .append(start)
      .append(':')
      .append(end);
    return invokeSimpleCommand(cmd.toString(), callback);
  }

  // -- Utility methods --

  /**
   * Quote the specified text if necessary.
   */
  static String quote(String text)
  {
    if (text.length() == 0 || text.indexOf(' ') != -1 ||
        text.indexOf('%') != -1)
      {
        StringBuilder buffer = new StringBuilder();
        buffer.append('"');
        buffer.append(text);
        buffer.append('"');
        return buffer.toString();
      }
    return text;
  }

}

