/*
 *  Jajuk
 *  Copyright (C) 2003 sgringoi
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
 * Revision 1.1  2003/10/24 14:56:29  sgringoi
 * Initial commit
 *
 *
 */

package org.jajuk.ui.perspectives;

import java.awt.Container;
import java.awt.GridLayout;
import java.util.Hashtable;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Perspectives manager.
 *
 * @author     sgringoi
 * @created    14 oct. 2003
 */
public class PerspectiveManagerJTabbedPane
	extends JPanel
	implements IPerspectiveManager {

	/** List of perspectives */
	private Hashtable perspectives = null;
	/** true if the perspective manager is initialized */
	private boolean initialized = false;
	
	private JTabbedPane tabbedPane = null;
	
	/**
	 * Constructor
	 */
	public PerspectiveManagerJTabbedPane()
	{
	}
	
	/**
	 * Initialize the perspectives manager.
	 */
	public void init() {
		if (!initialized) {
			initialized = true;

			tabbedPane = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
			
				// Perspectives creation
			String[] classnamesList = PerspectivesConfiguration.getPerspectivesNames();
			perspectives = new Hashtable(classnamesList.length);

			for (int i=0; i<classnamesList.length; i++)
			{
				Perspective newPerspective = createPerspective(classnamesList[i]);
				if (newPerspective != null) {
					perspectives.put(newPerspective.getName(), newPerspective);

						// Icon creation
					ImageIcon icon = new ImageIcon(newPerspective.getIconName());
					
						// Add the view to the perspective's panel
					tabbedPane.addTab("", icon, newPerspective);
				}
			}
			/*
			Component panel1 = makeTextPanel("Blah");
			tabbedPane.addTab("One", panel1);
			tabbedPane.setSelectedIndex(0);
	
			Component panel2 = makeTextPanel("Blah blah");
			tabbedPane.addTab("Two", panel2);
	
			Component panel3 = makeTextPanel("Blah blah blah");
			tabbedPane.addTab("Three", panel3);
	
			Component panel4 = makeTextPanel("Blah blah blah blah");
			tabbedPane.addTab("Four", panel4);
			*/
			setLayout(new GridLayout(1, 1)); 
			add(tabbedPane);
		}
	}
	
	/**
	 * Create the perspective.
	 * @param name Classname of the perspective to create.
	 * @return Perspective New perspective or null if the perspective can't be created.
	 */
	private Perspective createPerspective(String name) {
		Perspective res = null;
Log.debug("createPerspective(" + name);
		try {
			if ((name != null) && (!name.equals(""))) {
				res = (Perspective)Class.forName(PerspectivesConfiguration.getPerspectiveClassname(name)).newInstance();
				res.setName(name);
			} else {
				Log.info("No perspective was created.");
			}
		} catch (Exception e) {
			JajukException je = new JajukException("jajuk0003", name, e);
			je.display();
			res = null;
		}
		
		return res;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getPerspectives()
	 */
	public Perspective[] getPerspectives() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getCurrentPerspective()
	 */
	public Perspective getCurrentPerspective() throws JajukException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setMainWindow(java.awt.Container)
	 */
	public void setMainWindow(Container pContainer) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setCurrentPerspective(org.jajuk.ui.perspectives.Perspective)
	 */
	public void setCurrentPerspective(Perspective pCurPersp) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getPerspective(java.lang.String)
	 */
	public Perspective getPerspective(String pName) {
		if (pName != null) {
			return (Perspective)perspectives.get(pName);
		} else {
			return null;
		}
		
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setParentContainer(java.awt.Container)
	 */
	public void setParentContainer(Container pContainer) {
		// TODO Auto-generated method stub
	}

}
