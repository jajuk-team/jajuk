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
 * Revision 1.3  2003/11/18 21:50:56  bflorat
 * 18/11/2003
 *
 * Revision 1.2  2003/11/18 18:58:06  bflorat
 * 18/11/2003
 *
 * Revision 1.1  2003/11/16 17:57:18  bflorat
 * 16/11/2003
 *
 */

package org.jajuk.ui.views;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.ui.IView;

/**
 *  Default implementation for views
 *
 * @author     bflorat
 * @created    15 nov. 2003
 */
public abstract class ViewAdapter extends JPanel implements IView,ITechnicalStrings {

	/**
	 * 
	 */
	public ViewAdapter() {
		super();
	}

	/**
	 * toString method
	 */
	public String toString(){
		return "View[name="+getName()+" description='"+getDesc()+"]";
	}


}
