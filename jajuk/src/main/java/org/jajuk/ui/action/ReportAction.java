/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:20:46
 */
package org.jajuk.ui.action;

import org.jajuk.base.Item;
import org.jajuk.i18n.Messages;
import org.jajuk.reporting.Exporter;
import org.jajuk.reporting.ExporterFactory;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;
import org.jajuk.util.JajukFileFilter.ReportFilter;
import org.jajuk.util.error.JajukException;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

/**
 * Report collection as a file
 * 
 * @author Ronak Patel
 * @created Aug 20, 2006
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
		final JajukFileChooser chooser = new JajukFileChooser(new JajukFileFilter(ReportFilter
				.getInstance()));
		chooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home"))); //$NON-NLS-1$ 
		chooser.setDialogTitle(Messages.getString("LogicalTreeView.33"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// set a default file name
		if (XSLT_COLLECTION_LOGICAL.equals(type) || XSLT_COLLECTION_PHYSICAL.equals(type)) {
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
