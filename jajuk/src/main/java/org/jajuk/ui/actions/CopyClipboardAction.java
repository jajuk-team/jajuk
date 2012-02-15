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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.Track;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Copy to clipboard the item absolute address.
 */
public class CopyClipboardAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new copy clipboard action.
   */
  CopyClipboardAction() {
    super(Messages.getString("CopyClipboardAction.0"), IconLoader
        .getIcon(JajukIcons.COPY_TO_CLIPBOARD), true);
    setShortDescription(Messages.getString("CopyClipboardAction.0"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent e) throws Exception {
    // This action expect either an item or a simple String from DETAIL_CONTENT
    // Swing client property
    JComponent source = (JComponent) e.getSource();
    Object o = source.getClientProperty(Const.DETAIL_CONTENT);
    String sData = "";
    if (o instanceof List<?>) {
      @SuppressWarnings("unchecked")
      List<Item> list = (List<Item>) o;
      if (list.size() > 0) {
        o = list.get(0);
      }
    }
    if (o instanceof Item) {
      Item item = (Item) o;
      if (item instanceof File) {
        sData = ((File) item).getAbsolutePath();
      } else if (item instanceof Directory) {
        sData = ((Directory) item).getAbsolutePath();
      } else if (item instanceof Playlist) {
        sData = ((Playlist) item).getAbsolutePath();
      } else if (item instanceof Track) {
        sData = ((Track) item).getFiles().get(0).getAbsolutePath();
      } else if (item instanceof WebRadio) {
        sData = ((WebRadio) item).getUrl();
      }
    } else if (o instanceof String) {
      sData = (String) o;
    }
    StringSelection data = new StringSelection(sData);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(data, data);
  }
}
