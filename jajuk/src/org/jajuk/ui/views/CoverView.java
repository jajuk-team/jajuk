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
 *  $Revision$
 */

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import org.jajuk.base.FIFO;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.perspectives.PlayerPerspective;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Cover view. Displays an image for the current album
 * <p>Physical and logical perspectives
 * @author     bflorat
 * @created   28 dec. 2003
 */
public class CoverView extends ViewAdapter implements Observer,ComponentListener,ActionListener{
    
    /**Current directory used as a cache for perfs*/
    private File fDir;
    
    /**List of available covers for the current file*/
    ArrayList alCovers = new ArrayList(20);
    
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
    
    /**Disk covers*/
    ArrayList alFiles = new ArrayList(10);
    
    JLabel jl;
    
    /**Default cover */
    private static Cover coverDefault; 
    
    /**Concurency flag to avoid several refreshs blocked by the cover search*/
    static boolean bAlreadyRefreshing = false;
    
    /**Used Cover index*/
    int index = 0;
    
    /**Thread lock for displayer*/
    byte[] bLock = new byte[0];
    
    /**ID*/
    public String sID;
    
    /**Stop flag for covers download*/
    private boolean bStop = false;
    
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
        
        try {
            //check if the cover should be refreshed at startup
            coverDefault = new Cover(new URL(IMAGES_SPLASHSCREEN),Cover.DEFAULT_COVER); //instanciate default cover
        } catch (Exception e) {
            Log.error(e);
        }
        synchronized(alCovers){
            alCovers.add(coverDefault); //add the default cover
        }
        update(EVENT_COVER_REFRESH);
        this.addComponentListener(this); //listen for resize
        
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(String subject){
        if (bAlreadyRefreshing){ //another refreshing is probably blocked until time out by a web search
            return;
        }
        bAlreadyRefreshing = true;
        try{
            if ( EVENT_COVER_REFRESH.equals(subject)){
                //stop any other thread downloading covers
                bStop = true;
                final org.jajuk.base.File fCurrent = FIFO.getInstance().getCurrentFile();
                //if current file is null ( probably a file cannot be read ) 
                if ( fCurrent == null){
                    displayCurrentCover(); 
                    return; 
                }
                synchronized(alCovers){
                    alCovers.clear();
                    this.fDir = null; //analyzed directory
                    //search for local covers in all directories mapping the current track to reach other devices covers and display them together
                    Track trackCurrent = fCurrent.getTrack();
                    ArrayList alFiles = trackCurrent.getFiles(); //list of files mapping the track
                    Iterator it = alFiles.iterator();
                    while (it.hasNext()){
                        org.jajuk.base.File file = (org.jajuk.base.File)it.next();
                        if ( !file.getDirectory().getDevice().isReady()) { //if the device is not ready, just ignore it
                            continue;
                        }
                        fDir = new java.io.File(file.getAbsolutePath()).getParentFile(); //store this dir
                        java.io.File[] files = fDir.listFiles();
                        boolean bAbsoluteCover = false; //whether an absolute cover ( unique) has been found
                        for (int i=0;i<files.length;i++){
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
                        if (alCovers.size() == 0){//add the default cover if none other cover has been found
                            alCovers.add(coverDefault); 
                        }
                    }
                    //display right now local or default cover
                    Collections.sort(alCovers); //sort the list
                    Log.debug("Local cover list: "+alCovers); //$NON-NLS-1$
                    if (ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE) || PerspectiveManager.getCurrentPerspective() instanceof PlayerPerspective){ //in player perspective, always show shuffle covers
                        index = (int)(Math.random()*alCovers.size()); //choose a random cover
                    }
                    else{
                        index = alCovers.size()-1;  //current index points to the best available cover
                    }
                    setFoundText(); //update found text 
                }
                displayCurrentCover(); //display in advance without waiting for web search to accelerate local covers display
                //then we search for web covers asynchronously
                if (ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER)){
                    Thread t = new Thread(){ //start search asynchronously
                        public void run() {
                            synchronized(alCovers){
                                bStop = false; //allow download for this new thread
                                int iCoversBeforeSearch = alCovers.size(); //stores number of covers before web search
                                final String sQuery = createQuery(fCurrent);
                                Log.debug("Query="+sQuery); //$NON-NLS-1$
                                if (!sQuery.equals("")){ //there is not enough information in tags for a web search //$NON-NLS-1$
                                    final ArrayList alUrls;
                                    try{
                                        searching(true);  //display searching icon
                                        alUrls = DownloadManager.getRemoteCoversList(sQuery);
                                        Collections.reverse(alUrls); //set best results to be displayed  first
                                        //remove default cover if some remote cover have been found
                                        if ( alUrls.size() > 0 && alCovers.size()>0 && ((Cover)alCovers.get(0)).getType() == Cover.DEFAULT_COVER){
                                            alCovers.remove(0);
                                        }
                                        Iterator it = alUrls.iterator(); //add found covers 
                                        while ( it.hasNext() && !bStop){ //load each cover (pre-load or post-load) and stop if a signal has been emitted
                                            URL url = (URL)it.next();
                                            Log.debug("Found Cover: "+url.toString()); //$NON-NLS-1$
                                            alCovers.add(new Cover(url,Cover.REMOTE_COVER));//create a cover with given url ( image will be really downloaded when required)
                                        }
                                        if (bStop){
                                            Log.debug("Download stopped"); //$NON-NLS-1$
                                            return;
                                        }
                                        index += alUrls.size(); //reset index
                                        Collections.sort(alCovers); //sort the list again with new covers
                                        //refresh number of found covers
                                        setFoundText();
                                        //display the best found cover if no one was found before
                                        // force new cover display if the cover shuffle is activated or if nothing was displayed before
                                        if (ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE) //explicit shuffle
                                                || (iCoversBeforeSearch == 1 && alCovers.size() >1)  //nothing before
                                                || PerspectiveManager.getCurrentPerspective() instanceof PlayerPerspective){ //player perspective 
                                            if (ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE)){
                                                index = (int)(Math.random()*alCovers.size()); //choose a random cover
                                            }
                                            else{
                                                index = alCovers.size()-1;  //current index points to the best available cover
                                            }
                                            displayCurrentCover();
                                        }
                                    }
                                    catch(Exception e){
                                        return; //no web covers found, just leave
                                    }
                                    finally{
                                        searching(false); //hide searching icon
                                    }
                                }
                            }
                        }
                    };
                    t.setPriority(Thread.MIN_PRIORITY); //low priority
                    t.start();
                } 
            }
            else if ( EVENT_PLAYER_STOP.equals(subject) || EVENT_ZERO.equals(subject)){
                setFoundText("");  //$NON-NLS-1$ 
                setSizeText("");//$NON-NLS-1$
                synchronized(alCovers){
                    alCovers.clear();
                    alCovers.add(coverDefault); //add the default cover
                    index = 0;
                    displayCurrentCover();
                    fDir = null;
                }
            }
        }
        catch(Exception e){
            Log.error(e);
        }
        finally{
            bAlreadyRefreshing = false;  //unlock
        }
    }
    
    /**
     * Set the cover Found text
     */
    private void setFoundText(){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jlFound.setText((getCoverNumber()-index)+"/"+getCoverNumber()); //$NON-NLS-1$//$NON-NLS-2$
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
        return "CoverView.3";	 //$NON-NLS-1$
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
        if (!ConfigurationManager.getBoolean(CONF_COVERS_RESIZE)){ //if user didn't check resize option, just leave
            return;
        }
        long lCurrentDate = System.currentTimeMillis();  //adjusting code
        if ( lCurrentDate - lDateLastResize < 500){  //display image every 500 ms to save CPU
            lDateLastResize = lCurrentDate;
            return;
        }
        new Thread(){
            public void run(){
                displayCurrentCover();
                CoverView.this.revalidate();  //make sure the image is repainted
                CoverView.this.repaint();  //make sure the image is repainted
            }
        }.start();
    }
    
    /**
     * Display current cover
     *
     */
    private void displayCurrentCover(){
        if ( alCovers.size() == 0 ){
            return;
        }
        synchronized(bLock){
            Log.debug("display index: "+index); //$NON-NLS-1$
            searching(true); //lookup icon
            //find next correct cover
            ImageIcon icon = null;
            Cover cover = null; 
            while ( index >= 0){//there are some more covers after
                try{
                    setCursor(Util.WAIT_CURSOR); //waiting cursor
                    cover = (Cover)alCovers.get(index);  //take image at the given index
                    icon = cover.getImage();
                    icon.getImage().flush();      //free image memory             
                    setCursor(Util.DEFAULT_CURSOR);
                    break;
                }
                catch(Exception e){ //this cover cannot be loaded
                    synchronized(alCovers){
                        alCovers.remove(index);
                        //Add at last the default cover if all remote cover has been discarded
                        if (alCovers.size() == 0){
                            alCovers.add(coverDefault);
                        }
                        Log.debug("Removed cover: "+cover); //$NON-NLS-1$
                    }
                    //refresh number of found covers
                    setFoundText(); 
                    index  --; //look at next cover    
                }
            }
            if (icon == null){ //none available cover
                searching(false);
                return;
            }
            Image img = icon.getImage();
            if (ConfigurationManager.getBoolean(CONF_COVERS_RESIZE)){
                ImageFilter filter = new AreaAveragingScaleFilter(this.getWidth()-8,this.getHeight()-30);
                img = createImage(new FilteredImageSource(img.getSource(),filter));
                img.flush();//free image memory
            } 
           ImageIcon ii = new ImageIcon(img);
            jl = new JLabel(ii);
            jl.setMinimumSize(new Dimension(0,0)); //required for info node resizing
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if(alCovers.size() == 0){ //just a check
                        searching(false);
                        return;
                    }
                    Cover cover = (Cover)alCovers.get(index);  //take image at the given index
                    URL url = cover.getURL();
                    byte[] bData = cover.getData();
                    //enable delete button only for local covers
                    if (cover.getType() == Cover.LOCAL_COVER || cover.getType() == Cover.ABSOLUTE_DEFAULT_COVER){
                        jbDelete.setEnabled(true);
                    }
                    else{
                        jbDelete.setEnabled(false);
                    }
                    if (url != null){
                        int iSize = 0;
                        String sType = " (L)"; //local cover //$NON-NLS-1$
                        if (bData != null){
                            sType = " (@)"; //Web cover //$NON-NLS-1$
                            iSize = (int)(Math.ceil((double)bData.length/1024));
                        }
                        else{
                            iSize = (int)(Math.ceil((double)new File(url.getFile()).length()/1024));
                        }
                        jl.setToolTipText("<html>"+url.toString()+"<br>"+iSize+"K"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        setSizeText(iSize+"K"+sType); //$NON-NLS-1$
                        setFoundText();
                    }
                    //set tooltip for previous and next track
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
                    setCursor(Util.WAIT_CURSOR);
                    if (getComponentCount() > 0){
                        removeAll();
                    }
                    add(jpControl,"0,0");//$NON-NLS-1$
                    add(jl,"0,1");//$NON-NLS-1$
                    setCursor(Util.DEFAULT_CURSOR);
                    searching(false);
                    System.gc();//suggest JVM to perform a memory cleanup        
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == jcbAccuracy){
            ConfigurationManager.setProperty(CONF_COVERS_ACCURACY+"_"+sID,Integer.toString(jcbAccuracy.getSelectedIndex())); //$NON-NLS-1$
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    update(EVENT_COVER_REFRESH); //force refreshing
                }
            });
        }
        else if(e.getSource() == jbPrevious){  //previous : show a better cover
            index++;
            if (index > alCovers.size()-1){
                index = 0;
            }
            new Thread(){
                public void run(){
                    displayCurrentCover();
                }
            }.start();
        }
        else if(e.getSource() == jbNext){ //next : show a worse cover
            index--;
            if (index < 0){
                index = alCovers.size()-1;
            }
            new Thread(){
                public void run(){
                    displayCurrentCover();
                }
            }.start();
        }
        else if(e.getSource() == jbDelete){ //delete a local cover
            Cover cover = (Cover)alCovers.get(index);
            //show confirmation message if required
            if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_COVER)){
                int iResu = JOptionPane.showConfirmDialog(Main.getWindow(),Messages.getString("Confirmation_delete_cover")+" : "+cover.getURL().toString(),Messages.getString("Main.21"),JOptionPane.YES_NO_OPTION);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (iResu == JOptionPane.NO_OPTION){
                    return;
                }
            }
            //yet there? ok, delete the cover
            try{
                File file = new File(cover.getURL().getFile());
                if (file.isFile() && file.exists()){
                    file.delete();
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
                Directory dir = FIFO.getInstance().getCurrentFile().getDirectory(); 
                dir.removeProperty("default_cover"); //$NON-NLS-1$
            }
            // reorganize covers
            alCovers.remove(index);
            index--;
            if (index < 0){
                index = alCovers.size()-1;
            }
            new Thread(){
                public void run(){
                    displayCurrentCover();
                }
            }.start();
        }
        else if ( e.getSource() == jbDefault){ //choose a default
            //first commit this cover on the disk if it is a remote cover
            Cover cover = (Cover)alCovers.get(index);
            String sFilename = Util.getOnlyFile(cover.getURL().toString());
            if (cover.getType() == Cover.REMOTE_COVER){
                String sFilePath = fDir.getPath()+"/"+sFilename; //$NON-NLS-1$
                saveCover(sFilePath,cover);
            }
            //then make it the default cover in this directory
            Directory dir = FIFO.getInstance().getCurrentFile().getDirectory(); 
            dir.setProperty("default_cover",sFilename); //$NON-NLS-1$
        }
        else if(e.getSource() == jbSave || e.getSource() == jbSaveAs ){ //save a save with its original name
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Cover cover = (Cover)alCovers.get(index);
                    if (cover.getData() == null){ //means it is a default or local cover
                        Messages.showErrorMessage("130",cover.getURL().toString()); //$NON-NLS-1$
                        return;
                    }
                    String sFilePath = null;
                    if (e.getSource() == jbSave){
                        sFilePath = fDir.getPath()+"/"+Util.getOnlyFile(cover.getURL().toString()); //$NON-NLS-1$
                    }
                    else if(e.getSource() == jbSaveAs){
                        JFileChooser jfchooser = new JFileChooser(fDir);
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
                        jfchooser.setSelectedFile(new File(Util.getOnlyFile(cover.getURL().toString())));
                        int returnVal = jfchooser.showSaveDialog(Main.getWindow());
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            sFilePath = jfchooser.getSelectedFile().getAbsolutePath();
                        }
                        else{
                            return;
                        }
                    }
                    saveCover(sFilePath,cover);
                }
            });
        }
    }
    
    
    /**
     * Save a cover on disk 
     * @param sFilePath URL of the futur file
     * @param cover Jajuk cover to be saved
     */
    private void saveCover(String sFilePath,Cover cover){
        Util.waiting();
        File file = new File(sFilePath);
        try{
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(cover.getData());
            bos.flush();
            bos.close();
            InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
        }
        catch(Exception ex){
            Log.error("024",ex); //$NON-NLS-1$
            Messages.showErrorMessage("024"); //$NON-NLS-1$
        }
        finally{
            Util.stopWaiting();
        }
        
    }
    
    /**
     * 
     * @return number of real covers ( not default) covers found
     */ 
    private int getCoverNumber(){
        if (alCovers.size()>0 && ((Cover)alCovers.get(0)).getType() == Cover.DEFAULT_COVER){
            return alCovers.size() -1;
        }
        else{
            return alCovers.size() ;
        }
    }
    
    /**
     * 
     * @param file
     * @return an accurate  google search query for a file
     */
    public  String createQuery(org.jajuk.base.File file){
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
    
    
}
