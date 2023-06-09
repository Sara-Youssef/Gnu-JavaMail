/*
 * Headers.java
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

import java.io.InputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import gnu.inet.util.LineInputStream;

/**
 * A collection of HTTP header names and associated values.
 * Retrieval of values is case insensitive. An iteration over the keys
 * returns the header names in the order they were received.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class Headers
  implements Map
{

  static final DateFormat dateFormat = new HTTPDateFormat();

  static class Header
  {

    final String name;

    Header(String name)
    {
      if (name == null || name.length() == 0)
        {
          throw new IllegalArgumentException(name);
        }
      this.name = name;
    }

    public int hashCode()
    {
      return name.toLowerCase().hashCode();
    }

    public boolean equals(Object other)
    {
      if (other instanceof Header)
        {
          return ((Header) other).name.equalsIgnoreCase(name);
        }
      return false;
    }

    public String toString()
    {
      return name;
    }

  }

  static class HeaderEntry
    implements Map.Entry
  {

    final Map.Entry entry;

    HeaderEntry(Map.Entry entry)
    {
      this.entry = entry;
    }

    public Object getKey()
    {
      return ((Header) entry.getKey()).name;
    }

    public Object getValue()
    {
      return entry.getValue();
    }

    public Object setValue(Object value)
    {
      return entry.setValue(value);
    }

    public int hashCode()
    {
      return entry.hashCode();
    }

    public boolean equals(Object other)
    {
      return entry.equals(other);
    }

    public String toString()
    {
      return getKey().toString() + "=" + getValue();
    }

  }

  private LinkedHashMap headers;

  public Headers()
  {
    headers = new LinkedHashMap();
  }

  public int size()
  {
    return headers.size();
  }

  public boolean isEmpty()
  {
    return headers.isEmpty();
  }

  public boolean containsKey(Object key)
  {
    return headers.containsKey(new Header((String) key));
  }

  public boolean containsValue(Object value)
  {
    return headers.containsValue(value);
  }

  public Object get(Object key)
  {
    return headers.get(new Header((String) key));
  }

  /**
   * Returns the value of the specified header as a string.
   */
  public String getValue(String header)
  {
    return (String) headers.get(new Header(header));
  }

  /**
   * Returns the value of the specified header as an integer,
   * or -1 if the header is not present or not an integer.
   */
  public int getIntValue(String header)
  {
    String val = getValue(header);
    if (val == null)
      {
        return -1;
      }
    try
      {
        return Integer.parseInt(val);
      }
    catch (NumberFormatException e)
      {
      }
    return -1;
  }

  /**
   * Returns the value of the specified header as a date,
   * or <code>null</code> if the header is not present or not a date.
   */
  public Date getDateValue(String header)
  {
    String val = getValue(header);
    if (val == null)
      {
        return null;
      }
    try
      {
        return dateFormat.parse(val);
      }
    catch (ParseException e)
      {
        return null;
      }
  }

  public Object put(Object key, Object value)
  {
    return headers.put(new Header((String) key), value);
  }

  public Object remove(Object key)
  {
    return headers.remove(new Header((String) key));
  }

  public void putAll(Map t)
  {
    for (Iterator i = t.keySet().iterator(); i.hasNext(); )
      {
        String key = (String) i.next();
        String value = (String) t.get(key);
        headers.put(new Header(key), value);
      }
  }

  public void clear()
  {
    headers.clear();
  }

  public Set keySet()
  {
    Set keys = headers.keySet();
    Set ret = new LinkedHashSet();
    for (Iterator i = keys.iterator(); i.hasNext(); )
      {
        ret.add(((Header) i.next()).name);
      }
    return ret;
  }

  public Collection values()
  {
    return headers.values();
  }

  public Set entrySet()
  {
    Set entries = headers.entrySet();
    Set ret = new LinkedHashSet();
    for (Iterator i = entries.iterator(); i.hasNext(); )
      {
        Map.Entry entry = (Map.Entry) i.next();
        ret.add(new HeaderEntry(entry));
      }
    return ret;
  }

  public boolean equals(Object other)
  {
    return headers.equals(other);
  }

  public int hashCode()
  {
    return headers.hashCode();
  }

  /**
   * Parse the specified input stream, adding headers to this collection.
   */
  public void parse(InputStream in)
    throws IOException
  {
    LineInputStream lin = (in instanceof LineInputStream) ?
      (LineInputStream) in : new LineInputStream(in);

    String name = null;
    StringBuffer value = new StringBuffer();
    while (true)
      {
        String line = lin.readLine();
        if (line == null)
          {
            if (name != null)
              {
                addValue(name, value.toString());
              }
            break;
          }
        int len = line.length();
        if (len < 2)
          {
            if (name != null)
              {
                addValue(name, value.toString());
              }
            break;
          }
        char c1 = line.charAt(0);
        if (c1 == ' ' || c1 == '\t')
          {
            // Continuation
            value.append(line.substring(0, len - 1));
          }
        else
          {
            if (name != null)
              {
                addValue(name, value.toString());
              }

            int di = line.indexOf(':');
            name = line.substring(0, di);
            value.setLength(0);
            do
              {
                di++;
              }
            while (di < len && line.charAt(di) == ' ');
            value.append(line.substring(di, len - 1));
          }
      }
  }

  private void addValue(String name, String value)
  {
    Header key = new Header(name);
    String old = (String) headers.get(key);
    if (old == null)
      {
        headers.put(key, value);
      }
    else
      {
        headers.put(key, old + ", " + value);
      }
  }

}

