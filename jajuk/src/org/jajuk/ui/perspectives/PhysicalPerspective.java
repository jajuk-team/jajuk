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
 * Revision 1.5  2003/10/24 14:57:07  sgringoi
 * add the getIconName() method
 *
 * Revision 1.4  2003/10/21 20:43:06  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 * Revision 1.3  2003/10/17 20:43:56  bflorat
 * 17/10/2003
 *
 * Revision 1.2  2003/10/10 15:29:57  sgringoi
 * *** empty log message ***
 *
 */
package org.jajuk.ui.perspectives;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.jajuk.base.ITechnicalStrings;
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
public class PhysicalPerspective extends Perspective implements ITechnicalStrings {
		// List of views
	private HashMap viewsList = null;
	
	/**
	 * Constructor for PhysicalPerspective.
	 */
	public PhysicalPerspective() {
		super();
		
		viewsList = new HashMap();
		
			// List of views
		StringTokenizer strTok = new StringTokenizer(ConfigurationManager.getProperty(CONF_VIEW_PHYSICAL), ","); //$NON-NLS-1$
		while(strTok.hasMoreTokens())
		{
			IView view;
			String viewName = strTok.nextToken();
			
			try {
				view = (IView) Class.forName(viewName).newInstance();
				viewsList.put(view.getId(), view);
				
				view.setVisible(true);
			} catch (Exception e) {
				JajukException je = new JajukException("002", viewName, e); //$NON-NLS-1$
				je.printStackTrace();
				je.display();
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.Perspective#getIconName()
	 */
	public String getIconName() {
		return PerspectivesConfiguration.getPerspectiveIconName(getName());
	}

}
