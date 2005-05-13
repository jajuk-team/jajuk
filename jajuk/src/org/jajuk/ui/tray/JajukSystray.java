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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Player;
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukWindow;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
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
public class JajukSystray implements ITechnicalStrings,Observer,ActionListener,MouseWheelListener{
	//Systray variables
	SystemTray stray = SystemTray.getDefaultSystemTray();;
	TrayIcon trayIcon;
	JPopupMenu jmenu;
	JMenuItem jmiExit;
	JMenuItem jmiMute;
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
	/**Self instance singleton*/
	private static JajukSystray jsystray;
	
	
	/**
	 * 
	 * @return singleton
	 */
	public static JajukSystray getInstance() {
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
		
		jmenu = new JPopupMenu(Messages.getString("JajukWindow.3")); //$NON-NLS-1$
		jmiExit =  new JMenuItem(Messages.getString("JajukWindow.4"),Util.getIcon(ICON_EXIT)); //$NON-NLS-1$
		jmiExit.setToolTipText(Messages.getString("JajukWindow.21")); //$NON-NLS-1$
		jmiExit.addActionListener(this);
		
		jmiMute =  new JMenuItem(Messages.getString("JajukWindow.2"),Util.getIcon(ICON_MUTE)); //$NON-NLS-1$
		jmiMute.addActionListener(this);
		jmiMute.setToolTipText(Messages.getString("JajukWindow.19")); //$NON-NLS-1$
		
		jmiAbout =  new JMenuItem(Messages.getString("JajukWindow.5"),Util.getIcon(ICON_INFO)); //$NON-NLS-1$
		jmiAbout.addActionListener(this);
		jmiAbout.setToolTipText(Messages.getString("JajukWindow.22")); //$NON-NLS-1$
		
		jmiShuffle =  new JMenuItem(Messages.getString("JajukWindow.6"),Util.getIcon(ICON_SHUFFLE_GLOBAL)); //$NON-NLS-1$
		jmiShuffle.addActionListener(this);
		jmiShuffle.setToolTipText(Messages.getString("JajukWindow.23")); //$NON-NLS-1$
		
		jmiBestof =  new JMenuItem(Messages.getString("JajukWindow.7"),Util.getIcon(ICON_BESTOF)); //$NON-NLS-1$
		jmiBestof.addActionListener(this);
		jmiBestof.setToolTipText(Messages.getString("JajukWindow.24")); //$NON-NLS-1$
		
		jmiNorm =  new JMenuItem(Messages.getString("JajukWindow.16"),Util.getIcon(ICON_MODE_NORMAL)); //$NON-NLS-1$
		jmiNorm.addActionListener(this);
		jmiNorm.setToolTipText(Messages.getString("JajukWindow.32")); //$NON-NLS-1$
		
		jmiNovelties =  new JMenuItem(Messages.getString("JajukWindow.15"),Util.getIcon(ICON_NOVELTIES)); //$NON-NLS-1$
		jmiNovelties.addActionListener(this);
		jmiNovelties.setToolTipText(Messages.getString("JajukWindow.31")); //$NON-NLS-1$
		
		jcbmiVisible =  new JCheckBoxMenuItem(Messages.getString("JajukWindow.8")); //$NON-NLS-1$
		jcbmiVisible.setState(JajukWindow.getInstance().isVisible()); 
		jcbmiVisible.addActionListener(this);
		jcbmiVisible.setToolTipText(Messages.getString("JajukWindow.25")); //$NON-NLS-1$
		
		jmiPause = new JMenuItem(Messages.getString("JajukWindow.10"),Util.getIcon(ICON_PAUSE)); //$NON-NLS-1$
		jmiPause.addActionListener(this);
		jmiPause.setToolTipText(Messages.getString("JajukWindow.26")); //$NON-NLS-1$
		
		jmiStop = new JMenuItem(Messages.getString("JajukWindow.11"),Util.getIcon(ICON_STOP)); //$NON-NLS-1$
		jmiStop.addActionListener(this);
		jmiStop.setToolTipText(Messages.getString("JajukWindow.27")); //$NON-NLS-1$
		
		jmiPrevious = new JMenuItem(Messages.getString("JajukWindow.13"),Util.getIcon(ICON_PREVIOUS)); //$NON-NLS-1$
		jmiPrevious.addActionListener(this);
		jmiPrevious.setToolTipText(Messages.getString("JajukWindow.29")); //$NON-NLS-1$
		
		jmiNext = new JMenuItem(Messages.getString("JajukWindow.14"),Util.getIcon(ICON_NEXT)); //$NON-NLS-1$
		jmiNext.addActionListener(this);
		jmiNext.setToolTipText(Messages.getString("JajukWindow.30")); //$NON-NLS-1$
		
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
		jmenu.add(jmiMute);
		jmenu.addSeparator();
		jmenu.add(jmiExit);
		jmenu.add(jmiOut);
		
		trayIcon = new TrayIcon(Util.getIcon(ICON_LOGO_TRAY),Messages.getString("JajukWindow.18"),jmenu); //$NON-NLS-1$);
		jmenu.addMouseWheelListener(this);
		trayIcon.setIconAutoSize(true);
		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//hide menu if opened
				jmenu.setVisible(false);
				//show window if it is not visible and hide it if it is visible
				if (!JajukWindow.getInstance().isVisible()){
					JajukWindow.getInstance().setShown(true);
				}
				else{
					JajukWindow.getInstance().setShown(false);
					JajukWindow.getInstance().setState(Frame.ICONIFIED); //force iconification
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
		ObservationManager.register(EVENT_MUTE_STATE,this);
		
		//check if a fiel has been already started
		if (FIFO.getInstance().getCurrentFile() == null){
			update(new Event(EVENT_PLAYER_STOP,ObservationManager.getDetailsLastOccurence(EVENT_PLAYER_STOP)));
        }
		else{
			update(new Event(EVENT_FILE_LAUNCHED,ObservationManager.getDetailsLastOccurence(EVENT_FILE_LAUNCHED)));    
		}
	}
	
	/**
	 * ActionListener
	 */
	public void actionPerformed(final ActionEvent e) {
		//do not run this in a separate thread because Player actions would die with the thread
		try{
			if (e.getSource() == jmiExit){
				Main.exit(0);
            }
			else if (e.getSource() == jmiAbout){
				//set default perspective to show if UIi is not yet started
				if (Main.isUILauched()){
					PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_HELP);    
				}
				else{
					Main.setDefaultPerspective(PERSPECTIVE_NAME_HELP);
				}
				//make frame visible
				if ( !JajukWindow.getInstance().isVisible()){
					JajukWindow.getInstance().setShown(true);
				}
			}
			else if (e.getSource() == jmiShuffle){
				ArrayList alToPlay = FileManager.getGlobalShufflePlaylist();
				FIFO.getInstance().push(Util.createStackItems(alToPlay,
						ConfigurationManager.getBoolean(CONF_STATE_REPEAT),false),false);
			}
			else if (e.getSource() == jmiBestof){
				ArrayList alToPlay = FileManager.getGlobalBestofPlaylist();
				FIFO.getInstance().push(Util.createStackItems(alToPlay,
						ConfigurationManager.getBoolean(CONF_STATE_REPEAT),false),false);
			}
			else if (e.getSource() == jmiNovelties){
				ArrayList alToPlay = FileManager.getGlobalNoveltiesPlaylist();
                Collections.shuffle(alToPlay);//shuffle the selection
				FIFO.getInstance().push(Util.createStackItems(alToPlay,
						ConfigurationManager.getBoolean(CONF_STATE_REPEAT),false),false);
			}
			else if (e.getSource() == jmiNorm){
				StackItem item = FIFO.getInstance().getCurrentItem();//stores current item
				FIFO.getInstance().clear(); //clear fifo 
				FIFO.getInstance().push(item,true); //then re-add current item
				FIFO.getInstance().computesPlanned(true); //update planned list
				Properties properties = new Properties();
				properties.put(DETAIL_ORIGIN,DETAIL_SPECIAL_MODE_NORMAL);
				ObservationManager.notify(new Event(EVENT_SPECIAL_MODE,properties));
			}
			else if (e.getSource() == jcbmiVisible){
				ConfigurationManager.setProperty(CONF_SHOW_AT_STARTUP,Boolean.toString(jcbmiVisible.getState()));
			}
			else if (e.getSource() == jmiPrevious){
                if ( (e.getModifiers() & 4) !=4  && (e.getModifiers() & ActionEvent.SHIFT_MASK) != ActionEvent.SHIFT_MASK ){ //not right clic and shift not selected
                       FIFO.getInstance().playPrevious();
			    }
			    else { //shift selected
			        FIFO.getInstance().playPreviousAlbum();
			    }
			}
			else if (e.getSource() == jmiNext){
                if ( (e.getModifiers() & 4) !=4  && (e.getModifiers() & ActionEvent.SHIFT_MASK) != ActionEvent.SHIFT_MASK ){ //not right clic and shift not selected
                        FIFO.getInstance().playNext();
			    }
			    else {
			        FIFO.getInstance().playNextAlbum();
			    }
			}
			else if (e.getSource() == jmiStop){
				FIFO.getInstance().stopRequest();
				ObservationManager.notify(new Event(EVENT_PLAYLIST_REFRESH)); //alert playlists editors ( queue playlist ) something changed for him
			}
			else if (e.getSource() == jmiMute){
				Player.mute();  //change mute state 
			}
			else if (e.getSource() == jmiPause){
				if ( Player.isPaused()){  //player was paused, resume it
					Player.resume();
					jmiPause.setText(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
					ObservationManager.notify(new Event(EVENT_PLAYER_RESUME));  //notify of this event
				}
				else{ //player is not paused, pause it
					Player.pause();
					jmiPause.setText(Messages.getString("JajukWindow.12")); //$NON-NLS-1$
					ObservationManager.notify(new Event(EVENT_PLAYER_PAUSE));  //notify of this event
				}
			}
		}
		catch(Exception e2){
			Log.error(e2);
		}
		finally{
			ObservationManager.notify(new Event(EVENT_PLAYLIST_REFRESH)); //refresh playlist editor
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		String subject = event.getSubject();
		if (EVENT_ZERO.equals(subject)){
			trayIcon.setToolTip(Messages.getString("JajukWindow.18")); //$NON-NLS-1$
		}
		else if (EVENT_MUTE_STATE.equals(subject)){
			if (Player.isMuted()){
				jmiMute.setText(Messages.getString("JajukWindow.1")); //NON-NLS-1$ //$NON-NLS-1$
				jmiMute.setIcon(Util.getIcon(ICON_UNMUTE)); //show unmute icon
			}
			else{
				jmiMute.setText(Messages.getString("JajukWindow.2")); //NON-NLS-1$ //$NON-NLS-1$
				jmiMute.setIcon(Util.getIcon(ICON_MUTE)); //show mute icon
			}	
		}
		else if (EVENT_FILE_LAUNCHED.equals(subject)){
			File file  = FileManager.getFileById((String)ObservationManager.getDetail(event,DETAIL_CURRENT_FILE_ID));
			String sOut = ""; //$NON-NLS-1$
			if (file != null ){
				String sAuthor = file.getTrack().getAuthor().getName();
				if (!sAuthor.equals(UNKNOWN_AUTHOR)){
					sOut += sAuthor+" / "; //$NON-NLS-1$
				}
				String sAlbum = file.getTrack().getAlbum().getName();
				if (!sAlbum.equals(UNKNOWN_ALBUM)){
					sOut += sAlbum+" / "; //$NON-NLS-1$
				}
				sOut += file.getTrack().getName(); 
			}
			else{
				sOut = Messages.getString("JajukWindow.18"); //$NON-NLS-1$
			}
			trayIcon.setToolTip(sOut);
		}
		else if( EVENT_PLAYER_STOP.equals(subject) || EVENT_ZERO.equals(subject)){
			jmiPause.setEnabled(false);
			jmiStop.setEnabled(false);
			jmiNext.setEnabled(false);
			jmiPrevious.setEnabled(false);
			jmiPause.setIcon(Util.getIcon(ICON_PAUSE));
			jmiPause.setText(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
		}
		else if ( EVENT_PLAYER_PLAY.equals(subject)){
			jmiPause.setEnabled(true);
			jmiStop.setEnabled(true);
			jmiNext.setEnabled(true);
			jmiPrevious.setEnabled(true);
			jmiPause.setText(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
		}
		else if ( EVENT_PLAYER_PAUSE.equals(subject)){
			jmiPause.setText(Messages.getString("JajukWindow.12")); //$NON-NLS-1$
			jmiPause.setIcon(Util.getIcon(ICON_PLAY));
		}
		else if ( EVENT_PLAYER_RESUME.equals(subject)){
			jmiPause.setText(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
			jmiPause.setIcon(Util.getIcon(ICON_PAUSE));
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
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		int iOld = CommandJPanel.getInstance().getCurrentVolume();
		int iNew = iOld - (e.getUnitsToScroll()*3);
		if ( iNew<0){
			iNew = 0;
		}
		else if (iNew>99){
			iNew = 99;
		}
		CommandJPanel.getInstance().setCurrentVolume(iNew);
	}
	
}
