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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;

import javax.swing.BorderFactory;
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
import org.jajuk.ui.JajukWindow;
import org.jajuk.ui.LNFManager;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.SplashScreen;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;


/**
 * Jajuk launching class
 * 
 * @author bflorat @created 3 oct. 2003
 */
public class Main implements ITechnicalStrings {
	
	private static JajukWindow jw;
	public static CommandJPanel command;
	public static PerspectiveBarJPanel perspectiveBar;
	public static InformationJPanel information;
	public static JPanel jpDesktop;
	public static JPanel jpFrame;
	public static SplashScreen sc;
	
	public static void main(String[] args)  {
		try {
			//set exec location path ( normal or debug )
			Util.setExecLocation((args.length>0 && args[0].equals("-debug")));//$NON-NLS-1$ 
			
			//starts ui
			jw = new JajukWindow(); 
			
			//Launch splashscreen
			sc = new SplashScreen(jw);	
			
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
				TypeManager.registerType(Messages.getString("Type.mp3"), EXT_MP3, PLAYER_IMPL_JAVALAYER, TAG_IMPL_MP3INFO, true); //$NON-NLS-1$ //$NON-NLS-2$
				TypeManager.registerType(Messages.getString("Type.playlist"), EXT_PLAYLIST, PLAYER_IMPL_JAVALAYER, null, false); //$NON-NLS-1$ //$NON-NLS-2$
				TypeManager.registerType(Messages.getString("Type.ogg"), EXT_OGG, PLAYER_IMPL_JAVALAYER, null, true); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e1) {
				Log.error("026",e1); //$NON-NLS-1$
			}
			
			//Registers supported look and feels
			LNFManager.register(LNF_METAL,LNF_METAL_CLASS); 
			LNFManager.register(LNF_GTK,LNF_GTK_CLASS); 
			LNFManager.register(LNF_WINDOWS,LNF_WINDOWS_CLASS);
			LNFManager.register(LNF_KUNSTSTOFF,LNF_KUNSTSTOFF_CLASS);
			LNFManager.register(LNF_LIQUID,LNF_LIQUID_CLASS);
								
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
			
			//Set look and feel
			LNFManager.setLookAndFeel(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
		
			//check for another session
			checkOtherSession();
			
			//Set actual log verbosity
			Log.setVerbosity(Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_LOG_LEVEL)));
			
			//Set local
			Messages.setLocal(ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE));
			
			//Load history
			History.load();
			
			//Starts the FIFO
			FIFO.getInstance();
			
			//Creates the panel
			jpFrame = (JPanel)jw.getContentPane();
			jpFrame.setOpaque(true);
			jpFrame.setLayout(new BorderLayout());
			
			//create the command bar
			javax.swing.SwingUtilities.invokeAndWait(new Runnable() { //use invoke and wait to fix bug 910376 
				public void run() {
					command = CommandJPanel.getInstance();
				}
			});
			// Create the information bar panel
			information = InformationJPanel.getInstance();
					
			//Main panel
			jpDesktop = new JPanel();
			jpDesktop.setOpaque(true);
			jpDesktop.setBorder(BorderFactory.createEtchedBorder());
			jpDesktop.setLayout(new BorderLayout());
			
			//Add static panels
			jpFrame.add(command, BorderLayout.NORTH);
			jpFrame.add(information, BorderLayout.SOUTH);
			jpFrame.add(jpDesktop, BorderLayout.CENTER);
			JPanel jp = new JPanel(); //we use an empty panel to take west place before actual panel ( perspective bar ). just for a better displaying
			jp.setPreferredSize(new Dimension(700,(int)(0.78*Toolkit.getDefaultToolkit().getScreenSize().getHeight())));
			jpFrame.add(jp, BorderLayout.WEST);
			
			//Set menu bar to the frame
			jw.setJMenuBar(JajukJMenuBar.getInstance());
			
			//display window
			jw.pack();
			jw.setExtendedState(Frame.MAXIMIZED_BOTH);  //maximalize
			//show window if set in the systray conf
			if ( ConfigurationManager.getBoolean(CONF_SHOW_AT_STARTUP)){
				jw.setVisible(true);
			}
			//Mount and refresh devices
			mountAndRefresh();
			
			//Create the perspective manager 
			PerspectiveManager.load();
			
			// Create the perspective tool bar panel
			perspectiveBar = PerspectiveBarJPanel.getInstance();
			jpFrame.remove(jp);
			jpFrame.add(perspectiveBar, BorderLayout.WEST);
			
			//Initialize perspective manager and load all views
			PerspectiveManager.init();
			
			//Close splash screen
			sc.dispose();
			
			//Display info message if first session
			if (TRUE.equals(ConfigurationManager.getProperty(CONF_FIRST_CON))){
				ConfigurationManager.setProperty(CONF_FIRST_CON,FALSE);
				Messages.showInfoMessage(Messages.getString("Main.12")); //$NON-NLS-1$
				PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_CONFIGURATION);
				return;
			}
			
			//Display a message
			information.setMessage(Messages.getString("Main.13"), InformationJPanel.INFORMATIVE);  //$NON-NLS-1$
			
			//Launch startup track if any
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
		else{
			//Save collection asynchronously
			new Thread(){
				public void run(){
					long l = System.currentTimeMillis();
					Util.saveFile(new File(FILE_COLLECTION));
					Log.debug("Saved collection file in "+(System.currentTimeMillis()-l)+" ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}.start();
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
	 * check if another session is already started 
	 *
	 */
	private static void checkOtherSession(){
		try {
			//check for a concurrent jajuk session
			Socket socket = new Socket("127.0.0.1", 62321);  //try to connect to an existing socket server //$NON-NLS-1$
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(""); //$NON-NLS-1$
			//	No error? jajuk already started
			sc.dispose();
			Log.error(new JajukException("124")); //$NON-NLS-1$
			Messages.showErrorMessage("124");	 //$NON-NLS-1$
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
			//check if a confirmation is needed
			if (Boolean.valueOf(ConfigurationManager.getProperty(CONF_CONFIRMATIONS_EXIT)).booleanValue()){
				int iResu = JOptionPane.showConfirmDialog(jw,Messages.getString("Confirmation_exit"),Messages.getString("Main.21"),JOptionPane.YES_NO_OPTION);  //$NON-NLS-1$ //$NON-NLS-2$
				if (iResu == JOptionPane.NO_OPTION){
					return;
				}
			}
			 //hide systray
             jw.closeSystray();
			//display a message
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
		org.jajuk.base.File file = null;
		if (!ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOTHING)){
			if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST)){
				file = FileManager.getFile(History.getInstance().getLastFile());
			}
			else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_FILE)){
				file = FileManager.getFile(ConfigurationManager.getProperty(CONF_STARTUP_FILE));
			}
			else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_SHUFFLE)){
				file = FileManager.getShuffleFile();
			}
			//launch selected file
			if (file != null && file.isReady()){
				FIFO.getInstance().push(file,false);
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
					Log.error("112",device.getName(),e); //$NON-NLS-1$
					Messages.showErrorMessage("112",device.getName()); //$NON-NLS-1$
					continue;
				}
			}
			if (TRUE.equals(device.getProperty(DEVICE_OPTION_AUTO_REFRESH))){
				device.refresh(true);
			}
		}
	}
	/**
	 * @return Returns the jw.
	 */
	public static JajukWindow getWindow() {
		return jw;
	}

}
