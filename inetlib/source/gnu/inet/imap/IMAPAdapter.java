/*
 * IMAPAdapter.java
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

import java.util.List;
import java.util.Map;

/**
 * Adapter for an IMAP response callback.
 * This is provided for convenience of implementation.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class IMAPAdapter
  implements IMAPCallback
{

  private IMAPCallback proxy;

  public IMAPAdapter()
  {
  }

  public IMAPAdapter(IMAPCallback proxy)
  {
    this.proxy = proxy;
  }

  public void alert(String message)
  {
    if (proxy != null)
      {
        proxy.alert(message);
      }
  }

  public void capability(List<String> capabilities)
  {
    if (proxy != null)
      {
        proxy.capability(capabilities);
      }
  }

  public void exists(int messages)
  {
    if (proxy != null)
      {
        proxy.exists(messages);
      }
  }

  public void recent(int messages)
  {
    if (proxy != null)
      {
        proxy.recent(messages);
      }
  }

  public void expunge(int message)
  {
    if (proxy != null)
      {
        proxy.expunge(message);
      }
  }

  public void fetch(int message, List<FetchDataItem> data)
  {
    if (proxy != null)
      {
        proxy.fetch(message, data);
      }
  }

  public void flags(List<String> flags)
  {
    if (proxy != null)
      {
        proxy.flags(flags);
      }
  }

  public void permanentflags(List<String> flags)
  {
    if (proxy != null)
      {
        proxy.permanentflags(flags);
      }
  }

  public void firstUnseen(int message)
  {
    if (proxy != null)
      {
        proxy.firstUnseen(message);
      }
  }

  public void unseen(int messages)
  {
    if (proxy != null)
      {
        proxy.unseen(messages);
      }
  }

  public void uidvalidity(long uidvalidity)
  {
    if (proxy != null)
      {
        proxy.uidvalidity(uidvalidity);
      }
  }

  public void uidnext(long uid)
  {
    if (proxy != null)
      {
        proxy.uidnext(uid);
      }
  }

  public void readWrite()
  {
    if (proxy != null)
      {
        proxy.readWrite();
      }
  }

  public void readOnly()
  {
    if (proxy != null)
      {
        proxy.readOnly();
      }
  }

  public void tryCreate()
  {
    if (proxy != null)
      {
        proxy.tryCreate();
      }
  }

  public void list(List<String> attributes, String delimiter, String mailbox)
  {
    if (proxy != null)
      {
        proxy.list(attributes, delimiter, mailbox);
      }
  }

  public void search(List<Integer> results)
  {
    if (proxy != null)
      {
        proxy.search(results);
      }
  }

  public void namespace(List<Namespace> personal,
                        List<Namespace> otherUsers,
                        List<Namespace> shared)
  {
    if (proxy != null)
      {
        proxy.namespace(personal, otherUsers, shared);
      }
  }

  public void quota(String quotaRoot, Map<String,Integer> currentUsage,
                    Map<String,Integer> limit)
  {
    if (proxy != null)
      {
        proxy.quota(quotaRoot, currentUsage, limit);
      }
  }

  public void quotaroot(String mailbox, List<String> quotaRoots)
  {
    if (proxy != null)
      {
        proxy.quotaroot(mailbox, quotaRoots);
      }
  }

  public void acl(String mailbox, Map<String,String> rights)
  {
    if (proxy != null)
      {
        proxy.acl(mailbox, rights);
      }
  }

  public void listrights(String mailbox, String identifier, String required,
                         List<String> optional)
  {
    if (proxy != null)
      {
        proxy.listrights(mailbox, identifier, required, optional);
      }
  }

  public void myrights(String mailbox, String rights)
  {
    if (proxy != null)
      {
        proxy.myrights(mailbox, rights);
      }
  }

  public void appenduid(long uidvalidity, long uid)
  {
    if (proxy != null)
      {
        proxy.appenduid(uidvalidity, uid);
      }
  }

  public void copyuid(long uidvalidity, UIDSet source, UIDSet destination)
  {
    if (proxy != null)
      {
        proxy.copyuid(uidvalidity, source, destination);
      }
  }

  public void uidnotsticky()
  {
    if (proxy != null)
      {
        proxy.uidnotsticky();
      }
  }

}
