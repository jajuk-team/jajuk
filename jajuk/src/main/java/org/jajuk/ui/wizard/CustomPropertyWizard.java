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
package org.jajuk.ui.wizard;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.jajuk.base.AlbumManager;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.GenreManager;
import org.jajuk.base.ItemManager;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.TrackManager;
import org.jajuk.base.YearManager;
import org.jajuk.ui.perspectives.FilesPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;

/**
 * .
 */
public abstract class CustomPropertyWizard extends JajukJDialog implements ActionListener,
    ItemListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -5148687837661745898L;
  JLabel jlItemChoice;
  JComboBox jcbItemChoice;
  OKCancelPanel okp;
  JLabel jlName;

  /**
   * Constuctor.
   * 
   * @param sTitle 
   */
  CustomPropertyWizard(String sTitle) {
    setTitle(sTitle);
    setModal(true);
    setLocationRelativeTo(JajukMainWindow.getInstance());
  }

  /**
   * Create common UI for property wizards.
   */
  void populate() {
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
    UtilGUI.setShuffleLocation(this, 400, 400);
    jlItemChoice = new JLabel(Messages.getString("CustomPropertyWizard.0"));
    jlName = new JLabel(Messages.getString("CustomPropertyWizard.1"));
    jcbItemChoice = new JComboBox();
    // Note : we don't provide the possibility to add custom properties to AlbumArtists
    // (we don't see the need for it)
    jcbItemChoice.addItem(Messages.getString("Item_Track"));
    jcbItemChoice.addItem(Messages.getString("Item_File"));
    jcbItemChoice.addItem(Messages.getString("Item_Genre"));
    jcbItemChoice.addItem(Messages.getString("Item_Artist"));
    jcbItemChoice.addItem(Messages.getString("Item_Album"));
    jcbItemChoice.addItem(Messages.getString("Item_Device"));
    jcbItemChoice.addItem(Messages.getString("Item_Directory"));
    jcbItemChoice.addItem(Messages.getString("Item_Playlist_File"));
    jcbItemChoice.addItem(Messages.getString("Item_Year"));
    okp = new OKCancelPanel(this);
    okp.getOKButton().setEnabled(false);
    // In physical perspective, default item is file, otherwise, it is track
    if (PerspectiveManager.getCurrentPerspective().getClass().equals(FilesPerspective.class)) {
      jcbItemChoice.setSelectedIndex(1);
    } else {
      jcbItemChoice.setSelectedIndex(0);
    }
    jcbItemChoice.addItemListener(this);
  }

  /**
   * Gets the item manager.
   * 
   * @return ItemManager associated with selected element in combo box
   */
  ItemManager getItemManager() {
    ItemManager im = null;
    switch (jcbItemChoice.getSelectedIndex()) {
    case 0:
      im = TrackManager.getInstance();
      break;
    case 1:
      im = FileManager.getInstance();
      break;
    case 2:
      im = GenreManager.getInstance();
      break;
    case 3:
      im = ArtistManager.getInstance();
      break;
    case 4:
      im = AlbumManager.getInstance();
      break;
    case 5:
      im = DeviceManager.getInstance();
      break;
    case 6:
      im = DirectoryManager.getInstance();
      break;
    case 7:
      im = PlaylistManager.getInstance();
      break;
    case 8:
      im = YearManager.getInstance();
      break;
    }
    return im;
  }
}
