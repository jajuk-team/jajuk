/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

package org.jajuk.ui.widgets;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.filters.DirectoryFilter;

/**
 * This is a widgets that contains an editable text field given a PATH and a
 * PATH selection button opening up a file selector.
 */
public class PathSelector extends JPanel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -8370527665529267952L;

  /** DOCUMENT_ME. */
  JTextField jtfUrl;

  /** DOCUMENT_ME. */
  JButton button;

  /**
   * Construct a Path Selector.
   * 
   * @param filter the filter used to select the item
   * @param sDefault Initialized path, null of none
   */
  public PathSelector(final JajukFileFilter filter, final String sDefault) {
    super();

    initUI(sDefault);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JajukFileChooser jfc = new JajukFileChooser(filter);
        jfc.setAcceptDirectories(true);
        jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));
        jfc.setMultiSelectionEnabled(false);
        final String sUrl = jtfUrl.getText();
        if (!sUrl.isEmpty()) {
          // if URL is already set, use it as current directory
          jfc.setCurrentDirectory(new File(sUrl));
        }
        final int returnVal = jfc.showOpenDialog(JajukMainWindow.getInstance());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          final String previousURL = jtfUrl.getText();
          final java.io.File file = jfc.getSelectedFile();
          final String newPath = file.getAbsolutePath();
          jtfUrl.setText(newPath);
          // Call specific operation if URL changed
          if (!previousURL.equals(newPath)) {
            performOnURLChange();
          }
        }
      }
    });
  }

  /**
   * Construct a Path Selector for directory selection.
   * 
   * @param sDefault Initialized path, null of none
   */
  public PathSelector(final String sDefault) {
    super();

    initUI(sDefault);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(DirectoryFilter
            .getInstance()));
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));
        jfc.setMultiSelectionEnabled(false);
        final String sUrl = jtfUrl.getText();
        if (!sUrl.isEmpty()) {
          // if URL is already set, use it as current directory
          jfc.setCurrentDirectory(new File(sUrl));
        }
        final int returnVal = jfc.showOpenDialog(JajukMainWindow.getInstance());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          final String previousURL = jtfUrl.getText();
          final java.io.File file = jfc.getSelectedFile();
          final String newPath = file.getAbsolutePath();
          jtfUrl.setText(newPath);
          // Call specific operation if URL changed
          if (!previousURL.equals(newPath)) {
            performOnURLChange();
          }
        }
      }
    });
  }

  /**
   * Gets the url.
   * 
   * @return URL
   */
  public String getUrl() {
    return jtfUrl.getText();
  }

  /**
   * Inits the ui.
   * DOCUMENT_ME
   * 
   * @param sDefault DOCUMENT_ME
   */
  private void initUI(final String sDefault) {
    // Set layout
    setLayout(new MigLayout("ins 0", "[grow][]"));
    // Build items
    jtfUrl = new JTextField();
    if (sDefault != null) {
      jtfUrl.setText(sDefault);
    }
    jtfUrl.setToolTipText(Messages.getString("Path"));
    jtfUrl.setBorder(BorderFactory.createLineBorder(Color.BLUE));
    button = new JButton(IconLoader.getIcon(JajukIcons.OPEN_FILE));
    button.setToolTipText(Messages.getString("Path"));
    // Add items
    add(jtfUrl, "grow,gapx 5");
    add(button);
  }

  /**
   * This method can be extended to perform specific actions when selected
   * changes URL.
   */
  public void performOnURLChange() {
    // empty on purpose...
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#setEnabled(boolean)
   */
  @Override
  public void setEnabled(final boolean b) {
    jtfUrl.setEnabled(b);
    button.setEnabled(b);
  }

  /**
   * Set tooltip.
   * 
   * @param s DOCUMENT_ME
   */
  @Override
  public void setToolTipText(final String s) {
    jtfUrl.setToolTipText(s);
    button.setToolTipText(s);
  }

  /**
   * Sets the uRL.
   * 
   * @param sURL the new uRL
   */
  public void setURL(final String sURL) {
    jtfUrl.setText(sURL);
  }

}
