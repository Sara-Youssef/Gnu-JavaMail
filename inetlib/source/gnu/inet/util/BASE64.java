/*
 * BASE64.java
 * Copyright (C) 2003, 2010 The Free Software Foundation
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

package gnu.inet.util;

/**
 * Encodes and decodes text according to the BASE64 encoding.
 *
 * @author <a href="mailto:dog@gnu.org">Chris Burdess</a>
 */
public final class BASE64
{

  private static final byte[] src = {
    0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a,
    0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54,
    0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x61, 0x62, 0x63, 0x64,
    0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e,
    0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78,
    0x79, 0x7a, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
    0x38, 0x39, 0x2b, 0x2f
  };

  private static final byte[] dst;
  static
  {
    dst = new byte[0x100];
    for (int i = 0x0; i < 0xff; i++)
      {
        dst[i] = -1;
      }
    for (int i = 0; i < src.length; i++)
      {
        dst[src[i]] = (byte) i;
      }
  }

  private BASE64()
  {
  }

  /**
   * Encode the specified byte array using the BASE64 algorithm.
   *
   * @param bs the source byte array
   */
  public static byte[] encode(byte[] bs)
  {
    int si = 0, ti = 0;         // source/target array indices
    /* target byte array */
    byte[] bt = new byte[((bs.length + 2 - ((bs.length + 2) % 3)) * 4) / 3];
    for (; si < bs.length; si += 3)
      {
        int buflen = bs.length - si;
        if (buflen == 1)
          {
            byte b = bs[si];
            int i = 0;
            boolean flag = false;
            bt[ti++] = src[b >>> 2 & 0x3f];
            bt[ti++] = src[(b << 4 & 0x30) + (i >>> 4 & 0xf)];
          }
        else if (buflen == 2)
          {
            byte b1 = bs[si], b2 = bs[si + 1];
            int i = 0;
            bt[ti++] = src[b1 >>> 2 & 0x3f];
            bt[ti++] = src[(b1 << 4 & 0x30) + (b2 >>> 4 & 0xf)];
            bt[ti++] = src[(b2 << 2 & 0x3c) + (i >>> 6 & 0x3)];
          }
        else
          {
            byte b1 = bs[si], b2 = bs[si + 1], b3 = bs[si + 2];
            bt[ti++] = src[b1 >>> 2 & 0x3f];
            bt[ti++] = src[(b1 << 4 & 0x30) + (b2 >>> 4 & 0xf)];
            bt[ti++] = src[(b2 << 2 & 0x3c) + (b3 >>> 6 & 0x3)];
            bt[ti++] = src[b3 & 0x3f];
          }
      }
    while (ti < bt.length)
      {
        bt[ti++] = 0x3d;
      }
    return bt;
  }

  /**
   * Decode the specified byte array using the BASE64 algorithm.
   *
   * @param bs the source byte array
   */
  public static byte[] decode(byte[] bs)
  {
    int padding = 0;
    while (bs.length - padding > 0 && bs[bs.length - padding - 1] == 0x3d)
      {
        padding++;
      }
    int srclen = bs.length - padding; /* strip padding characters */
    byte[] buffer = new byte[(bs.length / 4) * 3 - padding]; /* target array */
    int buflen = 0;
    int si = 0;
    int len = srclen - si;
    while (len > 0)
      {
        byte b0 = dst[bs[si++] & 0xff];
        byte b2 = dst[bs[si++] & 0xff];
        buffer[buflen++] = (byte) (b0 << 2 & 0xfc | b2 >>> 4 & 0x3);
        if (len > 2)
          {
            b0 = b2;
            b2 = dst[bs[si++] & 0xff];
            buffer[buflen++] = (byte) (b0 << 4 & 0xf0 | b2 >>> 2 & 0xf);
            if (len > 3)
              {
                b0 = b2;
                b2 = dst[bs[si++] & 0xff];
                buffer[buflen++] = (byte) (b0 << 6 & 0xc0 | b2 & 0x3f);
              }
          }
        len = srclen - si;
      }
    return buffer;
  }

  public static void main(String[] args)
  {
    boolean decode = false;
    for (int i = 0; i < args.length; i++)
      {
        if (args[i].equals("-d"))
          {
            decode = true;
          }
        else
          {
            try
              {
                byte[] in = args[i].getBytes("US-ASCII");
                byte[] out = decode ? decode(in) : encode(in);
                System.out.println(args[i] + " = " +
                                   new String(out, "US-ASCII"));
              }
            catch (java.io.UnsupportedEncodingException e)
              {
                e.printStackTrace(System.err);
              }
          }
      }
  }

}
