/*
 * HTTPDateFormat.java
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

import java.io.PrintStream;
import java.text.*;
import java.util.*;

/**
 * HTTP date formatter and parser.
 * Formats dates according to RFC 822 (updated by RFC 1123).
 * Parses dates according to the above, <i>or</i> RFC 1036, <i>or</i> the
 * ANSI C <code>asctime()</code> format.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public class HTTPDateFormat
  extends DateFormat
{

  static final String[] DAYS_OF_WEEK = {
    null, "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
  };

  static final String[] MONTHS = {
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  };

  public HTTPDateFormat()
  {
    calendar = new GregorianCalendar(TimeZone.getTimeZone ("GMT"));
    numberFormat = new DecimalFormat();
  }

  /**
   * Appends the textual value for the specified field to the given string
   * buffer. This method should be avoided, use <code>format(Date)</code>
   * instead.
   * @param date the Date object
   * @param buf the buffer to append to
   * @param field the current field position
   * @return the modified buffer
   */
  public StringBuffer format(Date date, StringBuffer buf,
                             FieldPosition field)
  {
    calendar.clear();
    calendar.setTime(date);
    buf.setLength(0);

    // Day of week
    buf.append(DAYS_OF_WEEK[calendar.get(Calendar.DAY_OF_WEEK)]);
    buf.append(',');
    buf.append(' ');

    // Day of month
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    buf.append(Character.forDigit(day / 10, 10));
    buf.append(Character.forDigit(day % 10, 10));
    buf.append(' ');

    // Month
    buf.append(MONTHS[calendar.get(Calendar.MONTH)]);
    buf.append(' ');

    // Year
    int year = calendar.get(Calendar.YEAR);
    if (year < 1000)
      {
        buf.append('0');
        if (year < 100)
          {
            buf.append('0');
            if (year < 10)
              {
                buf.append('0');
              }
          }
      }
    buf.append(Integer.toString(year));
    buf.append(' ');

    // Hour
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    buf.append(Character.forDigit(hour / 10, 10));
    buf.append(Character.forDigit(hour % 10, 10));
    buf.append(':');

    // Minute
    int minute = calendar.get(Calendar.MINUTE);
    buf.append(Character.forDigit(minute / 10, 10));
    buf.append(Character.forDigit(minute % 10, 10));
    buf.append(':');

    // Second
    int second = calendar.get(Calendar.SECOND);
    buf.append(Character.forDigit(second / 10, 10));
    buf.append(Character.forDigit(second % 10, 10));
    buf.append(' ');

    // Timezone
    // Get time offset in minutes
    int zoneOffset =(calendar.get(Calendar.ZONE_OFFSET) +
                     calendar.get(Calendar.DST_OFFSET)) / 60000;

    // Apply + or - appropriately
    if (zoneOffset < 0)
      {
        zoneOffset = -zoneOffset;
        buf.append('-');
      }
    else
      {
        buf.append('+');
      }

    // Set the 2 2-char fields as specified above
    int tzhours = zoneOffset / 60;
    buf.append(Character.forDigit(tzhours / 10, 10));
    buf.append(Character.forDigit(tzhours % 10, 10));
    int tzminutes = zoneOffset % 60;
    buf.append(Character.forDigit(tzminutes / 10, 10));
    buf.append(Character.forDigit(tzminutes % 10, 10));

    field.setBeginIndex(0);
    field.setEndIndex(buf.length());
    return buf;
  }

  /**
   * Parses the given date in the current TimeZone.
   * @param text the formatted date to be parsed
   * @param pos the current parse position
   */
  public Date parse(String text, ParsePosition pos)
  {
    int date, month, year, hour, minute, second;
    String monthText;
    int start = 0, end = -1;
    int len = text.length();
    calendar.clear();
    pos.setIndex(start);
    try
      {
        // Advance to date
        if (Character.isLetter(text.charAt(start)))
          {
            start = skipNonWhitespace(text, start);
          }
        // Determine mode
        switch(start)
          {
          case 3:
            // asctime
            start = skipWhitespace(text, start);
            pos.setIndex(start);
            end = skipNonWhitespace(text, start + 1);
            monthText = text.substring(start, end);
            month = -1;
            for (int i = 0; i < 12; i++)
              {
                if (MONTHS[i].equals(monthText))
                  {
                    month = i;
                    break;
                  }
              }
            if (month == -1)
              {
                pos.setErrorIndex(end);
                return null;
              }
            // Advance to date
            start = skipWhitespace(text, end + 1);
            pos.setIndex(start);
            end = skipNonWhitespace(text, start + 1);
            date = Integer.parseInt(text.substring(start, end));
            // Advance to hour
            start = skipWhitespace(text, end + 1);
            pos.setIndex(start);
            end = skipTo(text, start + 1, ':');
            hour = Integer.parseInt(text.substring(start, end));
            // Advance to minute
            start = end + 1;
            pos.setIndex(start);
            end = skipTo(text, start + 1, ':');
            minute = Integer.parseInt(text.substring(start, end));
            // Advance to second
            start = end + 1;
            pos.setIndex(start);
            end = skipNonWhitespace(text, start + 1);
            second = Integer.parseInt(text.substring(start, end));
            // Advance to year
            start = skipWhitespace(text, end + 1);
            pos.setIndex(start);
            end = skipNonWhitespace(text, start + 1);
            year = Integer.parseInt(text.substring(start, end));
            break;
          case 0:
          case 4:
            // rfc822
            start = skipWhitespace(text, start);
            pos.setIndex(start);
            end = skipNonWhitespace(text, start + 1);
            date = Integer.parseInt(text.substring(start, end));
            // Advance to month
            start = skipWhitespace(text, end + 1);
            pos.setIndex(start);
            end = skipNonWhitespace(text, start + 1);
            monthText = text.substring(start, end);
            month = -1;
            for (int i = 0; i < 12; i++)
              {
                if (MONTHS[i].equals(monthText))
                  {
                    month = i;
                    break;
                  }
              }
            if (month == -1)
              {
                pos.setErrorIndex(end);
                return null;
              }
            // Advance to year
            start = skipWhitespace(text, end + 1);
            pos.setIndex(start);
            end = skipNonWhitespace(text, start + 1);
            year = Integer.parseInt(text.substring(start, end));
            // Advance to hour
            start = skipWhitespace(text, end + 1);
            pos.setIndex(start);
            end = skipTo(text, start + 1, ':');
            hour = Integer.parseInt(text.substring(start, end));
            // Advance to minute
            start = end + 1;
            pos.setIndex(start);
            end = skipTo(text, start + 1, ':');
            minute = Integer.parseInt(text.substring(start, end));
            // Advance to second
            start = end + 1;
            pos.setIndex(start);
            end = start + 1;
            while (end < len && !Character.isWhitespace(text.charAt(end)))
              {
                end++;
              }
            second = Integer.parseInt(text.substring(start, end));
            break;
          default:
            // rfc850(obsolete)
            start = skipWhitespace(text, start);
            pos.setIndex(start);
            end = skipTo(text, start + 1, '-');
            date = Integer.parseInt(text.substring(start, end));
            // Advance to month
            start = end + 1;
            pos.setIndex(start);
            end = skipTo(text, start + 1, '-');
            monthText = text.substring(start, end);
            month = -1;
            for (int i = 0; i < 12; i++)
              {
                if (MONTHS[i].equals(monthText))
                  {
                    month = i;
                    break;
                  }
              }
            if (month == -1)
              {
                pos.setErrorIndex(end);
                return null;
              }
            // Advance to year
            start = end + 1;
            pos.setIndex(start);
            end = skipNonWhitespace(text, start + 1);
            year = 1900 + Integer.parseInt(text.substring(start, end));
            // Advance to hour
            start = skipWhitespace(text, end + 1);
            pos.setIndex(start);
            end = skipTo(text, start + 1, ':');
            hour = Integer.parseInt(text.substring(start, end));
            // Advance to minute
            start = end + 1;
            pos.setIndex(start);
            end = skipTo(text, start + 1, ':');
            minute = Integer.parseInt(text.substring(start, end));
            // Advance to second
            start = end + 1;
            pos.setIndex(start);
            end = start + 1;
            while (end < len && !Character.isWhitespace(text.charAt(end)))
              {
                end++;
              }
            second = Integer.parseInt(text.substring(start, end));
          }

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, date);
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);

        if (end != len)
          {
            // Timezone
            start = skipWhitespace(text, end + 1);
            end = start + 1;
            while (end < len && !Character.isWhitespace(text.charAt(end)))
              {
                end++;
              }
            char pm = text.charAt(start);
            if (Character.isLetter(pm))
              {
                TimeZone tz =
                  TimeZone.getTimeZone(text.substring(start, end));
                calendar.set(Calendar.ZONE_OFFSET, tz.getRawOffset());
              }
            else
              {
                int zoneOffset = 0;
                zoneOffset += 600 * Character.digit(text.charAt(++start), 10);
                zoneOffset += 60 * Character.digit(text.charAt(++start), 10);
                zoneOffset += 10 * Character.digit(text.charAt(++start), 10);
                zoneOffset += Character.digit(text.charAt(++start), 10);
                zoneOffset *= 60000; // minutes -> ms
                if ('-' == pm)
                  {
                    zoneOffset = -zoneOffset;
                  }
                calendar.set(Calendar.ZONE_OFFSET, zoneOffset);
              }
          }
        pos.setIndex(end);

        return calendar.getTime();
      }
    catch (NumberFormatException e)
      {
        pos.setErrorIndex(Math.max(start, end));
      }
    catch (StringIndexOutOfBoundsException e)
      {
        pos.setErrorIndex(Math.max(start, end));
      }
    return null;
  }

  private int skipWhitespace(String text, int pos)
  {
    while(Character.isWhitespace(text.charAt(pos)))
      {
        pos++;
      }
    return pos;
  }

  private int skipNonWhitespace(String text, int pos)
  {
    while(!Character.isWhitespace(text.charAt(pos)))
      {
        pos++;
      }
    return pos;
  }

  private int skipTo(String text, int pos, char c)
  {
    while(text.charAt(pos) != c)
      {
        pos++;
      }
    return pos;
  }

  /**
   * Don't allow setting the calendar.
   */
  public void setCalendar(Calendar newCalendar)
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Don't allow setting the NumberFormat.
   */
  public void setNumberFormat(NumberFormat newNumberFormat)
  {
    throw new UnsupportedOperationException();
  }

}

