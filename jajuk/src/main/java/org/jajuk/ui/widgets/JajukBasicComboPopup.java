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

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicComboPopup;

/**
 * Basic combo popup.
 * 
 * @created 28 nov. 2003
 */
public class JajukBasicComboPopup extends BasicComboPopup {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new jajuk basic combo popup.
   * 
   * @param jcb 
   */
  public JajukBasicComboPopup(JComboBox jcb) {
    super(jcb);
  }

  /* (non-Javadoc)
   * @see javax.swing.plaf.basic.BasicComboPopup#show()
   */
  @Override
  public void show() {
    Dimension popupSize = ((SteppedComboBox) comboBox).getPopupSize();
    popupSize.setSize(popupSize.width, getPopupHeightForRowCount(comboBox.getMaximumRowCount()));
    Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height, popupSize.width,
        popupSize.height);
    scroller.setMaximumSize(popupBounds.getSize());
    scroller.setPreferredSize(popupBounds.getSize());
    scroller.setMinimumSize(popupBounds.getSize());
    list.invalidate();
    int selectedIndex = comboBox.getSelectedIndex();
    if (selectedIndex == -1) {
      list.clearSelection();
    } else {
      list.setSelectedIndex(selectedIndex);
    }
    list.ensureIndexIsVisible(list.getSelectedIndex());
    setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
    show(comboBox, popupBounds.x, popupBounds.y);
  }

  /* (non-Javadoc)
   * @see javax.swing.plaf.basic.BasicComboPopup#createScroller()
   */
  @Override
  protected JScrollPane createScroller() {
    return new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }
}
