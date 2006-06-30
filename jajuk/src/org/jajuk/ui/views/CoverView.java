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

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Cover;
import org.jajuk.base.Directory;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.perspectives.PlayerPerspective;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import ext.SwingWorker;

/**
 *  Cover view. Displays an image for the current album
 * <p>Physical and logical perspectives
 * @author     Bertrand Florat
 * @created   28 dec. 2003
 */
public class CoverView extends ViewAdapter implements Observer,ComponentListener,ActionListener,ITechnicalStrings{
    
    /**Current directory used as a cache for perfs*/
    private Directory dirCurrent;
    
    /**List of available covers for the current file*/
    private ArrayList alCovers = new ArrayList(20);
    
    //control panel
    JPanel jpControl;
    JButton jbPrevious;
    JButton jbNext;
    JButton jbDelete;
    JButton jbSave;
    JButton jbSaveAs;
    JButton jbDefault;
    JLabel jlSize;
    JLabel jlFound;
    JLabel jlSearching;
    JComboBox jcbAccuracy;
    
    /**Date last resize (used for adjustment management)*/
    private long lDateLastResize;
    
    /**URL and size of the image */
    JLabel jl;
    
    /**Default cover */
    private static Cover coverDefault; 
    
    /**Used Cover index*/
    int index = 0;
    
    /**ID*/
    public String sID;
    
    /**Generic locker*/
    private byte[] bLock = new byte[0];
    
    /**Event ID*/
    private volatile int iEventID;
    
    /**Flag telling that user wants to display a better cover*/
    private boolean bGotoBetter = false;
    
    /**Error counter to check connection availability*/
    private static int iErrorCounter = 0;
    
    /**Connected one flag : true if jajuk managed once to connect to the web to bring covers*/
    private static boolean bOnceConnected = false;
    
    /**Final image to display*/
    private ImageIcon ii;
    
    /**
     * Constructor
     * @param sID ID used to store independently parameters of views
     */
    public CoverView(String sID) {
        this.sID = sID;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#display()
     */
    public void populate(){
        //global layout
        double size[][] =
        {{0.99},
                {30,0.99}};
        setLayout(new TableLayout(size));
        //Control panel
        jpControl = new JPanel();
        jpControl.setBorder(BorderFactory.createEtchedBorder());
        int iXspace = 1;
        double sizeControl[][] =
        {{20,iXspace,20,2*iXspace,20,iXspace,20,iXspace,20,iXspace,20,iXspace,0.30,iXspace,0.30,2*iXspace,0.40,2*iXspace,20},
                {25}};
        jpControl.setLayout(new TableLayout(sizeControl));
        jbPrevious = new JButton(Util.getIcon(ICON_PREVIOUS));
        jbPrevious.addActionListener(this);
        jbPrevious.setToolTipText(Messages.getString("CoverView.4")); //$NON-NLS-1$
        jbNext = new JButton(Util.getIcon(ICON_NEXT));
        jbNext.addActionListener(this);
        jbNext.setToolTipText(Messages.getString("CoverView.5")); //$NON-NLS-1$
        jbDelete = new JButton(Util.getIcon(ICON_DELETE));
        jbDelete.addActionListener(this);
        jbDelete.setToolTipText(Messages.getString("CoverView.2")); //$NON-NLS-1$
        jbSave = new JButton(Util.getIcon(ICON_SAVE));
        jbSave.addActionListener(this);
        jbSave.setToolTipText(Messages.getString("CoverView.6")); //$NON-NLS-1$
        jbSaveAs = new JButton(Util.getIcon(ICON_SAVE_AS));
        jbSaveAs.addActionListener(this);
        jbSaveAs.setToolTipText(Messages.getString("CoverView.7")); //$NON-NLS-1$
        jbDefault = new JButton(Util.getIcon(ICON_DEFAULT_COVER));
        jbDefault.addActionListener(this);
        jbDefault.setToolTipText(Messages.getString("CoverView.8")); //$NON-NLS-1$
        jlSize = new JLabel(""); //$NON-NLS-1$
        jlFound = new JLabel(""); //$NON-NLS-1$
        jlSearching = new JLabel("",Util.getIcon(ICON_NET_SEARCH),JLabel.CENTER); //$NON-NLS-1$
        jcbAccuracy = new JComboBox();
        jcbAccuracy.setToolTipText(Messages.getString("ParameterView.155")); //$NON-NLS-1$
        jcbAccuracy.addItem(Messages.getString("ParameterView.156")); //$NON-NLS-1$
        jcbAccuracy.addItem(Messages.getString("ParameterView.157")); //$NON-NLS-1$
        jcbAccuracy.addItem(Messages.getString("ParameterView.158")); //$NON-NLS-1$
        jcbAccuracy.addItem(Messages.getString("ParameterView.168")); //$NON-NLS-1$
        jcbAccuracy.addItem(Messages.getString("CoverView.12")); //$NON-NLS-1$
        jcbAccuracy.addItem(Messages.getString("CoverView.13")); //$NON-NLS-1$
        jcbAccuracy.setSelectedIndex(Integer.parseInt(ConfigurationManager.getProperty(CONF_COVERS_ACCURACY+"_"+sID))); //$NON-NLS-1$
        jcbAccuracy.addActionListener(this);
        
        jpControl.add(jbPrevious,"0,0");//$NON-NLS-1$
        jpControl.add(jbNext,"2,0");//$NON-NLS-1$
        jpControl.add(jbDelete,"4,0");//$NON-NLS-1$
        jpControl.add(jbSave,"6,0");//$NON-NLS-1$
        jpControl.add(jbSaveAs,"8,0");//$NON-NLS-1$
        jpControl.add(jbDefault,"10,0");//$NON-NLS-1$
        jpControl.add(Util.getCentredPanel(jlSize,BoxLayout.X_AXIS),"12,0");//$NON-NLS-1$
        jpControl.add(Util.getCentredPanel(jlFound,BoxLayout.X_AXIS),"14,0");//$NON-NLS-1$
        jpControl.add(Util.getCentredPanel(jcbAccuracy,BoxLayout.X_AXIS),"16,0");//$NON-NLS-1$
        jpControl.add(Util.getCentredPanel(jlSearching,BoxLayout.X_AXIS),"18,0");//$NON-NLS-1$
        ObservationManager.register(EVENT_COVER_REFRESH,this);
        ObservationManager.register(EVENT_PLAYER_STOP,this);
        ObservationManager.register(EVENT_ZERO,this);
        ObservationManager.register(EVENT_COVER_CHANGE,this);
        try {
            //instanciate default cover
            if (coverDefault == null){
                coverDefault = new Cover(new URL(IMAGES_SPLASHSCREEN),Cover.DEFAULT_COVER);
            }
        } catch (Exception e) {
            Log.error(e);
        }
        add(jpControl,"0,0"); //$NON-NLS-1$
        //request cover refresh after a while to allow ui to paint
        new Thread(){
            public void run(){
                try{
                    if (ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER)){
                        DownloadManager.getRemoteCoversList("");//try to open connexion, this can take about 30 sec under linux if network not available //$NON-NLS-1$
                    }
                    Thread.sleep(3000); //more sec in case of...
                }
                catch(Exception e){
                    Log.error(e);
                }
                //Request for cover refresh
                update(new Event(EVENT_COVER_REFRESH,ObservationManager.getDetailsLastOccurence(EVENT_COVER_REFRESH)));
                
            }
        }.start();
        
        
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(Event event){
        removeComponentListener(CoverView.this);
        addComponentListener(CoverView.this); //listen for resize
        String subject = event.getSubject();
        this.iEventID = (int)(Integer.MAX_VALUE*Math.random());
        int iLocalEventID = this.iEventID;
        synchronized(bLock){//block any concurrent cover update
            try{
                searching(true);
                if (EVENT_COVER_REFRESH.equals(subject)){
                    org.jajuk.base.File fCurrent = FIFO.getInstance().getCurrentFile();
                    if (fCurrent == null){
                        this.dirCurrent = null;       
                    }
                    else{
                        this.dirCurrent = fCurrent.getDirectory(); //store this dir
                    }
                    alCovers.clear(); //remove all existing covers
                    //if current file is null ( probably a file cannot be read ) 
                    if ( this.dirCurrent == null){
                        alCovers.add(coverDefault);
                        index = 0;
                        displayCurrentCover(); 
                        return; 
                    }
                    //search for local covers in all directories mapping the current track to reach other devices covers and display them together
                    Track trackCurrent = fCurrent.getTrack();
                    ArrayList alFiles = trackCurrent.getFiles(); //list of files mapping the track
                    Iterator it = alFiles.iterator();
                    while (it.hasNext()){
                        org.jajuk.base.File file = (org.jajuk.base.File)it.next();
                        Directory dirScanned = file.getDirectory();
                        if ( !dirScanned.getDevice().isMounted()) { //if the device is not ready, just ignore it
                            continue;
                        }
                        java.io.File[] files = dirScanned.getFio().listFiles();//null if none file found
                        boolean bAbsoluteCover = false; //whether an absolute cover ( unique) has been found
                        for (int i=0;files != null && i<files.length;i++){
                            //check size to avoid out of memory errors
                            if (files[i].length() > MAX_COVER_SIZE*1024){
                                continue;
                            }
                            String sExt = Util.getExtension(files[i]);
                            if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png") || sExt.equalsIgnoreCase("gif")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                if (!bAbsoluteCover && Util.isAbsoluteDefaultCover(FIFO.getInstance().getCurrentFile().getDirectory(),files[i].getName())){
                                    //test the cover is not already used
                                    Cover cover = new Cover(files[i].toURL(),Cover.ABSOLUTE_DEFAULT_COVER);
                                    if (!alCovers.contains(cover)){
                                        alCovers.add(cover);
                                    }
                                    bAbsoluteCover = true;
                                }
                                else{ //normal local cover
                                    Cover cover = new Cover(files[i].toURL(),Cover.LOCAL_COVER);
                                    if (!alCovers.contains(cover)){
                                        alCovers.add(cover);
                                    }
                                }
                            }
                        }
                    }
                    // then we search for web covers online if max connection errors number is not reached or if user already managed to connect
                    if (ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER) 
                            && (bOnceConnected || iErrorCounter < STOP_TO_SEARCH)){
                        try{
                            int iCoversBeforeSearch = alCovers.size(); //stores number of covers before web search
                            final String sQuery = createQuery(fCurrent);
                            Log.debug("Query={{"+sQuery+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
                            if (!sQuery.equals("")){ //there is not enough information in tags for a web search //$NON-NLS-1$
                                ArrayList alUrls;
                                alUrls = DownloadManager.getRemoteCoversList(sQuery);
                                bOnceConnected = true; //user managed once to connect to the web
                                if (alUrls.size() > MAX_REMOTE_COVERS){ //limit number of remote covers
                                    alUrls = new ArrayList(alUrls.subList(0,MAX_REMOTE_COVERS));
                                }
                                Collections.reverse(alUrls); //set best results to be displayed  first
                                Iterator it2 = alUrls.iterator(); //add found covers 
                                while ( it2.hasNext() && this.iEventID == iLocalEventID){ //load each cover (pre-load or post-load) and stop if a signal has been emitted
                                    URL url = (URL)it2.next();
                                    try{
                                        Cover cover = new Cover(url,Cover.REMOTE_COVER);//create a cover with given url ( image will be really downloaded when required if no preload)
                                        if (!alCovers.contains(cover)){
                                            Log.debug("Found Cover: {{"+url.toString()+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
                                            alCovers.add(cover);
                                        }
                                    }
                                    catch(Exception e){
                                        Log.error(e); //can occur in case of timeout or error during cover download
                                        if (e instanceof org.apache.commons.httpclient.ConnectTimeoutException){
                                            iErrorCounter ++;
                                            if (iErrorCounter == STOP_TO_SEARCH){
                                                Log.warn("Too many connection fails, stop to search for covers online"); //$NON-NLS-1$
                                                InformationJPanel.getInstance().setMessage(Messages.getString("Error.030"),InformationJPanel.WARNING); //$NON-NLS-1$
                                            }
                                        }
                                    }
                                }
                                if (this.iEventID != iLocalEventID){ //a stop signal has been emmited from a concurrent thread
                                    Log.debug("Download stopped - 1"); //$NON-NLS-1$
                                    return;
                                }
                            }
                        }
                        catch(Exception e){
                            if (e instanceof org.apache.commons.httpclient.ConnectTimeoutException){
                                Log.warn(e.getMessage()); //can occur in case of timeout or error during covers list download
                                iErrorCounter ++;
                                if (iErrorCounter == STOP_TO_SEARCH){
                                    Log.warn("Too many connection fails, stop to search for covers online"); //$NON-NLS-1$
                                    InformationJPanel.getInstance().setMessage(Messages.getString("Error.030"),InformationJPanel.WARNING); //$NON-NLS-1$
                                }
                            }
                            else{
                                Log.error(e);
                            }
                        }
                    }
                    if (alCovers.size() == 0){//add the default cover if none other cover has been found
                        alCovers.add(coverDefault); 
                    }
                    Collections.sort(alCovers); //sort the list
                    Log.debug("Local cover list: {{"+alCovers+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
                    if (ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE) || PerspectiveManager.getCurrentPerspective() instanceof PlayerPerspective){ //in player perspective, always show shuffle covers
                        index = (int)(Math.random()*alCovers.size()); //choose a random cover
                    }
                    else{
                        index = alCovers.size()-1;  //current index points to the best available cover
                    }
                    setFoundText(); //update found text 
                    displayCurrentCover();
                }
                else if ( EVENT_PLAYER_STOP.equals(subject) || EVENT_ZERO.equals(subject)){
                    setFoundText("");  //$NON-NLS-1$ 
                    setSizeText("");//$NON-NLS-1$
                    alCovers.clear();
                    alCovers.add(coverDefault); //add the default cover
                    index = 0;
                    displayCurrentCover();
                    dirCurrent = null;
                }
                else if ( EVENT_COVER_CHANGE.equals(subject)
                        && isInCurrentPerspective()){
                    index = (int)(Math.random()*alCovers.size()-1); //choose a random cover
                    displayCurrentCover();
                }
            }
            catch(Exception e){
                Log.error(e);
            }
            finally{
                searching(false); //hide searching icon
            }
        }
    }
    
    /**
     * Set the cover Found text
     */
    private void setFoundText(){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //make sure not to display negative indexes
                int i = getCoverNumber()-index;
                if (i<0){
                    Log.debug("Negative cover index: "+i); //$NON-NLS-1$
                    i=0;
                }
                
                jlFound.setText(i+"/"+getCoverNumber()); //$NON-NLS-1$//$NON-NLS-2$
            }
        });
    }
    
    /**
     * Set the cover Found text
     * @param sFound specified text
     */
    private void setFoundText(final String sFound){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if ( sFound!=null){   
                    jlFound.setText(sFound); //$NON-NLS-1$//$NON-NLS-2$
                }
            }
        });
    }
    
    /**
     * Display or hide search icon
     * @param bSearching
     */
    public void searching(final boolean bSearching){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (bSearching){
                    jlSearching.setIcon(Util.getIcon(ICON_NET_SEARCH));
                }
                else{
                    jlSearching.setIcon(null);
                }
            }
        });
    }
    
    /**
     * Set the cover size text
     * @param sFound
     */
    private void setSizeText(final String sSize){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (sSize != null){
                    jlSize.setText(sSize); //$NON-NLS-1$
                }
            }
        });
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getDesc()
     */
    public String getDesc() {
        return "CoverView.3";    //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getID()
     */
    public String getID() {
        return "org.jajuk.ui.views.CoverView"; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    public void componentResized(ComponentEvent e) {
        Log.debug("Cover resized"); //$NON-NLS-1$
        long lCurrentDate = System.currentTimeMillis();  //adjusting code
        if ( lCurrentDate - lDateLastResize < 500){  //display image every 500 ms to save CPU
            lDateLastResize = lCurrentDate;
            return;
        }
        displayCurrentCover();
        CoverView.this.revalidate();  //make sure the image is repainted
        CoverView.this.repaint();  //make sure the image is repainted
    }
    
    /**
     * Display current cover (at this.index), try all covers in case of error
     */
    private void displayCurrentCover(){
        SwingWorker sw = new SwingWorker() {
            public Object  construct(){
                synchronized(bLock){
                    removeComponentListener(CoverView.this); //remove listener to avoid looping
                    if ( alCovers.size() == 0){ //should not append
                        alCovers.add(coverDefault); //Add at last the default cover if all remote cover has been discarded
                        try {
                            prepareDisplay(0);
                        } catch (JajukException e) {
                            Log.error(e);
                        }
                        return null;    
                    }
                    if (alCovers.size() == 1 &&
                            ((Cover)alCovers.get(0)).getType() == Cover.DEFAULT_COVER){ //only a default cover 
                        try {
                            prepareDisplay(0);
                        } catch (JajukException e) {
                            Log.error(e);
                        }
                        return null;    
                    }
                    //else, there is at least one local cover and no default cover
                    while (alCovers.size() > 0){
                        Cover cover = null;
                        try{
                            prepareDisplay(index);
                            return null; //OK, leave
                        }
                        catch(Exception e){
                            Log.debug("Removed cover: {{"+alCovers.get(index)+"}}"); //$NON-NLS-1$ //$NON-NLS-2$
                            alCovers.remove(index);    
                            //refresh number of found covers
                            if (!bGotoBetter){ //we go to worse covers. If we go to better covers, we just keep the same index
                                // try a worse cover...
                                if (index - 1 >= 0){
                                    index --;
                                }
                                else{ //no more worse cover
                                    index = alCovers.size()-1; //come back to best cover
                                }  
                            }
                            setFoundText();
                        }
                    }
                    //if this code is executed, it means than no available cover was found, then display default cover
                    alCovers.add(coverDefault); //Add at last the default cover if all remote cover has been discarded
                    try {
                        index = 0;
                        prepareDisplay(index);
                    } catch (JajukException e) {
                        Log.error(e);
                    }
                }
                return null;
            }
            public void finished() {
                displayCover(index);
                removeComponentListener(CoverView.this);
                addComponentListener(CoverView.this); //listen for resize
            }
        };
        sw.start();
    }
    
    
    /**
     * Display given cover
     * @param index index of the cover to display
     *
     */
    private void displayCover(int index) {
        if(alCovers.size() == 0 || index >= alCovers.size() || index < 0){ //just a check
            alCovers.add(coverDefault); //display default cover by default
            displayCover(0);
            return;
        }
        Cover cover = (Cover)alCovers.get(index);  //take image at the given index
        URL url = cover.getURL();
        //enable delete button only for local covers
        if (cover.getType() == Cover.LOCAL_COVER 
                || cover.getType() == Cover.ABSOLUTE_DEFAULT_COVER){
            jbDelete.setEnabled(true);
        }
        else{
            jbDelete.setEnabled(false);
        }
        if (url != null){
            jbSave.setEnabled(false);
            String sType = " (L)"; //local cover //$NON-NLS-1$
            if (cover.getType() == Cover.REMOTE_COVER){
                sType = " (@)"; //Web cover //$NON-NLS-1$
                jbSave.setEnabled(true);
            }
            String size = cover.getSize();
            jl = new JLabel(ii);
            jl.setMinimumSize(new Dimension(0,0)); //required for info node resizing
            jl.setToolTipText("<html>"+url.toString()+"<br>"+size+"K"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            setSizeText(size+"K"+sType); //$NON-NLS-1$
            setFoundText();
        }
        //set tooltip for previous and next track
        try{
            int indexPrevious  = index+1;
            if (indexPrevious > alCovers.size()-1){
                indexPrevious = 0;
            }
            URL urlPrevious = ((Cover)alCovers.get(indexPrevious)).getURL();
            if (urlPrevious != null){
                jbPrevious.setToolTipText("<html>"+Messages.getString("CoverView.4")+"<br>"+urlPrevious.toString()+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            int indexNext = index-1;
            if (indexNext < 0){
                indexNext = alCovers.size()-1;
            }
            final URL urlNext = ((Cover)alCovers.get(indexNext)).getURL();
            if (urlNext != null){
                jbNext.setToolTipText("<html>"+Messages.getString("CoverView.5")+"<br>"+urlNext.toString()+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
        }
        catch(Exception e){  //the url code can throw out of bounds exception for unkwown reasons so check it
            Log.debug("jl="+jl+" url={{"+url+"}}"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Log.error(e);
        }
        setCursor(Util.WAIT_CURSOR);
        if (getComponentCount() > 0){
            removeAll();
        }
        add(jpControl,"0,0");//$NON-NLS-1$
        add(jl,"0,1");//$NON-NLS-1$
        setCursor(Util.DEFAULT_CURSOR);
        searching(false);
    }
    
    /**
     * Long action to compute image to display (dowload, resizing...)
     * @param index
     * @return null (just used by the SwingWorker)
     * @throws JajukException
     */
    private Object prepareDisplay(int index) throws JajukException{
        int iLocalEventID = CoverView.this.iEventID;
        Log.debug("display index: "+index); //$NON-NLS-1$
        searching(true); //lookup icon
        //find next correct cover
        ImageIcon icon = null;
        Cover cover = null; 
        try{
            if (CoverView.this.iEventID == iLocalEventID){
                cover = (Cover)alCovers.get(index);  //take image at the given index
                icon = cover.getImage();
            }
            else{
                Log.debug("Download stopped - 2"); //$NON-NLS-1$
                return null;
            }
        }
        catch(Exception e){ //this cover cannot be loaded
            setCursor(Util.DEFAULT_CURSOR);
            searching(false);
            Log.error(e);
            throw new JajukException("000"); //$NON-NLS-1$
        }
        int iDisplayAreaHeight = CoverView.this.getHeight() - 30; 
        int iDisplayAreaWidth = CoverView.this.getWidth() - 8; 
        //check minimum sizes
        if (iDisplayAreaHeight < 1 || iDisplayAreaWidth <1){
            return null;
        }
        int iNewWidth;
        int iNewHeight;
        float fRatio;
        if ( iDisplayAreaHeight > iDisplayAreaWidth){
            // Width is smaller than height : try to optimize height
            iNewHeight = iDisplayAreaHeight; //take all possible height
            //we check now if width will be visible entirely with optimized height
            float fHeightRatio = (float)iNewHeight/icon.getIconHeight();
            if (icon.getIconWidth()*fHeightRatio <= iDisplayAreaWidth){
                iNewWidth = (int)(icon.getIconWidth()*fHeightRatio);
            }
            else{
                //no? so we optimize width 
                iNewWidth = iDisplayAreaWidth;
                iNewHeight = (int)(icon.getIconHeight() * ((float)iNewWidth/icon.getIconWidth())) ;     
            }
        } 
        else  {
            // Height is smaller or equal than width : try to optimize width
            iNewWidth = iDisplayAreaWidth; //take all possible width
            // we check now if height will be visible entirely with optimized width
            float fWidthRatio = (float)iNewWidth/icon.getIconWidth();
            if (icon.getIconHeight()*(fWidthRatio) <= iDisplayAreaHeight){
                iNewHeight = (int)(icon.getIconHeight()*fWidthRatio);
            }
            else{
                //no? so we optimize width 
                iNewHeight = iDisplayAreaHeight;
                iNewWidth = (int)(icon.getIconWidth() * ((float)iNewHeight/icon.getIconHeight())) ;     
            }
        }
        
        if (CoverView.this.iEventID == iLocalEventID){
            ii = Util.getResizedImage(icon,iNewWidth,iNewHeight);}
        else{
            Log.debug("Download stopped - 2"); //$NON-NLS-1$
            return null;
        }
        return null;
    }
    
    
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == jcbAccuracy){
            ConfigurationManager.setProperty(CONF_COVERS_ACCURACY+"_"+sID,Integer.toString(jcbAccuracy.getSelectedIndex())); //$NON-NLS-1$
            new Thread(){
                public void run(){
                    update(new Event(EVENT_COVER_REFRESH,ObservationManager.getDetailsLastOccurence(EVENT_COVER_REFRESH))); //force refreshing
                }
            }.start();
        }
        else if(e.getSource() == jbPrevious){  //previous : show a better cover
            bGotoBetter = true;
            index++;
            if (index > alCovers.size()-1){
                index = 0;
            }
            displayCurrentCover();
            bGotoBetter = false; //make sure default behavior is to go to worse covers
        }
        else if(e.getSource() == jbNext){ //next : show a worse cover
            bGotoBetter = false;
            index--;
            if (index < 0){
                index = alCovers.size()-1;
            }
            displayCurrentCover();
        }
        else if(e.getSource() == jbDelete){ //delete a local cover
            Cover cover = (Cover)alCovers.get(index);
            //show confirmation message if required
            if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_COVER)){
                int iResu = Messages.getChoice(Messages.getString("Confirmation_delete_cover")+" : "+cover.getURL().getFile(),JOptionPane.WARNING_MESSAGE);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (iResu != JOptionPane.YES_OPTION){
                    return;
                }
            }
            //yet there? ok, delete the cover
            try{
                File file = new File(cover.getURL().getFile());
                if (file.isFile() && file.exists()){
                    file.delete();
                    //check that file has been really deleted (sometimes, we get no exception)
                    if (file.exists()){
                        throw new Exception(""); //$NON-NLS-1$
                    }
                }
                else{  //not a file, must have a problem
                    throw new Exception(""); //$NON-NLS-1$
                }
            }
            catch(Exception ioe){
                Log.error("131",ioe); //$NON-NLS-1$
                Messages.showErrorMessage("131"); //$NON-NLS-1$
                return;
            }
            //If this was the absolute cover, remove the reference in the collection
            if (cover.getType() == Cover.ABSOLUTE_DEFAULT_COVER){
                dirCurrent.removeProperty("default_cover"); //$NON-NLS-1$
            }
            // reorganize covers
            synchronized(bLock){
                alCovers.remove(index);
                index--;
                if (index < 0){
                    index = alCovers.size()-1;
                }
                ObservationManager.notify(new Event(EVENT_COVER_REFRESH));
            }
        }
        else if ( e.getSource() == jbDefault){ //choose a default
            //first commit this cover on the disk if it is a remote cover
            Cover cover = (Cover)alCovers.get(index);
            String sFilename = Util.getOnlyFile(cover.getURL().toString());
            if (cover.getType() == Cover.REMOTE_COVER){
                String sFilePath = dirCurrent.getFio().getPath()+"/"+sFilename; //$NON-NLS-1$
                try{
                    //copy file from cache
                    File fSource = new File(Util.getCachePath(cover.getURL()));
                    File file = new File(sFilePath);
                    Util.copy(fSource,file);
                    Cover cover2 = new Cover(file.toURL(),Cover.ABSOLUTE_DEFAULT_COVER);
                    if (!alCovers.contains(cover2)){
                        alCovers.add(cover2);
                        setFoundText();
                    }
                    refreshThumbs(cover);
                    ObservationManager.notify(new Event(EVENT_COVER_REFRESH));//add new cover in others cover views
                    InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
                }
                catch(Exception ex){
                    Log.error("024",ex); //$NON-NLS-1$
                    Messages.showErrorMessage("024"); //$NON-NLS-1$
                }
            }
            else{
                refreshThumbs(cover);
                InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.8"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
            }
            //then make it the default cover in this directory
            dirCurrent.setProperty("default_cover",sFilename); //$NON-NLS-1$
            
        }
        else if(e.getSource() == jbSave ){ //save a file with its original name
            new Thread(){
                public void run() {
                    Cover cover = (Cover)alCovers.get(index);
                    //should not happen, only remote covers here
                    if (cover.getType() != Cover.REMOTE_COVER){ 
                        Log.debug("Try to save a local cover");//$NON-NLS-1$
                        return;
                    }
                    String sFilePath = null;
                    sFilePath = dirCurrent.getFio().getPath()+"/"+Util.getOnlyFile(cover.getURL().toString()); //$NON-NLS-1$
                    try{
                        //copy file from cache
                        File fSource = new File(Util.getCachePath(cover.getURL()));
                        File file = new File(sFilePath);
                        Util.copy(fSource,file);
                        InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
                        Cover cover2 = new Cover(file.toURL(),Cover.ABSOLUTE_DEFAULT_COVER);
                        if (!alCovers.contains(cover2)){
                            alCovers.add(cover2);
                            setFoundText();
                        }
                        ObservationManager.notify(new Event(EVENT_COVER_REFRESH));//add new cover in others cover views
                    }
                    catch(Exception ex){
                        Log.error("024",ex); //$NON-NLS-1$
                        Messages.showErrorMessage("024"); //$NON-NLS-1$
                    }
                }
            }.start();
        }
        else if(e.getSource() == jbSaveAs ){ //save a file as... (can be local now) 
            new Thread(){
                public void run() {
                    Cover cover = (Cover)alCovers.get(index);
                    JFileChooser jfchooser = new JFileChooser(dirCurrent.getFio());
                    FileFilter filter = new FileFilter() {
                        public boolean accept(File file) {
                            String sExt =Util.getExtension(file); 
                            if (sExt.equals("gif") || sExt.equals("png") || sExt.equals("jpg") ){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                return true;
                            }
                            return false;
                        }
                        
                        public String getDescription() {
                            return "*.gif,*.png,*.jpg"; //$NON-NLS-1$
                        }
                    };
                    jfchooser.setFileFilter(filter);
                    jfchooser.setDialogTitle(Messages.getString("CoverView.10")); //$NON-NLS-1$
                    File finalFile = new File(dirCurrent.getFio().getPath()+"/"+Util.getOnlyFile(cover.getURL().toString())); //$NON-NLS-1$
                    jfchooser.setSelectedFile(finalFile);
                    int returnVal = jfchooser.showSaveDialog(Main.getWindow());
                    File fNew = null;
                    if (returnVal == JFileChooser.APPROVE_OPTION ) {
                        fNew  = jfchooser.getSelectedFile();
                        //if user try to save as without changinng file name
                        if (fNew.getAbsolutePath().equals(cover.getFile().getAbsolutePath())){
                            return;
                        }
                        try {
                            Util.copy(cover.getFile(),fNew);
                            InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
                            ObservationManager.notify(new Event(EVENT_COVER_REFRESH));
                        }
                        catch(Exception ex){
                            Log.error("024",ex); //$NON-NLS-1$
                            Messages.showErrorMessage("024"); //$NON-NLS-1$
                        }
                    }
                }
            }.start();
        }
        
    }
    
    
    /**
     * Refresh default cover thumb (used in catalog view)
     *
     */
    private void refreshThumbs(Cover cover){
        //refresh thumbs
        try{
            for (int i=0; i<4; i++){
                Album album = dirCurrent.getFiles().iterator().next().getTrack().getAlbum();
                File fThumb = new File(FILE_THUMBS+'/'+(50+50*i)+"x"+(50+50*i)+'/'+album.getId()+'.'+EXT_THUMB); //$NON-NLS-1$
                Util.createThumbnail(cover.getFile(),fThumb,(50+50*i));
            }
            ObservationManager.notify(new Event(EVENT_COVER_DEFAULT_CHANGED));
        }
        catch(Exception ex){
            Log.error("024",ex); //$NON-NLS-1$
        }
    }
    
    /**
     * 
     * @return number of real covers (not default) covers found
     */ 
    private int getCoverNumber(){
        synchronized(bLock){
            return alCovers.size() ;
        }
    }
    
    /**
     * 
     * @param file
     * @return an accurate  google search query for a file
     */
    public String createQuery(org.jajuk.base.File file){
        String sQuery = ""; //$NON-NLS-1$
        int iAccuracy = ConfigurationManager.getInt(CONF_COVERS_ACCURACY+"_"+sID); //$NON-NLS-1$
        Track track = file.getTrack();
        Author author = track.getAuthor();
        Album album = track.getAlbum();
        switch(iAccuracy){
        case 0: //low, default
            if (!author.isUnknown()){
                sQuery += author.getName() + " "; //$NON-NLS-1$    
            }
            if (!album.isUnknown()){
                sQuery += album.getName() + " "; //$NON-NLS-1$    
            }
            break;
        case 1: //medium
            if (!author.isUnknown()){
                sQuery += "\"" +author.getName() + "\" "; //put "" around it //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!album.isUnknown()){
                sQuery += "\""+ album.getName() + "\" "; //$NON-NLS-1$ //$NON-NLS-2$    
            }
            break;
        case 2: //high 
            if (!author.isUnknown()){
                sQuery += "+\"" +author.getName() + "\" "; //put "" around it //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (!album.isUnknown()){
                sQuery += "+\""+ album.getName() + "\" "; //$NON-NLS-1$ //$NON-NLS-2$    
            }
            break;
        case 3: //by author 
            if (!author.isUnknown()){
                sQuery += author.getName() + " "; //$NON-NLS-1$    
            } 
            break;
        case 4: //by album 
            if (!album.isUnknown()){
                sQuery += album.getName() + " "; //$NON-NLS-1$    
            } 
            break;
        case 5: //by track name 
            sQuery += track.getName() ; 
            break;
        default :
            break;
        }
        return sQuery;
    }
    
    
    /**
     * To be refactored
     * @return whether this view is in current perspective
     */
    public boolean isInCurrentPerspective(){
        IPerspective current = PerspectiveManager.getCurrentPerspective();
        for (IView view:PerspectiveManager.getCurrentPerspective().getViews()){
            if (view instanceof CoverView){
                CoverView cv = (CoverView)view;
                if (cv.sID.equals(sID)){
                    return true;
                }
            }
        }
        return false;
    }
    
}
