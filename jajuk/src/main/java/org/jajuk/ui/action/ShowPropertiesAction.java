/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

/**
 * Display properties action. Allows displaying properties on single or multiple items of the same type.
 * <p>Action emitter is responsible to ensure all items provided share the same type</p>
 * <p>Selection data is provided using the swing properties DETAIL_SELECTION</p>
 */
public class ShowPropertiesAction extends ActionBase {

  private static final long serialVersionUID = -8078402652430413821L;

  ShowPropertiesAction() {
    super(Messages.getString("TracksTableView.14"), IconLoader.ICON_PROPERTIES, true);
    setShortDescription(Messages.getString("ShowPropertiesAction.0"));
  }
  
  /* (non-Javadoc)
   * @see org.jajuk.ui.action.ActionBase#perform(java.awt.event.ActionEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void perform(ActionEvent e) throws Exception {
    JComponent source = (JComponent) e.getSource();
    final ArrayList<Item> alSelection = (ArrayList<Item>) source.getClientProperty(DETAIL_SELECTION);
    if (alSelection.size() == 0){
      return;
    }
    //If selection contains files, we have to show the tracks along with files
    if (alSelection.get(0) instanceof File){
      List<Item> tracks = new ArrayList<Item>(alSelection.size());
      for (Item file: alSelection){
        tracks.add(((File)file).getTrack());
      }
      new PropertiesWizard(alSelection,tracks);
    }
    else{
      //All others types: just display the properties window
      new PropertiesWizard(alSelection);  
    }
    
  }

}
