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
 *
 */
package org.jajuk.ui;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import layout.TableLayout;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;

/**
 *  Command panel ( static view )
 *
 * @author     bflorat
 * @created    3 oct. 2003
 */
public class CommandJPanel extends JPanel implements ITechnicalStrings{
	
	//singleton
	static private CommandJPanel command;
	
	//widgets declaration
		 JToolBar jtbSearch;
			JTextField jtfSearch;
		JToolBar jtbHistory;
			JComboBox jcbHistory;
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
			JButton jbUp;
			JButton jbDown;
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
	
		
	public static CommandJPanel getInstance(){
		if (command == null){
			command = new CommandJPanel();
		}
		return command;
	}
	
	private CommandJPanel(){
		//dimensions
		int height1 = 25;  //buttons, components
		int height2 = 36; //slider ( at least this height in the gtk+ l&f ) 
		int iSeparator = 0;
		//set default layout and size
		double[][] size ={{0.15,iSeparator,0.15,iSeparator,0.13,iSeparator,0.10,iSeparator,0.19,iSeparator,0.13,iSeparator,0.13},
						{height1}};
		setLayout(new TableLayout(size));
		setBorder(BorderFactory.createEtchedBorder());
		//search toolbar
		jtbSearch = new JToolBar();
		jtfSearch = new JTextField();
		jtfSearch.setToolTipText(Messages.getString("CommandJPanel.search_1")); //$NON-NLS-1$
		jtbSearch.add(jtfSearch);
			
		//history toolbar
		jtbHistory = new JToolBar();
		jcbHistory = new JComboBox();
		jcbHistory.setToolTipText(Messages.getString("CommandJPanel.play_history_1")); //$NON-NLS-1$
		jtbHistory.add(jcbHistory);
		
		//Mode toolbar
		jtbMode = new JToolBar();
		jtbMode.setRollover(true);
		jbRepeat = new JButton(new ImageIcon(ConfigurationManager.getProperty(CONF_ICON_REPEAT))); 
		jbRepeat.setActionCommand(EVENT_REPEAT_MODE_STATUS_CHANGED);
		jbRepeat.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbRepeat);
		jbRandom = new JButton(new ImageIcon(ConfigurationManager.getProperty(CONF_ICON_SHUFFLE)));
		jbRandom.setToolTipText(Messages.getString("CommandJPanel.shuffle_mode___play_a_random_track_from_the_selection_2")); //$NON-NLS-1$
		jbRandom.setActionCommand(EVENT_SHUFFLE_MODE_STATUS_CHANGED);
		jbRandom.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbRandom);
		jbContinue = new JButton(new ImageIcon(ConfigurationManager.getProperty(CONF_ICON_CONTINUE))); 
		jbContinue.setToolTipText(Messages.getString("CommandJPanel.continue_mode___continue_to_play_next_tracks_when_finished_3")); //$NON-NLS-1$
		jbContinue.setActionCommand(EVENT_CONTINUE_MODE_STATUS_CHANGED);
		jbContinue.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbContinue);
		jbIntro = new JButton(new ImageIcon(ConfigurationManager.getProperty(CONF_ICON_INTRO))); 
		jbIntro.setToolTipText(Messages.getString("CommandJPanel.intro_mode___play_just_a_part_of_each_track_offset_and_time_can_be_set_in_the_parameters_view_4")); //$NON-NLS-1$
		jbIntro.setActionCommand(EVENT_INTRO_MODE_STATUS_CHANGED);
		jbIntro.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbIntro);
		
		//Special functions toolbar
		jtbSpecial = new JToolBar();
		jtbSpecial.setRollover(true);
		jbGlobalRandom = new JButton(new ImageIcon(ICON_ROLL)); 
		jbGlobalRandom.setToolTipText(Messages.getString("CommandJPanel.Play_a_shuffle_selection_from_the_entire_collection_1")); //$NON-NLS-1$
		jtbSpecial.add(jbGlobalRandom);
		jbBestof = new JButton(new ImageIcon(ICON_BESTOF)); 
		jbBestof.setToolTipText(Messages.getString("CommandJPanel.Play_your_own_favorite_tracks_2")); //$NON-NLS-1$
		jtbSpecial.add(jbBestof);
		jbMute = new JButton(new ImageIcon(ICON_MUTE)); 
		jbMute.setToolTipText(Messages.getString("CommandJPanel.Turn_sound_off_3")); //$NON-NLS-1$
		jtbSpecial.add(jbMute);
		
		//Play toolbar
		jtbPlay = new JToolBar();
		jtbPlay.setRollover(true);
		jbUp = new JButton(new ImageIcon(ICON_UP)); 
		jbUp.setToolTipText(Messages.getString("CommandJPanel.Play_previous_track_in_current_selection_4")); //$NON-NLS-1$
		jtbPlay.add(jbUp);
		jbDown = new JButton(new ImageIcon(ICON_DOWN)); 
		jbDown.setToolTipText(Messages.getString("CommandJPanel.Play_next_track_in_current_selection_5")); //$NON-NLS-1$
		jtbPlay.add(jbDown);
		jtbPlay.addSeparator();
		jbRew = new JButton(new ImageIcon(ICON_REW)); 
		jbRew.setToolTipText(Messages.getString("CommandJPanel.Fast_rewind_in_current_track_6")); //$NON-NLS-1$
		jtbPlay.add(jbRew);
		jbPlayPause = new JButton(new ImageIcon(ICON_PLAY)); 
		jbPlayPause.setToolTipText(Messages.getString("CommandJPanel.Play/pause_current_track_7")); //$NON-NLS-1$
		jtbPlay.add(jbPlayPause);
		jbStop = new JButton(new ImageIcon(ICON_STOP)); 
		jbStop.setToolTipText(Messages.getString("CommandJPanel.Stop_current_track_8")); //$NON-NLS-1$
		jtbPlay.add(jbStop);
		jbFwd = new JButton(new ImageIcon(ICON_FWD)); 
		jbFwd.setToolTipText(Messages.getString("CommandJPanel.Fast_forward_in_current_track_9")); //$NON-NLS-1$
		jtbPlay.add(jbFwd);
		
		//Volume toolbar
		jtbVolume = new JToolBar();
		jlVolume = new JLabel(new ImageIcon(ICON_VOLUME)); 
		jtbVolume.add(jlVolume);
		jsVolume = new JSlider(0,100,50);
		jsVolume.setToolTipText(Messages.getString("CommandJPanel.Volume_1")); //$NON-NLS-1$
		jtbVolume.add(jsVolume);
		
		//Position toolbar
		jtbPosition = new JToolBar();
		jlPosition = new JLabel(new ImageIcon(ICON_POSITION)); 
		jtbPosition.add(jlPosition);
		jsPosition = new JSlider(0,100,0);
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

}
