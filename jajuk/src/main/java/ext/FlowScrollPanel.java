/*
 * Code found at: http://groups.google.fr/group/comp.lang.java.gui/browse_thread/thread/11403002a25da9a3/da4cea55cf283a52?q=%2BJscrollPane++%2Bflowlayout&rnum=2&hl=fr#da4cea55cf283a52
 * Chris Long <*> all...@cs.berkeley.edu <*> http://www.cs.berkeley.edu/~allanl 
 *
 * This file has been adapted to Jajuk by the Jajuk Team.
 * 
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
 * .
 */
public class FlowScrollPanel extends JXPanel {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  private FlowLayout layout = new FlowLayout();
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
   * @param scrollPane 
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
   * @param visibleRect 
   * @param orientation 
   * @param direction 
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
   * @param visibleRect 
   * @param orientation 
   * @param direction 
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
