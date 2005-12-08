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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.jajuk.base.Album;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Filter;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import ext.FlowScrollPanel;
import ext.SwingWorker;

/**
 *  Catalog view. Displays all defaut covers by album
 * <p>Catalog perspectives
 * @author     Bertrand Florat
 * @created   01/12/2005
 */
public class CatalogView extends ViewAdapter implements Observer,ComponentListener,ActionListener,ITechnicalStrings{
    
    //control panel
    JPanel jpControl;
    JLabel jlSorter;
    JComboBox jcbSorter;
    JLabel jlFilter;
    JComboBox jcbFilter;
    JLabel jlContains;
    JTextField jtfValue;
    JCheckBox jcbShow;
    JLabel jlSize;
    JComboBox jcbSize;
    JButton jbRefresh;
    FlowScrollPanel jpItems;
    JScrollPane jsp;
    
    /**Filter properties*/
    ArrayList<PropertyMetaInformation> alFilters;
    
    /**Sorter properties*/
    ArrayList<PropertyMetaInformation> alSorters;
    
    /**Do search panel need a search*/
    private boolean bNeedSearch = false;
    
    /**Default time in ms before launching a search automaticaly*/
    private static final int WAIT_TIME = 300;
    
    /**Date last key pressed*/
    private long lDateTyped;
    
    /** Swing Timer to refresh the component*/ 
    private Timer timer = new Timer(WAIT_TIME,new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if ( bNeedSearch && (System.currentTimeMillis()-lDateTyped >= WAIT_TIME)){
                jtfValue.setEnabled(false);
                jcbSorter.setEnabled(false);
                jcbFilter.setEditable(false);
                populateCatalog();
                bNeedSearch = false;
                jtfValue.setEnabled(true);
                jcbSorter.setEnabled(true);
                jcbFilter.setEditable(true);
                jtfValue.requestFocusInWindow();
                
            }
        }
    });
    
    /**
     * Constructor
     */
    public CatalogView() {
        alFilters = new ArrayList(10);
        alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_STYLE));
        alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_AUTHOR));
        alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_ALBUM));
        alFilters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_YEAR));
        
        alSorters = new ArrayList(10);
        alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_STYLE));
        alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_AUTHOR));
        alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_ALBUM));
        alSorters.add(TrackManager.getInstance().getMetaInformation(XML_TRACK_YEAR));
        
        timer.start();
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#display()
     */
    public void populate(){
        //Control panel
        jpControl = new JPanel();
        jpControl.setBorder(BorderFactory.createEtchedBorder());
        int iXspace = 10;
        double sizeControl[][] =
            //        Sort by                       combo sorter                    Filter:                       combo filter                  contains:                     value textfield               show albums without covers       Size:                      Size combo                    refresh                          
        {{iXspace,TableLayout.PREFERRED,iXspace,TableLayout.PREFERRED,3*iXspace,TableLayout.PREFERRED,iXspace,TableLayout.PREFERRED,iXspace,TableLayout.PREFERRED,iXspace,TableLayout.PREFERRED,iXspace,TableLayout.PREFERRED,iXspace,TableLayout.PREFERRED,iXspace,TableLayout.PREFERRED,TableLayout.FILL,TableLayout.PREFERRED,2*iXspace},
                {25}};
        jpControl.setLayout(new TableLayout(sizeControl));
        jlSorter = new JLabel(Messages.getString("CatalogView.1"));
        jcbSorter = new JComboBox();
        //note that a single album can contains tracks with different authors or styles, we will show it only one
        for (PropertyMetaInformation meta:alSorters){
            jcbSorter.addItem(meta.getHumanName());
        }
        jcbSorter.setSelectedIndex(0);
        jcbSorter.addActionListener(this);
        
        jlFilter = new JLabel(Messages.getString("AbstractTableView.0"));
        jlContains = new JLabel(Messages.getString("AbstractTableView.7"));
        jcbFilter = new JComboBox();
        //note that a single album can contains tracks with different authors or styles, we will show it only one
        for (PropertyMetaInformation meta:alFilters){
            jcbFilter.addItem(meta.getHumanName());
        }
        jcbFilter.setSelectedIndex(0);
        jcbFilter.addActionListener(this);
        jtfValue = new JTextField(20);
        jtfValue.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                bNeedSearch = true;
                lDateTyped = System.currentTimeMillis();  
            }
        });
        
        jcbShow = new JCheckBox(Messages.getString("CatalogView.2"));
        jcbShow.setSelected(ConfigurationManager.getBoolean(CONF_THUMBS_SHOW_WITHOUT_COVER));
        jcbShow.addActionListener(this);
        
        jlSize = new JLabel(Messages.getString("CatalogView.4"));
        jcbSize = new JComboBox();
        jcbSize.addItem(THUMBNAIL_SIZE_50x50);
        jcbSize.addItem(THUMBNAIL_SIZE_100x100);
        jcbSize.addItem(THUMBNAIL_SIZE_150x150);
        jcbSize.addItem(THUMBNAIL_SIZE_200x200);
        jcbSize.setSelectedItem(ConfigurationManager.getProperty(CONF_THUMBS_SIZE));
        jcbSize.addActionListener(this);
        
        jbRefresh = new JButton(Util.getIcon(ICON_REFRESH));
        jbRefresh.setToolTipText(Messages.getString("CatalogView.3"));
        jbRefresh.addActionListener(this);
        
        jpControl.add(jlSorter,"1,0");//$NON-NLS-1$
        jpControl.add(jcbSorter,"3,0");//$NON-NLS-1$
        jpControl.add(jlFilter,"5,0");//$NON-NLS-1$
        jpControl.add(jcbFilter,"7,0");//$NON-NLS-1$
        jpControl.add(jlContains,"9,0");//$NON-NLS-1$
        jpControl.add(jtfValue,"11,0");//$NON-NLS-1$
        jpControl.add(jcbShow,"13,0");//$NON-NLS-1$
        jpControl.add(jlSize,"15,0");//$NON-NLS-1$
        jpControl.add(jcbSize,"17,0");//$NON-NLS-1$
        jpControl.add(jbRefresh,"19,0");//$NON-NLS-1$
        
        //Covers
        jpItems = new FlowScrollPanel();
        Dimension dim = new Dimension(getWidth(),getHeight());
        jpItems.setPreferredSize(dim);
        jsp = new JScrollPane(jpItems,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jpItems.setScroller(jsp);
        jpItems.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        //global layout
        double size[][] =
        {{0.99},
                {30,10,0.99}};
        setLayout(new TableLayout(size));
        add(jpControl,"0,0"); //$NON-NLS-1$
        add(jsp,"0,2");
        
        populateCatalog();
        
        //subscriptions to events
        ObservationManager.register(EVENT_DEVICE_REFRESH,this);
    }
    
    /**
     * Make thumbnail file exists (album id.jpg or.gif or .png) in thumbs directory
     * if it doesn't exist yet 
     * @param album
     */
    private void refreshThumbnail(Album album){
        File fThumb = new File(FILE_THUMBS+'/'+(String)jcbSize.getSelectedItem()+'/'+album.getId()+'.'+EXT_THUMB);
        if (!fThumb.exists()){
            InformationJPanel.getInstance().setMessage(Messages.getString("CatalogView.5")+' '+album.getName2(),InformationJPanel.INFORMATIVE);
            File fDir = null; //analyzed directory
            //search for local covers in all directories mapping the current track to reach other devices covers and display them together
            ArrayList<Track> alTracks = TrackManager.getInstance().getAssociatedTracks(album);
            if (alTracks.size() == 0){
                return;
            }
            Track trackCurrent = alTracks.get(0); //take first track found to get associated directories as we assume all tracks for an album are in the same directory 
            File fCover = trackCurrent.getAlbum().getCoverFile();
            if (fCover == null){
                try {
                    //use void file to store the fact we didn't find a cover, too long to scan again
                    fThumb.createNewFile();
                }
                catch (Exception e) {
                    Log.error(e);
                }
            }
            else{
                try {
                    Util.createThumbnail(fCover.toURL(),fThumb,100+(50*jcbSize.getSelectedIndex()));
                }
                catch (Exception e) {
                    Log.error(e);
                }
            }
        }
    }
    
    
    /**
     * Populate the catalog
     *
     */
    private void populateCatalog(){
        SwingWorker sw = new SwingWorker() {
            @Override
            public Object construct() {
                Util.waiting();
                String sMessageOld = InformationJPanel.getInstance().getMessage();
                int iMessageOld = InformationJPanel.getInstance().getMessageType();
                //remove all devices
                if (jpItems.getComponentCount() > 0 ){
                    jpItems.removeAll();
                }
                Filter filter = null;
                if (jtfValue.getText().length() > 0){
                    PropertyMetaInformation meta = alFilters.get(jcbFilter.getSelectedIndex());
                    filter = new Filter(meta,jtfValue.getText(),true,false);
                }
                //filter on tracks properties
                Collection<IPropertyable> alAllTracks = TrackManager.getInstance().getItems(filter);
                //keep matching albums (we use sets to drop doublons)
                HashSet<IPropertyable> hsAllAlbums = new HashSet();
                for (IPropertyable item:alAllTracks){
                    Track track = (Track)item;
                    hsAllAlbums.add(track.getAlbum());
                }
                for (IPropertyable item:hsAllAlbums){
                    Album album = (Album)item;
                    //if hide unmounted tracks is set, continue
                    if (ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)){
                        //test if album contains at least one mounted file
                        ArrayList<Track> alTracks = TrackManager.getInstance().getAssociatedTracks(album);
                        if (alTracks.size() > 0){
                            int count = 0;
                            for (Track track:alTracks){
                                if (track.getReadyFiles().size() > 0){
                                    count ++;
                                }
                            }
                            if (count == 0){
                                continue;
                            }
                        }
                        else{
                            continue;
                        }
                    }
                    //make sure thumbnail exists
                    refreshThumbnail(album);
                    CatalogItem cover = new CatalogItem(album,(String)jcbSize.getSelectedItem());
                    if (cover.isNoCover()){
                        if (jcbShow.isSelected()){
                            jpItems.add(cover);
                        }
                    }
                    else{
                        jpItems.add(cover);
                    }
                }
                //reset message dialog, OK I know that the message can change during long actions but...
                InformationJPanel.getInstance().setMessage(sMessageOld,iMessageOld);
                Util.stopWaiting();
                return null;
            }
            @Override
            public void finished() {
                jsp.revalidate();
                jsp.repaint();
            }
        };
        sw.start();  
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(Event event){
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getDesc()
     */
    public String getDesc() {
        return "CatalogView.0";    //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getID()
     */
    public String getID() {
        return "org.jajuk.ui.views.CatalogView"; //$NON-NLS-1$
    }
    
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
        if (e.getSource() == jcbFilter){
             bNeedSearch = true;
             lDateTyped = System.currentTimeMillis();  
        }
        else if (e.getSource() == jcbSorter){
             bNeedSearch = true;
             lDateTyped = System.currentTimeMillis();  
        }
        else if (e.getSource() == jbRefresh){
            cleanThumbs("50x50");
            cleanThumbs("100x100");
            cleanThumbs("150x150");
            cleanThumbs("200x200");
            
            //display thumbs
            populateCatalog();
        }
        else if (e.getSource() == jcbShow){
            ConfigurationManager.setProperty(CONF_THUMBS_SHOW_WITHOUT_COVER,Boolean.toString(jcbShow.isSelected()));
            //display thumbs
            populateCatalog();
        }
        else if (e.getSource() == jcbSize){
            ConfigurationManager.setProperty(CONF_THUMBS_SIZE,(String)jcbSize.getSelectedItem());
            //display thumbs
            populateCatalog();
            
        }
    }
    
    private void cleanThumbs(String size){
        File fThumb = new File(FILE_THUMBS+'/'+size);
        if (fThumb.exists()){
            File[] files = fThumb.listFiles();
            for (File file:files){
                if (!file.getAbsolutePath().matches(FILE_THUMB_NO_COVER)){
                    file.delete();
                }
            }
        }
    }
    
}

class CatalogItem extends JPanel implements ITechnicalStrings,ActionListener,MouseListener{
    
    /** Associated album*/
    Album album;
    
    /**Size*/
    String size;
    
    /**Associated file*/
    File fCover;
    
    /**No cover flag*/
    boolean bNoCover = false;
    
    JPanel jpIcon;
    JLabel jlIcon;
    JLabel jlAuthor;
    JLabel jlAlbum;
    
    /**
     * Constructor
     * @param album : associated album
     * @param size : size of the thumbnail
     */
    public CatalogItem(Album album,String size){
        this.album = album;
        this.size = size;
        this.fCover = new File(FILE_THUMBS+'/'+size+'/'+album.getId()+'.'+EXT_THUMB);
        if (!fCover.exists() || fCover.length() == 0){
            bNoCover = true;
            this.fCover = new File(FILE_THUMBS+'/'+size+'/'+FILE_THUMB_NO_COVER);
        }
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        jpIcon  = new JPanel();
        jpIcon.setLayout(new BoxLayout(jpIcon,BoxLayout.X_AXIS));
        jlIcon = new JLabel(new ImageIcon(fCover.getAbsolutePath()));
        jlIcon.addMouseListener(this);
        jpIcon.add(Box.createGlue());
        jpIcon.add(jlIcon);
        jpIcon.add(Box.createGlue());
        add(jpIcon);
        //take first track author as author
        String sAuthor = "";
        ArrayList<Track> alTracks = TrackManager.getInstance().getAssociatedTracks(album);
        if (alTracks.size() > 0){
            sAuthor = alTracks.get(0).getAuthor().getName2();
        }
        jlAuthor = new JLabel(sAuthor);
        jlAlbum = new JLabel(album.getName2());
        jlAuthor.setFont(new Font("Dialog",Font.PLAIN,12)); //$NON-NLS-1$
        jlAlbum.setFont(new Font("Dialog",Font.PLAIN,12)); //$NON-NLS-1$
        
        add(Util.getCentredPanel(jlAuthor));
        add(Util.getCentredPanel(jlAlbum));
        add(Box.createVerticalGlue());
    }
    
    public boolean isNoCover() {
        return bNoCover;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent arg0) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent arg0) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {
        if (e.getSource() == jlIcon){
            ArrayList<Track> alTracks = TrackManager.getInstance().getAssociatedTracks(album);
            //compute selection
            ArrayList alFilesToPlay = new ArrayList(alTracks.size());
            Iterator it = alTracks.iterator();
            while ( it.hasNext()){
                org.jajuk.base.File file = ((Track)it.next()).getPlayeableFile();
                if ( file != null){
                    alFilesToPlay.add(file);
                }
            }
            FIFO.getInstance().push(Util.createStackItems(alFilesToPlay,false,true),false);
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent arg0) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent arg0) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {
    }
}
