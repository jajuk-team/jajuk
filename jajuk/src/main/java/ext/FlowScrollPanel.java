/*
 * Code found at: http://groups.google.fr/group/comp.lang.java.gui/browse_thread/thread/11403002a25da9a3/da4cea55cf283a52?q=%2BJscrollPane++%2Bflowlayout&rnum=2&hl=fr#da4cea55cf283a52
 * Chris Long <*> all...@cs.berkeley.edu <*> http://www.cs.berkeley.edu/~allanl 
 */

package ext;

import org.jdesktop.swingx.JXPanel;

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
import javax.swing.Scrollable;

public class FlowScrollPanel extends JXPanel implements Scrollable {
	private static final long serialVersionUID = 1L;

	private FlowLayout layout = new FlowLayout();

	private JScrollPane scroller;

	public FlowScrollPanel() {
		this(null);
	}

	public FlowScrollPanel(JScrollPane scrollPane) {
		super();
		super.setLayout(layout);
		setScroller(scrollPane);
 	}

	public void setScroller(JScrollPane scrollPane) {
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
		Insets insets = getInsets();
		int hgap = layout.getHgap();
		int vgap = layout.getVgap();
		JScrollBar vsb = scroller.getVerticalScrollBar();
		if (vsb == null) {
			vsb = scroller.createVerticalScrollBar();
		}
		int scrollerWidth = scroller.getSize().width
				- (insets.left + insets.right + hgap * 2) - vsb.getSize().width /*-2*/;
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

	public void setLayout(LayoutManager l) {
		if (l instanceof FlowLayout) {
			layout = (FlowLayout) l;
			super.setLayout(l);
		} else
			throw new AWTError(
					"FlowScrollPane can have only FlowLayout, not " + l); 
	}

	//
	// Scrollable methods
	//

	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	/**
	 * Returns height of a row
	 */
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		Dimension prefSize = layout.preferredLayoutSize(this);
		return prefSize.height;
	}

	/**
	 * returns the height of the visible rect (so it scrolls by one screenfull).
	 */
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return visibleRect.height;
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

}
