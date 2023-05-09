/*
 * ENVELOPE.java
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An ENVELOPE data item in a FETCH response.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class ENVELOPE
  extends FetchDataItem
{

  public static class Address
  {

    private final String personal;
    private final String address;

    Address(String personal, String address)
    {
      this.personal = personal;
      this.address = address;
    }

    public String getPersonal()
    {
      return personal;
    }

    public String getAddress()
    {
      return address;
    }

  }

  private final String date;
  private final String subject;
  private final List<Address> from;
  private final List<Address> sender;
  private final List<Address> replyTo;
  private final List<Address> to;
  private final List<Address> cc;
  private final List<Address> bcc;
  private final String inReplyTo;
  private final String messageId;

  ENVELOPE(String date,
           String subject,
           List<Address> from,
           List<Address> sender,
           List<Address> replyTo,
           List<Address> to,
           List<Address> cc,
           List<Address> bcc,
           String inReplyTo,
           String messageId)
  {
    this.date = date;
    this.subject = subject;
    this.from = from;
    this.sender = sender;
    this.replyTo = replyTo;
    this.to = to;
    this.cc = cc;
    this.bcc = bcc;
    this.inReplyTo = inReplyTo;
    this.messageId = messageId;
  }

  public String getDate()
  {
    return date;
  }

  public String getSubject()
  {
    return subject;
  }

  public List<Address> getFrom()
  {
    return from;
  }

  public List<Address> getSender()
  {
    return sender;
  }

  public List<Address> getReplyTo()
  {
    return replyTo;
  }

  public List<Address> getTo()
  {
    return to;
  }

  public List<Address> getCc()
  {
    return cc;
  }

  public List<Address> getBcc()
  {
    return bcc;
  }

  public String getInReplyTo()
  {
    return inReplyTo;
  }

  public String getMessageId()
  {
    return messageId;
  }

}
