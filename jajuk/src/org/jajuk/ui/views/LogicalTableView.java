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

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.PatternSyntaxException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.StackItem;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.TracksTableModel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Logical table view
 * 
 * @author Bertrand Florat 
 * @created 13 dec. 2003
 */
public class LogicalTableView extends AbstractTableView implements Observer{
	
	/** Self instance */
	private static LogicalTableView ltv;
	
	JPopupMenu jmenuTrack;
	JMenuItem jmiTrackPlay;
	JMenuItem jmiTrackPush;
	JMenuItem jmiTrackPlayShuffle;
	JMenuItem jmiTrackPlayRepeat;
	JMenuItem jmiTrackPlayAlbum;
	JMenuItem jmiTrackPlayAuthor;
	JMenuItem jmiTrackSetProperty;
	JMenuItem jmiTrackProperties;
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "LogicalTableView.0"; //$NON-NLS-1$
	}
	
	
	/** Return singleton */
	public static synchronized LogicalTableView getInstance() {
		if (ltv == null) {
			ltv = new LogicalTableView();
		}
		return ltv;
	}
	
	/** Constructor */
	public LogicalTableView(){
		super();
		ltv = this;
	    // Track menu
		jmenuTrack = new JPopupMenu();
		jmiTrackPlay = new JMenuItem(Messages.getString("LogicalTableView.7")); //$NON-NLS-1$
		jmiTrackPlay.addActionListener(this);
		jmiTrackPush = new JMenuItem(Messages.getString("LogicalTableView.8")); //$NON-NLS-1$
		jmiTrackPush.addActionListener(this);
		jmiTrackPlayShuffle = new JMenuItem(Messages.getString("LogicalTableView.9")); //$NON-NLS-1$
		jmiTrackPlayShuffle.addActionListener(this);
		jmiTrackPlayRepeat = new JMenuItem(Messages.getString("LogicalTableView.10")); //$NON-NLS-1$
		jmiTrackPlayRepeat.addActionListener(this);
		jmiTrackPlayAlbum = new JMenuItem(Messages.getString("LogicalTableView.11")); //$NON-NLS-1$
		jmiTrackPlayAlbum.addActionListener(this);
		jmiTrackPlayAuthor = new JMenuItem(Messages.getString("LogicalTableView.12")); //$NON-NLS-1$
		jmiTrackPlayAuthor.addActionListener(this);
		jmiTrackSetProperty = new JMenuItem(Messages.getString("LogicalTableView.13")); //$NON-NLS-1$
		jmiTrackSetProperty.setEnabled(false);
		jmiTrackSetProperty.addActionListener(this);
		jmiTrackProperties = new JMenuItem(Messages.getString("LogicalTableView.14")); //$NON-NLS-1$
		jmiTrackProperties.setEnabled(false);
		jmiTrackProperties.addActionListener(this);
		jmenuTrack.add(jmiTrackPlay);
		jmenuTrack.add(jmiTrackPush);
		jmenuTrack.add(jmiTrackPlayShuffle);
		jmenuTrack.add(jmiTrackPlayRepeat);
		jmenuTrack.add(jmiTrackPlayAlbum);
		jmenuTrack.add(jmiTrackPlayAuthor);
		jmenuTrack.add(jmiTrackSetProperty);
		jmenuTrack.add(jmiTrackProperties);
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void populate(){
		populateTable();
		super.populate();
		//Register on the list for subject we are interrested in
		ObservationManager.register(EVENT_DEVICE_MOUNT,this);
		ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
		ObservationManager.register(EVENT_SYNC_TREE_TABLE,this);
	}
	
	/**Fill the tree */
	public void populateTable(){
		//col number
		int iColNum = 6;
		//Columns names
		String[] sColName = new String[]{Messages.getString("LogicalTableView.1"),Messages.getString("LogicalTableView.2"),Messages.getString("LogicalTableView.3"),Messages.getString("LogicalTableView.4"),Messages.getString("LogicalTableView.5"),Messages.getString("LogicalTableView.6")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		//Values
		ArrayList alTracks = TrackManager.getSortedTracks();
		ArrayList alToShow = new ArrayList(alTracks.size());
		Iterator it = alTracks.iterator();
		while ( it.hasNext()){
			Track track = (Track)it.next(); 
			if ( !track.shouldBeHidden()){
				alToShow.add(track);
			}
		}
		int iSize = alToShow.size();
		it = alToShow.iterator();
		Object[][] oValues = new Object[iSize][iColNum+1];
		//Track | Album | Author | Length | Style | Rate	
		for (int i = 0;it.hasNext();i++){
			Track track = (Track)it.next(); 
			oValues[i][0] = track.getName();
			oValues[i][1] = track.getAlbum().getName2();
			oValues[i][2] = track.getAuthor().getName2();
			oValues[i][3] = Util.formatTimeBySec(track.getLength(),false);
			oValues[i][4] = track.getStyle().getName2();
			oValues[i][5] = new Long(track.getRate());
			oValues[i][6] = track.getId();
		}
		//edtiable table  and class 
		boolean[][] bCellEditable = new boolean[8][iSize];
		for (int i =0;i<iColNum;i++){
			for (int j=0;j<iSize;j++){
				bCellEditable[i][j]=false;
			}
		}
		//model creation
		model = new TracksTableModel(iColNum,bCellEditable,sColName);
		model.setValues(oValues);
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getID()
	 */
	public String getID() {
		return "org.jajuk.ui.views.LogicalTableView"; //$NON-NLS-1$
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
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
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if ( e.getClickCount() == 2){ //double clic, can be only one track
			Track track = TrackManager.getTrack(jtable.getSortingModel().getValueAt(jtable.getSelectedRow(),jtable.getColumnCount()).toString());
			File file = track.getPlayeableFile();
			if ( file != null){
				try{
				    FIFO.getInstance().push(new StackItem(file,ConfigurationManager.getBoolean(CONF_STATE_REPEAT)),ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));//launch it
				}
				catch(JajukException je){
				    Log.error(je);
				}
			}
			else{
				Messages.showErrorMessage("010",track.getName()); //$NON-NLS-1$
			}
		}		
		else if (e.getClickCount() == 1 
                && e.getButton()==MouseEvent.BUTTON3){  //right clic on a selected node set
	        //if none or 1 node is selected, a right click on another node select it
            //if more than 1, we keep selection and display a popup for them
            if (jtable.getSelectedRowCount() < 2){
                int iSelection = jtable.rowAtPoint(e.getPoint());
                jtable.getSelectionModel().setSelectionInterval(iSelection,iSelection);
            }
            if ( jtable.getSelectedRowCount() > 1){
				jmiTrackProperties.setEnabled(false); //can read a property from one sole track
			}
			else{
				jmiTrackProperties.setEnabled(false); //TBI set to true when managing properties
			}
			jmenuTrack.show(jtable,e.getX(),e.getY());
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		new Thread(){
			public void run(){
				//let super class to test common ( physical/logical ) events 
				if ( e.getSource() == jbApplyFilter || e.getSource() == jbClearFilter){
					LogicalTableView.super.actionPerformed(e);
					return;
				}
				//then specifics
				//computes selected tracks
				ArrayList alFilesToPlay = new ArrayList(10);
				int[] indexes = jtable.getSelectedRows();
				for (int i=0;i<indexes.length;i++){ //each track in selection
					Track track = TrackManager.getTrack(jtable.getSortingModel().getValueAt(indexes[i],jtable.getColumnCount()).toString());
					ArrayList alTracks = new ArrayList(indexes.length);
					if (e.getSource() == jmiTrackPlayAlbum){
					    Album album = track.getAlbum();
					    alTracks.addAll(album.getTracks()); //add all tracks from the same album
					}
					if (e.getSource() == jmiTrackPlayAuthor){
					    Author author = track.getAuthor();
					    alTracks.addAll(author.getTracks()); //add all tracks from the same author
					}
					else{
					    alTracks.add(track);
					}
					Iterator it = alTracks.iterator();
					while (it.hasNext()){ //each selected track and tracks from same album /author if required 
					    Track track2 = (Track)it.next();
					    File file = track2.getPlayeableFile();
						if ( file != null && !alFilesToPlay.contains(file)){
							alFilesToPlay.add(file);
						}
					}
				}
				if ( alFilesToPlay.size() == 0){
					Messages.showErrorMessage("018"); //$NON-NLS-1$
					return;
				}
				//simple play
				if ( e.getSource() == jmiTrackPlay || e.getSource() == jmiTrackPlayAlbum || e.getSource() == jmiTrackPlayAuthor ){
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
				}
				//push
				else if ( e.getSource() == jmiTrackPush){
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
							ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),true);
				}
				//shuffle play
				else if ( e.getSource() == jmiTrackPlayShuffle){
				    Collections.shuffle(alFilesToPlay);
					FIFO.getInstance().push(Util.createStackItems(alFilesToPlay,
						ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),false);
				}
				//repeat play
				else if ( e.getSource() == jmiTrackPlayRepeat){
					FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alFilesToPlay),true,true),false);
				}
			}
		}.start();
	}
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.AbstractTableView#applyFilter()
	 */
    public void applyFilter(final String sPropertyName,final String sPropertyValue) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boolean bShowWithTree = true;
                HashSet hs = (HashSet)ObservationManager.getDetailLastOccurence(EVENT_SYNC_TREE_TABLE,DETAIL_SELECTION);//look at selection
                boolean bSyncWithTreeOption = ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE);
                //Values
                ArrayList alTracks = TrackManager.getSortedTracks();
                ArrayList alToShow = new ArrayList(alTracks.size());
                Iterator it = alTracks.iterator();
                while ( it.hasNext()){
                    Track track = (Track)it.next(); 
                    bShowWithTree =  !bSyncWithTreeOption || ((hs != null && hs.size() > 0 && hs.contains(track)));//show it if no sync option or if item is in the selection
                    if ( !track.shouldBeHidden() && bShowWithTree){
                        alToShow.add(track);
                    }
                }
                int iSize = alToShow.size();
                int iColNum = 6 ;
                it = alToShow.iterator();
                //Track | Album | Author | Length | Style | Rate
                String sNewPropertyValue = sPropertyValue;
                if ( !ConfigurationManager.getBoolean(CONF_REGEXP) && sPropertyValue != null){ //do we use regular expression or not? if not, we allow user to use '*'
                    sNewPropertyValue = sPropertyValue.replaceAll("\\*",".*"); //$NON-NLS-1$ //$NON-NLS-2$
                    sNewPropertyValue = ".*"+sNewPropertyValue+".*"; //$NON-NLS-1$ //$NON-NLS-2$
                }
                else if ("".equals(sNewPropertyValue)){//in regexp mode, if none selection, display all rows
                    sNewPropertyValue = ".*";
                }
                while (it.hasNext()){
                    Track track = (Track)it.next();
                    if ( sPropertyName != null && sNewPropertyValue!= null){ //if name or value are null, means there is no filter
                        String sValue = track.getProperty(sPropertyName);
                        if ( sValue == null){ //try to filter on a unknown property, don't take this file
                            continue;
                        }
                        else { 
                            boolean bMatch = false;
                            try{  //test using regular expressions
                                bMatch = sValue.toLowerCase().matches(sNewPropertyValue.toLowerCase());  // test if the file property contains this property value (ignore case)
                            }
                            catch(PatternSyntaxException pse){ //wrong pattern syntax
                                bMatch = false;
                            }
                            if (!bMatch){
                                it.remove(); //no? remove it
                            }
                        }	
                    }
                }
                //populate this values
                Object[][] oValues = new Object[alToShow.size()][iColNum+1];
                for  (int i=0;i<alToShow.size();i++){
                    Track track = (Track)alToShow.get(i);
                    oValues[i][0] = track.getName();
                    oValues[i][1] = track.getAlbum().getName2();
                    oValues[i][2] = track.getAuthor().getName2();
                    oValues[i][3] = Util.formatTimeBySec(track.getLength(),false);
                    oValues[i][4] = track.getStyle().getName2();
                    oValues[i][5] = new Long(track.getRate());
                    oValues[i][6] = track.getId();
                }
                model.setValues(oValues);
                model.fireTableDataChanged();
            }
        });
    }
  	
}


