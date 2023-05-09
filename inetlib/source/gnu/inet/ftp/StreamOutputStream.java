/*
 * StreamOutputStream.java
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
 * A DTP output stream that implements the FTP stream transfer mode.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
class StreamOutputStream
  extends DTPOutputStream
{

  StreamOutputStream(DTP dtp, OutputStream out)
  {
    super(dtp, out);
  }

  public void write(int c)
    throws IOException
  {
    if (transferComplete)
      {
        return;
      }
    out.write(c);
  }

  public void write(byte[] b)
    throws IOException
  {
    write(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len)
    throws IOException
  {
    if (transferComplete)
      {
        return;
      }
    out.write(b, off, len);
  }

}

