/*
 * CookieManager.java
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
 * Cookie manager interface.
 * If an application wants to handle cookies, they should implement this
 * interface and register the instance with each HTTPConnection they use.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public interface CookieManager
{

  /**
   * Stores a cookie in the cookie manager.
   * @param cookie the cookie to store
   */
  void setCookie(Cookie cookie);

  /**
   * Retrieves the cookies matching the specified criteria.
   * @param host the host name
   * @param secure whether the connection is secure
   * @param path the path to access
   */
  Cookie[] getCookies(String host, boolean secure, String path);

}
