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
 * Revision 1.5  2003/10/21 20:43:06  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 * Revision 1.4  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 * Revision 1.3  2003/10/10 22:33:12  bflorat
 * added border and separators
 *
 * Revision 1.2  2003/10/10 15:29:57  sgringoi
 * *** empty log message ***
 *
 */
package org.jajuk.ui;

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
 * @author		sgringoi
 * @version	1.0
 * @created		6 oct. 2003
 */
public class PerspectiveBarJPanel
	extends JPanel
	implements ITechnicalStrings
{

		// Perspectives tool bar
	private JToolBar jtbPerspective = null;
			// Perspectives access buttons
		private JButton jbPhysical		= null;
		private JButton jbLogical		= null;
		private JButton jbConfiguration= null;
		private JButton jbHelp			= null;
		private JButton jbStatistics	= null;
	
	/**
	 * Constructor for PerspectiveBarJPanel.
	 */
	public PerspectiveBarJPanel() {
		super();
		
			// set default layout and size
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS)); //we use a BoxLayout and not a FlowLayout to allow resizing
		setBorder(BorderFactory.createEtchedBorder());
			// Perspectives tool bar
		jtbPerspective = new JToolBar();
		jtbPerspective.setOrientation(JToolBar.VERTICAL);
		
			// Physical perspective access button
		jbPhysical = new JButton(new ImageIcon(ICON_PERSPECTIVE_PHYSICAL)); 
		jbPhysical.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_physical_perspective")); //$NON-NLS-1$
		jtbPerspective.add(jbPhysical);
			// Logical perspective access button
		jbLogical = new JButton(new ImageIcon(ICON_PERSPECTIVE_LOGICAL)); 
		jbLogical.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_logical_perspective")); //$NON-NLS-1$
		jtbPerspective.addSeparator();
		jtbPerspective.add(jbLogical);
			// Configuration perspective access button
		jbConfiguration = new JButton(new ImageIcon(ICON_PERSPECTIVE_CONFIGURATION)); 
		jbConfiguration.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_configuration_perspective")); //$NON-NLS-1$
		jtbPerspective.addSeparator();
		jtbPerspective.add(jbConfiguration);
			// Statistics perspective access button
		jbStatistics = new JButton(new ImageIcon(ICON_PERSPECTIVE_STATISTICS)); 
		jbStatistics.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_statistics_perspective")); //$NON-NLS-1$
		jtbPerspective.addSeparator();
		jtbPerspective.add(jbStatistics);
			// Help perspective access button
		jbHelp = new JButton(new ImageIcon(ICON_INFO)); 
		jbHelp.setToolTipText(Messages.getString("PerspectiveBarJPanel.Show_the_help_perspective")); //$NON-NLS-1$
		jtbPerspective.addSeparator();
		jtbPerspective.add(jbHelp);
		
		add(jtbPerspective);
	}

}
