/*
 * UIDSet.java
 * Copyright (C) 2013 The Free Software Foundation
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

package gnu.inet.imap;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A set of UIDs. These are long values.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @see RFC 2359
 */
public class UIDSet
  extends AbstractSet
{

  private final List elements;
  private int count;

  public UIDSet()
  {
    elements = new ArrayList();
  }

  UIDSet(String s)
  {
    count = 0;
    elements = new ArrayList();
    StringTokenizer st = new StringTokenizer(s, ",");
    while (st.hasMoreTokens())
      {
        String token = st.nextToken();
        int ci = token.indexOf(':');
        if (ci == -1)
          {
            elements.add(new Long(token));
            count++;
          }
        else
          {
            long start = Long.parseLong(token.substring(0, ci));
            long end = Long.parseLong(token.substring(ci + 1));
            elements.add(new Range(start, end));
            count += (end - start) + 1;
          }
      }
  }

  public int size()
  {
    return count;
  }

  public boolean isEmpty()
  {
    return count == 0;
  }

  public void clear()
  {
    throw new UnsupportedOperationException();
  }

  public boolean contains(Object o)
  {
    int len = elements.size();
    for (int i = 0; i < len; i++)
      {
        Object e = elements.get(i);
        if (e instanceof Range)
          {
            if (((Range) e).contains(o))
              {
                return true;
              }
          }
        else
          {
            if (e.equals(o))
              {
                return true;
              }
          }
      }
    return false;
  }

  public boolean add(Object o)
  {
    long src = ((Long) o).longValue();
    if (src < 1L)
      {
        return false;
      }
    int len = elements.size();
    for (int i = 0; i < len; i++)
      {
        Object e = elements.get(i);
        if (e instanceof Long)
          {
            long dst = ((Long) e).longValue();
            if (src == dst)
              {
                return false;
              }
            else if (src == dst - 1L) // replace with range
              {
                elements.set(i, new Range(src, dst));
                coalesceAt(i);
                count++;
                return true;
              }
            else if (src < dst) // insert before
              {
                elements.add(i, o);
                coalesceAt(i);
                count++;
                return true;
              }
          }
        else
          {
            Range range = (Range) e;
            if (range.contains(src))
              {
                return false;
              }
            else if (src == range.start - 1L)
              {
                range.start = src;
                coalesceAt(i);
                count++;
                return true;
              }
            else if (src < range.start)
              {
                elements.add(i, o);
                coalesceAt(i);
                count++;
                return true;
              }
          }
      }
    elements.add(o);
    coalesceAt(len);
    count++;
    return true;
  }

  /**
   * Coalesce object at index with its predecessor.
   */
  private void coalesceAt(int index)
  {
    if (index > 0)
      {
        Object o1 = elements.get(index - 1);
        Object o2 = elements.get(index);
        if (o1 instanceof Long)
          {
            long l1 = ((Long) o1).longValue();
            if (o2 instanceof Long)
              {
                long l2 = ((Long) o2).longValue();
                if (l1 == l2 - 1L)
                  {
                    elements.set(index - 1, new Range(l1, l2));
                    elements.remove(index);
                  }
              }
            else
              {
                Range r2 = (Range) o2;
                if (l1 == r2.start - 1L)
                  {
                    r2.start = l1;
                    elements.remove(index - 1);
                  }
              }
          }
        else
          {
            Range r1 = (Range) o1;
            if (o2 instanceof Long)
              {
                long l2 = ((Long) o2).longValue();
                if (r1.end == l2 - 1L)
                  {
                    r1.end = l2;
                    elements.remove(index);
                  }
              }
            else
              {
                Range r2 = (Range) o2;
                if (r1.end == r2.start - 1L)
                  {
                    r1.end = r2.end;
                    elements.remove(index);
                  }
              }
          }
      }
  }

  public boolean remove(Object o)
  {
    long src = ((Long) o).longValue();
    int len = elements.size();
    for (int i = 0; i < len; i++)
      {
        Object e = elements.get(i);
        if (e instanceof Long)
          {
            if (len == 1)
              {
                return false; // can't remove last element
              }
            long dst = ((Long) e).longValue();
            if (src == dst)
              {
                elements.remove(i);
                count--;
                return true;
              }
          }
        else
          {
            Range range = (Range) e;
            if (range.start == src)
              {
                range.start++;
                if (range.start == range.end)
                  {
                    elements.set(i, Long.valueOf(range.start));
                  }
                count--;
                return true;
              }
            else if (range.end == src)
              {
                range.end--;
                if (range.start == range.end)
                  {
                    elements.set(i, Long.valueOf(range.start));
                  }
                count--;
                return true;
              }
            else if (range.contains(src)) // split range
              {
                Object o1 = (range.start == src - 1L) ?
                  Long.valueOf(range.start) :
                  new Range(range.start, src - 1L);
                Object o2 = (range.end == src + 1L) ?
                  Long.valueOf(range.end) :
                  new Range(src + 1L, range.end);
                elements.set(i, o2);
                elements.add(i, o1);
                count--;
                return true;
              }
          }
      }
    return false;
  }

  public boolean equals(Object o)
  {
    return (o instanceof UIDSet && o.toString().equals(toString()));
  }

  public int hashCode()
  {
    return toString().hashCode();
  }

  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    int len = elements.size();
    for (int i = 0; i < len; i++)
      {
        if (i > 0)
          {
            buf.append(',');
          }
        buf.append(elements.get(i));
      }
    return buf.toString();
  }

  public Iterator iterator()
  {
    return new UIDSetIterator(elements.iterator());
  }

  public Object[] toArray()
  {
    Long[] l = new Long[count];
    return toArray(l);
  }

  public Object[] toArray(Object[] a)
  {
    if (a == null || !(a instanceof Long[]) || a.length < count)
      {
        a = new Long[count];
      }
    Iterator li = iterator();
    for (int i = 0; i < count; i++)
      {
        a[i] = li.next();
      }
    return a;
  }

  private static class Range
  {

    private long start;
    private long end;

    Range(long start, long end)
    {
      this.start = start;
      this.end = end;
    }

    public boolean contains(Object o)
    {
      long l = ((Long) o).longValue();
      return l >= start && l <= end;
    }

    public String toString()
    {
      StringBuilder buf = new StringBuilder();
      buf.append(start);
      buf.append(':');
      buf.append(end);
      return buf.toString();
    }

    public Iterator iterator()
    {
      return new RangeIterator(start, end);
    }

    private static class RangeIterator
      implements Iterator
    {

      private long index;
      private long end;

      RangeIterator(long start, long end) {
        index = start;
        this.end = end;
      }

      public Object next()
      {
        return Long.valueOf(index++);
      }

      public boolean hasNext()
      {
        return (index <= end);
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }

    }

  }

  private static class UIDSetIterator
    implements Iterator
  {

    private Iterator ei;
    private Iterator ri;

    UIDSetIterator(Iterator ei) {
      this.ei = ei;
    }

    public Object next()
    {
      if (ri != null)
        {
          Object ret = ri.next();
          if (!ri.hasNext())
            {
              ri = null;
            }
          return ret;
        }
      Object o = ei.next();
      if (o instanceof Range)
        {
          ri = ((Range) o).iterator();
          return next();
        }
      return o;
    }

    public boolean hasNext()
    {
      if (ri != null)
        {
          return ri.hasNext();
        }
      return ei.hasNext();
    }

    public void remove()
    {
      throw new UnsupportedOperationException();
    }

  }

}
