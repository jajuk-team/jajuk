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
 * Revision 1.3  2003/11/20 19:12:22  bflorat
 * 20/11/2003
 *
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
		double sizeHistory[][] = {{0.6,iXSeparator,0.3},
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
		//Options
		jpOptions = new JPanel();
		jpOptions.setBorder(BorderFactory.createTitledBorder("Options"));
		double sizeOptions[][] = {{0.99},
														 {iYSeparator,20,iYSeparator,20,iYSeparator,60+2*iYSeparator,iYSeparator,40+iYSeparator,iYSeparator}};
		jpOptions.setLayout(new TableLayout(sizeOptions));
		jcbDisplayUnmounted = new JCheckBox("Only display mounted devices");
		jcbDisplayUnmounted.setSelected(Boolean.valueOf(ConfigurationManager.getProperty(CONF_OPTIONS_HIDE_UNMOUNTED)).booleanValue());
		jcbDisplayUnmounted.setToolTipText("Hides tracks located in unmounted devices");
		jcbRestart = new JCheckBox("Restart when reaching end of collection");
		jcbRestart.setSelected(Boolean.valueOf(ConfigurationManager.getProperty(CONF_OPTIONS_RESTART)).booleanValue());
		jcbRestart.setToolTipText("Restart the entire collection when reaching the very end in continue mode");
		JPanel jpCombos = new JPanel();
		double sizeCombos[][] = {{0.50,0.50},
																 {20,iYSeparator,20,iYSeparator,20}};
		jpCombos.setLayout(new TableLayout(sizeCombos));
		jlLanguage = new JLabel("Language : ");
		jcbLanguage = new JComboBox();
		jcbLanguage.addItem(Messages.getString("options_language_default"));
		jcbLanguage.addItem("English (en)");
		jcbLanguage.addItem("French (fr)");
		jcbLanguage.setSelectedItem(ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE));
		jcbLanguage.setToolTipText("Interface language setting");
		jlLAF = new JLabel("Look and Feel : ");
		jlLAF.setToolTipText("Look and feel setting");
		jcbLAF = new JComboBox();
		jcbLAF.setToolTipText("Look and feel setting");
		//TODO implements l&f : use a property with ',' separator
		jlLogLevel = new JLabel("Log level : ");
		jcbLogLevel = new JComboBox();
		jcbLogLevel.addItem("FATAL");
		jcbLogLevel.addItem("ERROR");
		jcbLogLevel.addItem("WARNING");
		jcbLogLevel.addItem("INFO");
		jcbLogLevel.addItem("DEBUG");
		jcbLogLevel.setSelectedItem(ConfigurationManager.getProperty(CONF_OPTIONS_LOG_LEVEL));
		jcbLogLevel.setToolTipText("Jajuk verbosity : FATAL:display only critical erros, ERROR:+display errors, WARNING:+display warnings, DEBUG: all messages");
		jpCombos.add(jlLanguage,"0,0");
		jpCombos.add(jcbLanguage,"1,0");
		jpCombos.add(jlLAF,"0,2");
		jpCombos.add(jcbLAF,"1,2");
		jpCombos.add(jlLogLevel,"0,4");
		jpCombos.add(jcbLogLevel,"1,4");
		JPanel jpIntro = new JPanel();
		double sizeIntro[][] = {{0.50,0.50},
														 {20,iYSeparator,20}};
		jpIntro.setLayout(new TableLayout(sizeIntro));
		jlIntroPosition = new JLabel("Intro begin position (%) : ");
		jtfIntroPosition = new JTextField(3);
		jtfIntroPosition.setToolTipText("Introduction position inside track in %, from 0 ( begining of the file) to 99 ( end of the track )" );
		jlIntroLength = new JLabel("Intro length (sec) : ");
		jtfIntroLength = new JTextField(3);
		jtfIntroLength.setToolTipText("Introduction length in seconds" );
		jpIntro.add(jlIntroPosition,"0,0");
		jpIntro.add(jtfIntroPosition,"1,0");
		jpIntro.add(jlIntroLength,"0,2");
		jpIntro.add(jtfIntroLength,"1,2");
		jpOptions.add(jcbDisplayUnmounted,"0,1");
		jpOptions.add(jcbRestart,"0,3");
		jpOptions.add(jpCombos,"0,5");
		jpOptions.add(jpIntro,"0,7");
		
		
		//global layout
		double size[][] = {{0.5,0.5},
								 {0.40,iYSeparator,0.30,iYSeparator,0.20}};
		setLayout(new TableLayout(size));
		add(jpHistory,"0,0");
		add(jpStart,"0,2");
		add(jpConfirmations,"0,4");
		add(jpOptions,"1,0");
		
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
