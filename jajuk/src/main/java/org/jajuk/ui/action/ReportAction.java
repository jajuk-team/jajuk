/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:20:46
 */
package org.jajuk.ui.action;

import org.jajuk.base.AuthorManager;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.i18n.Messages;
import org.jajuk.reporting.ExporterFactory;
import org.jajuk.reporting.HTMLExporter;
import org.jajuk.reporting.XMLExporter;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.ui.views.LogicalTreeView;
import org.jajuk.util.ConfigurationManager;
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
		// Get required data from the tree (selected node and node type) and
		// check
		// selection contains at least one element
		final ArrayList<Track> alSelected = (ArrayList<Track>)getValue(DETAIL_SELECTION);
		// we manage only one item (can be a node or the entire collection)
		if (alSelected.size() != 1) {
			return;
		}
		// First item
		final Item selected = alSelected.get(0);
		final String type = (String)getValue(DETAIL_ORIGIN);
		// Display a save as dialog
		
		
		/*
		 * HTMLExporter htmlExporter = HTMLExporter.getInstance(); String result =
		 * htmlExporter.process(AuthorManager.getInstance().getAuthors().iterator().next()); //
		 * Create filters. ExportFileFilter xmlFilter = new
		 * ExportFileFilter(".xml"); ExportFileFilter htmlFilter = new
		 * ExportFileFilter(".html"); // ExportFileFilter pdfFilter = new //
		 * ExportFileFilter(".pdf");
		 */

		final JajukFileChooser chooser = new JajukFileChooser(new JajukFileFilter(ReportFilter.getInstance()));
		chooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home"))); //$NON-NLS-1$ 
		chooser.setDialogTitle(Messages.getString("LogicalTreeView.33"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// set a default file name
		if (XML_COLLECTION.equals(type)) {
			// collection node selected, use file name 'collection"
			chooser.setSelectedFile(new java.io.File("collection"));
		} else if (alSelected.size() > 1) {
			// use the node name
			Item item = alSelected.get(0);
			chooser.setSelectedFile(new java.io.File(item.getName()));
		}
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		// display the dialog
		int returnVal = chooser.showSaveDialog(null);
/*
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// make it in a separated thread to avoid freezing
			// screen for big collections
			new Thread() {
				public void run() {
					java.io.File file = chooser.getSelectedFile();
					String filepath = file.getAbsolutePath();
					String filetypename = Util.getExtension(file);
					int iSortOrder = ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER);
						XMLExporter xmlExporter = ExporterFactory.createExporter(filetypename);
						// Process the reporting string
						String result = null;
						if (XML_COLLECTION.equals(type)) {
							// If we are exporting the styles...
							if (iSortOrder == TrackComparator.STYLE_AUTHOR_ALBUM) {
								result = xmlExporter.processCollection(
										XMLExporter.LOGICAL_GENRE_COLLECTION, collection);
								// Else if we are exporting the
								// authors...
							} else if (iSortOrder == TrackComparator.STYLE_AUTHOR_ALBUM) {
								result = xmlExporter.processCollection(
										XMLExporter.LOGICAL_ARTIST_COLLECTION, collection);
								// Else if we are exporting the
								// albums...
							} else if (iSortOrder == TrackComparator.ALBUM) {
								result = xmlExporter.processCollection(
										XMLExporter.LOGICAL_ALBUM_COLLECTION, collection);
						} else if (iSortOrder == TrackComparator.YEAR_ALBUM) {
							// @TODO
					} else if (iSortOrder == TrackComparator.DISCOVERY_ALBUM) {
							// @TODO
					}
	
							// single item reporting
						} else {
							result = xmlExporter.process(selected);
						}
						if (result != null) {
							// Save the results.
							if (!xmlExporter.saveToFile(result, filepath)) {
								Log.error("Could not write out the xml to the specified file.");
							}
						} else {
							Log.error("Could not create report.");
						}
						// Else if we are exporting to html...
					} else if (filetypename.equals("html") || filetypename.equals("htm")) {
						HTMLExporter htmlExporter = HTMLExporter.getInstance();

						// If we are exporting an album...
						if (e.getSource() == jmiAlbumExport) {
							PopulatedAlbum album = LogicalTreeUtilities
									.getPopulatedAlbumFromTree((DefaultMutableTreeNode) paths[0]
											.getLastPathComponent());
							result = htmlExporter.process(album);
							// Else if we are exporting an author in
							// any
							// other view...
						} else if (e.getSource() == jmiAuthorExport) {
							PopulatedAuthor author = LogicalTreeUtilities
									.getPopulatedAuthorFromTree((DefaultMutableTreeNode) paths[0]
											.getLastPathComponent());
							result = htmlExporter.process(author);
							// Else if we are exporting a style...
						} else if (e.getSource() == jmiStyleExport) {
							PopulatedStyle style = LogicalTreeUtilities
									.getPopulatedStyleFromTree((DefaultMutableTreeNode) paths[0]
											.getLastPathComponent());
							result = htmlExporter.process(style);
							// Else if we are exporting a
							// collection...
						} else if (e.getSource() == jmiCollectionExport) {
							// If we are exporting the styles...
							if (iSortOrder == 0) {
								DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0]
										.getLastPathComponent();
								ArrayList collection = LogicalTreeUtilities
										.getStyleCollectionFromTree(node);
								result = htmlExporter.processCollection(
										HTMLExporter.LOGICAL_GENRE_COLLECTION, collection);
								// Else if we are exporting the
								// authors...
							} else if (iSortOrder == 1) {
								DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0]
										.getLastPathComponent();
								ArrayList collection = LogicalTreeUtilities
										.getAuthorCollectionFromTree(node);
								result = htmlExporter.processCollection(
										HTMLExporter.LOGICAL_ARTIST_COLLECTION, collection);
								// Else if we are exporting the
								// albums...
							} else if (iSortOrder == 2) {
								DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0]
										.getLastPathComponent();
								ArrayList collection = LogicalTreeUtilities
										.getAlbumCollectionFromTree(node);
								result = htmlExporter.processCollection(
										HTMLExporter.LOGICAL_ALBUM_COLLECTION, collection);
							}
						}

						if (result != null) {
							// Save the results.
							if (!htmlExporter.saveToFile(result, filepath)) {
								Log.error("Could not write out the xml to the specified file.");
							}
						} else {
							Log.error("Could not create report.");
						}
					}
				}
			}.start();
		}*/
	
	
	}
}
