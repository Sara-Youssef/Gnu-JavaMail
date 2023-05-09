/*
 * Tokenizer.java
 * Copyright (C) 2013 The Free Software Foundation
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * IMAP stream tokenizer.
 * This will produce tokens until EOL, whereupon it must be reset.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class Tokenizer
{

  private static final int STATE_INIT = 0;
  private static final int STATE_RESPONSE_BODY = 1;
  private static final int STATE_EOL = 2;
  private static final int SP = 32;
  private static final int LF = 10;
  private static final int CR = 13;

  private static final LiteralFactory DEFAULT_FACTORY =
    new DefaultLiteralFactory();

  private int state;
  private InputStream in;
  private IMAPConnection connection;
  private LiteralFactory literalFactory;
  private int literalThreshold;
  private ByteArrayOutputStream sink;
  private ByteArrayOutputStream trace;
  private boolean peeking;

  Tokenizer(InputStream in,
            IMAPConnection connection,
            LiteralFactory factory,
            int threshold)
  {
    this.in = in;
    this.connection = connection;
    literalFactory = (factory != null) ? factory : DEFAULT_FACTORY;
    literalThreshold = Math.max(4096, threshold);
    sink = new ByteArrayOutputStream();
    if (connection.isDebug())
      {
        trace = new ByteArrayOutputStream();
      }
  }

  private IOException createException(String key, Object... args)
  {
    String message = IMAPConnection.L10N.getString(key);
    if (args != null)
      {
        message = MessageFormat.format(message, args);
      }
    reset(); // flush for debug output
    return new IOException(message);
  }

  Token next()
    throws IOException
  {
    if (state == STATE_EOL)
      {
        return new Token(Token.EOL);
      }
    sink.reset();
    int c = readeof();
    if (c == CR)
      {
        in.mark(1);
        int d = readeof();
        if (d == LF)
          {
            state = STATE_EOL;
            return new Token(Token.EOL);
          }
        in.reset();
      }
    else if (c == SP)
      {
        c = readeof(); // skip SP
      }
    switch (state)
      {
      case STATE_INIT:
        state = STATE_RESPONSE_BODY;
        if (c == '*')
          {
            if (readeof() != SP)
              {
                throw createException("err.bad_untagged");
              }
            return new Token(Token.UNTAGGED_RESPONSE);
          }
        else if (c == '+')
          {
            if (readeof() != SP)
              {
                throw createException("err.bad_continuation");
              }
            return new Token(Token.CONTINUATION);
          }
        return parseAtom(Token.TAG, c);
      case STATE_RESPONSE_BODY:
        if (c == '(')
          {
            return new Token(Token.LPAREN);
          }
        else if (c == '[')
          {
            return new Token(Token.LBRACKET);
          }
        else if (c == ')')
          {
            return new Token(Token.RPAREN);
          }
        else if (c == ']')
          {
            return new Token(Token.RBRACKET);
          }
        else if (c >= 48 && c <= 57)
          {
            return parseNumber(c);
          }
        else if (c == '"')
          {
            return parseQuotedString();
          }
        else if (c == '{')
          {
            return parseLiteral();
          }
        else
          {
            return parseAtom(Token.ATOM, c);
          }
      default:
        throw new IllegalStateException();
      }
  }

  /**
   * We have reached EOL. Reset for the next line.
   */
  void reset()
  {
    state = STATE_INIT;
    if (trace != null)
      {
        connection.debug("< " + asciify(trace.toByteArray()));
        trace.reset();
      }
  }

  /**
   * For debugging.
   */
  static final String asciify(byte[] data)
  {
    StringBuilder buf = new StringBuilder(data.length);
    for (int i = 0; i < data.length; i++)
      {
        byte c = data[i];
        if (c >= 32 || c <= 126 || c == 9 || c == 10)
          {
            buf.append((char) c);
          }
        else if (c != 13)
          {
            buf.append('?');
          }
      }
    return buf.toString();
  }

  Token peek()
    throws IOException
  {
    in.mark(1024);
    peeking = true;
    Token token = next();
    peeking = false;
    in.reset();
    return token;
  }

  String collectToEOL()
    throws IOException
  {
    if (state == STATE_EOL)
      {
        return null;
      }
    int c;
    do
      {
        c = readeof();
        if (c == CR)
          {
            in.mark(1);
            int d = readeof();
            if (d == LF)
              {
                state = STATE_EOL;
                return new String(sink.toByteArray(), IMAPConnection.US_ASCII);
              }
            in.reset();
          }
        sink.write(c);
      }
    while (true);
  }

  private static final byte[] ATOM_SPECIALS = new byte[]
    {
      SP, '"', '%', '(', ')', '*', '[', '\\', ']', '{'
    };

  private int readeof()
    throws IOException
  {
    int c = in.read();
    if (c == -1)
      {
        throw new EOFException();
      }
    if (!peeking && trace != null && c != 13 && c != 10)
      {
        trace.write(c);
      }
    return c;
  }

  private Token parseAtom(int type, int c)
    throws IOException
  {
    boolean isFlag = false;
    if (c <= SP || Arrays.binarySearch(ATOM_SPECIALS, (byte) c) >= 0)
      {
        if (c == '\\') // NB we treat flags as atoms
          {
            isFlag = true;
          }
        else
          {
            throw createException("err.bad_atom", (char) c);
          }
      }
    do
      {
        sink.write(c);
        in.mark(1);
        c = readeof();
        if (c <= SP || Arrays.binarySearch(ATOM_SPECIALS, (byte) c) >= 0)
          {
            if (isFlag && c == '*' && sink.size() == 1)
              {
                // flag-perm
                sink.write(c);
              }
            else if (c != SP)
              {
                in.reset();
              }
            byte[] b = sink.toByteArray();
            if (b.length == 3 && b[0] == 'N' && b[1] == 'I' && b[2] == 'L')
              {
                return new Token(Token.NIL);
              }
            return new StringToken(type, b);
          }
      }
    while (true);
  }

  private Token parseNumber(int c)
    throws IOException
  {
    int t = Token.NUMBER;
    do
      {
        sink.write(c);
        in.mark(1);
        c = readeof();
        if (c <= SP || Arrays.binarySearch(ATOM_SPECIALS, (byte) c) >= 0)
          {
            if (c != SP)
              {
                in.reset();
              }
            return new StringToken(t, sink.toByteArray());
          }
        if (c < 48 || c > 57)
          {
            t = Token.ATOM;
          }
      }
    while (true);
  }

  private Token parseQuotedString()
    throws IOException
  {
    boolean escaped = false;
    do
      {
        int c = readeof();
        if (c == '\\')
          {
            if (escaped)
              {
                sink.write(c);
              }
            escaped = !escaped;
          }
        else if (escaped)
          {
            sink.write(c);
            escaped = false;
          }
        else if (c == '"')
          {
            return new StringToken(Token.QUOTED_STRING,
                                   sink.toByteArray());
          }
        else if (c == LF)
          {
            throw createException("err.bad_quoted_string");
          }
        else
          {
            sink.write(c);
          }
      }
    while (true);
  }

  private Token parseLiteral()
    throws IOException
  {
    int c;
    for (c = readeof(); c >= 48 && c <= 57; c = readeof())
      {
        sink.write(c);
      }
    if (c != '}' || readeof() != CR || readeof() != LF)
      {
        throw createException("err.bad_literal");
      }
    int total = Integer.parseInt(new String(sink.toByteArray(),
                                            IMAPConnection.US_ASCII));
    boolean large = total > literalThreshold;
    Literal literal = large ? literalFactory.newLiteral(total) : null;
    OutputStream out = large ? literal.getOutputStream() : sink;
    int count = 0;
    sink.reset();
    byte[] buf = new byte[Math.min(total, 4096)];
    while (count < total)
      {
        int needed = Math.min(buf.length, total - count);
        int len = in.read(buf, 0, needed);
        if (len == -1)
          {
            throw new EOFException();
          }
        out.write(buf, 0, len);
        if (!large && trace != null)
          {
            trace.write(buf, 0, len);
          }
        count += len;
      }
    if (large)
      {
        return new LiteralToken(Token.LITERAL, literal);
      }
    else
      {
        return new StringToken(Token.LITERAL, sink.toByteArray());
      }
  }

  /**
   * Literal handler that just stores literals in memory.
   */
  static class DefaultLiteralFactory
    implements LiteralFactory
  {

    public Literal newLiteral(int size)
    {
      return new DefaultLiteral();
    }

  }

  static class DefaultLiteral
    implements Literal
  {

    ByteArrayOutputStream sink = new ByteArrayOutputStream();

    public OutputStream getOutputStream()
    {
      return sink;
    }

    public InputStream getInputStream()
    {
      return new ByteArrayInputStream(sink.toByteArray());
    }

  }

}
