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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import layout.TableLayout;

import org.jajuk.Main;
import org.jajuk.base.Cover;
import org.jajuk.base.FIFO;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
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
    JButton jbSave;
    JButton jbSaveAs;
    JButton jbDefault;
    JLabel jlSize;
    JLabel jlFound;
    
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
    
    /**
     * Constructor
     */
    public CoverView() {
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
        int iXspace = 5;
        double sizeControl[][] =
        {{20,iXspace,20,2*iXspace,20,iXspace,20,2*iXspace,20,2*iXspace,0.50,2*iXspace,0.50},
                {25}};
        jpControl.setLayout(new TableLayout(sizeControl));
        jbPrevious = new JButton(Util.getIcon(ICON_PREVIOUS));
        jbPrevious.addActionListener(this);
        jbPrevious.setToolTipText(Messages.getString("CoverView.4")); //$NON-NLS-1$
        jbNext = new JButton(Util.getIcon(ICON_NEXT));
        jbNext.addActionListener(this);
        jbNext.setToolTipText(Messages.getString("CoverView.5")); //$NON-NLS-1$
        jbSave = new JButton(Util.getIcon(ICON_SAVE));
        jbSave.addActionListener(this);
        jbSave.setToolTipText(Messages.getString("CoverView.6")); //$NON-NLS-1$
        jbSaveAs = new JButton(Util.getIcon(ICON_SAVE_AS));
        jbSaveAs.addActionListener(this);
        jbSaveAs.setToolTipText(Messages.getString("CoverView.7")); //$NON-NLS-1$
        jbDefault = new JButton(Util.getIcon(ICON_DEFAULT_COVER));
        jbDefault.addActionListener(this);
        jbDefault.setToolTipText(Messages.getString("CoverView.8")); //$NON-NLS-1$
        jlSize = new JLabel("");
        jlFound = new JLabel("");
        jpControl.add(jbPrevious,"0,0");//$NON-NLS-1$
        jpControl.add(jbNext,"2,0");//$NON-NLS-1$
        jpControl.add(jbSave,"4,0");//$NON-NLS-1$
        jpControl.add(jbSaveAs,"6,0");//$NON-NLS-1$
        jpControl.add(jbDefault,"8,0");//$NON-NLS-1$
        jpControl.add(Util.getCentredPanel(jlSize,BoxLayout.X_AXIS),"10,0");//$NON-NLS-1$
        jpControl.add(Util.getCentredPanel(jlFound,BoxLayout.X_AXIS),"12,0");//$NON-NLS-1$
        
        ObservationManager.register(EVENT_COVER_REFRESH,this);
        ObservationManager.register(EVENT_PLAYER_STOP,this);
        
        try {
            //check if the cover should be refreshed at startup
            coverDefault = new Cover(new URL(IMAGES_SPLASHSCREEN),Cover.DEFAULT_COVER); //instanciate default cover
        } catch (MalformedURLException e) {
            Log.error(e);
        }
        synchronized(alCovers){
            alCovers.add(coverDefault); //add the default cover
        }
        update(EVENT_COVER_REFRESH);
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public synchronized void update(String subject){
        if (bAlreadyRefreshing){ //another refreshing is probably blocked until time out by a web search
            return;
        }
        bAlreadyRefreshing = true;
        try{
            if ( EVENT_COVER_REFRESH.equals(subject)){
                final org.jajuk.base.File fCurrent = FIFO.getInstance().getCurrentFile();
                //if current file is null ( probably a file cannot be read ) 
                if ( fCurrent == null){
                    displayCurrentCover(); //do nothing
                    return; 
                }
                java.io.File fDir = new java.io.File(fCurrent.getAbsolutePath()).getParentFile();
                if ( !fDir.exists() || (this.fDir!= null && this.fDir.equals(fDir)) ){  //if we are always in the same directory, just leave to save cpu
                    return;
                }
                this.fDir = fDir; //store this dir
                synchronized(alCovers){
                    alCovers.clear();
                    //search for local covers
                    java.io.File[] files = fDir.listFiles();
                    boolean bAbsoluteCover = false; //whether an absolute cover ( unique) has been found
                    for (int i=0;i<files.length;i++){
                        String sExt = Util.getExtension(files[i]);
                        if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png") || sExt.equalsIgnoreCase("gif")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            if (Util.isAbsoluteDefaultCover(files[i].getPath())){
                                if (!bAbsoluteCover){ //the concidere only the first found absolute cover
                                    alCovers.add(new Cover(files[i].toURL(),Cover.ABSOLUTE_DEFAULT_COVER));
                                    bAbsoluteCover = true;
                                }
                                else{
                                    continue;
                                }
                            }
                            else{ //normal local cover
                                alCovers.add(new Cover(files[i].toURL(),Cover.LOCAL_COVER));
                            }
                        }
                    }
                   if (alCovers.size() == 0){//add the default cover if none other cover has been found
                       alCovers.add(coverDefault); 
                   }
                    //display local or default cover without wait
                    Collections.sort(alCovers); //sort the list
                    Log.debug("Local cover list: "+alCovers);
                    index = alCovers.size()-1;  //current index points to the best available cover
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jlFound.setText(getCoverNumber()+" "+Messages.getString("CoverView.9"));
                        }
                    });
                }
                displayCurrentCover(); //display in advance without waiting for web search to accelerate local covers display
                //then we search for web covers asynchronously
                if (ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER)){
                    if ( DownloadManager.isActiveConnection()){ //if a download was yet being done, tell the download manager to trash it and leave
                        DownloadManager.setConcurrentConnection(true);
                    }
                    else{
                        new Thread(){ //start search asynchronously
                            public void run() {
                                synchronized(alCovers){
                                    int iCoversBeforeSearch = alCovers.size(); //stores number of covers before web search
                                    final String sQuery = Util.createQuery(fCurrent);
                                    Log.debug("Query="+sQuery);
                                    if (!sQuery.equals("")){ //there is not enough information in tags for a web search
                                        final ArrayList alUrls;
                                        try{
                                            alUrls = DownloadManager.getRemoteCoversList(sQuery);
                                            Collections.reverse(alUrls); //set best results to be displayed  first
                                        }
                                        catch(Exception e){
                                            return; //no web covers found, just leave
                                        }
                                        //remove default cover if some remote cover have been found
                                        if ( alCovers.size()>0 && ((Cover)alCovers.get(0)).getType() == Cover.DEFAULT_COVER){
                                            alCovers.remove(0);
                                        }
                                        Iterator it = alUrls.iterator(); //add logicaly found covers 
                                        while ( it.hasNext()){
                                            URL url = (URL)it.next();
                                            Log.debug("Found Cover: "+url.toString());
                                            Cover cover = null;
                                            cover = new Cover(url,Cover.REMOTE_COVER); //create a cover with given url ( image will be really downloaded when required)
                                            alCovers.add(cover);
                                        }
                                        index +=alUrls.size(); //reset index
                                        long lTime = System.currentTimeMillis();
                                        Collections.sort(alCovers); //sort the list again with new covers
                                        Log.debug("Global cover list: "+alCovers+" sorted in: "+(System.currentTimeMillis()-lTime));
                                        //refresh number of found covers
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                jlFound.setText(getCoverNumber()+" "+Messages.getString("CoverView.9"));
                                            }
                                        });
                                        //display the best found cover if no one was found before
                                        if (iCoversBeforeSearch == 1 && alCovers.size() >1){
                                            index = alCovers.size()-1;  //current index points to the best available cover
                                            displayCurrentCover();    
                                        }
                                    }
                                }
                            }
                        }.start();
                    }
                } 
            }
            else if ( EVENT_PLAYER_STOP.equals(subject)){
                clearFoundCover();
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
    
    /**Clear the found covers label*/
    private void clearFoundCover(){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jlFound.setText("");
                jlSize.setText("");
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
        Log.debug("Cover resized");
        long lCurrentDate = System.currentTimeMillis();  //adjusting code
        if ( lCurrentDate - lDateLastResize < 500){  //display image every 500 ms to save CPU
            lDateLastResize = lCurrentDate;
            return;
        }
        new Thread(){
            public void run(){
                displayCurrentCover();
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
            Log.debug("display index: "+index);
            //find next OK cover
            ImageIcon icon = null;
            Cover cover = null; 
            while ( index >= 0){//there are some more covers after
                try{
                    setCursor(Util.WAIT_CURSOR);
                    cover = (Cover)alCovers.get(index); 
                    icon = cover.getImage();
                    setCursor(Util.DEFAULT_CURSOR);
                    break;
                }
                catch(Exception e){ //this cover cannot be loaded
                    synchronized(alCovers){
                        alCovers.remove(index);
                        //Add at last the default cover if all remote cover has been discarded
                        if (alCovers.size() == 0){
                            alCovers.add(0,coverDefault);
                        }
                        Log.debug("Removed cover: "+cover);
                    }
                    //refresh number of found covers
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            jlFound.setText(jlFound+" "+Messages.getString("CoverView.9"));
                        }
                    });
                    index  --; //look at next cover    
                }
            }
            if (icon == null){ //none available cover
                return;
            }
            JInternalFrame ji = ViewManager.getFrame(this);
            ImageFilter filter = new AreaAveragingScaleFilter(ji.getWidth()-8,ji.getHeight()-60);
            Image img = createImage(new FilteredImageSource(icon.getImage().getSource(),filter));
            jl = new JLabel(new ImageIcon(img));
            final URL url = cover.getURL();
            final byte[] bData = cover.getData();
            if (url != null){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        int iSize = 0;
                        String sType = " (L)"; //local cover
                        if (bData != null){
                            sType = " (@)"; //Web cover
                            iSize = (int)(Math.ceil((double)bData.length/1024));
                        }
                        else{
                            iSize = (int)(Math.ceil((double)new File(url.getFile()).length()/1024));
                        }
                        jl.setToolTipText("<html>"+url.toString()+"<br>"+iSize+"K");
                        jlSize.setText(iSize+"K"+sType);
                    }
                });
            }
            //set tooltip for previous and next track
            int indexPrevious  = index+1;
            if (indexPrevious > alCovers.size()-1){
                indexPrevious = 0;
            }
            final URL urlPrevious = ((Cover)alCovers.get(indexPrevious)).getURL();
            if (urlPrevious != null){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jbPrevious.setToolTipText("<html>"+Messages.getString("CoverView.4")+"<br>"+urlPrevious.toString()+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                });
            }
            int indexNext = index-1;
            if (indexNext < 0){
                indexNext = alCovers.size()-1;
            }
            final URL urlNext = ((Cover)alCovers.get(indexNext)).getURL();
            if (urlNext != null){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        jbNext.setToolTipText("<html>"+Messages.getString("CoverView.5")+"<br>"+urlNext.toString()+"</html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    }
                });
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setCursor(Util.WAIT_CURSOR);
                    removeAll();
                    add(jpControl,"0,0");//$NON-NLS-1$
                    add(jl,"0,1");//$NON-NLS-1$
                    SwingUtilities.updateComponentTreeUI(CoverView.this.getRootPane());//refresh
                    setCursor(Util.DEFAULT_CURSOR);
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
        if(e.getSource() == jbPrevious){  //previous : show a better cover
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
        else if(e.getSource() == jbSave || e.getSource() == jbSaveAs || e.getSource() == jbDefault){ //save a save with its original name
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Cover cover = (Cover)alCovers.get(index);
                    if (cover.getData() == null){ //means it is a default or local cover
                        Messages.showErrorMessage("130",cover.getURL().toString());
                        return;
                    }
                    String sFilePath = null;
                    if (e.getSource() == jbSave){
                        sFilePath = fDir.getPath()+"/"+Util.getOnlyFile(cover.getURL().toString());
                    }
                    else if(e.getSource() == jbSaveAs){
                        JFileChooser jfchooser = new JFileChooser(fDir);
                        FileFilter filter = new FileFilter() {
                            public boolean accept(File file) {
                                String sExt =Util.getExtension(file); 
                                if (sExt.equals("gif") || sExt.equals("png") || sExt.equals("jpg")){
                                    return true;
                                }
                                return false;
                            }
                            
                            public String getDescription() {
                                return "*.gif,*.png,*.jpg";
                            }
                        };
                        jfchooser.setFileFilter(filter);
                        jfchooser.setDialogTitle(Messages.getString("CoverView.10"));
                        jfchooser.setSelectedFile(new File(Util.getOnlyFile(cover.getURL().toString())));
                        int returnVal = jfchooser.showSaveDialog(Main.getWindow());
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            sFilePath = jfchooser.getSelectedFile().getAbsolutePath();
                        }
                        else{
                            return;
                        }
                    }
                    else if (e.getSource() == jbDefault){
                        sFilePath = fDir.getPath()+"/"+FILE_ABSOLUTE_DEFAULT_COVER+Util.getExtension(new File(Util.getOnlyFile(cover.getURL().toString())));
                    }
                    Util.waiting();
                    File file = new File(sFilePath);
                    try{
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                        bos.write(cover.getData());
                        bos.flush();
                        bos.close();
                        InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),InformationJPanel.INFORMATIVE);
                    }
                    catch(Exception ex){
                        Log.error("024",ex);
                        Messages.showErrorMessage("024");
                    }
                    finally{
                        Util.stopWaiting();
                    }
                }
            });
        }
    }
   
   /**
    * 
    * @return number of real covers ( not default) covers found
    */ 
    private int getCoverNumber(){
        if (((Cover)alCovers.get(0)).getType() == Cover.DEFAULT_COVER){
            return alCovers.size() -1;
        }
        else{
            return alCovers.size() ;
        }
    }
    
}
