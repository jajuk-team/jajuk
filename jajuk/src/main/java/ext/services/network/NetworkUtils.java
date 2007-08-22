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

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

import java.awt.Image;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.net.Proxy.Type;

import javax.imageio.ImageIO;

public class NetworkUtils implements ITechnicalStrings {

	public static HttpURLConnection getConnection(String urlString, Proxy proxy) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection connection = getConnection(url, proxy);
		setConfiguration(connection);
		return connection;
	}

	public static HttpURLConnection getConnection(URL url, Proxy proxy) throws IOException {
		Log.debug("Opening Connection With: " + url);
		HttpURLConnection connection;
		if (proxy == null)
			connection = (HttpURLConnection) url.openConnection();
		else {
			connection = (HttpURLConnection) proxy.getConnection(url);
		}
		setConfiguration(connection);
		return connection;
	}
	
	private static void setConfiguration(HttpURLConnection connection){
		connection
				.setConnectTimeout(1000 * ConfigurationManager.getInt(CONF_NETWORK_CONNECTION_TO));
		//Google needs this
		connection.addRequestProperty("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
		connection.addRequestProperty("Accept-Language", "en-us");
		connection.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)");
		connection.addRequestProperty("Connection", "Keep-Alive");
	}

	public static String readURL(URLConnection connection,String charset) throws IOException {
		StringBuilder builder = new StringBuilder();
		InputStream input = connection.getInputStream();
		byte[] array = new byte[1024];
		int read;
		while ((read = input.read(array)) > 0) {
			builder.append(new String(array, 0, read, charset));
		}
		input.close();
		return builder.toString();
	}
	
	public static String readURL(URLConnection connection) throws IOException {
		return readURL(connection, "UTF-8");
	}

	public static String readPostURL(HttpURLConnection connection, String post) throws IOException {
		OutputStream out = null;
		DataOutputStream writer = null;
		out = connection.getOutputStream();
		writer = new DataOutputStream(out);
		writer.writeBytes(post);
		writer.flush();
		writer.close();

		if (connection.getResponseCode() != 200) {
			throw new RuntimeException("Invalid HTTP return code");
		}

		StringBuilder builder = new StringBuilder();
		InputStream input = connection.getInputStream();
		byte[] array = new byte[1024];
		int read;
		while ((read = input.read(array)) > 0) {
			builder.append(new String(array, 0, read, "UTF-8"));
		}
		input.close();
		return builder.toString();
	}



	public static Image getImage(URLConnection connection) throws IOException {
		InputStream input = connection.getInputStream();
		return ImageIO.read(input);
	}

	/**
	 * Encodes a string in a format suitable to send a http request
	 * 
	 * @param s
	 * @return
	 */
	public static String encodeString(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}

	public static Proxy getProxy(ProxyBean proxy) throws UnknownHostException, IOException {
		if (proxy == null)
			return null;

		return new Proxy(proxy.getType().equals(ProxyBean.HTTP_PROXY) ? Type.HTTP : Type.SOCKS,
				proxy.getUrl(), proxy.getPort(), proxy.getUser(), proxy.getPassword());
	}

}
