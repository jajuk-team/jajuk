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
 * Revision 1.4  2003/11/18 18:58:07  bflorat
 * 18/11/2003
 *
 * Revision 1.3  2003/10/23 22:07:41  bflorat
 * 23/10/2003
 *
 * Revision 1.2  2003/10/17 20:43:56  bflorat
 * 17/10/2003
 *
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
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

	JajukFileChooser() {
		setDialogTitle(Messages.getString("JajukFileChooser.Please_choose_track(s)_to_play_1")); //$NON-NLS-1$
		setFileFilter( JajukFileFilter.getInstance());
		setMultiSelectionEnabled(true);

	}

}
