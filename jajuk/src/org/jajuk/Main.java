/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute 
 * it and/or modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,USA
 * $Revision$
 */
package org.jajuk;


import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jajuk.base.Collection;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.base.TypeManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.ui.LNFManager;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.PerspectiveManager;
import org.jajuk.ui.SplashScreen;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;


/**
 * Jajuk lauching class
 * 
 * @author bflorat @created 3 oct. 2003
 */
public class Main implements ITechnicalStrings {

	public static JFrame jframe;
	public static CommandJPanel command;
	public static PerspectiveBarJPanel perspectiveBar;
	public static InformationJPanel information;
	public static JPanel jpDesktop;
	public static JPanel jpFrame;
	public static SplashScreen sc;

	public static void main(String[] args) {
		try {
			//starts ui
			jframe = new JFrame("Jajuk : Just Another Jukebox"); //$NON-NLS-1$
			jframe.setIconImage(new ImageIcon(ICON_STYLE).getImage());
			
			//Launch splashscreen
			new Thread(){
				public void run(){
					sc = new SplashScreen(jframe);	
				}
			}.start();
		
			//Register locals
			Messages.registerLocal("en","Language_desc_en"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.registerLocal("fr","Language_desc_fr"); //$NON-NLS-1$ //$NON-NLS-2$
			
			//configuration manager startup
			org.jajuk.util.ConfigurationManager.getInstance();
					
			//check for jajuk home directory presence
			File fJajukDir = new File(FILE_JAJUK_DIR);
			if (!fJajukDir.exists() || !fJajukDir.isDirectory()) {
				fJajukDir.mkdir(); //create the directory if it doesn't exist
			}
			
			//log startup
			Log.getInstance();
			Log.setVerbosity(Log.DEBUG);
			
			//registers supported types
			try {
				TypeManager.registerType(Messages.getString("Main.Mpeg_layer_3_5"), EXT_MP3, PLAYER_IMPL_JAVALAYER, TAG_IMPL_MP3INFO, true); //$NON-NLS-1$ //$NON-NLS-2$
				TypeManager.registerType(Messages.getString("Main.Playlist_7"), EXT_PLAYLIST, PLAYER_IMPL_JAVALAYER, null, false); //$NON-NLS-1$ //$NON-NLS-2$
				TypeManager.registerType(Messages.getString("Main.Ogg_vorbis_9"), EXT_OGG, PLAYER_IMPL_JAVALAYER, null, true); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e1) {
				Log.error(Messages.getString("Main.Error_registering_players_11"), e1); //$NON-NLS-1$
			}
	
			//Registers supported look and feels
			LNFManager.register(LNF_METAL,LNF_METAL_CLASS); //$NON-NLS-1$
			LNFManager.register(LNF_GTK,LNF_GTK_CLASS); //$NON-NLS-1$
			LNFManager.register(LNF_WINDOWS,LNF_WINDOWS_CLASS);//$NON-NLS-1$
			LNFManager.register(LNF_KUNSTSTOFF,LNF_KUNSTSTOFF_CLASS);//$NON-NLS-1$
			LNFManager.register(LNF_LIQUID,LNF_LIQUID_CLASS);//$NON-NLS-1$
			
			//perform initial checkups
			initialCheckups();
			
			//Display user configuration
			Log.debug(System.getProperties().toString());
			
			//Load collection
			Collection.load();
		
			//	Clean the collection up
			org.jajuk.base.Collection.cleanup();
								
			//Load user configuration
			org.jajuk.util.ConfigurationManager.load();
			
			//Set actual log verbosity
			Log.setVerbosity(Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_LOG_LEVEL)));
			
			//Set look and feel
			LNFManager.setLookAndFeel(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
			
			//Set local
			Messages.setLocal(ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE));
			
			//Load history
			History.load();
			
			//Starts the FIFO
			FIFO.getInstance().start();
			
			jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			jframe.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					exit(0);
					return; 
				}
			});
			jpFrame = (JPanel)jframe.getContentPane();
			jpFrame.setLayout(new BorderLayout());
			jpFrame.setOpaque(true);
			
			//Creates the command panel
			command = CommandJPanel.getInstance();
	
			// Create the perspective tool bar panel
			perspectiveBar = PerspectiveBarJPanel.getInstance();
			// Create the information bar panel
			information = InformationJPanel.getInstance();
			//****temp
			information.setSelection("124 items : 4.5Mo"); //temp //$NON-NLS-1$
			//**************************
	
			//Main panel
			jpDesktop = new JPanel();
			jpDesktop.setOpaque(true);
			jpDesktop.setBorder(BorderFactory.createEtchedBorder());
			jpDesktop.setLayout(new BorderLayout());
		
			//Add static panels
			jpFrame.add(command, BorderLayout.NORTH);
			jpFrame.add(perspectiveBar, BorderLayout.WEST);
			jpFrame.add(information, BorderLayout.SOUTH);
			jpFrame.add(jpDesktop, BorderLayout.CENTER);
				
			//Set menu bar to the frame
			jframe.setJMenuBar(JajukJMenuBar.getInstance());
			
			//display window
			jframe.pack();
			jframe.setExtendedState(Frame.MAXIMIZED_BOTH);  //maximalize
			jframe.setVisible(true);
		
			//Mount and refresh devices
			mountAndRefresh();
					
			//Create the perspective manager 
			PerspectiveManager.load();
						
			//Initialize perspective manager and load all views
			PerspectiveManager.init();
					
			//Close splash screen
			sc.dispose();
			
			//Display info message if first session
			if (TRUE.equals(ConfigurationManager.getProperty(CONF_FIRST_CON))){
				ConfigurationManager.setProperty(CONF_FIRST_CON,FALSE);
				Messages.showInfoMessage("Main_first_connection");
				PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_CONFIGURATION);
				return;
			}
			
			//Display a message
			information.setMessage("Jajuk successfully started", InformationJPanel.INFORMATIVE); //$NON-NLS-1$
			
			//Lauch startup track if any
			launchInitialTrack();
				
		} catch (JajukException je) { //last chance to catch any error for logging purpose
			Log.error(je);
			exit(1);
		} catch (Exception e) { //last chance to catch any error for logging purpose
			Log.error("106", e); //$NON-NLS-1$
			exit(1);
		}
	}
	
	
	/**
	 * Performs some basic startup tests
	 * 
	 * @throws Exception
	 */
	private static void initialCheckups() throws Exception {
		try {
			//check for a concurrent jajuk session
			Socket socket = new Socket("127.0.0.1", 62321);  //try to connect to an existing socket server
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject("");
			//	No error? jajuk already started
			sc.dispose();
			Log.error(new JajukException("124"));
			Messages.showErrorMessage("124");	
			System.exit(-1);
			
		} catch (IOException e) { //error? looks like Jajuk is not started, lets start the listener 
			new Thread(){
				public void run(){
					try{
						ServerSocket ss = new ServerSocket(62321);
						ss.accept();
					}
					catch(Exception e){
					}
				}
			}.start();
		}
		//check for configuration file presence
		File fConfig = new File(FILE_CONFIGURATION);
		if (!fConfig.exists()) { //if config file doesn't exit, create it with default values
			org.jajuk.util.ConfigurationManager.commit();
		}
		//check for collection.xml file
		File fCollection = new File(FILE_COLLECTION);
		if (!fCollection.exists()) { //if collection file doesn't exit, create it empty
			Collection.commit();
		}
		//	check for perspectives.xml file
		File fPerspectives = new File(FILE_PERSPECTIVES_CONF);
		if (!fPerspectives.exists()) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fPerspectives));
			bw.write(XML_PERSPECTIVES_CONF);
			bw.close();
		}
		//check for history.xml file
		File fHistory = new File(FILE_HISTORY);
		if (!fHistory.exists()) { //if history file doesn't exit, create it empty
			History.commit();
		}
	}

	/**
	 * Exit code, used to perform saves...
	 * 
	 * @param iExitCode
	 *                exit code
	 *                <p>
	 *                0 : normal exit
	 *                <p>1: unexpected error
	 */
	public static void exit(int iExitCode) {
		try {
			if (Boolean.valueOf(ConfigurationManager.getProperty(CONF_CONFIRMATIONS_EXIT)).booleanValue()){
				int iResu = JOptionPane.showConfirmDialog(jframe,Messages.getString("Confirmation_exit"),Messages.getString("Main.16"),JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				if (iResu == JOptionPane.NO_OPTION){
					return;
				}
			}
			Log.debug("Exit with code: "+iExitCode); //$NON-NLS-1$
			if (iExitCode == 0){ //commit only if exit is safe to avoid commiting empty collection
				//commit configuration
				org.jajuk.util.ConfigurationManager.commit();
				//commit collection
				org.jajuk.base.Collection.commit();
				//commit history
				History.commit();
				
			}
		} catch (IOException e) {
			Log.error("", e); //$NON-NLS-1$
		}
		System.exit(iExitCode);
	}
	
	/**
	 * Launch initial track at startup
	 */
	private static void launchInitialTrack(){
		if (!ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOTHING)){
			if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST)){
				ArrayList alFiles = new ArrayList(1);
				alFiles.add(FileManager.getFile(History.getInstance().getLastFile()));
				FIFO.getInstance().push(alFiles,false);
			}
			else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_FILE)){
				//TODO implements file selection
			}
			else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_SHUFFLE)){
				org.jajuk.base.File file = FileManager.getShuffleFile();
				if (file != null){
					FIFO.getInstance().push(file,false);
				}
			}
		}
	}
	
	
	/**
	 * Auto-Mount and auto-refresh required devices
	 *
	 */
	private static void mountAndRefresh(){
		Iterator it = DeviceManager.getDevices().iterator();
		while (it.hasNext()){
			Device device = (Device)it.next();
			if (TRUE.equals(device.getProperty(DEVICE_OPTION_AUTO_MOUNT))){
				try{
					device.mount();
				}
				catch(Exception e){
					Log.error("112",device.getName(),e);
					Messages.showErrorMessage("112",device.getName());
				}
			}
			if (TRUE.equals(device.getProperty(DEVICE_OPTION_AUTO_REFRESH))){
				device.refresh();
			}
		}
	}
}
