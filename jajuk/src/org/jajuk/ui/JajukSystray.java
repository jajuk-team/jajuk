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
 *  $Revision$
 */

package org.jajuk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.JajukTimer;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import ext.SliderMenuItem;

/**
 *  Jajuk systray
 *
 * @author     Administrateur
 * @created    22 sept. 2004
 */
public class JajukSystray implements ITechnicalStrings,Observer,ActionListener,MouseWheelListener,ChangeListener{
    //Systray variables
    SystemTray stray = SystemTray.getDefaultSystemTray();;
    TrayIcon trayIcon;
    JPopupMenu jmenu;
    JMenuItem jmiExit;
    JMenuItem jmiMute;
    JMenuItem jmiAbout;
    JMenuItem jmiShuffle;
    JMenuItem jmiBestof;
    JMenuItem jmiDJ;
    JMenuItem jmiNovelties;
    JMenuItem jmiNorm;
    JMenuItem jmiPause;
    JMenuItem jmiStop;
    JMenuItem jmiPrevious;
    JMenuItem jmiNext;
    JMenuItem jmiOut;
    JLabel jlVolume;
    JSlider jsVolume;
    JLabel jlPosition;
    JSlider jsPosition;
    long lDateLastAdjust;
    /**Visible at startup?*/
    JCheckBoxMenuItem jcbmiVisible;
    /**Self instance singleton*/
    private static JajukSystray jsystray;
    
    /** Swing Timer to refresh the component*/ 
    private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT,new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            update(new Event(EVENT_HEART_BEAT));
        }
    });
    
    
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
     * Reset the systray (useful for language reload)
     *
     */
    public static void dispose(){
        if (jsystray != null){
            jsystray.closeSystray();
            jsystray = null;
        }
    }
    
    /**
     * Systray constructor
     *
     */
    public JajukSystray(){
        
        jmenu = new JPopupMenu(Messages.getString("JajukWindow.3")); //$NON-NLS-1$

        jmiExit = new JMenuItem(ActionManager.getAction(JajukAction.EXIT));
        jmiMute =  new JMenuItem(ActionManager.getAction(JajukAction.MUTE_STATE));
        jmiAbout = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_ABOUT));
        jmiShuffle =  new JMenuItem(ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL));
        jmiBestof =  new JMenuItem(ActionManager.getAction(JajukAction.BEST_OF));
        jmiDJ =  new JMenuItem(ActionManager.getAction(JajukAction.DJ));
        jmiNorm =  new JMenuItem(ActionManager.getAction(JajukAction.FINISH_ALBUM));
        jmiNovelties =  new JMenuItem(ActionManager.getAction(JajukAction.NOVELTIES));

        jcbmiVisible =  new JCheckBoxMenuItem(Messages.getString("JajukWindow.8")); //$NON-NLS-1$
        jcbmiVisible.setState(JajukWindow.getInstance().isVisible()); 
        jcbmiVisible.addActionListener(this);
        jcbmiVisible.setToolTipText(Messages.getString("JajukWindow.25")); //$NON-NLS-1$
        
        jmiPause = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_PAUSE_TRACK));
        jmiStop = new JMenuItem(ActionManager.getAction(JajukAction.STOP_TRACK));
        jmiPrevious = new JMenuItem(ActionManager.getAction(JajukAction.PREVIOUS_TRACK));
        jmiNext = new JMenuItem(ActionManager.getAction(JajukAction.NEXT_TRACK));

        jlPosition = new JLabel(Util.getIcon(ICON_POSITION)); 
        String sTitle = Messages.getString("JajukWindow.34"); //$NON-NLS-1$
        jsPosition = new SliderMenuItem(0,100,0,sTitle);
        jsPosition.setEnabled(false);
        jsPosition.setToolTipText(Messages.getString("CommandJPanel.15")); //$NON-NLS-1$
        
        /**Important: due to a bug probably in swing or jdic, we have to add a jmenuitem in the popup menu 
         * and not the panel itself, otherwise no action event occurs*/
        
        jlVolume = new JLabel(Util.getIcon(ICON_VOLUME)); 
        sTitle = Messages.getString("JajukWindow.33"); //$NON-NLS-1$
        int iVolume = (int)(100*ConfigurationManager.getFloat(CONF_VOLUME));
        if (iVolume > 100){ //can occur in some undefined cases
            iVolume = 100;
        }
        jsVolume = new SliderMenuItem(0,100,iVolume,sTitle);
        jsVolume.setToolTipText(sTitle); //$NON-NLS-1$
        jsVolume.addMouseWheelListener(this);
        jsVolume.addChangeListener(this);
        
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
        jmenu.add(jmiDJ);
        jmenu.add(jmiNovelties);
        jmenu.add(jmiNorm);
        jmenu.addSeparator();
        jmenu.add(jmiAbout);
        jmenu.addSeparator();
        jmenu.add(jmiMute);
        jmenu.addSeparator();
        jmenu.add(jsPosition);
        jmenu.add(jsVolume);
        jmenu.addSeparator();
        jmenu.add(jmiExit);
        jmenu.add(jmiOut);
        
        trayIcon = new TrayIcon(Util.getIcon(ICON_LOGO_TRAY),Messages.getString("JajukWindow.18"),jmenu); //$NON-NLS-1$);
        trayIcon.setIconAutoSize(true);
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                //hide menu if opened
                if (jmenu != null && jmenu.isVisible()){
                    jmenu.setVisible(false);
                }
                //show window if it is not visible and hide it if it is visible
                if (!JajukWindow.getInstance().isVisible()){
                    JajukWindow.getInstance().setShown(true);
                }
                else{
                    JajukWindow.getInstance().setShown(false);
                }
            }
        });
        stray.addTrayIcon(trayIcon);
        //start timer
        timer.start();
        //Register needed events
        ObservationManager.register(EVENT_ZERO,this);
        ObservationManager.register(EVENT_FILE_LAUNCHED,this);
        ObservationManager.register(EVENT_PLAYER_PAUSE,this);
        ObservationManager.register(EVENT_PLAYER_PLAY,this);
        ObservationManager.register(EVENT_PLAYER_RESUME,this);
        ObservationManager.register(EVENT_PLAYER_STOP,this);
        ObservationManager.register(EVENT_MUTE_STATE,this);
        ObservationManager.register(EVENT_VOLUME_CHANGED,this);
        
        //check if a file has been already started
        if (FIFO.getInstance().getCurrentFile() == null){
            update(new Event(EVENT_PLAYER_STOP,ObservationManager.getDetailsLastOccurence(EVENT_PLAYER_STOP)));
        }
        else{
            update(new Event(EVENT_FILE_LAUNCHED,ObservationManager.getDetailsLastOccurence(EVENT_FILE_LAUNCHED)));    
        }
    }
    
    
    public JPopupMenu getPopup(){
        return jmenu;
    }
    /**
     * ActionListener
     */
    public void actionPerformed(final ActionEvent e) {
        //do not run this in a separate thread because Player actions would die with the thread
        try{
            if (e.getSource() == jcbmiVisible) {
                ConfigurationManager.setProperty(CONF_SHOW_AT_STARTUP, Boolean.toString(
                    jcbmiVisible.getState()));
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
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     * DO NOT set value to sliders directly here to avoid loops, value is only set by the timer
     */
    public void stateChanged(ChangeEvent e) {
        if ( e.getSource() == jsVolume){
            if (System.currentTimeMillis()-lDateLastAdjust > 20){ //this value should be low to make sure we can reach zero
                setVolume((float)jsVolume.getValue()/100);
                lDateLastAdjust = System.currentTimeMillis();
            }
        }
        else if (e.getSource() == jsPosition){
            if (jsPosition.getValueIsAdjusting()){
                lDateLastAdjust = System.currentTimeMillis();
            }
            else{
                setPosition((float)jsPosition.getValue()/100);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(final Event event) {
        if (jsystray == null){ //test if the systray is visible
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String subject = event.getSubject();
                if (EVENT_FILE_LAUNCHED.equals(subject)){
                    //remove and re-add listener to make sure not to add it twice
                    jsPosition.removeMouseWheelListener(JajukSystray.this);
                    jsPosition.addMouseWheelListener(JajukSystray.this);
                    jsPosition.removeChangeListener(JajukSystray.this);
                    jsPosition.addChangeListener(JajukSystray.this);
                    jsPosition.setEnabled(true);
                    String sID = (String)ObservationManager.getDetail(event,DETAIL_CURRENT_FILE_ID);
                    if (sID == null){
                        return;
                    }
                    File file  = (File)FileManager.getInstance().getItem((String)ObservationManager.getDetail(event,DETAIL_CURRENT_FILE_ID));
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
                        if (ConfigurationManager.getBoolean(CONF_OPTIONS_SHOW_POPUP)){
                            trayIcon.displayMessage(Messages.getString("JajukWindow.35"),sOut,TrayIcon.INFO_MESSAGE_TYPE);     //$NON-NLS-1$
                        }
                    }
                    else{
                        sOut = Messages.getString("JajukWindow.18"); //$NON-NLS-1$
                    }
                    trayIcon.setToolTip(sOut);
                }
                else if( EVENT_PLAYER_STOP.equals(subject) || EVENT_ZERO.equals(subject)){
                    trayIcon.setToolTip(Messages.getString("JajukWindow.18")); //$NON-NLS-1$
                    jmiPause.setEnabled(false);
                    jmiStop.setEnabled(false);
                    jmiNext.setEnabled(false);
                    jmiPrevious.setEnabled(false);
                    jsPosition.removeMouseWheelListener(JajukSystray.this);
                    jsPosition.removeChangeListener(JajukSystray.this);
                    jsPosition.setEnabled(false);
                    jsPosition.setValue(0);
                    jmiNorm.setEnabled(false);
                }
                else if ( EVENT_PLAYER_PLAY.equals(subject)){
                    jsPosition.removeMouseWheelListener(JajukSystray.this);
                    jsPosition.addMouseWheelListener(JajukSystray.this);
                    jsPosition.removeChangeListener(JajukSystray.this);
                    jsPosition.addChangeListener(JajukSystray.this);
                    jsPosition.setEnabled(true);
                    jmiPause.setEnabled(true);
                    jmiStop.setEnabled(true);
                    jmiNext.setEnabled(true);
                    jmiPrevious.setEnabled(true);
                    jmiNorm.setEnabled(true);
                }
                else if ( EVENT_PLAYER_PAUSE.equals(subject)){
                    jsPosition.removeMouseWheelListener(JajukSystray.this);
                    jsPosition.removeChangeListener(JajukSystray.this);
                    jsPosition.setEnabled(false);
                }
                else if ( EVENT_PLAYER_RESUME.equals(subject)){
                    jsPosition.removeMouseWheelListener(JajukSystray.this);
                    jsPosition.addMouseWheelListener(JajukSystray.this);
                    jsPosition.removeChangeListener(JajukSystray.this);
                    jsPosition.addChangeListener(JajukSystray.this);
                    jsPosition.setEnabled(true);
                }
                else if(EVENT_VOLUME_CHANGED.equals(event.getSubject())){
                    jsVolume.removeChangeListener(JajukSystray.this);
                    jsVolume.setValue((int)(100*Player.getCurrentVolume()));
                    jsVolume.addChangeListener(JajukSystray.this);
                    ActionManager.getAction(JajukAction.MUTE_STATE).setIcon(Util.getIcon(ICON_MUTE));
                    ActionManager.getAction(JajukAction.MUTE_STATE).setName(Messages.getString("JajukWindow.2")); //$NON-NLS-1$
                }
                else if (EVENT_HEART_BEAT.equals(subject) &&!FIFO.isStopped() && !Player.isPaused()){
                    //if position is adjusting, no dont disturb user
                    if (jsPosition.getValueIsAdjusting()){
                        return;
                    }
                    //make sure not to set to old position
                    if ((System.currentTimeMillis() - lDateLastAdjust) < 4000){
                        return;
                    }
                    long length = JajukTimer.getInstance().getCurrentTrackTotalTime(); 
                    long lTime = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
                    int iPos = (int)(100*JajukTimer.getInstance().getCurrentTrackPosition());
                    jsPosition.removeChangeListener(JajukSystray.this);
                    jsPosition.setValue(iPos);    
                    jsPosition.addChangeListener(JajukSystray.this);
                }
            }
            
        });
    }
      
    /**
     * Hide systray 
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
        if (e.getSource() == jsPosition){
            int iOld = jsPosition.getValue();
            int iNew = iOld - (e.getUnitsToScroll()*3);
            setPosition(((float)iNew)/100);
        }
        else if (e.getSource() == jsVolume){
            int iOld = jsVolume.getValue();
            int iNew = iOld - (e.getUnitsToScroll()*3);
            setVolume(((float)iNew)/100);
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
        Player.seek(fPosition);
    }
    
    private void setVolume(float fVolume){
        jsVolume.removeChangeListener(this);
        jsVolume.removeMouseWheelListener(this);
        //if user move the volume slider, unmute
        Player.mute(false);
        Player.setVolume(fVolume);
        jmiMute.setIcon(Util.getIcon(ICON_MUTE));
        jmiMute.setText(Messages.getString("JajukWindow.2")); //$NON-NLS-1$
        jsVolume.addChangeListener(this);
        jsVolume.addMouseWheelListener(this);
    }
    
}
