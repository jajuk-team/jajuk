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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.imageio.ImageIO;
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
import javax.swing.UIManager;

import org.jajuk.Main;
import org.jajuk.Main.MPlayerStatus;
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
import org.jajuk.base.Year;
import org.jajuk.dj.Ambience;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.IPerspective;
import org.jajuk.ui.IconLabel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukSystray;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.jdesktop.swingx.painter.MattePainter;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.theme.ThemeInfo;
import org.jvnet.substance.utils.SubstanceConstants;
import org.jvnet.substance.watermark.SubstanceImageWatermark;
import org.jvnet.substance.watermark.SubstanceNoneWatermark;
import org.jvnet.substance.watermark.SubstanceStripeWatermark;
import org.jvnet.substance.watermark.WatermarkInfo;

/**
 * General use utilities methods
 */
public class Util implements ITechnicalStrings {

	/* Cursors */
	public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);

	public static final Cursor LINK_CURSOR = new Cursor(Cursor.HAND_CURSOR);

	public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

	/** contains clipboard data */
	public static String copyData;

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
	private static DropShadowBorder shadowBorder = new DropShadowBorder(Color.BLACK, 5, .5f, 12,
			false, true, true, true);

	/** Generic gradiant* */
	public static MattePainter grayGrandient = new MattePainter(new GradientPaint(new Point(0, 0),
			new Color(226, 226, 226), new Point(0, 1000), new Color(250, 248, 248)));

	public static MattePainter aerithGrandient = new MattePainter(new GradientPaint(
			new Point(0, 0), Color.WHITE, new Point(0, 1000), new Color(64, 110, 161)));

	/** Are we under Windows ? * */
	private static final boolean bUnderWindows;

	/** Are we under Windows 32 bits ? * */
	private static final boolean bUnderWindows32bits;

	/** Are we under Linux ? * */
	private static final boolean bUnderLinux;

	/** Are we under MAC OS intel ? * */
	private static final boolean bUnderOSXintel;

	/** Are we under MAC OS power ? * */
	private static final boolean bUnderOSXpower;

	/** Are we under Windows 64 bits ? * */
	private static final boolean bUnderWindows64bits;

	// Computes OS detection operations for perf reasons (can be called in loop
	// in refresh method for ie)
	static {
		String sOS = (String) System.getProperties().get("os.name");
		// os.name can be null with JWS under MacOS
		bUnderWindows = (sOS != null && sOS.trim().toLowerCase().lastIndexOf("windows") != -1);
	}
	static {
		bUnderWindows32bits = isUnderWindows()
				&& System.getProperties().get("sun.arch.data.model").equals("32");
	}

	static {
		bUnderWindows64bits = isUnderWindows()
				&& !System.getProperties().get("sun.arch.data.model").equals("32");
	}

	static {
		String sOS = (String) System.getProperties().get("os.name");
		// os.name can be null with JWS under MacOS
		bUnderLinux = (sOS != null && sOS.trim().toLowerCase().lastIndexOf("linux") != -1);
	}

	static {
		String sArch = System.getProperty("os.arch");
		bUnderOSXintel = org.jdesktop.swingx.util.OS.isMacOSX()
				&& (sArch != null && sArch.matches(".*86"));
	}

	static {
		String sArch = System.getProperty("os.arch");
		bUnderOSXpower = org.jdesktop.swingx.util.OS.isMacOSX()
				&& (sArch != null && !sArch.matches(".*86"));
	}

	/**
	 * Genres
	 */
	public static final String[] genres = { "Blues", "Classic Rock", "Country", "Dance", "Disco",
			"Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop",
			"R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska",
			"Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal",
			"Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game",
			"Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space",
			"Meditative", "Instrumental Pop", "Instrumental Rock", "Ethnic", "Gothic", "Darkwave",
			"Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance", "Dream", "Southern Rock",
			"Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle",
			"Native American", "Cabaret", "New Wave", "Psychedelic", "Rave", "Showtunes",
			"Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical",
			"Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk", "Swing",
			"Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde",
			"Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock", "Slow Rock",
			"Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech", "Chanson",
			"Opera", "Chamber Music", "Sonata", "Symphony", "Booty Brass", "Primus", "Porn Groove",
			"Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad",
			"Rhytmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "Acapella",
			"Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror",
			"Indie", "BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta",
			"Heavy Metal", "Black Metal", "Crossover", "Contemporary C", "Christian Rock",
			"Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "SynthPop" };

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
		StringTokenizer st = new StringTokenizer(s, ".");
		String sExt = "";
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
			throw new JajukException(9, e);
		}
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			JajukException te = new JajukException(9, path, e);
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
			JajukException te = new JajukException(9, path, e);
			throw te;
		}

		// Close the bufferedReader
		try {
			input.close();
		} catch (IOException e) {
			JajukException te = new JajukException(9, path, e);
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
			JajukException te = new JajukException(9, e);
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
		if (s.contains("&")) {
			sOut = sOut.replaceAll("&", "&amp;");
		}
		if (s.contains("\'")) {
			sOut = sOut.replaceAll("\'", "&apos;");
		}
		if (s.contains("\"")) {
			sOut = sOut.replaceAll("\"", "&quot;");
		}
		if (s.contains("<")) {
			sOut = sOut.replaceAll("<", "&lt;");
		}
		if (s.contains(">")) {
			sOut = sOut.replaceAll(">", "&gt;");
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
		if (s.contains("&") || s.contains("\'") || s.contains("\"") || s.contains("<")
				|| s.contains(">")) {
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
			return Messages.getString("unknown_style");
		}
	}

	/** Format a time from secs to a human readable format */
	public static String formatTimeBySec(long lTime, boolean bTrimZeros) {
		long l = lTime;
		if (l == -1) { // means we are in repeat mode
			return "--:--";
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
			sbResult.append(sbHours).append(":");
		return sbResult.append(sbMins).append(":").append(sbSecs).toString();
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
	 * Get required image with specified url
	 * 
	 * @param sURL
	 * @return the image
	 */
	public static ImageIcon getImage(URL url) {
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
				if ((lUsedMB - file.length()) / 1048576 > iMB) {
					// too much backup files, delete older
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
			String sExt = new SimpleDateFormat("yyyyMMdd").format(new Date());
			File fileNew = new File(Util.removeExtension(file.getAbsolutePath()) + "-backup-"
					+ sExt + "." + Util.getExtension(file));
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
	public static void createEmptyFile(File file) throws IOException {
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
		Log.debug("Copying: " + file.getAbsolutePath() + "  to : " + directory.getAbsolutePath());
		File fileNew = new File(new StringBuffer(directory.getAbsolutePath()).append("/").append(
				file.getName()).toString());
		if (!file.exists() || !file.canRead()) {
			throw new JajukException(9, file.getAbsolutePath(), null);
		}
		if (!fileNew.getParentFile().canWrite()) {
			throw new JajukException(24, file.getAbsolutePath(), null);
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
		Log.debug("Copying: " + file.getAbsolutePath() + "  to : " + fNew.getAbsolutePath());
		if (!file.exists() || !file.canRead()) {
			throw new JajukException(9, file.getAbsolutePath(), null);
		}
		if (!fNew.getParentFile().canWrite()) {
			throw new JajukException(24, file.getAbsolutePath(), null);
		}
		FileChannel fcSrc = new FileInputStream(file).getChannel();
		FileChannel fcDest = new FileOutputStream(fNew).getChannel();
		fcDest.transferFrom(fcSrc, 0, fcSrc.size());
		fcSrc.close();
		fcDest.close();
		// Display a warning if copied file is void as it can happen with full
		// disks
		if (fNew.length() == 0) {
			Log.warn("Copied file is void: " + file.getAbsolutePath());
		}
	}

	/**
	 * Copy recursively files and directories Inspirated from
	 * 
	 * @param str
	 * @param dst
	 * @throws IOException
	 */
	public static void copyRecursively(File src, File dst) throws Exception {
		if (src.isDirectory()) {
			dst.mkdirs();
			String list[] = src.list();
			for (int i = 0; i < list.length; i++) {
				String dest1 = dst.getAbsolutePath() + '/' + list[i];
				String src1 = src.getAbsolutePath() + '/' + list[i];
				copyRecursively(new File(src1), new File(dest1));
			}
		} else {
			copy(src, dst);
		}
	}

	/**
	 * Copy a file
	 * 
	 * @param file :
	 *            source file
	 * @param sNewName :
	 *            dest file
	 */
	public static void copy(File file, String sNewName) throws Exception {
		Log.debug("Renaming: " + file.getAbsolutePath() + "  to : " + sNewName);
		File fileNew = new File(new StringBuffer(file.getParentFile().getAbsolutePath())
				.append('/').append(sNewName).toString());
		if (!file.exists() || !file.canRead()) {
			throw new JajukException(9, file.getAbsolutePath(), null);
		}
		if (!fileNew.getParentFile().canWrite()) {
			throw new JajukException(24, file.getAbsolutePath(), null);
		}
		FileChannel fcSrc = new FileInputStream(file).getChannel();
		FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
		fcDest.transferFrom(fcSrc, 0, fcSrc.size());
		fcSrc.close();
		fcDest.close();
	}

	/**
	 * Copy a URL resource to a file We don't use nio but Buffered Reader /
	 * writer because we can only get channels from a FileInputStream that can
	 * be or not be in a Jar (production / test)
	 * 
	 * @param src
	 *            source designed by URL
	 * @param dest
	 *            destination file full path
	 * @throws Exception
	 */
	public static void copy(URL src, String dest) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(src.openStream()));
		BufferedWriter bw = new BufferedWriter(new FileWriter(dest));
		String sLine = null;
		do {
			sLine = br.readLine();
			if (sLine != null) {
				bw.write(sLine);
				bw.newLine();
			}
		} while (sLine != null);
		br.close();
		bw.flush();
		bw.close();
	}

	/**
	 * @param sFileName
	 * @return whether the given filename is a standard cover or not
	 */
	public static boolean isStandardCover(String sFileName) {
		return sFileName.toLowerCase().matches(".*" + FILE_DEFAULT_COVER + ".*")
				|| sFileName.toLowerCase().matches(".*" + FILE_DEFAULT_COVER_2 + ".*")
				// just for previous compatibility, now it is a directory
				// property
				|| sFileName.toLowerCase().matches(".*" + FILE_ABSOLUTE_DEFAULT_COVER + ".*");

	}

	/**
	 * Tell whether a file is an absolute default cover or not
	 * 
	 * @param directory
	 *            Jajuk Directory in which we analyze the given file name
	 * @param sFileName
	 * @return whether the given filename is an absolute default cover
	 */
	public static boolean isAbsoluteDefaultCover(Directory directory, String sFilename) {
		String sDefault = directory.getStringValue(XML_DIRECTORY_DEFAULT_COVER);
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
			String sOut = "";
			FileChannel fc = new FileInputStream(fio).getChannel();
			ByteBuffer bb = ByteBuffer.allocate(500);
			fc.read(bb, fio.length() / 2);
			fc.close();
			sOut = new String(bb.array());
			return MD5Processor.hash(sOut);
		} catch (Exception e) {
			throw new JajukException(103, e);
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
	 *            resource URL
	 * @param id
	 *            unique identifier for the file
	 * @return Cache directory
	 */
	public static File getCachePath(URL url, String id) {
		File out = null;
		if (id == null) {
			out = Util.getConfFileByPath(FILE_CACHE + '/' + Util.getOnlyFile(url.toString()));
		} else {
			out = Util.getConfFileByPath(FILE_CACHE + '/' + id + '_'
					+ Util.getOnlyFile(url.toString()));
		}
		return out;
	}

	/**
	 * Clear locale images cache
	 */
	public static void clearCache() {
		File fCache = Util.getConfFileByPath(FILE_CACHE);
		File[] files = fCache.listFiles();
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}

	/**
	 * @return whether we are under Windows
	 */
	public static boolean isUnderWindows() {
		return bUnderWindows;
	}

	/**
	 * @return whether we are under Windows 32 bits
	 */
	public static boolean isUnderWindows32bits() {
		return bUnderWindows32bits;
	}

	/**
	 * @return whether we are under Windows 64 bits
	 */
	public static boolean isUnderWindows64bits() {
		return bUnderWindows64bits;
	}

	/**
	 * @return whether we are under Linux
	 */
	public static boolean isUnderLinux() {
		return bUnderLinux;
	}

	/**
	 * Try to compute time length in milliseconds using BasicPlayer API. (code
	 * from jlGui 2.3)
	 */
	public static long getTimeLengthEstimation(Map properties) {
		long milliseconds = -1;
		int byteslength = -1;
		if (properties != null) {
			if (properties.containsKey("audio.length.bytes")) {
				byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
			}
			if (properties.containsKey("duration")) {
				milliseconds = (((Long) properties.get("duration")).longValue()) / 1000;
			} else {
				// Try to compute duration
				int bitspersample = -1;
				int channels = -1;
				float samplerate = -1.0f;
				int framesize = -1;
				if (properties.containsKey("audio.samplesize.bits")) {
					bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue();
				}
				if (properties.containsKey("audio.channels")) {
					channels = ((Integer) properties.get("audio.channels")).intValue();
				}
				if (properties.containsKey("audio.samplerate.hz")) {
					samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue();
				}
				if (properties.containsKey("audio.framesize.bytes")) {
					framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue();
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
		jpOut.setMinimumSize(new Dimension(0, 0));
		// allow resing with info node
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
	 * <p>
	 * null files are ignored
	 * </p>
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
			if (file != null) {
				try {
					StackItem item = new StackItem(file);
					item.setRepeat(bRepeat);
					item.setUserLaunch(bUserLauched);
					alOut.add(item);
				} catch (JajukException je) {
					Log.error(je);
				}
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
		// Wait for full image loading
		MediaTracker mediaTracker = new MediaTracker(new Container());
		mediaTracker.addImage(img.getImage(), 0);
		try {
			mediaTracker.waitForID(0);
		} catch (InterruptedException e) {
			Log.error(e);
		}
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
	 * @param Do
	 *            we need alpha (transparency) ?
	 * @param new
	 *            image width
	 * @param height
	 *            new image height
	 * @return buffured image from an image
	 */
	public static BufferedImage toBufferedImage(Image image, boolean alpha, int width, int height) {
		if (image instanceof BufferedImage) {
			return ((BufferedImage) image);
		} else {
			/** Create the new image */
			BufferedImage bufferedImage = null;
			if (alpha) {
				bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			} else {
				// Save memory
				bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			}
			Graphics2D graphics2D = bufferedImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2D.drawImage(image, 0, 0, width, height, null);
			graphics2D.dispose();
			return (bufferedImage);
		}
	}

	public static BufferedImage toBufferedImage(Image image, boolean alpha) {
		return toBufferedImage(image, alpha, image.getWidth(null), image.getHeight(null));
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
		if (Main.getSystray() != null && JajukSystray.getInstance().jmenu != null) {
			updateComponentTreeUI(JajukSystray.getInstance().jmenu);
		}
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
	 * Set a look and feel. We always use Substance Look And Feel with various
	 * themes
	 * 
	 * @param theme
	 */
	public static void setLookAndFeel(String pTheme) {
		try {
			// Set substance laf
			UIManager.setLookAndFeel(LNF_SUBSTANCE_CLASS);
			// hide some useless elements such locker for not editable labels
			UIManager.put(SubstanceLookAndFeel.NO_EXTRA_ELEMENTS, Boolean.TRUE);
			UIManager.put(SubstanceLookAndFeel.WATERMARK_TO_BLEED, Boolean.TRUE);
			UIManager.put(SubstanceLookAndFeel.ENABLE_INVERTED_THEMES, Boolean.TRUE);
			UIManager.put(SubstanceLookAndFeel.ENABLE_NEGATED_THEMES, Boolean.TRUE);
			// Check the theme is known, if not take the default theme
			Map<String, ThemeInfo> themes = SubstanceLookAndFeel.getAllThemes();
			if (themes.get(pTheme) == null) {
				pTheme = LNF_DEFAULT_THEME;
			}
			// Set substance theme
			SubstanceLookAndFeel.setCurrentTheme(themes.get(pTheme).getClassName());
		} catch (Exception e) {
			Log.error(e);
		}
	}

	/**
	 * Set a watermark
	 * 
	 * @param watermark
	 *            name
	 */
	public static void setWatermark(String pWatermark) {
		try {
			String watermark = pWatermark;
			// Check the watermark is known, if not take the default one
			Map<String, WatermarkInfo> watermarks = SubstanceLookAndFeel.getAllWatermarks();
			if (watermarks.get(watermark) == null) {
				// the image watermark is not included in the list for unknown
				// reasons
				if (!"Image".equals(watermark)) {
					watermark = LNF_DEFAULT_WATERMARK;
				}
			}
			// Set the watermark
			String image = ConfigurationManager.getProperty(CONF_OPTIONS_WATERMARK_IMAGE);
			if ("Image".equals(watermark)) {
				//Check that the backgroud image is readable 
				if (new File(image).exists()) {
					SubstanceLookAndFeel.setCurrentWatermark(new SubstanceImageWatermark(image));
					SubstanceLookAndFeel
							.setImageWatermarkKind(SubstanceConstants.ImageWatermarkKind.SCREEN_CENTER_SCALE);
				}
				else{
					//None watermark
					SubstanceLookAndFeel.setCurrentWatermark(new SubstanceNoneWatermark());
				}
			} else {
				SubstanceLookAndFeel.setCurrentWatermark(watermarks.get(watermark).getClassName());
			}
		} catch (Exception e) {
			Log.error(e);
			// Set default watermark
			SubstanceLookAndFeel.setCurrentWatermark(new SubstanceStripeWatermark());
			ConfigurationManager.setProperty(CONF_OPTIONS_WATERMARK, LNF_DEFAULT_WATERMARK);
		}
	}

	/**
	 * Formater for properties dialog window
	 * 
	 * @param sDesc
	 * @return
	 */
	public static String formatPropertyDesc(String sDesc) {
		return "<HTML><center><b><font size=+0 color=#000000>" + sDesc + "</font></b><HTML>";
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
			return "";
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
		// Wait for full image loading
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
		BufferedImage thumbImage = toBufferedImage(image, !(Util.getExtension(thumb)
				.equalsIgnoreCase("jpg")), thumbWidth, thumbHeight);
		// Need alpha only for png and gif files
		// save thumbnail image to OUTFILE
		ImageIO.write(thumbImage, Util.getExtension(thumb), thumb);
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
		properties.remove("user.name");
		properties.remove("java.ext.dirs");
		properties.remove("sun.boot.class.path");
		properties.remove("deployment.user.security.trusted.certs");
		properties.remove("deployment.user.security.trusted.clientauthcerts");
		properties.remove("jajuk.log");

		return properties;
	}

	/**
	 * @return Anonymized Jajuk properties (for log or quality agent)
	 */
	public static Properties getAnonymizedJajukProperties() {
		Properties properties = (Properties) ConfigurationManager.getProperties().clone();
		// We remove sensible data from logs
		properties.remove("jajuk.network.proxy_login");
		properties.remove("jajuk.network.proxy_port");
		properties.remove("jajuk.network.proxy_hostname");
		properties.remove("jajuk.options.p2p.password");
		return properties;
	}

	/**
	 * 
	 * @param s
	 *            String to test
	 * @return whether the string is void or not
	 */
	public static boolean isVoid(String s) {
		return s == null || s.trim().equals("");
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
		if (oData instanceof Style || oData instanceof Author || oData instanceof Year
				|| oData instanceof Album || oData instanceof Track) {
			if (oData instanceof Style || oData instanceof Year || oData instanceof Author
					|| oData instanceof Album) {
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
	public static String getMPlayerWindowsPath() {
		// Use cache
		if (sMplayerPath != null) {
			return sMplayerPath;
		}
		File file = null;
		// Check in ~/.jajuk directory (used by .exe or .jar distribution
		// installers)
		if ((file = Util.getConfFileByPath(FILE_MPLAYER_EXE)).exists()) {
			sMplayerPath = file.getAbsolutePath();
			return sMplayerPath;
		} else {
			// Check in the path where jajuk.jar is executed
			String sPATH = null;
			try {
				// Extract file name from URL. URI returns jar path, its parent
				// is the bin directory and the right dir is the parent of bin
				// dir
				// Note: When starting from jnlp, next line thorws an exception
				// as URI is invalid (contains %20), the method returns null and
				// the
				// file is downloaded again. This is usefull only when using
				// stand-alone version
				sPATH = new File(getJarLocation(Main.class).toURI()).getParentFile()
						.getParentFile().getAbsolutePath();
				// Add MPlayer file name
				if ((file = new File(sPATH + '/' + FILE_MPLAYER_EXE)).exists()) {
					sMplayerPath = file.getAbsolutePath();
				}
			} catch (Exception e) {
				return sMplayerPath;
			}
		}
		return sMplayerPath; // can be null if none suitable file found
	}

	/**
	 * @return MPLayer binary MAC full path
	 */
	public static String getMPlayerOSXPath() {
		String forced = ConfigurationManager.getProperty(CONF_MPLAYER_PATH_FORCED);
		if (forced != null && !"".equals(forced)) {
			return forced + "/mplayer";
		} else if (Util.isUnderOSXintel()
				&& new File(FILE_DEFAULT_MPLAYER_X86_OSX_PATH + "/mplayer").exists()) {
			return FILE_DEFAULT_MPLAYER_X86_OSX_PATH + "/mplayer";
		} else if (Util.isUnderOSXpower()
				&& new File(FILE_DEFAULT_MPLAYER_POWER_OSX_PATH + "/mplayer").exists()) {
			return FILE_DEFAULT_MPLAYER_POWER_OSX_PATH + "/mplayer";
		} else {
			// Simply return mplayer from PATH, works if app is launch from CLI
			return "mplayer";
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
			sValue = track.getAuthor().getName().replace("[/\\:]", "-");
			sValue = sValue.trim();
			if (!sValue.equals(UNKNOWN_AUTHOR)) {
				out = out.replace(PATTERN_ARTIST, AuthorManager.format(sValue));
			} else {
				if (bMandatory) {
					throw new JajukException(150, file.getAbsolutePath());
				} else {
					out = out.replace(PATTERN_ARTIST, Messages.getString(UNKNOWN_AUTHOR));
				}
			}
		}
		// Check Style name
		if (sPattern.contains(PATTERN_GENRE)) {
			sValue = track.getStyle().getName().replace("[/\\:]", "-");
			sValue = sValue.trim();
			if (!sValue.equals(UNKNOWN_STYLE)) {
				out = out.replace(PATTERN_GENRE, StyleManager.format(sValue));
			} else {
				if (bMandatory) {
					throw new JajukException(153, file.getAbsolutePath());
				} else {
					out = out.replace(PATTERN_GENRE, Messages.getString(UNKNOWN_STYLE));
				}
			}
		}
		// Check Album Name
		if (sPattern.contains(PATTERN_ALBUM)) {
			sValue = track.getAlbum().getName().replace("[/\\:]", "-");
			sValue = sValue.trim();
			if (!sValue.equals(UNKNOWN_ALBUM)) {
				out = out.replace(PATTERN_ALBUM, AlbumManager.format(sValue));
			} else {
				if (bMandatory) {
					throw new JajukException(149, file.getAbsolutePath());
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
					String sTo = file.getName().substring(0, 3).trim().replaceAll("[^0-9]", "");
					for (char c : sTo.toCharArray()) {
						if (!Character.isDigit(c)) {
							throw new JajukException(152, file.getAbsolutePath());
						}
					}
					lOrder = Long.parseLong(sTo);
				} else {
					if (bMandatory) {
						throw new JajukException(152, file.getAbsolutePath());
					} else {
						lOrder = 0;
					}
				}
			}
			if (lOrder < 10) {
				out = out.replace(PATTERN_TRACKORDER, "0" + lOrder);
			} else {
				out = out.replace(PATTERN_TRACKORDER, lOrder + "");
			}
		}
		// Check Track name
		if (sPattern.contains(PATTERN_TRACKNAME)) {
			sValue = track.getName().replace("[/\\:]", "-");
			sValue = sValue.trim();
			out = out.replace(PATTERN_TRACKNAME, sValue);
		}
		// Check Year Value
		if (sPattern.contains(PATTERN_YEAR)) {
			if (track.getYear().getValue() != 0) {
				out = out.replace(PATTERN_YEAR, track.getYear().getValue() + "");
			} else {
				if (bMandatory) {
					throw new JajukException(148, file.getAbsolutePath());
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

	/**
	 * 
	 * @param sPATH
	 *            Configuration file or directory path
	 * @return the file relative to jajuk directory
	 */
	public static final File getConfFileByPath(String sPATH) {
		String sRoot = System.getProperty("user.home");
		if (Main.workspace != null && !Main.workspace.trim().equals("")) {
			sRoot = Main.workspace;
		}
		return new File(sRoot + '/' + (Main.bTestMode ? ".jajuk_test_" + TEST_VERSION : ".jajuk")
				+ '/' + sPATH);
	}

	/**
	 * 
	 * @return This box hostname
	 */
	public static String getHostName() {
		String sHostname = null;
		// Try to get hostname using the standard way
		try {
			sHostname = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			Log.debug("Cannot get Hostname using the standard way");
		}
		if (sHostname == null) {
			// Try using IP now
			try {
				java.net.InetAddress inetAdd = java.net.InetAddress.getByName("127.0.0.1");
				sHostname = inetAdd.getHostName();
			} catch (Exception e) {
				Log.debug("Cannot get Hostname by IP");
			}
		}
		// If still no hostname, return a default value
		if (sHostname == null) {
			sHostname = DEFAULT_HOSTNAME;
		}
		return sHostname;
	}

	/**
	 * Make thumbnail file exists (album id.jpg or.gif or .png) in thumbs
	 * directory if it doesn't exist yet
	 * 
	 * @param album
	 * @return whether a new cover has been created
	 */
	public static boolean refreshThumbnail(Album album, String size) {
		File fThumb = Util.getConfFileByPath(FILE_THUMBS + '/' + size + '/' + album.getId() + '.'
				+ EXT_THUMB);
		File fCover = null;
		if (!fThumb.exists()) {
			// search for local covers in all directories mapping the
			// current track to reach other
			// devices covers and display them together
			Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
			if (tracks.size() == 0) {
				return false;
			}
			// take first track found to get associated directories as we
			// assume all tracks for an album are in the same directory
			Track trackCurrent = tracks.iterator().next();
			fCover = trackCurrent.getAlbum().getCoverFile();
			if (fCover != null) {
				try {
					int iSize = Integer.parseInt(new StringTokenizer(size, "x").nextToken());
					Util.createThumbnail(fCover, fThumb, iSize);
					InformationJPanel.getInstance().setMessage(
							Messages.getString("CatalogView.5") + ' ' + album.getName2(),
							InformationJPanel.INFORMATIVE);
					return true;
				} catch (Exception e) {
					// create a void thumb to avoid trying to create again
					// this thumb
					try {
						fThumb.createNewFile();
					} catch (IOException e1) {
						Log.error(e1);
					}
					Log.error(e);
				}
			}
		}
		return false; // thumb already exist
	}

	/**
	 * Pad an int with zeros
	 * 
	 * @param l
	 *            the number to be padded
	 * @param size
	 *            the targetted size
	 * @return
	 */
	public static String padNumber(long l, int size) {
		String sOut = Long.toString(l);
		while (sOut.length() < size) {
			sOut = '0' + sOut;
		}
		return sOut;
	}

	/**
	 * Make sure to reduce a string to the given size
	 * 
	 * @param sIn
	 *            Input string, exemple: blabla
	 * @param iSize
	 *            max size, exemple: 3
	 * @return bla...
	 */
	public static String getLimitedString(String sIn, int iSize) {
		String sOut = sIn;
		if (sIn.length() > iSize) {
			sOut = sIn.substring(0, iSize) + "...";
		}
		return sOut;
	}

	public static MPlayerStatus getMplayerStatus(String mplayerPATH) {
		Process proc = null;
		MPlayerStatus mplayerStatus = MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
		try {
			String fullPath = null;
			if ("".equals(mplayerPATH)) {
				fullPath = "mplayer";
			} else {
				fullPath = mplayerPATH + "/mplayer";
			}
			Log.debug("Testing path: " + fullPath);
			// check MPlayer release : 1.0pre8 min
			proc = Runtime.getRuntime().exec(new String[] { fullPath, "-input", "cmdlist" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = null;
			mplayerStatus = MPlayerStatus.MPLAYER_STATUS_WRONG_VERSION;
			for (; (line = in.readLine()) != null;) {
				if (line.matches("get_time_pos.*")) { //$NON-NLS-1$
					mplayerStatus = MPlayerStatus.MPLAYER_STATUS_OK;
					break;
				}
			}
		} catch (Exception e) {
			mplayerStatus = MPlayerStatus.MPLAYER_STATUS_NOT_FOUND;
		}
		return mplayerStatus;
	}

	/**
	 * @return whether we are under OS X Intel
	 */
	public static boolean isUnderOSXintel() {
		return bUnderOSXintel;
	}

	/**
	 * @return whether we are under OS X Power
	 */
	public static boolean isUnderOSXpower() {
		return bUnderOSXpower;
	}

	/**
	 * Encode URLS
	 * 
	 * @param s
	 * @return
	 */
	public static String encodeString(String s) {
		return s.replaceAll(" +", "+");
	}

	public static int countDirectories(File root) {
		int count = 0;
		// index init
		File fCurrent = root;
		int[] indexTab = new int[100]; // directory index
		for (int i = 0; i < 100; i++) { // init
			indexTab[i] = -1;
		}
		int iDeep = 0; // deep
		File dParent = root;
		// Start actual scan
		while (iDeep >= 0 && !Main.isExiting()) {
			// only directories
			File[] files = fCurrent.listFiles(Util.dirFilter);
			// files is null if fCurrent is a not a directory
			if (files == null || files.length == 0) {
				// re-init for next time we will reach this deep
				indexTab[iDeep] = -1;
				iDeep--; // come up
				fCurrent = fCurrent.getParentFile();
				if (dParent != null) {
					dParent = dParent.getParentFile();
				}
			} else {
				if (indexTab[iDeep] < files.length - 1) {
					// enter sub-directory
					indexTab[iDeep]++; // inc index for next time we
					// will reach this deep
					fCurrent = files[indexTab[iDeep]];
					count++;
					iDeep++;
				} else {
					indexTab[iDeep] = -1;
					iDeep--;
					fCurrent = fCurrent.getParentFile();
					if (dParent != null) {
						dParent = dParent.getParentFile();
					}
				}
			}
		}
		return count;
	}

	/**
	 * @param color
	 *            java color
	 * @return HTML RGB color ex: FF0000
	 */
	public static String getHTMLColor(Color color) {
		return Long.toString(color.getRed(), 16) + Long.toString(color.getGreen(), 16)
				+ Long.toString(color.getBlue(), 16);

	}

	/**
	 * Rot13 encode/decode,
	 * <p>
	 * Thx
	 * http://www.idevelopment.info/data/Programming/java/security/java_cryptography_extension/rot13.java
	 * </p>
	 * 
	 * @param in
	 *            text to encode / decode in rote 13
	 * @return encoded /decoded text
	 */
	public static String rot13(String in) {
		if (Util.isVoid(in)) {
			return "";
		}
		int abyte = 0;
		StringBuffer tempReturn = new StringBuffer();
		for (int i = 0; i < in.length(); i++) {
			abyte = in.charAt(i);
			int cap = abyte & 32;
			abyte &= ~cap;
			abyte = ((abyte >= 'A') && (abyte <= 'Z') ? ((abyte - 'A' + 13) % 26 + 'A') : abyte)
					| cap;
			tempReturn.append((char) abyte);
		}
		return tempReturn.toString();
	}

	/**
	 * @return Number of stars
	 */
	public static int getStarsNumber(long lRate) {
		long lInterval = TrackManager.getInstance().getMaxRate() / 4;
		if (lRate <= lInterval) {
			return 1;
		} else if (lRate <= 2 * lInterval) {
			return 2;
		} else if (lRate <= 3 * lInterval) {
			return 3;
		} else {
			return 4;
		}
	}

	/**
	 * @return the stars icon
	 */
	public static IconLabel getStars(long rate) {
		int starsNumber = getStarsNumber(rate);
		IconLabel ilRate = null;
		switch (starsNumber) {
		case 1:
			ilRate = new IconLabel(IconLoader.ICON_STAR_1, "", null, null, null, Long
					.toString(rate));
			break;
		case 2:
			ilRate = new IconLabel(IconLoader.ICON_STAR_2, "", null, null, null, Long
					.toString(rate));
			break;
		case 3:
			ilRate = new IconLabel(IconLoader.ICON_STAR_3, "", null, null, null, Long
					.toString(rate));
			break;
		case 4:
			ilRate = new IconLabel(IconLoader.ICON_STAR_4, "", null, null, null, Long
					.toString(rate));
			break;
		default:
			return null;
		}
		ilRate.setInteger(true);
		return ilRate;
	}

	/**
	 * Write down a memory image to a file
	 * 
	 * @param src
	 * @param dest
	 */
	public static void extractImage(Image src, File dest) {
		BufferedImage bi = toBufferedImage(src, !(Util.getExtension(dest).equalsIgnoreCase("jpg")));
		// Need alpha only for png and gif files);
		try {
			ImageIO.write(bi, Util.getExtension(dest), dest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Extract files from current jar to "cache/internal" directory
	 * <p>
	 * Thanks several websites, especially
	 * http://www.developer.com/java/other/article.php/607931
	 * 
	 * @param entryName
	 *            name of the file to extract. Example: img.png
	 * @param file
	 *            destination PATH
	 * @throws Exception
	 */
	public static void extractFile(String entryName, String destName) throws Exception {
		JarFile jar = null;
		// Open the jar.
		try {
			File dir = new File(getJarLocation(Main.class).toURI()).getParentFile();
			// We have to call getParentFile() method because the toURI() method
			// returns an URI than is not always valid (contains %20 for spaces
			// for instance)
			File jarFile = new File(dir.getAbsolutePath() + "/jajuk.jar");
			Log.debug("Open jar: " + jarFile.getAbsolutePath());
			jar = new JarFile(jarFile);
		} catch (Exception e) {
			Log.error(e);
			return;
		}
		try {
			// Get the entry and its input stream.
			JarEntry entry = jar.getJarEntry(entryName);
			// If the entry is not null, extract it. Otherwise, print a
			// message.
			if (entry != null) {
				// Get an input stream for the entry.
				InputStream entryStream = jar.getInputStream(entry);
				try {
					// Create the output file (clobbering the file if it
					// exists).
					FileOutputStream file = new FileOutputStream(getConfFileByPath(FILE_CACHE + '/'
							+ FILE_INTERNAL_CACHE + '/' + destName));
					try {
						// Allocate a buffer for reading the entry data.
						byte[] buffer = new byte[1024];
						int bytesRead;
						// Read the entry data and write it to the output file.
						while ((bytesRead = entryStream.read(buffer)) != -1) {
							file.write(buffer, 0, bytesRead);
						}
					} catch (Exception e) {
						Log.error(e);
					} finally {
						file.flush();
						file.close();
					}
				} catch (Exception e) {
					Log.error(e);
				} finally {
					entryStream.close();
				}
			} else {
				Log.debug(entryName + " not found.");
			} // end if
		} catch (Exception e) {
			Log.error(e);
		} finally {
			jar.close();
		}
	}

}
