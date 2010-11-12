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
package ext;

import java.awt.AWTError;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.jdesktop.swingx.JXPanel;

/**
 * DOCUMENT_ME.
 */
public class FlowScrollPanel extends JXPanel {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private FlowLayout layout = new FlowLayout();

  /** DOCUMENT_ME. */
  private JScrollPane scroller;

  /**
   * Instantiates a new flow scroll panel.
   */
  public FlowScrollPanel() {
    this(null);
  }

  /**
   * Instantiates a new flow scroll panel.
   * 
   * @param scrollPane DOCUMENT_ME
   */
  public FlowScrollPanel(JScrollPane scrollPane) {
    super();
    super.setLayout(layout);
    setScroller(scrollPane);
  }

  /**
   * Sets the scroller.
   * 
   * @param scrollPane the new scroller
   */
  public final void setScroller(JScrollPane scrollPane) {
    // FIXME: do we really want to compare instances here instead of content??
    if (scroller != scrollPane) {
      scroller = scrollPane;
      if (scroller != null) {
        scroller.getViewport().setView(this);
        scroller.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            scroller.getViewport().setViewSize(getSize());
            scroller.invalidate();
            scroller.validate();
            doLayout();
          }
        });
      }
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    if (scroller == null) {
      return super.getPreferredSize();
    }
    Insets insets = getInsets();
    int hgap = layout.getHgap();
    int vgap = layout.getVgap();
    JScrollBar vsb = scroller.getVerticalScrollBar();
    if (vsb == null) {
      vsb = scroller.createVerticalScrollBar();
    }
    int scrollerWidth = scroller.getSize().width - (insets.left + insets.right + hgap * 2)
        - vsb.getSize().width /*-2*/;
    // the -2 is a voodoo constant. I don't know why it's needed, but
    // it is. (I suspect that this routine and FlowLayout compute
    // required sizes in a subtly different way.)
    // No longer needed with Swing 1.1 (I think).
    int nmembers = getComponentCount();
    int x = 0, y = insets.top + vgap;
    int rowh = 0;
    int maxRowWidth = scrollerWidth;

    for (int i = 0; i < nmembers; i++) {
      Component m = getComponent(i);
      if (m.isVisible()) {
        Dimension d = m.getPreferredSize();
        if ((x == 0) || ((x + d.width) <= scrollerWidth)) {
          if (x > 0) {
            x += hgap;
          }
          x += d.width;
          rowh = Math.max(rowh, d.height);
        } else {
          if (x > maxRowWidth) {
            maxRowWidth = x + hgap;
          }
          x = d.width;
          y += vgap + rowh;
          rowh = d.height;
        }
      }
    }
    if (x > maxRowWidth) {
      maxRowWidth = x + 2 * hgap + insets.left + insets.right;
    }
    y += vgap + rowh + insets.bottom;
    return new Dimension(maxRowWidth, y);
  }

  /* (non-Javadoc)
   * @see java.awt.Container#setLayout(java.awt.LayoutManager)
   */
  @Override
  public void setLayout(LayoutManager l) {
    if (l instanceof FlowLayout) {
      layout = (FlowLayout) l;
      super.setLayout(l);
    } else {
      throw new AWTError("FlowScrollPane can have only FlowLayout, not " + l);
    }
  }

  //
  // Scrollable methods
  //

  /* (non-Javadoc)
   * @see org.jdesktop.swingx.JXPanel#getPreferredScrollableViewportSize()
   */
  @Override
  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  /**
   * Returns height of a row.
   * 
   * @param visibleRect DOCUMENT_ME
   * @param orientation DOCUMENT_ME
   * @param direction DOCUMENT_ME
   * 
   * @return the scrollable unit increment
   */
  @Override
  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    Dimension prefSize = layout.preferredLayoutSize(this);
    return prefSize.height / 20;
  }

  /**
   * returns the height of the visible rect (so it scrolls by one screenfull).
   * 
   * @param visibleRect DOCUMENT_ME
   * @param orientation DOCUMENT_ME
   * @param direction DOCUMENT_ME
   * 
   * @return the scrollable block increment
   */
  @Override
  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    return visibleRect.height;
  }

  /* (non-Javadoc)
   * @see org.jdesktop.swingx.JXPanel#getScrollableTracksViewportWidth()
   */
  @Override
  public boolean getScrollableTracksViewportWidth() {
    return true;
  }

  /* (non-Javadoc)
   * @see org.jdesktop.swingx.JXPanel#getScrollableTracksViewportHeight()
   */
  @Override
  public boolean getScrollableTracksViewportHeight() {
    return false;
  }

}
