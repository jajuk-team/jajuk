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
package org.jajuk.ui.views;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * Pysical tree view.
 * 
 * @author sgringoi
 * @version 1.0
 * @created 7 oct. 03
 */
public class PhysicalTreeView extends JPanel implements IView {

	/**
	 * Constructor for PhysicalTreeView.
	 */
	public PhysicalTreeView() {
		super();
	}

	/**
	 * @see org.jajuk.ui.views.IView#getIdView()
	 */
	public String getIdView() {
		return VIEW_PHYSICAL_TREE;
	}

}
