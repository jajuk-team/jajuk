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
 *  $Revision$
 */
package org.jajuk.util;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import com.sun.media.sound.MixerSourceLine;

/**
 * General use utilities methods
 * 
 * @author bflorat @created 12 oct. 2003
 */
public class Util implements ITechnicalStrings {
	
	/*Cursors*/
	public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
	public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
	
	/**
	 * Genres
	 */
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
	
	/** Mute state*/
	private static boolean bMute;
	
	/** Volume ( master gain )*/ 
	private static float fVolume = 50.0f;
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
			throw new JajukException("009",e);
		}
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			JajukException te = new JajukException("009", path, e);
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
			JajukException te = new JajukException("009", path, e);
			throw te;
		}

		// Close the bufferedReader
		try {
			input.close();
		} catch (IOException e) {
			JajukException te = new JajukException("009", path, e);
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
			JajukException te = new JajukException("009", e);
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
	
	/**Format a time (ms) to a human readable format*/
	public static String formatTime(long l){
		String sOut = new SimpleDateFormat("HH:mm:ss").format(new Date(l+82800000));
		return sOut;
	}
	
	/**
	 * Shake a list and return a randomized new list
	 * @param alIn
	 * @return
	 */
	public static ArrayList randomize(ArrayList alIn){
		int[] iNewIndexes = new int[alIn.size()];
		int size = alIn.size();
		for (int i=0;i<size;i++){
			iNewIndexes[i]= -1;
		}
		for (int i=0;i<size;i++){
			int newIndex = 0;
			boolean bOK = false;
			while (!bOK){
				newIndex = (int)(Math.random()*size);
				iNewIndexes[i]=newIndex;
				bOK = true;
				for (int j=0;j<size;j++){ //check this index is not already token
					if ( j!= i && iNewIndexes[j] == newIndex){
						bOK = false;
						break;
					}
				}
			}
		}			
		ArrayList alOut = (ArrayList)alIn.clone();
		for (int i =0;i<size;i++){
			alOut.set(i,alIn.get(iNewIndexes[i]));
		}
		return alOut;
	}
	
	/**
	 * Set current cursor as waiting cursor
	 */
	public static void waiting(){
		Main.jframe.setCursor(WAIT_CURSOR);
	}

	/**
	 * Set current cursor as default cursor
	 */
	public static void stopWaiting(){
		SwingUtilities.invokeLater(new Runnable(){  //actually change cursor when last repaint in awt repaint thread fifo is done 
			public void run(){
				Main.jframe.setCursor(DEFAULT_CURSOR);
			}
		});
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
	 * Save a file in the same directory with name <filename>~
	 * @param file
	 */
	public static void saveFile(File file){
		try{
			File fileNew = new File(file.getAbsolutePath()+"~");
			BufferedReader br = new BufferedReader(new FileReader(file));
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileNew));
			String sLine = null;
			while ((sLine=br.readLine())!=null){
				bw.write(sLine+"\n");
			}
			br.close();
			bw.flush();
			bw.close();
			
		}
		catch(IOException ie){
			Log.error(ie);
		}
	}
	
	/**
	 * Set volume in % ( ex: 10.0 )
	 * @param fVolume
	 */
	public static void setVolume(float fVolume){
		try {
			Line line = getCurrentLine();	FloatControl  volCtrl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
			float fCurrent = 0.0f; 
			if ( fVolume<=50){
				fCurrent = Math.abs(volCtrl.getMinimum()*2*(fVolume/100)) + volCtrl.getMinimum();
			}
			else{
				fCurrent = volCtrl.getMaximum()*(fVolume/100);
			}
			volCtrl.setValue(fCurrent);
			Util.fVolume = fVolume;
		} catch (Exception e) {
			Log.error(e);
		}
	}

	/**
	 * Set mute state
	 * @param bMute
	 */
	public static void setMute(boolean bMute){
		try{
			Line line = getCurrentLine();
			BooleanControl  bc = (BooleanControl)line.getControl(BooleanControl.Type.MUTE);
			bc.setValue(bMute);
			Util.bMute = bMute;
		} catch (Exception e) {
			Log.error(e);
		}
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
	 * Get mute state
	 * @return
	 */
	public static boolean getMute(){
		return bMute;
	}
	
	/**
	 * @return Returns the volume in %
	 */
	public static float getVolume() {
		return fVolume;
	}

}
