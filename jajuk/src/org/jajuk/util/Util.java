/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Revision$
 *
 */
package org.jajuk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;

/**
 * General use utilities methods
 * 
 * @author bflorat @created 12 oct. 2003
 */
public class Util implements ITechnicalStrings {
	
	
	public static final String [] genres = {
		"Blues","Classic Rock","Country","Dance","Disco","Funk","Grunge",
		"Hip-Hop","Jazz","Metal","New Age","Oldies","Other","Pop","R&B",
		"Rap","Reggae","Rock","Techno","Industrial","Alternative","Ska",
		"Death Metal","Pranks","Soundtrack","Euro-Techno","Ambient",
		"Trip-Hop","Vocal","Jazz+Funk","Fusion","Trance","Classical",
		"Instrumental","Acid","House","Game","Sound Clip","Gospel",
		"Noise","AlternRock","Bass","Soul","Punk","Space","Meditative",
		"Instrumental Pop","Instrumental Rock","Ethnic","Gothic",
		"Darkwave","Techno-Industrial","Electronic","Pop-Folk",
		"Eurodance","Dream","Southern Rock","Comedy","Cult","Gangsta",
		"Top 40","Christian Rap","Pop/Funk","Jungle","Native American",
		"Cabaret","New Wave","Psychedelic","Rave","Showtunes","Trailer",
		"Lo-Fi","Tribal","Acid Punk","Acid Jazz","Polka","Retro",
		"Musical","Rock & Roll","Hard Rock","Folk","Folk-Rock",
		"National Folk","Swing","Fast Fusion","Bebob","Latin","Revival",
		"Celtic","Bluegrass","Avantgarde","Gothic Rock",
		"Progressive Rock","Psychedelic Rock","Symphonic Rock",
		"Slow Rock","Big Band","Chorus","Easy Listening","Acoustic",
		"Humour","Speech","Chanson","Opera","Chamber Music","Sonata",
		"Symphony","Booty Brass","Primus","Porn Groove","Satire",
		"Slow Jam","Club","Tango","Samba","Folklore","Ballad",
		"Power Ballad","Rhytmic Soul","Freestyle","Duet","Punk Rock",
		"Drum Solo","Acapella","Euro-House","Dance Hall", "Goa","Drum & Bass","Club-House","Hardcore",
		"Terror","Indie","BritPop","Negerpunk","Polsk Punk","Beat","Christian Gangsta","Heavy Metal",
		"Black Metal","Crossover","Contemporary C","Christian Rock","Merengue","Salsa","Thrash Metal",
		"Anime","JPop","SynthPop"};
	
	/**
	 * No constructor
	 */
	private Util() {
	}

	/**
	 * Get a file extension
	 * 
	 * @param file
	 * @return
	 */
	public static String getExtension(File file) {
		String s = file.getName();
		StringTokenizer st = new StringTokenizer(s, "."); //$NON-NLS-1$
		String sExt = ""; //$NON-NLS-1$
		while (st.hasMoreTokens()) {
			sExt = st.nextToken();
		}
		return sExt.toLowerCase();
	}

	/**
	 * Open a file and return a string buffer with the file content.
	 * 
	 * @param path -
	 *                 File path
	 * @return StringBuffer - File content.
	 * @throws JajukException -
	 *                  Throws a JajukException if a problem occurs during the file
	 *                  access.
	 */
	public static StringBuffer readFile(String path) throws JajukException {
		/*
		 * URL url; try { url = new URL(path); } catch (MalformedURLException e) {
		 * JajukException te = new JajukException("jajuk0004", path, e); throw
		 * te; } URLConnection urlConn; try { urlConn = url.openConnection(); }
		 * catch (IOException e) { JajukException te = new
		 * JajukException("jajuk0005", path, e); throw te; }
		 *  // Use cache if (urlConn != null) { urlConn.setUseCaches(true); }
		 *  // Read BufferedReader input; try { input = new BufferedReader(new
		 * InputStreamReader(urlConn.getInputStream())); } catch (IOException e) {
		 * JajukException te = new JajukException("jajuk0006", path, e); throw
		 * te; }
		 */
		// Read
		File file = new File(path);
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			JajukException te = new JajukException("jajuk0006", path, e);
			throw te;
		}
		BufferedReader input = new BufferedReader(fileReader);

		// Read
		StringBuffer strColl = new StringBuffer();
		String line = null;
		try {
			while ((line = input.readLine()) != null) {
				strColl.append(line);
			}
		} catch (IOException e) {
			JajukException te = new JajukException("jajuk0006", path, e);
			throw te;
		}

		// Close the bufferedReader
		try {
			input.close();
		} catch (IOException e) {
			JajukException te = new JajukException("jajuk0007", path, e);
			throw te;
		}

		return strColl;
	}
	
/**
	 * Format a string before XML write
	 * <p>see http://www.w3.org/TR/2000/REC-xml-20001006
	 * <p> substrings 
	 * <p>' to &apos;
	 * <p>" to &quot;
	 * <p>< to &lt;
	 * <p>> to &gt;
	 * <p>& to &amp;
	 * @param s
	 * @return
	 */
	public static String formatXML(String s){
		String sOut = s.replaceAll("&","&amp;");
		sOut = sOut.replaceAll("\'","&apos;");
		sOut = sOut.replaceAll("\"","&quot;");
		sOut = sOut.replaceAll("<","&lt;");
		sOut = sOut.replaceAll(">","&qt;");
		return sOut;
	}
	
	/**
	 * Performs some cleanups for strings comming from tag libs 
	 * @param s
	 * @return
	 */
	public static String formatTag(String s){
		return s.replace('\u0000',' ').trim();
	}
	
	
	
	/**Return a genre string for a given genre id **/
	public static String getStringGenre(int i){
		if (i>= 0 && i<126){
			return genres[i];
		}
		else{
			return Messages.getString("Track_unknown_style");
		}
	}		
	
}
