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

import org.jajuk.services.network.HttpClientService;
import org.jajuk.services.network.ProxyConfig;
import org.jajuk.util.log.Log;

import java.net.Proxy;

public class UtilProxy {
  /**
   * Adaptation of the setDefaultProxySettings method to use HttpClientService and ProxyConfig.
   * Should be called at application startup or when network settings change.
   */
  public synchronized static void setDefaultProxySettings() {
    // Get configuration parameter
    String sProxyHost = Conf.getString(Const.CONF_NETWORK_PROXY_HOSTNAME);
    int iProxyPort = Conf.getInt(Const.CONF_NETWORK_PROXY_PORT);
    String sProxyLogin = Conf.getString(Const.CONF_NETWORK_PROXY_LOGIN);
    String sProxyPwd = Conf.getString(Const.CONF_NETWORK_PROXY_PWD);

    boolean useProxy = Conf.getBoolean(Const.CONF_NETWORK_USE_PROXY);

    HttpClientService httpService = HttpClientService.getInstance();

    if (useProxy && sProxyHost != null && !sProxyHost.trim().isEmpty()) {
      // Dzterminz proxy type
      Proxy.Type proxyType;
      String proxyTypeStr = Conf.getString(Const.CONF_NETWORK_PROXY_TYPE);

      if (Const.PROXY_TYPE_HTTP.equals(proxyTypeStr)) {
        proxyType = Proxy.Type.HTTP;
      } else if (Const.PROXY_TYPE_SOCKS.equals(proxyTypeStr)) {
        proxyType = Proxy.Type.SOCKS;
      } else {
        Log.warn("Unknown proxy type '" + proxyTypeStr + "', defaulting to HTTP");
        proxyType = Proxy.Type.HTTP;
      }

      try {
        // Creation of immuable object ProxyConfig
        ProxyConfig config = new ProxyConfig(proxyType, sProxyHost, iProxyPort, sProxyLogin, sProxyPwd);

        // Config Injection with proxy
        httpService.setProxyConfig(config);

        Log.info("Proxy configured successfully: " + proxyType + "://" + sProxyHost + ":" + iProxyPort +
                (sProxyLogin != null ? " [Authenticated]" : ""));

      } catch (Exception e) {
        Log.warn("Failed to configure proxy: " + e.getMessage());
        httpService.setProxyConfig(null);
      }
    } else {
      // Disabling proxy
      httpService.setProxyConfig(null);
      Log.info("Proxy disabled by configuration.");

      // Clearing system properties if disabled
      System.clearProperty("http.proxyHost");
      System.clearProperty("http.proxyPort");
      System.clearProperty("socksProxyHost");
      System.clearProperty("socksProxyPort");
      // Authenticator.setDefault(null); // Optionnal
    }

    // Update internet access setting
    httpService.updateInternetAccessSetting();
  }
}
