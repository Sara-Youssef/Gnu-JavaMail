/*
 * DataContentHandler.java
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Provider that can convert streams to objects and vice versa.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
public interface DataContentHandler
{

  /**
   * Returns a list of the flavors that data can be provided in, ordered
   * by preference.
   */
  DataFlavor[] getTransferDataFlavors();

  /**
   * Returns an object representing the data to be transferred.
   * @param df the flavor representing the requested type
   * @param ds the data source of the data to be converted
   */
  Object getTransferData(DataFlavor df, DataSource ds)
    throws UnsupportedFlavorException, IOException;

  /**
   * Returns an object representing the data in its most preferred form.
   * @param ds the data source of the data to be converted
   */
  Object getContent(DataSource ds)
    throws IOException;

  /**
   * Writes the object as a stream of bytes.
   * @param obj the object to convert
   * @param mimeType the MIME type of the stream
   * @param os the byte stream
   */
  void writeTo(Object obj, String mimeType, OutputStream os)
    throws IOException;

}

