/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.util;

import org.jajuk.util.log.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ext.services.network.NetworkUtils;
import ext.services.network.Proxy;

/**
 * Manages network downloads
 */
public class DownloadManager implements ITechnicalStrings {

	private static Proxy proxy;

	/**
	 * @param search
	 * @return a list of urls
	 */
	public static ArrayList<URL> getRemoteCoversList(String search) throws Exception {
		ArrayList<URL> alOut = new ArrayList<URL>(20); // URL list
		// check void searches
		if (search == null || search.trim().equals("")) {
			return alOut;
		}
		// Select cover size
		int i = ConfigurationManager.getInt(CONF_COVERS_SIZE);
		String size = null;
		switch (i) {
		case 0: // small only
			size = "small";
			break;
		case 1: // small or medium
			size = "small|medium";
			break;
		case 2: // medium only
			size = "medium";
			break;
		case 3: // medium or large
			size = "medium|large";
			break;
		case 4: // large only
			size = "large";
			break;
		}
		String sSearchUrl = "http://images.google.com/images?q="
				+ URLEncoder.encode(search, "ISO-8859-1")
				+ "&ie=ISO-8859-1&hl=en&btnG=Google+Search" + "&imgsz=" + size;
		Log.debug("Search URL: {{" + sSearchUrl + "}}");
		String sRes = downloadHtml(new URL(sSearchUrl));
		if (sRes == null || sRes.length() == 0) {
			return alOut;
		}
		// Extract urls
		Pattern pattern = Pattern.compile("http://[^,<>]*(.jpg|.gif|.png)");
		// "http://[^,]*(.jpg|.gif|.png).*[0-9]* [xX] [0-9]*.*- [0-9]*");
		Matcher matcher = pattern.matcher(sRes);
		while (matcher.find()) {
			// Clean up URLS
			String sUrl = matcher.group().replaceAll("%2520", "%20");
			URL url = new URL(sUrl);

			// Remove duplicates
			if (alOut.contains(url)) {
				continue;
			}
			// Ignore URLs related to Google
			if (url.toString().toLowerCase().matches(".*google.*")) {
				continue;
			}
			// Add the new url
			alOut.add(url);
		}

		return alOut;
	}

	/**
	 * Download the resource at the given url
	 * 
	 * @param url
	 *            to download
	 * @param Use
	 *            cache : store file in image cache
	 * @throws Exception
	 */
	public static void download(URL url, File fDestination) throws Exception {
		HttpURLConnection connection = NetworkUtils.getConnection(url, proxy);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fDestination));
		BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
		int i;
		while ((i = bis.read()) != -1) {
			bos.write(i);
		}
		bos.close();
		bis.close();
		connection.disconnect();
	}

	/**
	 * Download the resource at the given url
	 * 
	 * @param url
	 *            to download
	 * @throws Exception
	 * @return result as an array of bytes, null if a problem occured
	 */
	public static void downloadCover(URL url) throws Exception {
		// check if file is not already downloaded or being downloaded
		if (Util.getCachePath(url).exists()) {
			return;
		}
		HttpURLConnection connection = NetworkUtils.getConnection(url, proxy);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(Util
				.getCachePath(url)));
		BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
		int i;
		while ((i = bis.read()) != -1) {
			bos.write(i);
		}
		bos.close();
		bis.close();
		connection.disconnect();
	}

	public static void setProxy(Proxy proxy) {
		DownloadManager.proxy = proxy;
	}

	/**
	 * Download the cover list
	 * 
	 * @param url
	 *            to download
	 * @throws Exception
	 * @return result as an array of bytes, null if a problem occured
	 */
	public static String downloadHtml(URL url) throws Exception {
		return NetworkUtils.readURL(NetworkUtils.getConnection(url, proxy));
	}

	/**
	 * Set default proxy settings, used by cobra for ie
	 * 
	 */
	public synchronized static void setDefaultProxySettings() {
		String sProxyHost = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME);
		int iProxyPort = ConfigurationManager.getInt(CONF_NETWORK_PROXY_PORT);
		String sProxyLogin = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN);
		String sProxyPwd = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_PWD);
		if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
			// Set default proxy value
			Type proxyType = null;
			if (!ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
				proxyType = Type.DIRECT;
			} else if (PROXY_TYPE_HTTP.equals(ConfigurationManager
					.getProperty(CONF_NETWORK_PROXY_TYPE))) {
				proxyType = Type.HTTP;
			} else if (PROXY_TYPE_SOCKS.equals(ConfigurationManager
					.getProperty(CONF_NETWORK_PROXY_TYPE))) {
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
		if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
			System.getProperties().put("proxySet", "true");
			if (PROXY_TYPE_HTTP.equals(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_TYPE))) {
				System.setProperty("http.proxyHost", sProxyHost);
				System.setProperty("http.proxyPort", Integer.toString(iProxyPort));
			} else if (PROXY_TYPE_SOCKS.equals(ConfigurationManager
					.getProperty(CONF_NETWORK_PROXY_TYPE))) {
				System.setProperty("socksProxyHost", sProxyHost);
				System.setProperty("socksProxyPort ", Integer.toString(iProxyPort));
			}
			Authenticator.setDefault(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					String user = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN);
					char[] pwd = Util.rot13(
							ConfigurationManager.getProperty(CONF_NETWORK_PROXY_PWD)).toCharArray();
					return new PasswordAuthentication(user, pwd);
				}
			});
		}
	}

}
