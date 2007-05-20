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
 *  $$Revision: 2321 $$
 */
package org.jajuk.ui.action;

import org.jajuk.base.Item;
import org.jajuk.i18n.Messages;
import org.jajuk.reporting.Exporter;
import org.jajuk.reporting.ExporterFactory;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukFileFilter.HTMLFilter;
import org.jajuk.util.JajukFileFilter.XMLFilter;
import org.jajuk.util.error.JajukException;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * Report collection as a file
 */
public class ReportAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	ReportAction() {
		super(Messages.getString("LogicalTreeView.33"), IconLoader.ICON_REPORT,true); 
		setShortDescription(Messages.getString("LogicalTreeView.33")); 
	}

	@SuppressWarnings("unchecked")
	public void perform(final ActionEvent e) throws JajukException {
		JComponent source = (JComponent) e.getSource();
		// First item
		final String type = (String) source.getClientProperty(DETAIL_ORIGIN);
		// Get required data from the tree (selected node and node type)
		final ArrayList<Item> alSelected = (ArrayList<Item>) source
				.getClientProperty(DETAIL_SELECTION);
		// Display a save as dialog
		final JajukFileChooser chooser = new JajukFileChooser();
		JajukFileFilter filter1 = new JajukFileFilter(XMLFilter.getInstance());
		//Allow to navigate between directories
		filter1.setAcceptDirectories(true);
		JajukFileFilter filter2 = new JajukFileFilter(HTMLFilter.getInstance());
		filter2.setAcceptDirectories(true);
		// Accept XML files
		chooser.addChoosableFileFilter(filter1);
		// Accept HTML files
		chooser.addChoosableFileFilter(filter2);
		chooser.setDialogTitle(Messages.getString("LogicalTreeView.33"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// set a default file name
		if (COLLECTION_LOGICAL.equals(type) || COLLECTION_PHYSICAL.equals(type)) {
			// collection node selected, use file name 'collection"
			chooser.setSelectedFile(new java.io.File(Messages.getString("ReportAction.17")));
		} else {
			// use the first node name
			Item item = alSelected.get(0);
			// Use html format as a default
			chooser.setSelectedFile(new java.io.File(item.getName()));
		}
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		// display the dialog
		int returnVal = chooser.showSaveDialog(null);
		// Wait for user selection
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			//Make sure user didn't select a directory
			if (chooser.getSelectedFile().isDirectory()){
				return;
			}
			// make it in a separated thread to avoid freezing
			// screen for big collections
			new Thread() {
				public void run() {
					String filepath = chooser.getSelectedFile().getAbsolutePath();
					String filetypename = chooser.getFileFilter().getDescription();
					// Create an exporter according to file extension
					Exporter exporter = ExporterFactory.createExporter(filetypename);
					// Process the reporting string
					String result = null;
					// Full logical collection report
					if (COLLECTION_LOGICAL.equals(type)) {
						result = exporter.processCollection(Exporter.LOGICAL_COLLECTION);
					}
					// Full physical collection report
					else if (COLLECTION_PHYSICAL.equals(type)) {
						result = exporter.processCollection(Exporter.PHYSICAL_COLLECTION);
					}
					// Normal report on an item or a set of items
					else {
						result = exporter.process(alSelected);
					}
					if (result != null) {
						// Save the results
						String filename = filepath;
						// Append extension only if needed.
						// (if user selected an existing item, the extension
						// musn't be appended twice)
						if (!filepath.endsWith(filetypename)) {
							filename = filepath + '.' + filetypename;
						}
						if (!exporter.saveToFile(result, filename)) {
							Messages.showErrorMessage("024");
						} else {
							// Success
							Messages.showInfoMessage(Messages.getString("ReportAction.0"));
						}
					} else {
						Messages.showErrorMessage("167");
					}
				}
			}.start();
		}

	}
}
