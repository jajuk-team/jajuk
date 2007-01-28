/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
 *  
 *  Methods updateAllUIs(), updateComponentTreeUI() and updateComponentTreeUIO() thanks BigLee in this forum:
 *  http://forum.java.sun.com/thread.jspa?threadID=596251&messageID=3160062
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
 *  $Revision$
 */
package org.jajuk.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.StackItem;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.dj.Ambience;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.IPerspective;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * General use utilities methods
 * 
 * @author Bertrand Florat
 * @created 12 oct. 2003
 */
public class Util implements ITechnicalStrings {

	/* Cursors */
	public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

	public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

	/** Waiting flag for perfs */
	private static boolean bWaiting = false;

	/** Addition date Date format */
	private static SimpleDateFormat sdfAdded = new SimpleDateFormat(ADDITION_DATE_FORMAT);

	/** Default locale format* */
	private static DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale
			.getDefault());

	/** Directory filter used in refresh */
	public static JajukFileFilter dirFilter = new JajukFileFilter(JajukFileFilter.DirectoryFilter
			.getInstance());

	/** File filter used in refresh */
	public static JajukFileFilter fileFilter = new JajukFileFilter(JajukFileFilter.KnownTypeFilter
			.getInstance());

	/** Icons cache */
	private static HashMap<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>(200);

	/** Mplayer exe path */
	private static String sMplayerPath = null;

	/** downdown shadow border */
	private static DropShadowBorder shadowBorder = new DropShadowBorder(Color.BLACK, 0, 5, .5f, 12,
			false, true, true, true);

	/** Generic gradiant* */
	private static BasicGradientPainter grandiant = new BasicGradientPainter(
			BasicGradientPainter.GRAY);

	/**
	 * Genres
	 */
	public static final String[] genres = {
			"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Top 40", "Christian Rap", "Pop/Funk", "Jungle", "Native American", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Cabaret", "New Wave", "Psychedelic", "Rave", "Showtunes", "Trailer", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Progressive Rock", "Psychedelic Rock", "Symphonic Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Humour", "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Symphony", "Booty Brass", "Primus", "Porn Groove", "Satire", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Power Ballad", "Rhytmic Soul", "Freestyle", "Duet", "Punk Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Drum Solo", "Acapella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"Terror", "Indie", "BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"Black Metal", "Crossover", "Contemporary C", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"Anime", "JPop", "SynthPop" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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
		if (st.countTokens() > 1) {
			while (st.hasMoreTokens()) {
				sExt = st.nextToken();
			}
		}
		return sExt.toLowerCase();
	}

	/**
	 * Remove an extension from a file name
	 * 
	 * @param filename
	 * @return filename without extension
	 */
	public static String removeExtension(String sFilename) {
		return sFilename.substring(0, sFilename.lastIndexOf('.'));
	}

	/**
	 * Open a file and return a string buffer with the file content.
	 * 
	 * @param path
	 *            -File path
	 * @return StringBuffer - File content.
	 * @throws JajukException -
	 *             Throws a JajukException if a problem occurs during the file
	 *             access.
	 */
	public static StringBuffer readFile(String path) throws JajukException {
		// Read
		File file = null;
		try {
			new File(path);
		} catch (Exception e) {
			throw new JajukException("009", e); //$NON-NLS-1$
		}
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			JajukException te = new JajukException("009", path, e); //$NON-NLS-1$
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
			JajukException te = new JajukException("009", path, e); //$NON-NLS-1$
			throw te;
		}

		// Close the bufferedReader
		try {
			input.close();
		} catch (IOException e) {
			JajukException te = new JajukException("009", path, e); //$NON-NLS-1$
			throw te;
		}

		return strColl;
	}

	/**
	 * Open a file from current jar and return a string buffer with the file
	 * content.
	 * 
	 * @param sUrl :
	 *            relative file url
	 * @return StringBuffer - File content.
	 * @throws JajukException
	 *             -Throws a JajukException if a problem occurs during the file
	 *             access.
	 */
	public static StringBuffer readJarFile(String sURL) throws JajukException {
		// Read
		InputStream is;
		StringBuffer sb = null;
		try {
			is = Main.class.getResourceAsStream(sURL);
			// Read
			byte[] b = new byte[200];
			sb = new StringBuffer();
			int i = 0;
			do {
				i = is.read(b, 0, b.length);
				sb.append(new String(b));
			} while (i > 0);
			// Close the bufferedReader
			is.close();
		} catch (IOException e) {
			JajukException te = new JajukException("009", e); //$NON-NLS-1$
			throw te;
		}
		return sb;

	}

	/**
	 * Format a string before XML write
	 * <p>
	 * see http://www.w3.org/TR/2000/REC-xml-20001006
	 * <p>
	 * substrings
	 * <p>' to &apos;
	 * <p>" to &quot;
	 * <p>< to &lt;
	 * <p>> to &gt;
	 * <p>& to &amp;
	 * 
	 * @param s
	 * @return
	 */
	public static String formatXML(String s) {
		String sOut = s;
		if (s.contains("&")) { //$NON-NLS-1$
			sOut = sOut.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (s.contains("\'")) { //$NON-NLS-1$
			sOut = sOut.replaceAll("\'", "&apos;"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (s.contains("\"")) { //$NON-NLS-1$
			sOut = sOut.replaceAll("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (s.contains("<")) { //$NON-NLS-1$
			sOut = sOut.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if (s.contains(">")) { //$NON-NLS-1$
			sOut = sOut.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		StringBuffer sbOut = new StringBuffer(sOut.length());
		/*
		 * Transform String to XML-valid characters. XML 1.0 specs ; Character
		 * Range [2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
		 * [#x10000-#x10FFFF] any Unicode character, excluding the surrogate
		 * blocks, FFFE, and FFFF.
		 */
		for (int i = 0; i < sOut.length(); i++) {
			char c = sOut.charAt(i);
			if (isChar(c)) {
				sbOut.append(c);
			}
		}
		return sbOut.toString();
	}

	/**
	 * @param ucs4char
	 *            char to test
	 * @return whether the char is valid, code taken from Apache sax
	 *         implementation
	 */
	public static boolean isChar(int ucs4char) {
		return ucs4char >= 32 && ucs4char <= 55295 || ucs4char == 10 || ucs4char == 9
				|| ucs4char == 13 || ucs4char >= 57344 && ucs4char <= 65533 || ucs4char >= 0x10000
				&& ucs4char <= 0x10ffff;
	}

	/**
	 * @param s
	 * @return whether given string is XML-valid
	 */
	public static boolean isXMLValid(String s) {
		// check reserved chars
		if (s.contains("&") || //$NON-NLS-1$
				s.contains("\'") || //$NON-NLS-1$
				s.contains("\"") || //$NON-NLS-1$
				s.contains("<") || //$NON-NLS-1$
				s.contains(">")) { //$NON-NLS-1$
			return false;
		}
		// check invalid chars
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (!isChar(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Performs some cleanups for strings comming from tag libs
	 * 
	 * @param s
	 * @return
	 */
	public static String formatTag(String s) {
		// we delete all non char characters to avoid parsing errors
		char c;
		StringBuffer sb = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if (isChar(c)) {
				sb.append(c);
			}
		}
		String sOut = sb.toString().trim();
		return sOut;
	}

	/** Return a genre string for a given genre id * */
	public static String getStringGenre(int i) {
		if (i >= 0 && i < 126) {
			return genres[i];
		} else {
			return Messages.getString("unknown_style"); //$NON-NLS-1$
		}
	}

	/** Format a time from secs to a human readable format */
	public static String formatTimeBySec(long lTime, boolean bTrimZeros) {
		long l = lTime;
		if (l == -1) { // means we are in repeat mode
			return "--:--"; //$NON-NLS-1$
		}
		if (l < 0)
			l = 0; // make sure to to get negative values
		long lHours = l / 3600;
		long lMins = l / 60 - (lHours * 60);
		long lSecs = l - (lHours * 3600) - (lMins * 60);
		StringBuffer sbHours = new StringBuffer(Long.toString(lHours));
		if (sbHours.length() == 1 && !bTrimZeros)
			sbHours.insert(0, '0');
		StringBuffer sbMins = new StringBuffer(Long.toString(lMins));
		if (sbMins.length() == 1 && !bTrimZeros)
			sbMins.insert(0, '0');
		StringBuffer sbSecs = new StringBuffer(Long.toString(lSecs));
		if (sbSecs.length() == 1)
			sbSecs.insert(0, '0');

		StringBuffer sbResult = new StringBuffer();
		if (lHours > 0)
			sbResult.append(sbHours).append(":"); //$NON-NLS-1$
		return sbResult.append(sbMins).append(":").append(sbSecs).toString(); //$NON-NLS-1$
	}

	/** Waiting cursor thread, stored to avoid construction */
	private static Thread tWaiting = new Thread() {
		public void run() {
			Container container = null;
			IPerspective perspective = PerspectiveManager.getCurrentPerspective();
			if (perspective != null) {
				container = perspective.getContentPane();
				container.setCursor(WAIT_CURSOR);
				CommandJPanel.getInstance().setCursor(WAIT_CURSOR);
				InformationJPanel.getInstance().setCursor(WAIT_CURSOR);
				PerspectiveBarJPanel.getInstance().setCursor(WAIT_CURSOR);
			}
		}
	};

	/** Default cursor thread, stored to avoid construction */
	private static Thread tDefault = new Thread() {
		public void run() {
			Container container = null;
			IPerspective perspective = PerspectiveManager.getCurrentPerspective();
			if (perspective != null) {
				container = perspective.getContentPane();
				container.setCursor(DEFAULT_CURSOR);
				CommandJPanel.getInstance().setCursor(DEFAULT_CURSOR);
				InformationJPanel.getInstance().setCursor(DEFAULT_CURSOR);
				PerspectiveBarJPanel.getInstance().setCursor(DEFAULT_CURSOR);
			}
		}
	};

	/**
	 * Set current cursor as waiting cursor
	 */
	public static synchronized void waiting() {
		if (!bWaiting) {
			bWaiting = true;
			SwingUtilities.invokeLater(tWaiting);
		}
	}

	/**
	 * Set current cursor as default cursor
	 */
	public static synchronized void stopWaiting() {
		if (bWaiting) {
			bWaiting = false;
			SwingUtilities.invokeLater(tDefault);
		}
	}

	/**
	 * Get required icon or image with specified url
	 * 
	 * @param sURL
	 * @return the image
	 */
	public static ImageIcon getIcon(URL url) {
		ImageIcon ii = null;
		String sURL = url.toString();
		try {
			if (iconCache.containsKey(sURL)) {
				ii = iconCache.get(sURL);
			} else {
				ii = new ImageIcon(url);
				iconCache.put(sURL, ii);
			}

		} catch (Exception e) {
			Log.error(e);
		}
		return ii;
	}

	/**
	 * Save a file in the same directory with name <filename>_YYYYmmddHHMM.xml
	 * and with a given maximum Mb size for the file and its backup files
	 * 
	 * @param file
	 */
	public static void backupFile(File file, int iMB) {
		try {
			if (Integer.parseInt(ConfigurationManager.getProperty(CONF_BACKUP_SIZE)) <= 0) { // 0 or
				// less
				// means
				// no backup
				return;
			}
			// calculates total size in MB for the file to backup and its
			// backup
			// files
			long lUsedMB = 0;
			ArrayList<File> alFiles = new ArrayList<File>(10);
			File[] files = new File(file.getAbsolutePath()).getParentFile().listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().indexOf(removeExtension(file.getName())) != -1) {
						lUsedMB += files[i].length();
						alFiles.add(files[i]);
					}
				}
				// sort found files
				alFiles.remove(file);
				Collections.sort(alFiles);
				if ((lUsedMB - file.length()) / 1048576 > iMB) { // too
					// much
					// backup
					// files,
					// delete
					// older
					if (alFiles.size() > 0) {
						File fileToDelete = alFiles.get(0);
						if (fileToDelete != null) {
							fileToDelete.delete();
						}
					}
				}
			}
			// backup itself using nio, file name is
			// collection-backup-yyyMMdd.xml
			String sExt = new SimpleDateFormat("yyyyMMdd").format(new Date()); //$NON-NLS-1$
			File fileNew = new File(Util.removeExtension(file.getAbsolutePath())
					+ "-backup-" + sExt + "." + Util.getExtension(file)); //$NON-NLS-1$//$NON-NLS-2$
			FileChannel fcSrc = new FileInputStream(file).getChannel();
			FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
			fcDest.transferFrom(fcSrc, 0, fcSrc.size());
			fcSrc.close();
			fcDest.close();
		} catch (IOException ie) {
			Log.error(ie);
		}
	}

	/**
	 * Create empty file
	 * 
	 * @param sFullPath
	 * @throws Exception
	 */
	public static void createEmptyFile(String sFullPath) throws IOException {
		File file = new File(sFullPath);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(new byte[0]);
		fos.close();
	}

	/**
	 * Copy a file to given directory
	 * 
	 * @param file :
	 *            file to copy
	 * @param directory :
	 *            destination directory
	 */
	public static void copyToDir(File file, File directory) throws Exception {
		Log.debug("Copying: " + file.getAbsolutePath() + "  to : " + directory.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
		File fileNew = new File(new StringBuffer(directory.getAbsolutePath())
				.append("/").append(file.getName()).toString()); //$NON-NLS-1$
		if (!file.exists() || !file.canRead()) {
			throw new JajukException("009", file.getAbsolutePath(), null); //$NON-NLS-1$
		}
		if (!fileNew.getParentFile().canWrite()) {
			throw new JajukException("024", file.getAbsolutePath(), null); //$NON-NLS-1$
		}
		FileChannel fcSrc = new FileInputStream(file).getChannel();
		FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
		fcDest.transferFrom(fcSrc, 0, fcSrc.size());
		fcSrc.close();
		fcDest.close();
	}

	/**
	 * Copy a file to another file
	 * 
	 * @param file :
	 *            file to copy
	 * @param fNew :
	 *            destination file
	 */
	public static void copy(File file, File fNew) throws Exception {
		Log.debug("Copying: " + file.getAbsolutePath() + "  to : " + fNew.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
		if (!file.exists() || !file.canRead()) {
			throw new JajukException("009", file.getAbsolutePath(), null); //$NON-NLS-1$
		}
		if (!fNew.getParentFile().canWrite()) {
			throw new JajukException("024", file.getAbsolutePath(), null); //$NON-NLS-1$
		}
		FileChannel fcSrc = new FileInputStream(file).getChannel();
		FileChannel fcDest = new FileOutputStream(fNew).getChannel();
		fcDest.transferFrom(fcSrc, 0, fcSrc.size());
		fcSrc.close();
		fcDest.close();
	}

	/**
	 * Rename a file
	 * 
	 * @param file :
	 *            file to rename
	 * @param sNewName :
	 *            file new name
	 */
	public static void copy(File file, String sNewName) throws Exception {
		Log.debug("Renaming: " + file.getAbsolutePath() + "  to : " + sNewName); //$NON-NLS-1$ //$NON-NLS-2$
		File fileNew = new File(new StringBuffer(file.getParentFile().getAbsolutePath())
				.append('/').append(sNewName).toString()); //$NON-NLS-1$
		if (!file.exists() || !file.canRead()) {
			throw new JajukException("009", file.getAbsolutePath(), null); //$NON-NLS-1$
		}
		if (!fileNew.getParentFile().canWrite()) {
			throw new JajukException("024", file.getAbsolutePath(), null); //$NON-NLS-1$
		}
		FileChannel fcSrc = new FileInputStream(file).getChannel();
		FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
		fcDest.transferFrom(fcSrc, 0, fcSrc.size());
		fcSrc.close();
		fcDest.close();
	}

	/**
	 * @param sFileName
	 * @return whether the given filename is a standard cover or not
	 */
	public static boolean isStandardCover(String sFileName) {
		return sFileName.toLowerCase().matches(".*" + FILE_DEFAULT_COVER + ".*") //$NON-NLS-1$ //$NON-NLS-2$
				|| sFileName.toLowerCase().matches(".*" + FILE_DEFAULT_COVER_2 + ".*") //$NON-NLS-1$ //$NON-NLS-2$
				|| sFileName.toLowerCase().matches(".*" + FILE_ABSOLUTE_DEFAULT_COVER + ".*"); // just
		// for
		// //$NON-NLS-1$
		// //$NON-NLS-2$
		// previous
		// compatibility,
		// now it is
		// a
		// directory
		// property
		// //$NON-NLS-1$
		// //$NON-NLS-2$
	}

	/**
	 * Tell whether a file is an absolute default cover or not
	 * 
	 * @param directory
	 *            Jajuk Directory in which we analyse the given file name
	 * @param sFileName
	 * @return whether the given filename is an absolute default cover
	 */
	public static boolean isAbsoluteDefaultCover(Directory directory, String sFilename) {
		String sDefault = directory.getStringValue(XML_DIRECTORY_DEFAULT_COVER); //$NON-NLS-1$
		if (sDefault != null && sDefault.equals(sFilename)) {
			return true;
		}
		return false;
	}

	/**
	 * Return url of jar we are executing
	 * 
	 * @return URL of jar we are executing
	 */
	public static URL getJarLocation(Class cClass) {
		return cClass.getProtectionDomain().getCodeSource().getLocation();
	}

	/**
	 * Additional file checkusm used to prevent bug 886098. Simply return some
	 * bytes read at the middle of the file
	 * <p>
	 * uses nio api for performances
	 * 
	 * @return
	 */
	public static String getFileChecksum(File fio) throws JajukException {
		try {
			String sOut = ""; //$NON-NLS-1$
			FileChannel fc = new FileInputStream(fio).getChannel();
			ByteBuffer bb = ByteBuffer.allocate(500);
			fc.read(bb, fio.length() / 2);
			fc.close();
			sOut = new String(bb.array());
			return MD5Processor.hash(sOut);
		} catch (Exception e) {
			throw new JajukException("103", e); //$NON-NLS-1$
		}
	}

	/**
	 * Return only the name of a file from a complete URL
	 * 
	 * @param sPath
	 * @return
	 */
	public static String getOnlyFile(String sPath) {
		return new File(sPath).getName();
	}

	/**
	 * @param url
	 *            ressource URL
	 * @return Cache PATH
	 */
	public static String getCachePath(URL url) {
		return FILE_IMAGE_CACHE + '/' + Util.getOnlyFile(url.toString());
	}

	/**
	 * Clear locale images cache
	 */
	public static void clearCache() {
		File fCache = new File(FILE_IMAGE_CACHE);
		File[] files = fCache.listFiles();
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}

	/**
	 * @return whether we are under Windows
	 */
	public static boolean isUnderWindows() {
		String sOS = (String) System.getProperties().get("os.name"); //$NON-NLS-1$;
		// os.name can be null with JWS under MacOS
		if (sOS != null && sOS.trim().toLowerCase().lastIndexOf("windows") != -1) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * @return whether we are under Linux
	 */
	public static boolean isUnderLinux() {
		String sOS = (String) System.getProperties().get("os.name"); //$NON-NLS-1$;
		// os.name can be null with JWS under MacOS
		if (sOS != null && sOS.trim().toLowerCase().lastIndexOf("linux") != -1) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * Try to compute time length in milliseconds using BasicPlayer API. (code
	 * from jlGui 2.3)
	 */
	public static long getTimeLengthEstimation(Map properties) {
		long milliseconds = -1;
		int byteslength = -1;
		if (properties != null) {
			if (properties.containsKey("audio.length.bytes")) //$NON-NLS-1$
			{
				byteslength = ((Integer) properties.get("audio.length.bytes")).intValue(); //$NON-NLS-1$
			}
			if (properties.containsKey("duration")) //$NON-NLS-1$
			{
				milliseconds = (((Long) properties.get("duration")).longValue()) / 1000; //$NON-NLS-1$
			} else {
				// Try to compute duration
				int bitspersample = -1;
				int channels = -1;
				float samplerate = -1.0f;
				int framesize = -1;
				if (properties.containsKey("audio.samplesize.bits")) //$NON-NLS-1$
				{
					bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue(); //$NON-NLS-1$
				}
				if (properties.containsKey("audio.channels")) //$NON-NLS-1$
				{
					channels = ((Integer) properties.get("audio.channels")).intValue(); //$NON-NLS-1$
				}
				if (properties.containsKey("audio.samplerate.hz")) //$NON-NLS-1$
				{
					samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue(); //$NON-NLS-1$
				}
				if (properties.containsKey("audio.framesize.bytes")) //$NON-NLS-1$
				{
					framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue(); //$NON-NLS-1$
				}
				if (bitspersample > 0) {
					milliseconds = (int) (1000.0f * byteslength / (samplerate * channels * (bitspersample / 8)));
				} else {
					milliseconds = (int) (1000.0f * byteslength / (samplerate * framesize));
				}
			}
		}
		return milliseconds;
	}

	/**
	 * @param jc
	 * @param iOrientation :
	 *            vertical or horizontal orientation, use BoxLayout.X_AXIS or
	 *            BoxLayout.Y_AXIS
	 * @return a centred panel
	 */
	public static JPanel getCentredPanel(JComponent jc, int iOrientation) {
		JPanel jpOut = new JPanel();
		jpOut.setLayout(new BoxLayout(jpOut, iOrientation));
		if (iOrientation == BoxLayout.X_AXIS) {
			jpOut.add(Box.createHorizontalGlue());
			jpOut.add(jc);
			jpOut.add(Box.createHorizontalGlue());
		} else {
			jpOut.add(Box.createVerticalGlue());
			jpOut.add(jc);
			jpOut.add(Box.createVerticalGlue());
		}
		jpOut.setMinimumSize(new Dimension(0, 0)); // allow resing with
		// info
		// node
		return jpOut;
	}

	/**
	 * @param jc
	 * @return an horizontaly centred panel
	 */
	public static JPanel getCentredPanel(JComponent jc) {
		return getCentredPanel(jc, BoxLayout.X_AXIS);
	}

	/**
	 * @param file1
	 * @param file2
	 * @return whether file1 is a file2 descendant
	 */
	public static boolean isDescendant(File file1, File file2) {
		File fParent = file1.getParentFile();
		boolean bOut = false;
		while (fParent != null) {
			if (fParent.equals(file2)) {
				bOut = true;
				break;
			}
			fParent = fParent.getParentFile();
		}
		return bOut;
	}

	/**
	 * @param file1
	 * @param file2
	 * @return whether file1 is a file2 ancestor
	 */
	public static boolean isAncestor(File file1, File file2) {
		File fParent = file2.getParentFile();
		boolean bOut = false;
		while (fParent != null) {
			if (fParent.equals(file1)) {
				bOut = true;
				break;
			}
			fParent = fParent.getParentFile();
		}
		return bOut;
	}

	/**
	 * @param alFiles
	 * @return Given list to play with shuffle or others runles applied
	 */
	@SuppressWarnings("unchecked")
	public static List<org.jajuk.base.File> applyPlayOption(List<org.jajuk.base.File> alFiles) {
		if (ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE)) {
			List<org.jajuk.base.File> alFilesToPlay = (List<org.jajuk.base.File>) ((ArrayList<org.jajuk.base.File>) alFiles)
					.clone();
			Collections.shuffle(alFilesToPlay, new Random());
			return alFilesToPlay;
		}
		return alFiles;
	}

	/**
	 * Convert a list of files into a list of StackItem
	 * 
	 * @param alFiles
	 * @param bRepeat
	 * @param bUserLauched
	 * @return
	 */
	public static List<StackItem> createStackItems(List<org.jajuk.base.File> alFiles,
			boolean bRepeat, boolean bUserLauched) {
		ArrayList<StackItem> alOut = new ArrayList<StackItem>(alFiles.size());
		Iterator it = alFiles.iterator();
		while (it.hasNext()) {
			org.jajuk.base.File file = (org.jajuk.base.File) it.next();
			try {
				StackItem item = new StackItem(file);
				item.setRepeat(bRepeat);
				item.setUserLaunch(bUserLauched);
				alOut.add(item);
			} catch (JajukException je) {
				Log.error(je);
			}
		}
		return alOut;
	}

	/**
	 * Resize an image
	 * 
	 * @param img
	 *            image to resize
	 * @param iNewWidth
	 * @param iNewHeight
	 * @return resized image
	 */
	public static ImageIcon getResizedImage(ImageIcon img, int iNewWidth, int iNewHeight) {
		ImageIcon iiNew = new ImageIcon();
		Image image = img.getImage();
		Image scaleImg = image.getScaledInstance(iNewWidth, iNewHeight, Image.SCALE_AREA_AVERAGING);
		iiNew.setImage(scaleImg);
		return iiNew;
	}

	/**
	 * Transform an image to a BufferedImage
	 * <p>
	 * Thanks http://java.developpez.com/faq/java/?page=graphique_general_images
	 * </p>
	 * 
	 * @param image
	 * @return buffured image from an image
	 */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return ((BufferedImage) image);
		} else {
			/** Make sure image is fullt loaded */
			Image image2 = new ImageIcon(image).getImage();
			/** Create the new image */
			BufferedImage bufferedImage = new BufferedImage(image2.getWidth(null), image
					.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics g = bufferedImage.createGraphics();
			g.drawImage(image2, 0, 0, null);
			g.dispose();
			return (bufferedImage);
		}
	}

	/**
	 * @param img
	 * @param iScale
	 * @return a scaled image
	 */
	public static ImageIcon getScaledImage(ImageIcon img, int iScale) {
		int iNewWidth;
		int iNewHeight;
		// Height is smaller or equal than width : try to optimize width
		iNewWidth = iScale; // take all possible width
		// we check now if height will be visible entirely with optimized width
		float fWidthRatio = (float) iNewWidth / img.getIconWidth();
		if (img.getIconHeight() * (fWidthRatio) <= iScale) {
			iNewHeight = (int) (img.getIconHeight() * fWidthRatio);
		} else {
			// no? so we optimize width
			iNewHeight = iScale;
			iNewWidth = (int) (img.getIconWidth() * ((float) iNewHeight / img.getIconHeight()));
		}
		return getResizedImage(img, iNewWidth, iNewHeight);
	}

	/**
	 * Method to attempt a dynamic update for any GUI accessible by this JVM. It
	 * will filter through all frames and sub-components of the frames.
	 */
	public static void updateAllUIs() {
		Frame frames[];
		frames = Frame.getFrames();

		for (int i = 0; i < frames.length; i++) {
			updateWindowUI(frames[i]);
		}
		// update tray
		// DO NOT SET A LAF, for unknwon reason, with some LAF (plastic x) and
		// under linux, it causes an audio line blocking, to be investigated
		// updateComponentTreeUI(JajukSystray.getInstance().getPopup());

	}

	/**
	 * Method to attempt a dynamic update for all components of the given
	 * <code>Window</code>.
	 * 
	 * @param window
	 *            The <code>Window</code> for which the look and feel update
	 *            has to be performed against.
	 */
	public static void updateWindowUI(Window window) {
		try {
			updateComponentTreeUI(window);
		} catch (Exception exception) {
		}

		Window windows[] = window.getOwnedWindows();

		for (int i = 0; i < windows.length; i++)
			updateWindowUI(windows[i]);
	}

	/**
	 * A simple minded look and feel change: ask each node in the tree to
	 * <code>updateUI()</code> -- that is, to initialize its UI property with
	 * the current look and feel. Based on the Sun
	 * SwingUtilities.updateComponentTreeUI, but ensures that the update happens
	 * on the components of a JToolbar before the JToolbar itself.
	 */
	public static void updateComponentTreeUI(Component c) {
		updateComponentTreeUI0(c);
		c.invalidate();
		c.validate();
		c.repaint();
	}

	private static void updateComponentTreeUI0(Component c) {

		Component[] children = null;

		if (c instanceof JToolBar) {
			children = ((JToolBar) c).getComponents();

			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					updateComponentTreeUI0(children[i]);
				}
			}

			((JComponent) c).updateUI();
		} else {
			if (c instanceof JComponent) {
				((JComponent) c).updateUI();
			}

			if (c instanceof JMenu) {
				children = ((JMenu) c).getMenuComponents();
			} else if (c instanceof Container) {
				children = ((Container) c).getComponents();
			}

			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					updateComponentTreeUI0(children[i]);
				}
			}
		}
	}

	/**
	 * Formater for properties dialog window
	 * 
	 * @param sDesc
	 * @return
	 */
	public static String formatPropertyDesc(String sDesc) {
		return "<HTML><center><b><font size=+0 color=#000000>" + //$NON-NLS-1$
				sDesc + "</font></b><HTML>"; //$NON-NLS-1$
	}

	/**
	 * @return Addition date simple format singleton
	 */
	public static DateFormat getAdditionDateFormat() {
		return sdfAdded;
	}

	/**
	 * Display given container at given position
	 * 
	 * @param container
	 * @param iFromTop
	 *            max number of pixels from top
	 * @param iFromLeft
	 *            max number of pixels from left
	 */
	public static void setShuffleLocation(Window window, int iFromTop, int iFromLeft) {
		window.setLocation((int) (Math.random() * iFromTop), (int) (Math.random() * iFromLeft));
	}

	/**
	 * Parse a string to an object
	 * 
	 * @param sValue
	 * @param cType
	 * @return parsed item
	 * @throws Exception
	 */
	public static Object parse(String sValue, Class cType) throws Exception {
		Object oDefaultValue = sValue; // String by default
		if (cType.equals(Boolean.class)) {
			// "y" and "n" is an old boolean
			// attribute notation prior to 1.0
			if (sValue.equals("y")) {
				oDefaultValue = true;
			} else if (sValue.equals("n")) {
				oDefaultValue = false;
			} else {
				oDefaultValue = Boolean.parseBoolean(sValue);
			}
		} else if (cType.equals(Date.class)) {
			oDefaultValue = getAdditionDateFormat().parseObject(sValue);
		} else if (cType.equals(Long.class)) {
			oDefaultValue = Long.parseLong(sValue);
		} else if (cType.equals(Double.class)) {
			oDefaultValue = Double.parseDouble(sValue);
		} else if (cType.equals(Class.class)) {
			oDefaultValue = Class.forName(sValue);
		}
		return oDefaultValue;
	}

	/**
	 * Format an object to a string.
	 * 
	 * @param sValue
	 * @param cType
	 * @return
	 * @throws Exception
	 */
	public static String format(Object oValue, PropertyMetaInformation meta) throws Exception {
		Class cType = meta.getType();
		// default (works for strings, long and double)
		String sValue = oValue.toString();
		if (cType.equals(Date.class)) {
			sValue = getAdditionDateFormat().format(oValue);
		} else if (cType.equals(Class.class)) {
			sValue = oValue.getClass().getName();
		}
		return sValue;
	}

	/**
	 * format style: first letter uppercase and others lowercase
	 * 
	 * @param style
	 * @return
	 */
	public static String formatStyle(String style) {
		if (style.length() == 0) {
			return ""; //$NON-NLS-1$
		}
		if (style.length() == 1) {
			return style.substring(0, 1).toUpperCase();
		}
		String sOut = style.toLowerCase().substring(1);
		sOut = style.substring(0, 1).toUpperCase() + sOut;
		return sOut;
	}

	/**
	 * Reads an image in a file and creates a thumbnail in another file. Will be
	 * created if necessary. the thumbnail must be maxDim pixels or less. Thanks
	 * Marco Schmidt
	 * http://schmidt.devlib.org/java/save-jpeg-thumbnail.html#source
	 * 
	 * @param orig
	 *            source image
	 * @param thumb
	 *            destination file (jpg)
	 * @param maxDim
	 *            required size
	 * @throws Exception
	 */
	public static void createThumbnail(File orig, File thumb, int maxDim) throws Exception {
		/*
		 * do not use URL object has it can corrupt special paths
		 */
		createThumbnail(new ImageIcon(orig.getAbsolutePath()), thumb, maxDim);
	}

	/**
	 * Display a given image in a frame (for debuging purpose)
	 * 
	 * @param ii
	 */
	public static void displayImage(ImageIcon ii) {
		JFrame jf = new JFrame();
		jf.add(new JLabel(ii));
		jf.pack();
		jf.setVisible(true);
	}

	/**
	 * Reads an image in a file and creates a thumbnail in another file. Use
	 * this method to get thumbs from images inside jar files, some bugs in URL
	 * encoding makes impossible to create the image from a file. Will be
	 * created if necessary. the thumbnail must be maxDim pixels or less. Thanks
	 * Marco Schmidt
	 * http://schmidt.devlib.org/java/save-jpeg-thumbnail.html#source
	 * 
	 * @param orig
	 *            source image
	 * @param thumb
	 *            destination file (jpg)
	 * @param maxDim
	 *            required size
	 * @throws Exception
	 */
	public static void createThumbnail(ImageIcon ii, File thumb, int maxDim) throws Exception {
		Image image = ii.getImage();
		MediaTracker mediaTracker = new MediaTracker(new Container());
		mediaTracker.addImage(image, 0);
		mediaTracker.waitForID(0);
		// determine thumbnail size from WIDTH and HEIGHT
		int thumbWidth = maxDim;
		int thumbHeight = maxDim;
		double thumbRatio = (double) thumbWidth / (double) thumbHeight;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		double imageRatio = (double) imageWidth / (double) imageHeight;
		if (thumbRatio < imageRatio) {
			thumbHeight = (int) (thumbWidth / imageRatio);
		} else {
			thumbWidth = (int) (thumbHeight * imageRatio);
		}
		// draw original image to thumbnail image object and
		// scale it to the new size on-the-fly
		BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);
		// save thumbnail image to OUTFILE
		BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(thumb));
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
		int quality = 100;
		quality = Math.max(0, Math.min(quality, 100));
		param.setQuality(quality / 100.0f, false);
		encoder.setJPEGEncodeParam(param);
		encoder.encode(thumbImage);
		out.close();
		image.flush(); // free memory
	}

	/**
	 * @return whether we need a full gc or not
	 */
	public static boolean needFullFC() {
		float fTotal = Runtime.getRuntime().totalMemory();
		float fFree = Runtime.getRuntime().freeMemory();
		float fLevel = (fTotal - fFree) / fTotal;
		return fLevel >= NEED_FULL_GC_LEVEL;
	}

	/**
	 * @return Anonymized System properties (for log or quality agent)
	 */
	public static Properties getAnonymizedSystemProperties() {
		Properties properties = (Properties) System.getProperties().clone();
		// We remove sensible data from logs
		/*
		 * can contain external program paths
		 */
		properties.remove("java.library.path");
		properties.remove("java.class.path");
		// user name is private
		properties.remove("user.name");//$NON-NLS-1$ 
		properties.remove("java.ext.dirs");
		properties.remove("sun.boot.class.path");
		properties.remove("deployment.user.security.trusted.certs"); //$NON-NLS-1$
		properties.remove("deployment.user.security.trusted.clientauthcerts"); //$NON-NLS-1$
		properties.remove("jajuk.log"); //$NON-NLS-1$

		return properties;
	}

	/**
	 * @return Anonymized Jajuk properties (for log or quality agent)
	 */
	public static Properties getAnonymizedJajukProperties() {
		Properties properties = (Properties) ConfigurationManager.getProperties().clone();
		// We remove sensible data from logs
		properties.remove("jajuk.network.proxy_login"); //$NON-NLS-1$
		properties.remove("jajuk.network.proxy_port"); //$NON-NLS-1$
		properties.remove("jajuk.network.proxy_hostname"); //$NON-NLS-1$
		properties.remove("jajuk.options.p2p.password"); //$NON-NLS-1$
		return properties;
	}

	/**
	 * @param parent
	 *            parent directory
	 * @param name
	 *            file name
	 * @return whether the file name is correct on the current filesystem
	 */
	public static boolean isValidFileName(File parent, String name) {
		// General tests
		if (parent == null || name == null) {
			return false;
		}
		// only digits or letters, OK, no need to test
		if (!Util.containsNonDigitOrLetters(name)) {
			return true;
		}
		File f = new File(parent, name);
		if (!f.exists()) {
			try {
				// try to create the file
				f.createNewFile();
				// test if the file is seen into the directory
				File[] files = parent.listFiles();
				boolean b = false;
				for (int i = 0; i < files.length; i++) {
					if (files[i].getName().equals(name)) {
						b = true;
						break;
					}
				}
				// remove test file
				if (f.exists()) {
					f.delete();
				}
				return b;
			} catch (IOException ioe) {
				return false;
			}
		} else { // file already exists
			return true;
		}
	}

	/**
	 * @param s
	 *            String to analyse
	 * @return whether the given string contains non digit or letters
	 *         chararcters
	 */
	public static boolean containsNonDigitOrLetters(String s) {
		boolean bOK = false;
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isLetterOrDigit(s.charAt(i))) {
				bOK = true;
				break;
			}
		}
		return bOK;
	}

	/**
	 * @param col
	 * @return a single shuffle element from a list, null if none element in
	 *         provided collection
	 */
	public static Object getShuffleItem(Collection<? extends Object> col) {
		if (col.size() == 0) {
			return null;
		}
		List list = null;
		if (col instanceof List) {
			list = (List<? extends Object>) col;
		} else {
			list = new ArrayList<Object>(col);
		}
		return list.get((int) (Math.random() * list.size()));
	}

	/**
	 * Computes file selection from any item
	 * 
	 * @param oData
	 *            the item (a directory, a file...)
	 * @return the files
	 */
	public static ArrayList<org.jajuk.base.File> getFilesFromSelection(Item oData) {
		// computes selection
		ArrayList<org.jajuk.base.File> alSelectedFiles = new ArrayList<org.jajuk.base.File>(100);
		// computes logical selection if any
		Set<Track> alLogicalTracks = null;
		if (oData instanceof Style || oData instanceof Author || oData instanceof Album
				|| oData instanceof Track) {
			if (oData instanceof Style || oData instanceof Author || oData instanceof Album) {
				alLogicalTracks = TrackManager.getInstance().getAssociatedTracks(oData);
			} else if (oData instanceof Track) {
				alLogicalTracks = new LinkedHashSet<Track>(100);
				alLogicalTracks.add((Track) oData);
			}
			// prepare files
			if (alLogicalTracks != null && alLogicalTracks.size() > 0) {
				Iterator it = alLogicalTracks.iterator();
				while (it.hasNext()) {
					Track track = (Track) it.next();
					org.jajuk.base.File file = track.getPlayeableFile(false);
					if (file == null) { // none mounted file for this track
						continue;
					}
					alSelectedFiles.add(file);
				}
			}
		}
		// computes physical selection if any
		else if (oData instanceof org.jajuk.base.File || oData instanceof Directory
				|| oData instanceof Device) {
			if (oData instanceof org.jajuk.base.File) {
				alSelectedFiles.add((org.jajuk.base.File) oData);
			} else if (oData instanceof Directory) {
				alSelectedFiles = ((Directory) oData).getFilesRecursively();
			} else if (oData instanceof Device) {
				alSelectedFiles = ((Device) oData).getFilesRecursively();
			}
		}
		return alSelectedFiles;
	}

	/**
	 * Filter a given file list by ambience
	 * 
	 * @param al
	 *            file list
	 * @param ambience
	 *            ambience
	 * @return the list filtered
	 */
	public static List<org.jajuk.base.File> filterByAmbience(List<org.jajuk.base.File> al,
			Ambience ambience) {
		// Void filter, return the input
		if (ambience == null || ambience.getStyles().size() == 0) {
			return al;
		}
		// Filter by ambience
		ArrayList<org.jajuk.base.File> out = new ArrayList<org.jajuk.base.File>(al.size() / 2);
		for (org.jajuk.base.File file : al) {
			if (ambience.getStyles().contains(file.getTrack().getStyle())) {
				out.add(file);
			}
		}
		return out;
	}

	/**
	 * @return locale date formatter
	 */
	public static DateFormat getLocaleDateFormatter() {
		return dateFormatter;
	}

	/**
	 * code from
	 * http://java.sun.com/developer/onlineTraining/new2java/supplements/2005/July05.html#1
	 * Used to correctly display long messages
	 * 
	 * @param maxCharactersPerLineCount
	 * @return
	 */
	public static JOptionPane getNarrowOptionPane(int maxCharactersPerLineCount) {
		// Our inner class definition
		class NarrowOptionPane extends JOptionPane {
			private static final long serialVersionUID = 1L;

			int maxCharactersPerLineCount;

			NarrowOptionPane(int maxCharactersPerLineCount) {
				this.maxCharactersPerLineCount = maxCharactersPerLineCount;
			}

			public int getMaxCharactersPerLineCount() {
				return maxCharactersPerLineCount;
			}
		}
		return new NarrowOptionPane(maxCharactersPerLineCount);
	}

	/**
	 * Resource loading is done this way to meet the requirements for Web Start.
	 * http://java.sun.com/j2se/1.5.0/docs/guide/javaws/developersguide/faq.html#211
	 */
	public static URL getResource(String name) {
		return Thread.currentThread().getContextClassLoader().getResource(name);
	}

	/**
	 * @return MPLayer exe path
	 */
	public static String getMPlayerPath() {
		// Use cache
		if (sMplayerPath != null) {
			return sMplayerPath;
		}
		File file = null;
		// Check in ~/.jajuk directory (used by .exe or .jar distribution
		// installers)
		if ((file = new java.io.File(FILE_JAJUK_DIR + "/" + FILE_MPLAYER_EXE)).exists()) {
			sMplayerPath = file.getAbsolutePath();
			return sMplayerPath;
		} else {
			// Check in the path where jajuk.jar is executed
			String sPATH = null;
			try {
				// Extract file name from URL. URI returns jar path, its parent
				// is the bin directory and the right dir is the parent of bin
				// dir
				sPATH = new File(getJarLocation(Main.class).toURI()).getParentFile()
						.getParentFile().getAbsolutePath();
				// Add MPlayer file name
				if ((file = new File(sPATH + '/' + FILE_MPLAYER_EXE)).exists()) {
					sMplayerPath = file.getAbsolutePath();
				}
			} catch (Exception e) {
				return sMplayerPath;
			}
			return sMplayerPath;
		}
	}

	/**
	 * Apply a pattern
	 * 
	 * @param file
	 *            file on whish to apply pattern
	 * @param sPattern
	 * @param bMandatory
	 *            are all needed tags mandatory ?
	 * @return computed string
	 * @throws JajukException
	 *             if some tags are missing
	 */
	public static String applyPattern(org.jajuk.base.File file, String sPattern, boolean bMandatory)
			throws JajukException {
		String out = sPattern;
		Track track = file.getTrack();
		String sValue = null;
		// Check Author name
		if (sPattern.contains(PATTERN_ARTIST)) {
			sValue = track.getAuthor().getName().replace("[/\\:]", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			sValue = sValue.trim();
			if (!sValue.equals(UNKNOWN_AUTHOR)) { //$NON-NLS-1$
				out = out.replace(PATTERN_ARTIST, AuthorManager.format(sValue));
			} else {
				if (bMandatory) {
					throw new JajukException(file.getAbsolutePath() + " (" //$NON-NLS-1$
							+ Messages.getString("Error.150") + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					out = out.replace(PATTERN_ARTIST, Messages.getString(UNKNOWN_AUTHOR));
				}
			}
		}
		// Check Style name
		if (sPattern.contains(PATTERN_GENRE)) {
			sValue = track.getStyle().getName().replace("[/\\:]", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			sValue = sValue.trim();
			if (!sValue.equals(UNKNOWN_STYLE)) { //$NON-NLS-1$
				out = out.replace(PATTERN_GENRE, StyleManager.format(sValue));
			} else {
				if (bMandatory) {
					throw new JajukException(file.getAbsolutePath() + " (" //$NON-NLS-1$
							+ Messages.getString("Error.153") + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					out = out.replace(PATTERN_GENRE, Messages.getString(UNKNOWN_STYLE));
				}
			}
		}
		// Check Album Name
		if (sPattern.contains(PATTERN_ALBUM)) {
			sValue = track.getAlbum().getName().replace("[/\\:]", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			sValue = sValue.trim();
			if (!sValue.equals(UNKNOWN_ALBUM)) { //$NON-NLS-1$
				out = out.replace(PATTERN_ALBUM, AlbumManager.format(sValue));
			} else {
				if (bMandatory) {
					throw new JajukException(file.getAbsolutePath() + " (" //$NON-NLS-1$
							+ Messages.getString("Error.149") + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					out = out.replace(PATTERN_ALBUM, Messages.getString(UNKNOWN_ALBUM));
				}
			}
		}
		// Check Track Order
		if (sPattern.contains(PATTERN_TRACKORDER)) {
			long lOrder = track.getOrder();
			if (lOrder == 0) {
				String sFilename = file.getName();
				if (Character.isDigit(sFilename.charAt(0))) {
					String sTo = file.getName().substring(0, 3).trim().replaceAll("[^0-9]", ""); //$NON-NLS-1$ //$NON-NLS-2$
					for (char c : sTo.toCharArray()) {
						if (!Character.isDigit(c)) {
							throw new JajukException(file.getAbsolutePath() + " (" //$NON-NLS-1$
									+ Messages.getString("Error.152") //$NON-NLS-1$
									+ ")\n"); //$NON-NLS-1$
						}
					}
					lOrder = Long.parseLong(sTo);
				} else {
					if (bMandatory) {
						throw new JajukException(file.getAbsolutePath() + " (" //$NON-NLS-1$
								+ Messages.getString("Error.152") + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						lOrder = 0;
					}
				}
			}
			if (lOrder < 10) {
				out = out.replace(PATTERN_TRACKORDER, "0" //$NON-NLS-1$
						+ lOrder);
			} else {
				out = out.replace(PATTERN_TRACKORDER, lOrder + ""); //$NON-NLS-1$
			}
		}
		// Check Track name
		if (sPattern.contains(PATTERN_TRACKNAME)) {
			sValue = track.getName().replace("[/\\:]", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			sValue = sValue.trim();
			out = out.replace(PATTERN_TRACKNAME, sValue);
		}
		// Check Year Value
		if (sPattern.contains(PATTERN_YEAR)) {
			if (track.getYear() != 0) {
				out = out.replace(PATTERN_YEAR, track.getYear() + ""); //$NON-NLS-1$
			} else {
				if (bMandatory) {
					throw new JajukException(file.getAbsolutePath() + " (" //$NON-NLS-1$
							+ Messages.getString("Error.148") + ")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					out = out.replace(PATTERN_YEAR, "?");
				}
			}
		}
		return out;
	}

	public static DropShadowBorder getShadowBorder() {
		return shadowBorder;
	}

	public static BasicGradientPainter getGrandiant() {
		return grandiant;
	}

}
