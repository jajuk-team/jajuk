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

package org.jajuk.ui.views;

import javax.swing.JPanel;

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
		setOpaque(true);
	}

	/**
	 * toString method
	 */
	public String toString(){
		return "View[name="+getViewName()+" description='"+getDesc()+"]";
	}


}
