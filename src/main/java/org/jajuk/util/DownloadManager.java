/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.util;

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
import java.net.Proxy.Type;
import java.net.URL;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.log.Log;

import ext.services.network.NetworkUtils;
import ext.services.network.Proxy;

/**
 * Manages network downloads.
 */
public final class DownloadManager {
  private static Proxy proxy;

  private static final int BUFFER_SIZE = 8000;

  /**
   * private constructor to avoid instantiating utility class.
   */
  private DownloadManager() {
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
    HttpURLConnection connection = NetworkUtils.getConnection(url, proxy);
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fDestination));
    try {
      BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
      try {
        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = bis.read(buffer)) != -1) {
          bos.write(buffer, 0, bytesRead);
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
      if (file.exists() && file.length() > 0) {
        return file;
      }
      download(url,file);
      return file;
    }
  }

  /**
   * Download the cover list.
   * 
   * @param url to download
   * @param charset 
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
   * @param url 
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
