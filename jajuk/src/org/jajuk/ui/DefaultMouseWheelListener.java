/*
 *  Jajuk
 *  Copyright (C) 2004 bflorat
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

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JSlider;

/**
 * A basic Mouse wheel listener for jsliders
 * 
 * @author Bertrand Florat
 * @created 22 may 2006
 */
public class DefaultMouseWheelListener implements MouseWheelListener {

	JSlider js;

	/**
	 * 
	 * @param js
	 *            associated jslider
	 */
	public DefaultMouseWheelListener(JSlider js) {
		super();
		this.js = js;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent mwe) {
		int iOld = js.getValue();
		int iNew = iOld - mwe.getWheelRotation();
		if (js.isEnabled()) {
			js.setValue(iNew);
		}
	}

}
