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
 *  $Revision$
 */

package org.jajuk.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 *  Jajuk Splashscreen
 *
 * @author     bflorat
 * @created    20 nov. 2003
 */
public class SplashScreen extends JDialog implements ITechnicalStrings
{
	public SplashScreen(Frame f)
	{
		setUndecorated(true);
		JPanel jpContent = (JPanel)getContentPane();
		jpContent.setLayout(new BoxLayout(jpContent,BoxLayout.Y_AXIS));
		JLabel l = new JLabel(Util.getIcon(IMAGES_SPLASHSCREEN));
		setTitle(Messages.getString("JajukWindow.17"));  //$NON-NLS-1$
		jpContent.add(l);
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = l.getPreferredSize();
		setLocation(screenSize.width/2 - (labelSize.width/2),screenSize.height/2 - (labelSize.height/2));
		setVisible(true);
	}
}