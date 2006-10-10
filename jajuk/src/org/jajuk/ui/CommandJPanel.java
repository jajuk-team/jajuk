/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

import static org.jajuk.ui.action.JajukAction.BEST_OF;
import static org.jajuk.ui.action.JajukAction.CONFIGURE_AMBIENCES;
import static org.jajuk.ui.action.JajukAction.CONFIGURE_DJS;
import static org.jajuk.ui.action.JajukAction.DECREASE_VOLUME;
import static org.jajuk.ui.action.JajukAction.FAST_FORWARD_TRACK;
import static org.jajuk.ui.action.JajukAction.FINISH_ALBUM;
import static org.jajuk.ui.action.JajukAction.INCREASE_VOLUME;
import static org.jajuk.ui.action.JajukAction.MUTE_STATE;
import static org.jajuk.ui.action.JajukAction.NEXT_ALBUM;
import static org.jajuk.ui.action.JajukAction.NEXT_TRACK;
import static org.jajuk.ui.action.JajukAction.NOVELTIES;
import static org.jajuk.ui.action.JajukAction.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.action.JajukAction.PREVIOUS_ALBUM;
import static org.jajuk.ui.action.JajukAction.PREVIOUS_TRACK;
import static org.jajuk.ui.action.JajukAction.REPEAT_MODE_STATUS_CHANGE;
import static org.jajuk.ui.action.JajukAction.REWIND_TRACK;
import static org.jajuk.ui.action.JajukAction.SHUFFLE_GLOBAL;
import static org.jajuk.ui.action.JajukAction.SHUFFLE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.action.JajukAction.STOP_TRACK;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.HistoryItem;
import org.jajuk.base.JajukTimer;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Player;
import org.jajuk.base.SearchResult;
import org.jajuk.base.StackItem;
import org.jajuk.dj.Ambience;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.dj.DigitalDJ;
import org.jajuk.dj.DigitalDJManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.action.ActionBase;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.ActionUtil;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import ext.DropDownButton;
import ext.SwingWorker;

/**
 *  Command panel ( static view )
 * <p>Singleton</p>
 * @author     Bertrand Florat
 * @created    3 oct. 2003
 */
public class CommandJPanel extends JPanel implements ITechnicalStrings,ActionListener,ListSelectionListener,ChangeListener,Observer,MouseWheelListener{

    private static final long serialVersionUID = 1L;
    
	//singleton
	static private CommandJPanel command;

	//widgets declaration
	SearchBox sbSearch;
	SteppedComboBox jcbHistory;
	JPanel jpMode;
	public JajukToggleButton jbRepeat;
	public JajukToggleButton jbRandom;
	public JajukToggleButton jbContinue;
	public JajukToggleButton jbIntro;
	JToolBar jtbSpecial;
    DropDownButton jbGlobalRandom;
    JMenuItem jmiShuffleModeSong;
    JMenuItem jmiShuffleModeAlbum;
    JPopupMenu popupGlobalRandom;
    JButton jbBestof;
	DropDownButton jbNovelties;
	JPopupMenu popupNovelties;
    JMenuItem jmiNoveltiesModeSong;
    JMenuItem jmiNoveltiesModeAlbum;
    JButton jbNorm;
    DropDownButton ddbDDJ;
    JPopupMenu popupDDJ;
	SteppedComboBox ambiencesCombo;
    
    JPanel jpPlay;
	JButton jbPrevious;
	JButton jbNext;
	JPressButton jbRew;
	JButton jbPlayPause;
	JButton jbStop;
	JPressButton jbFwd;
	JLabel jlVolume;
	JPanel jpVolume;
    JSlider jsVolume;
	JPanel jpPosition;
    JLabel jlPosition;
	JSlider jsPosition;
	public JajukToggleButton jbMute;

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
	/**Last slider manual move date*/
	private static long lDateLastAdjust;
	/** Swing Timer to refresh the component*/
	private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT,new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			update(new Event(EventSubject.EVENT_HEART_BEAT));
		}
	});
    /**Ambience combo listener*/
	class ambienceListener implements ActionListener{
	    public void actionPerformed(ActionEvent ae) {
	        //Selected 'Any" ambience
	        if (ambiencesCombo.getSelectedIndex() == 0){
	            //reset default ambience
	            ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE,""); //$NON-NLS-1$
	        }
	        else {//Selected an ambience
	            Ambience ambience = AmbienceManager.getInstance().getAmbienceByName((String)ambiencesCombo.getSelectedItem());    
	            ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE,ambience.getID());
	        }
	        ObservationManager.notify(new Event(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE));
        }
     }
     /**An instance of the ambience combo listener*/
     ambienceListener ambienceListener;
        

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
	 * Constructor, this objects needs to be implemented for the tray (child object)
	 */
	CommandJPanel(){
	    //mute
        jbMute = new JajukToggleButton(ActionManager.getAction(MUTE_STATE));
    }
    
    public void initUI(){
		sbSearch = new SearchBox(CommandJPanel.this);
		//history
		jcbHistory = new SteppedComboBox();
        //we use a combobox model to make sure we get good performances after rebuilding the entire model like after a refresh
        jcbHistory.setModel(new DefaultComboBoxModel(History.getInstance().getHistory()));
    	int iWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2);
		jcbHistory.setPopupWidth(iWidth);
		jcbHistory.setToolTipText(Messages.getString("CommandJPanel.0")); //$NON-NLS-1$
		jcbHistory.addActionListener(CommandJPanel.this);

        //Mode toolbar
		jpMode = new JPanel();
		jpMode.setLayout(new BoxLayout(jpMode,BoxLayout.X_AXIS));

        jbRepeat = new JajukToggleButton(ActionManager.getAction(REPEAT_MODE_STATUS_CHANGE));
        jbRepeat.setSelected(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));

        jbRandom = new JajukToggleButton(ActionManager.getAction(SHUFFLE_MODE_STATUS_CHANGED));
        jbRandom.setSelected(ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE));

        jbContinue = new JajukToggleButton(ActionManager.getAction(JajukAction.CONTINUE_MODE_STATUS_CHANGED));
        jbContinue.setSelected(ConfigurationManager.getBoolean(CONF_STATE_CONTINUE));

        jbIntro = new JajukToggleButton(ActionManager.getAction(JajukAction.INTRO_MODE_STATUS_CHANGED));
        jbIntro.setSelected(ConfigurationManager.getBoolean(CONF_STATE_INTRO));

		jpMode.add(jbRepeat);
		jpMode.add(jbRandom);
		jpMode.add(jbContinue);
		jpMode.add(jbIntro);
        
        //Volume
        jpVolume = new JPanel();
        ActionUtil.installKeystrokes(jpVolume, ActionManager.getAction(DECREASE_VOLUME),
                                     ActionManager.getAction(INCREASE_VOLUME));

        jpVolume.setLayout(new BoxLayout(jpVolume,BoxLayout.X_AXIS));
        jlVolume = new JLabel(Util.getIcon(ICON_VOLUME));
        int iVolume = (int)(100*ConfigurationManager.getFloat(CONF_VOLUME));
        if (iVolume > 100){ //can occur in some undefined cases
            iVolume = 100;
        }
        jsVolume = new JSlider(0,100,iVolume);
        jpVolume.add(jlVolume);
        jpVolume.add(jsVolume);
        jsVolume.setToolTipText(Messages.getString("CommandJPanel.14")); //$NON-NLS-1$
        jsVolume.addChangeListener(CommandJPanel.this);
        jsVolume.addMouseWheelListener(CommandJPanel.this);

        //Position
        jpPosition = new JPanel();
        jpPosition.setLayout(new BoxLayout(jpPosition,BoxLayout.X_AXIS));
        jlPosition = new JLabel(Util.getIcon(ICON_POSITION));
        jsPosition = new JSlider(0,100,0);
        jpPosition.add(jlPosition);
        jpPosition.add(jsPosition);
        jsPosition.addChangeListener(CommandJPanel.this);
        jsPosition.setEnabled(false);
        jsPosition.setToolTipText(Messages.getString("CommandJPanel.15")); //$NON-NLS-1$
        
        //Ambience combo
        ambiencesCombo = new SteppedComboBox();
        iWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()/4);
        ambiencesCombo.setPopupWidth(iWidth);
        populateAmbiences();
        ambienceListener = new ambienceListener();
        ambiencesCombo.addActionListener(ambienceListener);    
		
        //Special functions toolbar
		jtbSpecial = new JToolBar();
        jtbSpecial.setFloatable(false);
        jbGlobalRandom = new DropDownButton(Util.getIcon(ICON_SHUFFLE_GLOBAL)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected JPopupMenu getPopupMenu() {
                return popupGlobalRandom;
            }
        };
        jbGlobalRandom.setAction(ActionManager.getAction(SHUFFLE_GLOBAL));
        popupGlobalRandom = new JPopupMenu();
        jmiShuffleModeSong = new JMenuItem(Messages.getString("CommandJPanel.20"));
        jmiShuffleModeSong.addActionListener(this);
        jmiShuffleModeAlbum = new JMenuItem(Messages.getString("CommandJPanel.21"));
        jmiShuffleModeAlbum.addActionListener(this);
        if (ConfigurationManager.getProperty(CONF_GLOBAL_RANDOM_MODE).equals(MODE_TRACK)){
            jmiShuffleModeSong.setSelected(true);
            //display in bold (note that selection stick is not displayed on some Laf like liquid)
            jmiShuffleModeSong.setFont(new Font("Dialog",Font.BOLD,11));
        }
        else{
            jmiShuffleModeAlbum.setSelected(true);
            jmiShuffleModeAlbum.setFont(new Font("Dialog",Font.BOLD,11));
        }
        popupGlobalRandom.add(jmiShuffleModeSong);
        popupGlobalRandom.add(jmiShuffleModeAlbum);
        jbGlobalRandom.setText("");//no text visible //$NON-NLS-1$
        
        jbBestof = new JajukButton(ActionManager.getAction(BEST_OF));
        
        jbNovelties = new DropDownButton(Util.getIcon(ICON_NOVELTIES)) {
            private static final long serialVersionUID = 1L;
            @Override
            protected JPopupMenu getPopupMenu() {
                return popupNovelties;
            }
        };
        jbNovelties.setAction(ActionManager.getAction(NOVELTIES));
        popupNovelties = new JPopupMenu();
        jmiNoveltiesModeSong = new JMenuItem(Messages.getString("CommandJPanel.20"));
        jmiNoveltiesModeSong.addActionListener(this);
        jmiNoveltiesModeAlbum = new JMenuItem(Messages.getString("CommandJPanel.21"));
        jmiNoveltiesModeAlbum.addActionListener(this);
        if (ConfigurationManager.getProperty(CONF_NOVELTIES_MODE).equals(MODE_TRACK)){
            jmiNoveltiesModeSong.setSelected(true);
            //display in bold (note that selection stick is not displayed on some Laf like liquid)
            jmiNoveltiesModeSong.setFont(new Font("Dialog",Font.BOLD,11));
        }
        else{
            jmiNoveltiesModeAlbum.setSelected(true);
            jmiNoveltiesModeAlbum.setFont(new Font("Dialog",Font.BOLD,11));
        }
        popupNovelties.add(jmiNoveltiesModeSong);
        popupNovelties.add(jmiNoveltiesModeAlbum);
        jbNovelties.setText("");//no text visible //$NON-NLS-1$
        
		jbNorm = new JajukButton(ActionManager.getAction(FINISH_ALBUM));
        popupDDJ = new JPopupMenu();
        ddbDDJ = new DropDownButton(Util.getIcon(ICON_DIGITAL_DJ)) {
        	private static final long serialVersionUID = 1L;
            @Override
        	protected JPopupMenu getPopupMenu() {
        		return popupDDJ;
        	}
        };
        ddbDDJ.setAction(ActionManager.getAction(JajukAction.DJ));
        populateDJs();
        ddbDDJ.setText("");//no text visible //$NON-NLS-1$
        jbGlobalRandom.addToToolBar(jtbSpecial);
        jbNovelties.addToToolBar(jtbSpecial);
        ddbDDJ.addToToolBar(jtbSpecial);
        jtbSpecial.add(jbBestof);
        jtbSpecial.add(jbNorm);
        
		//Play toolbar
        jpPlay = new JPanel();
        ActionUtil.installKeystrokes(jpPlay, ActionManager.getAction(NEXT_ALBUM),
                                     ActionManager.getAction(PREVIOUS_ALBUM));
        jpPlay.setLayout(new BoxLayout(jpPlay, BoxLayout.X_AXIS));
        jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
        jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
        jbRew = new JPressButton(ActionManager.getAction(REWIND_TRACK));
        jbPlayPause = new JajukButton(ActionManager.getAction(PLAY_PAUSE_TRACK));
        jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
        jbFwd = new JPressButton(ActionManager.getAction(FAST_FORWARD_TRACK));

        jpPlay.add(jbPrevious);
        jpPlay.add(jbNext);
		jpPlay.add(Box.createHorizontalGlue());
		jpPlay.add(jbRew);
		jpPlay.add(jbPlayPause);
		jpPlay.add(jbStop);
		jpPlay.add(jbFwd);

		//dimensions
		int height1 = 25;  //buttons, components
		int iXSeparator = 10;
		//set default layout and size
		double[][] size ={{iXSeparator/2,0.14,iXSeparator, //search box
			TableLayout.FILL,iXSeparator,// history 
			TableLayout.PREFERRED,iXSeparator, //mode buttons
			0.1,iXSeparator, //Ambience combo
            TableLayout.PREFERRED,iXSeparator, //special functions buttons
			TableLayout.PREFERRED,iXSeparator, //play buttons
			0.2,iXSeparator/2, //position
			0.2,iXSeparator, //volume
			20,iXSeparator},   //mute button
			{height1}}; //note we can't set a % for history combo box because of popup size
		TableLayout layout = new TableLayout(size);
        setLayout(layout);
		setAlignmentY(Component.CENTER_ALIGNMENT);

		//add toolbars to main panel
		add(sbSearch,"1,0"); //$NON-NLS-1$
		add(jcbHistory,"3,0"); //$NON-NLS-1$
		add(jpMode,"5,0,c,c");  //$NON-NLS-1$
		add(ambiencesCombo,"7,0,c,c"); //$NON-NLS-1$
        add(jtbSpecial,"9,0,c,c"); //$NON-NLS-1$
		add(jpPlay,"11,0,c,c"); //$NON-NLS-1$
		add(jpPosition,"13,0"); //$NON-NLS-1$
		add(jpVolume,"15,0"); //$NON-NLS-1$
		add(jbMute,"17,0"); //$NON-NLS-1$

		//register to player events
		ObservationManager.register(CommandJPanel.this);
        
        //if a track is playing, display right state
		if ( FIFO.getInstance().getCurrentFile() != null){
			//update initial state
			update(new Event(EventSubject.EVENT_PLAYER_PLAY,ObservationManager.getDetailsLastOccurence(EventSubject.EVENT_PLAYER_PLAY)));
			//check if some track has been lauched before the view has been displayed
			update(new Event(EventSubject.EVENT_HEART_BEAT));
		}
		//start timer
		timer.start();
	}

    public Set<EventSubject> getRegistrationKeys(){
        HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
        eventSubjectSet.add(EventSubject.EVENT_PLAYER_PLAY);
        eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
        eventSubjectSet.add(EventSubject.EVENT_PLAYER_PAUSE);
        eventSubjectSet.add(EventSubject.EVENT_PLAYER_RESUME);
        eventSubjectSet.add(EventSubject.EVENT_PLAY_ERROR);
        eventSubjectSet.add(EventSubject.EVENT_SPECIAL_MODE);
        eventSubjectSet.add(EventSubject.EVENT_ZERO);
        eventSubjectSet.add(EventSubject.EVENT_MUTE_STATE);
        eventSubjectSet.add(EventSubject.EVENT_REPEAT_MODE_STATUS_CHANGED);
        eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
        eventSubjectSet.add(EventSubject.EVENT_CLEAR_HISTORY);
        eventSubjectSet.add(EventSubject.EVENT_VOLUME_CHANGED);
        eventSubjectSet.add(EventSubject.EVENT_DJS_CHANGE);
        eventSubjectSet.add(EventSubject.EVENT_AMBIENCES_CHANGE);
        eventSubjectSet.add(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE);
        return eventSubjectSet;
    }
    
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent ae) {
		//do not run this in a separate thread because Player actions would die with the thread
		try{
			if ( ae.getSource() == jcbHistory){
			    HistoryItem hi;
			    hi = History.getInstance().getHistoryItem(jcbHistory.getSelectedIndex());
			    if (hi != null){
			        org.jajuk.base.File file = (File)FileManager.getInstance().getItem(hi.getFileId());
			        if (file != null){ 
			            try{
			                FIFO.getInstance().push(new StackItem(file,ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
			            }
			            catch(JajukException je){  //can be thrown if file is null
                        }
			        }
			        else{
			            Messages.showErrorMessage("120"); //$NON-NLS-1$ //$NON-NLS-2$
			            jcbHistory.setSelectedItem(null);
			        }
			    }
			}
            else if (ae.getSource().equals(jmiNoveltiesModeSong)){
                ConfigurationManager.setProperty(CONF_NOVELTIES_MODE, MODE_TRACK);
                jmiNoveltiesModeSong.setFont(new Font("Dialog",Font.BOLD,11));
                jmiNoveltiesModeAlbum.setFont(new Font("Dialog",Font.PLAIN,11));
            }
            else if (ae.getSource().equals(jmiNoveltiesModeAlbum)){
                ConfigurationManager.setProperty(CONF_NOVELTIES_MODE, MODE_ALBUM);
                jmiNoveltiesModeAlbum.setFont(new Font("Dialog",Font.BOLD,11));
                jmiNoveltiesModeSong.setFont(new Font("Dialog",Font.PLAIN,11));
            }
            else if (ae.getSource().equals(jmiShuffleModeSong)){
                ConfigurationManager.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_TRACK);
                jmiShuffleModeSong.setFont(new Font("Dialog",Font.BOLD,11));
                jmiShuffleModeAlbum.setFont(new Font("Dialog",Font.PLAIN,11));
            }
            else if (ae.getSource().equals(jmiShuffleModeAlbum)){
                ConfigurationManager.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_ALBUM);
                jmiShuffleModeAlbum.setFont(new Font("Dialog",Font.BOLD,11));
                jmiShuffleModeSong.setFont(new Font("Dialog",Font.PLAIN,11));
            }
		}
		catch(Exception e){
			Log.error(e);
		}
		finally{
			ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH)); //refresh playlist editor
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
					try {
						FIFO.getInstance().push(new StackItem(sr.getFile(),ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
					}
					catch(JajukException je){
						Log.error(je);
					}
				}
				return null;
			}

			public void finished() {
				if (!e.getValueIsAdjusting()){
					sbSearch.popup.hide();
					requestFocusInWindow();
				}
			}
		};
		sw.start();
	}

	/*
	 *  @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if ( e.getSource() == jsVolume ){
            if (System.currentTimeMillis()-lDateLastAdjust > 20){ //this value should be low to make sure we can reach zero
                setVolume((float)jsVolume.getValue()/100);
                lDateLastAdjust = System.currentTimeMillis();
            }
        }
        else if (e.getSource() == jsPosition ){
            lDateLastAdjust = System.currentTimeMillis();
			setPosition((float)jsPosition.getValue()/100);
        }
	}

    /**
     * Call a seek
     * @param fPosition
     */
	private void setPosition(final float fPosition){
        new Thread(){
            public void run(){
                Player.seek(fPosition);        
            }
        }.start();
    }

	/**
	 * @return Position value
	 */
	public int getCurrentPosition(){
		return this.jsPosition.getValue();
	}


	/**
	 * @return Volume value
	 */
	public int getCurrentVolume(){
		return this.jsVolume.getValue();
	}


	private void setVolume(final float fVolume){
	    jsVolume.removeChangeListener(CommandJPanel.this);
	    jsVolume.removeMouseWheelListener(CommandJPanel.this);
	    //if user move the volume slider, unmute
	    if (Player.isMuted()){
	        Player.mute(false);    
        }
        Player.setVolume(fVolume);
	    jsVolume.addChangeListener(CommandJPanel.this);
	    jsVolume.addMouseWheelListener(CommandJPanel.this);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(final Event event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
                EventSubject subject = event.getSubject();
				if(EventSubject.EVENT_PLAYER_STOP.equals(subject) || EventSubject.EVENT_ZERO.equals(subject)){
                    ActionManager.getAction(PREVIOUS_TRACK).setEnabled(false);
                    ActionManager.getAction(NEXT_TRACK).setEnabled(false);
                    ActionManager.getAction(REWIND_TRACK).setEnabled(false);
                    ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(false);
                    ActionManager.getAction(STOP_TRACK).setEnabled(false);
                    ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
                    ActionManager.getAction(NEXT_ALBUM).setEnabled(false);
                    ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(false);
                    ActionManager.getAction(FINISH_ALBUM).setEnabled(false);
                    ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(Util.getIcon(ICON_PAUSE));
                    jsPosition.setEnabled(false);
                    jsPosition.removeMouseWheelListener(CommandJPanel.this);
					jsPosition.removeChangeListener(CommandJPanel.this);
                    jsPosition.setValue(0);//use set value, not setPosition that would cause a seek that could fail with some formats
			        ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION,"0");//reset startup position //$NON-NLS-1$
				}
				else if (EventSubject.EVENT_PLAYER_PLAY.equals(subject)){
                    //remove and re-add listener to make sure not to add it twice
                    jsPosition.removeMouseWheelListener(CommandJPanel.this);
                    jsPosition.addMouseWheelListener(CommandJPanel.this);
                    jsPosition.removeChangeListener(CommandJPanel.this);
                    jsPosition.addChangeListener(CommandJPanel.this);
                    ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
                    ActionManager.getAction(NEXT_TRACK).setEnabled(true);
                    ActionManager.getAction(REWIND_TRACK).setEnabled(true);
                    ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(true);
                    ActionManager.getAction(STOP_TRACK).setEnabled(true);
                    ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
                    ActionManager.getAction(NEXT_ALBUM).setEnabled(true);
                    ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(true);
                    ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
                    ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(Util.getIcon(ICON_PAUSE));
                    jsPosition.setEnabled(true);
				}
				else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)){
                    ActionManager.getAction(REWIND_TRACK).setEnabled(false);
                    ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
                    ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(Util.getIcon(ICON_PLAY));
					jsPosition.setEnabled(false);
					jsPosition.removeMouseWheelListener(CommandJPanel.this);
                    jsPosition.removeChangeListener(CommandJPanel.this);
            	}
				else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)){
                    //remove and re-add listener to make sure not to add it twice
                    jsPosition.removeMouseWheelListener(CommandJPanel.this);
                    jsPosition.addMouseWheelListener(CommandJPanel.this);
                    jsPosition.removeChangeListener(CommandJPanel.this);
                    jsPosition.addChangeListener(CommandJPanel.this);
                    ActionManager.getAction(REWIND_TRACK).setEnabled(true);
                    ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
                    ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(Util.getIcon(ICON_PAUSE));
					jsPosition.setEnabled(true);
             	}
				else if (EventSubject.EVENT_HEART_BEAT.equals(subject) &&!FIFO.isStopped() && !Player.isPaused()){
					 //if position is adjusting, no dont disturb user
                    if (jsPosition.getValueIsAdjusting() || Player.isSeeking()){
                        return;
                    }
                    //make sure not to set to old position
                    if ((System.currentTimeMillis() - lDateLastAdjust) < 2000){
                        return;
                    }
                    int iPos = (int)(100*JajukTimer.getInstance().getCurrentTrackPosition());
                    jsPosition.removeChangeListener(CommandJPanel.this);
                    jsPosition.removeChangeListener(CommandJPanel.this);
                    jsPosition.setValue(iPos);
                    jsPosition.addChangeListener(CommandJPanel.this);
				}
				else if(EventSubject.EVENT_SPECIAL_MODE.equals(subject)){
				    if (ObservationManager.getDetail(event,DETAIL_ORIGIN).equals(DETAIL_SPECIAL_MODE_NORMAL)){
						// deselect shuffle mode
						ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, FALSE);
                        JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(false);
						CommandJPanel.getInstance().jbRandom.setSelected(false);
						//computes planned tracks
						FIFO.getInstance().computesPlanned(true);
					}
				}
				else if(EventSubject.EVENT_REPEAT_MODE_STATUS_CHANGED.equals(subject)){
					if (ObservationManager.getDetail(event,DETAIL_SELECTION).equals(FALSE)){
						//    deselect repeat mode
						ConfigurationManager.setProperty(CONF_STATE_REPEAT, FALSE);
						JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(false);
						CommandJPanel.getInstance().jbRepeat.setSelected(false);
					}
				}
                else if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)){
                    //Remove history listener, otherwise u get a recursive event generation
                    jcbHistory.removeActionListener(CommandJPanel.this);
                    if (jcbHistory.getItemCount() > 0){
                        jcbHistory.setSelectedIndex(0);
                    }
                    jcbHistory.addActionListener(CommandJPanel.this);
                }
                else if(EventSubject.EVENT_CLEAR_HISTORY.equals(event.getSubject())){
                  jcbHistory.setSelectedItem(null); //clear selection bar (data itself is clear from the model by History class)
                }
                else if(EventSubject.EVENT_VOLUME_CHANGED.equals(event.getSubject())){
                    jsVolume.removeChangeListener(CommandJPanel.this);
                    jsVolume.setValue((int)(100*Player.getCurrentVolume()));
                    jsVolume.addChangeListener(CommandJPanel.this);
                    jbMute.setSelected(false);
                }
                else if(EventSubject.EVENT_DJS_CHANGE.equals(event.getSubject())){
                    populateDJs();
                    //If no more DJ, chnage the tooltip
                    if (DigitalDJManager.getInstance().getDJs().size() == 0){
                        ActionBase action = ActionManager.getAction(JajukAction.DJ);
                        action.setShortDescription(Messages.getString("CommandJPanel.18")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                }
                else if(EventSubject.EVENT_AMBIENCES_CHANGE.equals(event.getSubject())
                        || EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE.equals(event.getSubject())){
                    populateAmbiences();
                    updateTooltips();
                }
			}
		});
	}
    
	/**
     * Update global functions tooltip after a change in ambiences or an ambience selection 
     * using the ambience selector 
     *
	 */
    private void updateTooltips(){
	    //Selected 'Any" ambience
	    if (ambiencesCombo.getSelectedIndex() == 0){
	        ActionBase action = ActionManager.getAction(JajukAction.NOVELTIES);
	        action.setShortDescription(Messages.getString("JajukWindow.31")); //$NON-NLS-1$
	        action = ActionManager.getAction(JajukAction.BEST_OF);
	        action.setShortDescription(Messages.getString("JajukWindow.24")); //$NON-NLS-1$
	        action = ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL);
	        action.setShortDescription(Messages.getString("JajukWindow.23")); //$NON-NLS-1$
	    }
	    else {//Selected an ambience
	        Ambience ambience = AmbienceManager.getInstance().getAmbienceByName((String)ambiencesCombo.getSelectedItem());    
	        ActionBase action = ActionManager.getAction(JajukAction.NOVELTIES);
	        action.setShortDescription("<html>"+Messages.getString("JajukWindow.31")+"<p><b>"+ambience.getName()+"</b></p></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	        action = ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL);
	        action.setShortDescription("<html>"+Messages.getString("JajukWindow.23")+"<p><b>"+ambience.getName()+"</b></p></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	        action = ActionManager.getAction(JajukAction.BEST_OF);
	        action.setShortDescription("<html>"+Messages.getString("JajukWindow.24")+"<p><b>"+ambience.getName()+"</b></p></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    }
	}

    /**
     * Populate DJs
     *
     */
    private void populateDJs(){
        try{
            popupDDJ.removeAll();
            JMenuItem jmiNew = new JMenuItem(ActionManager.getAction(CONFIGURE_DJS));  //$NON-NLS-1$
            popupDDJ.add(jmiNew);
            popupDDJ.addSeparator();
            //Ambiences
            JMenuItem jmiAmbiences = new JMenuItem(ActionManager.getAction(CONFIGURE_AMBIENCES));  //$NON-NLS-1$
            popupDDJ.addSeparator();
            popupDDJ.add(jmiAmbiences);
            Iterator it = DigitalDJManager.getInstance().getDJs().iterator();
            while (it.hasNext()){
                final DigitalDJ dj = (DigitalDJ)it.next();
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(dj.getName(),Util.getIcon(ICON_DIGITAL_DJ));
                jmi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        ConfigurationManager.setProperty(CONF_DEFAULT_DJ,dj.getID());
                        populateDJs();
                        ActionBase action = ActionManager.getAction(JajukAction.DJ);
                        action.setShortDescription("<html>"+Messages.getString("CommandJPanel.18")+"<p><b>"+dj.getName()+"</b></p></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                });
                popupDDJ.add(jmi);
                jmi.setSelected(ConfigurationManager.getProperty(CONF_DEFAULT_DJ).equals(dj.getID()));
            }
        }
        catch(Exception e){
            Log.error(e);
        }
    }
    
    /**
     * Populate ambiences combo
     *
     */
    void populateAmbiences(){
        ambiencesCombo.removeActionListener(ambienceListener);
        ambiencesCombo.removeAllItems();
        ambiencesCombo.addItem("<html><i>"+ //$NON-NLS-1$
                Messages.getString("DigitalDJWizard.64")+"</i></html>");
        //Add available ambiences
        for (final Ambience ambience: AmbienceManager.getInstance().getAmbiences()){
            ambiencesCombo.addItem(ambience.getName());
        }
        //Select right item
        ambiencesCombo.setSelectedIndex(0); //Any by default
        //or any other existing ambience
        Ambience defaultAmbience = AmbienceManager.getInstance()
        .getAmbience(ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE));
        if (defaultAmbience != null){
            ambiencesCombo.setSelectedItem(defaultAmbience.getName());
        }
        ambiencesCombo.setToolTipText(Messages.getString("DigitalDJWizard.66"));
        ambiencesCombo.addActionListener(ambienceListener);    
   }

	/**
	 * ToString() method
	 */
	public String toString(){
		return getClass().getName();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getSource() == jsPosition){
            int iOld = jsPosition.getValue();
            int iNew = iOld - (e.getUnitsToScroll()*3);
            jsPosition.setValue(iNew);
        }
        else if (e.getSource() == jsVolume){
            int iOld = jsVolume.getValue();
            int iNew = iOld - (e.getUnitsToScroll()*3);
            jsVolume.setValue(iNew);
        }
	}
	
}
