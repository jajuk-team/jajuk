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

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

public class Proxy extends java.net.Proxy {

  private static final long serialVersionUID = 7495084217081194366L;
  private final String url;
  private final int port;
  private final String user;
  private final String password;

  public Proxy(Type type, String url, int port, String user, String password)
      throws UnknownHostException, IOException {
    super(type, new Socket(url, port).getRemoteSocketAddress());
    this.url = url;
    this.port = port;
    this.user = user;
    this.password = password;
  }

  public URLConnection getConnection(URL u) throws IOException {
    URLConnection con = u.openConnection(this);
    String encodedUserPwd = Base64Coder.encodeString((user + ':' + password));
    con.setRequestProperty("Proxy-Authorization", "Basic " + encodedUserPwd);
    return con;
  }

  public String getPassword() {
    return password;
  }

  public String getUrl() {
    return url;
  }

  public int getPort() {
    return port;
  }

  public String getUser() {
    return user;
  }

}
