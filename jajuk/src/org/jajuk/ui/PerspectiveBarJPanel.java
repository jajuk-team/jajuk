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
 * $Revision$
 *
 */
package org.jajuk.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;

/**
 * Menu bar used to choose the current perspective.
 * 
 * @author		bflorat
 * @version	1.0
 * @created		6 oct. 2003
 */
public class PerspectiveBarJPanel
	extends JPanel
	implements ITechnicalStrings,ActionListener
{

		// Perspectives tool bar
	private JToolBar jtbPerspective = null;
			// Perspectives access buttons
		private JButton jbPhysical		= null;
		private JButton jbLogical		= null;
		private JButton jbConfiguration= null;
		private JButton jbHelp			= null;
		private JButton jbStatistics	= null;
		
		/**Self instance*/
		static private PerspectiveBarJPanel pb = null; 	
	
	
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
		
			// set default layout and size
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); //we use a BoxLayout and not a FlowLayout to allow resizing
		setBorder(BorderFactory.createEtchedBorder());
			// Perspectives tool bar
		jtbPerspective = new JToolBar();
		jtbPerspective.setFloatable(false);
		jtbPerspective.setOrientation(JToolBar.VERTICAL);
		
			// Physical perspective access button
		jbPhysical = new JButton(new ImageIcon(ICON_PERSPECTIVE_PHYSICAL)); 
		jbPhysical.addActionListener(this);
		jbPhysical.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_physical_perspective")); //$NON-NLS-1$
		jtbPerspective.add(jbPhysical);
		// Logical perspective access button
		jbLogical = new JButton(new ImageIcon(ICON_PERSPECTIVE_LOGICAL)); 
		jbLogical.addActionListener(this);
		jbLogical.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_logical_perspective")); //$NON-NLS-1$
		jtbPerspective.addSeparator();
		jtbPerspective.add(jbLogical);
			// Configuration perspective access button
		jbConfiguration = new JButton(new ImageIcon(ICON_PERSPECTIVE_CONFIGURATION)); 
		jbConfiguration.addActionListener(this);
		jbConfiguration.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_configuration_perspective")); //$NON-NLS-1$
		jtbPerspective.addSeparator();
		jtbPerspective.add(jbConfiguration);
			// Statistics perspective access button
		jbStatistics = new JButton(new ImageIcon(ICON_PERSPECTIVE_STATISTICS)); 
		jbStatistics.addActionListener(this);
		jbStatistics.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_statistics_perspective")); //$NON-NLS-1$
		jtbPerspective.addSeparator();
		jtbPerspective.add(jbStatistics);
			// Help perspective access button
		jbHelp = new JButton(new ImageIcon(ICON_INFO)); 
		jbHelp.addActionListener(this);
		jbHelp.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_help_perspective")); //$NON-NLS-1$
		jtbPerspective.addSeparator();
		jtbPerspective.add(jbHelp);
		
		add(jtbPerspective);
	}
	
	
	/**
	 * Show selected perspective
	 * @param perspective
	 */
	public void setActivated(IPerspective perspective){
		//remove all borders
		jbPhysical.setBorder(BorderFactory.createEtchedBorder());
		jbLogical.setBorder(BorderFactory.createEtchedBorder());
		jbConfiguration.setBorder(BorderFactory.createEtchedBorder());
		jbStatistics.setBorder(BorderFactory.createEtchedBorder());
		jbHelp.setBorder(BorderFactory.createEtchedBorder());
		if (perspective.getName().equals(PERSPECTIVE_NAME_PHYSICAL)){
			jbPhysical.setBorder(BorderFactory.createLineBorder(Color.BLACK,4));
		}
		else if (perspective.getName().equals(PERSPECTIVE_NAME_LOGICAL)){
			jbLogical.setBorder(BorderFactory.createLineBorder(Color.BLACK,4));
		}
		else if (perspective.getName().equals(PERSPECTIVE_NAME_CONFIGURATION)){
			jbConfiguration.setBorder(BorderFactory.createLineBorder(Color.BLACK,4));
		}
		else if (perspective.getName().equals(PERSPECTIVE_NAME_STATISTICS)){
			jbStatistics.setBorder(BorderFactory.createLineBorder(Color.BLACK,4));
		}
		else if (perspective.getName().equals(PERSPECTIVE_NAME_HELP)){
			jbHelp.setBorder(BorderFactory.createLineBorder(Color.BLACK,4));
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jbPhysical){
			PerspectiveManager.notify(PERSPECTIVE_NAME_PHYSICAL);
		}
		else if (e.getSource() == jbLogical){
			PerspectiveManager.notify(PERSPECTIVE_NAME_LOGICAL);
		}
		if (e.getSource() == jbConfiguration){
			PerspectiveManager.notify(PERSPECTIVE_NAME_CONFIGURATION);
		}
		if (e.getSource() == jbStatistics){
			PerspectiveManager.notify(PERSPECTIVE_NAME_STATISTICS);
		}
		if (e.getSource() == jbHelp){
			PerspectiveManager.notify(PERSPECTIVE_NAME_HELP);
		}
		
	}
	
	

}
