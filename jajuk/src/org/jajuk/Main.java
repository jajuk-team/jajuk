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
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jajuk.base.Collection;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.Player;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.DeviceWizard;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.ui.JajukWindow;
import org.jajuk.ui.LNFManager;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.SplashScreen;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.tray.JajukSystray;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;


/**
 * Jajuk launching class
 * 
 * @author bflorat 
 * @created 3 oct. 2003
 */
public class Main implements ITechnicalStrings {
	
	/** Main window*/
	private static JajukWindow jw;
	/**Top command panel*/
	public static CommandJPanel command;
	/**Left side perspective selection panel*/
	public static PerspectiveBarJPanel perspectiveBar;
	/**Lower information panel*/
	public static InformationJPanel information;
	/**Main content pane*/
	public static JPanel jpContentPane;
	/**Main frame panel*/
	public static JPanel jpFrame;
	/**Jajuk slashscreen*/
	public static SplashScreen sc;
	/**Temporary panel*/
	private static JPanel jpTmp;
	/**Exit code*/
	private static int iExitCode = 0;
	/**Debug mode*/
	private static boolean bDebugMode = false;
	/**Exiting flag*/
	public static boolean bExiting = false;
	/**General use lock used for synchronization*/
	private static byte[] bLock = new byte[0];
	/**List of auto-refreshed devices */
	private static ArrayList alAutoRefreshedDevices = new ArrayList(4);
	/**Systray*/
	private static JajukSystray jsystray;
	/**UI lauched flag*/
	private static boolean bUILauched = false;
	/**No taskbar presence flag when window is minimized (only tray)*/
	private static boolean bNoTaskBar = false;
	/**default perspective to shoose, if null, we take the configuration one*/
	private static String sPerspective;
	/** Server socket used to check other sessions*/
	private static ServerSocket ss;
	/**
	 * Main entry
	 * @param args
	 */
	public static void main(final String[] args){
		//non ui init
		try{
		   //check JVM version
		    String sJVM = System.getProperty("java.vm.version"); //$NON-NLS-1$
		    if (sJVM.startsWith("1.0") || sJVM.startsWith("1.1") || sJVM.startsWith("1.2") || sJVM.startsWith("1.3") || sJVM.startsWith("1.4.0") || sJVM.startsWith("1.4.1") ){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		        System.out.println("Java Runtime Environment 1.4.2 minimum required. You use a JVM "+sJVM); //$NON-NLS-1$
		        System.exit(2); //error code 2 : wrong JVM
		    }
		    
		    //set command line options
		    for (int i=0;i<args.length;i++){
		        if (args[i].equals("-debug")){//$NON-NLS-1$
		            bDebugMode = true;
		        }
		        if (args[i].equals("-notaskbar")){//$NON-NLS-1$
		            bNoTaskBar = true;
		        }
		    }
		    
		    //set exec location path ( normal or debug )
			Util.setExecLocation(bDebugMode);//$NON-NLS-1$ 
			
			//check for jajuk home directory presence, needed by log
			File fJajukDir = new File(FILE_JAJUK_DIR);
			if (!fJajukDir.exists() || !fJajukDir.isDirectory()) {
				fJajukDir.mkdir(); //create the directory if it doesn't exist
			}
			
			// log startup
			Log.getInstance();
			Log.setVerbosity(Log.DEBUG);
			
			//Launch splashscreen 
			SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    sc = new SplashScreen(jw);
                  }
            });
				
			//Register locals, needed by ConfigurationManager to choose default language
			Messages.getInstance().registerLocal("en","Language_desc_en"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("fr","Language_desc_fr"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("de","Language_desc_de"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("it","Language_desc_it"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("sv","Language_desc_sv"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("nl","Language_desc_nl"); //$NON-NLS-1$ //$NON-NLS-2$
			Messages.getInstance().registerLocal("zh","Language_desc_zh"); //$NON-NLS-1$ //$NON-NLS-2$
			
			//configuration manager startup
			org.jajuk.util.ConfigurationManager.getInstance();
			
			//Upgrade configuration from previous releases
			upgrade();
			
			//Registers supported look and feels
			LNFManager.register(LNF_METAL,LNF_METAL_CLASS); 
			LNFManager.register(LNF_GTK,LNF_GTK_CLASS); 
			LNFManager.register(LNF_WINDOWS,LNF_WINDOWS_CLASS);
			LNFManager.register(LNF_KUNSTSTOFF,LNF_KUNSTSTOFF_CLASS);
			LNFManager.register(LNF_LIQUID,LNF_LIQUID_CLASS);
			LNFManager.register(LNF_METOUIA,LNF_METOUIA_CLASS);
					
			//perform initial checkups
			initialCheckups();
			
			//Load user configuration
			org.jajuk.util.ConfigurationManager.load();
		
			//Set actual log verbosity
			Log.setVerbosity(Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_LOG_LEVEL)));

			//Display user system configuration
			Log.debug(System.getProperties().toString());
			
			//Display user Jajuk configuration
			Log.debug(ConfigurationManager.getProperties().toString());

			//Set locale
			Messages.getInstance().setLocal(ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE));
		
			//Set look and feel, needs local to be set for error messages
			LNFManager.setLookAndFeel(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
			
			//Register device types
			DeviceManager.registerDeviceType(Messages.getString("Device_type.directory"));//$NON-NLS-1$
			DeviceManager.registerDeviceType(Messages.getString("Device_type.file_cd"));//$NON-NLS-1$
			DeviceManager.registerDeviceType(Messages.getString("Device_type.remote"));//$NON-NLS-1$
			DeviceManager.registerDeviceType(Messages.getString("Device_type.extdd"));//$NON-NLS-1$
			DeviceManager.registerDeviceType(Messages.getString("Device_type.player"));//$NON-NLS-1$
			
			//registers supported audio supports and default properties
			registerTypes();
			
			//Load collection
			Collection.load();
			
			//Clean the collection up
			Collection.cleanup();
			
			//check for another session
			checkOtherSession();
						
			//Load history
			History.load();
			
			//start exit hook
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try{
						if (iExitCode == 0){ //commit only if exit is safe (to avoid commiting empty collection)
							//commit configuration
							org.jajuk.util.ConfigurationManager.commit();
							//commit history
							History.commit();
							//commit perspectives
							PerspectiveManager.commit();
							//commit collection if not refreshing ( fix for 939816 )
							if ( !DeviceManager.isAnyDeviceRefreshing()){
								Collection.commit();
								//backup this file
								Util.backupFile(new File(FILE_COLLECTION),ConfigurationManager.getInt(CONF_BACKUP_SIZE));
							}
						}
					} catch (IOException e) {
						Log.error("", e); //$NON-NLS-1$
					}
				}
			});
					
			//Mount and refresh devices
			mountAndRefresh();
			
			//Launch startup track if any
			launchInitialTrack();        
			
			//lauch systray if needed, only for linux and windows, not mac for the moment
		    if (Util.isUnderLinux() || Util.isUnderWindows()){
		    	new Thread(){  //do it in a thread to avoid tray disparition under windows
		    		public void run(){
		    			jsystray = JajukSystray.getInstance();	
		    		}
		    	}.start();
		    }
			
			//show window if set in the systray conf
			if ( ConfigurationManager.getBoolean(CONF_SHOW_AT_STARTUP) ){
			    lauchUI();
			}
					
		} catch (JajukException je) { //last chance to catch any error for logging purpose
			Log.error(je);
			if ( je.getCode().equals("005")){ //$NON-NLS-1$
				Messages.showErrorMessage("005"); //$NON-NLS-1$
			}
			exit(1);
		} catch (Exception e) { //last chance to catch any error for logging purpose
			e.printStackTrace();
			Log.error("106", e); //$NON-NLS-1$
			exit(1);
		} catch (Error error) { //last chance to catch any error for logging purpose
		    error.printStackTrace();
		    Log.error("106", error); //$NON-NLS-1$
		    exit(1);
		}
		finally{  //make sure to close splashscreen in all cases
		    if (sc != null){
		        sc.dispose();
		    }
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
		//check for history.xml file
		File fHistory = new File(FILE_HISTORY);
		if (!fHistory.exists()) { //if history file doesn't exit, create it empty
			History.commit();
		}
	}
	
	
	/**
	 * Registers supported audio supports and default properties
	 */
	private static void registerTypes(){
		try {
			//mp3
			Type type = TypeManager.registerType(Messages.getString("Type.mp3"), EXT_MP3, PLAYER_IMPL_JAVALAYER, TAG_IMPL_JLGUI_MP3); //$NON-NLS-1$ //$NON-NLS-2$
			type.setProperty(TYPE_PROPERTY_IS_MUSIC,"true"); //$NON-NLS-1$
			type.setProperty(TYPE_PROPERTY_SEEK_SUPPORTED,"true"); //$NON-NLS-1$
			//playlists
			type = TypeManager.registerType(Messages.getString("Type.playlist"), EXT_PLAYLIST, PLAYER_IMPL_JAVALAYER, null); //$NON-NLS-1$ //$NON-NLS-2$
			type.setProperty(TYPE_PROPERTY_IS_MUSIC,"false"); //$NON-NLS-1$
			type.setProperty(TYPE_PROPERTY_SEEK_SUPPORTED,"false"); //$NON-NLS-1$
			//Ogg vorbis
			type = TypeManager.registerType(Messages.getString("Type.ogg"), EXT_OGG, PLAYER_IMPL_JAVALAYER, TAG_IMPL_JLGUI_OGG); //$NON-NLS-1$ //$NON-NLS-2$
			type.setProperty(TYPE_PROPERTY_IS_MUSIC,"true"); //$NON-NLS-1$
			type.setProperty(TYPE_PROPERTY_SEEK_SUPPORTED,"false"); //$NON-NLS-1$
			//Wave
			type = TypeManager.registerType(Messages.getString("Type.wav"), EXT_WAV, PLAYER_IMPL_JAVALAYER, TAG_IMPL_NO_TAGS); //$NON-NLS-1$ //$NON-NLS-2$
			type.setProperty(TYPE_PROPERTY_IS_MUSIC,"true"); //$NON-NLS-1$
			type.setProperty(TYPE_PROPERTY_SEEK_SUPPORTED,"true"); //$NON-NLS-1$
			//au
			type = TypeManager.registerType(Messages.getString("Type.au"), EXT_AU, PLAYER_IMPL_JAVALAYER, TAG_IMPL_NO_TAGS); //$NON-NLS-1$ //$NON-NLS-2$
			type.setProperty(TYPE_PROPERTY_IS_MUSIC,"true"); //$NON-NLS-1$
			type.setProperty(TYPE_PROPERTY_SEEK_SUPPORTED,"false"); //$NON-NLS-1$
			//aiff
			type = TypeManager.registerType(Messages.getString("Type.aiff"), EXT_AIFF, PLAYER_IMPL_JAVALAYER, TAG_IMPL_NO_TAGS); //$NON-NLS-1$ //$NON-NLS-2$
			type.setProperty(TYPE_PROPERTY_IS_MUSIC,"true"); //$NON-NLS-1$
			type.setProperty(TYPE_PROPERTY_SEEK_SUPPORTED,"false"); //$NON-NLS-1$
			
		} catch (Exception e1) {
			Log.error("026",e1); //$NON-NLS-1$
		}
	}
	
	/**
	 * check if another session is already started 
	 *
	 */
	private static void checkOtherSession(){
	    //check for a concurrent jajuk session, try to create a new server socket
        try{
    	    ss = new ServerSocket(62321);
            // 	No error? jajuk was not started, leave
        } catch (IOException e) { //error? looks like Jajuk is already started 
            sc.dispose();
            Log.error(new JajukException("124")); //$NON-NLS-1$
            Messages.showErrorMessage("124");	 //$NON-NLS-1$
            System.exit(-1);
        }
	    //start listening
        new Thread(){
	        public void run(){
	                try {
                        ss.accept();
                    } catch (IOException e) {
                        Log.error(e);
                    }
	        }
	    }.start();
	}
	
	/**
	 * Exit code, then system will execute the exit hook
	 * 
	 * @param iExitCode
	 *                exit code
	 *                <p>
	 *                0 : normal exit
	 *                <p>1: unexpected error
	 */
	public static void exit(int iExitCode) {
		//stop sound to avoid strange crash when stopping
	    Player.mute(true);
	    //set exiting flag
		bExiting = true;
		//store exit code to be read by the system hook
		Main.iExitCode = iExitCode;
		//force sound to stop quickly
		FIFO.getInstance().stopRequest();  
		ObservationManager.notify(EVENT_PLAYLIST_REFRESH); //alert playlists editors ( queue playlist ) something changed for him
		//hide window
		if (jw!=null) jw.setShown(false);
		//check if a confirmation is needed
		if (Boolean.valueOf(ConfigurationManager.getProperty(CONF_CONFIRMATIONS_EXIT)).booleanValue()){
			int iResu = JOptionPane.showConfirmDialog(jw,Messages.getString("Confirmation_exit"),Messages.getString("Main.21"),JOptionPane.YES_NO_OPTION);  //$NON-NLS-1$ //$NON-NLS-2$
			if (iResu == JOptionPane.NO_OPTION){
				return;
			}
		}
		//hide systray
		if (jsystray != null) jsystray.closeSystray();
		//display a message
		Log.debug("Exit with code: "+iExitCode); //$NON-NLS-1$
		System.exit(iExitCode);
	}
	
	/**
	 * Launch initial track at startup
	 */
	private static void launchInitialTrack(){
	    ArrayList alToPlay = new ArrayList();
		org.jajuk.base.File fileToPlay = null;
	    if (!ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOTHING)){
			if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST) ||
			        ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST_KEEP_POS) ||
			        ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_FILE)){
				
			    if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_FILE)){
				    fileToPlay = FileManager.getFile(ConfigurationManager.getProperty(CONF_STARTUP_FILE));
				}
				else {  //last file from begining or last file keep position
				    if (ConfigurationManager.getBoolean(CONF_STATE_WAS_PLAYING) && History.getInstance().getHistory().size()>0){  //make sure user didn't exit jajuk in the stopped state and that history is not void
				        fileToPlay = FileManager.getFile(History.getInstance().getLastFile());
				    }
				    else{ //do not try to lauch anything, stay in stop state
				        return;
				    }
				}
			   if (fileToPlay != null){
				    //if the required track is in a refreshed device, do not lauch anything, leave without boring error message 
				    if (alAutoRefreshedDevices.contains(fileToPlay.getDirectory().getDevice())){
				        Log.debug("Startup file is in an auto-refreshed device, leave"); //$NON-NLS-1$
				        return;
				    }
				    else{
				        alToPlay.add(fileToPlay);    
				    }
				}
				else{ //file no more exists
		            String sup = null;
		            if (fileToPlay != null){
		                sup =fileToPlay.getDirectory().getDevice().getName();
		            }
				    Messages.showErrorMessage("009",sup); //$NON-NLS-1$
				}
			}
			else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_SHUFFLE)){
				alToPlay = FileManager.getGlobalShufflePlaylist();
			}
			else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_BESTOF)){
			    alToPlay = FileManager.getGlobalBestofPlaylist();
			}
			else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOVELTIES)){
			    alToPlay = FileManager.getGlobalNoveltiesPlaylist();
			}
			//launch selected file
			if (alToPlay  != null && alToPlay.size() >0){
				FIFO.getInstance().push(Util.createStackItems(alToPlay,
						ConfigurationManager.getBoolean(CONF_STATE_REPEAT),false),false);
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
					//show a confirm dialog if the device can't be mounted, we can't use regular Messages.showErrprMessage because main window is not yet displayed
					String sMessage = Messages.getErrorMessage("112")+" : "+device.getName(); //$NON-NLS-1$ //$NON-NLS-2$ 
					JOptionPane.showMessageDialog(null,sMessage,Messages.getString("Error"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					continue;
				}
			}
			if (TRUE.equals(device.getProperty(DEVICE_OPTION_AUTO_REFRESH))){
				alAutoRefreshedDevices.add(device);
			    device.refresh(true);
			}
		}
	}
	
	
	/**
	 * @return Returns the main window.
	 */
	public static JajukWindow getWindow() {
		return jw;
	}
	
	/**
	 * Actions to migrate an existing installation
	 *
	 */
	public static void upgrade() throws Exception {
		//--For jajuk < 0.2 : remove backup file : collection~.xml
		File file = new File(FILE_COLLECTION+"~"); //$NON-NLS-1$
		if ( file!= null ){
			file.delete();
		}
	}
	
	/**
	 * @return Returns the bDebugMode.
	 */
	public static boolean isDebugMode() {
		return bDebugMode;
	}
	
    /**
     * @return Returns whether jajuk is in exiting state
     */
    public static boolean isExiting() {
        return bExiting;
    }
    
    /**
     * Lauch UI
     */
    public static void lauchUI() throws Exception{
        if (bUILauched){
            return;
        }
        //ui init 
		SwingUtilities.invokeAndWait(new Runnable() { //use invokeAndWait to get a better progressive ui display
		   public void run(){
				try {
				    //starts ui
					jw = JajukWindow.getInstance();
				    jw.setCursor(Util.WAIT_CURSOR);
							
					//Creates the panel
					jpFrame = (JPanel)jw.getContentPane();
					jpFrame.setOpaque(true);
					jpFrame.setLayout(new BorderLayout());
					
					//Set menu bar to the frame
					jw.setJMenuBar(JajukJMenuBar.getInstance());
		
					//create the command bar
					command = CommandJPanel.getInstance();
					
					// Create the information bar panel
					information = InformationJPanel.getInstance();
					
					//Main panel
					jpContentPane = new JPanel();
					jpContentPane.setOpaque(true);
					jpContentPane.setBorder(BorderFactory.createEtchedBorder());
					jpContentPane.setLayout(new BorderLayout());
					
					//Add static panels
					jpFrame.add(command, BorderLayout.NORTH);
					jpFrame.add(information, BorderLayout.SOUTH);
					jpTmp = new JPanel(); //we use an empty panel to take west place before actual panel ( perspective bar ). just for a better displaying
					jpTmp.setPreferredSize(new Dimension(3000,3000));//make sure the temp panel makes screen maximalized
					jpFrame.add(jpTmp, BorderLayout.CENTER);
				
					//Create the perspective manager 
					PerspectiveManager.load();

					// Create the perspective tool bar panel
					perspectiveBar = PerspectiveBarJPanel.getInstance();
					jpFrame.add(perspectiveBar, BorderLayout.WEST);
					
					//display window
					jw.pack();
					jw.setExtendedState(Frame.MAXIMIZED_BOTH);  //maximalize
					jw.setVisible(true); //show main window
					sc.toFront();
					
					//Display info message if first session
					if (ConfigurationManager.getBoolean(CONF_FIRST_CON)){
						ConfigurationManager.setProperty(CONF_FIRST_CON,FALSE);
						Messages.showInfoMessage(Messages.getString("Main.12")); //$NON-NLS-1$
						//set parameter perspective
					    PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_CONFIGURATION);
						//show device creation wizard
						DeviceWizard dw = new DeviceWizard();
						dw.updateWidgetsDefault();
						dw.pack();
						dw.setVisible(true);
					}
							
				} catch (Exception e) { //last chance to catch any error for logging purpose
					e.printStackTrace();
					Log.error("106", e); //$NON-NLS-1$
				}
			}
		});
	
		Thread.sleep(2000);  //make sure static panels are drawed
		
		//Initialize perspective manager and load all views
		//display window
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jpFrame.remove(jpTmp);	
                jpFrame.add(jpContentPane, BorderLayout.CENTER);
                PerspectiveManager.init();
                jw.setCursor(Util.DEFAULT_CURSOR);
            }
        });
		
		//Close splash screen
		sc.dispose();
		
        bUILauched = true;
    }
    
    /**
     * @return Returns the bUILauched.
     */
    public static boolean isUILauched() {
        return bUILauched;
    }
    
    /**
     * @return Returns the bForceTaskBar.
     */
    public static boolean isNoTaskBar() {
        return bNoTaskBar;
    }
    /**
     * @return Returns the sPerspective.
     */
    public static String getDefaultPerspective() {
        return sPerspective;
    }
    /**
     * @param perspective The sPerspective to set.
     */
    public static void setDefaultPerspective(String perspective) {
        sPerspective = perspective;
    }
}
