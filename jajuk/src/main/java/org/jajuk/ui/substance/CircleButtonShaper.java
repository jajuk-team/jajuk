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

import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import javax.swing.AbstractButton;

/*
 * based on code from Xtreme Media Player
 */
/**
 * The Class CircleButtonShaper.
 */
public class CircleButtonShaper extends ButtonShaper {

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#getDisplayName()
   */
  @Override
  public String getDisplayName() {
    return "Circle";
  }

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#getButtonOutline(javax.swing.AbstractButton, java.awt.Insets, int, int, boolean)
   */
  @Override
  public Shape getButtonOutline(AbstractButton button, Insets insets, int w, int h, boolean isInner) {
    int width = w - 1;
    int height = h - 1;

    Shape shape = new Ellipse2D.Double(0, 0, width, height);
    GeneralPath generalPath = new GeneralPath(shape);

    return generalPath;
  }

}
