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
 * Revision 1.1  2003/10/07 21:02:18  bflorat
 * Initial commit
 *
 */
package org.jajuk.ui.perspectives;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.jajuk.ui.views.IView;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;

/**
 * Physical perspective representation.
 * 
 * @author sgringoi
 * @version 1.0
 * @created 6 oct. 03
 */
public class PhysicalPerspective extends Perspective {
		// List of views
	HashMap viewsList = null;
	
	/**
	 * Constructor for PhysicalPerspective.
	 */
	public PhysicalPerspective() {
		super();
		
			// List of views
		StringTokenizer strTok = new StringTokenizer(ConfigurationManager.getProperty("jajuk.preference.perspective.physical.views"), ",");
		while(strTok.hasMoreTokens())
		{
			IView view;
			String viewName = strTok.nextToken();
			
			try {
				view = (IView) Class.forName(viewName).newInstance();
				viewsList.put(view.getIdView(), view);
				
				view.setVisible(true);
			} catch (Exception e) {
				JajukException je = new JajukException("jajuk0002", viewName, e);
				je.printStackTrace();
				je.display();
			}
		}
	}
}
