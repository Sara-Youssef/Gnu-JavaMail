/*
 * FetchProfile.java
 * Copyright (C) 2002, 2013 The Free Software Foundation
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

package javax.mail;

import java.util.ArrayList;

/**
 * Specification of the facets of a message that are to be preloaded from
 * the server.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.5
 */
public class FetchProfile
{

  /**
   * Base class of all the facets of a message that can be fetched.
   */
  public static class Item
  {

    /**
     * The common attributes of a message, e.g.:
     * From, To, Cc, Bcc, ReplyTo, Subject and Date.
     * Further items may be included as required.
     */
    public static final Item ENVELOPE = new Item("ENVELOPE");

    /**
     * The message content metadata, e.g.:
     * ContentType, ContentDisposition, ContentDescription, Size and LineCount.
     * Further items may be included as required.
     */
    public static final Item CONTENT_INFO = new Item("CONTENT_INFO");

    /**
     * The message flags.
     */
    public static final Item FLAGS = new Item("FLAGS");

    /**
     * The size of the message.
     * @since JavaMail 1.5
     */
    public static final Item SIZE = new Item("SIZE");

    private String name;

    protected Item(String name)
    {
      this.name = name;
    }

  }


  private ArrayList items = null;
  private ArrayList headers = null;

  /**
   * Create an empty fetch profile.
   */
  public FetchProfile()
  {
  }

  /**
   * Add the given item.
   */
  public void add(Item item)
  {
    if (items == null)
      {
        items = new ArrayList();
      }
    synchronized (items)
      {
        items.add(item);
      }
  }

  /**
   * Add the specified header name.
   */
  public void add(String header)
  {
    if (headers == null)
      {
        headers = new ArrayList();
      }
    synchronized (headers)
      {
        headers.add(header);
      }
  }

  /**
   * Indicates whether the fetch profile contains the specified item.
   */
  public boolean contains(Item item)
  {
    return (items != null && items.contains(item));
  }

  /**
   * Indicates whether the fetch profile contains the given header name.
   */
  public boolean contains(String header)
  {
    return (headers!=null && headers.contains(header));
  }

  /**
   * Get the items in this profile.
   */
  public Item[] getItems()
  {
    if (items == null)
      {
        return new Item[0];
      }
    synchronized (items)
      {
        Item[] i = new Item[items.size()];
        items.toArray(i);
        return i;
      }
  }

  /**
   * Get the names of the header fields in this profile.
   */
  public String[] getHeaderNames()
  {
    if (headers == null)
      {
        return new String[0];
      }
    synchronized (headers)
      {
        String[] h = new String[headers.size()];
        headers.toArray(h);
        return h;
      }
  }

}
