/*
 * SaslNTLM.java
 * Copyright (C) 2013 The Free Software Foundation
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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

/**
 * SASL mechanism for NTLM.
 * This is based on the
 * <a href='http://davenport.sourceforge.net/ntlm.html'>Davenport WebDAV
 * gateway documentation for NTLM</a>. It is pre-alpha quality and will
 * probably not work. If you know anything about NTLM and/or can test,
 * please contect us.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 * @version 1.2
 */
public class SaslNTLM
  implements SaslClient
{

  private static final String OEM ="ISO-8859-1";
  private static final String UNICODE = "UnicodeLittleUnmarked";

  private static final int STATE_TYPE1MESSAGE = 0;
  private static final int STATE_TYPE3MESSAGE = 1;
  private static final int STATE_DONE = 2;

  private static final byte[] NTLMSSP = getBytes("NTLMSSP", OEM);
  private static final int NTLM_MESSAGE_TYPE1 = 0x1000000;
  private static final int NTLM_MESSAGE_TYPE3 = 0x3000000;

  private static final int FLAG_UNICODE = 1;
  private static final int FLAG_OEM = 2;
  private static final int FLAG_NTLM = 0x200;
  private static final int FLAG_DOMAIN_SUPPLIED = 0x1000;
  private static final int FLAG_WORKSTATION_SUPPLIED = 0x2000;
  private static final int FLAG_LOCAL_CALL = 0x4000;
  private static final int FLAG_TARGET_INFO = 0x800000;

  private static final byte[] MAGIC = new byte[]
    { 0x4b, 0x47, 0x53, 0x21, 0x40, 0x23, 0x24, 0x25 };

  private String username;
  private String password;
  private String domain;
  private String hostname;
  private int flags;
  private int state;
  private SecretKeyFactory factory;
  private Cipher cipher;
  private MessageDigest md4;

  public SaslNTLM(String username, String password)
    throws GeneralSecurityException
  {
    factory = SecretKeyFactory.getInstance("DES");
    cipher = Cipher.getInstance("DES/ECB/NoPadding");
    md4 = MessageDigest.getInstance("MD4");
    int di = username.indexOf('.');
    if (di != -1)
      {
        domain = username.substring(0, di).toUpperCase();
        username = username.substring(di + 1);
      }
    else
      {
        domain = "";
      }
    this.username = username;
    this.password = password;
    try
      {
        InetAddress localhost = InetAddress.getLocalHost();
        hostname = localhost.getCanonicalHostName();
        if (hostname == null)
          {
            hostname = new StringBuilder()
              .append('[')
              .append(localhost.getHostAddress())
              .append(']')
              .toString();
          }
        else
          {
            di = hostname.indexOf('.');
            if (di != -1)
              {
                hostname = hostname.substring(0, di);
              }
          }
      }
    catch (UnknownHostException e)
      {
        hostname = "";
      }
    flags = FLAG_OEM | FLAG_NTLM;
    int dl = domain.length();
    if (dl > 0)
      {
        flags |= FLAG_DOMAIN_SUPPLIED;
        if (dl > 8)
          {
            domain = domain.substring(0, 8);
          }
      }
    int hl = hostname.length();
    if (hl > 0)
      {
        flags |= FLAG_WORKSTATION_SUPPLIED;
        if (hl > 8)
          {
            hostname = hostname.substring(0, 8);
          }
      }
  }

  public String getMechanismName()
  {
    return "NTLM";
  }

  public boolean hasInitialResponse()
  {
    return true;
  }

  public byte[] evaluateChallenge(byte[] challenge)
    throws SaslException
  {
    try
      {
        switch (state)
          {
          case STATE_TYPE1MESSAGE:
            state = STATE_TYPE3MESSAGE;
            return createType1Message();
          case STATE_TYPE3MESSAGE:
            state = STATE_DONE;
            return createType3Message(challenge);
          default:
            return null;
          }
      }
    catch (GeneralSecurityException e)
      {
        throw new SaslException("NTLM", e);
      }
  }

  private static byte[] getBytes(String text, String charset)
  {
    try
      {
        return text.getBytes(charset);
      }
    catch (UnsupportedEncodingException e)
      {
        throw (RuntimeException) new RuntimeException().initCause(e);
      }
  }

  byte[] createType1Message()
    throws GeneralSecurityException
  {
    byte[] bdomain = getBytes(domain, OEM);
    byte[] bhostname = getBytes(hostname, OEM);
    int domainstart = 32;
    int hostnamestart = domainstart + bdomain.length;
    int end = hostnamestart + bhostname.length;
    byte[] t1 = new byte[end];
    System.arraycopy(NTLMSSP, 0, t1, 0, NTLMSSP.length);
    writeNumber(32, NTLM_MESSAGE_TYPE1, t1, 8);
    writeNumber(32, flags, t1, 12);
    if (bdomain.length > 0)
      {
        writeSecurityBuffer(t1, 16, bdomain.length, domainstart);
        System.arraycopy(bdomain, 0, t1, domainstart, bdomain.length);
      }
    if (bhostname.length > 0)
      {
        writeSecurityBuffer(t1, 24, bhostname.length, hostnamestart);
        System.arraycopy(bhostname, 0, t1, hostnamestart, bhostname.length);
      }
    return t1;
  }

  byte[] createType3Message(byte[] challenge)
    throws GeneralSecurityException
  {
    int cflags = readNumber(32, challenge, 20);
    String charset = ((cflags & FLAG_UNICODE) != 0) ? UNICODE : OEM;
    byte[] targetName = readSecurityBuffer(challenge, 12);
    byte[] busername = getBytes(username, charset);
    byte[] bhostname = getBytes(hostname, charset);
    // NTLM challenge itself is 8 bytes starting at offset 24
    byte[] lmresponse = response(lmhash(), challenge, 24);
    byte[] ntlmresponse = response(ntlmhash(), challenge, 24);
    int lmstart = 52;
    int ntlmstart = lmstart + 24;
    int targetstart = ntlmstart + 24;
    int usernamestart = targetstart + targetName.length;
    int hostnamestart = usernamestart + busername.length;
    int end = hostnamestart + bhostname.length;
    // Create the message
    byte[] t3 = new byte[end];
    System.arraycopy(NTLMSSP, 0, t3, 0, NTLMSSP.length);
    writeNumber(32, NTLM_MESSAGE_TYPE3, t3, 8);
    writeSecurityBuffer(t3, 12, 24, lmstart);
    System.arraycopy(lmresponse, 0, t3, lmstart, 24);
    writeSecurityBuffer(t3, 20, 24, ntlmstart);
    System.arraycopy(ntlmresponse, 0, t3, ntlmstart, 24);
    if (targetName.length > 0)
      {
        writeSecurityBuffer(t3, 28, targetName.length, targetstart);
        System.arraycopy(targetName, 0, t3, targetstart, targetName.length);
      }
    if (busername.length > 0)
      {
        writeSecurityBuffer(t3, 36, busername.length, usernamestart);
        System.arraycopy(busername, 0, t3, usernamestart, busername.length);
      }
    if (bhostname.length > 0)
      {
        writeSecurityBuffer(t3, 44, bhostname.length, hostnamestart);
        System.arraycopy(bhostname, 0, t3, hostnamestart, bhostname.length);
      }
    return t3;
  }

  // Read little-endian number.
  private static int readNumber(int bits, byte[] msg, int off)
  {
    int ret = 0, n = bits / 8;
    for (int i = 0; i < n; i++)
      {
        ret |= (((int) msg[off + i]) << (i * 4));
      }
    return ret;
  }

  // Write little-endian number.
  private static void writeNumber(int bits, int val, byte[] msg, int off)
  {
    int n = bits / 8;
    for (int i = 0; i < n; i++)
      {
        msg[off + i] = (byte) ((val >> (i * 4)) & 0xff);
      }
  }

  private static byte[] readSecurityBuffer(byte[] msg, int off)
  {
    int len = readNumber(16, msg, off);
    int start = readNumber(32, msg, off + 4);
    byte[] ret = new byte[len];
    System.arraycopy(msg, start, ret, 0, len);
    return ret;
  }

  private static void writeSecurityBuffer(byte[] msg, int off, int buflen,
                                          int start)
  {
    writeNumber(16, buflen, msg, off);
    writeNumber(16, buflen, msg, off + 2);
    writeNumber(32, start, msg, off + 4);
  }

  private byte[] deskey(byte[] pw, int off)
  {
    int[] key = new int[pw.length];
    for (int i = 0; i < pw.length; i++)
      {
        key[i] = ((int) pw[i]) & 0xff;
      }
    byte[] ret = new byte[8];
    ret[0] = (byte) key[off];
    for (int i = 1; i < 7; i++)
      {
        ret[i] = (byte) (key[off + (i - 1)] << (8 - i) & 0xff |
                         key[off + i] >> i);
      }
    ret[7] = (byte) (key[off + 6] << 1 & 0xff);
    return ret;
  }

  private byte[] lmhash()
    throws GeneralSecurityException
  {
    byte[] pw = getBytes(password.toUpperCase(), OEM);
    // pad to 14 bytes
    if (pw.length != 14)
      {
        byte[] tmp = new byte[14];
        System.arraycopy(pw, 0, tmp, 0, pw.length);
        pw = tmp;
      }
    SecretKey key;
    byte[] ret = new byte[21];
    cipher.init(1, factory.generateSecret(new DESKeySpec(deskey(pw, 0))));
    System.arraycopy(cipher.doFinal(MAGIC, 0, 8), 0, ret, 0, 8);
    cipher.init(1, factory.generateSecret(new DESKeySpec(deskey(pw, 7))));
    System.arraycopy(cipher.doFinal(MAGIC, 0, 8), 0, ret, 8, 8);
    return ret;
  }

  private byte[] ntlmhash()
  {
    byte[] pw = getBytes(password, UNICODE);
    byte[] hash = md4.digest(pw);
    byte[] ret = new byte[21];
    System.arraycopy(hash, 0, ret, 0, 16);
    return ret;
  }

  private byte[] response(byte[] hash, byte[] challenge, int off)
    throws GeneralSecurityException
  {
    byte[] ret = new byte[24];
    for (int i = 0; i < 3; i++)
      {
        byte[] key = deskey(hash, i * 7);
        cipher.init(1, factory.generateSecret(new DESKeySpec(key)));
        System.arraycopy(cipher.doFinal(challenge, off, 8), 0, ret, i * 8, 8);
      }
    return ret;
  }

  public boolean isComplete()
  {
    return state == STATE_DONE;
  }

  public byte[] unwrap(byte[] incoming, int off, int len)
    throws SaslException
  {
    byte[] ret = new byte[len - off];
    System.arraycopy(incoming, off, ret, 0, len);
    return ret;
  }

  public byte[] wrap(byte[] outgoing, int off, int len)
    throws SaslException
  {
    byte[] ret = new byte[len - off];
    System.arraycopy(outgoing, off, ret, 0, len);
    return ret;
  }

  public Object getNegotiatedProperty(String name)
  {
    return null;
  }

  public void dispose()
  {
  }

}

