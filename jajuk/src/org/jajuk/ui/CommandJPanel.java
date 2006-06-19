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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Iterator;

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
import org.jajuk.dj.DigitalDJ;
import org.jajuk.dj.DigitalDJManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.ActionUtil;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.util.ConfigurationManager;
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
    JButton jbGlobalRandom;
	JButton jbBestof;
	JButton jbNovelties;
	JButton jbNorm;
    DropDownButton ddbDDJ;
    JPopupMenu popupDDJ;
	
    JPanel jpPlay;
	JButton jbPrevious;
	JButton jbNext;
	JPressButton jbRew;
	JButton jbPlayPause;
	JButton jbStop;
	JPressButton jbFwd;
	JLabel jlVolume;
	JSlider jsVolume;
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
			update(new Event(EVENT_HEART_BEAT));
		}
	});

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

		//Special functions toolbar
		jtbSpecial = new JToolBar();
        jtbSpecial.setFloatable(false);
        jbGlobalRandom = new JajukButton(ActionManager.getAction(SHUFFLE_GLOBAL));
		jbBestof = new JajukButton(ActionManager.getAction(BEST_OF));
		jbNovelties = new JajukButton(ActionManager.getAction(NOVELTIES));
		jbNorm = new JajukButton(ActionManager.getAction(FINISH_ALBUM));
        popupDDJ = new JPopupMenu();
        ddbDDJ = new DropDownButton(Util.getIcon(ICON_DIGITAL_DJ)) {
        	@Override
        	protected JPopupMenu getPopupMenu() {
        		return popupDDJ;
        	}
        };
        ddbDDJ.setAction(ActionManager.getAction(JajukAction.DJ));
        populateDJs();
        ddbDDJ.setText("");//no text visible
        jtbSpecial.add(jbGlobalRandom);
		jtbSpecial.add(jbBestof);
		jtbSpecial.add(jbNovelties);
		jtbSpecial.add(jbNorm);
        ddbDDJ.addToToolBar(jtbSpecial);

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

		//Volume
		JPanel jpVolume = new JPanel();
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
		JPanel jpPosition = new JPanel();
        jpPosition.setLayout(new BoxLayout(jpPosition,BoxLayout.X_AXIS));
    	jlPosition = new JLabel(Util.getIcon(ICON_POSITION));
		jsPosition = new JSlider(0,100,0);
		jpPosition.add(jlPosition);
		jpPosition.add(jsPosition);
		jsPosition.addChangeListener(CommandJPanel.this);
		jsPosition.setEnabled(false);
		jsPosition.setToolTipText(Messages.getString("CommandJPanel.15")); //$NON-NLS-1$

		//mute
        jbMute = new JajukToggleButton(ActionManager.getAction(MUTE_STATE));

		//dimensions
		int height1 = 25;  //buttons, components
		int iXSeparator = 10;
		//set default layout and size
		double[][] size ={{iXSeparator/2,0.14,iXSeparator, //search box
			TableLayout.FILL,iXSeparator,// history 
			TableLayout.PREFERRED,iXSeparator, //mode buttons
			TableLayout.PREFERRED,iXSeparator, //special functions buttons
			TableLayout.PREFERRED,iXSeparator, //play buttons
			0.2,iXSeparator/2, //position
			0.2,iXSeparator, //volume
			20,iXSeparator},   //mute button
			{height1}}; //note we can't set a % for history combo box because of popup size
		setLayout(new TableLayout(size));
		setAlignmentY(Component.CENTER_ALIGNMENT);

		//add toolbars to main panel
		add(sbSearch,"1,0"); //$NON-NLS-1$
		add(jcbHistory,"3,0"); //$NON-NLS-1$
		add(Util.getCentredPanel(Util.getCentredPanel(jpMode),BoxLayout.Y_AXIS),"5,0");  //$NON-NLS-1$
		add(Util.getCentredPanel(Util.getCentredPanel(jtbSpecial),BoxLayout.Y_AXIS),"7,0"); //$NON-NLS-1$
		add(Util.getCentredPanel(jpPlay),"9,0"); //$NON-NLS-1$
		add(jpPosition,"11,0"); //$NON-NLS-1$
		add(jpVolume,"13,0"); //$NON-NLS-1$
		add(jbMute,"15,0"); //$NON-NLS-1$

		//register to player events
		ObservationManager.register(EVENT_PLAYER_PLAY,CommandJPanel.this);
		ObservationManager.register(EVENT_PLAYER_STOP,CommandJPanel.this);
		ObservationManager.register(EVENT_PLAYER_PAUSE,CommandJPanel.this);
		ObservationManager.register(EVENT_PLAYER_RESUME,CommandJPanel.this);
		ObservationManager.register(EVENT_PLAY_ERROR,CommandJPanel.this);
		ObservationManager.register(EVENT_SPECIAL_MODE,CommandJPanel.this);
		ObservationManager.register(EVENT_ZERO,CommandJPanel.this);
		ObservationManager.register(EVENT_MUTE_STATE,CommandJPanel.this);
		ObservationManager.register(EVENT_REPEAT_MODE_STATUS_CHANGED,CommandJPanel.this);
		ObservationManager.register(EVENT_FILE_LAUNCHED,this);
        ObservationManager.register(EVENT_CLEAR_HISTORY,this);
        ObservationManager.register(EVENT_VOLUME_CHANGED,this);
        ObservationManager.register(EVENT_DJ_CHANGE,this);
        
        //if a track is playing, display right state
		if ( FIFO.getInstance().getCurrentFile() != null){
			//update initial state
			update(new Event(EVENT_PLAYER_PLAY,ObservationManager.getDetailsLastOccurence(EVENT_PLAYER_PLAY)));
			//check if some track has been lauched before the view has been displayed
			update(new Event(EVENT_HEART_BEAT));
		}
		//start timer
		timer.start();
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
			            Messages.showErrorMessage("120",file == null ? "null" : file.getDirectory().getDevice().getName()); //$NON-NLS-1$ //$NON-NLS-2$
			            jcbHistory.setSelectedItem(null);
			        }
			    }
			}
		}
		catch(Exception e){
			Log.error(e);
		}
		finally{
			ObservationManager.notify(new Event(EVENT_PLAYLIST_REFRESH)); //refresh playlist editor
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
        else if (e.getSource() == jsPosition && !jsPosition.getValueIsAdjusting()){
			if (jsPosition.getValueIsAdjusting()){
                lDateLastAdjust = System.currentTimeMillis();
            }
            else{
                setPosition((float)jsPosition.getValue()/100);
            }

		}
	}

	private void setPosition(float fPosition){
        Log.debug("Seeking to: "+fPosition); //$NON-NLS-1$
        //max position can't be 100% to allow seek properly
        if (fPosition < 0.0f){
            fPosition = 0.0f;
        }
        if (fPosition == 1.0f){
            fPosition = 0.99f;
        }
        final float f = fPosition;
        new Thread(){
            public void run(){
                Player.seek(f);        
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


	public void setVolume(final float fVolume){
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
				String subject = event.getSubject();
				if(EVENT_PLAYER_STOP.equals(subject) || EVENT_ZERO.equals(subject)){
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
				else if (EVENT_PLAYER_PLAY.equals(subject)){
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
				else if (EVENT_PLAYER_PAUSE.equals(subject)){
                    ActionManager.getAction(REWIND_TRACK).setEnabled(false);
                    ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
                    ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(Util.getIcon(ICON_PLAY));
					jsPosition.setEnabled(false);
					jsPosition.removeMouseWheelListener(CommandJPanel.this);
                    jsPosition.removeChangeListener(CommandJPanel.this);
            	}
				else if (EVENT_PLAYER_RESUME.equals(subject)){
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
				else if (EVENT_HEART_BEAT.equals(subject) &&!FIFO.isStopped() && !Player.isPaused()){
					 //if position is adjusting, no dont disturb user
                    if (jsPosition.getValueIsAdjusting() || Player.isSeeking()){
                        return;
                    }
                    //make sure not to set to old position
                    if ((System.currentTimeMillis() - lDateLastAdjust) < 4000){
                        return;
                    }
                    int iPos = (int)(100*JajukTimer.getInstance().getCurrentTrackPosition());
                    jsPosition.removeChangeListener(CommandJPanel.this);
                    jsPosition.setValue(iPos);
                    jsPosition.addChangeListener(CommandJPanel.this);
				}
				else if(EVENT_SPECIAL_MODE.equals(subject)){
				    if (ObservationManager.getDetail(event,DETAIL_ORIGIN).equals(DETAIL_SPECIAL_MODE_NORMAL)){
						// deselect shuffle mode
						ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, FALSE);
                        JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(false);
						CommandJPanel.getInstance().jbRandom.setSelected(false);
						//computes planned tracks
						FIFO.getInstance().computesPlanned(true);
					}
				}
				else if(EVENT_REPEAT_MODE_STATUS_CHANGED.equals(subject)){
					if (ObservationManager.getDetail(event,DETAIL_SELECTION).equals(FALSE)){
						//    deselect repeat mode
						ConfigurationManager.setProperty(CONF_STATE_REPEAT, FALSE);
						JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(false);
						CommandJPanel.getInstance().jbRepeat.setSelected(false);
					}
				}
                else if (EVENT_FILE_LAUNCHED.equals(subject)){
                    //Remove history listener, otherwise u get a recursive event generation
                    jcbHistory.removeActionListener(CommandJPanel.this);
                    if (jcbHistory.getItemCount() > 0){
                        jcbHistory.setSelectedIndex(0);
                    }
                    jcbHistory.addActionListener(CommandJPanel.this);
                }
                else if(EVENT_CLEAR_HISTORY.equals(event.getSubject())){
                  jcbHistory.setSelectedItem(null); //clear selection bar (data itself is clear from the model by History class)
                }
                else if(EVENT_VOLUME_CHANGED.equals(event.getSubject())){
                    jsVolume.removeChangeListener(CommandJPanel.this);
                    jsVolume.setValue((int)(100*Player.getCurrentVolume()));
                    jsVolume.addChangeListener(CommandJPanel.this);
                    jbMute.setSelected(false);
                }
                else if(EVENT_DJ_CHANGE.equals(event.getSubject())){
                    populateDJs();
                }
			}
		});

	}
    
    /**
     * Populate DJs
     *
     */
    private void populateDJs(){
        try{
            popupDDJ.removeAll();
            Iterator it = DigitalDJManager.getInstance().getDJs().iterator();
            while (it.hasNext()){
                final DigitalDJ dj = (DigitalDJ)it.next();
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(dj.getName(),Util.getIcon(ICON_DIGITAL_DJ));
                jmi.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent arg0) {
                        ConfigurationManager.setProperty(CONF_DEFAULT_DJ,dj.getID());
                        ObservationManager.notify(new Event(EVENT_DJ_CHANGE));
                    }
                });
                popupDDJ.add(jmi);
                jmi.setSelected(ConfigurationManager.getProperty(CONF_DEFAULT_DJ).equals(dj.getID()));
            }
            popupDDJ.addSeparator();
            JMenuItem jmiNew = new JMenuItem(Messages.getString("CommandJPanel.17"),Util.getIcon(ICON_WIZARD)); 
            jmiNew.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    new DigitalDJWizard();
                }
            });
            JMenuItem jmiAmbiences = new JMenuItem(Messages.getString("CommandJPanel.19"),Util.getIcon(ICON_STYLE)); 
            jmiAmbiences.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    new AmbienceWizard();
                }
            });
            popupDDJ.add(jmiNew);
            popupDDJ.add(jmiAmbiences);
            //Set new tooltip for DJ button
            DigitalDJ dj = DigitalDJManager.getInstance().getDJByID(ConfigurationManager.getProperty(CONF_DEFAULT_DJ));
            if (dj != null){
            	String sDJ = dj.getName();
            	ddbDDJ.setToolTipText("<html>"+Messages.getString("CommandJPanel.18")+"<p><b>"+sDJ+"</b></p></html>"); //$NON-NLS-1$
            }
            else{
            	ddbDDJ.setToolTipText(Messages.getString("CommandJPanel.18")); //$NON-NLS-1$
            }
        }
        catch(Exception e){
            Log.error(e);
        }
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
