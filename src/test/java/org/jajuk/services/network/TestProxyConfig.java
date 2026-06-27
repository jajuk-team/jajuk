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
 */
package org.jajuk.services.network; // ou org.jajuk.services.network selon votre choix final

import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;

import static org.junit.Assert.*;

/**
 * Unit tests for the immutable ProxyConfig class.
 * Replaces the old TestProxy which tested the mutable, network-blocking Proxy class.
 */
public class TestProxyConfig {

  /**
   * Test valid constructor with HTTP proxy and credentials.
   */
  @Test
  public void testValidConstructorHTTP() {
    String host = "proxy.example.com";
    int port = 8080;
    String user = "user123";
    String pass = "pass456";

    ProxyConfig config = new ProxyConfig(Proxy.Type.HTTP, host, port, user, pass);

    assertEquals("Host mismatch", host, config.getHost());
    assertEquals("Port mismatch", port, config.getPort());
    assertEquals("User mismatch", user, config.getUsername().orElse(null));
    assertEquals("Password mismatch", pass, config.getPassword().orElse(null));
    assertEquals("Type mismatch", Proxy.Type.HTTP, config.getType());

    assertTrue("Username should be present", config.getUsername().isPresent());
    assertTrue("Password should be present", config.getPassword().isPresent());
  }

  /**
   * Test valid constructor without credentials.
   */
  @Test
  public void testValidConstructorNoAuth() {
    String host = "proxy.example.com";
    int port = 8080;

    ProxyConfig config = new ProxyConfig(Proxy.Type.HTTP, host, port, null, null);

    assertEquals("Host mismatch", host, config.getHost());
    assertFalse("Username should be empty", config.getUsername().isPresent());
    assertFalse("Password should be empty", config.getPassword().isPresent());
  }

  /**
   * Test constructor with empty username (should be treated as no auth).
   */
  @Test
  public void testEmptyUsername() {
    ProxyConfig config = new ProxyConfig(Proxy.Type.HTTP, "host", 8080, "", "pass");
    assertFalse("Empty username should result in Optional.empty()", config.getUsername().isPresent());
  }

  /**
   * Test invalid host (null) throws IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidHostNull() {
    new ProxyConfig(Proxy.Type.HTTP, null, 8080, "user", "pass");
  }

  /**
   * Test invalid host (empty string) throws IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidHostEmpty() {
    new ProxyConfig(Proxy.Type.HTTP, "", 8080, "user", "pass");
  }

  /**
   * Test invalid port (negative) throws IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidPortNegative() {
    new ProxyConfig(Proxy.Type.HTTP, "host", -1, "user", "pass");
  }

  /**
   * Test invalid port (too high) throws IllegalArgumentException.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidPortHigh() {
    new ProxyConfig(Proxy.Type.HTTP, "host", 65536, "user", "pass");
  }

  /**
   * Test getSocketAddress returns correct InetSocketAddress.
   */
  @Test
  public void testGetSocketAddress() {
    String host = "192.168.1.1";
    int port = 3128;
    ProxyConfig config = new ProxyConfig(Proxy.Type.HTTP, host, port, null, null);

    InetSocketAddress addr = config.getSocketAddress();
    assertNotNull("Address should not be null", addr);
    assertEquals("Host mismatch in socket address", host, addr.getHostString());
    assertEquals("Port mismatch in socket address", port, addr.getPort());
    // Verify it is unresolved (hostname based) as per implementation
    assertTrue("Socket address should be unresolved (hostname)", addr.isUnresolved() || addr.getAddress() == null);
  }

  /**
   * Test toProxySelector creates a valid ProxySelector.
   */
  @Test
  public void testToProxySelector() {
    String host = "proxy.test.local";
    int port = 1080;
    ProxyConfig config = new ProxyConfig(Proxy.Type.SOCKS, host, port, null, null);

    ProxySelector selector = config.toProxySelector();
    assertNotNull("ProxySelector should not be null", selector);
    // We cannot easily test the selection logic without a real URL,
    // but we ensure the object is created.
  }

  /**
   * Test toString format.
   */
  @Test
  public void testToString() {
    ProxyConfig config = new ProxyConfig(Proxy.Type.HTTP, "my.proxy", 9090, "admin", "secret");
    String str = config.toString();

    assertTrue("toString should contain host", str.contains("my.proxy"));
    assertTrue("toString should contain port", str.contains("9090"));
    assertTrue("toString should contain type", str.contains("HTTP"));
    // Security: verify password is NOT printed in toString (though our impl doesn't print it)
    assertFalse("toString should NOT contain password", str.contains("secret"));
  }

  /**
   * Test that proxy type defaults to HTTP if null provided.
   */
  @Test
  public void testDefaultType() {
    ProxyConfig config = new ProxyConfig(null, "host", 8080, null, null);
    assertEquals("Default type should be HTTP", Proxy.Type.HTTP, config.getType());
  }
}