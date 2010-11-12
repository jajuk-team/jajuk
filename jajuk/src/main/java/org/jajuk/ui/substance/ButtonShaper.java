/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

package org.jajuk.ui.substance;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import javax.swing.AbstractButton;
import javax.swing.border.Border;

import org.jvnet.substance.SubstanceButtonUI;
import org.jvnet.substance.shaper.StandardButtonShaper;
import org.jvnet.substance.shaper.SubstanceButtonShaper;
import org.jvnet.substance.utils.border.SubstanceButtonBorder;

/*
 * based on code from Xtreme Media Player
 */
/**
 * The Class ButtonShaper.
 */
public abstract class ButtonShaper implements SubstanceButtonShaper {

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#getButtonOutline(javax.swing.AbstractButton)
   */
  @Override
  public Shape getButtonOutline(AbstractButton button) {
    return getButtonOutline(button, null, button.getWidth(), button.getHeight(), true);
  }

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#getButtonOutline(javax.swing.AbstractButton, java.awt.Insets)
   */
  @Override
  public GeneralPath getButtonOutline(AbstractButton button, Insets insets) {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#getButtonOutline(javax.swing.AbstractButton, java.awt.Insets, int, int)
   */
  @Override
  public GeneralPath getButtonOutline(AbstractButton button, Insets insets, int width, int height) {
    throw new UnsupportedOperationException();
  }

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#getPreferredSize(javax.swing.AbstractButton, java.awt.Dimension)
   */
  @Override
  public Dimension getPreferredSize(AbstractButton button, Dimension uiPreferredSize) {
    if (button.getClientProperty(SubstanceButtonUI.BORDER_COMPUTED) == null) {
      boolean isBorderComputing = (button.getClientProperty(SubstanceButtonUI.BORDER_COMPUTING) != null);
      Border border = button.getBorder();
      int uiw = uiPreferredSize.width;
      int uih = uiPreferredSize.height;
      Insets bi = border.getBorderInsets(button);
      if (!isBorderComputing) {
        button.setBorder(null);
      }
      uiPreferredSize.setSize(uiw - bi.left - bi.right, uih - bi.top - bi.bottom);

      if (!isBorderComputing) {
        button.setBorder(this.getButtonBorder(button));
        button.putClientProperty(SubstanceButtonUI.BORDER_COMPUTED, "");
      }
    }
    return uiPreferredSize;
  }

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#getButtonBorder(javax.swing.AbstractButton)
   */
  @Override
  public Border getButtonBorder(AbstractButton button) {
    return new SubstanceButtonBorder(StandardButtonShaper.class) {
      @Override
      public Insets getBorderInsets(Component c) {
        return new Insets(0, 0, 0, 0);
      }
    };
  }

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#isProportionate()
   */
  @Override
  public boolean isProportionate() {
    return true;
  }
}
