/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
 *  http://jajuk.info
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision$
 */

package org.jajuk.util;

import ext.services.network.NetworkUtils;
import ext.services.network.Proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.log.Log;

/**
 * Manages network downloads.
 */
public final class DownloadManager {

  /** DOCUMENT_ME. */
  private static Proxy proxy;

  /**
   * private constructor to avoid instantiating utility class.
   */
  private DownloadManager() {
  }

  /**
   * Gets the remote covers list.
   * 
   * @param search DOCUMENT_ME
   * 
   * @return a list of urls
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List<URL> getRemoteCoversList(String search) throws IOException {
    List<URL> alOut = new ArrayList<URL>(20); // URL list
    // check void searches
    if (search == null || search.trim().equals("")) {
      return alOut;
    }
    // Select cover size
    int i = Conf.getInt(Const.CONF_COVERS_SIZE);
    String size = null;
    switch (i) {
    case 0: // small only
      size = "small";
      break;
    case 1: // small or medium
      size = "small|medium";
      break;
    case 2: // medium only
      size = "medium";
      break;
    case 3: // medium or large
      size = "medium|large";
      break;
    case 4: // large only
      size = "large";
      break;
    }
    String sSearchUrl = "http://images.google.com/images?q="
        + URLEncoder.encode(search, "ISO-8859-1") + "&ie=ISO-8859-1&hl=en&btnG=Google+Search"
        + "&imgsz=" + size;
    Log.debug("Search URL: {{" + sSearchUrl + "}}");
    String sRes = downloadText(new URL(sSearchUrl));
    if (sRes == null || sRes.length() == 0) {
      return alOut;
    }
    // Extract urls
    Pattern pattern = Pattern.compile("http://[^,<>]*(.jpg|.gif|.png)");
    // "http://[^,]*(.jpg|.gif|.png).*[0-9]* [xX] [0-9]*.*- [0-9]*");
    Matcher matcher = pattern.matcher(sRes);
    while (matcher.find()) {
      // Clean up URLS
      String sUrl = matcher.group().replaceAll("%2520", "%20");
      URL url = new URL(sUrl);

      // Remove duplicates
      if (alOut.contains(url)) {
        continue;
      }
      // Ignore URLs related to Google
      if (url.toString().toLowerCase(Locale.getDefault()).matches(".*google.*")) {
        continue;
      }
      // Add the new url
      alOut.add(url);
    }
    return alOut;
  }

  /**
   * Download the resource at the given url.
   * 
   * @param url url to download
   * @param fDestination destination file
   * 
   * @throws IOException If a network problem occurs.
   */
  public static void download(URL url, File fDestination) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return;
    }
    HttpURLConnection connection = NetworkUtils.getConnection(url, proxy);
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fDestination));
    try {
      BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
      try {
        int i;
        while ((i = bis.read()) != -1) {
          bos.write(i);
        }
      } finally {
        bis.close();
      }

      bos.flush();
    } finally {
      bos.close();
    }
    connection.disconnect();
  }

  /**
   * Download the resource at the given url and cache it <br>
   * If the file is already in cache, it is returned immediately <br>
   * The cache is currently cleared at each Jajuk session startup.
   * 
   * @param url url to download
   * 
   * @return cached file or null if a problem occurred
   * 
   * @throws IOException If a network problem occurs or a temporary file cannot be
   * written.
   */
  public static File downloadToCache(URL url) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    File file = SessionService.getCachePath(url);
    // We synchronize the (interned) name of the cached file to avoid
    // probable collisions between views
    synchronized (file.getName().intern()) {
      // check if file is not already downloaded or being downloaded
      if (file.exists()) {
        return file;
      }
      HttpURLConnection connection = NetworkUtils.getConnection(url, proxy);
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
      try {
        BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
        try {
          int i;
          while ((i = bis.read()) != -1) {
            bos.write(i);
          }
        } finally {
          bis.close();
        }
        bos.flush();
      } finally {
        bos.close();
      }
      connection.disconnect();
      return file;
    }
  }

  /**
   * Download the cover list.
   * 
   * @param url to download
   * @param charset DOCUMENT_ME
   * 
   * @return result as an array of bytes, null if a problem occurred
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String downloadText(URL url, String charset) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    HttpURLConnection connection = NetworkUtils.getConnection(url, proxy);
    try {
      return NetworkUtils.readURL(connection, charset);
    } finally {
      connection.disconnect();
    }
  }

  /**
   * Download text with the default charset UTF-8.
   * 
   * @param url DOCUMENT_ME
   * 
   * @return the string
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String downloadText(URL url) throws IOException {
    return downloadText(url, "UTF-8");
  }

  /**
   * Return a string for a given URL and encoding, used to retrieve text from a
   * cached file.
   * 
   * @param url url to read
   * @param encoding encoding of the content of the file
   * 
   * @return a string for a given URL and encoding
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static String getTextFromCachedFile(URL url, String encoding) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    File file = downloadToCache(url);
    StringBuilder builder = new StringBuilder();
    InputStream input = new BufferedInputStream(new FileInputStream(file));
    try {
      byte[] array = new byte[1024];
      int read;
      while ((read = input.read(array)) > 0) {
        builder.append(new String(array, 0, read, encoding));
      }
    } finally {
      input.close();
    }
    return builder.toString();
  }

  /**
   * Set default proxy settings, used by cobra for ie.
   */
  public synchronized static void setDefaultProxySettings() {
    String sProxyHost = Conf.getString(Const.CONF_NETWORK_PROXY_HOSTNAME);
    int iProxyPort = Conf.getInt(Const.CONF_NETWORK_PROXY_PORT);
    String sProxyLogin = Conf.getString(Const.CONF_NETWORK_PROXY_LOGIN);
    String sProxyPwd = Conf.getString(Const.CONF_NETWORK_PROXY_PWD);
    Type proxyType = Type.DIRECT;
    if (Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY)) {
      // Set default proxy value
      if (Const.PROXY_TYPE_HTTP.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
        proxyType = Type.HTTP;
      } else if (Const.PROXY_TYPE_SOCKS.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
        proxyType = Type.SOCKS;
      }
      try {
        proxy = new Proxy(proxyType, sProxyHost, iProxyPort, sProxyLogin, sProxyPwd);
      } catch (Exception e) {
        Log.error(e);
        return;
      }
    }
    // Set system defaults proxy values, if we don't use DownloadManager
    // methods
    // see http://java.sun.com/j2se/1.4.2/docs/guide/net/properties.html
    if (Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY)) {
      System.getProperties().put("proxySet", "true");
      if (Const.PROXY_TYPE_HTTP.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
        System.setProperty("http.proxyHost", sProxyHost);
        System.setProperty("http.proxyPort", Integer.toString(iProxyPort));
      } else if (Const.PROXY_TYPE_SOCKS.equals(Conf.getString(Const.CONF_NETWORK_PROXY_TYPE))) {
        System.setProperty("socksProxyHost", sProxyHost);
        System.setProperty("socksProxyPort ", Integer.toString(iProxyPort));
      }
      Authenticator.setDefault(new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          String user = Conf.getString(Const.CONF_NETWORK_PROXY_LOGIN);
          char[] pwd = UtilString.rot13(Conf.getString(Const.CONF_NETWORK_PROXY_PWD)).toCharArray();
          return new PasswordAuthentication(user, pwd);
        }
      });
    }
  }

  /**
   * Gets the proxy.
   * 
   * @return the proxy
   */
  public static Proxy getProxy() {
    return proxy;
  }

}
