// See end of file for software license.

package ext;

import org.jdesktop.swingx.JXPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * <P>
 * This software is distributed under the
 * <A HREF="http://guir.cs.berkeley.edu/projects/COPYRIGHT.txt">
 * Berkeley Software License</A>.
 *
 * Modified by jajuk team

 */
public class FlowScrollPanel extends JXPanel implements Scrollable {
  private FlowLayout layout = new FlowLayout();
  private JScrollPane scroller;

  public FlowScrollPanel()
  {
    this(null);
  }

  public FlowScrollPanel(JScrollPane scrollPane)
  {
    super();
    super.setLayout(layout);
    setScroller(scrollPane);
  }

  public void setScroller(JScrollPane scrollPane)
  {
    if (scroller != scrollPane) {
      scroller = scrollPane;
      if (scroller != null) {
        scroller.getViewport().setView(this);
      }
      scroller.addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
          scroller.getViewport().setViewSize(getSize());
          scroller.invalidate();
          scroller.validate();
          doLayout();
        }
      });
    }
  }

  public Dimension getPreferredSize() {
    if (scroller == null) {
      Dimension result = super.getPreferredSize();
      return result;
    }
    else {
      Insets insets = getInsets();
      int hgap = layout.getHgap();
      int vgap = layout.getVgap();
      JScrollBar vsb = scroller.getVerticalScrollBar();
      if (vsb == null) {
        vsb = scroller.createVerticalScrollBar();
      }
      int scrollerWidth = scroller.getSize().width -
              (insets.left + insets.right + hgap*2) - vsb.getSize().width /*-2*/;
      // the -2 is a voodoo constant.  I don't know why it's needed, but
      // it is.  (I suspect that this routine and FlowLayout compute
      // required sizes in a subtly different way.)
      // No longer needed with Swing 1.1 (I think).
      int nmembers = getComponentCount();
      int x = 0, y = insets.top + vgap;
      int rowh = 0, start = 0;
      int maxRowWidth = scrollerWidth;

      for (int i = 0 ; i < nmembers ; i++) {
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
            if (x > maxRowWidth)
              maxRowWidth = x + hgap;
            x = d.width;
            y += vgap + rowh;
            rowh = d.height;
          }
        }
      }
      if (x > maxRowWidth)
        maxRowWidth = x + 2 * hgap + insets.left + insets.right;
      y += vgap + rowh + insets.bottom;
      return new Dimension(maxRowWidth, y);
    }
  }

  public void setLayout(LayoutManager l)
  {
    if (l instanceof FlowLayout) {
      layout = (FlowLayout) l;
      super.setLayout(l);
    }
    else
      throw new
              AWTError("FlowScrollPane can have only FlowLayout, not " + l);
  }

  //
  // Scrollable methods
  //

  public Dimension getPreferredScrollableViewportSize()
  {
    return getPreferredSize();
  }

  /**
   * Returns height of a row
   */
  public int getScrollableUnitIncrement(Rectangle visibleRect,
                                        int orientation,
                                        int direction)
  {
    Dimension prefSize = layout.preferredLayoutSize(this);
    return prefSize.height;
  }

  /**
   * returns the height of the visible rect (so it scrolls by one
   * screenfull).
   */
  public int getScrollableBlockIncrement(Rectangle visibleRect,
                                         int orientation,
                                         int direction)
  {
    return visibleRect.height;
  }

  public boolean getScrollableTracksViewportWidth()
  {
    return true;
  }

  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }


}

/*
Copyright (c) 2001 Regents of the University of California.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

3. All advertising materials mentioning features or use of this software
   must display the following acknowledgement:

      This product includes software developed by the Group for User
      Interface Research at the University of California at Berkeley.

4. The name of the University may not be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.
*/