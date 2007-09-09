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

package ext.services.lyrics;

import org.jajuk.util.DownloadManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class LyricsService {

	private static final String ARTIST_WILDCARD = "(%ARTIST%)";

	private static final String SONG_WILDCARD = "(%SONG%)";

	private static String baseURL = "http://www.lyrc.com.ar/en/tema1en.php?artist="
			+ ARTIST_WILDCARD + "&songname=" + SONG_WILDCARD;

	private static String suggestionsURL = "http://www.lyrc.com.ar/en/";

	private static String getLyrics(String urlString, String artist, String song) {
		try {
			URL url = new URL(urlString);
			// read html return
			String html = DownloadManager.downloadHtml(url, "ISO-8859-1");

			if (html.contains("Suggestions : <br>")) { // More than one
														// posibility, find the
														// best one
				html = html.substring(html.indexOf("Suggestions : <br>"));
				html = html.substring(0, html.indexOf("<br><br"));

				// Find suggestions and add to a map
				HashMap<String, String> suggestions = new HashMap<String, String>();
				while (html.indexOf("href=\"") != -1) {
					// Parse uri from html tag <a href="....
					String uri = html.substring(html.indexOf("href=\"") + 6);
					uri = uri.substring(0, uri.indexOf("\">"));
					// Parse suggestion text font color='white'>TEXT</font>
					String text = html.substring(html.indexOf("'white'>") + 8);
					text = text.substring(0, text.indexOf("</font>"));
					suggestions.put(text, uri);

					// Skip element
					html = html.substring(html.indexOf("</font>") + 11);
				}

				// Get tokens from artist and song names
				ArrayList<String> tokensToFind = new ArrayList<String>();
				StringTokenizer st = new StringTokenizer(artist, " ");
				while (st.hasMoreTokens()) {
					String t = st.nextToken();
					if (validToken(t))
						tokensToFind.add(t);
				}
				st = new StringTokenizer(song, " ");
				while (st.hasMoreTokens()) {
					String t = st.nextToken();
					if (validToken(t))
						tokensToFind.add(t);
				}

				// Now find at map, a string that contains all artist and song
				// tokens. This will be the selected lyric
				for (String suggestion : suggestions.keySet()) {
					boolean matches = true;
					for (String t : tokensToFind) {
						if (!suggestion.toLowerCase().contains(t.toLowerCase())) {
							matches = false;
							break;
						}
					}
					if (matches) {
						// We have found it, build url and call again
						Log.debug("Found suggestion " + suggestion);

						String auxUrl = suggestionsURL.concat(suggestions.get(suggestion));
						return getLyrics(auxUrl, artist, song);
					}
				}

				Log.debug("No suitable suggestion found");
				// If we reach this code, no suggestion was found, so return
				// null
				return "";
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
				return "";

			//Jajuk code
			//Replace HTML codes
			html = html.replaceAll("&#8217;","'");
			html = html.replaceAll("&#8211;","-");
			html = html.replaceAll("\u0092","'");
			html = html.replaceAll("\u009c","oe");
			
			return html;
		} catch (Exception e) {
			return null;
		}
	}

	public static String getLyrics(String artist, String song) {
		String urlString = baseURL.replace(ARTIST_WILDCARD, Util.encodeString(artist)).replace(
				SONG_WILDCARD, Util.encodeString(song));
		return getLyrics(urlString, artist, song);
	}

	/**
	 * Returns if a string is composed only by letters
	 */
	private static boolean validToken(String t) {
		return t.matches("[A-Za-z]+");
	}

}
