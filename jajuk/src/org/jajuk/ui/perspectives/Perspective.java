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
 * Revision 1.3  2003/10/24 12:41:53  sgringoi
 * Add the init() and createView() methods
 *
 * Revision 1.2  2003/10/10 15:29:57  sgringoi
 * *** empty log message ***
 *
 */
package org.jajuk.ui.perspectives;


import java.util.Vector;

import javax.swing.JPanel;

import org.jajuk.ui.views.IView;
import org.jajuk.util.error.JajukException;

/**
 * Representation of a perspective
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public abstract class Perspective extends JPanel {
	
	/** List of views showned in the perspective */
	private Vector views = null;
	/** Perspective's name */
	private String name = null;
	/** true if the perspective is initialized */
	private boolean initialized = false;

	/**
	 * Constructor of Perspective
	 */
	public Perspective() {
		super(false);
	}

	/**
	 * Initialize the perspective.
	 */
	protected void init() {
		if (!initialized) {
			initialized = true;

				// Views creation
			String[] classnamesList = PerspectivesConfiguration.getViewsNames(getName());
			views = new Vector(classnamesList.length);
			for (int i=0; i<classnamesList.length; i++)
			{
				IView newView = createView(classnamesList[i]);
				if (newView != null) {
					views.add(newView);
			
						// Add the view to the perspective's panel
					add(newView.getComponent());
				}
			}
		}
	}
	
	/**
	 * Add a view to the perspective.
	 * 
	 * @param pView View to add to the perspective.
	 * @return void 
	 */
	protected void addView(IView pView) {
		views.add(pView);
	}
	
	/**
	 * Remove a view from the perspective.
	 * 
	 * @param pView View to remove.
	 * @return void 
	 */
	public void removeView(IView pView) {
		views.remove(pView);
	}

	/*
	 * @see java.awt.Component#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		if (!initialized && b) {
			init();
		} else if (b) {
			PerspectiveManagerFactory.getPerspectiveManager().setCurrentPerspective(this);
		}

		super.setVisible(b);
	}

	/**
	 * Create the view.
	 * @param name Classname of the view to create.
	 * @return IView New perspective or null if the view can't be created.
	 */
	private IView createView(String name) {
		IView res = null;
		
		try {
			if (name != null) {
				res = (IView)Class.forName(PerspectivesConfiguration.getViewClassname(name)).newInstance();
			}
		} catch (Exception e) {
			JajukException je = new JajukException("jajuk0002", name, e);
			je.display();
			res = null;
		}
		
		return res;
	}

	/**
	 * @return the perspective's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the icon's name of the perspective. 
	 * @return String Icon's name representing the perspective.
	 */
	public abstract String getIconName();
	/**
	 * Set the perspective name.
	 * @param string Perspective's name
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @return Vector Views register in the perspective.
	 */
	public Vector getViews() {
		return new Vector(views);
	}

}