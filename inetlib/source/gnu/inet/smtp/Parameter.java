/*
 * Parameter.java
 * Copyright (C) 2003 The Free Software Foundation
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

package gnu.inet.smtp;

/**
 * An ESMTP parameter.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public final class Parameter
{

  final String key;
  final String value;

  /**
   * Creates a new parameter with the specified key and value.
   * @param key the key
   * @param value the value
   */
  public Parameter(String key, String value)
  {
    this.key = key;
    this.value = value;
  }

  /**
   * Returns the key.
   */
  public String getKey()
  {
    return key;
  }

  /**
   * Returns the value.
   */
  public String getValue()
  {
    return value;
  }

  /**
   * String form.
   */
  public String toString()
  {
    if (value == null)
      {
        return key;
      }
    else
      {
        return key + '=' + value;
      }
  }

}

