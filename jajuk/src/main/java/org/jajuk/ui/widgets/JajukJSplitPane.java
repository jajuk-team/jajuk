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
package org.jajuk.ui.widgets;

import java.awt.Graphics;

import javax.swing.JSplitPane;

/**
 * Splitpane fixing the setDeviderLocation bug Thanks
 * http://www.jguru.com/faq/view.jsp?EID=27191
 */
public class JajukJSplitPane extends JSplitPane {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -7358047597849102849L;

  /** DOCUMENT_ME. */
  protected boolean isPainted = false;

  /** DOCUMENT_ME. */
  protected boolean hasProportionalLocation = false;

  /** DOCUMENT_ME. */
  protected double proportionalLocation = -1;

  /* (non-Javadoc)
   * @see javax.swing.JSplitPane#setDividerLocation(double)
   */
  @Override
  public void setDividerLocation(double proportionalLocation) {
    if (!isPainted) {
      hasProportionalLocation = true;
      this.proportionalLocation = proportionalLocation;
    } else {
      super.setDividerLocation(proportionalLocation);
    }
  }

  /* (non-Javadoc)
   * @see javax.swing.JComponent#paint(java.awt.Graphics)
   */
  @Override
  public void paint(Graphics g) {
    if (!isPainted) {
      if (hasProportionalLocation) {
        super.setDividerLocation(proportionalLocation);
      }
      isPainted = true;
    }
    super.paint(g);
  }

  /**
   * Instantiates a new jajuk j split pane.
   * 
   * @param orientation DOCUMENT_ME
   */
  public JajukJSplitPane(int orientation) {
    super(orientation);
  }
}
