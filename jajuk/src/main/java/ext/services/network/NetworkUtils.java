/**
 * aTunes 1.6.6
 * Copyright (C) 2006-2007 Alex Aranda (fleax) alex@atunes.org
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package ext.services.network;

import java.awt.Image;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.imageio.ImageIO;

import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

public final class NetworkUtils {

  private NetworkUtils() {
    // default hidden constructor for utility classes
  }

  public static HttpURLConnection getConnection(String urlString, Proxy proxy) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    URL url = new URL(urlString);
    HttpURLConnection connection = getConnection(url, proxy);
    setConfiguration(connection);
    return connection;
  }

  public static HttpURLConnection getConnection(URL url, Proxy proxy) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    Log.debug("Opening Connection With: {{" + url + "}}");
    HttpURLConnection connection;
    if (proxy == null) {
      URLConnection urlconnection = url.openConnection();
      if (urlconnection instanceof HttpURLConnection) {
        connection = (HttpURLConnection) urlconnection;
      } else {
        throw new IllegalArgumentException("URL to connect to is not a HttpURL: " + url.toString());
      }
    } else {
      connection = (HttpURLConnection) proxy.getConnection(url);
    }
    setConfiguration(connection);
    return connection;
  }

  private static void setConfiguration(HttpURLConnection connection) {
    connection.setConnectTimeout(1000 * Conf.getInt(Const.CONF_NETWORK_CONNECTION_TO));
    // Google needs this
    connection.addRequestProperty("Accept",
        "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
    connection.addRequestProperty("Accept-Language", "en-us");
    connection.addRequestProperty("User-Agent",
        "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
    connection.addRequestProperty("Connection", "Keep-Alive");
  }

  public static String readURL(URLConnection connection, String charset) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    StringBuilder builder = new StringBuilder();
    InputStream input = connection.getInputStream();
    try {
      byte[] array = new byte[1024];
      int read;
      while ((read = input.read(array)) > 0) {
        builder.append(new String(array, 0, read, charset));
      }
    } finally {
      input.close();
    }
    return builder.toString();
  }

  public static String readURL(URLConnection connection) throws IOException {
    return readURL(connection, "UTF-8");
  }

  public static String readPostURL(HttpURLConnection connection, String post) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    OutputStream out = connection.getOutputStream();
    DataOutputStream writer = new DataOutputStream(out);
    try {
      writer.writeBytes(post);
      writer.flush();
    } finally {
      writer.close();
    }

    if (connection.getResponseCode() != 200) {
      throw new IllegalArgumentException("Invalid HTTP return code");
    }

    StringBuilder builder = new StringBuilder();
    InputStream input = connection.getInputStream();
    try {
      byte[] array = new byte[1024];
      int read;
      while ((read = input.read(array)) > 0) {
        builder.append(new String(array, 0, read, "UTF-8"));
      }
    } finally {
      input.close();
    }

    return builder.toString();
  }

  public static Image getImage(URLConnection connection) throws IOException {
    // Drop the query if user required "none Internet access from jajuk".
    // This method shouldn't be called anyway because we views have to deal with
    // this option at their level, this is a additional control.
    if (Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)) {
      return null;
    }
    InputStream input = connection.getInputStream();
    return ImageIO.read(input);
  }

  /**
   * Encodes a string in a format suitable to send a http request
   * 
   * @param s
   * @return
   */
  public static String encodeString(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return s;
    }
  }

}
