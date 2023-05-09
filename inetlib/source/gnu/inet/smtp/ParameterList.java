/*
 * ParameterList.java
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

import java.util.ArrayList;
import java.util.List;

/**
 * A list of ESMTP parameters.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public final class ParameterList
{

  private List parameters = new ArrayList();

  /**
   * Returns the number of parameters in the list.
   */
  public int size()
  {
    synchronized (parameters)
      {
        return parameters.size();
      }
  }

  /**
   * Returns the parameter at the specified index.
   */
  public Parameter get(int index)
  {
    synchronized (parameters)
      {
        return(Parameter) parameters.get(index);
      }
  }

  /**
   * Adds a new parameter to the list.
   */
  public void add(Parameter parameter)
  {
    synchronized (parameters)
      {
        parameters.add(parameter);
      }
  }

  /**
   * String form.
   */
  public String toString()
  {
    synchronized (parameters)
      {
        int len = parameters.size();
        if (len == 0)
          {
            return "";
          }
        StringBuffer buffer = new StringBuffer();
        buffer.append(parameters.get(0));
        for (int i = 1; i < len; i++)
          {
            buffer.append(' ');
            buffer.append(parameters.get(i));
          }
        return buffer.toString();
      }
  }

}

