/*
 * Jajuk Copyright (C) 2003 Bertrand Florat
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA. 
 * $Revision$
 */

package org.jajuk.ui.views;

import java.awt.Component;
import java.awt.Font;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.StackItem;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.PropertiesWizard;
import org.jajuk.ui.TransferableTreeNode;
import org.jajuk.ui.TreeTransferHandler;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import ext.SwingWorker;

/**
 * Logical tree view
 * 
 * @author Bertrand Florat 
 * @created 28 nov. 2003
 */
public class LogicalTreeView extends AbstractTreeView implements ActionListener,Observer{
    
    /** Self instance */
    private static LogicalTreeView ltv;
    
    /** Top tree node */
    DefaultMutableTreeNode top;
    
    /** Track selection*/
    ArrayList alTracks;
    
    /** Current selection */
    TreePath[] paths;
    
    JPopupMenu jmenuStyle;
    JMenuItem jmiStylePlay;
    JMenuItem jmiStylePush;
    JMenuItem jmiStylePlayShuffle;
    JMenuItem jmiStylePlayRepeat;
    JMenuItem jmiStyleDelete;
    JMenuItem jmiStyleProperties;
    
    JPopupMenu jmenuAuthor;
    JMenuItem jmiAuthorPlay;
    JMenuItem jmiAuthorPush;
    JMenuItem jmiAuthorPlayShuffle;
    JMenuItem jmiAuthorPlayRepeat;
    JMenuItem jmiAuthorDelete;
    JMenuItem jmiAuthorProperties;
    
    JPopupMenu jmenuAlbum;
    JMenuItem jmiAlbumPlay;
    JMenuItem jmiAlbumPush;
    JMenuItem jmiAlbumPlayShuffle;
    JMenuItem jmiAlbumPlayRepeat;
    JMenuItem jmiAlbumDelete;
    JMenuItem jmiAlbumProperties;
    
    JPopupMenu jmenuTrack;
    JMenuItem jmiTrackPlay;
    JMenuItem jmiTrackPush;
    JMenuItem jmiTrackDelete;
    JMenuItem jmiTrackProperties;
    
    
    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.IView#getDesc()
     */
    public String getDesc() {
        return "LogicalTreeView.0"; //$NON-NLS-1$
    }
    
    /** Return singleton */
    public static synchronized LogicalTreeView getInstance() {
        if (ltv == null) {
            ltv = new LogicalTreeView();
        }
        return ltv;
    }
    
    /** Constructor */
    public LogicalTreeView(){
        ltv = this;
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#display()
     */
    public void populate(){
        //**Menu items**
        
        //Style menu
        jmenuStyle = new JPopupMenu();
        jmiStylePlay = new JMenuItem(Messages.getString("LogicalTreeView.1")); //$NON-NLS-1$
        jmiStylePlay.addActionListener(this);
        jmiStylePush = new JMenuItem(Messages.getString("LogicalTreeView.2")); //$NON-NLS-1$
        jmiStylePush.addActionListener(this);
        jmiStylePlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.3")); //$NON-NLS-1$
        jmiStylePlayShuffle.addActionListener(this);
        jmiStylePlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.4")); //$NON-NLS-1$
        jmiStylePlayRepeat.addActionListener(this);
        jmiStyleDelete = new JMenuItem(Messages.getString("LogicalTreeView.5")); //$NON-NLS-1$
        jmiStyleDelete.setEnabled(false);
        jmiStyleDelete.addActionListener(this);
        jmiStyleProperties = new JMenuItem(Messages.getString("LogicalTreeView.7")); //$NON-NLS-1$
        jmiStyleProperties.addActionListener(this);
        jmenuStyle.add(jmiStylePlay);
        jmenuStyle.add(jmiStylePush);
        jmenuStyle.add(jmiStylePlayShuffle);
        jmenuStyle.add(jmiStylePlayRepeat);
        jmenuStyle.add(jmiStyleDelete);
        jmenuStyle.add(jmiStyleProperties);
        
        //Author menu
        jmenuAuthor = new JPopupMenu();
        jmiAuthorPlay = new JMenuItem(Messages.getString("LogicalTreeView.8")); //$NON-NLS-1$
        jmiAuthorPlay.addActionListener(this);
        jmiAuthorPush = new JMenuItem(Messages.getString("LogicalTreeView.9")); //$NON-NLS-1$
        jmiAuthorPush.addActionListener(this);
        jmiAuthorPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.10")); //$NON-NLS-1$
        jmiAuthorPlayShuffle.addActionListener(this);
        jmiAuthorPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.11")); //$NON-NLS-1$
        jmiAuthorPlayRepeat.addActionListener(this);
        jmiAuthorDelete = new JMenuItem(Messages.getString("LogicalTreeView.12")); //$NON-NLS-1$
        jmiAuthorDelete.setEnabled(false);
        jmiAuthorDelete.addActionListener(this);
        jmiAuthorProperties = new JMenuItem(Messages.getString("LogicalTreeView.14")); //$NON-NLS-1$
        jmiAuthorProperties.addActionListener(this);
        jmenuAuthor.add(jmiAuthorPlay);
        jmenuAuthor.add(jmiAuthorPush);
        jmenuAuthor.add(jmiAuthorPlayShuffle);
        jmenuAuthor.add(jmiAuthorPlayRepeat);
        jmenuAuthor.add(jmiAuthorDelete);
        jmenuAuthor.add(jmiAuthorProperties);
        
        //Album menu
        jmenuAlbum = new JPopupMenu();
        jmiAlbumPlay = new JMenuItem(Messages.getString("LogicalTreeView.15")); //$NON-NLS-1$
        jmiAlbumPlay.addActionListener(this);
        jmiAlbumPush = new JMenuItem(Messages.getString("LogicalTreeView.16")); //$NON-NLS-1$
        jmiAlbumPush.addActionListener(this);
        jmiAlbumPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.17")); //$NON-NLS-1$
        jmiAlbumPlayShuffle.addActionListener(this);
        jmiAlbumPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.18")); //$NON-NLS-1$
        jmiAlbumPlayRepeat.addActionListener(this);
        jmiAlbumDelete = new JMenuItem(Messages.getString("LogicalTreeView.19")); //$NON-NLS-1$
        jmiAlbumDelete.setEnabled(false);
        jmiAlbumDelete.addActionListener(this);
        jmiAlbumProperties = new JMenuItem(Messages.getString("LogicalTreeView.21")); //$NON-NLS-1$
        jmiAlbumProperties.addActionListener(this);
        jmenuAlbum.add(jmiAlbumPlay);
        jmenuAlbum.add(jmiAlbumPush);
        jmenuAlbum.add(jmiAlbumPlayShuffle);
        jmenuAlbum.add(jmiAlbumPlayRepeat);
        jmenuAlbum.add(jmiAlbumDelete);
        jmenuAlbum.add(jmiAlbumProperties);
        
        //Track menu
        jmenuTrack = new JPopupMenu();
        jmiTrackPlay = new JMenuItem(Messages.getString("LogicalTreeView.22")); //$NON-NLS-1$
        jmiTrackPlay.addActionListener(this);
        jmiTrackPush = new JMenuItem(Messages.getString("LogicalTreeView.23")); //$NON-NLS-1$
        jmiTrackPush.addActionListener(this);
        jmiTrackDelete = new JMenuItem(Messages.getString("LogicalTreeView.24")); //$NON-NLS-1$
        jmiTrackDelete.setEnabled(false);
        jmiTrackDelete.addActionListener(this);
        jmiTrackProperties = new JMenuItem(Messages.getString("LogicalTreeView.26")); //$NON-NLS-1$
        jmiTrackProperties.addActionListener(this);
        jmenuTrack.add(jmiTrackPlay);
        jmenuTrack.add(jmiTrackPush);
        jmenuTrack.add(jmiTrackDelete);
        jmenuTrack.add(jmiTrackProperties);
        
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        top = new DefaultMutableTreeNode(Messages.getString("LogicalTreeView.27")); //$NON-NLS-1$
        
        //Register on the list for subject we are interrested in
        ObservationManager.register(EVENT_DEVICE_MOUNT,this);
        ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
        ObservationManager.register(EVENT_DEVICE_REFRESH,this);
        //fill the tree
        populateTree();
        //create tree
        jtree = new JTree(top){
            public void setUI(TreeUI ui) { //overwrite this method to make sure all rows have icon own height
                super.setUI(ui);
                setRowHeight(-1);
            }
        };
        jtree.putClientProperty("JTree.lineStyle", "Angled"); //$NON-NLS-1$ //$NON-NLS-2$
        jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        jtree.setCellRenderer(new DefaultTreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                setFont(new Font("Dialog",Font.PLAIN,10)); //$NON-NLS-1$
                if (value instanceof StyleNode ){
                    setIcon(Util.getIcon(ICON_STYLE));
                }
                else if (value instanceof AuthorNode){
                    setIcon(Util.getIcon(ICON_AUTHOR));
                }
                else if (value instanceof AlbumNode){
                    setIcon(Util.getIcon(ICON_ALBUM));
                }
                else if (value instanceof TrackNode){
                    setIcon(Util.getIcon(ICON_FILE));
                }
                return this;
            }
        });
        DefaultTreeModel treeModel = new DefaultTreeModel(top);
        //Tree model listener to detect changes in the tree structure
        treeModel.addTreeModelListener(new TreeModelListener(){
            
            public void treeNodesChanged(TreeModelEvent e) {
                DefaultMutableTreeNode node;
                node = (DefaultMutableTreeNode)
                (e.getTreePath().getLastPathComponent());
                
                try {
                    int index = e.getChildIndices()[0];
                    node = (DefaultMutableTreeNode)
                    (node.getChildAt(index));
                } catch (NullPointerException exc) {}
                
            }
            
            public void treeNodesInserted(TreeModelEvent e) {
            }
            
            public void treeNodesRemoved(TreeModelEvent e) {
            }
            
            public void treeStructureChanged(TreeModelEvent e) {
            }
            
        });
        
        //Tree selection listener to detect a selection
        jtree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                TreePath[] tpSelected = jtree.getSelectionModel().getSelectionPaths();
                if (tpSelected == null){
                    return;
                }
                //disable menus that are realated to only one item
                if (tpSelected.length != 1){
                    jmiStyleProperties.setEnabled(false);
                    jmiAuthorProperties.setEnabled(false);
                    jmiAlbumProperties.setEnabled(false);
                    jmiTrackProperties.setEnabled(false);
                }
                else{
                    jmiStyleProperties.setEnabled(true);
                    jmiAuthorProperties.setEnabled(true);
                    jmiAlbumProperties.setEnabled(true);
                    jmiTrackProperties.setEnabled(true);
                }
                HashSet hsSelectedTracks = new HashSet(100);
                int items = 0;
                //get all components recursively
                for (int i=0;i<tpSelected.length;i++){
                    Object o = tpSelected[i].getLastPathComponent();
                    Enumeration e2 = ((DefaultMutableTreeNode)o).depthFirstEnumeration(); //return all childs nodes recursively
                    while ( e2.hasMoreElements()){
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)e2.nextElement();
                        if (node instanceof TrackNode){
                            Track track = ((TrackNode)node).getTrack();
                            if (hsSelectedTracks.contains(track)){ //don't count the same track several time if user select directory and then tracks inside
                                continue;
                            }
                            items ++;
                            hsSelectedTracks.add(track);
                        }
                    }
                }
                StringBuffer sbOut = new StringBuffer().append(items).append(Messages.getString("LogicalTreeView.31")); //$NON-NLS-1$
                InformationJPanel.getInstance().setSelection(sbOut.toString());
                if (ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE)){ //if table is synchronized with tree, notify the selection change
                    Properties properties = new Properties();
                    properties.put(DETAIL_SELECTION,hsSelectedTracks);
                    ObservationManager.notify(new Event(EVENT_SYNC_TREE_TABLE,properties));
                }
            }
        });
        //Listen for double clic
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
                if (path == null){
                    return;
                }
                if ( e.getClickCount() == 2 ){
                    Object o = path.getLastPathComponent();
                    if (o instanceof TrackNode){
                        Track track = ((TrackNode)o).getTrack();
                        File file = track.getPlayeableFile();
                        if (file != null){
                            try{
                                FIFO.getInstance().push(new StackItem(file,ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
                            }
                            catch(JajukException je){
                                Log.error(je);
                            }
                        }
                        else{
                            Messages.showErrorMessage("010",track.getName()); //$NON-NLS-1$
                        }
                    }
                }
                else if ( e.getClickCount() == 1 
                        && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
                    //Right clic behavior identical to konqueror tree:
                    //if none or 1 node is selected, a right click on another node select it
                    //if more than 1, we keep selection and display a popup for them
                    if (jtree.getSelectionCount() < 2){
                        jtree.getSelectionModel().setSelectionPath(path);
                    }
                    paths = jtree.getSelectionModel().getSelectionPaths();
                    getInstance().alTracks = new ArrayList(100);
                    //test mix between types ( not allowed )
                    String sClass = paths[0].getLastPathComponent().getClass().toString();
                    for (int i=0;i<paths.length;i++){
                        if (!paths[i].getLastPathComponent().getClass().toString().equals(sClass)){
                            return;
                        }	
                    }
                    //get all components recursively
                    for (int i=0;i<paths.length;i++){
                        Object o = paths[i].getLastPathComponent();
                        Enumeration e2 = ((DefaultMutableTreeNode)o).depthFirstEnumeration(); //return all childs nodes recursively
                        while ( e2.hasMoreElements()){
                            DefaultMutableTreeNode node = (DefaultMutableTreeNode)e2.nextElement();
                            if (node instanceof TrackNode){
                                Track track = ((TrackNode)node).getTrack();
                                if (track.getPlayeableFile() != null ){
                                    getInstance().alTracks.add(((TrackNode)node).getTrack());
                                }
                            }
                        }
                    }
                    //display menus according node type
                    if (paths[0].getLastPathComponent() instanceof TrackNode ){
                        jmenuTrack.show(jtree,e.getX(),e.getY());
                    }
                    else if (paths[0].getLastPathComponent() instanceof AlbumNode){
                        jmenuAlbum.show(jtree,e.getX(),e.getY());
                    }
                    else if (paths[0].getLastPathComponent() instanceof AuthorNode){
                        jmenuAuthor.show(jtree,e.getX(),e.getY()); 
                    }
                    else if (paths[0].getLastPathComponent() instanceof StyleNode){
                        jmenuStyle.show(jtree,e.getX(),e.getY()); 
                    }
                }
            }
        };
        jtree.addMouseListener(ml);
        //Expansion analyse to keep expended state 
        jtree.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent event) {
                Object o = event.getPath().getLastPathComponent(); 
                if (o instanceof StyleNode){
                    Style style = ((StyleNode)o).getStyle();
                    style.removeProperty(XML_EXPANDED);
                }
                else if (o instanceof AuthorNode){
                    Author author = ((AuthorNode)o).getAuthor();
                    author.removeProperty(XML_EXPANDED);
                }
                else if (o instanceof AlbumNode){
                    Album album = ((AlbumNode)o).getAlbum();
                    album.removeProperty(XML_EXPANDED);
                }
            }
            
            public void treeExpanded(TreeExpansionEvent event) {
                Object o = event.getPath().getLastPathComponent(); 
                if (o instanceof StyleNode){
                    Style style = ((StyleNode)o).getStyle();
                    style.setProperty(XML_EXPANDED,"y"); //$NON-NLS-1$
                }
                else if (o instanceof AuthorNode){
                    Author author = ((AuthorNode)o).getAuthor();
                    author.setProperty(XML_EXPANDED,"y"); //$NON-NLS-1$
                }
                else if (o instanceof AlbumNode){
                    Album album = ((AlbumNode)o).getAlbum();
                    album.setProperty(XML_EXPANDED,"y"); //$NON-NLS-1$
                }
            }
        });
        jtree.setAutoscrolls(true);
        //DND support
        new TreeTransferHandler(jtree, DnDConstants.ACTION_COPY_OR_MOVE,true);
        jspTree = new JScrollPane(jtree);
        add(jspTree);
        expand();
    }
    
    /**Fill the tree */
    public void populateTree(){
        //delete previous tree
        top.removeAllChildren();
        ArrayList alTracks = TrackManager.getSortedTracks();
        Iterator it1 = alTracks.iterator();
        while ( it1.hasNext()){
            Track track = (Track)it1.next();
            if ( !track.shouldBeHidden()){
                
                StyleNode styleNode = null;
                Style style=track.getStyle();
                AuthorNode authorNode = null;
                Author author = track.getAuthor();
                AlbumNode albumNode = null;
                Album album = track.getAlbum();
                
                //create style
                Enumeration e = top.children();
                boolean b = false;
                while (e.hasMoreElements()){  //check the style doesn't already exist
                    StyleNode sn = (StyleNode)e.nextElement();
                    if ( sn.getStyle().equals(style)){
                        b = true;
                        styleNode = sn;
                        break;
                    }
                }
                if ( !b){
                    styleNode = new StyleNode(style);
                    top.add(styleNode);
                }
                //create author
                e = styleNode.children();
                b = false;
                while (e.hasMoreElements()){  //check if the author doesn't already exist
                    AuthorNode an = (AuthorNode)e.nextElement();
                    if ( an.getAuthor().equals(author)){
                        b = true;
                        authorNode = an;
                        break;
                    }
                }
                if ( !b){
                    authorNode = new AuthorNode(author);
                    styleNode.add(authorNode);
                }
                //create album
                e = authorNode.children();
                b = false;
                while (e.hasMoreElements()){  //check if the album doesn't already exist
                    AlbumNode an = (AlbumNode)e.nextElement();
                    if ( an.getAlbum().equals(album)){
                        b = true;
                        albumNode = an;
                        break;
                    }
                }
                if ( !b){
                    albumNode = new AlbumNode(album);
                    authorNode.add(albumNode);
                }
                //create track
                albumNode.add(new TrackNode(track));
            }
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent e) {
        new Thread(){
            public void run(){
                if (e.getSource() == jmiStyleProperties){
                    Style style =  ((StyleNode)paths[0].getLastPathComponent()).getStyle();
                    new PropertiesWizard(style);
                }
                else if (e.getSource() == jmiAuthorProperties){
                    Author author =  ((AuthorNode)paths[0].getLastPathComponent()).getAuthor();
                    new PropertiesWizard(author);
                }
                else if (e.getSource() == jmiAlbumProperties){
                    Album album =  ((AlbumNode)paths[0].getLastPathComponent()).getAlbum();
                    new PropertiesWizard(album);
                }
                else if (e.getSource() == jmiTrackProperties){
                    Track track =  ((TrackNode)paths[0].getLastPathComponent()).getTrack();
                    new PropertiesWizard(track);
                }
                else{
                    //compute selection
                    ArrayList alFilesToPlay = new ArrayList(alTracks.size());
                    Iterator it = alTracks.iterator();
                    while ( it.hasNext()){
                        File file = ((Track)it.next()).getPlayeableFile();
                        if ( file != null){
                            alFilesToPlay.add(file);
                        }
                    }
                    if ( alFilesToPlay.size() == 0){
                        Messages.showErrorMessage("018"); //$NON-NLS-1$
                        return;
                    }
                    if ( (e.getSource() == jmiTrackPlay 
                            || e.getSource() == jmiAlbumPlay
                            || e.getSource() == jmiAuthorPlay
                            || e.getSource() == jmiStylePlay )){
                        FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
                            ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
                    }
                    else if ( ( e.getSource() == jmiTrackPush 
                            || e.getSource() == jmiAlbumPush
                            || e.getSource() == jmiAuthorPush
                            || e.getSource() == jmiStylePush) ){
                        FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
                            ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),true);
                    }
                    else if ( (e.getSource() == jmiAlbumPlayShuffle
                            || e.getSource() == jmiAuthorPlayShuffle
                            || e.getSource() == jmiStylePlayShuffle )){
                        Collections.shuffle(alFilesToPlay);
                        FIFO.getInstance().push(Util.createStackItems(alFilesToPlay,
                            ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
                    }
                    else if ( ( e.getSource() == jmiAlbumPlayRepeat
                            || e.getSource() == jmiAuthorPlayRepeat
                            || e.getSource() == jmiStylePlayRepeat) ){
                        FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),true,true),false);
                    }
                }
            }
        }.start();
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getID()
     */
    public String getID() {
        return "org.jajuk.ui.views.LogicalTreeView"; //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if ( subject.equals(EVENT_DEVICE_MOUNT) || subject.equals(EVENT_DEVICE_UNMOUNT)){
            SwingWorker sw = new SwingWorker() {
                public Object  construct(){
                    populateTree();
                    return null;
                }
                public void finished() {
                    SwingUtilities.updateComponentTreeUI(jtree);
                    expand();
                    int i = jspTree.getVerticalScrollBar().getValue();
                    jspTree.getVerticalScrollBar().setValue(i);
                }
            };
            sw.start();
        }
        else if( subject.equals(EVENT_DEVICE_REFRESH)){
            SwingWorker sw = new SwingWorker() {
                public Object  construct(){
                    populateTree();
                    return null;
                }
                public void finished() {
                    SwingUtilities.updateComponentTreeUI(jtree);
                    expand();
                    int i = jspTree.getVerticalScrollBar().getValue();
                    jspTree.getVerticalScrollBar().setValue(i);
                }
            };
            sw.start();
        }
    }
    
    /**
     * Manages auto-expand
     *
     */
    public void expand(){
        //expand all
        for (int i=0;i<jtree.getRowCount();i++){
            Object o = jtree.getPathForRow(i).getLastPathComponent(); 
            if ( o instanceof StyleNode){
                Style style = ((StyleNode)o).getStyle();
                String sExp = style.getValue(XML_EXPANDED); 
                if ( "y".equals(sExp)){ //$NON-NLS-1$
                    jtree.expandRow(i);	
                }
            }
            else if ( o instanceof AuthorNode){
                Author author = ((AuthorNode)o).getAuthor();
                String sExp = author.getValue(XML_EXPANDED); 
                if ( "y".equals(sExp)){ //$NON-NLS-1$
                    jtree.expandRow(i);	
                }
            }
            else if ( o instanceof AlbumNode){
                Album album = ((AlbumNode)o).getAlbum();
                String sExp = album.getValue(XML_EXPANDED); 
                if ( "y".equals(sExp)){ //$NON-NLS-1$
                    jtree.expandRow(i);	
                }
            }
        }
    }
    
    
    /**
     * @return Returns the alTracks.
     */
    public ArrayList getTrackSelection() {
        return alTracks;
    }
    
    
    
    
}


/**
 * Style node 
 * @author     Bertrand Florat
 * @created    29 nov. 2003
 */
class StyleNode extends TransferableTreeNode{
    
    /**
     * Constructor
     * @param track
     */
    public StyleNode(Style track){
        super(track);
    }
    
    /**
     * return a string representation of this track node
     */
    public String toString(){
        return ((Style)super.getData()).getName2();
    }
    /**
     * @return Returns the track.
     */
    public Style getStyle() {
        return (Style)super.getData();
    }
}

/**
 * Author node 
 * @author     Bertrand Florat
 * @created    29 nov. 2003
 */
class AuthorNode extends TransferableTreeNode{
    
    /**
     * Constructor
     * @param author
     */
    public AuthorNode(Author author){
        super(author);
    }
    
    /**
     * return a string representation of this author node
     */
    public String toString(){
        return ((Author)super.getData()).getName2();
    }
    /**
     * @return Returns the author.
     */
    public Author getAuthor() {
        return (Author)super.getData();
    }
    
}


/**
 * Album node 
 * @author     Bertrand Florat
 * @created    29 nov. 2003
 */
class AlbumNode  extends TransferableTreeNode{
    
    /**
     * Constructor
     * @param album
     */
    public AlbumNode(Album album){
        super(album);
    }
    
    /**
     * return a string representation of this album node
     */
    public String toString(){
        return ((Album)super.getData()).getName2();
    }
    /**
     * @return Returns the album.
     */
    public Album getAlbum() {
        return (Album)super.getData();
    }
}

/**
 * Track node 
 * @author     Bertrand Florat
 * @created    29 nov. 2003
 */
class TrackNode  extends TransferableTreeNode{
    
    /**
     * Constructor
     * @param track
     */
    public TrackNode(Track track){
        super(track);
    }
    
    /**
     * return a string representation of this track node
     */
    public String toString(){
        return ((Track)super.getData()).getName();
    }
    /**
     * @return Returns the track.
     */
    public Track getTrack() {
        return (Track)super.getData();
    }
}



