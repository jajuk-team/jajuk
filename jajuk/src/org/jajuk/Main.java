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
 * $Log$
 * Revision 1.1  2003/10/07 21:02:23  bflorat
 * Initial commit
 *
 */
package org.jajuk;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukJMenuBar;

import com.sun.corba.se.internal.iiop.LocalClientRequestImpl;

/**
 * Jajuk lauching class
 *
 * @author     bflorat
 * @created    3 oct. 2003
 */
public class Main {

	public static void main(String[] args) {
		//set look and feel
		try {
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e) { }
		//test
		//fixe la langue
		Messages.setLocal("en"); //$NON-NLS-1$
		JFrame jf = new JFrame("Jajuk : Just Another Jukebox"); //$NON-NLS-1$
		jf.setSize(1280,1024);
		Container container = jf.getContentPane(); //default layout for content panes are Border Layout
		CommandJPanel command = new CommandJPanel();
		//command.setPreferredSize(new Dimension(1021,20));
		container.add(command,BorderLayout.NORTH);
		container.add(Box.createVerticalStrut(1000),BorderLayout.CENTER);
		
		jf.setJMenuBar(JajukJMenuBar.getInstance());
		
		jf.show();
		
	}
}
