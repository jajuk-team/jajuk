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

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.jajuk.ui.Observer;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Allows  to navigate in collection with buttons 
 * <p>Logical perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
public class LogicalNavigationBarView extends ViewAdapter implements Observer{

	/**Self instance*/
	private static LogicalNavigationBarView lnb;
	
	/**Return self instance*/
	public static LogicalNavigationBarView getInstance(){
		if (lnb == null){
			lnb = new LogicalNavigationBarView();
		}
		return lnb;
	}
	
	/**
	 * Constructor
	 */
	public LogicalNavigationBarView() {
		lnb = this;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Navigation view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.LogicalNavigationBarView";
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
	}

}
