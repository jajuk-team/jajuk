/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

/**
 *  Jajuk Splashscreen
 *
 * @author     Bertrand Florat
 * @created    20 nov. 2003
 */
public class SplashScreen extends JDialog implements ITechnicalStrings{
	
    private JPanel jpContent;
    private JLabel jlImage;
    private JPanel jpRelease; 
    private JLabel jlRelease;
    
    /**
     * Constructor
     * @param f
     */
    public SplashScreen(Frame f){
		setUndecorated(true);
		jpContent = (JPanel)getContentPane();
		double[][] dSize = { {TableLayout.FILL},
		        							{TableLayout.FILL,20}	};
		jpContent.setLayout(new TableLayout(dSize));
		jlImage = new JLabel(Util.getIcon(IMAGES_SPLASHSCREEN));
		jlRelease = new JLabel("Jajuk "+JAJUK_VERSION+" "+JAJUK_VERSION_DATE);
		jlRelease.setFont(new Font("Dialog",Font.PLAIN,12));
		setTitle(Messages.getString("JajukWindow.17"));  //$NON-NLS-1$
		jpContent.add(jlImage,"0,0");
		jpRelease = Util.getCentredPanel(jlRelease); //centred horizontaly
		jpContent.add(jpRelease,"0,1");
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = jlImage.getPreferredSize();
		setLocation(screenSize.width/2 - (labelSize.width/2),screenSize.height/2 - (labelSize.height/2));
		setVisible(true);
	}
}