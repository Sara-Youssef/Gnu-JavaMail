/*
 * Token.java
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

import java.util.HashMap;
import java.util.Map;

/**
 * Token in an IMAP stream.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class Token
{

  static final int NIL =  1<<0;
  static final int ATOM = 1<<1;
  static final int NUMBER = 1<<2;
  static final int LITERAL = 1<<3;
  static final int QUOTED_STRING = 1<<4;
  static final int LPAREN = 1<<5;
  static final int RPAREN = 1<<6;
  static final int LBRACKET = 1<<7;
  static final int RBRACKET = 1<<8;
  static final int TAG = 1<<9;
  static final int UNTAGGED_RESPONSE = 1<<10;
  static final int CONTINUATION = 1<<11;
  static final int EOL = 1<<12;

  static final int STRING = QUOTED_STRING | LITERAL;
  static final int ASTRING = ATOM | STRING;
  static final int NSTRING = NIL | STRING;

  final int type;

  Token(int type)
  {
    this.type = type;
  }

  int intValue()
  {
    return -1;
  }

  long longValue()
  {
    return -1L;
  }

  String stringValue()
  {
    return null;
  }

  Literal literalValue()
  {
    return null;
  }

  public String toString()
  {
    return TYPES.get(type);
  }

  private static final Map<Integer,String> TYPES;
  static
  {
    TYPES = new HashMap();
    TYPES.put(NIL, "NIL");
    TYPES.put(ATOM, "ATOM");
    TYPES.put(NUMBER, "NUMBER");
    TYPES.put(NUMBER, "NUMBER");
    TYPES.put(LITERAL, "LITERAL");
    TYPES.put(QUOTED_STRING, "QUOTED_STRING");
    TYPES.put(LPAREN, "LPAREN");
    TYPES.put(RPAREN, "RPAREN");
    TYPES.put(LBRACKET, "LBRACKET");
    TYPES.put(RBRACKET, "RBRACKET");
    TYPES.put(TAG, "TAG");
    TYPES.put(UNTAGGED_RESPONSE, "UNTAGGED_RESPONSE");
    TYPES.put(CONTINUATION, "CONTINUATION");
    TYPES.put(EOL, "EOL");
  }

}
