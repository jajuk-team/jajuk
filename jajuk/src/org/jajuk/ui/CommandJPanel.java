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
 * $Revision$
 */
package org.jajuk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import layout.TableLayout;

import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.HistoryItem;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.base.Player;
import org.jajuk.base.SearchResult;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;

import com.sun.SwingWorker;

/**
 *  Command panel ( static view )
 *
 * @author     bflorat
 * @created    3 oct. 2003
 */
public class CommandJPanel extends JPanel implements ITechnicalStrings,ActionListener,ListSelectionListener,ChangeListener,Observer{
	
	//singleton
	static private CommandJPanel command;
	
	//widgets declaration
	SearchBox sbSearch;
	SteppedComboBox jcbHistory;
	JToolBar jtbMode;
	JButton jbRepeat;
	JButton jbRandom;
	JButton jbContinue;
	JButton jbIntro;
	JToolBar jtbSpecial;
	JButton jbGlobalRandom;
	JButton jbBestof;
	JButton jbNovelties;
	JButton jbNorm;
	JButton jbMute;
	JToolBar jtbPlay;
	JButton jbPrevious;
	JButton jbNext;
	JButton jbRew;
	JButton jbPlayPause;
	JButton jbStop;
	JButton jbFwd;
	JLabel jlVolume;
	JSlider jsVolume;
	JLabel jlPosition;
	JSlider jsPosition;
	
	
	//variables declaration
	/**Repeat mode flag*/
	static boolean bIsRepeatEnabled = false;
	/**Shuffle mode flag*/
	static boolean bIsShuffleEnabled = false;
	/**Continue mode flag*/
	static boolean bIsContinueEnabled = true;
	/**Intro mode flag*/
	static boolean bIsIntroEnabled = false;
	/**Forward or rewind jump size in track percentage*/
	static final float JUMP_SIZE = 0.1f;
	/**Position slider moving*/
	private static boolean bPositionChanging = false;
	
	
	
	/**
	 * @return singleton
	 */
	public static synchronized CommandJPanel getInstance(){
		if (command == null){
			command = new CommandJPanel();
		}
		return command;
	}
	
	/**
	 * Constructor
	 */
	private CommandJPanel(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//dimensions
				int height1 = 25;  //buttons, components
				//int height2 = 36; //slider ( at least this height in the gtk+ l&f ) 
				int iSeparator = 1;
				//set default layout and size
				double[][] size ={{5*iSeparator,0.15,10*iSeparator,0.17,iSeparator,0.11,iSeparator,
					0.11,iSeparator,0.18,iSeparator,0.12,iSeparator,0.15,10*iSeparator,20},
						{height1}}; //note we can't set a % for history combo box because of popup size
				setLayout(new TableLayout(size));
				sbSearch = new SearchBox(CommandJPanel.this);
				
				//history
				jcbHistory = new SteppedComboBox(History.getInstance().getHistory().toArray());
				jcbHistory.setPopupWidth(1000);
				jcbHistory.setToolTipText(Messages.getString("CommandJPanel.0")); //$NON-NLS-1$
				jcbHistory.addActionListener(CommandJPanel.this);
				
				//Mode toolbar
				jtbMode = new JToolBar();
				jtbMode.setRollover(true);
				jtbMode.setFloatable(false);
				jtbMode.add(Box.createHorizontalGlue());
				jbRepeat = new JButton(Util.getIcon(ICON_REPEAT_ON)); 
				jbRepeat.setActionCommand(EVENT_REPEAT_MODE_STATUS_CHANGED);
				jbRepeat.setToolTipText(Messages.getString("CommandJPanel.1")); //$NON-NLS-1$
				jbRepeat.addActionListener(JajukListener.getInstance());
				jtbMode.add(jbRepeat);
				jbRandom = new JButton(Util.getIcon(ICON_SHUFFLE_ON));
				jbRandom.setToolTipText(Messages.getString("CommandJPanel.2")); //$NON-NLS-1$
				jbRandom.setActionCommand(EVENT_SHUFFLE_MODE_STATUS_CHANGED);
				jbRandom.addActionListener(JajukListener.getInstance());
				jtbMode.add(jbRandom);
				jbContinue = new JButton(Util.getIcon(ICON_CONTINUE_ON));
				jbContinue.setToolTipText(Messages.getString("CommandJPanel.3")); //$NON-NLS-1$
				jbContinue.setActionCommand(EVENT_CONTINUE_MODE_STATUS_CHANGED);
				jbContinue.addActionListener(JajukListener.getInstance());
				jtbMode.add(jbContinue);
				jbIntro = new JButton(Util.getIcon(ICON_INTRO_ON));
				jbIntro.setToolTipText(Messages.getString("CommandJPanel.4")); //$NON-NLS-1$
				jbIntro.setActionCommand(EVENT_INTRO_MODE_STATUS_CHANGED);
				jbIntro.addActionListener(JajukListener.getInstance());
				jtbMode.add(jbIntro);
				jtbMode.add(Box.createHorizontalGlue());
				
				//Special functions toolbar
				jtbSpecial = new JToolBar();
				jtbSpecial.setOrientation(JToolBar.HORIZONTAL);
				jtbSpecial.add(Box.createHorizontalGlue());
				jtbSpecial.setFloatable(false);
				jtbSpecial.setRollover(true);
				jbGlobalRandom = new JButton(Util.getIcon(ICON_SHUFFLE_GLOBAL_ON));
				jbGlobalRandom.addActionListener(CommandJPanel.this);
				jbGlobalRandom.setToolTipText(Messages.getString("CommandJPanel.5")); //$NON-NLS-1$
				jtbSpecial.add(jbGlobalRandom);
				jbBestof = new JButton(Util.getIcon(ICON_BESTOF_ON)); 
				jbBestof.addActionListener(CommandJPanel.this);
				jbBestof.setToolTipText(Messages.getString("CommandJPanel.6")); //$NON-NLS-1$
				jtbSpecial.add(jbBestof);
				jbNovelties = new JButton(Util.getIcon(ICON_NOVELTIES_ON)); 
				jbNovelties.addActionListener(CommandJPanel.this);
				jbNovelties.setToolTipText(Messages.getString("CommandJPanel.16")); //$NON-NLS-1$
				jtbSpecial.add(jbNovelties);
				jbNorm = new JButton(Util.getIcon(ICON_MODE_NORMAL)); 
				jbNorm.addActionListener(CommandJPanel.this);
				jbNorm.setToolTipText(Messages.getString("CommandJPanel.17")); //$NON-NLS-1$
				jtbSpecial.add(jbNorm);
				
				jtbSpecial.add(Box.createHorizontalGlue());
				
				//Play toolbar
				jtbPlay = new JToolBar();
				jtbPlay.setRollover(true);
				jtbPlay.setFloatable(false);
				jtbPlay.add(Box.createHorizontalGlue());
				jbPrevious = new JButton(Util.getIcon(ICON_PREVIOUS)); 
				jbPrevious.setToolTipText(Messages.getString("CommandJPanel.8")); //$NON-NLS-1$
				jbPrevious.addActionListener(CommandJPanel.this);
				jtbPlay.add(jbPrevious);
				jbNext = new JButton(Util.getIcon(ICON_NEXT)); 
				jbNext.setToolTipText(Messages.getString("CommandJPanel.9")); //$NON-NLS-1$
				jbNext.addActionListener(CommandJPanel.this);
				jtbPlay.add(jbNext);
				jtbPlay.addSeparator();
				jbRew = new JButton(Util.getIcon(ICON_REW)); 
				jbRew.setEnabled(false);
				jbRew.setToolTipText(Messages.getString("CommandJPanel.10")); //$NON-NLS-1$
				jbRew.addActionListener(CommandJPanel.this);
				jtbPlay.add(jbRew);
				jbPlayPause = new JButton(Util.getIcon(ICON_PAUSE)); 
				jbPlayPause.setToolTipText(Messages.getString("CommandJPanel.11")); //$NON-NLS-1$
				jbPlayPause.setEnabled(false);
				jbPlayPause.addActionListener(CommandJPanel.this);
				jtbPlay.add(jbPlayPause);
				jbStop = new JButton(Util.getIcon(ICON_STOP)); 
				jbStop.setToolTipText(Messages.getString("CommandJPanel.12")); //$NON-NLS-1$
				jbStop.addActionListener(CommandJPanel.this);
				jbStop.setEnabled(false);
				jtbPlay.add(jbStop);
				jbFwd = new JButton(Util.getIcon(ICON_FWD)); 
				jbFwd.setToolTipText(Messages.getString("CommandJPanel.13")); //$NON-NLS-1$
				jbFwd.setEnabled(false);
				jbFwd.addActionListener(CommandJPanel.this);
				jtbPlay.add(jbFwd);
				jtbPlay.add(Box.createHorizontalGlue());
				
				//Volume
				jbMute = new JButton(Util.getIcon(ICON_MUTE_ON)); 
				jbMute.addActionListener(CommandJPanel.this);
				jbMute.setToolTipText(Messages.getString("CommandJPanel.7")); //$NON-NLS-1$
				JPanel jpVolume = new JPanel();
				jpVolume.setLayout(new BoxLayout(jpVolume,BoxLayout.X_AXIS));
				jlVolume = new JLabel(Util.getIcon(ICON_VOLUME)); 
				jsVolume = new JSlider(0,100,(int)(100*ConfigurationManager.getFloat(CONF_VOLUME)));
				jpVolume.add(jlVolume);
				jpVolume.add(jsVolume);
				jsVolume.setToolTipText(Messages.getString("CommandJPanel.14")); //$NON-NLS-1$
				jsVolume.addChangeListener(CommandJPanel.this);
				
				//Position
				JPanel jpPosition = new JPanel();
				jpPosition.setLayout(new BoxLayout(jpPosition,BoxLayout.X_AXIS));
				jlPosition = new JLabel(Util.getIcon(ICON_POSITION)); 
				jsPosition = new JSlider(0,100,0);
				jpPosition.add(jlPosition);
				jpPosition.add(jsPosition);
				jsPosition.addChangeListener(CommandJPanel.this);
				jsPosition.setEnabled(false);
				jsPosition.setToolTipText(Messages.getString("CommandJPanel.15")); //$NON-NLS-1$
				
				//add toolbars to main panel
				add(sbSearch,"1,0"); //$NON-NLS-1$
				add(jcbHistory,"3,0"); //$NON-NLS-1$
				add(jtbMode,"5,0"); //$NON-NLS-1$
				add(jtbSpecial,"7,0"); //$NON-NLS-1$
				add(jtbPlay,"9,0"); //$NON-NLS-1$
				add(jpPosition,"11,0"); //$NON-NLS-1$
				add(jpVolume,"13,0"); //$NON-NLS-1$
				add(jbMute,"15,0"); //$NON-NLS-1$
				
				//register to player events
				ObservationManager.register(EVENT_PLAYER_PLAY,CommandJPanel.this);
				ObservationManager.register(EVENT_PLAYER_STOP,CommandJPanel.this);
				ObservationManager.register(EVENT_PLAYER_PAUSE,CommandJPanel.this);
				ObservationManager.register(EVENT_PLAYER_RESUME,CommandJPanel.this);
				ObservationManager.register(EVENT_HEART_BEAT,CommandJPanel.this);
				ObservationManager.register(EVENT_ADD_HISTORY_ITEM,CommandJPanel.this);
				ObservationManager.register(EVENT_SPECIAL_MODE,CommandJPanel.this);
				ObservationManager.register(EVENT_ZERO,CommandJPanel.this);
				
				//update initial state 
				update(EVENT_PLAYER_PLAY);
				//check if some track has been lauched before the view has been displayed
				update(EVENT_HEART_BEAT);
			}
		});
		//set buttons borders, must be here for an unknwon reason due to a liquid lnf 
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if ( ConfigurationManager.getBoolean(CONF_STATE_REPEAT)){
					jbRepeat.setBorder(BorderFactory.createLoweredBevelBorder());
				}
				else{
					jbRepeat.setBorder(BorderFactory.createRaisedBevelBorder());
				}
				if ( ConfigurationManager.getBoolean(CONF_STATE_INTRO)){
					jbIntro.setBorder(BorderFactory.createLoweredBevelBorder());
				}
				else{
					jbIntro.setBorder(BorderFactory.createRaisedBevelBorder());
				}
				if ( ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE)){
					jbRandom.setBorder(BorderFactory.createLoweredBevelBorder());
				}
				else{
					jbRandom.setBorder(BorderFactory.createRaisedBevelBorder());
				}
				if ( ConfigurationManager.getBoolean(CONF_STATE_CONTINUE)){
					jbContinue.setBorder(BorderFactory.createLoweredBevelBorder());
				}
				else{
					jbContinue.setBorder(BorderFactory.createRaisedBevelBorder());
				}
				jbMute.setBorder(BorderFactory.createRaisedBevelBorder());
			}	
		});
	} 
	
	
	/** 
	 * Add an history item in the history combo box
	 * @param file
	 */
	public void addHistoryItem(HistoryItem hi){
		String sOut = hi.toString();
		if (sOut == null){
			return;
		}
		jcbHistory.removeActionListener(this); //stop listening this item when manupulating it
		jcbHistory.insertItemAt(sOut,0);
		jcbHistory.setSelectedIndex(0);
		jcbHistory.addActionListener(this);
	}
	
	
	/**
	 * Clear history bar
	 */
	public void clearHistoryBar(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jcbHistory.removeAllItems();
			}
		});
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent ae) {
		if ( ae.getSource() == jcbHistory){
			SwingWorker sw = new SwingWorker() {
				HistoryItem hi = null;
				public Object construct() {
					hi = History.getInstance().getHistoryItem(jcbHistory.getSelectedIndex());
					return null;
				}
				
				public void finished() {
					if (hi != null){
						org.jajuk.base.File file = FileManager.getFile(hi.getFileId());
						if (file!= null && !file.isScanned()){  //file must be on a mounted device not refreshing
							FIFO.getInstance().push(file,false);
						}
						else{
							Messages.showErrorMessage("120",file.getDirectory().getDevice().getName()); //$NON-NLS-1$
							jcbHistory.setSelectedItem(null);
						}
					}	
				}
			};
			sw.start();
		}
		if (ae.getSource() == jbBestof ){
			ArrayList alToPlay = FileManager.getGlobalBestofPlaylist();
			if ( alToPlay.size() > 0){
				Properties pDetails = new Properties();
				pDetails.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_BESTOF);
				pDetails.put(DETAIL_SELECTION,alToPlay);
				ObservationManager.notify(EVENT_SPECIAL_MODE,pDetails);
			}
		}
		if (ae.getSource() == jbGlobalRandom ){
			ArrayList alToPlay = FileManager.getGlobalShufflePlaylist();
			if ( alToPlay.size() > 0){
				Properties pDetail = new Properties();
				pDetail.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_SHUFFLE);
				pDetail.put(DETAIL_SELECTION,alToPlay);
				ObservationManager.notify(EVENT_SPECIAL_MODE,pDetail);
			}
		}
		if (ae.getSource() == jbNovelties ){
			ArrayList alToPlay  = FileManager.getGlobalNoveltiesPlaylist();
			if ( alToPlay.size() > 0){
				Properties pDetail = new Properties();
				pDetail.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_NOVELTIES);
				pDetail.put(DETAIL_SELECTION,alToPlay);
				ObservationManager.notify(EVENT_SPECIAL_MODE,pDetail);
			}
			else{ //none novelty found
				Messages.showErrorMessage("127"); //$NON-NLS-1$
			}
		}
		if (ae.getSource() == jbNorm ){
			Properties pDetail = new Properties();
			pDetail.put(DETAIL_SPECIAL_MODE,DETAIL_SPECIAL_MODE_NORMAL);
			ObservationManager.notify(EVENT_SPECIAL_MODE,pDetail);
		}
		else if (ae.getSource() == jbMute ){
			Player.mute();
			if ( Player.isMuted()){
				jbMute.setBorder(BorderFactory.createLoweredBevelBorder());
			}
			else{
				jbMute.setBorder(BorderFactory.createRaisedBevelBorder());
			}
		}
		else if(ae.getSource() == jbStop){
			FIFO.getInstance().stopRequest();
			ObservationManager.notify(EVENT_PLAYLIST_REFRESH); //alert playlists editors ( queue playlist ) something changed for him
		}
		else if(ae.getSource() == jbPlayPause){
			if ( Player.isPaused()){  //player was paused, resume it
				Player.resume();
				ObservationManager.notify(EVENT_PLAYER_RESUME);  //notify of this event
			}
			else{ //player is not paused, pause it
				Player.pause();
				ObservationManager.notify(EVENT_PLAYER_PAUSE);  //notify of this event
			}
		}
		else if (ae.getSource() == jbPrevious){
			FIFO.getInstance().playPrevious();
			if ( Player.isPaused()){  //player was paused, reset pause button when changing of track
				Player.setPaused(false);
				ObservationManager.notify(EVENT_PLAYER_RESUME);  //notify of this event
			}
		}
		else if (ae.getSource() == jbNext){
			FIFO.getInstance().playNext();
			if ( Player.isPaused()){  //player was paused, reset pause button
				Player.setPaused(false);
				ObservationManager.notify(EVENT_PLAYER_RESUME);  //notify of this event
			}
		}
		else if (ae.getSource() == jbRew){
			float fCurrentPosition = Player.getCurrentPosition();
			Player.seek(fCurrentPosition-JUMP_SIZE);
		}
		else if (ae.getSource() == jbFwd){
			float fCurrentPosition = Player.getCurrentPosition();
			Player.seek(fCurrentPosition+JUMP_SIZE);
		}
		
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(final ListSelectionEvent e) {
		SwingWorker sw = new SwingWorker() {
			public Object construct() {
				if (!e.getValueIsAdjusting()){
					SearchResult sr = (SearchResult)sbSearch.alResults.get(sbSearch.jlist.getSelectedIndex());
					FIFO.getInstance().push(sr.getFile(),false);
				}
				return null;
			}
			
			public void finished() {
				if (!e.getValueIsAdjusting()){
					sbSearch.popup.hide();
					requestFocus();	
				}	
			}
		};
		sw.start();
	}
	
	/*
	 *  @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if ( e.getSource() == jsVolume && !jsVolume.getValueIsAdjusting()){
			Player.setVolume((float)jsVolume.getValue()/100);
		}
		else if (e.getSource() == jsPosition && !bPositionChanging && !jsPosition.getValueIsAdjusting()){
			bPositionChanging = true;
			Player.seek((float)jsPosition.getValue()/100);
			bPositionChanging = false;
		}
	}
	
	/**
	 * Set Slider position
	 * @param i percentage of slider
	 */
	public void setCurrentPosition(final int i){
		if ( !bPositionChanging ){//don't move slider when user do it himself a	t the same time
			bPositionChanging = true;  //block events so player is not affected
			CommandJPanel.this.jsPosition.setValue(i);
			bPositionChanging = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public synchronized void update(final String subject) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if( EVENT_PLAYER_STOP.equals(subject) || EVENT_ZERO.equals(subject)){
					jbRew.setEnabled(false);
					jbPlayPause.setEnabled(false);
					jbStop.setEnabled(false);
					jbFwd.setEnabled(false);
					jsPosition.setEnabled(false);
					setCurrentPosition(0);
					jbPlayPause.setIcon(Util.getIcon(ICON_PAUSE)); //resume any current pause
					ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION,"0");//reset startup position //$NON-NLS-1$
				}
				else if ( EVENT_PLAYER_PLAY.equals(subject)){
					jbRew.setEnabled(true);
					jbPlayPause.setEnabled(true);
					jbStop.setEnabled(true);
					jbFwd.setEnabled(true);
					jsPosition.setEnabled(true);
				}
				else if ( EVENT_PLAYER_PAUSE.equals(subject)){
					jbRew.setEnabled(false);
					jbFwd.setEnabled(false);
					jsPosition.setEnabled(false);
					jbPlayPause.setIcon(Util.getIcon(ICON_PLAY));
				}
				else if ( EVENT_PLAYER_RESUME.equals(subject)){
					jbRew.setEnabled(true);
					jbFwd.setEnabled(true);
					jsPosition.setEnabled(true);
					jbPlayPause.setIcon(Util.getIcon(ICON_PAUSE));
				}
				else if (EVENT_HEART_BEAT.equals(subject)){
					Integer iPos = (Integer)ObservationManager.getDetail(EVENT_HEART_BEAT,DETAIL_CURRENT_POSITION); 
					if (iPos != null){
						setCurrentPosition(iPos.intValue());
					}
				}
				else if (EVENT_ADD_HISTORY_ITEM.equals(subject)){
					HistoryItem hi = (HistoryItem)ObservationManager.getDetail(EVENT_ADD_HISTORY_ITEM,DETAIL_HISTORY_ITEM);
					if (hi != null ){
						addHistoryItem(hi);
					}
				}
			}
		});
		
	}
	
	/**
	 * ToString() method
	 */
	public String toString(){
	    return getClass().getName();
	}
	
}
