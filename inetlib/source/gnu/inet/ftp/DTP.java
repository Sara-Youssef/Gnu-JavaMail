/*
 * DTP.java
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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An FTP data transfer process.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
interface DTP
{

  /**
   * Returns an input stream from which a remote file can be read.
   */
  InputStream getInputStream()
    throws IOException;

  /**
   * Returns an output stream to which a local file can be written for
   * upload.
   */
  OutputStream getOutputStream()
    throws IOException;

  /**
   * Sets the transfer mode to be used with this DTP.
   */
  void setTransferMode(int mode);

  /**
   * Marks this DTP completed.
   * When the current transfer has finished, any resources will be released.
   */
  void complete();

  /**
   * Aborts any current transfer and releases all resources held by this
   * DTP.
   * @return true if a transfer was interrupted, false otherwise
   */
  boolean abort();

  /**
   * Used to notify the DTP that its current transfer is complete.
   * This occurs either when end-of-stream is reached or a 226 response is
   * received.
   */
  void transferComplete();

}

