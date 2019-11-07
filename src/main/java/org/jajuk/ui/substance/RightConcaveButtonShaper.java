/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
package org.jajuk.ui.substance;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;

/*
 * based on code from Xtreme Media Player
 */
/**
 * The Class RightConcaveButtonShaper.
 */
public class RightConcaveButtonShaper extends ButtonShaper {
  private int concaveDiameter;

  /**
   * Instantiates a new right concave button shaper.
   * 
   * @param concaveDiameter 
   */
  public RightConcaveButtonShaper(int concaveDiameter) {
    super();
    this.concaveDiameter = concaveDiameter;
  }

  /* (non-Javadoc)
   * @see org.jvnet.substance.shaper.SubstanceButtonShaper#getDisplayName()
   */
  @Override
  public String getDisplayName() {
    return "RightConcave";
  }

  /* (non-Javadoc)
   * @see org.pushingpixels.substance.api.shaper.SubstanceButtonShaper#getButtonOutline(javax.swing.AbstractButton, float, float, float, boolean)
   */
  @Override
  public Shape getButtonOutline(AbstractButton arg0, float arg1, float w, float h,
      boolean arg4) {
    int width = (int)w - 1;
    int height = (int)h - 1;
    int z = concaveDiameter / 3;
    Shape shape = new Ellipse2D.Double(width - z, 0, z, height);
    Area area = new Area(new RoundRectangle2D.Double(z / 2d, 0, width - z, height, z, z));
    area.subtract(new Area(shape));
    return new GeneralPath(area);
  }
}
