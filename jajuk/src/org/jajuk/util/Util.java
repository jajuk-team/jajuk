/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA. $Log$
 * Place - Suite 330, Boston, MA 02111-1307, USA. Revision 1.5  2003/10/31 13:05:39  bflorat
 * Place - Suite 330, Boston, MA 02111-1307, USA. 31/10/2003
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * Place - Suite 330, Boston, MA 02111-1307, USA. Revision 1.4  2003/10/26 21:28:49  bflorat
 * Place - Suite 330, Boston, MA 02111-1307, USA. 26/10/2003
 * Place - Suite 330, Boston, MA 02111-1307, USA. Revision
 * 1.3 2003/10/24 15:44:53 bflorat 24/10/2003
 * 
 * Revision 1.2 2003/10/24 12:46:58 sgringoi Add readFile() method
 * 
 * Revision 1.1 2003/10/12 21:08:12 bflorat 12/10/2003
 *  
 */
package org.jajuk.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.ID3v1;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.util.error.JajukException;

/**
 * General use utilities methods
 * 
 * @author bflorat @created 12 oct. 2003
 */
public class Util implements ITechnicalStrings {

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
		return sExt;
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
}
