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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 * Menu bar used to choose the current perspective.
 * 
 * @author		bflorat
 * @version	1.0
 * @created		6 oct. 2003
 */
public class PerspectiveBarJPanel extends JPanel implements ITechnicalStrings{

		// Perspectives tool bar
	private JToolBar jtbPerspective = null;
		/**Self instance*/
		static private PerspectiveBarJPanel pb = null; 	
		/**Perspective button*/
		private ArrayList alButtons = new ArrayList(10); 
	
	
	/**
	 * Singleton access
	 * @return
	 */
	public static PerspectiveBarJPanel getInstance(){
		if (pb == null){
			pb = new PerspectiveBarJPanel();
		}
		return pb;
	}
	
	/**
	 * Constructor for PerspectiveBarJPanel.
	 */
	private PerspectiveBarJPanel() {
		super();
		update();
	}
	
	
	/**
	 * update contents
	 *
	 */
	public void update(){
		// set default layout and size
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); //we use a BoxLayout and not a FlowLayout to allow resizing
		setBorder(BorderFactory.createEtchedBorder());
			// Perspectives tool bar
		jtbPerspective = new JToolBar();
		jtbPerspective.setFloatable(false);
		jtbPerspective.addSeparator();
		jtbPerspective.setOrientation(JToolBar.VERTICAL);
		
		Iterator it = PerspectiveManager.getPerspectives().iterator();
		while ( it.hasNext()){
			final IPerspective perspective = (IPerspective)it.next();
			JButton jb = new JButton(Util.getIcon("jar:"+PATH_CURRENT_JAR+"!"+perspective.getIconPath())); //$NON-NLS-1$ //$NON-NLS-2$
			try{
				jb.setToolTipText(Messages.getString("PerspectiveBarJPanel."+perspective.getName())); //$NON-NLS-1$
			}
			catch(Exception e){};  //ignore tooltip missing
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PerspectiveManager.notify(perspective.getName());
				}
			});
			jtbPerspective.add(jb);
			jtbPerspective.addSeparator();
			alButtons.add(jb);
		}
		add(jtbPerspective);
	}
	
	/**
	 * Show selected perspective
	 * @param perspective
	 */
	public void setActivated(IPerspective perspective){
		ArrayList alPerspectives = PerspectiveManager.getPerspectives();
		Iterator it = alButtons.iterator();
		Iterator it2 = alPerspectives.iterator();
		while ( it.hasNext()){
			JButton jb = (JButton)it.next();
			IPerspective perspective2 = (IPerspective)it2.next();
			if ( perspective2.equals(perspective)){  //this perspective is selected 
				jb.setBorder(BorderFactory.createLineBorder(Color.BLACK,4));
			}
			else{
				jb.setBorder(BorderFactory.createEtchedBorder());
			}
		}
	}
}
