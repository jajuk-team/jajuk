/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui;

import org.jdesktop.swingx.painter.MattePainter;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JComponent;

import com.vlsolutions.swing.toolbars.BackgroundPainter;

/**
 * Custom background painter
 */
public class JajukBackgroundPainter extends MattePainter implements BackgroundPainter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.vlsolutions.swing.toolbars.BackgroundPainter#paintBackground(javax.swing.JComponent,
	 *      java.awt.Graphics)
	 */
	public void paintBackground(JComponent component, Graphics g) {
		super.paint((Graphics2D) g, component, component.getWidth(), component.getHeight());
	}

	public JajukBackgroundPainter() {
		super(new GradientPaint(new Point(0,0),new Color(226, 226, 226),
                new Point(0,20),  new Color(250, 248, 248)));
	}

}
