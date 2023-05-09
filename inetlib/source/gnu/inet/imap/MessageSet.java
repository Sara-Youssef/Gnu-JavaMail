/*
 * MessageSet.java
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
 * A set of message numbers. These are integer values.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class MessageSet
  extends AbstractSet
{

  private final List elements;
  private int count;

  public MessageSet()
  {
    elements = new ArrayList();
  }

  MessageSet(String s)
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
            elements.add(new Integer(token));
            count++;
          }
        else
          {
            int start = Integer.parseInt(token.substring(0, ci));
            int end = Integer.parseInt(token.substring(ci + 1));
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
    int src = ((Integer) o).intValue();
    if (src < 1L)
      {
        return false;
      }
    int len = elements.size();
    for (int i = 0; i < len; i++)
      {
        Object e = elements.get(i);
        if (e instanceof Integer)
          {
            int dst = ((Integer) e).intValue();
            if (src == dst)
              {
                return false;
              }
            else if (src == dst - 1) // replace with range
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
            else if (src == range.start - 1)
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
        if (o1 instanceof Integer)
          {
            int l1 = ((Integer) o1).intValue();
            if (o2 instanceof Integer)
              {
                int l2 = ((Integer) o2).intValue();
                if (l1 == l2 - 1)
                  {
                    elements.set(index - 1, new Range(l1, l2));
                    elements.remove(index);
                  }
              }
            else
              {
                Range r2 = (Range) o2;
                if (l1 == r2.start - 1)
                  {
                    r2.start = l1;
                    elements.remove(index - 1);
                  }
              }
          }
        else
          {
            Range r1 = (Range) o1;
            if (o2 instanceof Integer)
              {
                int l2 = ((Integer) o2).intValue();
                if (r1.end == l2 - 1)
                  {
                    r1.end = l2;
                    elements.remove(index);
                  }
              }
            else
              {
                Range r2 = (Range) o2;
                if (r1.end == r2.start - 1)
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
    int src = ((Integer) o).intValue();
    int len = elements.size();
    if (count == 1)
      {
        return false; // can't remove last element
      }
    for (int i = 0; i < len; i++)
      {
        Object e = elements.get(i);
        if (e instanceof Integer)
          {
            int dst = ((Integer) e).intValue();
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
                    elements.set(i, Integer.valueOf(range.start));
                  }
                count--;
                return true;
              }
            else if (range.end == src)
              {
                range.end--;
                if (range.start == range.end)
                  {
                    elements.set(i, Integer.valueOf(range.start));
                  }
                count--;
                return true;
              }
            else if (range.contains(src)) // split range
              {
                Object o1 = (range.start == src - 1) ?
                  Integer.valueOf(range.start) :
                  new Range(range.start, src - 1);
                Object o2 = (range.end == src + 1) ?
                  Integer.valueOf(range.end) :
                  new Range(src + 1, range.end);
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
    return (o instanceof MessageSet &&
            o.toString().equals(toString()));
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
    return new MessageSetIterator(elements.iterator());
  }

  public Object[] toArray()
  {
    Integer[] l = new Integer[count];
    return toArray(l);
  }

  public Object[] toArray(Object[] a)
  {
    if (a == null || !(a instanceof Integer[]) || a.length < count)
      {
        a = new Integer[count];
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

    int start;
    int end;

    Range(int start, int end)
    {
      this.start = start;
      this.end = end;
    }

    public boolean contains(Object o)
    {
      int l = ((Integer) o).intValue();
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

      private int index;
      private int end;

      RangeIterator(int start, int end) {
        index = start;
        this.end = end;
      }

      public Object next()
      {
        return Integer.valueOf(index++);
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

  private static class MessageSetIterator
    implements Iterator
  {

    private Iterator ei;
    private Iterator ri;

    MessageSetIterator(Iterator ei) {
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
