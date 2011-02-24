/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.jajuk.base.Item;
import org.jajuk.services.reporting.Exporter;
import org.jajuk.services.reporting.ExporterFactory;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.HTMLFilter;
import org.jajuk.util.filters.XMLFilter;
import org.jajuk.util.log.Log;

/**
 * Report collection as a file.
 */
public class ReportAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new report action.
   */
  ReportAction() {
    super(Messages.getString("TracksTreeView.33"), IconLoader.getIcon(JajukIcons.REPORT), true);
    setShortDescription(Messages.getString("TracksTreeView.33"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void perform(final ActionEvent e) throws JajukException {
    final JComponent source = (JComponent) e.getSource();
    // First item
    final String type = (String) source.getClientProperty(Const.DETAIL_ORIGIN);
    // Get required data from the tree (selected node and node type)
    final List<Item> alSelected = (List<Item>) source.getClientProperty(Const.DETAIL_SELECTION);
    // Display a save as dialog
    final JajukFileFilter filter = new JajukFileFilter(XMLFilter.getInstance(), HTMLFilter
        .getInstance());
    final JajukFileChooser chooser = new JajukFileChooser(filter);
    // Allow to navigate between directories
    chooser.setAcceptDirectories(true);
    chooser.setDialogTitle(Messages.getString("TracksTreeView.33"));
    // set a default file name
    if (Const.COLLECTION_LOGICAL.equals(type) || Const.COLLECTION_PHYSICAL.equals(type)) {
      // collection node selected, use file name 'collection"
      chooser.setSelectedFile(new java.io.File(Messages.getString("ReportAction.17")));
    } else {
      // use the first node name
      final Item item = alSelected.get(0);
      // Use html format as a default
      chooser.setSelectedFile(new java.io.File(item.getName()));
    }
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    // display the dialog
    final int returnVal = chooser.showSaveDialog(null);
    // Wait for user selection
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      // Make sure user didn't select a directory (we have to accept
      // them to allow user to navigate)
      if (chooser.getSelectedFile().isDirectory()) {
        return;
      }
      // make it in a separated thread to avoid freezing
      // screen for big collections
      new Thread("Report Thread") {
        @Override
        public void run() {
          try {
            UtilGUI.waiting();
            final String filepath = chooser.getSelectedFile().getAbsolutePath();
            final String filetypename = chooser.getFileFilter().getDescription();
            // Create an exporter according to file extension
            final Exporter exporter = ExporterFactory.createExporter(filetypename);
            // Full logical collection report
            if (Const.COLLECTION_LOGICAL.equals(type)) {
              exporter.processCollection(Exporter.LOGICAL_COLLECTION);
            }
            // Full physical collection report
            else if (Const.COLLECTION_PHYSICAL.equals(type)) {
              exporter.processCollection(Exporter.PHYSICAL_COLLECTION);
            }
            // Normal report on an item or a set of items
            else {
              exporter.process(alSelected);
            }
            // Save the results
            String filename = filepath;
            // Append extension only if needed.
            // (if user selected an existing item, the extension
            // musn't be appended twice)
            if (!filepath.endsWith(filetypename)) {
              filename = filepath + '.' + filetypename;
            }
            // Save created report
            exporter.saveToFile(filename);
            // Success
            Messages.showInfoMessage(Messages.getString("ReportAction.0"));
          } catch (final Exception e) {
            Log.error(e);
            Messages.showErrorMessage(167, e.getMessage());
          } finally {
            UtilGUI.stopWaiting();
          }
        }
      }.start();
    }

  }
}
