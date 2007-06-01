/**
 * aTunes 1.6.0
 * Copyright (C) 2006-2007 Alex Aranda (fleax) alex.aranda@gmail.com
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

package ext;

import org.jajuk.util.DownloadManager;

import java.net.URL;

public class LyricsService {

	private static final String ARTIST_WILDCARD = "(%ARTIST%)";

	private static final String SONG_WILDCARD = "(%SONG%)";

	private static String baseURL = "http://www.lyrc.com.ar/en/tema1en.php?artist="
			+ ARTIST_WILDCARD + "&songname=" + SONG_WILDCARD;

	private static String suggestionsURL = "http://www.lyrc.com.ar/en/";

	private static String getLyrics(String urlString) {
		try {
			URL url = new URL(urlString);
			// read html return
			String html = new String(DownloadManager.downloadUrl(url), "ISO-8859-1");

			if (html.contains("Suggestions : <br>")) { // More than one
														// posibility, take the
														// first one
				html = html.substring(html.indexOf("Suggestions : <br>"));

				String href = html.substring(html.indexOf("tema1en.php"));
				href = href.substring(0, href.indexOf("\""));

				String auxURL = suggestionsURL.concat(href);

				return getLyrics(auxURL);

			}

			// Remove html before lyrics
			html = html.substring(html.indexOf("</table>") + 8);

			// Remove html after lyrics
			int pPos = html.indexOf("<p>");
			int brPos = html.indexOf("<br>");

			if (pPos == -1)
				pPos = Integer.MAX_VALUE;

			if (brPos == -1)
				brPos = Integer.MAX_VALUE;

			html = html.substring(0, pPos < brPos ? pPos : brPos);

			// Remove <br/>
			html = html.replaceAll("<br />", "");

			// Bad parsing....
			if (html.contains("<head>"))
				return null;

			return html;
		} catch (Exception e) {
			return null;
		}
	}

	public static String getLyrics(String artist, String song) {
		String urlString = baseURL.replace(ARTIST_WILDCARD, encodeString(artist)).replace(
				SONG_WILDCARD, encodeString(song));
		return getLyrics(urlString);
	}

	private static String encodeString(String s) {
		return s.replaceAll(" +", "+");
	}

}
