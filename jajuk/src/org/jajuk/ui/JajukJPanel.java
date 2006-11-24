/*
 *  Jajuk
 *  Copyright (C) 2006 bflorat
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

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.gradient.BasicGradientPainter;

/**
 *  a customized JXPanel that supports gradiant
 *
 * @author     Bertrand Florat
 * @created    23 nov. 06
 */
public class JajukJPanel extends JXPanel {

	/**
	 * 
	 */
	public JajukJPanel() {
		setOpaque(false);
		setBackgroundPainter(new BasicGradientPainter(
			BasicGradientPainter.GRAY));
	}

	private static final long serialVersionUID = 1L;

}
