/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import org.jajuk.base.Bookmarks;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.StackItem;
import org.jajuk.ui.helpers.FilesTableModel;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.TableTransferHandler;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * Logical table view
 */
public class FilesTableView extends AbstractTableView implements MouseListener {

	private static final long serialVersionUID = 1L;

	JPopupMenu jmenuFile;

	JMenuItem jmiFilePlay;

	JMenuItem jmiFilePush;

	JMenuItem jmiFilePlayShuffle;

	JMenuItem jmiFilePlayRepeat;

	JMenuItem jmiFilePlayDirectory;

	JMenuItem jmiFileAddFavorites;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("FilesTableView.0");
	}

	public void initUI(){
		//Perform common table view initializations
		super.initUI();
		// File menu
		jmenuFile = new JPopupMenu();
		jmiFilePlay = new JMenuItem(Messages.getString("FilesTableView.1"),
				IconLoader.ICON_PLAY_16x16);
		jmiFilePlay.addActionListener(this);
		jmiFilePush = new JMenuItem(Messages.getString("FilesTableView.2"), IconLoader.ICON_PUSH);
		jmiFilePush.addActionListener(this);
		jmiFilePlayShuffle = new JMenuItem(Messages.getString("FilesTableView.3"),
				IconLoader.ICON_SHUFFLE);
		jmiFilePlayShuffle.addActionListener(this);
		jmiFilePlayRepeat = new JMenuItem(Messages.getString("FilesTableView.4"),
				IconLoader.ICON_REPEAT);
		jmiFilePlayRepeat.addActionListener(this);
		jmiFilePlayDirectory = new JMenuItem(Messages.getString("FilesTableView.15"),
				IconLoader.ICON_PLAY_16x16);
		jmiFilePlayDirectory.addActionListener(this);
		jmiProperties = new JMenuItem(Messages.getString("FilesTableView.6"),
				IconLoader.ICON_PROPERTIES);
		jmiProperties.addActionListener(this);
		jmiFileAddFavorites = new JMenuItem(Messages.getString("FilesTableView.16"),
				IconLoader.ICON_BOOKMARK_FOLDERS);
		jmiFileAddFavorites.addActionListener(this);
		jmenuFile.add(jmiFilePlay);
		jmenuFile.add(jmiFilePush);
		jmenuFile.add(jmiFilePlayShuffle);
		jmenuFile.add(jmiFilePlayRepeat);
		jmenuFile.add(jmiFilePlayDirectory);
		jmenuFile.add(jmiFileAddFavorites);
		jmenuFile.add(jmiProperties);
	}

	/** populate the table */
	public JajukTableModel populateTable() {
		// model creation
		FilesTableModel model = new FilesTableModel();
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
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

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			handlePopup(e);
			// Left click
		} else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
			int iSelectedCol = jtable.getSelectedColumn();
			// selected column in view Test click on play icon
			// launch track only if only first column is selected (fixes issue
			// with Ctrl-A)
			if (jtable.getSelectedColumnCount() == 1
					&& (jtable.convertColumnIndexToModel(iSelectedCol) == 0)
					// click on play icon
					|| (e.getClickCount() == 2 && !jtbEditable.isSelected())) {
				// double click on any column and edition state false

				// selected row in view
				int iSelectedRow = jtable.getSelectedRow();
				File file = (File) model.getItemAt(jtable.convertRowIndexToModel(iSelectedRow));
				try {
					// launch it
					FIFO.getInstance().push(
							new StackItem(file, ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
									true),
							ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));

				} catch (JajukException je) {
					Log.error(je);
				}
			} else if (e.getClickCount() == 1) {
				int iSelectedRow = jtable.rowAtPoint(e.getPoint());
				TableTransferHandler.iSelectedRow = iSelectedRow;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			handlePopup(e);
		}
	}

	public void handlePopup(final MouseEvent e) {
		int iSelectedRow = jtable.rowAtPoint(e.getPoint());
		TableTransferHandler.iSelectedRow = iSelectedRow;
		// o if none or 1 node is selected, a right click on another
		// node, select it
		// o if more than 1, we keep selection and display a popup for them
		if (jtable.getSelectedRowCount() < 2) {
			jtable.getSelectionModel().setSelectionInterval(iSelectedRow, iSelectedRow);
		}
		jmenuFile.show(jtable, e.getX(), e.getY());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		new Thread() {
			public void run() {
				// Editable state
				if (e.getSource() == jtbEditable) {
					ConfigurationManager.setProperty(CONF_TRACKS_TABLE_EDITION, Boolean
							.toString(jtbEditable.isSelected()));
					model.setEditable(jtbEditable.isSelected());
					return;
				}
				// computes selected files
				ArrayList<File> alFilesToPlay = new ArrayList<File>(jtable.getSelectedRowCount());
				ArrayList<Item> alSelectedFiles = new ArrayList<Item>(jtable.getSelectedRowCount());
				int[] indexes = jtable.getSelectedRows();
				for (int i = 0; i < indexes.length; i++) { // each selected
					// track
					File file = (File) model.getItemAt(jtable.convertRowIndexToModel(indexes[i]));
					alSelectedFiles.add(file);
					ArrayList<File> alFilesToPlay2 = new ArrayList<File>(indexes.length);
					if (e.getSource() == jmiFilePlayDirectory) {
						alFilesToPlay2.addAll(FileManager.getInstance().getAllDirectory(file));
					} else {
						alFilesToPlay2.add(file);
					}
					for (File file2:alFilesToPlay2){
						if (!alFilesToPlay.contains(file2)) {
							alFilesToPlay.add(file2);
						}
					}
				}
				// simple play
				if (e.getSource() == jmiFilePlay || e.getSource() == jmiFilePlayDirectory) {
					FIFO.getInstance().push(
							Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
									ConfigurationManager.getBoolean(CONF_STATE_REPEAT), true),
							false);
				}
				// push
				else if (e.getSource() == jmiFilePush) {
					FIFO.getInstance()
							.push(
									Util.createStackItems(Util.applyPlayOption(alFilesToPlay),
											ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
											true), true);
				}
				// shuffle play
				else if (e.getSource() == jmiFilePlayShuffle) {
					Collections.shuffle(alFilesToPlay, new Random());
					FIFO.getInstance().push(
							Util.createStackItems(alFilesToPlay, ConfigurationManager
									.getBoolean(CONF_STATE_REPEAT), true), false);
				}
				// repeat play
				else if (e.getSource() == jmiFilePlayRepeat) {
					FIFO.getInstance().push(
							Util.createStackItems(Util.applyPlayOption(alFilesToPlay), true, true),
							false);
				}
				// Bookmark
				else if (e.getSource() == jmiFileAddFavorites) {
					Bookmarks.getInstance().addFiles(alFilesToPlay);
				}
				// properties
				else if (e.getSource() == jmiProperties) {
					ArrayList<Item> alItems1 = new ArrayList<Item>(1); // file
					// items
					ArrayList<Item> alItems2 = new ArrayList<Item>(1); // tracks
					// items
					if (jtable.getSelectedRowCount() == 1) { // mono
						// selection
						File file = (File) model.getItemAt(jtable.convertRowIndexToModel(jtable
								.getSelectedRow()));
						// show file and associated track properties
						alItems1.add(file);
						alItems2.add(file.getTrack());
					} else {// multi selection
						for (int i = 0; i <= jtable.getRowCount(); i++) {
							if (jtable.getSelectionModel().isSelectedIndex(i)) {
								File file = (File) model
										.getItemAt(jtable.convertRowIndexToModel(i));
								alItems1.add(file);
								alItems2.add(file.getTrack());
							}
						}
					}
					new PropertiesWizard(alItems1, alItems2);
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
		boolean bEditable = ConfigurationManager.getBoolean(CONF_FILES_TABLE_EDITION);
		jtbEditable.setSelected(bEditable);
	}

}
