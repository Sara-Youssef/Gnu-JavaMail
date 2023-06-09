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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A simple non-persistent cookie manager. This class can be extended to
 * provide cookie persistence.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class SimpleCookieManager
  implements CookieManager
{

  /**
   * The cookie cache.
   * This is a dictionary mapping domains to maps of cookies by name.
   */
  protected Map cookies;

  /**
   * Constructor.
   */
  public SimpleCookieManager()
  {
    cookies = new HashMap();
  }

  public void setCookie(Cookie cookie)
  {
    String domain = cookie.getDomain();
    Map map =(Map) cookies.get(domain);
    if (map == null)
      {
        map = new HashMap();
        cookies.put(domain, map);
      }
    String name = cookie.getName();
    map.put(name, cookie); // will replace a cookie of the same name
  }

  public Cookie[] getCookies(String host, boolean secure, String path)
  {
    List matches = new ArrayList();
    Date now = new Date();
    if (Character.isLetter(host.charAt(0)))
      {
        int di = host.indexOf('.');
        while (di != -1)
          {
            addCookies(matches, host, secure, path, now);
            host = host.substring(di);
            di = host.indexOf('.', 1);
          }
      }
    addCookies(matches, host, secure, path, now);
    Cookie[] ret = new Cookie[matches.size()];
    matches.toArray(ret);
    return ret;
  }

  private void addCookies(List matches, String domain, boolean secure,
                          String path, Date now)
  {
    Map map = (Map) cookies.get(domain);
    if (map != null)
      {
        List expired = new ArrayList();
        for (Iterator i = map.entrySet().iterator(); i.hasNext(); )
          {
            Map.Entry entry = (Map.Entry) i.next();
            Cookie cookie = (Cookie) entry.getValue();
            Date expires = cookie.getExpiryDate();
            if (expires != null && expires.before(now))
              {
                expired.add(entry.getKey());
                continue;
              }
            if (secure && !cookie.isSecure())
              {
                continue;
              }
            if (path.startsWith(cookie.getPath()))
              {
                matches.add(cookie);
              }
          }
        // Good housekeeping
        for (Iterator i = expired.iterator(); i.hasNext(); )
          {
            map.remove(i.next());
          }
      }
  }

}

