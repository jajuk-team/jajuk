/*
 * aTunes 1.14.0
 * Copyright (C) 2006-2009 Alex Aranda, Sylvain Gaudard, Thomas Beckers and contributors
 *
 * See http://www.atunes.org/wiki/index.php?title=Contributing for information about contributors
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 *  Adapted by Jajuk team
 *  Copyright (C) 2003 The Jajuk Team
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
 * $Revision: 4770 $
 */

package org.jajuk.ui.widgets;

import com.jhlabs.image.PerspectiveFilter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.jajuk.util.UtilGUI;

/**
 * The Class Cover3D.
 */
public class Cover3D extends JPanel {

  /** The Constant angle. */
  private static final int angle = 30;

  /** The Constant gap. */
  private static final int gap = 10;

  /** The Constant opacity. */
  private static final float opacity = 0.3f;

  /** The Constant fadeHeight. */
  private static final float fadeHeight = 0.6f;

  /** The image. */
  private BufferedImage image;

  /** The reflected image. */
  private BufferedImage reflectedImage;

  /**
   * Instantiates a new cover3 d.
   */
  public Cover3D() {
    super();
    setOpaque(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  public void paintComponent(Graphics g) {
    if (image == null) {
      super.paintComponent(g);
      return;
    }

    Graphics2D g2d = (Graphics2D) g;
    int width = getWidth();
    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    g2d.translate((width - imageWidth) / 2, 0);
    g2d.drawRenderedImage(image, null);

    g2d.translate(0, 2 * imageHeight + gap);
    g2d.scale(1, -1);
    g2d.drawRenderedImage(reflectedImage, null);
  }

  /**
   * Sets the image.
   * 
   * @param imageScaled
   *          the image to set
   */
  public void setImage(Image image) {
    if (image != null) {
      int size = (int) (getHeight() / 1.5);
      if (size <= 0) {
        size = 350;
      }

      // IMAGE
      ImageIcon imageScaled = UtilGUI.scaleImageBicubic(image, size, size);

      this.image = UtilGUI.toBufferedImage(imageScaled.getImage(), true);

      PerspectiveFilter filter1 = new PerspectiveFilter(0, angle, size - angle / 2,
          (int) (angle * (5.0 / 3.0)), size - angle / 2, size, 0, size + angle);
      this.image = filter1.filter(this.image, null);

      // REFLECTED IMAGE
      int imageWidth = this.image.getWidth();
      int imageHeight = this.image.getHeight();
      BufferedImage reflection = new BufferedImage(imageWidth, imageHeight,
          BufferedImage.TYPE_INT_ARGB);
      Graphics2D rg = reflection.createGraphics();
      rg.drawRenderedImage(this.image, null);
      rg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
      rg.setPaint(new GradientPaint(0, imageHeight * fadeHeight, new Color(0.0f, 0.0f, 0.0f, 0.0f),
          0, imageHeight, new Color(0.0f, 0.0f, 0.0f, opacity)));
      rg.fillRect(0, 0, imageWidth, imageHeight);
      rg.dispose();

      PerspectiveFilter filter2 = new PerspectiveFilter(0, 0, size - angle / 2, angle * 2, size
          - angle / 2, size + angle * 2, 0, size);
      reflectedImage = filter2.filter(reflection, null);
    } else {
      this.image = null;
      this.reflectedImage = null;
    }
    this.invalidate();
    this.repaint();
  }
}
