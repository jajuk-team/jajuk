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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
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
import org.jajuk.base.SearchResult;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Command panel ( static view )
 *
 * @author     bflorat
 * @created    3 oct. 2003
 */
public class CommandJPanel extends JPanel implements ITechnicalStrings,ActionListener,ListSelectionListener,ChangeListener{
	
	//singleton
	static private CommandJPanel command;
	
	//widgets declaration
		 JToolBar jtbSearch;
			SearchBox  sbSearch;
		JToolBar jtbHistory;
			public SteppedComboBox jcbHistory;
		JToolBar jtbMode;
			JButton jbRepeat;
			JButton jbRandom;
			JButton jbContinue;
			JButton jbIntro;
		JToolBar jtbSpecial;
			JButton jbGlobalRandom;
			JButton jbBestof;
			JButton jbMute;
		JToolBar jtbPlay;
			JButton jbPrevious;
			JButton jbNext;
			JButton jbRew;
			JButton jbPlayPause;
			JButton jbStop;
			JButton jbFwd;
		JToolBar jtbVolume;
			JLabel jlVolume;
			JSlider jsVolume;
		JToolBar jtbPosition;
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
	static final int JUMP_SIZE = 10;
	/**Slider move event filter*/
	private boolean bPositionChanging = false;

	
		
	public static CommandJPanel getInstance(){
		if (command == null){
			command = new CommandJPanel();
		}
		return command;
	}
	
	private CommandJPanel(){
		//dimensions
		int height1 = 25;  //buttons, components
		//int height2 = 36; //slider ( at least this height in the gtk+ l&f ) 
		int iSeparator = 0;
		//set default layout and size
		double[][] size ={{0.25,iSeparator,300,iSeparator,0.13,iSeparator,0.10,iSeparator,0.19,iSeparator,0.16,iSeparator,0.16},
						{height1}};
		setLayout(new TableLayout(size));
		setBorder(BorderFactory.createEtchedBorder());
		//search toolbar
		jtbSearch = new JToolBar();
		jtbSearch.setFloatable(false);
		sbSearch = new SearchBox(this);
		jtbSearch.add(sbSearch);
			
		//history toolbar
		jtbHistory = new JToolBar();
		jcbHistory = new SteppedComboBox(History.getInstance().getHistory().toArray());
		jtbHistory.setFloatable(false);
		jcbHistory.setMinimumSize(new Dimension(150,20));
		jcbHistory.setPreferredSize(new Dimension(300,20));
		jcbHistory.setMaximumSize(new Dimension(300,20));
		jcbHistory.setPopupWidth(1000);
		jcbHistory.setToolTipText(Messages.getString("CommandJPanel.play_history_1")); //$NON-NLS-1$
		jcbHistory.addActionListener(this);
		jtbHistory.add(jcbHistory);
		
		//Mode toolbar
		jtbMode = new JToolBar();
		jtbMode.setRollover(true);
		jtbMode.setFloatable(false);
		jbRepeat = new JButton(Util.getIcon(ConfigurationManager.getProperty(CONF_ICON_REPEAT))); 
		jbRepeat.setActionCommand(EVENT_REPEAT_MODE_STATUS_CHANGED);
		jbRepeat.setToolTipText("Repeat mode : play tracks in a loop");
		jbRepeat.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbRepeat);
		jbRandom = new JButton(Util.getIcon(ConfigurationManager.getProperty(CONF_ICON_SHUFFLE)));
		jbRandom.setToolTipText(Messages.getString("CommandJPanel.shuffle_mode___play_a_random_track_from_the_selection_2")); //$NON-NLS-1$
		jbRandom.setActionCommand(EVENT_SHUFFLE_MODE_STATUS_CHANGED);
		jbRandom.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbRandom);
		jbContinue = new JButton(Util.getIcon(ConfigurationManager.getProperty(CONF_ICON_CONTINUE))); 
		jbContinue.setToolTipText(Messages.getString("CommandJPanel.continue_mode___continue_to_play_next_tracks_when_finished_3")); //$NON-NLS-1$
		jbContinue.setActionCommand(EVENT_CONTINUE_MODE_STATUS_CHANGED);
		jbContinue.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbContinue);
		jbIntro = new JButton(Util.getIcon(ConfigurationManager.getProperty(CONF_ICON_INTRO))); 
		jbIntro.setToolTipText(Messages.getString("CommandJPanel.intro_mode___play_just_a_part_of_each_track_offset_and_time_can_be_set_in_the_parameters_view_4")); //$NON-NLS-1$
		jbIntro.setActionCommand(EVENT_INTRO_MODE_STATUS_CHANGED);
		jbIntro.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbIntro);
		
		//Special functions toolbar
		jtbSpecial = new JToolBar();
		jtbSpecial.setFloatable(false);
		jtbSpecial.setRollover(true);
		jbGlobalRandom = new JButton(Util.getIcon(ICON_ROLL)); 
		jbGlobalRandom.addActionListener(this);
		jbGlobalRandom.setToolTipText(Messages.getString("CommandJPanel.Play_a_shuffle_selection_from_the_entire_collection_1")); //$NON-NLS-1$
		jtbSpecial.add(jbGlobalRandom);
		jbBestof = new JButton(Util.getIcon(ICON_BESTOF)); 
		jbBestof.addActionListener(this);
		jbBestof.setToolTipText(Messages.getString("CommandJPanel.Play_your_own_favorite_tracks_2")); //$NON-NLS-1$
		jtbSpecial.add(jbBestof);
		jbMute = new JButton(Util.getIcon(ICON_MUTE)); 
		jbMute.addActionListener(this);
		jbMute.setToolTipText(Messages.getString("CommandJPanel.Turn_sound_off_3")); //$NON-NLS-1$
		jtbSpecial.add(jbMute);
		
		//Play toolbar
		jtbPlay = new JToolBar();
		jtbPlay.setRollover(true);
		jtbPlay.setFloatable(false);
		jbPrevious = new JButton(Util.getIcon(ICON_PREVIOUS)); 
		jbPrevious.setToolTipText(Messages.getString("CommandJPanel.Play_previous_track_in_current_selection_4")); //$NON-NLS-1$
		jbPrevious.addActionListener(this);
		jtbPlay.add(jbPrevious);
		jbNext = new JButton(Util.getIcon(ICON_NEXT)); 
		jbNext.setToolTipText(Messages.getString("CommandJPanel.Play_next_track_in_current_selection_5")); //$NON-NLS-1$
		jbNext.addActionListener(this);
		jtbPlay.add(jbNext);
		jtbPlay.addSeparator();
		jbRew = new JButton(Util.getIcon(ICON_REW)); 
		jbRew.setToolTipText(Messages.getString("CommandJPanel.Fast_rewind_in_current_track_6")); //$NON-NLS-1$
		jbRew.addActionListener(this);
		jtbPlay.add(jbRew);
		jbPlayPause = new JButton(Util.getIcon(ICON_PLAY)); 
		jbPlayPause.setToolTipText(Messages.getString("CommandJPanel.Play/pause_current_track_7")); //$NON-NLS-1$
		jbPlayPause.addActionListener(this);
		jtbPlay.add(jbPlayPause);
		jbStop = new JButton(Util.getIcon(ICON_STOP)); 
		jbStop.setToolTipText(Messages.getString("CommandJPanel.Stop_current_track_8")); //$NON-NLS-1$
		jbStop.addActionListener(this);
		jtbPlay.add(jbStop);
		jbFwd = new JButton(Util.getIcon(ICON_FWD)); 
		jbFwd.setToolTipText(Messages.getString("CommandJPanel.Fast_forward_in_current_track_9")); //$NON-NLS-1$
		jbFwd.addActionListener(this);
		jtbPlay.add(jbFwd);
		
		//Volume toolbar
		jtbVolume = new JToolBar();
		jtbVolume.setFloatable(false);
		jlVolume = new JLabel(Util.getIcon(ICON_VOLUME)); 
		jtbVolume.add(jlVolume);
		jsVolume = new JSlider(0,100,50);
		jsVolume.setToolTipText(Messages.getString("CommandJPanel.Volume_1")); //$NON-NLS-1$
		jsVolume.addChangeListener(this);
		jtbVolume.add(jsVolume);
		
		//Position toolbar
		jtbPosition = new JToolBar();
		jtbPosition.setFloatable(false);
		jlPosition = new JLabel(Util.getIcon(ICON_POSITION)); 
		jtbPosition.add(jlPosition);
		jsPosition = new JSlider(0,100,0);
		jsPosition.addChangeListener(this);
		jsPosition.setToolTipText(Messages.getString("CommandJPanel.Go_to_this_position_in_the_played_track_2")); //$NON-NLS-1$
		jtbPosition.add(jsPosition);
				
		//add toolbars to main panel
		add(jtbSearch,"0,0");
		add(jtbHistory,"2,0");
		add(jtbMode,"4,0");
		add(jtbSpecial,"6,0");
		add(jtbPlay,"8,0");
		add(jtbVolume,"10,0");
		add(jtbPosition,"12,0");
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
		jcbHistory.removeAllItems();
	}

	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		if ( ae.getSource() == jcbHistory){
			HistoryItem hi = History.getInstance().getHistoryItem(jcbHistory.getSelectedIndex());
			if (hi != null){
				org.jajuk.base.File file = FileManager.getFile(hi.getFileId());
				FIFO.getInstance().push(file,false);
			}
		}
		if (ae.getSource() == jbGlobalRandom ){
			org.jajuk.base.File file = FileManager.getShuffleFile();
			if (file != null){
				FIFO.getInstance().setBestof(false); //break best of mode if set
				FIFO.getInstance().setGlobalRandom(true);
				FIFO.getInstance().push(file,false,true);
			}
		}
		if (ae.getSource() == jbBestof ){
			org.jajuk.base.File file = FileManager.getBestOfFile();
			if (file != null){
				FIFO.getInstance().setGlobalRandom(false); //break global random mode if set
				FIFO.getInstance().setBestof(true);
				FIFO.getInstance().push(file,false,true);
			}
		}
		else if (ae.getSource() == jbMute ){
			Util.setMute(!Util.getMute());
		}
		else if(ae.getSource() == jbStop){
			FIFO.getInstance().stopRequest();
		}
		else if(ae.getSource() == jbPlayPause){
			FIFO.getInstance().pauseRequest();
		}
		else if (ae.getSource() == jbPrevious){
			FIFO.getInstance().playPrevious();
		}
		else if (ae.getSource() == jbNext){
			FIFO.getInstance().playNext();
		}
		else if (ae.getSource() == jbRew){
			int iCurrentPosition = FIFO.getInstance().getCurrentPosition();
			int iTrackLength = (int)(FIFO.getInstance().getCurrentFile().getTrack().getLength());
			float fCurrentPercent = 100*(float)iCurrentPosition/iTrackLength;
			FIFO.getInstance().setCurrentPosition(fCurrentPercent-JUMP_SIZE);
		}
		else if (ae.getSource() == jbFwd){
			int iCurrentPosition = FIFO.getInstance().getCurrentPosition();
			int iTrackLength = (int)(FIFO.getInstance().getCurrentFile().getTrack().getLength());
			float fCurrentPercent = 100*(float)iCurrentPosition/iTrackLength;
			FIFO.getInstance().setCurrentPosition(fCurrentPercent+JUMP_SIZE);
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()){
			SearchResult sr = (SearchResult)sbSearch.alResults.get(sbSearch.jlist.getSelectedIndex());
			FIFO.getInstance().push(sr.getFile(),false);
			sbSearch.popup.hide();
			requestFocus();	
		}
	}
	
	 /*
	  *  @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if ( e.getSource() == jsVolume){
			Util.setVolume((float)jsVolume.getValue());
		}
		else if (e.getSource() == jsPosition && !bPositionChanging){
			bPositionChanging = true;
			new Thread(){  //set position asynchonously and after a delay to take only one value when slider is moved
				public void run(){
					try{
						Thread.sleep(500);
					}
					catch(InterruptedException ie){
						Log.error(ie);
					}
					FIFO.getInstance().setCurrentPosition(jsPosition.getValue());
					bPositionChanging = false;
				}
			}.start();
		}
	}

	/**
	 * Set Slider position
	 * @param i percentage of slider
	 */
	public void setCurrentPosition(int i){
		if ( !bPositionChanging ){//don't move slider when user do it himself a	t the same time
			bPositionChanging = true;  //block events so player is not affected
			this.jsPosition.setValue(i);
			bPositionChanging = false;
		}
	}

}
