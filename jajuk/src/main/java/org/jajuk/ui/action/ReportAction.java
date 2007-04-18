/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:20:46
 */
package org.jajuk.ui.action;

import org.jajuk.base.AuthorManager;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.reporting.HTMLExporter;
import org.jajuk.util.error.JajukException;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComponent;

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
		System.out.println(((JComponent)e.getSource()).getClientProperty(DETAIL_ORIGIN));
		HTMLExporter htmlExporter = HTMLExporter.getInstance();
		String result = htmlExporter.process(AuthorManager.getInstance().getAuthors().iterator().next());
		
		final ArrayList<Track> alSelected = (ArrayList<Track>)getValue(DETAIL_SELECTION);
		final String type = (String)getValue(DETAIL_ORIGIN);
	/*	final int iSortOrder = ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER);
		// Create filters.
		ExportFileFilter xmlFilter = new ExportFileFilter(".xml");
		ExportFileFilter htmlFilter = new ExportFileFilter(".html");
		// ExportFileFilter pdfFilter = new
		
		// ExportFileFilter(".pdf");

		final JFileChooser filechooser = new JFileChooser();
		// Add filters.
		filechooser.addChoosableFileFilter(xmlFilter);
		filechooser.addChoosableFileFilter(htmlFilter);
		// filechooser.addChoosableFileFilter(pdfFilter);

		filechooser.setCurrentDirectory(new java.io.File(System.getProperty("user.home"))); //$NON-NLS-1$ 
		filechooser.setDialogTitle(Messages.getString("LogicalTreeView.33"));
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// set a default file name
		if (alSelected.size() == 1) {
			Item item = alSelected.get(0);
			filechooser.setSelectedFile(new java.io.File(item.getName()));
		} else if (alSelected.size() > 1) {
			// collection node selected
			filechooser.setSelectedFile(new java.io.File("collection"));
		}
		filechooser.setDialogType(JFileChooser.SAVE_DIALOG);

		int returnVal = filechooser.showSaveDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// make it in a separated thread to avoid freezing
			// screen for big collections
			new Thread() {
				public void run() {

					java.io.File file = filechooser.getSelectedFile();

					String filepath = file.getAbsolutePath();
					String filetypename = Util.getExtension(file);

					if (filetypename.equals("")) {
						ExportFileFilter filter = (ExportFileFilter) filechooser.getFileFilter();
						filetypename = filter.getExtension();
						filepath += "." + filetypename;
					}

					String result = null; //$NON-NLS-1$                		

					// If we are exporting to xml...
					if (filetypename.equals("xml")) { //$NON-NLS-1$
						XMLExporter xmlExporter = XMLExporter.getInstance();

						// If we are exporting a album...
						if ("album".equals(type)) {
							PopulatedAlbum album = 
							result = xmlExporter.process(album);
							// Else if we are exporting an author in
							// any
							// other view...
						} else if (e.getSource() == jmiAuthorExport) {
							PopulatedAuthor author = LogicalTreeUtilities
									.getPopulatedAuthorFromTree((DefaultMutableTreeNode) paths[0]
											.getLastPathComponent());
							result = xmlExporter.process(author);
							// Else if we are exporting a style...
						} else if (e.getSource() == jmiStyleExport) {
							PopulatedStyle style = LogicalTreeUtilities
									.getPopulatedStyleFromTree((DefaultMutableTreeNode) paths[0]
											.getLastPathComponent());
							result = xmlExporter.process(style);
							// Else if we are exporting a
							// collection...
						} else if (e.getSource() == jmiCollectionExport) {
							// If we are exporting the styles...
							if (iSortOrder == 0) {
								DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0]
										.getLastPathComponent();
								ArrayList collection = LogicalTreeUtilities
										.getStyleCollectionFromTree(node);
								result = xmlExporter.processCollection(
										XMLExporter.LOGICAL_GENRE_COLLECTION, collection);
								// Else if we are exporting the
								// authors...
							} else if (iSortOrder == 1) {
								DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0]
										.getLastPathComponent();
								ArrayList collection = LogicalTreeUtilities
										.getAuthorCollectionFromTree(node);
								result = xmlExporter.processCollection(
										XMLExporter.LOGICAL_ARTIST_COLLECTION, collection);
								// Else if we are exporting the
								// albums...
							} else if (iSortOrder == 2) {
								DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0]
										.getLastPathComponent();
								ArrayList collection = LogicalTreeUtilities
										.getAlbumCollectionFromTree(node);
								result = xmlExporter.processCollection(
										XMLExporter.LOGICAL_ALBUM_COLLECTION, collection);
							}
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
