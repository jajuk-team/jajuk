/*
 *  Jajuk
 *  Copyright (C) 2003 Administrateur
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

package org.jajuk.ui.tray;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jajuk.Main;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukWindow;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

/**
 *  Jajuk systray
 *
 * @author     Administrateur
 * @created    22 sept. 2004
 */
public class JajukSystray implements ITechnicalStrings,Observer,ActionListener{
	//Systray variables
	SystemTray stray = SystemTray.getDefaultSystemTray();;
	TrayIcon trayIcon;
    JPopupMenu jmenu;
	JMenuItem jmiExit;
	JMenuItem jmiAbout;
	JMenuItem jmiShuffle;
	JMenuItem jmiBestof;
	JMenuItem jmiNovelties;
	JMenuItem jmiNorm;
	JMenuItem jmiPause;
	JMenuItem jmiStop;
	JMenuItem jmiPrevious;
	JMenuItem jmiNext;
	JMenuItem jmiOut;
	/**Visible at startup?*/
	JCheckBoxMenuItem jcbmiVisible;
	/**Pause status*/
	private boolean bPaused = false;
	/**Self instance singleton*/
	private static JajukSystray jsystray;
	
	
	/**
	 * 
	 * @return singleton
	 */
	public static JajukSystray getInstance(){
		if (jsystray == null){
			jsystray = new JajukSystray();
		}
		return jsystray;
	}
	
	/**
	 * Systray constructor
	 *
	 */
	public JajukSystray(){
			URL url = null;
			try {
				url = new URL(ICON_LOGO_ICO);
			} catch (MalformedURLException e) {
				Log.error(e);
			}
			jmenu = new JPopupMenu(Messages.getString("JajukWindow.3")); //$NON-NLS-1$
			jmiExit =  new JMenuItem(Messages.getString("JajukWindow.4")); //$NON-NLS-1$
			jmiExit.addActionListener(this);
			jmiAbout =  new JMenuItem(Messages.getString("JajukWindow.5")); //$NON-NLS-1$
			jmiAbout.addActionListener(this);
			jmiShuffle =  new JMenuItem(Messages.getString("JajukWindow.6")); //$NON-NLS-1$
			jmiShuffle.addActionListener(this);
			jmiBestof =  new JMenuItem(Messages.getString("JajukWindow.7")); //$NON-NLS-1$
			jmiBestof.addActionListener(this);
			jmiNorm =  new JMenuItem(Messages.getString("JajukWindow.16")); //$NON-NLS-1$
			jmiNorm.addActionListener(this);
			jmiNovelties =  new JMenuItem(Messages.getString("JajukWindow.15")); //$NON-NLS-1$
			jmiNovelties.addActionListener(this);
			jcbmiVisible =  new JCheckBoxMenuItem(Messages.getString("JajukWindow.8")); //$NON-NLS-1$
			jcbmiVisible.setState(JajukWindow.getInstance().isVisible()); 
			jcbmiVisible.addActionListener(this);
			jmiPause = new JMenuItem(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
			jmiPause.addActionListener(this);
			jmiStop = new JMenuItem(Messages.getString("JajukWindow.11")); //$NON-NLS-1$
			jmiStop.addActionListener(this);
			jmiPrevious = new JMenuItem(Messages.getString("JajukWindow.13")); //$NON-NLS-1$
			jmiPrevious.addActionListener(this);
			jmiNext = new JMenuItem(Messages.getString("JajukWindow.14")); //$NON-NLS-1$
			jmiNext.addActionListener(this);
			jmiOut = new JMenuItem(" "); //$NON-NLS-1$
			
			jmenu.add(jcbmiVisible);
			jmenu.addSeparator();
			jmenu.add(jmiPause);
			jmenu.add(jmiStop);
			jmenu.add(jmiPrevious);
			jmenu.add(jmiNext);
			jmenu.addSeparator();
			jmenu.add(jmiShuffle);
			jmenu.add(jmiBestof);
			jmenu.add(jmiNovelties);
			jmenu.add(jmiNorm);
			jmenu.addSeparator();
			jmenu.add(jmiAbout);
			jmenu.addSeparator();
			jmenu.add(jmiExit);
			jmenu.add(jmiExit);
			jmenu.add(jmiOut);
			
			trayIcon = new TrayIcon(Util.getIcon(ICON_LOGO_TRAY),Messages.getString("JajukWindow.18"),jmenu); //$NON-NLS-1$);
			trayIcon.setIconAutoSize(true);
			trayIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    //hide menu if opened
                    jmenu.setVisible(false);
                    //show window if it is not visible and hide it if it is visible
                    if (!JajukWindow.getInstance().isVisible()){
                        //start ui if needed
                        if (!Main.isUILauched()){
                            new Thread(){
                                public void run(){
                                    try {
                                        Main.lauchUI();
                                    } catch (Exception e) {
                                        Log.error(e);
                                    }
                                }
                            }.start();
                        }
                        else{
                            JajukWindow.getInstance().setVisible(true);
                        }
                    }
                    else{
                        JajukWindow.getInstance().setVisible(false);
                    }
                }
            });
			stray.addTrayIcon(trayIcon);
			
			//Register needed events
			ObservationManager.register(EVENT_ZERO,this);
			ObservationManager.register(EVENT_FILE_LAUNCHED,this);
			ObservationManager.register(EVENT_PLAYER_PAUSE,this);
			ObservationManager.register(EVENT_PLAYER_PLAY,this);
			ObservationManager.register(EVENT_PLAYER_RESUME,this);
			ObservationManager.register(EVENT_PLAYER_STOP,this);
			
			//check if a fiel has been already started
			if (FIFO.getInstance().getCurrentFile() == null){
			    update(EVENT_PLAYER_STOP);    
			}
			else{
			    update(EVENT_FILE_LAUNCHED);    
			}
	}
	
	/**
	 * ActionListener
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jmiExit){
			Main.exit(0);
		}
		else if (e.getSource() == jmiAbout){
			//make frame visible
			if ( !JajukWindow.getInstance().isVisible()){
				JajukWindow.getInstance().setVisible(true);
			}
			//set help perspectievb to show "about" view
			PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_HELP);
		}
		else if (e.getSource() == jmiShuffle){
			ArrayList alToPlay = FileManager.getGlobalShufflePlaylist();
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_SHUFFLE);
			pDetails.put(DETAIL_SELECTION,alToPlay);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);
		}
		else if (e.getSource() == jmiBestof){
			ArrayList alToPlay = FileManager.getGlobalBestofPlaylist();
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_BESTOF);
			pDetails.put(DETAIL_SELECTION,alToPlay);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);		
		}
		else if (e.getSource() == jmiNovelties){
			ArrayList alToPlay = FileManager.getGlobalNoveltiesPlaylist();
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_NOVELTIES);
			pDetails.put(DETAIL_SELECTION,alToPlay);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);
		}
		else if (e.getSource() == jmiNorm){
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_NORMAL);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);
		}
		else if (e.getSource() == jcbmiVisible){
			ConfigurationManager.setProperty(CONF_SHOW_AT_STARTUP,Boolean.toString(jcbmiVisible.getState()));
		}
		else if (e.getSource() == jmiPrevious){
			FIFO.getInstance().playPrevious();
		}
		else if (e.getSource() == jmiNext){
			FIFO.getInstance().playNext();
		}
		else if (e.getSource() == jmiStop){
			FIFO.getInstance().stopRequest();
			ObservationManager.notify(EVENT_PLAYLIST_REFRESH); //alert playlists editors ( queue playlist ) something changed for him
		}
		else if (e.getSource() == jmiPause){
		    if ( Player.isPaused()){  //player was paused, resume it
		        bPaused = false;
				Player.resume();
				jmiPause.setText(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
				ObservationManager.notify(EVENT_PLAYER_RESUME);  //notify of this event
			}
		    else{ //player is not paused, pause it
		        Player.pause();
		        bPaused = true;
		        jmiPause.setText(Messages.getString("JajukWindow.12")); //$NON-NLS-1$
		        ObservationManager.notify(EVENT_PLAYER_PAUSE);  //notify of this event
		    }
		}
	}
	
	
	/**
	 * Set systray tooltip
	 * @param s
	 */
	public void setTooltip(String s){
		try{
			if ( trayIcon != null){
				trayIcon.setToolTip(s);
			}
		}
		catch(Exception e){
			Log.error(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
		if (EVENT_ZERO.equals(subject)){
			trayIcon.setToolTip(Messages.getString("JajukWindow.18")); //$NON-NLS-1$
		}
		else if (EVENT_FILE_LAUNCHED.equals(subject)){
		    File file  = FileManager.getFile((String)ObservationManager.getDetail(EVENT_FILE_LAUNCHED,DETAIL_CURRENT_FILE_ID));
		    String sOut = "";
		    if (file != null ){
		        sOut = file.getTrack().getName(); 
		    }
		    else{
		        sOut = Messages.getString("JajukWindow.18"); //$NON-NLS-1$
		    }
		    
		    trayIcon.setToolTip(sOut);
		}
        else if( EVENT_PLAYER_STOP.equals(subject) || EVENT_ZERO.equals(subject)){
            jmiPause.setEnabled(false);
            jmiStop.setEnabled(false);
            jmiPause.setText(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
        }
        else if ( EVENT_PLAYER_PLAY.equals(subject)){
            jmiPause.setEnabled(true);
            jmiStop.setEnabled(true);
            jmiPause.setText(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
         }
        else if ( EVENT_PLAYER_PAUSE.equals(subject)){
            jmiPause.setText(Messages.getString("JajukWindow.12")); //$NON-NLS-1$
        }
        else if ( EVENT_PLAYER_RESUME.equals(subject)){
            jmiPause.setText(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
        }
   }
	
	/**
	 * Hide systray 
	 *
	 */
	public void closeSystray(){
		if ( stray != null && trayIcon != null ){
			stray.removeTrayIcon(trayIcon);
		}
	}

}
