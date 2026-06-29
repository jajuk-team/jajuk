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
package org.jajuk.services.network;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Optional;

/**
 * Immutable configuration object for HTTP proxy settings including authentication.
 * Provides clean separation between proxy configuration and network execution.
 */
public class ProxyConfig {

  private final String host;
  private final int port;
  private final Optional<String> username;
  private final Optional<String> password;
  private final Proxy.Type type;

  /**
   * Creates a new proxy configuration without blocking network connection.
   *
   * @param type     The proxy type (HTTP or SOCKS)
   * @param host     The proxy hostname
   * @param port     The proxy port number (1-65535)
   * @param username The username for authentication (null/empty if none)
   * @param password The password for authentication (ignored if no username)
   */
  public ProxyConfig(Proxy.Type type, String host, int port, String username, String password) {
    if (host == null || host.trim().isEmpty()) {
      throw new IllegalArgumentException("Proxy host cannot be null or empty");
    }
    if (port <= 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid port: " + port);
    }

    this.type = type != null ? type : java.net.Proxy.Type.HTTP;
    this.host = host;
    this.port = port;
    this.username = Optional.ofNullable(username).filter(u -> !u.isEmpty());
    this.password = Optional.ofNullable(password);
  }

  /**
   * Returns the underlying InetSocketAddress for legacy compatibility.
   */
  public InetSocketAddress getSocketAddress() {
    return InetSocketAddress.createUnresolved(host, port);
  }

  /**
   * Creates a standard ProxySelector for use with HttpClient.
   */
  public ProxySelector toProxySelector() {
    return ProxySelector.of(getSocketAddress());
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public Optional<String> getUsername() {
    return username;
  }

  public Optional<String> getPassword() {
    return password;
  }

  public java.net.Proxy.Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "ProxyConfig{host='" + host + "', port=" + port + ", type=" + type + "}";
  }
}