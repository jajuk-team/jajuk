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

import javax.swing.JFileChooser;

import org.jajuk.i18n.Messages;
import org.jajuk.util.JajukFileFilter;

/**
 *  Music-oriented file chooser 
 * <p>decorator
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class JajukFileChooser extends JFileChooser {

	javax.swing.filechooser.FileFilter filter;

	/**
	 * Constructor with specified file filter
	 * @param jfilter filter to use
	 */
	public JajukFileChooser(JajukFileFilter jfilter) {
		setDialogTitle(Messages.getString("JajukFileChooser.0"));//default title  //$NON-NLS-1$
		this.filter = jfilter;
		setFileFilter(jfilter);
		setMultiSelectionEnabled(true);
	}

	/**
	 * Default constructor
	 *
	 */
	public JajukFileChooser() {
		this(new JajukFileFilter());
	}
	
}
