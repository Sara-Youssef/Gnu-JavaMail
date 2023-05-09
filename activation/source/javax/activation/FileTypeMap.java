/*
 * FileTypeMap.java
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
package javax.activation;

import java.io.File;

/**
 * Classifier for the MIME content type of files.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public abstract class FileTypeMap
{

  /* Class scope */

  private static FileTypeMap defaultMap;

  /**
   * Returns the system default file type map.
   * If one has not been set, this returns a MimetypesFileTypeMap.
   */
  public static FileTypeMap getDefaultFileTypeMap()
  {
    if (defaultMap == null)
      {
        defaultMap = new MimetypesFileTypeMap();
      }
    return defaultMap;
  }

  /**
   * Sets the default file type map.
   * @param map the new file type map
   */
  public static void setDefaultFileTypeMap(FileTypeMap map)
  {
    SecurityManager security = System.getSecurityManager();
    if (security != null)
      {
        try
          {
            security.checkSetFactory();
          }
        catch (SecurityException e)
          {
            if (map != null && FileTypeMap.class.getClassLoader() !=
                map.getClass().getClassLoader())
              {
                throw e;
              }
          }
      }
    defaultMap = map;
  }

  /* Instance scope */

  /**
   * Returns the content type of the specified file.
   * @param file the file to classify
   */
  public abstract String getContentType(File file);

  /**
   * Returns the content type of the specified file path.
   * @param filename the path of the file to classify
   */
  public abstract String getContentType(String filename);

}

