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
 * Revision 1.1  2003/11/18 18:58:06  bflorat
 * 18/11/2003
 *
 */

package org.jajuk.ui.views;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import layout.TableLayout;

import org.jajuk.i18n.Messages;

/**
 *  View used to set Jajuk paramers. 
 * <p>Configuration perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created    17 nov. 2003
 */
public class ParameterView extends ViewAdapter implements ActionListener {

	/**Self instance*/
	private static ParameterView pv;
	
	JPanel jpHistory;
		JLabel jlHistory;
		JTextField jtfHistory;
		JButton jbClearHistory;
	JPanel jpStart;
		JLabel jlStart;
		ButtonGroup bgStart;
		JRadioButton jrbNothing;
		JRadioButton jrbLast;
		JRadioButton jrbShuffle;
		JRadioButton jrbFile;
		JButton jbFile;		
	JPanel jpConfirmations;
		JCheckBox jcbBeforeDelete;
	JPanel jpOptions;
		JCheckBox jcbDisplayUnmounted;
		JCheckBox jcbRestart;
		JLabel jlLanguage;
		JComboBox jcbLanguage;
		JLabel jlLAF;
		JComboBox jcbLAF;
		JLabel jlLogLevel;
		JComboBox jcbLogLevel;
		JLabel jlIntroPosition;
		JTextField jtfIntroPosition;
		JLabel jlIntroLength;
		JTextField jtfIntroLength;
	JPanel jpP2P;
		JCheckBox jcbShare;
		JLabel jlPasswd;
		JPasswordField jpfPasswd;
		JCheckBox jcbAddRemoteProperties;
		JCheckBox jcbHideProperties;
	
	/**Return self instance*/
	public static ParameterView getInstance(){
		if (pv == null){
			pv = new ParameterView();
		}
		return pv;
	}
	
	/**
	 * 
	 */
	public ParameterView() {
		//History
		jpHistory = new JPanel();
		double sizeHistory[][] = {{0.6,0.05,0.3,0.05},
										 {20,10,25}};
		jpHistory.setLayout(new TableLayout(sizeHistory));
		jlHistory = new JLabel("History duration: ");
		jlHistory.setToolTipText("Set here time in days you want to keep traces of listened tracks. Set to -1 if you don't want any history and 0 if you want permanent history");
		jtfHistory = new JTextField();
		jtfHistory.setToolTipText("Set here time in days you want to keep traces of listened tracks. Set to -1 if you don't want any history and 0 if you want permanent history");
		jbClearHistory = new JButton("Clear history");
		jbClearHistory.setToolTipText("Clear history");
		jbClearHistory.addActionListener(this);
		jpHistory.add(jlHistory,"0,0");
		jpHistory.add(jtfHistory,"2,0");
		jpHistory.add(jbClearHistory,"0,2");
		jpHistory.setBorder(BorderFactory.createTitledBorder("History"));
		//Start
		jpStart = new JPanel();
		double sizeStart[][] = {{0.15,0.05,0.6,0.05,0.1,0.05},
												 {20,10,20,10,20,10,20}};
		jpStart.setLayout(new TableLayout(sizeStart));
		jlStart = new JLabel("Play :");
		bgStart = new ButtonGroup();
		jrbNothing = new JRadioButton("Nothing");
		jrbLast = new JRadioButton("Last one");
		jrbShuffle = new JRadioButton("Shuffle track");
		jrbFile = new JRadioButton("Specified file : ");
		jbFile = new JButton(new ImageIcon(ICON_OPEN_FILE));		
		bgStart.add(jrbNothing);
		bgStart.add(jrbLast);
		bgStart.add(jrbShuffle);
		bgStart.add(jrbFile);
		jbFile.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));	
		jpStart.setBorder(BorderFactory.createTitledBorder("Startup"));
		jpStart.add(jlStart,"0,1");
		jpStart.add(jrbNothing,"2,0");
		jpStart.add(jrbLast,"2,2");
		jpStart.add(jrbShuffle,"2,4");
		jpStart.add(jrbFile,"2,6");
		jpStart.add(jbFile,"4,6");
		
		//global layout
		double size[][] = {{0.5,0.5},
								 {0.20,5,0.40}};
		setLayout(new TableLayout(size));
		add(jpHistory,"0,0");
		add(jpStart,"0,2");
		
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("View_Description_Parameters");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
	}

}
