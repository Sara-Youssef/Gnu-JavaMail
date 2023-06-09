/*
 * FolderEvent.java
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

package javax.mail.event;

import javax.mail.Folder;

/**
 * A folder event.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 * @version 1.5
 */
public class FolderEvent
  extends MailEvent
{

  /**
   * The folder was created.
   */
  public static final int CREATED = 1;

  /**
   * The folder was deleted.
   */
  public static final int DELETED = 2;

  /**
   * The folder was renamed.
   */
  public static final int RENAMED = 3;

  /**
   * The event type.
   */
  protected int type;

  /**
   * The folder the event occurred on.
   */
  protected transient Folder folder;

  /**
   * The folder representing the new name, in the case of a RENAMED event.
   */
  protected transient Folder newFolder;

  /**
   * Constructor.
   * @param source the source
   * @param folder the affected folder
   * @param type the event type (CREATED or DELETED)
   */
  public FolderEvent(Object source, Folder folder, int type)
  {
    this(source, folder, folder, type);
  }

  /**
   * Constructor for RENAMED events.
   * @param source the source
   * @param oldFolder the folder that is renamed
   * @param newFolder the folder that represents the new name
   * @param type the event type (RENAMED)
   */
  public FolderEvent(Object source, Folder oldFolder, Folder newFolder,
                     int type)
  {
    super(source);
    folder = oldFolder;
    this.newFolder = newFolder;
    this.type = type;
  }

  /**
   * Returns the type of this event.
   */
  public int getType()
  {
    return type;
  }

  /**
   * Returns the affected folder.
   * @see #getNewFolder
   */
  public Folder getFolder()
  {
    return folder;
  }

  /**
   * Returns the folder representing the new name, in the case of a RENAMED
   * event.
   * @see #getFolder
   */
  public Folder getNewFolder()
  {
    return newFolder;
  }

  /**
   * Invokes the appropriate listener method.
   */
  public void dispatch(Object listener)
  {
    FolderListener l = (FolderListener) listener;
    switch (type)
    {
      case CREATED:
        l.folderCreated(this);
        break;
      case DELETED:
        l.folderDeleted(this);
        break;
      case RENAMED:
        l.folderRenamed(this);
        break;
    }
  }

}

