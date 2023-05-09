/*
 * AttributeValues.java
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

package gnu.inet.ldap;

import java.util.Set;

/**
 * An attribute type and set of values to associate with it.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class AttributeValues
{

  /**
   * The attribute type.
   */
  protected final String type;

  /**
   * The values to assign.
   */
  protected final Set values;

  /**
   * Constructor.
   * @param type the attribute type
   * @param values the values to assign
   */
  public AttributeValues(String type, Set values)
  {
    if (type == null)
      {
        throw new NullPointerException("type");
      }
    this.type = type;
    this.values = values;
  }

  /**
   * @see #type
   */
  public String getType()
  {
    return type;
  }

  /**
   * @see #values
   */
  public Set getValues()
  {
    return values;
  }

}

