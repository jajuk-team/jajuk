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

package org.jajuk.ui.widgets;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;

/**
 * Label that scrolls text horizontally
 */
public class ScrollingLabel extends JLabel {
    private static final long serialVersionUID = 1L;
    private int posHoriz = 250;

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public ScrollingLabel(String text) {
        super(text);
    }

    public void startScrolling() {
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                repaint();
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }


    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(Graphics g) {
        FontMetrics fm = g.getFontMetrics();
        posHoriz = (posHoriz - 10) % getWidth();
        int textSize = fm.stringWidth(super.getText());
        if (posHoriz <= -1 * textSize) {
            posHoriz = getWidth();
        }
        //Draw the text on the left
        g.drawString(getText(), posHoriz, fm.getHeight());
        // Draw the text on the right
        g.drawString(getText(), posHoriz + textSize, fm.getHeight());
    }
}
