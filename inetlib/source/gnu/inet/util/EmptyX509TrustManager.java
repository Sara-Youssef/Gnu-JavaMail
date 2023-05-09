/*
 * EmptyX509TrustManager.java
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

package gnu.inet.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

/**
 * Empty implementation of an X509 trust manager.
 * This implementation does not check any certificates in the chain.
 *
 * @author <a href='mailto:dog@gnu.org'>Chris Burdess</a>
 */
public class EmptyX509TrustManager
  implements X509TrustManager
{

  public void checkClientTrusted(X509Certificate[] chain, String authType)
    throws CertificateException
  {
  }

  public void checkServerTrusted(X509Certificate[] chain, String authType)
    throws CertificateException
  {
  }

  public X509Certificate[] getAcceptedIssuers()
  {
    return new X509Certificate[0];
  }

}

