/*
 * Credentials.java
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

/**
 * Represents a username/password combination that can be used to
 * authenticate to an HTTP server.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class Credentials
{

  /**
   * The username.
   */
  private String username;

  /**
   * The password.
   */
  private String password;

  /**
   * Constructor.
   * @param username the username
   * @param password the password
   */
  public Credentials(String username, String password)
  {
    this.username = username;
    this.password = password;
  }

  /**
   * Returns the username.
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Returns the password.
   */
  public String getPassword()
  {
    return password;
  }

}

