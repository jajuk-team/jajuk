/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

package ext.services.network;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.apache.commons.codec.binary.Base64;

/**
 * DOCUMENT_ME.
 */
public class Proxy extends java.net.Proxy {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 7495084217081194366L;

  /** DOCUMENT_ME. */
  private final String url;

  /** DOCUMENT_ME. */
  private final int port;

  /** DOCUMENT_ME. */
  private final String user;

  /** DOCUMENT_ME. */
  private final String password;

  /**
   * Instantiates a new proxy.
   * 
   * @param type DOCUMENT_ME
   * @param url DOCUMENT_ME
   * @param port DOCUMENT_ME
   * @param user DOCUMENT_ME
   * @param password DOCUMENT_ME
   * 
   * @throws UnknownHostException the unknown host exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public Proxy(Type type, String url, int port, String user, String password)
      throws UnknownHostException, IOException {
    super(type, new Socket(url, port).getRemoteSocketAddress());
    this.url = url;
    this.port = port;
    this.user = user;
    this.password = password;
  }

  /**
   * Gets the connection.
   * 
   * @param u DOCUMENT_ME
   * 
   * @return the connection
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public URLConnection getConnection(URL u) throws IOException {
    URLConnection con = u.openConnection(this);
    String encodedUserPwd = new String(Base64.encodeBase64((user + ':' + password).getBytes()));
    con.setRequestProperty("Proxy-Authorization", "Basic " + encodedUserPwd);
    return con;
  }

  /**
   * Gets the password.
   * 
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Gets the port.
   * 
   * @return the port
   */
  public int getPort() {
    return port;
  }

  /**
   * Gets the user.
   * 
   * @return the user
   */
  public String getUser() {
    return user;
  }

}
