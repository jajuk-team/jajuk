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
 * Revision 1.1  2003/10/07 21:02:22  bflorat
 * Initial commit
 *
 */
package org.jajuk.ui;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.jajuk.i18n.Messages;
;

/**
 *  Jajuk menu bar
 * <p>Singleton
 *
 * @author     bflorat
 * @created    4 oct. 2003
 */
public class JajukJMenuBar extends JMenuBar {

	static private JajukJMenuBar jjmb;
	
	private JajukJMenuBar(){
		setAlignmentX(0.0f);
		add(new JMenu(Messages.getString("JajukJMenuBar.File_1"))); //$NON-NLS-1$
		add(new JMenu(Messages.getString("JajukJMenuBar.Views_2"))); //$NON-NLS-1$
		add(new JMenu(Messages.getString("JajukJMenuBar.Properties_3"))); //$NON-NLS-1$
		add(new JMenu(Messages.getString("JajukJMenuBar.Mode_4"))); //$NON-NLS-1$
		add(new JMenu(Messages.getString("JajukJMenuBar.Help_5"))); //$NON-NLS-1$
	}
	
	static public JajukJMenuBar getInstance(){
		if (jjmb == null){
			jjmb = new JajukJMenuBar();
		}
		return jjmb;
	}
}
