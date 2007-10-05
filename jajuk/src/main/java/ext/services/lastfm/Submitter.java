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

package ext.services.lastfm;

import org.jajuk.base.Track;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.log.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ext.services.network.NetworkUtils;

public class Submitter {

	/**
	 * Max num of retries
	 */
	private static final int MAX_RETRIES = 3;

	private static String protocolVersion = "1.1";

	private static String clientId = "atu";

	private static String clientVer = "0.1";

	private static String user;

	private static String password;

	private static String md5Challenge;

	private static String submissionURL;

	private static ext.services.network.Proxy proxy;

	private static void handshake() throws SubmitterException {
		String url = "http://post.audioscrobbler.com/?hs=true&p=" + protocolVersion + "&c="
				+ clientId + "&v=" + clientVer + "&u=" + user;
		try {
			String result = DownloadManager.downloadHtml(new URL(url));
			// Parse result
			String[] lines = result.split("\n");
			if (lines[0].equals("UPTODATE")) {
				md5Challenge = lines[1];
				submissionURL = lines[2];
			} else
				throw new SubmitterException(lines[0]);
		} catch (Exception e) {
			throw new SubmitterException(e.getMessage());
		}
	}

	public static void submitTrack(Track track, long secondsPlayed) throws SubmitterException {
		if (user == null || user.equals("")) {
			Log.debug("Don't submit to Last.fm. Empty user");
			return;
		}

		if (password == null || password.equals("")) {
			Log.debug("Don't submit to Last.fm. Empty password");
			return;
		}

		// Get started to play
		long startedToPlay = System.currentTimeMillis() / 1000 - secondsPlayed;

		// If artist unknown don't submit
		if (track.getAuthor().isUnknown()) {
			Log.debug("Don't submit to Last.fm. Unknown Artist");
			return;
		}

		// If title unknown don't submit
		if (track.getName().trim().equals("")) {
			Log.debug("Don't submit to Last.fm. Unknown Title");
			return;
		}

		// Do not submit tracks under 30 seconds
		if (track.getDuration() < 30) {
			Log.debug("Don't submit to Last.fm. Lenght < 30");
			return;
		}

		Log.info("Submitting song to Last.fm: " + track.getName() + " " + secondsPlayed + " secs");
		submitTrackToLastFm(track, startedToPlay, 1);
	}

	private static void submitTrackToLastFm(Track track, long startedToPlay, int retries)
			throws SubmitterException {
		if (submissionURL == null)
			handshake();

		HttpURLConnection connection;
		String queryString = getQueryString(track, startedToPlay);
		try {
			connection = NetworkUtils.getConnection(submissionURL, proxy);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", new Integer(queryString.length())
					.toString());
			connection.setRequestProperty("Connection", "close");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			String result = NetworkUtils.readPostURL(connection, queryString);

			String[] lines = result.split("\n");
			if (!lines[0].equals("OK")) {
				if (lines[0].equals("BADAUTH")) { // Retry up to MAX_RETRIES
					if (retries == MAX_RETRIES)
						throw new SubmitterException(lines[0]);
					submissionURL = null;
					// Wait one second before retry
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					submitTrackToLastFm(track, startedToPlay, retries + 1);
				}
			}
		} catch (IOException e) {
			throw new SubmitterException(e.getMessage());
		}
	}

	private static String getQueryString(Track track, long startedToPlay) {
		StringBuilder builder = new StringBuilder();
		builder.append("u=" + NetworkUtils.encodeString(user));
		builder.append("&s=" + NetworkUtils.encodeString(getMd5Response()));
		builder.append("&a[0]=" + NetworkUtils.encodeString(track.getAuthor().getName2()));
		builder.append("&t[0]=" + NetworkUtils.encodeString(track.getName()));
		builder.append("&b[0]="
				+ (!track.getAlbum().isUnknown() ? NetworkUtils.encodeString(track.getAlbum()
						.getName2()) : ""));
		builder.append("&m[0]=");
		builder.append("&l[0]=" + NetworkUtils.encodeString(Long.toString(track.getDuration())));

		Date date = new Date(startedToPlay * 1000);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		builder.append("&i[0]=" + NetworkUtils.encodeString(formatter.format(date)));
		return builder.toString();
	}

	private static String getMd5Response() {
		return md5DigestPassword(md5DigestPassword(password) + md5Challenge);
	}

	/**
	 * Encodes a byte array into a hexidecimal String.
	 * 
	 * @param array
	 *            The byte array to encode.
	 * @return A heidecimal String representing the byte array.
	 */
	private static String hexEncode(byte[] array) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
		}

		return sb.toString();
	}

	/**
	 * Creates a MD5 digest String from a given password.
	 * 
	 * @param password
	 *            The password to digest.
	 * @return The MD5 digested password.
	 */
	private static String md5DigestPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");

			return hexEncode(md.digest(password.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("No MD5 algorithm present on the system");
		}
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public static void setPassword(String password) {
		Submitter.password = password;
	}

	/**
	 * @param proxy
	 *            the proxy to set
	 */
	public static void setProxy(ext.services.network.Proxy proxy) {
		Submitter.proxy = proxy;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public static void setUser(String user) {
		Submitter.user = user;
	}

}
