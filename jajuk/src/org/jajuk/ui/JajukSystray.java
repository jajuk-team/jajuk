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

package org.jajuk.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import org.jajuk.Main;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import snoozesoft.systray4j.CheckableMenuItem;
import snoozesoft.systray4j.SysTrayMenu;
import snoozesoft.systray4j.SysTrayMenuEvent;
import snoozesoft.systray4j.SysTrayMenuIcon;
import snoozesoft.systray4j.SysTrayMenuItem;
import snoozesoft.systray4j.SysTrayMenuListener;

/**
 *  Jajuk systray
 *
 * @author     Administrateur
 * @created    22 sept. 2004
 */
public class JajukSystray implements ITechnicalStrings,SysTrayMenuListener,Observer{
	//Systray variables
	SysTrayMenuIcon stmi;
	SysTrayMenu stm;
	SysTrayMenuItem stmiExit;
	SysTrayMenuItem stmiAbout;
	SysTrayMenuItem stmiShuffle;
	SysTrayMenuItem stmiBestof;
	SysTrayMenuItem stmiNovelties;
	SysTrayMenuItem stmiNorm;
	SysTrayMenuItem stmiPause;
	SysTrayMenuItem stmiStop;
	SysTrayMenuItem stmiPrevious;
	SysTrayMenuItem stmiNext;
	/**Visible at startup?*/
	CheckableMenuItem cmiVisible;
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
		//systray, only for window for now
		if (Util.isUnderWindows()){
			URL url = null;
			try {
				url = new URL(ICON_LOGO_ICO);
			} catch (MalformedURLException e) {
				Log.error(e);
			}
			stmi = new SysTrayMenuIcon(url);
			stmi.addSysTrayMenuListener(this);
			stm = new SysTrayMenu(stmi,Messages.getString("JajukWindow.3")); //$NON-NLS-1$
			stmiExit =  new SysTrayMenuItem(Messages.getString("JajukWindow.4")); //$NON-NLS-1$
			stmiExit.addSysTrayMenuListener(this);
			stmiAbout =  new SysTrayMenuItem(Messages.getString("JajukWindow.5")); //$NON-NLS-1$
			stmiAbout.addSysTrayMenuListener(this);
			stmiShuffle =  new SysTrayMenuItem(Messages.getString("JajukWindow.6")); //$NON-NLS-1$
			stmiShuffle.addSysTrayMenuListener(this);
			stmiBestof =  new SysTrayMenuItem(Messages.getString("JajukWindow.7")); //$NON-NLS-1$
			stmiBestof.addSysTrayMenuListener(this);
			stmiNorm =  new SysTrayMenuItem(Messages.getString("JajukWindow.16")); //$NON-NLS-1$
			stmiNorm.addSysTrayMenuListener(this);
			stmiNovelties =  new SysTrayMenuItem(Messages.getString("JajukWindow.15")); //$NON-NLS-1$
			stmiNovelties.addSysTrayMenuListener(this);
			cmiVisible =  new CheckableMenuItem(Messages.getString("JajukWindow.8")); //$NON-NLS-1$
			cmiVisible.setState(JajukWindow.getInstance().isVisible()); 
			cmiVisible.addSysTrayMenuListener(this);
			stmiPause = new SysTrayMenuItem(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
			stmiPause.addSysTrayMenuListener(this);
			stmiStop = new SysTrayMenuItem(Messages.getString("JajukWindow.11")); //$NON-NLS-1$
			stmiStop.addSysTrayMenuListener(this);
			stmiPrevious = new SysTrayMenuItem(Messages.getString("JajukWindow.13")); //$NON-NLS-1$
			stmiPrevious.addSysTrayMenuListener(this);
			stmiNext = new SysTrayMenuItem(Messages.getString("JajukWindow.14")); //$NON-NLS-1$
			stmiNext.addSysTrayMenuListener(this);
			stm.addItem(stmiExit);
			stm.addSeparator();
			stm.addItem(stmiAbout);
			stm.addSeparator();
			stm.addItem(stmiNorm);
			stm.addItem(stmiNovelties);
			stm.addItem(stmiBestof);
			stm.addItem(stmiShuffle);
			stm.addSeparator();
			stm.addItem(stmiNext);
			stm.addItem(stmiPrevious);
			stm.addItem(stmiStop);
			stm.addItem(stmiPause);
			stm.addSeparator();
			stm.addItem(cmiVisible);
			
			//Register needed events
			ObservationManager.register(EVENT_ZERO,this);
		}
	
	}
	
	
	/* (non-Javadoc)
	 * @see snoozesoft.systray4j.SysTrayMenuListener#menuItemSelected(snoozesoft.systray4j.SysTrayMenuEvent)
	 */
	public void menuItemSelected(SysTrayMenuEvent e) {
		if (e.getSource() == stmiExit){
			Main.exit(0);
		}
		else if (e.getSource() == stmiAbout){
			//make frame visible
			if ( !JajukWindow.getInstance().isVisible()){
				JajukWindow.getInstance().setVisible(true);
			}
			//set help perspectievb to show "about" view
			PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_HELP);
		}
		else if (e.getSource() == stmiShuffle){
			ArrayList alToPlay = FileManager.getGlobalShufflePlaylist();
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_SHUFFLE);
			pDetails.put(DETAIL_SELECTION,alToPlay);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);
		}
		else if (e.getSource() == stmiBestof){
			ArrayList alToPlay = FileManager.getGlobalBestofPlaylist();
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_BESTOF);
			pDetails.put(DETAIL_SELECTION,alToPlay);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);		
		}
		else if (e.getSource() == stmiNovelties){
			ArrayList alToPlay = FileManager.getGlobalNoveltiesPlaylist();
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_NOVELTIES);
			pDetails.put(DETAIL_SELECTION,alToPlay);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);
		}
		else if (e.getSource() == stmiNorm){
			Properties pDetails = new Properties();
			pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_NORMAL);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);
		}
		else if (e.getSource() == cmiVisible){
			ConfigurationManager.setProperty(CONF_SHOW_AT_STARTUP,Boolean.toString(cmiVisible.getState()));
		}
		else if (e.getSource() == stmiPrevious){
			FIFO.getInstance().playPrevious();
		}
		else if (e.getSource() == stmiNext){
			FIFO.getInstance().playNext();
		}
		else if (e.getSource() == stmiStop){
			FIFO.getInstance().stopRequest();
			ObservationManager.notify(EVENT_PLAYLIST_REFRESH); //alert playlists editors ( queue playlist ) something changed for him
		}
		else if (e.getSource() == stmiPause){
		    if ( Player.isPaused()){  //player was paused, resume it
		        bPaused = false;
				Player.resume();
				stmiPause.setLabel(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
				ObservationManager.notify(EVENT_PLAYER_RESUME);  //notify of this event
			}
		    else{ //player is not paused, pause it
		        Player.pause();
		        bPaused = true;
		        stmiPause.setLabel(Messages.getString("JajukWindow.12")); //$NON-NLS-1$
		        ObservationManager.notify(EVENT_PLAYER_PAUSE);  //notify of this event
		        
		    }
		}
	}
	
	/* (non-Javadoc)
	 * @see snoozesoft.systray4j.SysTrayMenuListener#iconLeftClicked(snoozesoft.systray4j.SysTrayMenuEvent)
	 */
	public void iconLeftClicked(SysTrayMenuEvent e) {
		if ( e.getSource() == stmi){
			if ( !JajukWindow.getInstance().isVisible()){
				JajukWindow.getInstance().setVisible(true);
			}
			else{
				JajukWindow.getInstance().setVisible(false);
			}
		}
	}


	/* (non-Javadoc)
	 * @see snoozesoft.systray4j.SysTrayMenuListener#iconLeftDoubleClicked(snoozesoft.systray4j.SysTrayMenuEvent)
	 */
	public void iconLeftDoubleClicked(SysTrayMenuEvent arg0) {
	}
	
	/**
	 * Hide systray 
	 *
	 */
	public void closeSystray(){
		if ( stm != null ){
			stm.hideIcon();
		}
	}
	
	/**
	 * Set systray tooltip
	 * @param s
	 */
	public void setTooltip(String s){
		try{
			if ( stm != null){
				if (s.length() > 63){
					s = s.substring(0,63);
				}
				stm.setToolTip(s); //note that the systray tooltip length must be <= 63
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
		if (subject.equals(EVENT_ZERO)){
			setTooltip(Messages.getString("JajukWindow.18")); //$NON-NLS-1$
		}
		
	}

}
