/*
 * SaslLogin.java
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

package gnu.inet.util;

import java.io.UnsupportedEncodingException;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

/**
 * SASL mechanism for LOGIN.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class SaslLogin
  implements SaslClient
{

  private static final int STATE_USERNAME = 0;
  private static final int STATE_PASSWORD = 1;
  private static final int STATE_COMPLETE = 2;

  private String username;
  private String password;
  private int state;

  public SaslLogin(String username, String password)
  {
    this.username = username;
    this.password = password;
    state = STATE_USERNAME;
  }

  public String getMechanismName()
  {
    return "LOGIN";
  }

  public boolean hasInitialResponse()
  {
    return false;
  }

  public byte[] evaluateChallenge(byte[] challenge)
    throws SaslException
  {
    try
      {
        switch (state)
          {
          case STATE_USERNAME:
            state = STATE_PASSWORD;
            return username.getBytes("UTF-8");
          case STATE_PASSWORD:
            state = STATE_COMPLETE;
            return password.getBytes("UTF-8");
          default:
            return new byte[0];
          }
      }
    catch (UnsupportedEncodingException e)
      {
        String msg = "The UTF-8 character set is not supported by the VM";
        throw new SaslException(msg, e);
      }
  }

  public boolean isComplete()
  {
    return (state == STATE_COMPLETE);
  }

  public byte[] unwrap(byte[] incoming, int off, int len)
    throws SaslException
  {
    byte[] ret = new byte[len - off];
    System.arraycopy(incoming, off, ret, 0, len);
    return ret;
  }

  public byte[] wrap(byte[] outgoing, int off, int len)
    throws SaslException
  {
    byte[] ret = new byte[len - off];
    System.arraycopy(outgoing, off, ret, 0, len);
    return ret;
  }

  public Object getNegotiatedProperty(String name)
  {
    return null;
  }

  public void dispose()
  {
  }

}

