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
import java.net.Proxy.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.jajuk.util.log.Log;

/**
 * .
 */
public class TestProxy extends TestCase {
  /** The Constant PROXY_PORT.   */
  private static final int PROXY_PORT = 0; // auto-choose
  ServerSocket socket;
  boolean bStop = false;

  @Override
  protected void setUp() throws Exception {
    socket = new ServerSocket(PROXY_PORT);
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
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    bStop = true;
    socket.close();
    super.tearDown();
  }

  /**
   * Test method for.
   *
   * @throws UnknownHostException the unknown host exception
   * @throws Exception the exception
   * {@link ext.services.network.Proxy#Proxy(java.net.Proxy.Type, java.lang.String, int, java.lang.String, java.lang.String)}
   * .
   */
  public void testProxyTypeStringIntStringString() throws UnknownHostException, Exception {
    new Proxy(Type.SOCKS, "localhost", socket.getLocalPort(), "user", "pwd");
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.services.network.Proxy#getConnection(java.net.URL)}.
   */
  public void testGetConnection() throws Exception {
    Proxy proxy = new Proxy(Type.SOCKS, "localhost", socket.getLocalPort(), "user", "pwd");
    proxy.getConnection(new URL("http://www.google.com/"));
  }

  /**
   * Test method for {@link ext.services.network.Proxy#getPassword()}.
   *
   * @throws Exception the exception
   */
  public void testGetPassword() throws Exception {
    Proxy proxy = new Proxy(Type.SOCKS, "localhost", socket.getLocalPort(), "user", "pwd");
    assertEquals("pwd", proxy.getPassword());
  }

  /**
   * Test method for {@link ext.services.network.Proxy#getUrl()}.
   *
   * @throws Exception the exception
   */
  public void testGetUrl() throws Exception {
    Proxy proxy = new Proxy(Type.SOCKS, "localhost", socket.getLocalPort(), "user", "pwd");
    assertEquals("localhost", proxy.getUrl());
  }

  /**
   * Test method for {@link ext.services.network.Proxy#getPort()}.
   *
   * @throws Exception the exception
   */
  public void testGetPort() throws Exception {
    Proxy proxy = new Proxy(Type.SOCKS, "localhost", socket.getLocalPort(), "user", "pwd");
    assertEquals(socket.getLocalPort(), proxy.getPort());
  }

  /**
   * Test method for {@link ext.services.network.Proxy#getUser()}.
   *
   * @throws Exception the exception
   */
  public void testGetUser() throws Exception {
    Proxy proxy = new Proxy(Type.SOCKS, "localhost", socket.getLocalPort(), "user", "pwd");
    assertEquals("user", proxy.getUser());
  }
}
