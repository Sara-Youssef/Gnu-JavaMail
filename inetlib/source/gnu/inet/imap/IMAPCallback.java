/*
 * IMAPCallback.java
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
 * An IMAP response callback.
 * @version 1.2
 * @since 1.2
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public interface IMAPCallback
{

  /**
   * Notification of a user alert.
   * This should be displayed to the user somehow.
   * @param message the message text
   */
  void alert(String message);

  /**
   * Notification of capabilities change.
   * @param capabilities the new capabilities
   */
  void capability(List<String> capabilities);

  /**
   * Notification of the number of messages in a mailbox.
   * @param messages the number of messages
   */
  void exists(int messages);

  /**
   * Notification of the number of messages with the recent flag set.
   * @param messages the number of messages
   */
  void recent(int messages);

  /**
   * Notification that a given messages has been permanently deleted.
   * @param message the message number
   */
  void expunge(int message);

  /**
   * Notification of data specific to a message
   * @param message the message number
   * @param data the message data
   */
  void fetch(int message, List<FetchDataItem> data);

  /**
   * Notification of the flags applicable for the associated mailbox.
   */
  void flags(List<String> flags);

  /**
   * Notification of the permanent flags applicable for the associated
   * mailbox. These are flags that the user can change permanently. If not
   * present, all flags can be changed permanently.
   */
  void permanentflags(List<String> flags);

  /**
   * The message sequence number of the first unseen message in the mailbox.
   * If not present, a SEARCH command must be issued to discover this value.
   */
  void firstUnseen(int message);

  /**
   * The number of messages which do not have the \Seen flag set.
   */
  void unseen(int messages);

  /**
   * The unique identifier validity value. If not present, the server does
   * not support UIDs.
   */
  void uidvalidity(long uidvalidity);

  /**
   * The next unique identifier value.
   */
  void uidnext(long uid);

  /**
   * Indicates that the mailbox was opened read-write.
   */
  void readWrite();

  /**
   * Indicates that the mailbox was opened read-only.
   */
  void readOnly();

  /**
   * Indicates that the mailbox does not exist yet but could be created.
   */
  void tryCreate();

  /**
   * Notification of a list entry.
   * @param attributes the mailbox attributes
   * @param delimiter the mailbox hierarchy delimiter
   * @param mailbox the mailbox name
   */
  void list(List<String> attributes, String delimiter, String mailbox);

  /**
   * Notification of a search result.
   * @param results the search results. For an ordinary search these are
   * message sequence numbers; for a UID search they are UIDs.
   */
  void search(List<Integer> results);

  /**
   * Notification of a namespace result.
   * @param personal the server's Personal namespaces
   * @param otherUsers the Other Users' namespaces
   * @param shared the Shared namespaces
   */
  void namespace(List<Namespace> personal,
                 List<Namespace> otherUsers,
                 List<Namespace> shared);

  /**
   * Notification of a quota.
   * @param quotaRoot the quota root
   * @param currentUsage current usage by resource
   * @param limit limits of each resource
   * @see RFC 2087
   */
  void quota(String quotaRoot, Map<String,Integer> currentUsage,
             Map<String,Integer> limit);

  /**
   * Notification of the quota roots for a mailbox.
   * @param mailbox the mailbox
   * @param quotaRoots the quota roots for the mailbox
   * @see RFC 2087
   */
  void quotaroot(String mailbox, List<String> quotaRoots);

  /**
   * Notification of access control rights.
   * This occurs in response to a GETACL command.
   * @param mailbox the mailbox name
   * @param rights the identifier-rights pairs
   * @see RFC 4314
   */
  void acl(String mailbox, Map<String,String> rights);

  /**
   * Notification of a list of rights.
   * This occurs in response to a LISTRIGHTS command.
   * @param mailbox the mailbox name
   * @param identifier the identifier
   * @param required the required rights for this identifier
   * @param rights the list of optional rights for this identifier
   * @see RFC 4314
   */
  void listrights(String mailbox, String identifier, String required,
                  List<String> optional);

  /**
   * Notification of the user's rights with respect to the mailbox.
   * This occurs in response to a MYRIGHTS command.
   * @param mailbox the mailbox name
   * @param rights the rights for the current user
   * @see RFC 4314
   */
  void myrights(String mailbox, String rights);

  /**
   * Indicates that the message has been appended to the destination
   * mailbox.
   * @param uidvalidity uidvalidity of the destination mailbox
   * @param uid uid of the appended message in the destination mailbox
   * @see RFC 2359
   */
  void appenduid(long uidvalidity, long uid);

  /**
   * Indicates that the message(s) have been copied to the destination
   * mailbox with the stated UID(s).
   * @param uidvalidity uidvalidity of the destination mailbox
   * @param source UID(s) of the message(s) in the source mailbox
   * @param destination UID(s) of the message(s) in the destination mailbox
   * @see RFC 2359
   */
  void copyuid(long uidvalidity, UIDSet source, UIDSet destination);

  /**
   * Indicates that the server does not support persistent UIDs, and APPEND
   * and COPY will not return APPENDUID or COPYUID responses.
   * @see RFC 2359
   */
  void uidnotsticky();

}
