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
 *  $$Revision: 2403 $$
 */
package org.jajuk.ui.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;

import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

/**
 * 
 * Copy to clipboard the string stored in Util.copyData
 */
public class CopyClipboardAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  CopyClipboardAction() {
    super(Messages.getString("CopyClipboardAction.0"), IconLoader.ICON_COPY, true);
    setShortDescription(Messages.getString("CopyClipboardAction.0"));
  }

  public void perform(ActionEvent evt) throws JajukException {
    StringSelection data = new StringSelection(Util.copyData);
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(data, data);
  }
}
