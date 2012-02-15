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
 *  
 */
package org.jajuk.ui.widgets;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Messages;

/**
 * Music-oriented file chooser.
 */
public class JajukFileChooser extends JFileChooser {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private final JajukFileFilter filter;

  /**
   * Constructor with specified file filter.
   * 
   * @param jfilter filter to use
   */
  public JajukFileChooser(JajukFileFilter jfilter) {
    super();

    this.filter = jfilter;
    for (int i = 0; i < jfilter.getFilters().length; i++) {
      addChoosableFileFilter(jfilter.getFilters()[i]);
    }

    init();
  }

  /**
   * Constructor with specified file filter and starting directory/file.
   * 
   * @param jfilter filter to use
   * @param file DOCUMENT_ME
   */
  public JajukFileChooser(JajukFileFilter jfilter, File file) {
    super(file);

    this.filter = jfilter;
    for (int i = 0; i < jfilter.getFilters().length; i++) {
      addChoosableFileFilter(jfilter.getFilters()[i]);
    }

    init();
  }

  /**
   * Inits the.
   */
  private final void init() {
    setDialogTitle(Messages.getString("JajukFileChooser.0"));
    setMultiSelectionEnabled(true);
    // don't hide hidden files
    setFileHidingEnabled(false);
    setAcceptAllFileFilterUsed(false);
    // Use default directory to store documents (My Documents under Windows
    // for ie)
    setCurrentDirectory(FileSystemView.getFileSystemView().getDefaultDirectory());
  }

  /**
   * Force the filter to accept directories.
   * 
   * @param b DOCUMENT_ME
   */
  public void setAcceptDirectories(boolean b) {
    for (int i = 0; i < filter.getFilters().length; i++) {
      filter.getFilters()[i].setAcceptDirectories(b);
    }
  }

  /**
   * Make sure to keep the dialog always on top.
   * 
   * @param parent DOCUMENT_ME
   * 
   * @return the j dialog
   * 
   * @throws HeadlessException the headless exception
   */
  @Override
  protected JDialog createDialog(Component parent) throws HeadlessException {
    JDialog dialog = super.createDialog(parent);
    dialog.setAlwaysOnTop(true);
    return dialog;

  }

}
