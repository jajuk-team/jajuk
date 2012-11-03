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
package ext.services.network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy.Type;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.lang.StringUtils;
import org.jajuk.TestHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * .
 */
public class TestNetworkUtils extends JajukTestCase {
  /** The Constant PROXY_PORT.   */
  private static final int PROXY_PORT = 0; // auto-choose
  /** The Constant URL.   */
  private static final String URL = "http://www.google.com/";
  /** The Constant FTP_URL.   */
  private static final String FTP_URL = "ftp://www.google.com/";

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "false");
    super.setUp();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.services.network.NetworkUtils#getConnection(java.lang.String, ext.services.network.Proxy)}
   * .
   */
  public void testGetConnectionStringProxy() throws Exception {
    // null when no connection is allowed
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "true");
    assertNull(NetworkUtils.getConnection(URL, null));
    // useful content when inet access is allowed
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "false");
    HttpURLConnection connection = NetworkUtils.getConnection(URL, null);
    assertNotNull(connection);
    connection.disconnect();
  }

  /**
   * Test get connection string proxy invalid url.
   * 
   *
   * @throws Exception the exception
   */
  public void testGetConnectionStringProxyInvalidURL() throws Exception {
    // useful content when inet access is allowed
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "false");
    try {
      NetworkUtils.getConnection(FTP_URL, null);
      fail("Should fail here");
    } catch (IllegalArgumentException e) {
      // make sure the url is part of the error message
      assertTrue(e.getMessage(), e.getMessage().contains(FTP_URL));
    }
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.services.network.NetworkUtils#getConnection(java.net.URL, ext.services.network.Proxy)}
   * .
   */
  public void testGetConnectionURLProxy() throws Exception {
    // null when no connection is allowed
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "true");
    assertNull(NetworkUtils.getConnection(new java.net.URL(URL), null));
    // useful content when inet access is allowed
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "false");
    HttpURLConnection connection = NetworkUtils.getConnection(new java.net.URL(URL), null);
    assertNotNull(connection);
    connection.disconnect();
  }

  boolean bStop = false;

  /**
   * Test get connection url proxy with proxy.
   * 
   *
   * @throws Exception the exception
   */
  public void testGetConnectionURLProxyWithProxy() throws Exception {
    final ServerSocket socket = new ServerSocket(PROXY_PORT);
    Thread thread = new Thread("ProxySocketAcceptThread") {
      @Override
      public void run() {
        try {
          while (!bStop) {
            Socket sock = socket.accept();
            Log.debug("Accepted connection, sending back garbage and close socket...");
            sock.getOutputStream().write(1);
            sock.close();
          }
        } catch (IOException e) {
          Log.error(e);
        }
      }
    };
    thread.setDaemon(true); // to finish tests even if this is still running
    thread.start();
    Log.debug("Using local port: " + socket.getLocalPort());
    try {
      // useful content when inet access is allowed
      Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "false");
      HttpURLConnection connection = NetworkUtils.getConnection(new java.net.URL(URL), new Proxy(
          Type.SOCKS, "localhost", socket.getLocalPort(), "user", "password"));
      assertNotNull(connection);
      connection.disconnect();
    } finally {
      bStop = true;
      socket.close();
      thread.join();
    }
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.services.network.NetworkUtils#readURL(java.net.URLConnection, java.lang.String)}
   * .
   */
  public void testReadURLURLConnectionString() throws Exception {
    HttpURLConnection connection = NetworkUtils.getConnection(URL, null);
    assertNotNull(connection);
    String str = NetworkUtils.readURL(connection, "UTF-8");
    assertNotNull(str);
    assertTrue(StringUtils.isNotBlank(str));
    connection.disconnect();
  }

  /**
   * Test read urlurl connection string disabled.
   * 
   *
   * @throws Exception the exception
   */
  public void testReadURLURLConnectionStringDisabled() throws Exception {
    HttpURLConnection connection = NetworkUtils.getConnection(URL, null);
    assertNotNull(connection);
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "true");
    assertNull(NetworkUtils.readURL(connection, "UTF-8"));
    connection.disconnect();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.services.network.NetworkUtils#readURL(java.net.URLConnection)}.
   */
  public void testReadURLURLConnection() throws Exception {
    HttpURLConnection connection = NetworkUtils.getConnection(URL, null);
    assertNotNull(connection);
    String str = NetworkUtils.readURL(connection);
    assertNotNull(str);
    assertTrue(StringUtils.isNotBlank(str));
    connection.disconnect();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.services.network.NetworkUtils#readPostURL(java.net.HttpURLConnection, java.lang.String)}
   * .
   */
  public void testReadPostURL() throws Exception {
    HttpURLConnection connection = NetworkUtils.getConnection(URL, null);
    assertNotNull(connection);
    connection.setDoOutput(true);
    // TODO: currently I do not have an URL that works via POST, therefore we
    // get an invalid return code
    try {
      NetworkUtils.readPostURL(connection, "post");
      fail("Currently fails here");
    } catch (IllegalArgumentException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("Invalid HTTP return code"));
    }
    connection.disconnect();
  }

  /**
   * Test read post url disabled.
   * 
   *
   * @throws Exception the exception
   */
  public void testReadPostURLDisabled() throws Exception {
    HttpURLConnection connection = NetworkUtils.getConnection(URL, null);
    assertNotNull(connection);
    connection.setDoOutput(true);
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "true");
    assertNull(NetworkUtils.readPostURL(connection, "post"));
    connection.disconnect();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.services.network.NetworkUtils#getImage(java.net.URLConnection)}.
   */
  public void testGetImage() throws Exception {
    HttpURLConnection connection = NetworkUtils.getConnection(URL, null);
    assertNotNull(connection);
    connection.setDoOutput(true);
    assertNull(NetworkUtils.getImage(connection));
  }

  /**
   * Test get image disabled.
   * 
   *
   * @throws Exception the exception
   */
  public void testGetImageDisabled() throws Exception {
    HttpURLConnection connection = NetworkUtils.getConnection(URL, null);
    assertNotNull(connection);
    connection.setDoOutput(true);
    Conf.setProperty(Const.CONF_NETWORK_NONE_INTERNET_ACCESS, "true");
    assertNull(NetworkUtils.getImage(connection));
  }

  /**
   * Test method for.
   *
   * {@link ext.services.network.NetworkUtils#encodeString(java.lang.String)}.
   */
  public void testEncodeString() {
    assertEquals("teststring", NetworkUtils.encodeString("teststring"));
    assertEquals(
        "teststring%26%21%40%23*%21%40%23%28%40%23%25%24%23%40%24%29%5E*%2F%3F%3E%3C%3F%3E%2F%2C.%22%3A%22%3B%27%27%3B%27%7B%7D%7C%5C%5D%5B%5C%22",
        NetworkUtils.encodeString("teststring&!@#*!@#(@#%$#@$)^*/?><?>/,.\":\";'';'{}|\\][\\\""));
  }

  // helper method to emma-coverage of the unused constructor
  /**
   * Test private constructor.
   * 
   *
   * @throws Exception the exception
   */
  public void testPrivateConstructor() throws Exception { // For EMMA
    // code-coverage tests
    TestHelpers.executePrivateConstructor(NetworkUtils.class);
  }
}
