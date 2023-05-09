/*
 * DTPInputStream.java
 * Copyright (C) 2003 The Free Software Foundation
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

package gnu.inet.ftp;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * An input stream that notifies a DTP on completion.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
abstract class DTPInputStream
  extends FilterInputStream
{

  DTP dtp;
  boolean transferComplete;

  /**
   * Constructor.
   * @param dtp the controlling data transfer process
   * @param in the underlying socket stream
   */
  DTPInputStream (DTP dtp, InputStream in)
  {
    super(in);
    this.dtp = dtp;
    transferComplete = false;
  }

  /**
   * Marks this input stream complete.
   * This is called by the DTP.
   */
  void setTransferComplete(boolean flag)
  {
    transferComplete = flag;
  }

  /**
   * Notifies the controlling DTP that this stream has completed transfer.
   */
  public void close()
    throws IOException
  {
    dtp.transferComplete();
  }

}

