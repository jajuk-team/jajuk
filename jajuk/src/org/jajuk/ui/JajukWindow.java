/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Release$
 */

package org.jajuk.ui;

import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 *  Jajuk main window
 *
 * @author     bflorat
 * @created    23 mars 2004
 */
public class JajukWindow extends JFrame implements ITechnicalStrings,ComponentListener {
	
	/**Initial width at startup*/
	private int iWidth ; 
	/**Initial height at startup*/
	private int iHeight;
	
	/**Constructor*/
	public JajukWindow(){
		iWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		iHeight = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		setTitle(Messages.getString("Main.10"));  //$NON-NLS-1$
		setIconImage(Util.getIcon(ICON_LOGO).getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addComponentListener(this);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				Main.exit(0);
				return; 
			}
		});
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
	
		int width = getWidth();
		int height = getHeight();
		
		
		/*Goal here is to fix a bug : when starting, restaure the window ( middle button near close ) set a strange size ( very large * very small ). So if size is too small or too large in front of
		 * screen size, we set 100% of screen
		 */
		
		boolean resize = false;
				
		if (width > 1.1*iWidth) { 
			resize = true;
			width = iWidth;
		}
		if (height > 1.1*iHeight) { 
			resize = true;
			height = iHeight;
		}
		if (resize) {
			setSize(width, height);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
	}
	
}
