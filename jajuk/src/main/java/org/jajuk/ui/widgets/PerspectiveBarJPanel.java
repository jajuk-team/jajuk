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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jdesktop.swingx.JXPanel;

/**
 * Menu bar used to choose the current perspective.
 */
public final class PerspectiveBarJPanel extends JXPanel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Perspectives tool bar*. */
  private JToolBar jtbPerspective;

  /** Self instance. */
  private static PerspectiveBarJPanel pb = new PerspectiveBarJPanel();

  /** Perspective button. */
  private final List<JButton> alButtons = new ArrayList<JButton>(10);

  /**
   * Singleton access.
   * 
   * @return the instance
   */
  public static PerspectiveBarJPanel getInstance() {
    return pb;
  }

  /**
   * Constructor for PerspectiveBarJPanel.
   */
  private PerspectiveBarJPanel() {
    update();
  }

  /**
   * update contents.
   */
  public void update() {
    // Perspectives tool bar
    jtbPerspective = new JajukJToolbar(SwingConstants.VERTICAL);
    Iterator<IPerspective> it = PerspectiveManager.getPerspectives().iterator();
    while (it.hasNext()) {
      final IPerspective perspective = it.next();
      Font font = FontManager.getInstance().getFont(JajukFont.PERSPECTIVES);
      int iconSize = Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE);

      // resize if necessary
      ImageIcon icon = perspective.getIcon();
      if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
        icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
      }

      JButton jb = new JButton(icon);
      jb.setToolTipText(perspective.getDesc());
      jb.setBorder(new EmptyBorder(5, 5, 0, 5));
      if (iconSize >= 32) {
        int glyphSize = font.getSize();
        // Limit perspective label to icon width
        String desc = UtilString
            .getLimitedString(perspective.getDesc(), 3 + (iconSize / glyphSize));
        // No text for icon < 32 pixels in width: too narrow
        jb.setText(desc);
      }
      jb.setVerticalTextPosition(SwingConstants.BOTTOM);
      jb.setHorizontalTextPosition(SwingConstants.CENTER);
      jb.setFont(font);
      jb.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // no thread, it causes ugly screen repaint
          PerspectiveManager.setCurrentPerspective(perspective.getID());
        }
      });
      jtbPerspective.add(jb);
      alButtons.add(jb);
    }
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    JScrollPane jsp = new JScrollPane(jtbPerspective);
    jsp.setBorder(null);
    jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    add(jsp);
  }

  /**
   * Show selected perspective.
   * 
   * @param perspective 
   */
  public void setActivated(IPerspective perspective) {
    Collection<IPerspective> perspectives = PerspectiveManager.getPerspectives();
    Iterator<JButton> it = alButtons.iterator();
    Iterator<IPerspective> it2 = perspectives.iterator();
    while (it.hasNext()) {
      final JButton jb = it.next();
      IPerspective perspective2 = it2.next();
      if (perspective2.equals(perspective)) { // this perspective is
        // selected
        jb.setSelected(true);
      } else {
        jb.setSelected(false);
      }
    }
  }

  /**
   * ToString() method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return getClass().getName();
  }
}