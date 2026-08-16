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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;


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

    assertEquals(host, config.getHost(), "Host mismatch");
    assertEquals(port, config.getPort(), "Port mismatch");
    assertEquals(user, config.getUsername().orElse(null), "User mismatch");
    assertEquals(pass, config.getPassword().orElse(null), "Password mismatch");
    assertEquals(Proxy.Type.HTTP, config.getType(), "Type mismatch");

    assertTrue(config.getUsername().isPresent(), "Username should be present");
    assertTrue(config.getPassword().isPresent(), "Password should be present");
  }

  /**
   * Test valid constructor without credentials.
   */
  @Test
  public void testValidConstructorNoAuth() {
    String host = "proxy.example.com";
    int port = 8080;

    ProxyConfig config = new ProxyConfig(Proxy.Type.HTTP, host, port, null, null);

    assertEquals(host, config.getHost(), "Host mismatch");
    assertFalse(config.getUsername().isPresent(), "Username should be empty");
    assertFalse(config.getPassword().isPresent(), "Password should be empty");
  }

  /**
   * Test constructor with empty username (should be treated as no auth).
   */
  @Test
  public void testEmptyUsername() {
    ProxyConfig config = new ProxyConfig(Proxy.Type.HTTP, "host", 8080, "", "pass");
    assertFalse(config.getUsername().isPresent(), "Empty username should result in Optional.empty()");
  }

  /**
   * Test invalid host (null) throws IllegalArgumentException.
   */
  @Test
  public void testInvalidHostNull() {
    assertThrows(IllegalArgumentException.class, () -> new ProxyConfig(Proxy.Type.HTTP, null, 8080, "user", "pass"));
  }

  /**
   * Test invalid host (empty string) throws IllegalArgumentException.
   */
  @Test
  public void testInvalidHostEmpty() {
    assertThrows(IllegalArgumentException.class, () -> new ProxyConfig(Proxy.Type.HTTP, "", 8080, "user", "pass"));
  }

  /**
   * Test invalid port (negative) throws IllegalArgumentException.
   */
  @Test
  public void testInvalidPortNegative() {
    assertThrows(IllegalArgumentException.class, () -> new ProxyConfig(Proxy.Type.HTTP, "host", -1, "user", "pass"));
  }

  /**
   * Test invalid port (too high) throws IllegalArgumentException.
   */
  @Test
  public void testInvalidPortHigh() {
    assertThrows(IllegalArgumentException.class, () -> new ProxyConfig(Proxy.Type.HTTP, "host", 65536, "user", "pass"));
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
    assertNotNull(addr, "Address should not be null");
    assertEquals(host, addr.getHostString(), "Host mismatch in socket address");
    assertEquals(port, addr.getPort(), "Port mismatch in socket address");
    // Verify it is unresolved (hostname based) as per implementation
    assertTrue(addr.isUnresolved() || addr.getAddress() == null, "Socket address should be unresolved (hostname)");
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
    assertNotNull(selector, "ProxySelector should not be null");
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

    assertTrue(str.contains("my.proxy"), "toString should contain host");
    assertTrue(str.contains("9090"), "toString should contain port");
    assertTrue(str.contains("HTTP"), "toString should contain type");
    // Security: verify password is NOT printed in toString (though our impl doesn't print it)
    assertFalse(str.contains("secret"), "toString should NOT contain password");
  }

  /**
   * Test that proxy type defaults to HTTP if null provided.
   */
  @Test
  public void testDefaultType() {
    ProxyConfig config = new ProxyConfig(null, "host", 8080, null, null);
    assertEquals(Proxy.Type.HTTP, config.getType(), "Default type should be HTTP");
  }
}