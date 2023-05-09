/*
 * RequestEvent.java
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

package gnu.inet.http.event;

import java.util.EventObject;
import gnu.inet.http.Request;

/**
 * A request event.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class RequestEvent
  extends EventObject
{

  /**
   * The request created event type.
   */
  public static final int REQUEST_CREATED = 0;

  /**
   * The request sending event type.
   */
  public static final int REQUEST_SENDING = 1;

  /**
   * The request sent event type.
   */
  public static final int REQUEST_SENT = 2;

  /**
   * The type of this event.
   */
  protected int type;

  /**
   * The request associated with this event.
   */
  protected Request request;

  /**
   * Constructs a request event with the specified source, type, and request.
   */
  public RequestEvent(Object source, int type, Request request)
  {
    super(source);
    this.type = type;
    this.request = request;
  }

  /**
   * Returns the type of this event.
   * @see #type
   */
  public int getType()
  {
    return type;
  }

  /**
   * Returns the request associated with this event.
   */
  public Request getRequest()
  {
    return request;
  }

}

