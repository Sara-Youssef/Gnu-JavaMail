/*
 * SentDateTerm.java
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

package javax.mail.search;

import java.util.Date;
import javax.mail.Message;

/**
 * A comparison of message sent dates.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.5
 */
public final class SentDateTerm
  extends DateTerm
{

  /**
   * Constructor.
   * @param comparison the comparison operator
   * @param date the date for comparison
   */
  public SentDateTerm(int comparison, Date date)
  {
    super(comparison, date);
  }

  /**
   * Returns true only if the given message's sent date matches the
   * specified date using the specified operator.
   */
  public boolean match(Message msg)
  {
    try
      {
        Date d = msg.getSentDate();
        if (d != null)
          {
            return super.match(d);
          }
      }
    catch (Exception e)
      {
      }
    return false;
  }

  public boolean equals(Object other)
  {
    return (other instanceof SentDateTerm && super.equals(other));
  }

}

