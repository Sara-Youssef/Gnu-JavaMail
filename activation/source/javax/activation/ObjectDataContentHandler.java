/*
 * ObjectDataContentHandler.java
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
 * Data content handler that uses an existing DCH and reified object.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
class ObjectDataContentHandler
  implements DataContentHandler
{

  private DataContentHandler dch;
  private Object object;
  private String mimeType;
  private DataFlavor[] flavors;

  public ObjectDataContentHandler(DataContentHandler dch, Object object,
                                  String mimeType)
  {
    this.dch = dch;
    this.object = object;
    this.mimeType = mimeType;
  }

  public Object getContent(DataSource ds)
  {
    return object;
  }

  public DataContentHandler getDCH()
  {
    return dch;
  }

  public Object getTransferData(DataFlavor flavor, DataSource ds)
    throws UnsupportedFlavorException, IOException
  {
    if (dch != null)
      {
        return dch.getTransferData(flavor, ds);
      }
    if (flavors == null)
      {
        getTransferDataFlavors();
      }
    if (flavor.equals(flavors[0]))
      {
        return object;
      }
    throw new UnsupportedFlavorException(flavor);
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    if (flavors == null)
      {
        if (dch != null)
          {
            flavors = dch.getTransferDataFlavors();
          }
        else
          {
            flavors = new DataFlavor[1];
            flavors[0] = new ActivationDataFlavor(object.getClass(),
                                                  mimeType, mimeType);
          }
      }
    return flavors;
  }

  public void writeTo(Object object, String mimeType, OutputStream out)
    throws IOException
  {
    if (dch != null)
      {
        dch.writeTo(object, mimeType, out);
      }
    else
      {
        throw new UnsupportedDataTypeException("no object DCH for MIME type " + mimeType);
      }
  }

}

