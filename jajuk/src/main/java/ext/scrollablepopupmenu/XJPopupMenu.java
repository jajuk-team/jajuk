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
package ext.scrollablepopupmenu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSeparatorUI;

/**
 * This class implements a scrollable Popup Menu.
 * 
 * @author balajihe from
 * http://www.beginner-java-tutorial.com/scrollable-jpopupmenu.html
 */
public class XJPopupMenu extends JPopupMenu implements ActionListener {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1;

  /** DOCUMENT_ME. */
  private final JPanel panelMenus = new JPanel();

  /** DOCUMENT_ME. */
  private JScrollPane scroll = null;

  /** DOCUMENT_ME. */
  private JFrame jframe = null;

  /** The Constant EMPTY_IMAGE_ICON.  DOCUMENT_ME */
  public static final Icon EMPTY_IMAGE_ICON = new ImageIcon("menu_spacer.gif");

  /**
   * Instantiates a new xJ popup menu.
   * 
   * @param jframe DOCUMENT_ME
   */
  public XJPopupMenu(JFrame jframe) {
    super();
    this.jframe = jframe;
    this.setLayout(new BorderLayout());
    panelMenus.setLayout(new GridLayout(0, 1));
    panelMenus.setBackground(UIManager.getColor("MenuItem.background"));
    // panelMenus.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    init();

  }

  /**
   * Inits the.
   * DOCUMENT_ME
   */
  private void init() {
    super.removeAll();
    scroll = new JScrollPane();
    scroll.setViewportView(panelMenus);
    scroll.setBorder(null);
    scroll.setMinimumSize(new Dimension(240, 40));

    scroll.setMaximumSize(new Dimension(scroll.getMaximumSize().width,

    this.getToolkit().getScreenSize().height
        - this.getToolkit().getScreenInsets(jframe.getGraphicsConfiguration()).top
        - this.getToolkit().getScreenInsets(jframe.getGraphicsConfiguration()).bottom - 4));
    super.add(scroll, BorderLayout.CENTER);
    // super.add(scroll);
  }

  /* (non-Javadoc)
   * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
   */
  @Override
  public void show(Component invoker, int x, int y) {
    init();
    // this.pack();
    panelMenus.validate();
    int maxsize = scroll.getMaximumSize().height;
    int realsize = panelMenus.getPreferredSize().height;

    int sizescroll = 0;

    if (maxsize < realsize) {
      sizescroll = scroll.getVerticalScrollBar().getPreferredSize().width;
    }
    scroll.setPreferredSize(new Dimension(scroll.getPreferredSize().width + sizescroll + 20,

    scroll.getPreferredSize().height));
    this.pack();
    this.setInvoker(invoker);
    if (sizescroll != 0) {
      // Set popup size only if scrollbar is visible
      this.setPopupSize(new Dimension(scroll.getPreferredSize().width + 20,

      scroll.getMaximumSize().height - 20));
    }
    // this.setMaximumSize(scroll.getMaximumSize());
    Point invokerOrigin = invoker.getLocationOnScreen();
    this.setLocation((int) invokerOrigin.getX() + x, (int) invokerOrigin.getY() + y);
    this.setVisible(true);
  }

  /**
   * Hidemenu.
   * DOCUMENT_ME
   */
  public void hidemenu() {
    if (this.isVisible()) {
      this.setVisible(false);
    }
  }

  /**
   * Adds the.
   * DOCUMENT_ME
   * 
   * @param menuItem DOCUMENT_ME
   */
  public void add(AbstractButton menuItem) {
    // menuItem.setMargin(new Insets(0, 20, 0 , 0));
    if (menuItem == null) {
      return;
    }
    panelMenus.add(menuItem);
    menuItem.removeActionListener(this);
    menuItem.addActionListener(this);
    if (menuItem.getIcon() == null) {
      menuItem.setIcon(EMPTY_IMAGE_ICON);
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.JPopupMenu#addSeparator()
   */
  @Override
  public void addSeparator() {
    panelMenus.add(new XSeperator());
  }

  /* (non-Javadoc)
   * @see java.awt.Container#removeAll()
   */
  @Override
  public void removeAll() {
    panelMenus.removeAll();
  }

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    this.hidemenu();
  }

  /* (non-Javadoc)
   * @see java.awt.Container#getComponents()
   */
  @Override
  public Component[] getComponents() {
    return panelMenus.getComponents();
  }

  /**
   * DOCUMENT_ME.
   */
  private static class XSeperator extends JSeparator {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = -6249719411021239596L;

    /**
     * Instantiates a new x seperator.
     */
    XSeperator() {
      ComponentUI ui = XBasicSeparatorUI.createUI(this);
      XSeperator.this.setUI(ui);
    }

    /**
     * DOCUMENT_ME.
     */
    private static class XBasicSeparatorUI extends BasicSeparatorUI {

      /**
       * Creates the ui.
       * DOCUMENT_ME
       * 
       * @param c DOCUMENT_ME
       * 
       * @return the component ui
       */
      public static ComponentUI createUI(JComponent c) {
        return new XBasicSeparatorUI();
      }

      /* (non-Javadoc)
       * @see javax.swing.plaf.basic.BasicSeparatorUI#paint(java.awt.Graphics, javax.swing.JComponent)
       */
      @Override
      public void paint(Graphics g, JComponent c) {
        Dimension s = c.getSize();

        if (((JSeparator) c).getOrientation() == SwingConstants.VERTICAL) {
          g.setColor(c.getForeground());
          g.drawLine(0, 0, 0, s.height);

          g.setColor(c.getBackground());
          g.drawLine(1, 0, 1, s.height);
        } else // HORIZONTAL
        {
          g.setColor(c.getForeground());
          g.drawLine(0, 7, s.width, 7);

          g.setColor(c.getBackground());
          g.drawLine(0, 8, s.width, 8);
        }
      }
    }
  }

}
