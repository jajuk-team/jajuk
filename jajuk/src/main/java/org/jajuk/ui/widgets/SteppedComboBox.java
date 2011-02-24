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
package org.jajuk.ui.widgets;

import java.awt.Dimension;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import org.jajuk.util.log.Log;

/**
 * Stepped combo box allowing to display a long text in the history bar.
 */

public class SteppedComboBox extends JComboBox {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  protected int popupWidth;

  /**
   * Instantiates a new stepped combo box.
   */
  public SteppedComboBox() {
    super();
    init();
  }

  /**
   * Instantiates a new stepped combo box.
   * 
   * @param aModel DOCUMENT_ME
   */
  public SteppedComboBox(ComboBoxModel aModel) {
    super(aModel);
    init();
  }

  /**
   * Instantiates a new stepped combo box.
   * 
   * @param items DOCUMENT_ME
   */
  public SteppedComboBox(final Object[] items) {
    super(items);
    init();
  }

  /**
   * Instantiates a new stepped combo box.
   * 
   * @param items DOCUMENT_ME
   */
  @SuppressWarnings("unchecked")
  public SteppedComboBox(List<?> items) {
    super(new Vector(items));
    init();
  }

  /**
   * Sets the popup width.
   * 
   * @param width the new popup width
   */
  public void setPopupWidth(int width) {
    popupWidth = width;
  }

  /**
   * Gets the popup size.
   * 
   * @return the popup size
   */
  public Dimension getPopupSize() {
    Dimension size = getSize();
    if (popupWidth < 1) {
      popupWidth = size.width;
    }
    return new Dimension(popupWidth, size.height);
  }

  /**
   * Inits the.
   * DOCUMENT_ME
   */
  protected final void init() {
    try {
      ComboBoxUI cbui = new org.jvnet.substance.SubstanceComboBoxUI() {
        @Override
        protected ComboPopup createPopup() {
          BasicComboPopup popup1 = new org.jajuk.ui.widgets.JajukBasicComboPopup(comboBox);
          popup1.getAccessibleContext().setAccessibleParent(comboBox);
          // Non opaque to avoid being transparent so we can't read
          // popup content over others text
          popup1.setOpaque(true);
          return popup1;
        }
      };

      setUI(cbui);
      popupWidth = 0;
    } catch (Exception e) {
      Log.error(e);
    }
  }
}
