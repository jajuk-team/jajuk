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
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
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
		super(Messages.getString("LogicalTreeView.33"), true); //$NON-NLS-1$
		setShortDescription(Messages.getString("LogicalTreeView.33")); //$NON-NLS-1$
	}

	public void perform(final ActionEvent e) throws JajukException {
		JComponent source = (JComponent)e.getSource();
		// Get required data from the tree (selected node and node type)
		final ArrayList<Item> alSelected = (ArrayList<Item>) source.getClientProperty(DETAIL_SELECTION);
		// Check we have at least one item
		if (alSelected.size() < 1) {
			return;
		}
		// First item
		final String type = (String)source.getClientProperty(DETAIL_ORIGIN);
		// Display a save as dialog
		final JajukFileChooser chooser = new JajukFileChooser();
		//Accept XML files
		chooser.addChoosableFileFilter(new JajukFileFilter(XMLFilter
				.getInstance()));
		//Accept HTML files
		chooser.addChoosableFileFilter(new JajukFileFilter(HTMLFilter
				.getInstance()));
		chooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home"))); //$NON-NLS-1$ 
		chooser.setDialogTitle(Messages.getString("LogicalTreeView.33"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// set a default file name
		if (XSLT_COLLECTION_LOGICAL.equals(type) 
				|| XSLT_COLLECTION_PHYSICAL.equals(type)) {
			// collection node selected, use file name 'collection"
			chooser.setSelectedFile(new java.io.File("collection"));
		} 
		else {
			// use the first node name
			Item item = alSelected.get(0);
			chooser.setSelectedFile(new java.io.File(item.getName()));
		}
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		// display the dialog
		int returnVal = chooser.showSaveDialog(null);
		//Wait for user selection
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// make it in a separated thread to avoid freezing
			// screen for big collections
			new Thread() {
				public void run() {
					java.io.File file = chooser.getSelectedFile();
					String filepath = file.getAbsolutePath();
					String filetypename = Util.getExtension(file);
					//Create an exporter according to file extension
					Exporter exporter = ExporterFactory.createExporter(filetypename);
					// Process the reporting string
					String result = null;
					// Full logical collection report
					if (XSLT_COLLECTION_LOGICAL.equals(type)) {
						exporter.processCollection(Exporter.LOGICAL_COLLECTION, alSelected);
					}
					// Full physical collection report
					else if (XSLT_COLLECTION_PHYSICAL.equals(type)) {
						exporter.processCollection(Exporter.LOGICAL_COLLECTION, alSelected);
					} 
					// Normal report on an item or a set of items
					else {
						exporter.processCollection(alSelected);
					}
					if (result != null) {
						// Save the results.
						if (!exporter.saveToFile(result, filepath)) {
							Messages.showErrorMessage("024");
						}
					} else {
						Messages.showErrorMessage("167");
					}
				}
			}.start();
		}

	}
}
