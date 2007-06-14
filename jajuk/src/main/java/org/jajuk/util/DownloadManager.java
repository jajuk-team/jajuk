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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.PasswordDialog;
import org.jajuk.util.log.Log;

/**
 * Manages network downloads
 */
public class DownloadManager implements ITechnicalStrings {

	/** Proxy pwd */
	private static String sProxyPwd = null;

	/**
	 * 
	 * @return a custom content handler of any mime type and based on HTTP
	 *         protocol
	 
	public static URLStreamHandler getStreamHandler() {
		return handler;
	}*/

	/**
	 * @param sProxyUser
	 * @param sProxyPassswd
	 * @return an HTTP client
	 */
	private static HttpClient getHTTPClient(String sProxyUser, String sProxyPassswd,
			int iConTimeout, int iDataTimeout) {
		HttpClient client = new HttpClient();
		// connection to
		client.getHttpConnectionManager().getParams().setConnectionTimeout(iConTimeout);
		// data reception timeout
		client.getHttpConnectionManager().getParams().setSoTimeout(iDataTimeout);
		// Add proxy-specific configuration
		if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
			client.getHostConfiguration().setProxy(
					ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME),
					Integer.parseInt(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_PORT)));
			// The proxy requires authentication
			if (sProxyUser != null && sProxyPassswd != null) {
				client.getState().setProxyCredentials(new AuthScope(AuthScope.ANY),
						new UsernamePasswordCredentials(sProxyUser, sProxyPwd));
			}
		}
		return client;
	}

	/**
	 * @return Get an HTTP client
	 */
	private static HttpClient getHTTPClient(int iConTimeout, int iDataTimeout) {
		return getHTTPClient(null, null, iConTimeout, iDataTimeout);
	}

	/**
	 * @param sHostname,
	 *            the host name
	 * @param sProxyURL
	 *            The proxy port, null if you don't use a proxy
	 * @param iProxyPort,
	 *            proxy port if you use one of -1 if not
	 * @return An host configuration
	 */
	private static HostConfiguration getHostConfiguration(String sHostname, String sProxyURL,
			int iProxyPort) {
		HostConfiguration host = new HostConfiguration();
		host.setHost(sHostname);
		if (sProxyURL != null && iProxyPort > 0) {
			host.setProxy(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME),
					ConfigurationManager.getInt(CONF_NETWORK_PROXY_PORT));
		}
		return host;
	}

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
		byte[] bRes = downloadUrl(new URL(sSearchUrl));
		if (bRes == null || bRes.length == 0) {
			return alOut;
		}
		String sRes = new String(bRes);
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
		GetMethod get = null;
		HttpClient client = null;
		int iConTO = 2000 * ConfigurationManager.getInt(CONF_NETWORK_CONNECTION_TO);
		int iTraTO = 2000 * ConfigurationManager.getInt(CONF_NETWORK_TRANSFERT_TO);
		if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
			client = getHTTPClient(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN),
					DownloadManager.getProxyPwd(), iConTO, iTraTO);
		} else {
			client = getHTTPClient(iConTO, iTraTO);
		}
		get = new GetMethod(url.toString());
		get.addRequestHeader("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
		get.addRequestHeader("Accept-Language", "en-us");
		get.addRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
		get.addRequestHeader("Connection", "Keep-Alive");
		int status = client.executeMethod(get);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fDestination));
		BufferedInputStream bis = new BufferedInputStream(get.getResponseBodyAsStream());
		int i;
		while ((i = bis.read()) != -1) {
			bos.write(i);
		}
		bos.close();
		bis.close();
		if (get.isRequestSent()) {
			get.releaseConnection();
		}
	}

	/**
	 * Download the resource at the given url
	 * 
	 * @param url
	 *            to download
	 * @throws Exception
	 * @return result as an array of bytes, null if a problem occured
	 */
	public static byte[] downloadCover(URL url) throws Exception {
		byte[] bOut = null;
		// check if file is not already downloaded or being downloaded
		if (Util.getCachePath(url).exists()) {
			return bOut;
		}
		GetMethod get = null;
		HttpClient client = null;
		int iConTO = 1000 * ConfigurationManager.getInt(CONF_NETWORK_CONNECTION_TO);
		int iTraTO = 1000 * ConfigurationManager.getInt(CONF_NETWORK_TRANSFERT_TO);
		if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
			client = getHTTPClient(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN),
					DownloadManager.getProxyPwd(), iConTO, iTraTO);
		} else {
			client = getHTTPClient(iConTO, iTraTO);
		}
		get = new GetMethod(url.toString());
		get.addRequestHeader("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
		get.addRequestHeader("Accept-Language", "en-us");
		get.addRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
		get.addRequestHeader("Connection", "Keep-Alive");
		int status = client.executeMethod(get);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(Util
				.getCachePath(url)));
		BufferedInputStream bis = new BufferedInputStream(get.getResponseBodyAsStream());
		int i;
		while ((i = bis.read()) != -1) {
			bos.write(i);
		}
		bos.close();
		bis.close();
		if (get.isRequestSent()) {
			get.releaseConnection();
		}
		return bOut;
	}

	/**
	 * Download the cover list
	 * 
	 * @param url
	 *            to download
	 * @throws Exception
	 * @return result as an array of bytes, null if a problem occured
	 */
	public static byte[] downloadUrl(URL url) throws Exception {
		byte[] bOut = null;
		GetMethod get = null;
		HttpClient client = null;
		int iConTO = 1000 * ConfigurationManager.getInt(CONF_NETWORK_CONNECTION_TO);
		int iTraTO = 1000 * ConfigurationManager.getInt(CONF_NETWORK_TRANSFERT_TO);
		if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
			client = getHTTPClient(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN),
					DownloadManager.getProxyPwd(), iConTO, iTraTO);
		} else {
			client = getHTTPClient(iConTO, iTraTO);
		}
		get = new GetMethod(url.toString());
		get.addRequestHeader("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
		get.addRequestHeader("Accept-Language", "en-us");
		get.addRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
		get.addRequestHeader("Connection", "Keep-Alive");
		int status = client.executeMethod(get);
		bOut = get.getResponseBody();
		if (get.isRequestSent()) {
			get.releaseConnection();
		}
		return bOut;
	}

	/**
	 * @return the required proxy pwd
	 *         <p>
	 *         must be synchronized to avoid displaying several password dialogs
	 *         </p>
	 */
	public synchronized static String getProxyPwd() {
		String sLogin = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN);
		// If user didn't specified a user, it means it's an unauthenticated
		// proxy session,
		// don't ask for a password
		if (sLogin == null || sLogin.trim().equals("")) {
			return null;
		}
		if (sProxyPwd == null || sProxyPwd.trim().equals("")) {
			PasswordDialog pd = new PasswordDialog(Messages.getString("DownloadManager.1"));
			sProxyPwd = (String) pd.getOptionPane().getValue();
		}
		return sProxyPwd;
	}
	
	/**
	 * Set default proxy settings, used by cobra for ie
	 *
	 */
	public synchronized static void setDefaultProxySettings(){
		if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)) {
			System.getProperties().put( "proxySet", "true" );
			System.setProperty("http.proxyHost", ConfigurationManager
					.getProperty(CONF_NETWORK_PROXY_HOSTNAME));
			System.setProperty("http.proxyPort", ConfigurationManager
					.getProperty(CONF_NETWORK_PROXY_PORT));
			 Authenticator.setDefault( new Authenticator() {
			
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					String user = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN);
					char[] pwd = DownloadManager.getProxyPwd().toCharArray();
					return new PasswordAuthentication(user,pwd);
				}
			} ); 
		}
	}

}
