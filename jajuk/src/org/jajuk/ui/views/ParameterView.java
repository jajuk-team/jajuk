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
 * Revision 1.2  2003/11/18 21:50:56  bflorat
 * 18/11/2003
 *
 * Revision 1.1  2003/11/18 18:58:06  bflorat
 * 18/11/2003
 *
 */

package org.jajuk.ui.views;

import java.awt.Checkbox;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import layout.TableLayout;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukFileChooser;
import org.jajuk.util.ConfigurationManager;

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
	JPanel jpOKCancel;
		JButton jbOK;
		
	
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
		int iXSeparator = 5;
		int iYSeparator = 5;
		
		//History
		jpHistory = new JPanel();
		double sizeHistory[][] = {{0.6,iXSeparator,0.3,iXSeparator},
										 {20,iYSeparator,25}};
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
		double sizeStart[][] = {{0.15,iXSeparator,0.6,iXSeparator,0.1,iXSeparator},
												 {20,iYSeparator,20,iYSeparator,20,iYSeparator,20}};
		jpStart.setLayout(new TableLayout(sizeStart));
		jlStart = new JLabel("Play :");
		bgStart = new ButtonGroup();
		jrbNothing = new JRadioButton("Nothing");
		jrbNothing.setToolTipText("No music at all at startup");
		jrbLast = new JRadioButton("Last one");
		jrbLast.setToolTipText("Play the last track played during previous session");
		jrbShuffle = new JRadioButton("Shuffle track");
		jrbShuffle.setToolTipText("Play a random track from the entire collection");
		jrbShuffle.setSelected(true);
		jrbFile = new JRadioButton("Specified file : ");
		jrbFile.setToolTipText("Select a file to played at startup");
		jbFile = new JButton(new ImageIcon(ICON_OPEN_FILE));		
		jbFile.addActionListener(this);
		jbFile.setToolTipText("Select a file to played at startup");
		bgStart.add(jrbNothing);
		bgStart.add(jrbLast);
		bgStart.add(jrbShuffle);
		bgStart.add(jrbFile);
		jbFile.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));	
		jpStart.setBorder(BorderFactory.createTitledBorder("Startup"));
		jpStart.add(jlStart,"0,2");
		jpStart.add(jrbNothing,"2,0");
		jpStart.add(jrbLast,"2,2");
		jpStart.add(jrbShuffle,"2,4");
		jpStart.add(jrbFile,"2,6");
		jpStart.add(jbFile,"4,6");
		//Confirmations
		jpConfirmations = new JPanel();
		jpConfirmations.setBorder(BorderFactory.createTitledBorder("Confirmations"));
		double sizeConfirmations[][] = {{0.99},
														 {iYSeparator,20,iYSeparator}};
		jpConfirmations.setLayout(new TableLayout(sizeConfirmations));
		jcbBeforeDelete = new JCheckBox("Before physically delete a file");
		jcbBeforeDelete.setToolTipText("Ask before physically delete a file");
		jcbBeforeDelete.setSelected(Boolean.valueOf(ConfigurationManager.getProperty(CONF_CONFIRMATIONS_DELETE_FILE)).booleanValue());
		jpConfirmations.add(jcbBeforeDelete,"0,1");
		
		//global layout
		double size[][] = {{0.5,0.5},
								 {0.15,iYSeparator,0.30,iYSeparator,0.20}};
		setLayout(new TableLayout(size));
		add(jpHistory,"0,0");
		add(jpStart,"0,2");
		add(jpConfirmations,"0,4");
		
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
		if (e.getSource() == jbClearHistory){
			
		}
		else if (e.getSource() == jbFile){
			jrbFile.setSelected(true);
			JajukFileChooser jfc = new JajukFileChooser();
			jfc.setMultiSelectionEnabled(false);
			int returnVal = jfc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File file = jfc.getSelectedFile();
				ConfigurationManager.setProperty(CONF_STARTUP_FILE,file.getAbsolutePath());	
			}
		}
		else if (e.getSource() == jbOK){
			
		}
		
	}

}
