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

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.HistoryItem;
import org.jajuk.base.JajukTimer;
import org.jajuk.base.Player;
import org.jajuk.base.SearchResult;
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import com.sun.SwingWorker;

/**
 *  Command panel ( static view )
 *
 * @author     Bertrand Florat
 * @created    3 oct. 2003
 */
public class CommandJPanel extends JPanel implements ITechnicalStrings,ActionListener,ListSelectionListener,ChangeListener,Observer,MouseListener,MouseWheelListener{
    
    //singleton
    static private CommandJPanel command;
    
    //widgets declaration
    SearchBox sbSearch;
    SteppedComboBox jcbHistory;
    JPanel jpMode;
    JButton jbRepeat;
    JButton jbRandom;
    JButton jbContinue;
    JButton jbIntro;
    JPanel jpSpecial;
    JButton jbGlobalRandom;
    JButton jbBestof;
    JButton jbNovelties;
    JButton jbNorm;
    JPanel jpPlay;
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
    JButton jbMute;
    
    
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
    /**Position slider move*/
    private static boolean bPositionChanging = false;
    /**Last slider manual move date*/
    private static long lDateLastPosMove;
    /**Lock to avoid multiple next/previous*/
    private static byte[] bLock = new byte[0];
    /** Swing Timer to refresh the component*/ 
    private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT,new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            update(EVENT_HEART_BEAT);
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
        jcbHistory = new SteppedComboBox(History.getInstance().getHistory().toArray());
        int iWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2);
        jcbHistory.setPopupWidth(iWidth);
        jcbHistory.setToolTipText(Messages.getString("CommandJPanel.0")); //$NON-NLS-1$
        jcbHistory.addActionListener(CommandJPanel.this);
        
        //Mode toolbar
        jpMode = new JPanel();
        jpMode.setLayout(new BoxLayout(jpMode,BoxLayout.X_AXIS));
        jbRepeat = new JButton(Util.getIcon(ICON_REPEAT)); 
        jbRepeat.setActionCommand(EVENT_REPEAT_MODE_STATUS_CHANGED);
        jbRepeat.setToolTipText(Messages.getString("CommandJPanel.1")); //$NON-NLS-1$
        jbRepeat.addActionListener(JajukListener.getInstance());
        if ( ConfigurationManager.getBoolean(CONF_STATE_REPEAT)){
            jbRepeat.setBorder(BorderFactory.createLoweredBevelBorder());
        }
        else{
            jbRepeat.setBorder(BorderFactory.createRaisedBevelBorder());
        }
        jbRandom = new JButton(Util.getIcon(ICON_SHUFFLE));
        jbRandom.setToolTipText(Messages.getString("CommandJPanel.2")); //$NON-NLS-1$
        jbRandom.setActionCommand(EVENT_SHUFFLE_MODE_STATUS_CHANGED);
        jbRandom.addActionListener(JajukListener.getInstance());
        if ( ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE)){
            jbRandom.setBorder(BorderFactory.createLoweredBevelBorder());
        }
        else{
            jbRandom.setBorder(BorderFactory.createRaisedBevelBorder());
        }
        jbContinue = new JButton(Util.getIcon(ICON_CONTINUE));
        jbContinue.setToolTipText(Messages.getString("CommandJPanel.3")); //$NON-NLS-1$
        jbContinue.setActionCommand(EVENT_CONTINUE_MODE_STATUS_CHANGED);
        jbContinue.addActionListener(JajukListener.getInstance());
        if ( ConfigurationManager.getBoolean(CONF_STATE_CONTINUE)){
            jbContinue.setBorder(BorderFactory.createLoweredBevelBorder());
        }
        else{
            jbContinue.setBorder(BorderFactory.createRaisedBevelBorder());
        }
        jbIntro = new JButton(Util.getIcon(ICON_INTRO));
        jbIntro.setToolTipText(Messages.getString("CommandJPanel.4")); //$NON-NLS-1$
        jbIntro.setActionCommand(EVENT_INTRO_MODE_STATUS_CHANGED);
        jbIntro.addActionListener(JajukListener.getInstance());
        if ( ConfigurationManager.getBoolean(CONF_STATE_INTRO)){
            jbIntro.setBorder(BorderFactory.createLoweredBevelBorder());
        }
        else{
            jbIntro.setBorder(BorderFactory.createRaisedBevelBorder());
        }
        jpMode.add(jbRepeat);
        jpMode.add(jbRandom);
        jpMode.add(jbContinue);
        jpMode.add(jbIntro);
        
        //Special functions toolbar
        jpSpecial = new JPanel();
        jpSpecial.setLayout(new BoxLayout(jpSpecial,BoxLayout.X_AXIS));
        jbGlobalRandom = new JButton(Util.getIcon(ICON_SHUFFLE_GLOBAL));
        jbGlobalRandom.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbGlobalRandom.addActionListener(CommandJPanel.this);
        jbGlobalRandom.setToolTipText(Messages.getString("CommandJPanel.5")); //$NON-NLS-1$
        jbBestof = new JButton(Util.getIcon(ICON_BESTOF)); 
        jbBestof.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbBestof.addActionListener(CommandJPanel.this);
        jbBestof.setToolTipText(Messages.getString("CommandJPanel.6")); //$NON-NLS-1$
        jbNovelties = new JButton(Util.getIcon(ICON_NOVELTIES)); 
        jbNovelties.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbNovelties.addActionListener(CommandJPanel.this);
        jbNovelties.setToolTipText(Messages.getString("CommandJPanel.16")); //$NON-NLS-1$
        jbNorm = new JButton(Util.getIcon(ICON_MODE_NORMAL)); 
        jbNorm.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbNorm.addActionListener(CommandJPanel.this);
        jbNorm.setToolTipText(Messages.getString("CommandJPanel.17")); //$NON-NLS-1$
        jpSpecial.add(jbGlobalRandom);
        jpSpecial.add(jbBestof);
        jpSpecial.add(jbNovelties);
        jpSpecial.add(jbNorm);
        
        //Play toolbar
        jpPlay = new JPanel();
        jpPlay.setLayout(new BoxLayout(jpPlay,BoxLayout.X_AXIS));
        jbPrevious = new JButton(Util.getIcon(ICON_PREVIOUS)); 
        jbPrevious.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbPrevious.setToolTipText(Messages.getString("CommandJPanel.8")); //$NON-NLS-1$
        jbPrevious.addMouseListener(CommandJPanel.this);
        jbNext = new JButton(Util.getIcon(ICON_NEXT)); 
        jbNext.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbNext.setToolTipText(Messages.getString("CommandJPanel.9")); //$NON-NLS-1$
        jbNext.addMouseListener(CommandJPanel.this);
        jbRew = new JButton(Util.getIcon(ICON_REW)); 
        jbRew.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbRew.setEnabled(false);
        jbRew.setToolTipText(Messages.getString("CommandJPanel.10")); //$NON-NLS-1$
        jbRew.addActionListener(CommandJPanel.this);
        jbPlayPause = new JButton(Util.getIcon(ICON_PAUSE)); 
        jbPlayPause.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbPlayPause.setToolTipText(Messages.getString("CommandJPanel.11")); //$NON-NLS-1$
        jbPlayPause.setEnabled(false);
        jbPlayPause.addActionListener(CommandJPanel.this);
        jbStop = new JButton(Util.getIcon(ICON_STOP)); 
        jbStop.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbStop.setToolTipText(Messages.getString("CommandJPanel.12")); //$NON-NLS-1$
        jbStop.addActionListener(CommandJPanel.this);
        jbStop.setEnabled(false);
        jbFwd = new JButton(Util.getIcon(ICON_FWD)); 
        jbFwd.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        jbFwd.setToolTipText(Messages.getString("CommandJPanel.13")); //$NON-NLS-1$
        jbFwd.setEnabled(false);
        jbFwd.addActionListener(CommandJPanel.this);
        jpPlay.add(jbPrevious);
        jpPlay.add(jbNext);
        jpPlay.add(Box.createHorizontalGlue());
        jpPlay.add(jbRew);
        jpPlay.add(jbPlayPause);
        jpPlay.add(jbStop);
        jpPlay.add(jbFwd);
        
        //Volume
        JPanel jpVolume = new JPanel();
        jpVolume.setLayout(new BoxLayout(jpVolume,BoxLayout.X_AXIS));
        jlVolume = new JLabel(Util.getIcon(ICON_VOLUME)); 
        jsVolume = new JSlider(0,100,(int)(100*ConfigurationManager.getFloat(CONF_VOLUME)));
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
        jsPosition.addMouseWheelListener(CommandJPanel.this);
        
        //mute
        jbMute = new JButton(Util.getIcon(ICON_MUTE)); 
        jbMute.addActionListener(CommandJPanel.this);
        jbMute.setToolTipText(Messages.getString("CommandJPanel.7")); //$NON-NLS-1$
        jbMute.setBorder(BorderFactory.createRaisedBevelBorder());
        
        
        //dimensions
        int height1 = 25;  //buttons, components
        //int height2 = 36; //slider ( at least this height in the gtk+ l&f ) 
        int iSeparator = 1;
        //set default layout and size
        double[][] size ={{5*iSeparator,0.15,10*iSeparator,0.17,5*iSeparator,  //search box + history
            0.11,5*iSeparator, //mode buttons
            0.13,10*iSeparator, //special functions buttons
            0.20,10*iSeparator, //play buttons
            0.13,iSeparator,0.11,TableLayout.FILL,20,5*iSeparator},  //position + volume sliders + mute button
            {height1}}; //note we can't set a % for history combo box because of popup size
        setLayout(new TableLayout(size));
        TableLayout tl = new TableLayout();
        setAlignmentY(Component.CENTER_ALIGNMENT);
        
        //add toolbars to main panel
        add(sbSearch,"1,0"); //$NON-NLS-1$
        add(jcbHistory,"3,0"); //$NON-NLS-1$
        add(Util.getCentredPanel(Util.getCentredPanel(jpMode),BoxLayout.Y_AXIS),"5,0");  //$NON-NLS-1$
        add(Util.getCentredPanel(Util.getCentredPanel(jpSpecial),BoxLayout.Y_AXIS),"7,0"); //$NON-NLS-1$
        add(Util.getCentredPanel(jpPlay),"9,0"); //$NON-NLS-1$
        add(jsPosition,"11,0"); //$NON-NLS-1$
        add(jsVolume,"13,0"); //$NON-NLS-1$
        add(jbMute,"15,0"); //$NON-NLS-1$
        
        //register to player events
        ObservationManager.register(EVENT_PLAYER_PLAY,CommandJPanel.this);
        ObservationManager.register(EVENT_PLAYER_STOP,CommandJPanel.this);
        ObservationManager.register(EVENT_PLAYER_PAUSE,CommandJPanel.this);
        ObservationManager.register(EVENT_PLAYER_RESUME,CommandJPanel.this);
        ObservationManager.register(EVENT_ADD_HISTORY_ITEM,CommandJPanel.this);
        ObservationManager.register(EVENT_PLAY_ERROR,CommandJPanel.this);
        ObservationManager.register(EVENT_SPECIAL_MODE,CommandJPanel.this);
        ObservationManager.register(EVENT_ZERO,CommandJPanel.this);
        ObservationManager.register(EVENT_MUTE_STATE,CommandJPanel.this);
        ObservationManager.register(EVENT_REPEAT_MODE_STATUS_CHANGED,CommandJPanel.this);
        
        //if a track is playing, display right state
        if ( FIFO.getInstance().getCurrentFile() != null){
            //update initial state 
            update(EVENT_PLAYER_PLAY);
            //check if some track has been lauched before the view has been displayed
            update(EVENT_HEART_BEAT);
        }
        
        //start timer
        timer.start();
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
        new Thread() {
            public void run() {
                try{
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
                                        try{
                                            FIFO.getInstance().push(new StackItem(file,ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
                                        }
                                        catch(JajukException je){  //can be thrown if file is null
                                            return;
                                        }
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
                            FIFO.getInstance().push(Util.createStackItems(alToPlay,
                                    ConfigurationManager.getBoolean(CONF_STATE_REPEAT),false),false);
                        }
                        Properties properties = new Properties();
                        properties.put(DETAIL_ORIGIN,DETAIL_SPECIAL_MODE_BESTOF);
                        ObservationManager.notify(EVENT_SPECIAL_MODE,properties);
                    }
                    if (ae.getSource() == jbGlobalRandom ){
                        ArrayList alToPlay = FileManager.getGlobalShufflePlaylist();
                        if ( alToPlay.size() > 0){
                            FIFO.getInstance().push(Util.createStackItems(alToPlay,
                                    ConfigurationManager.getBoolean(CONF_STATE_REPEAT),false),false);
                        }
                        Properties properties = new Properties();
                        properties.put(DETAIL_ORIGIN,DETAIL_SPECIAL_MODE_SHUFFLE);
                        ObservationManager.notify(EVENT_SPECIAL_MODE,properties);
                    }
                    if (ae.getSource() == jbNovelties ){
                        ArrayList alToPlay  = FileManager.getGlobalNoveltiesPlaylist();
                        if ( alToPlay.size() > 0){
                            FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alToPlay),
                                    ConfigurationManager.getBoolean(CONF_STATE_REPEAT),false),false);
                            Properties properties = new Properties();
                            properties.put(DETAIL_ORIGIN,DETAIL_SPECIAL_MODE_NOVELTIES);
                            ObservationManager.notify(EVENT_SPECIAL_MODE,properties);
                        }
                        else{ //none novelty found
                            Messages.showErrorMessage("127"); //$NON-NLS-1$
                        }
                    }
                    if (ae.getSource() == jbNorm ){
                        StackItem item = FIFO.getInstance().getCurrentItem();//stores current item
                        FIFO.getInstance().clear(); //clear fifo 
                        FIFO.getInstance().push(item,true); //then re-add current item
                        FIFO.getInstance().computesPlanned(true); //update planned list
                        Properties properties = new Properties();
                        properties.put(DETAIL_ORIGIN,DETAIL_SPECIAL_MODE_NORMAL);
                        ObservationManager.notify(EVENT_SPECIAL_MODE,properties);
                    }
                    else if (ae.getSource() == jbMute ){
                        Player.mute(); //change mute state
                    }
                    else if(ae.getSource() == jbStop){
                        FIFO.getInstance().stopRequest();
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
                    else if (ae.getSource() == jbRew){
                        float fCurrentPosition = Player.getCurrentPosition();
                        Player.seek(fCurrentPosition-JUMP_SIZE);
                    }
                    else if (ae.getSource() == jbFwd){
                        float fCurrentPosition = Player.getCurrentPosition();
                        Player.seek(fCurrentPosition+JUMP_SIZE);
                    }
                }
                catch(Exception e){
                    Log.error(e);
                }
                finally{
                    ObservationManager.notify(EVENT_PLAYLIST_REFRESH); //refresh playlist editor
                }
            }
        }.start();
        
        
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
        if ( e.getSource() == jsVolume ){
            Player.setVolume((float)jsVolume.getValue()/100);
            //if user move the volume slider, unmute
            Player.mute(false);
            jbMute.setBorder(BorderFactory.createRaisedBevelBorder());
        }
        else if (e.getSource() == jsPosition && !bPositionChanging && !jsPosition.getValueIsAdjusting()){
            bPositionChanging = true;
            lDateLastPosMove = System.currentTimeMillis();
            float fPosition = (float)jsPosition.getValue()/100;
            Log.debug("Seeking to: "+fPosition); //$NON-NLS-1$
            //max position can't be 100% to allow seek properly
            if (fPosition == 1.0f){
                fPosition = 0.99f;
            }
            Player.seek(fPosition);
            Player.mute(false); //if user move the slider, unmute
            jbMute.setBorder(BorderFactory.createRaisedBevelBorder());
            bPositionChanging = false;
        }
    }
    
    /**
     * Set Slider position
     * @param i percentage of slider
     */
    public void setCurrentPosition(final int i){
        if ( !bPositionChanging ){//don't move slider when user do it himself at the same time
            bPositionChanging = true;  //block events so player is not affected
            //wait 3 secs after end of last move to avoid slider to come back to previous position
            if (System.currentTimeMillis() - lDateLastPosMove > 3000){
                CommandJPanel.this.jsPosition.setValue(i);    
            }
            bPositionChanging = false;
        }
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
    
    
    /**
     * Set Volume
     * @param volume
     */
    public void setCurrentVolume(int iValue){
        this.jsVolume.setValue(iValue);
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(final String subject) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if( EVENT_PLAYER_STOP.equals(subject) || EVENT_ZERO.equals(subject)){
                    jbRew.setEnabled(false);
                    jbPlayPause.setEnabled(false);
                    jbStop.setEnabled(false);
                    jbFwd.setEnabled(false);
                    jbNext.setEnabled(false);
                    jbPrevious.setEnabled(false);
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
                    jbNext.setEnabled(true);
                    jbPrevious.setEnabled(true);
                    jsPosition.setEnabled(true);
                    jbPlayPause.setIcon(Util.getIcon(ICON_PAUSE)); //resume any current pause
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
                    long length = JajukTimer.getInstance().getCurrentTrackTotalTime(); 
                    long lTime = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
                    int iPos = (length!=0)?(int)(100*lTime/length):0;  //if length=0, pos is always 0 to avoid division by zero
                    setCurrentPosition(iPos);
                }
                else if (EVENT_MUTE_STATE.equals(subject)){
                    if ( Player.isMuted()){
                        jbMute.setBorder(BorderFactory.createLoweredBevelBorder());
                    }
                    else{
                        jbMute.setBorder(BorderFactory.createRaisedBevelBorder());
                    }
                }      
                else if (EVENT_ADD_HISTORY_ITEM.equals(subject)){
                    HistoryItem hi = (HistoryItem)ObservationManager.getDetail(EVENT_ADD_HISTORY_ITEM,DETAIL_HISTORY_ITEM);
                    if (hi != null ){
                        addHistoryItem(hi);
                    }
                }
                else if (EVENT_PLAY_ERROR.equals(subject)){
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (jcbHistory.getItemCount() > 0){
                                jcbHistory.removeActionListener(CommandJPanel.this); //stop listening this item when manupulating it
                                jcbHistory.removeItemAt(0);
                                //select first row
                                if (jcbHistory.getItemCount()>0){
                                    jcbHistory.setSelectedIndex(0);
                                }
                                jcbHistory.addActionListener(CommandJPanel.this);
                            }
                        }
                    });
                }
                else if(EVENT_SPECIAL_MODE.equals(subject)){
                    if (ObservationManager.getDetail(EVENT_SPECIAL_MODE,DETAIL_ORIGIN).equals(DETAIL_SPECIAL_MODE_NORMAL)){
                        // deselect shuffle mode
                        ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, FALSE);
                        JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(false);
                        CommandJPanel.getInstance().jbRandom.setBorder(BorderFactory.createRaisedBevelBorder());
                        //computes planned tracks
                        FIFO.getInstance().computesPlanned(true);
                    }
                }
                else if(EVENT_REPEAT_MODE_STATUS_CHANGED.equals(subject)){
                    if (ObservationManager.getDetail(EVENT_REPEAT_MODE_STATUS_CHANGED,DETAIL_SELECTION).equals(FALSE)){
                        //    deselect repeat mode
                        ConfigurationManager.setProperty(CONF_STATE_REPEAT, FALSE);
                        JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(false);
                        CommandJPanel.getInstance().jbRepeat.setBorder(BorderFactory.createRaisedBevelBorder());    
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
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {
        //left button and no shift pressed :track level
        if (e.getButton() == MouseEvent.BUTTON1 && ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) != MouseEvent.SHIFT_DOWN_MASK)){
            if (e.getSource() == jbPrevious){
                synchronized(bLock){
                    new Thread(){
                        public void run(){
                            try{
                                FIFO.getInstance().playPrevious();
                            }
                            catch(Exception e){
                                Log.error(e);
                            }
                        }
                    }.start();
                    if ( Player.isPaused()){  //player was paused, reset pause button when changing of track
                        Player.setPaused(false);
                        ObservationManager.notify(EVENT_PLAYER_RESUME);  //notify of this event
                    }
                }
            }
            else if (e.getSource() == jbNext){
                synchronized(bLock){
                    new Thread(){
                        public void run(){
                            try{
                                FIFO.getInstance().playNext();
                            }
                            catch(Exception e){
                                Log.error(e);
                            }
                        }
                    }.start();
                    if ( Player.isPaused()){  //player was paused, reset pause button
                        Player.setPaused(false);
                        ObservationManager.notify(EVENT_PLAYER_RESUME);  //notify of this event
                    }
                }
            }
        }
        //right click or shift+left click
        else if (e.getButton() == MouseEvent.BUTTON3 || ((e.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK)){
            if (e.getSource() == jbNext){
                synchronized(bLock){
                    new Thread(){
                        public void run(){
                            try{
                                FIFO.getInstance().playNextAlbum();
                            }
                            catch(Exception e){
                                Log.error(e);
                            }
                        }
                    }.start();
                }
            }
            else if (e.getSource() == jbPrevious){
                synchronized(bLock){
                    new Thread(){
                        public void run(){
                            try{
                                FIFO.getInstance().playPreviousAlbum();
                            }
                            catch(Exception e){
                                Log.error(e);
                            }
                        }
                    }.start();
                    if ( Player.isPaused()){  //player was paused, reset pause button when changing of track
                        Player.setPaused(false);
                        ObservationManager.notify(EVENT_PLAYER_RESUME);  //notify of this event
                    }
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getSource() == jsPosition){
            int iOld = jsPosition.getValue();
            int iNew = iOld - (e.getUnitsToScroll()*3);
            if ( iNew<0){
                iNew = 0;
            }
            else if (iNew>99){
                iNew = 99;
            }
            jsPosition.setValue(iNew);
        }
        else if (e.getSource() == jsVolume){
            int iOld = jsVolume.getValue();
            int iNew = iOld - (e.getUnitsToScroll()*3);
            if ( iNew<0){
                iNew = 0;
            }
            else if (iNew>99){
                iNew = 99;
            }
            jsVolume.setValue(iNew);
        }
    }
    
}
