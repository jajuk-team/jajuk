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

import java.awt.Color;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.Cover;
import org.jajuk.base.Directory;
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
import org.jajuk.ui.OKCancelPanel;
import org.jajuk.ui.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
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
    
    JPopupMenu jmenu;
    JMenuItem jmiAlbumPlay;
    JMenuItem jmiAlbumPush;
    JMenuItem jmiAlbumPlayShuffle;
    JMenuItem jmiAlbumPlayRepeat;
    JMenuItem jmiGetCovers;
    JMenuItem jmiAlbumProperties;
    
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
    
    /**Last selected item*/
    public CatalogItem item;
    
    /** Swing Timer to refresh the component*/ 
    private Timer timer = new Timer(WAIT_TIME,new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if ( bNeedSearch && (System.currentTimeMillis()-lDateTyped >= WAIT_TIME)){
                jtfValue.setEnabled(false);
                jcbSorter.setEnabled(false);
                jcbFilter.setEnabled(false);
                populateCatalog();
                bNeedSearch = false;
                jtfValue.setEnabled(true);
                jcbSorter.setEnabled(true);
                jcbFilter.setEnabled(true);
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
        jcbSorter.setEditable(false);
        //note that a single album can contains tracks with different authors or styles, we will show it only one
        for (PropertyMetaInformation meta:alSorters){
            jcbSorter.addItem(meta.getHumanName());
        }
        jcbSorter.setSelectedIndex(ConfigurationManager.getInt(CONF_THUMBS_SORTER));
        jcbSorter.addActionListener(this);
        
        jlFilter = new JLabel(Messages.getString("AbstractTableView.0"));
        jlContains = new JLabel(Messages.getString("AbstractTableView.7"));
        jcbFilter = new JComboBox();
        jcbFilter.setEditable(false);
        //note that a single album can contains tracks with different authors or styles, we will show it only one
        for (PropertyMetaInformation meta:alFilters){
            jcbFilter.addItem(meta.getHumanName());
        }
        jcbSorter.setSelectedIndex(ConfigurationManager.getInt(CONF_THUMBS_FILTER));
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
        jsp.setBackground(Color.WHITE);
        
        //Menu items
        //Album menu
        jmenu = new JPopupMenu();
        jmiAlbumPlay = new JMenuItem(Messages.getString("LogicalTreeView.15")); //$NON-NLS-1$
        jmiAlbumPlay.addActionListener(this);
        jmiAlbumPush = new JMenuItem(Messages.getString("LogicalTreeView.16")); //$NON-NLS-1$
        jmiAlbumPush.addActionListener(this);
        jmiAlbumPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.17")); //$NON-NLS-1$
        jmiAlbumPlayShuffle.addActionListener(this);
        jmiAlbumPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.18")); //$NON-NLS-1$
        jmiAlbumPlayRepeat.addActionListener(this);
        jmiGetCovers = new JMenuItem(Messages.getString("CatalogView.7")); //$NON-NLS-1$        
        jmiGetCovers.addActionListener(this);
        jmiAlbumProperties = new JMenuItem(Messages.getString("LogicalTreeView.21")); //$NON-NLS-1$
        jmiAlbumProperties.addActionListener(this);
        jmenu.add(jmiAlbumPlay);
        jmenu.add(jmiAlbumPush);
        jmenu.add(jmiAlbumPlayShuffle);
        jmenu.add(jmiAlbumPlayRepeat);
        jmenu.add(jmiGetCovers);
        jmenu.add(jmiAlbumProperties);
        
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
                //keep matching albums (we use sets to drop duplicates)
                ArrayList<Album> albums = new ArrayList();
                for (IPropertyable item:alAllTracks){
                    Track track = (Track)item;
                    Album album = track.getAlbum();
                    if (!albums.contains(album)){
                        albums.add(album);
                    }
                }
                //sort albums
                final int index = jcbSorter.getSelectedIndex();
                //store mapped tracks for perfs
                final HashMap<Album,Track> hmAlbumTrack = new HashMap();
                for (Album album:albums){
                    Track track = null;
                    for (IPropertyable item : TrackManager.getInstance().getItems()){
                        track = (Track)item;
                        if (track.getAlbum().equals(album)){
                            hmAlbumTrack.put(album,track);
                            break;
                        }
                    }
                    hmAlbumTrack.put(album,track);
                }
                
                Collections.sort(albums,new Comparator() {
                    
                    public int compare(Object arg0, Object arg1) {
                        Album album1 = (Album)arg0;
                        Album album2 = (Album)arg1;
                        //for albums, perform a fast compare
                        if (index == 2){
                            return album1.compareTo(album2);
                        }
                        //get a track for each album
                        Track track1 = hmAlbumTrack.get(album1);
                        Track track2 = hmAlbumTrack.get(album2);
                        //check tracks (normaly useless)
                        if (track1 == null || track2 == null){
                            return 0;
                        }
                        switch (index){
                        case 0: //style
                            return track1.getStyle().compareTo(track2.getStyle());
                        case 1: //author
                            return track1.getAuthor().compareTo(track2.getAuthor());
                        case 3: //year
                            return (int)(track1.getYear() - track2.getYear());    
                        }
                        return 0;
                    }
                    
                });
                for (Object item:albums){
                    Album album = (Album)item;
                    //if hide unmounted tracks is set, continue
                    if (ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED)){
                        //test if album contains at least one mounted file
                        ArrayList<Track> alTracks = TrackManager.getInstance().getAssociatedTracks(album);
                        if (alTracks.size() > 0){
                            boolean bOK = false;
                            for (Track track:alTracks){
                                if (track.getReadyFiles().size() > 0){
                                    bOK = true;
                                    break;
                                }
                            }
                            if (!bOK){
                                continue;
                            }
                        }
                        else{
                            continue;
                        }
                    }
                    //make sure thumbnail exists
                    refreshThumbnail(album);
                    CatalogItem cover = new CatalogItem(album,(String)jcbSize.getSelectedItem(),hmAlbumTrack.get(album));
                    
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
            ConfigurationManager.setProperty(CONF_THUMBS_FILTER,Integer.toString(jcbFilter.getSelectedIndex()));
        }
        else if (e.getSource() == jcbSorter){
            bNeedSearch = true;
            lDateTyped = System.currentTimeMillis();
            ConfigurationManager.setProperty(CONF_THUMBS_SORTER,Integer.toString(jcbSorter.getSelectedIndex()));
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
        else{
            this.item.actionPerformed(e);
        }
    }
    
    private void cleanThumbs(String size){
        File fThumb = new File(FILE_THUMBS+'/'+size);
        if (fThumb.exists()){
            File[] files = fThumb.listFiles();
            for (File file:files){
                if (!file.getAbsolutePath().matches(".*"+FILE_THUMB_NO_COVER)){
                    file.delete();
                }
            }
        }
    }
    class CatalogItem extends JPanel implements ITechnicalStrings,ActionListener,MouseListener{
        
        /** Associated album*/
        Album album;
        
        /** Addociated track */
        Track track;
        
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
        public CatalogItem(Album album,String size,Track track){
            this.album = album;
            this.size = size;
            this.track = track;
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
            jlAuthor = new JLabel(track.getAuthor().getName2());
            jlAlbum = new JLabel(album.getName2());
            jlAuthor.setFont(new Font("Dialog",Font.PLAIN,12)); //$NON-NLS-1$
            jlAlbum.setFont(new Font("Dialog",Font.PLAIN,12)); //$NON-NLS-1$
            
            add(Util.getCentredPanel(jlAuthor));
            add(Util.getCentredPanel(jlAlbum));
            add(Box.createVerticalGlue());
            
            setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        }
        
        public boolean isNoCover() {
            return bNoCover;
        }
        
        private void play(boolean bRepeat,boolean bShuffle,boolean bPush){
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
            if (bShuffle){
                Collections.shuffle(alFilesToPlay);
            }
            FIFO.getInstance().push(Util.createStackItems(alFilesToPlay,bRepeat,true),bPush);
        }
        
        public void setIcon(ImageIcon ii){
            jlIcon.setIcon(ii);
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            //Menu items
            if (e.getSource() == jmiAlbumPlay){
                play(false,false,false);
            }
            else if (e.getSource() == jmiAlbumPlayRepeat){
                play(true,false,false);
            }
            else if (e.getSource() == jmiAlbumPlayShuffle){
                play(false,true,false);
            }
            else if (e.getSource() == jmiAlbumPush){
                play(false,false,true);
            }
            else if (e.getSource() == jmiGetCovers){
                new CoverSelectionWizard();
            }
            else if (e.getSource() == jmiAlbumProperties){
                ArrayList<IPropertyable> alAlbums = new ArrayList();
                alAlbums.add(album);
                new PropertiesWizard(alAlbums,TrackManager.getInstance().getAssociatedTracks(album));
            }
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
            if (CatalogView.this.item != null){
                CatalogView.this.item.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            }
            
            CatalogView.this.item = this;
            setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.RED));
            if (e.getButton() == MouseEvent.BUTTON1 && e.getSource() == jlIcon){
                play(false,false,false);
            }
            else if (e.getButton() == MouseEvent.BUTTON3 && e.getSource() == jlIcon){
                jmenu.show(jlIcon,e.getX(),e.getY());
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
        
        public boolean equals(Object o){
            if (!(o instanceof CatalogItem)){
                return false;
            }
            CatalogItem other = (CatalogItem)o;
            //if no cover, compare album
            if (other.getCoverFile().equals(FILE_THUMB_NO_COVER) || 
                    getCoverFile().equals(FILE_THUMB_NO_COVER)){
                return other.getAlbum().equals(album);
            }
            //compare cover file
            return other.getCoverFile().equals(fCover);
        }
        
        public File getCoverFile() {
            return fCover;
        }
        
        public Album getAlbum() {
            return album;
        }
    }
    
    
    /**
     * 
     *  Cover selection wizard
     *
     * @author     bflorat
     * @created    16 déc. 2005
     */
    class CoverSelectionWizard extends JDialog implements ActionListener{
        
        JLabel jlIcon;
        JPanel jpControls;
        JButton jbPrevious;
        JButton jbNext;
        OKCancelPanel okc;
        
        ArrayList<URL> alUrls;
        
        int width = 200;
        int height = 200;
        
        int index = 0;
        
        public CoverSelectionWizard(){
            super(Main.getWindow(),Messages.getString("CatalogView.7"),true); //modal //$NON-NLS-1$
            //Control
            double[][] dControl = {{TableLayout.FILL,TableLayout.PREFERRED,
                TableLayout.FILL,TableLayout.PREFERRED,
                TableLayout.FILL,TableLayout.PREFERRED},
                {TableLayout.PREFERRED}};
            jpControls = new JPanel();
            okc = new OKCancelPanel(this);
            jbPrevious = new JButton(Messages.getString("Previous"));
            jbPrevious.setEnabled(false); //always false at startup
            jbPrevious.addActionListener(this);
            jbNext = new JButton(Messages.getString("Next"));
            jbNext.addActionListener(this);
            jbNext.setEnabled(false);
            jpControls.setLayout(new TableLayout(dControl));
            jpControls.add(jbPrevious,"1,0");
            jpControls.add(jbNext,"3,0");
            jpControls.add(okc,"5,0");
            //Main
            double[][] dMain = {{10,TableLayout.PREFERRED,10},
                    {TableLayout.PREFERRED,20,TableLayout.PREFERRED}};
            jlIcon = new JLabel();
            JPanel jpMain = (JPanel)getContentPane();
            jpMain.setLayout(new TableLayout(dMain));
            jpMain.add(jlIcon,"1,0");
            jpMain.add(jpControls,"1,2");
            String sQuery = CatalogView.this.item.getAlbum().getName2();
            try {
                alUrls = DownloadManager.getRemoteCoversList(sQuery);
                if (alUrls.size() == 0){
                    jlIcon.setText(Messages.getString("CatalogView.8"));
                }
                else{
                    jbNext.setEnabled(true);
                    jlIcon.setText("");
                    Cover cover = new Cover(alUrls.get(0),Cover.REMOTE_COVER);
                    jlIcon.setIcon(Util.getResizedImage(cover.getImage(),width,height));
                }
            } catch (Exception e) {
                Log.error(e);
                jlIcon.setText(Messages.getString("Error.129"));
                okc.getOKButton().setEnabled(false);
            }
            finally{
                pack();
                setLocationRelativeTo(Main.getWindow());
                setVisible(true);
                Util.stopWaiting();
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            Util.waiting();
            if (e.getSource() == okc.getCancelButton()){
                dispose();
            }
            else if (e.getSource() == okc.getOKButton()){
                //first commit this cover on the disk if it is a remote cover
                Cover cover;
                try {
                    cover = new Cover(alUrls.get(index),Cover.REMOTE_COVER);
                } catch (Exception e1) {
                    Log.error(e1);
                    dispose();
                    return;
                }
                String sFilename = Util.getOnlyFile(cover.getURL().toString());
                //write cover in the first available directory we find
                Album album = CatalogView.this.item.getAlbum();
                //test if album contains at least one mounted file
                ArrayList<Track> alTracks = TrackManager.getInstance().getAssociatedTracks(album);
                Track mountedTrack = null;
                if (alTracks.size() > 0){
                    boolean bOK= false;
                    for (Track track:alTracks){
                        if (track.getReadyFiles().size() > 0){
                            bOK = true;
                            mountedTrack = track;
                            break;
                        }
                    }
                    if (!bOK){
                        Messages.showErrorMessage("024");
                        dispose();
                        return;
                    }
                }
                ArrayList<org.jajuk.base.File> alFiles = mountedTrack.getReadyFiles();
                String sFilePath =  alFiles.get(0).getDirectory().getAbsolutePath()+"/"+sFilename; //$NON-NLS-1$
                try{
                    //copy file from cache
                    File fSource = new File(Util.getCachePath(cover.getURL()));
                    File file = new File(sFilePath);
                    Util.copy(fSource,file);
                    InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
                    //then make it the default cover in this directory
                    Directory dir = FIFO.getInstance().getCurrentFile().getDirectory(); 
                    dir.setProperty("default_cover",sFilename); //$NON-NLS-1$
                    //create new thumbnail
                    File fThumb = new File(FILE_THUMBS+'/'+(String)jcbSize.getSelectedItem()+'/'+album.getId()+'.'+EXT_THUMB);
                    Util.createThumbnail(new URL(file.getAbsolutePath()),fThumb,100+(50*jcbSize.getSelectedIndex()));
                    CatalogView.this.item.setIcon(new ImageIcon(fThumb.getAbsolutePath()));
                }
                catch(Exception ex){
                    Log.error("024",ex); //$NON-NLS-1$
                    Messages.showErrorMessage("024"); //$NON-NLS-1$
                }
                finally{
                    dispose();
                }
                
            } 
            else if (e.getSource() == jbNext){
                if (index < alUrls.size()-1){
                    index ++;
                    try{
                        Cover cover = new Cover(alUrls.get(index),Cover.REMOTE_COVER);
                        okc.getOKButton().setEnabled(true);
                        jlIcon.setText("");
                        jlIcon.setIcon(Util.getResizedImage(cover.getImage(),width,height));
                    } catch (Exception ex) {
                        Log.error(ex);
                        jlIcon.setIcon(null);
                        jlIcon.setText(Messages.getString("Error.129"));
                        okc.getOKButton().setEnabled(false);
                    }
                }
            }
            else if (e.getSource() == jbPrevious){
                if (index > 0){
                    index --;
                    try{
                        Cover cover = new Cover(alUrls.get(index),Cover.REMOTE_COVER);
                        okc.getOKButton().setEnabled(true);
                        jlIcon.setText("");
                        jlIcon.setIcon(Util.getResizedImage(cover.getImage(),width,height));
                    } catch (Exception ex) {
                        Log.error(ex);
                        jlIcon.setIcon(null);
                        jlIcon.setText(Messages.getString("Error.129"));
                        okc.getOKButton().setEnabled(false);
                    }
                }
            }
            //check button state
            if (index == alUrls.size()-1){
                jbNext.setEnabled(false);
            }
            else{
                jbNext.setEnabled(true);
            }
            if (index == 0){
                jbPrevious.setEnabled(false);
            }
            else{
                jbPrevious.setEnabled(true);
            }
            Util.stopWaiting();
            
        }
    }
    
}

