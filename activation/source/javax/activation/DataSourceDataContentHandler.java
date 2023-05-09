/*
 * DataSourceDataContentHandler.java
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
 * Data content handler using an existing DCH and a data source.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.1
 */
class DataSourceDataContentHandler
  implements DataContentHandler
{

  private DataSource ds;
  private DataFlavor[] flavors;
  private DataContentHandler dch;

  public DataSourceDataContentHandler(DataContentHandler dch, DataSource ds)
  {
    this.ds = ds;
    this.dch = dch;
  }

  public Object getContent(DataSource ds)
    throws IOException
  {
    if (dch != null)
      {
        return dch.getContent(ds);
      }
    else
      {
        return ds.getInputStream();
      }
  }

  public Object getTransferData(DataFlavor flavor, DataSource ds)
    throws UnsupportedFlavorException, IOException
  {
    if (dch != null)
      {
        return dch.getTransferData(flavor, ds);
      }
    DataFlavor[] tdf = getTransferDataFlavors();
    if (tdf.length > 0 && flavor.equals(tdf[0]))
      {
        return ds.getInputStream();
      }
    else
      {
        throw new UnsupportedFlavorException(flavor);
      }
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
            String mimeType = ds.getContentType();
            flavors = new DataFlavor[1];
            flavors[0] = new ActivationDataFlavor(mimeType, mimeType);
          }
      }
    return flavors;
  }

  public void writeTo(Object obj, String mimeType, OutputStream out)
    throws IOException
  {
    if (dch == null)
      {
        throw new UnsupportedDataTypeException("no DCH for content type " +
                                               ds.getContentType());
      }
    dch.writeTo(obj, mimeType, out);
  }

}

