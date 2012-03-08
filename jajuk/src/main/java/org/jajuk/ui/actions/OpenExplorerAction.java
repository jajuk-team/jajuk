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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Open directory in default explorer program.
 */
public class OpenExplorerAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new open explorer action.
   */
  OpenExplorerAction() {
    super(Messages.getString("OpenExplorerAction.0"), IconLoader.getIcon(JajukIcons.OPEN_EXPLORER),
        true);
    setShortDescription(Messages.getString("OpenExplorerAction.1"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void perform(ActionEvent e) throws Exception {
    // This action expect either an item or a simple String from DETAIL_CONTENT
    // Swing client property
    JComponent source = (JComponent) e.getSource();
    try {
      List<Item> selection = (List<Item>) source.getClientProperty(Const.DETAIL_CONTENT);
      if(selection.get(0) instanceof Directory){
        Directory dir = (Directory) selection.get(0);
        UtilSystem.openInExplorer(dir.getFio());
      }
      else if(selection.get(0) instanceof File){
        File f = (File) selection.get(0);
        UtilSystem.openInExplorer(f.getDirectory().getFio());
      }
      else if(selection.get(0) instanceof Playlist){
        Playlist pl = (Playlist) selection.get(0);
        UtilSystem.openInExplorer(pl.getDirectory().getFio());
      }  
    } catch (Exception ex) {
      Log.error(ex);
    }
  }
}
