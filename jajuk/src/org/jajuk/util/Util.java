/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.Directory;
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukContainer;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import com.sun.media.sound.MixerSourceLine;

/**
 * General use utilities methods
 * @author Bertrand Florat 
 * @created 12 oct. 2003
 */
public class Util implements ITechnicalStrings {
	
	/*Cursors*/
	public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
	public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
	/**Contains execution location ( jar or directory )*/
	public static String sExecLocation;
	/**Waiting flag for perfs*/
	private static boolean bWaiting = false;
	
	/**
	 * Genres
	 */
	public static final String [] genres = {
			"Blues","Classic Rock","Country","Dance","Disco","Funk","Grunge", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"Hip-Hop","Jazz","Metal","New Age","Oldies","Other","Pop","R&B", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"Rap","Reggae","Rock","Techno","Industrial","Alternative","Ska", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"Death Metal","Pranks","Soundtrack","Euro-Techno","Ambient", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Trip-Hop","Vocal","Jazz+Funk","Fusion","Trance","Classical", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Instrumental","Acid","House","Game","Sound Clip","Gospel", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Noise","AlternRock","Bass","Soul","Punk","Space","Meditative", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"Instrumental Pop","Instrumental Rock","Ethnic","Gothic", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Darkwave","Techno-Industrial","Electronic","Pop-Folk", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Eurodance","Dream","Southern Rock","Comedy","Cult","Gangsta", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Top 40","Christian Rap","Pop/Funk","Jungle","Native American", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Cabaret","New Wave","Psychedelic","Rave","Showtunes","Trailer", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Lo-Fi","Tribal","Acid Punk","Acid Jazz","Polka","Retro", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Musical","Rock & Roll","Hard Rock","Folk","Folk-Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"National Folk","Swing","Fast Fusion","Bebob","Latin","Revival", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Celtic","Bluegrass","Avantgarde","Gothic Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Progressive Rock","Psychedelic Rock","Symphonic Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"Slow Rock","Big Band","Chorus","Easy Listening","Acoustic", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Humour","Speech","Chanson","Opera","Chamber Music","Sonata", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Symphony","Booty Brass","Primus","Porn Groove","Satire", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Slow Jam","Club","Tango","Samba","Folklore","Ballad", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Power Ballad","Rhytmic Soul","Freestyle","Duet","Punk Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"Drum Solo","Acapella","Euro-House","Dance Hall", "Goa","Drum & Bass","Club-House","Hardcore", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"Terror","Indie","BritPop","Negerpunk","Polsk Punk","Beat","Christian Gangsta","Heavy Metal", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
			"Black Metal","Crossover","Contemporary C","Christian Rock","Merengue","Salsa","Thrash Metal", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			"Anime","JPop","SynthPop"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
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
	 * Remove an extension from a file name
	 * @param filename
	 * @return filename without extension
	 */
	public static String removeExtension(String sFilename) {
		return sFilename.substring(0,sFilename.lastIndexOf('.'));
	}
	
	
	/**
	 * Open a file and return a string buffer with the file content.
	 * 
	 * @param path -File path
	 * @return StringBuffer - File content.
	 * @throws JajukException - Throws a JajukException if a problem occurs during the file  access.
	 */
	public static StringBuffer readFile(String path) throws JajukException {
		// Read
		File file = null;
		try{
			new File(path);
		}
		catch(Exception e){
			throw new JajukException("009",e); //$NON-NLS-1$
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
	 * Open a file from current jar and return a string buffer with the file content.
	 * 
	 * @param sUrl : relative file url
	 * @return StringBuffer - File content.
	 * @throws JajukException -Throws a JajukException if a problem occurs during the file  access.
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
			int i=0;
			do {
				i = is.read(b,0,b.length);
				sb.append(new String(b));
			}
			while (i > 0);
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
		String sOut = s.replaceAll("&","&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		sOut = sOut.replaceAll("\'","&apos;"); //$NON-NLS-1$ //$NON-NLS-2$
		sOut = sOut.replaceAll("\"","&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
		sOut = sOut.replaceAll("<","&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		sOut = sOut.replaceAll(">","&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		StringBuffer sbOut = new StringBuffer(sOut);
		/* Transform String to XML-valid characters. XML 1.0 specs ; 
		 * Character Range
		[2]     Char    ::=     #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]  
		any Unicode character, excluding the surrogate blocks, FFFE, and FFFF. */
		for (int i=0;i<sbOut.length();i++){
			char c = sbOut.charAt(i);
			if ( !isChar(c)){
				sbOut.deleteCharAt(i); //remove this char, it will be replaced by the XML format &#x?; or by a space if it is invalid
				sbOut.insert(i,' '); //replace invalid character by a space
			}
		}
		return sbOut.toString();
	}
	
	/**
	 * @param ucs4char char to test
	 * @return whether the char is valid, code taken from Apache sax implementation
	 */
	public static boolean isChar(int ucs4char)
	{
		return ucs4char >= 32 && ucs4char <= 55295 || ucs4char == 10 || ucs4char == 9 || ucs4char == 13 
		|| ucs4char >= 57344 && ucs4char <= 65533 || ucs4char >= 0x10000 && ucs4char<= 0x10ffff;
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
			return Messages.getString("unknown_style"); //$NON-NLS-1$
		}
	}	
	
	/**Format a time from secs to a human readable format*/
	public static String formatTimeBySec(long l, boolean bTrimZeros){
		if (l == -1){ //means we are in repeat mode
		    return "--:--"; //$NON-NLS-1$
		}
	    if (l<0) l =0;  //make sure to to get negative values
	    long lHours = l/3600;
		long lMins = l/60-(lHours*60);
		long lSecs = l-(lHours*3600)-(lMins*60);
		StringBuffer sbHours = new StringBuffer(Long.toString(lHours));
		if ( sbHours.length() == 1 && !bTrimZeros) sbHours.insert(0,'0');
		StringBuffer sbMins = new StringBuffer(Long.toString(lMins));
		if ( sbMins.length() == 1 && !bTrimZeros) sbMins.insert(0,'0');
		StringBuffer sbSecs = new StringBuffer(Long.toString(lSecs));
		if ( sbSecs.length() == 1) sbSecs.insert(0,'0');

	    StringBuffer sbResult = new StringBuffer();
	    if (lHours > 0) sbResult.append(sbHours).append(":"); //$NON-NLS-1$
	    return sbResult.append(sbMins).append(":").append(sbSecs).toString(); //$NON-NLS-1$
	}
	
		
	/** Waiting cursor thread, stored to avoid construction */
	private static Thread tWaiting = new Thread(){
		public void run(){
			Container container = null;
			IPerspective perspective = PerspectiveManager.getCurrentPerspective();
			if ( perspective != null){
				container = (Container)perspective.getContentPane();
				int numComp = container.getComponentCount();
				Component comp = null;
				for (int i = 0; i < numComp; i++) {
					comp = container.getComponent(i);
					if (comp instanceof JajukContainer) {// ?
						((JajukContainer)comp).setWaiting(true);
					}
				}
				container.setCursor(WAIT_CURSOR);
				CommandJPanel.getInstance().setCursor(WAIT_CURSOR);
				InformationJPanel.getInstance().setCursor(WAIT_CURSOR);
				PerspectiveBarJPanel.getInstance().setCursor(WAIT_CURSOR);
			}
		}
	};
	
	/** Default cursor thread, stored to avoid construction */
	private static Thread tDefault = new Thread(){
		public void run(){
			Container container = null;
			IPerspective perspective = PerspectiveManager.getCurrentPerspective();
			if ( perspective != null){
			    container = (Container)perspective.getContentPane();
				int numComp = container.getComponentCount();
				Component comp = null;
				for (int i = 0; i < numComp; i++) {
					comp = container.getComponent(i);
					if (comp instanceof JajukContainer) {// ?
						((JajukContainer)comp).setWaiting(true);
					}
				}
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
	public static synchronized void waiting(){
		if (!bWaiting){
		    bWaiting = true;
		    SwingUtilities.invokeLater(tWaiting);    
		}
	}
	
	/**
	 * Set current cursor as default cursor
	 */
	public static synchronized void stopWaiting(){
	    if (bWaiting){
	        bWaiting = false;
			SwingUtilities.invokeLater(tDefault); 
		}
	}
	
	/**
	 * Get required icon or image with specified url
	 * @param sURL
	 * @return the image
	 */
	public static ImageIcon getIcon(String sURL){
		ImageIcon ii = null;
		try{
			ii = new ImageIcon(new URL(sURL));
		}
		catch(Exception e){
			Log.error(e);
		}
		return ii;
	}
	
	
	/**
	 * Save a file in the same directory with name <filename>_YYYYmmddHHMM.xml and with a given maximum Mb size for the file and its backup files
	 * @param file
	 */
	public static void backupFile(File file,int iMB){
		try{
			if (Integer.parseInt(ConfigurationManager.getProperty(CONF_BACKUP_SIZE))<=0){ //0 or less means no backup
			    return;
			}
		    //calculates total size in MB for the file to backup and its backup files
			long lUsedMB = 0;
			ArrayList alFiles = new ArrayList(10);
			File[] files = new File(file.getAbsolutePath()).getParentFile().listFiles();
			if ( files != null){
				for ( int i=0;i<files.length;i++){
					if ( files[i].getName().indexOf(removeExtension(file.getName()))!= -1){ //if the file contains the file name without extension
						lUsedMB += files[i].length();
						alFiles.add(files[i]);
					}
				}
				//sort found files
				alFiles.remove(file);
				Collections.sort(alFiles);
				if ( (lUsedMB-file.length())/1048576 > iMB){  //too much backup files
				    // delete older backup
				    if (alFiles.size() > 0){
				        File fileToDelete = (File)alFiles.get(0);
				        if (fileToDelete != null){
				            fileToDelete.delete();
				        }
				    }
				}
			}
			//backup itself using nio, file name is collection-backup-yyyMMdd.xml
			String sExt = new SimpleDateFormat("yyyyMMdd").format(new Date()); //$NON-NLS-1$
			File fileNew = new File(Util.removeExtension(file.getAbsolutePath())+"-backup-"+sExt+"."+Util.getExtension(file)); //$NON-NLS-1$//$NON-NLS-2$
			FileChannel fcSrc = new FileInputStream(file).getChannel();
			FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
			fcDest.transferFrom(fcSrc, 0, fcSrc.size());
			fcSrc.close();
			fcDest.close();
		}
		catch(IOException ie){
			Log.error(ie);
		}
	}
	
	
	/**
	 * Copy a file to given directory
	 * @param file : file to copy
	 * @param directory : destination directory
	 */
	public static void copy(File file,File directory) throws Exception{
		Log.debug("Copying: "+file.getAbsolutePath() +"  to : "+directory.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
		File fileNew = new File(new StringBuffer(directory.getAbsolutePath()).append("/").append(file.getName()).toString()); //$NON-NLS-1$
		if ( !file.exists() || !file.canRead() ){
			throw new JajukException("023",file.getAbsolutePath(),null); //$NON-NLS-1$
		}
		if (  !fileNew.getParentFile().canWrite() ){
			throw new JajukException("024",file.getAbsolutePath(),null); //$NON-NLS-1$
		}
		FileChannel fcSrc = new FileInputStream(file).getChannel();
		FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
		fcDest.transferFrom(fcSrc, 0, fcSrc.size());
		fcSrc.close();
		fcDest.close();
	}
	
	/**
	 * Rename a file
	 * @param file : file to rename
	 * @param sNewName : file new name
	 */
	public static void copy(File file,String sNewName) throws Exception{
		Log.debug("Renaming: "+file.getAbsolutePath() +"  to : "+sNewName); //$NON-NLS-1$ //$NON-NLS-2$
		File fileNew = new File(new StringBuffer(file.getParentFile().getAbsolutePath()).append('/').append(sNewName).toString()); //$NON-NLS-1$
		if ( !file.exists() || !file.canRead() ){
			throw new JajukException("009",file.getAbsolutePath(),null); //$NON-NLS-1$
		}
		if (  !fileNew.getParentFile().canWrite() ){
			throw new JajukException("024",file.getAbsolutePath(),null); //$NON-NLS-1$
		}
		FileChannel fcSrc = new FileInputStream(file).getChannel();
		FileChannel fcDest = new FileOutputStream(fileNew).getChannel();
		fcDest.transferFrom(fcSrc, 0, fcSrc.size());
		fcSrc.close();
		fcDest.close();
	}

	
	
	/**
	 * Get current line. Wait until line appears ( with a time out ) 
	 * @return waited audio line
	 */	
	private static Line getCurrentLine(){
		Mixer mixer = AudioSystem.getMixer(null);
		Line line = null;
		int iTimeOut = 200;  //time out to exit line waiting and kill a calling thread 
		do{
			Line[] lines = mixer.getSourceLines();
			for (int i=0;i<lines.length;i++){
				if ( lines[i] instanceof MixerSourceLine ){
					line = lines[i];
					break;
				}
			}
			if ( iTimeOut > 0){
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					Log.error(e);
				}
				iTimeOut --;
			}
			else{
				return null;  //time out reached, leave 
			}
		}
		while(line == null);
		return line;
	}

	/**
	 * 
	 * @param sFileName
	 * @return whether the given filename is a standard cover or not
	 */
	public static boolean isStandardCover(String sFileName){
	    return sFileName.toLowerCase().matches(".*"+FILE_DEFAULT_COVER+".*") //$NON-NLS-1$ //$NON-NLS-2$
	    				|| sFileName.toLowerCase().matches(".*"+FILE_DEFAULT_COVER_2+".*") //$NON-NLS-1$ //$NON-NLS-2$
	    		        || sFileName.toLowerCase().matches(".*"+FILE_ABSOLUTE_DEFAULT_COVER+".*"); //just for previous compatibility, now it is a directory property  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Tell whether  a file is an absolute default cover or not
	 * @param directory Jajuk Directory in which we analyse the given file name 
	 * @param sFileName
	 * @return whether the given filename is an absolute default cover
	 */
	public static boolean isAbsoluteDefaultCover(Directory directory,String sFilename){
	    String sDefault = directory.getProperty("default_cover"); //$NON-NLS-1$
	    if (sDefault != null && sDefault.equals(sFilename)){
	        return true;
	    }
	   return false;
	}
	
	
	
	/**
	 * Return url of jar we are executing 
	 * @return URL of jar we are executing
	 */
	public static URL getJarLocation (Class cClass)  {
		return cClass.getProtectionDomain().getCodeSource().getLocation();
	}
	
	/**
	 * Additional file checkusm used to prevent bug 886098. Simply return 10 bytes read at the middle of the file
	 * <p> uses nio api for performances
	 * @return
	 */
	public static String getFileChecksum(File fio ){
		String sOut = ""; //$NON-NLS-1$
		try{
			FileChannel fc = new FileInputStream(fio).getChannel();
			ByteBuffer bb = ByteBuffer.allocate(10);
			fc.read(bb,fio.length()/2);
			fc.close();
			sOut = new String(bb.array());
		}
		catch(Exception e){
			Log.error("000",fio.getAbsolutePath(),e);	 //$NON-NLS-1$
		}
		return sOut;
	}
	
	/**
	 * Return only the name of a file from a complete URL 
	 * @param sPath
	 * @return
	 */
	public static String getOnlyFile(String sPath){
	    return new File(sPath).getName();
	}

	/**Return exec location*/
	public static String getExecLocation(){
		return sExecLocation;
	}
	
	/**
	 * Set exec location
	 * @param bDebug
	 */
	public static void setExecLocation(boolean bDebug){
		if ( bDebug){
			sExecLocation = "file:"+System.getProperty("user.dir")+"/jajuk.jar"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else{
			sExecLocation = Util.getJarLocation(Main.class).toString();
		}
	}
	
	
	/**
	 * @return whether we are under Windows
	 */
	public static boolean isUnderWindows(){
		String sOS = (String)System.getProperties().get("os.name"); //$NON-NLS-1$;
		if (sOS.trim().toLowerCase().lastIndexOf("windows")!=-1){ //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	/**
	 * @return whether we are under Linux
	 */
	public static boolean isUnderLinux(){
		String sOS = (String)System.getProperties().get("os.name"); //$NON-NLS-1$;
		if (sOS.trim().toLowerCase().lastIndexOf("linux")!=-1){ //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * Try to compute time length in milliseconds using BasicPlayer API. (code from jlGui 2.3)
	 */
	public static long getTimeLengthEstimation(Map properties)
	{
		long milliseconds = -1;
		int byteslength = -1;
		if (properties != null)
		{
			if (properties.containsKey("audio.length.bytes")) //$NON-NLS-1$
			{
				byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();			 //$NON-NLS-1$
			}
			if (properties.containsKey("duration")) //$NON-NLS-1$
			{
				milliseconds = (int) (((Long) properties.get("duration")).longValue())/1000;			 //$NON-NLS-1$
			}
			else
			{
				// Try to compute duration
				int bitspersample = -1;
				int channels = -1;
				float samplerate = -1.0f;
				int framesize = -1;			 
				if (properties.containsKey("audio.samplesize.bits")) //$NON-NLS-1$
				{
					bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue();  //$NON-NLS-1$
				}
				if (properties.containsKey("audio.channels")) //$NON-NLS-1$
				{
					channels = ((Integer) properties.get("audio.channels")).intValue();  //$NON-NLS-1$
				}
				if (properties.containsKey("audio.samplerate.hz")) //$NON-NLS-1$
				{
					samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue();  //$NON-NLS-1$
				}
				if (properties.containsKey("audio.framesize.bytes")) //$NON-NLS-1$
				{
					framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue();  //$NON-NLS-1$
				}
				if (bitspersample > 0)
				{
					milliseconds = (int) (1000.0f*byteslength/(samplerate * channels * (bitspersample/8))); 
				} 
				else
				{
					milliseconds = (int)(1000.0f*byteslength/(samplerate*framesize)); 
				}			
			}
		}
		return milliseconds;
	}
	
    
    /**
     * 
     * @param jc
     * @param iOrientation : vertical or horizontal orientation, use BoxLayout.X_AXIS or BoxLayout.Y_AXIS
     * @return a centred panel
     */
    public static JPanel getCentredPanel(JComponent jc,int iOrientation){
        JPanel jpOut = new JPanel();
        jpOut.setLayout(new BoxLayout(jpOut,iOrientation));
        if (iOrientation == BoxLayout.X_AXIS){
            jpOut.add(Box.createHorizontalGlue());
            jpOut.add(jc);
            jpOut.add(Box.createHorizontalGlue());
        }
        else{
            jpOut.add(Box.createVerticalGlue());
            jpOut.add(jc);
            jpOut.add(Box.createVerticalGlue());
        }
        jpOut.setMinimumSize(new Dimension(0,0)); //allow resing with info node
        return jpOut;
    }
    
    /**
     * 
     * @param jc
     * @return an horizontaly  centred panel
     */
    public static JPanel getCentredPanel(JComponent jc){
        return getCentredPanel(jc,BoxLayout.X_AXIS);
    }
    
    /**
     * @param file1 
     * @param file2
     * @return whether file1 is a file2 descendant
     */
    public static boolean isDescendant(File file1,File file2){
        File fParent = file1.getParentFile();
		boolean bOut = false;
		while (fParent != null){
		    if (fParent.equals(file2)){
		        bOut =true;
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
    public static boolean isAncestor(File file1,File file2){
        File fParent = file2.getParentFile();
		boolean bOut = false;
		while (fParent != null){
		    if (fParent.equals(file1)){
		        bOut = true;
		        break;
		    }
		    fParent = fParent.getParentFile();
		}
		return bOut;
    }
    
    /**
     * 
     * @param alFiles
     * @return Given list to play with shuffle or others runles applied
     */
    public static ArrayList applyPlayOption(ArrayList alFiles){
        if (ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE)){
            ArrayList alFilesToPlay = (ArrayList)alFiles.clone();
            Collections.shuffle(alFilesToPlay);
            return alFilesToPlay;
        }
        return alFiles; 
    }
      
    /**
     * Convert a list of files into a list of StackItem
     * @param alFiles
     * @param bRepeat
     * @param bUserLauched
     * @return
     */
    public static ArrayList createStackItems(ArrayList alFiles,boolean bRepeat,boolean bUserLauched){
        ArrayList alOut = new ArrayList(alFiles.size());
        Iterator it = alFiles.iterator();
        while (it.hasNext()){
            org.jajuk.base.File file = (org.jajuk.base.File)it.next();
            try{
                StackItem item = new StackItem(file);
                item.setRepeat(bRepeat);
                item.setUserLaunch(bUserLauched);
                alOut.add(item);
            }
            catch(JajukException je){
                Log.error(je);
            }
        }
        return alOut;
    }
	
}
