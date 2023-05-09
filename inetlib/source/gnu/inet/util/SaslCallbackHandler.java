/*
 * SaslCallbackHandler.java
 * Copyright (C) 2002 The Free Software Foundation
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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * A callback handler that can manage username and password callbacks.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public final class SaslCallbackHandler
  implements CallbackHandler
{

  /*
   * The username.
   */
  private final String username;

  /*
   * The password.
   */
  private final String password;

  /**
   * Constructor.
   * @param username the value to respond to Name callbacks with
   * @param password the value to respond to Password callbacks with
   */
  public SaslCallbackHandler(String username, String password)
  {
    this.username = username;
    this.password = password;
  }

  /**
   * Handle callbacks.
   */
  public void handle(Callback[] callbacks)
    throws IOException, UnsupportedCallbackException
  {
    for (int i = 0; i < callbacks.length; i++)
      {
        if (callbacks[i] instanceof NameCallback)
          {
            NameCallback nc = (NameCallback) callbacks[i];
            nc.setName(username);
          }
        else if (callbacks[i] instanceof PasswordCallback)
          {
            PasswordCallback pc = (PasswordCallback) callbacks[i];
            pc.setPassword(password.toCharArray ());
          }
        else
          {
            throw new UnsupportedCallbackException(callbacks[i]);
          }
      }
  }

}

