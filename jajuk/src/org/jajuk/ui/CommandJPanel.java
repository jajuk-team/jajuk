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
 * $Log$
 * Revision 1.8  2003/11/22 15:40:28  bflorat
 * 22/11/2003
 *
 * Revision 1.7  2003/11/18 18:58:07  bflorat
 * 18/11/2003
 *
 * Revision 1.6  2003/10/21 20:43:06  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 * Revision 1.5  2003/10/17 20:43:56  bflorat
 * 17/10/2003
 *
 * Revision 1.4  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 * Revision 1.3  2003/10/10 22:32:42  bflorat
 * *** empty log message ***
 *
 * Revision 1.2  2003/10/10 15:58:26  bflorat
 * - Set rollover on buttons
 * - border
 *
 * Revision 1.1  2003/10/07 21:02:22  bflorat
 * Initial commit
 *
 */
package org.jajuk.ui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;

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
		//set default layout and size
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS)); //we use a BoxLayout and not a FlowLayout to allow resizing
		setBorder(BorderFactory.createEtchedBorder());
		//dimensions
		int height1 = 25;  //buttons, components
		int height2 = 36; //slider ( at least this height in the gtk+ l&f ) 
		Dimension d25_1 = new Dimension(25,height1);
		Dimension d50_1 = new Dimension(50,height1);
		Dimension d75_1 = new Dimension(75,height1);
		Dimension d90_1 = new Dimension(90,height1);
		Dimension d100_1 = new Dimension(100,height1);
		Dimension d125_1 = new Dimension(125,height1);
		Dimension d160_1 = new Dimension(160,height1);
		Dimension d180_1 = new Dimension(180,height1);
		Dimension d200_1 = new Dimension(200,height1);
		Dimension d225_1 = new Dimension(225,height1);
		Dimension d230_1 = new Dimension(230,height1);
		Dimension d250_1 = new Dimension(250,height1);
		
		Dimension d50_2 = new Dimension(50,height2);
		Dimension d125_2 = new Dimension(125,height2);
		Dimension d160_2 = new Dimension(160,height2);
		Dimension d225_2 = new Dimension(225,height2);
		Dimension d200_2 = new Dimension(200,height2);
		
				
		//search toolbar
		jtbSearch = new JToolBar();
		jtbSearch.setMinimumSize(d50_2); //We set the same sizes to the toolbar and the text field to avoid getting blanks
		jtbSearch.setPreferredSize(d200_2);
		jtbSearch.setMaximumSize(d200_2);
		jtfSearch = new JTextField();
		jtfSearch.setToolTipText(Messages.getString("CommandJPanel.search_1")); //$NON-NLS-1$
		jtfSearch.setMinimumSize(d50_1);
		jtfSearch.setPreferredSize(d200_1);
		jtfSearch.setMaximumSize(d200_1);
		jtbSearch.add(jtfSearch);
		
		//history toolbar
		jtbHistory = new JToolBar();
		jtbHistory.setMinimumSize(d50_2);
		jtbHistory.setPreferredSize(d225_2);
		jtbHistory.setMaximumSize(d225_2);
		jcbHistory = new JComboBox();
		jcbHistory.setMinimumSize(d25_1);
		jcbHistory.setPreferredSize(d200_1);
		jcbHistory.setMaximumSize(d200_1);
		jcbHistory.setToolTipText(Messages.getString("CommandJPanel.play_history_1")); //$NON-NLS-1$
		jtbHistory.add(jcbHistory);
		
		//Mode toolbar
		jtbMode = new JToolBar();
		jtbMode.setRollover(true);
		jtbMode.setMinimumSize(d160_2);
		jtbMode.setPreferredSize(d160_2);
		jtbMode.setMaximumSize(d160_2);
		jbRepeat = new JButton(new ImageIcon(ConfigurationManager.getProperty(CONF_ICON_REPEAT))); 
		jbRepeat.setActionCommand(EVENT_REPEAT_MODE_STATUS_CHANGED);
		jbRepeat.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbRepeat);
		jtbMode.addSeparator();
		jbRandom = new JButton(new ImageIcon(ConfigurationManager.getProperty(CONF_ICON_SHUFFLE)));
		jbRandom.setToolTipText(Messages.getString("CommandJPanel.shuffle_mode___play_a_random_track_from_the_selection_2")); //$NON-NLS-1$
		jbRandom.setActionCommand(EVENT_SHUFFLE_MODE_STATUS_CHANGED);
		jbRandom.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbRandom);
		jtbMode.addSeparator();
		jbContinue = new JButton(new ImageIcon(ConfigurationManager.getProperty(CONF_ICON_CONTINUE))); 
		jbContinue.setToolTipText(Messages.getString("CommandJPanel.continue_mode___continue_to_play_next_tracks_when_finished_3")); //$NON-NLS-1$
		jbContinue.setActionCommand(EVENT_CONTINUE_MODE_STATUS_CHANGED);
		jbContinue.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbContinue);
		jtbMode.addSeparator();
		jbIntro = new JButton(new ImageIcon(ConfigurationManager.getProperty(CONF_ICON_INTRO))); 
		jbIntro.setToolTipText(Messages.getString("CommandJPanel.intro_mode___play_just_a_part_of_each_track_offset_and_time_can_be_set_in_the_parameters_view_4")); //$NON-NLS-1$
		jbIntro.setActionCommand(EVENT_INTRO_MODE_STATUS_CHANGED);
		jbIntro.addActionListener(JajukListener.getInstance());
		jtbMode.add(jbIntro);
		
		//Special functions toolbar
		jtbSpecial = new JToolBar();
		jtbSpecial.setRollover(true);
		jtbSpecial.setMinimumSize(d125_2);
		jtbSpecial.setPreferredSize(d125_2);
		jtbSpecial.setMaximumSize(d125_2);
		jbGlobalRandom = new JButton(new ImageIcon(ICON_ROLL)); 
		jbGlobalRandom.setToolTipText(Messages.getString("CommandJPanel.Play_a_shuffle_selection_from_the_entire_collection_1")); //$NON-NLS-1$
		jtbSpecial.add(jbGlobalRandom);
		jtbSpecial.addSeparator();
		jbBestof = new JButton(new ImageIcon(ICON_BESTOF)); 
		jbBestof.setToolTipText(Messages.getString("CommandJPanel.Play_your_own_favorite_tracks_2")); //$NON-NLS-1$
		jtbSpecial.add(jbBestof);
		jtbSpecial.addSeparator();
		jbMute = new JButton(new ImageIcon(ICON_MUTE)); 
		jbMute.setToolTipText(Messages.getString("CommandJPanel.Turn_sound_off_3")); //$NON-NLS-1$
		
		jtbSpecial.add(jbMute);
		
		//Play toolbar
		jtbPlay = new JToolBar();
		jtbPlay.setRollover(true);
		jtbPlay.setMinimumSize(d200_2);
		jtbPlay.setPreferredSize(d200_2);
		jtbPlay.setMaximumSize(d200_2);
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
		jtbVolume.setMinimumSize(d50_1);
		jtbVolume.setPreferredSize(d200_2);
		jtbVolume.setMaximumSize(d200_2);
		jlVolume = new JLabel(new ImageIcon(ICON_VOLUME)); 
		jtbVolume.add(jlVolume);
		jsVolume = new JSlider(0,100,50);
		jsVolume.setToolTipText(Messages.getString("CommandJPanel.Volume_1")); //$NON-NLS-1$
		jsVolume.setMinimumSize(d50_2);
		jsVolume.setPreferredSize(d200_2);
		jsVolume.setMaximumSize(d200_2);
		jtbVolume.add(jsVolume);
		
		//Position toolbar
		jtbPosition = new JToolBar();
		jtbPosition.setMinimumSize(d50_2);
		jtbPosition.setPreferredSize(d200_2);
		jtbPosition.setMaximumSize(d200_2);
		jlPosition = new JLabel(new ImageIcon(ICON_POSITION)); 
		jtbPosition.add(jlPosition);
		jsPosition = new JSlider(0,100,0);
		jsPosition.setToolTipText(Messages.getString("CommandJPanel.Go_to_this_position_in_the_played_track_2")); //$NON-NLS-1$
		jsPosition.setMinimumSize(d50_2);
		jsPosition.setPreferredSize(d200_2);
		jsPosition.setMaximumSize(d200_2);
		jtbPosition.add(jsPosition);
				
		//add toolbars to main panel
		add(jtbSearch);
		add(jtbHistory);
		add(jtbMode);
		add(jtbSpecial);
		add(jtbPlay);
		add(jtbVolume);
		add(jtbPosition);
	}

}
