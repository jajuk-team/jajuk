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
package org.qdwizard;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Screen Header
 * <p>
 * Contains a wizard title, a subtitle used to display the name of the current
 * screen and an optional background image
 * </p>.
 * 
 * @author Bertrand Florat
 * @created 1 may 2006
 */
@SuppressWarnings("serial")
class Header extends JPanel {
  private JPanel panel;
  private Image backgroundImage;
  private ImageIcon icon;
  private String title;
  private String subtitle;

  /**
   * Build a header 
   */
  public Header() {
    setLayout(new GridLayout());
    panel = new JPanel();
    panel.setOpaque(true);
    panel.setPreferredSize(new Dimension(0, 70));
    add(panel);
  }

  /**
   * Set the header Image.
   * 
   * @param img 
   */
  public void setBackgroundImage(Image img) {
    backgroundImage = img;
  }

  /**
   * Set the header title. The title is the screen name.
   * 
   * @param title the title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Set the header subtitle text. It is a text that can be changed at will among screens.
   * 
   * @param subtitle the subtitle 
   */
  public void setSubtitle(String subtitle) {
    this.subtitle = subtitle;
  }

  /**
   * Set the header right-side icon.
   * 
   * @param icon 
   */
  public void setIcon(ImageIcon icon) {
    this.icon = icon;
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  @Override
  public void paint(java.awt.Graphics g) {
    super.paint(g);
    Graphics2D g2D = (Graphics2D) g;
    java.awt.Rectangle rect = getBounds();
    g2D.setColor(java.awt.Color.WHITE);
    g2D.fillRect(rect.x, rect.y, rect.width, rect.height);
    if (backgroundImage != null) {
      g2D.drawImage(backgroundImage, 0, 0, rect.width, rect.height, this);
    }
    if (icon != null) {
      int h = icon.getIconHeight();
      int w = icon.getIconWidth();
      g2D.drawImage(icon.getImage(), rect.width - w - 10, (rect.height - h) / 2, w, h, this);
    }
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2D.setColor(java.awt.Color.BLACK);
    g2D.setFont(new Font("Dialog", Font.BOLD, 14));
    g2D.drawString(title, 20, 25);
    g2D.setFont(new Font("Dialog", Font.PLAIN, 13));
    g2D.drawString(subtitle, 20, 50);
    g2D.setColor(java.awt.Color.BLACK);
    g2D.drawLine(rect.x, rect.height - 1, rect.width, rect.height - 1);
  }
}