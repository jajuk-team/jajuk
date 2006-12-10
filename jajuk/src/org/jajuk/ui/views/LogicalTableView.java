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
import java.util.Iterator;
import java.util.Random;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.StackItem;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukTableModel;
import org.jajuk.ui.PropertiesWizard;
import org.jajuk.ui.TableTransferHandler;
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
public class LogicalTableView extends AbstractTableView {

	private static final long serialVersionUID = 1L;

	/** Self instance */
	private static LogicalTableView ltv;

	JPopupMenu jmenuTrack;

	JMenuItem jmiTrackPlay;

	JMenuItem jmiTrackPush;

	JMenuItem jmiTrackPlayShuffle;

	JMenuItem jmiTrackPlayRepeat;

	JMenuItem jmiTrackPlayAlbum;

	JMenuItem jmiTrackPlayAuthor;

	JMenuItem jmiTrackAddFavorite;

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
	public LogicalTableView() {
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
		jmiTrackAddFavorite = new JMenuItem(Messages.getString("LogicalTableView.15")); //$NON-NLS-1$
		jmiTrackAddFavorite.addActionListener(this);
		jmiProperties = new JMenuItem(Messages.getString("LogicalTableView.14")); //$NON-NLS-1$
		jmiProperties.addActionListener(this);
		jmenuTrack.add(jmiTrackPlay);
		jmenuTrack.add(jmiTrackPush);
		jmenuTrack.add(jmiTrackPlayShuffle);
		jmenuTrack.add(jmiTrackPlayRepeat);
		jmenuTrack.add(jmiTrackPlayAlbum);
		jmenuTrack.add(jmiTrackPlayAuthor);
		jmenuTrack.add(jmiTrackAddFavorite);
		jmenuTrack.add(jmiProperties);
	}

	/** Fill the table */
	public JajukTableModel populateTable() {
		// model creation
		TracksTableModel model = new TracksTableModel();
		model.addTableModelListener(this);
		model.setEditable(ConfigurationManager.getBoolean(CONF_LOGICAL_TABLE_EDITION));
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		int iSelectedCol = jtable.getSelectedColumn();
		// selected column in view Test click on play icon launch track only if
		// only first column is selected (fixes issue with
		// Ctrl-A)
		if (jtable.getSelectedColumnCount() == 1
		// click on play icon
				&& (jtable.convertColumnIndexToModel(iSelectedCol) == 0)
				// double click on any column and edition state false
				|| (e.getClickCount() == 2 && !jtbEditable.isSelected())) {
			// selected row in view
			int iSelectedRow = jtable.getSelectedRow();
			Track track = (Track) model.getItemAt(jtable.convertRowIndexToModel(iSelectedRow));
			File file = track.getPlayeableFile(false);
			if (file != null) {
				try {
					// launch it
					FIFO.getInstance()
							.push(
									new StackItem(file, ConfigurationManager
											.getBoolean(CONF_STATE_REPEAT)),
									ConfigurationManager
											.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));

				} catch (JajukException je) {
					Log.error(je);
				}
			} else {
				Messages.showErrorMessage("010", track.getName()); //$NON-NLS-1$
			}
		} else if (e.getClickCount() == 1) {
			int iSelectedRow = jtable.rowAtPoint(e.getPoint());
			// Store real row index
			TableTransferHandler.iSelectedRow = iSelectedRow;
			if (e.getButton() == MouseEvent.BUTTON3) {
				// right clic on a selected node set if none or 1 node is
				// selected, a right click on another node
				// select it if more than 1, we keep selection and display a
				// popup for them
				if (jtable.getSelectedRowCount() < 2) {
					jtable.getSelectionModel().setSelectionInterval(iSelectedRow, iSelectedRow);
				}
				jmenuTrack.show(jtable, e.getX(), e.getY());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void othersActionPerformed(final ActionEvent e) {
		new Thread() {
			public void run() {
				// computes selected tracks
				ArrayList<File> alFilesToPlay = new ArrayList<File>(10);
				int[] indexes = jtable.getSelectedRows();
				ArrayList<Item> alSelectedTracks = new ArrayList<Item>(indexes.length);
				for (int i = 0; i < indexes.length; i++) { // each track in
					// selection
					Track track = (Track) model
							.getItemAt(jtable.convertRowIndexToModel(indexes[i]));
					alSelectedTracks.add(track);
					ArrayList<Track> alTracks = new ArrayList<Track>(indexes.length);
					if (e.getSource() == jmiTrackPlayAlbum) {
						Album album = track.getAlbum();
						alTracks.addAll(TrackManager.getInstance().getAssociatedTracks(album)); 
						// add all tracks from the same album
					}
					if (e.getSource() == jmiTrackPlayAuthor) {
						Author author = track.getAuthor();
						// add all tracks from the same author
						alTracks.addAll(TrackManager.getInstance().getAssociatedTracks(author)); 
					} else {
						alTracks.add(track);
					}
					Iterator it = alTracks.iterator();
					while (it.hasNext()) { 
						// each selected track and tracks from same album author if required
						Track track2 = (Track) it.next();
						File file = track2.getPlayeableFile(false);
						if (file != null && !alFilesToPlay.contains(file)) {
							alFilesToPlay.add(file);
						}
					}
				}
				if (alFilesToPlay.size() == 0) {
					Messages.showErrorMessage("018"); //$NON-NLS-1$
					return;
				}
				// simple play
				if (e.getSource() == jmiTrackPlay || e.getSource() == jmiTrackPlayAlbum
						|| e.getSource() == jmiTrackPlayAuthor) {
					FIFO.getInstance().push(
							Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
									ConfigurationManager.getBoolean(CONF_STATE_REPEAT), true),
							false);
				}
				// push
				else if (e.getSource() == jmiTrackPush) {
					FIFO.getInstance()
							.push(
									Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
											ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
											true), true);
				}
				// shuffle play
				else if (e.getSource() == jmiTrackPlayShuffle) {
					Collections.shuffle(alFilesToPlay, new Random(System.currentTimeMillis()));
					FIFO.getInstance().push(
							Util.createStackItems(alFilesToPlay, ConfigurationManager
									.getBoolean(CONF_STATE_REPEAT), true), false);
				}
				// repeat play
				else if (e.getSource() == jmiTrackPlayRepeat) {
					FIFO.getInstance().push(
							Util.createStackItems(Util.applyPlayOption(alFilesToPlay), true, true),
							false);
				}
				// bookmark
				else if (e.getSource() == jmiTrackAddFavorite) {
					Bookmarks.getInstance().addFiles(alFilesToPlay);
				}
				// editable state
				else if (e.getSource() == jtbEditable) {
					ConfigurationManager.setProperty(CONF_LOGICAL_TABLE_EDITION, Boolean
							.toString(jtbEditable.isSelected()));
					model.setEditable(jtbEditable.isSelected());
				}
				// properties
				else if (e.getSource() == jmiProperties) {
					if (jtable.getSelectedRowCount() == 1) { 
						// mono selection
						Track track = (Track) model.getItemAt(jtable.convertRowIndexToModel(jtable
								.getSelectedRow()));
						ArrayList<Item> alItems = new ArrayList<Item>(1);
						alItems.add(track);
						new PropertiesWizard(alItems);
					} else {// multi selection
						ArrayList<Item> alTracks = new ArrayList<Item>(10);
						for (int i = 0; i <= jtable.getRowCount(); i++) {
							if (jtable.getSelectionModel().isSelectedIndex(i)) {
								Track track = (Track) model.getItemAt(jtable
										.convertRowIndexToModel(i));
								alTracks.add(track);
							}
						}
						new PropertiesWizard(alTracks);
					}
				}
			}
		}.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.AbstractTableView#initTable()
	 */
	@Override
	void initTable() {
		boolean bEditable = ConfigurationManager.getBoolean(CONF_LOGICAL_TABLE_EDITION);
		jtbEditable.setSelected(bEditable);
		model.setEditable(bEditable);
	}

}
