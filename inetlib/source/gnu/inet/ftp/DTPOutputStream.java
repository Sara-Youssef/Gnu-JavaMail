/*
 * DTPOutputStream.java
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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that notifies a DTP on end of stream.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
abstract class DTPOutputStream
  extends FilterOutputStream
{

  DTP dtp;
  boolean transferComplete;

  /**
   * Constructor.
   * @param dtp the controlling data transfer process
   * @param out the socket output stream
   */
  DTPOutputStream(DTP dtp, OutputStream out)
  {
    super(out);
    this.dtp = dtp;
    transferComplete = false;
  }

  /**
   * Tells this stream whether transfer has completed or not.
   * @param flag true if the process has completed, false otherwise
   */
  void setTransferComplete(boolean flag)
  {
    transferComplete = flag;
  }

  /**
   * Notifies the controlling DTP that this stream has been terminated.
   */
  public void close()
    throws IOException
  {
    dtp.transferComplete();
  }

}

