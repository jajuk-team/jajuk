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

package org.jajuk.ui.views;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import layout.TableLayout;

import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.SearchResult;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.LNFManager;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.SearchBox;
import org.jajuk.ui.ViewManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.log.Log;

/**
 *  View used to set Jajuk paramers. 
 * <p>Configuration perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created    17 nov. 2003
 */
public class ParameterView extends ViewAdapter implements ActionListener,ListSelectionListener {

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
		SearchBox sbSearch;		
	JPanel jpConfirmations;
		JCheckBox jcbBeforeDelete;
		JCheckBox jcbBeforeExit;
	JPanel jpOptions;
		JCheckBox jcbDisplayUnmounted;
		JCheckBox jcbRestart;//TODO TBI hide unmounted devices
		JCheckBox jcbCover;
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
	JPanel jpTags;
		JCheckBox jcbDeepScan;
		JCheckBox jcbUseParentDir;
	JPanel jpOKCancel;
		JButton jbOK;
		JButton jbDefault;
		
	/** Previous value for hidden option, used to check if a refresh is need*/
	boolean bHidden;
		
		
	/**Return self instance*/
	public static synchronized ParameterView getInstance(){
		if (pv == null){
			pv = new ParameterView();
		}
		return pv;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		int iXSeparator = 5;
		int iYSeparator = 5;
		
		//History
		jpHistory = new JPanel();
		double sizeHistory[][] = {{0.6,iXSeparator,0.3},
										 {20,13*iYSeparator,25}};
		jpHistory.setLayout(new TableLayout(sizeHistory));
		jlHistory = new JLabel(Messages.getString("ParameterView.0")); //$NON-NLS-1$
		jtfHistory = new JTextField();
		jtfHistory.setInputVerifier(new InputVerifier(){
			 public boolean verify(JComponent input) {
			 	JTextField tf = (JTextField) input;
			 	String sText = tf.getText();
			 	try{
			 		int iValue = Integer.parseInt(sText);
			 		if (iValue < -1 ){
			 			jbOK.setEnabled(false);
			 			return false;
			 		}
			 	}
			 	catch(Exception e){
			 		jbOK.setEnabled(false);
			 		return false;
			 	}
			 	jbOK.setEnabled(true);
			 	return true;
			 }
			 
			  public boolean shouldYieldFocus(JComponent input) {
			  	return verify(input);
			  }
		});
		jtfHistory.setToolTipText(Messages.getString("ParameterView.2")); //$NON-NLS-1$
		jbClearHistory = new JButton(Messages.getString("ParameterView.3")); //$NON-NLS-1$
		jbClearHistory.setToolTipText(Messages.getString("ParameterView.4")); //$NON-NLS-1$
		jbClearHistory.addActionListener(this);
		jpHistory.add(jlHistory,"0,0"); //$NON-NLS-1$
		jpHistory.add(jtfHistory,"2,0"); //$NON-NLS-1$
		jpHistory.add(jbClearHistory,"0,2"); //$NON-NLS-1$
		jpHistory.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.8"))); //$NON-NLS-1$
		//Start
		jpStart = new JPanel();
		double sizeStart[][] = {{0.15,iXSeparator,0.4,iXSeparator,0.3,iXSeparator},
												 {20,iYSeparator,20,iYSeparator,20,iYSeparator,20}};
		jpStart.setLayout(new TableLayout(sizeStart));
		jlStart = new JLabel(Messages.getString("ParameterView.9")); //$NON-NLS-1$
		bgStart = new ButtonGroup();
		jrbNothing = new JRadioButton(Messages.getString("ParameterView.10")); //$NON-NLS-1$
		jrbNothing.setToolTipText(Messages.getString("ParameterView.11")); //$NON-NLS-1$
		jrbLast = new JRadioButton(Messages.getString("ParameterView.12")); //$NON-NLS-1$
		jrbLast.setToolTipText(Messages.getString("ParameterView.13")); //$NON-NLS-1$
		jrbShuffle = new JRadioButton(Messages.getString("ParameterView.14")); //$NON-NLS-1$
		jrbShuffle.setToolTipText(Messages.getString("ParameterView.15")); //$NON-NLS-1$
		jrbFile = new JRadioButton(Messages.getString("ParameterView.16")); //$NON-NLS-1$
		jrbFile.setToolTipText(Messages.getString("ParameterView.17")); //$NON-NLS-1$
		sbSearch = new SearchBox(this);
		if (STARTUP_MODE_FILE.equals(ConfigurationManager.getProperty(CONF_STARTUP_MODE))){
			String sFileId = ConfigurationManager.getProperty(CONF_STARTUP_FILE);
			if ( !"".equals(sFileId)){ //$NON-NLS-1$
				sbSearch.setText(FileManager.getFile(sFileId).getTrack().getName());
			}
		}
		sbSearch.setToolTipText(Messages.getString("ParameterView.18")); //$NON-NLS-1$
		bgStart.add(jrbNothing);
		bgStart.add(jrbLast);
		bgStart.add(jrbShuffle);
		bgStart.add(jrbFile);
		jpStart.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.19"))); //$NON-NLS-1$
		jpStart.add(jlStart,"0,2"); //$NON-NLS-1$
		jpStart.add(jrbNothing,"2,0"); //$NON-NLS-1$
		jpStart.add(jrbLast,"2,2"); //$NON-NLS-1$
		jpStart.add(jrbShuffle,"2,4"); //$NON-NLS-1$
		jpStart.add(jrbFile,"2,6"); //$NON-NLS-1$
		jpStart.add(sbSearch,"4,6"); //$NON-NLS-1$
		//Confirmations
		jpConfirmations = new JPanel();
		jpConfirmations.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.26"))); //$NON-NLS-1$
		double sizeConfirmations[][] = {{0.99},
										 {iYSeparator,20,iYSeparator,20,iYSeparator}};
		jpConfirmations.setLayout(new TableLayout(sizeConfirmations));
		jcbBeforeDelete = new JCheckBox(Messages.getString("ParameterView.27")); //$NON-NLS-1$
		jcbBeforeDelete.setToolTipText(Messages.getString("ParameterView.28")); //$NON-NLS-1$
		jcbBeforeExit = new JCheckBox(Messages.getString("ParameterView.29")); //$NON-NLS-1$
		jcbBeforeExit.setToolTipText(Messages.getString("ParameterView.30")); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeDelete,"0,1"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeExit,"0,3"); //$NON-NLS-1$
		//Options
		jpOptions = new JPanel();
		jpOptions.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.33"))); //$NON-NLS-1$
		double sizeOptions[][] = {{0.99},
														 {iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,60+2*iYSeparator,iYSeparator,40+iYSeparator,iYSeparator}};
		jpOptions.setLayout(new TableLayout(sizeOptions));
		jcbDisplayUnmounted = new JCheckBox(Messages.getString("ParameterView.34")); //$NON-NLS-1$
		jcbDisplayUnmounted.setToolTipText(Messages.getString("ParameterView.35")); //$NON-NLS-1$
		jcbRestart = new JCheckBox(Messages.getString("ParameterView.36")); //$NON-NLS-1$
		jcbRestart.setToolTipText(Messages.getString("ParameterView.37")); //$NON-NLS-1$
		jcbCover = new JCheckBox(Messages.getString("ParameterView.96")); //$NON-NLS-1$
		jcbCover.setToolTipText(Messages.getString("ParameterView.97")); //$NON-NLS-1$
		JPanel jpCombos = new JPanel();
		double sizeCombos[][] = {{0.50,0.50},
																 {20,iYSeparator,20,iYSeparator,20}};
		jpCombos.setLayout(new TableLayout(sizeCombos));
		jlLanguage = new JLabel(Messages.getString("ParameterView.38")); //$NON-NLS-1$
		jcbLanguage = new JComboBox();
		Iterator itDescs = Messages.getDescs().iterator();
		while (itDescs.hasNext()){
			String sDesc = (String)itDescs.next();
			jcbLanguage.addItem(Messages.getString(sDesc));
		}
		jcbLanguage.setToolTipText(Messages.getString("ParameterView.42")); //$NON-NLS-1$
		jlLAF = new JLabel(Messages.getString("ParameterView.43")); //$NON-NLS-1$
		jlLAF.setToolTipText(Messages.getString("ParameterView.44")); //$NON-NLS-1$
		jcbLAF = new JComboBox();
		Iterator it = LNFManager.getSupportedLNF().iterator();
		while (it.hasNext()){
			jcbLAF.addItem(it.next());
		}
		jcbLAF.setToolTipText(Messages.getString("ParameterView.45")); //$NON-NLS-1$
		jlLogLevel = new JLabel(Messages.getString("ParameterView.46")); //$NON-NLS-1$
		jcbLogLevel = new JComboBox();
		jcbLogLevel.addItem(Messages.getString("ParameterView.47")); //$NON-NLS-1$
		jcbLogLevel.addItem(Messages.getString("ParameterView.48")); //$NON-NLS-1$
		jcbLogLevel.addItem(Messages.getString("ParameterView.49")); //$NON-NLS-1$
		jcbLogLevel.addItem(Messages.getString("ParameterView.50")); //$NON-NLS-1$
		jcbLogLevel.addItem(Messages.getString("ParameterView.51")); //$NON-NLS-1$
		jcbLogLevel.setToolTipText(Messages.getString("ParameterView.52")); //$NON-NLS-1$
		jpCombos.add(jlLanguage,"0,0"); //$NON-NLS-1$
		jpCombos.add(jcbLanguage,"1,0"); //$NON-NLS-1$
		jpCombos.add(jlLAF,"0,2"); //$NON-NLS-1$
		jpCombos.add(jcbLAF,"1,2"); //$NON-NLS-1$
		jpCombos.add(jlLogLevel,"0,4"); //$NON-NLS-1$
		jpCombos.add(jcbLogLevel,"1,4"); //$NON-NLS-1$
		JPanel jpIntro = new JPanel();
		double sizeIntro[][] = {{0.50,0.50},
							 {20,iYSeparator,20}};
		jpIntro.setLayout(new TableLayout(sizeIntro));
		jlIntroPosition = new JLabel(Messages.getString("ParameterView.59")); //$NON-NLS-1$
		jtfIntroPosition = new JTextField(3);
		jtfIntroPosition.setInputVerifier(new InputVerifier(){
			 public boolean verify(JComponent input) {
			 	JTextField tf = (JTextField) input;
			 	String sText = tf.getText();
			 	if (sText.length()<1 || sText.length()>2){
			 		jbOK.setEnabled(false);
			 		return false;
			 	}
			 	try{
			 		int iValue = Integer.parseInt(sText);
			 		if (iValue < 0 || iValue>99){
			 			jbOK.setEnabled(false);
			 			return false;
			 		}
			 	}
			 	catch(Exception e){
			 		return false;
			 	}
			 	jbOK.setEnabled(true);
			 	return true;
			 }
			 
			  public boolean shouldYieldFocus(JComponent input) {
			  	return verify(input);
			  }
		});
		
		jtfIntroPosition.setToolTipText(Messages.getString("ParameterView.60") ); //$NON-NLS-1$
		jlIntroLength = new JLabel(Messages.getString("ParameterView.61")); //$NON-NLS-1$
		jtfIntroLength = new JTextField(3);
		jtfIntroLength.setInputVerifier(new InputVerifier(){
			 public boolean verify(JComponent input) {
			 	JTextField tf = (JTextField) input;
			 	String sText = tf.getText();
			 	try{
			 		int iValue = Integer.parseInt(sText);
			 		if (iValue <= 0 ){
			 			jbOK.setEnabled(false);
			 			return false;
			 		}
			 	}
			 	catch(Exception e){
			 		jbOK.setEnabled(false);
			 		return false;
			 	}
			 	jbOK.setEnabled(true);
			 	return true;
			 }
			 
			  public boolean shouldYieldFocus(JComponent input) {
			  	return verify(input);
			  }
		});
		jtfIntroLength.setToolTipText(Messages.getString("ParameterView.62") ); //$NON-NLS-1$
		jpIntro.add(jlIntroPosition,"0,0"); //$NON-NLS-1$
		jpIntro.add(jtfIntroPosition,"1,0"); //$NON-NLS-1$
		jpIntro.add(jlIntroLength,"0,2"); //$NON-NLS-1$
		jpIntro.add(jtfIntroLength,"1,2"); //$NON-NLS-1$
		jpOptions.add(jcbDisplayUnmounted,"0,1"); //$NON-NLS-1$
		jpOptions.add(jcbRestart,"0,3"); //$NON-NLS-1$
		jpOptions.add(jcbCover,"0,5"); //$NON-NLS-1$
		jpOptions.add(jpCombos,"0,7"); //$NON-NLS-1$
		jpOptions.add(jpIntro,"0,9"); //$NON-NLS-1$
		//P2P
		jpP2P = new JPanel();
		jpP2P.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.71"))); //$NON-NLS-1$
		double sizeP2P[][] = {{0.6,0.3,0.1},
				{iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
		jpP2P.setLayout(new TableLayout(sizeP2P));
		jcbShare = new JCheckBox(Messages.getString("ParameterView.72")); //$NON-NLS-1$
		jcbShare.setToolTipText(Messages.getString("ParameterView.73")); //$NON-NLS-1$
		jlPasswd = new JLabel(Messages.getString("ParameterView.74")); //$NON-NLS-1$
		jpfPasswd = new JPasswordField();
		jpfPasswd.setToolTipText(Messages.getString("ParameterView.75")); //$NON-NLS-1$
		jcbAddRemoteProperties = new JCheckBox(Messages.getString("ParameterView.76")); //$NON-NLS-1$
		jcbAddRemoteProperties.setToolTipText(Messages.getString("ParameterView.77")); //$NON-NLS-1$
		jcbHideProperties = new JCheckBox(Messages.getString("ParameterView.78")); //$NON-NLS-1$
		jcbHideProperties.setToolTipText(Messages.getString("ParameterView.79")); //$NON-NLS-1$
		jpP2P.add(jcbShare,"0,1"); //$NON-NLS-1$
		jpP2P.add(jlPasswd,"0,3"); //$NON-NLS-1$
		jpP2P.add(jpfPasswd,"1,3"); //$NON-NLS-1$
		jpP2P.add(jcbAddRemoteProperties,"0,5"); //$NON-NLS-1$
		jpP2P.add(jcbHideProperties,"0,7"); //$NON-NLS-1$
		//Tags
		jpTags = new JPanel();
		jpTags.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.98")));  //$NON-NLS-1$
		double sizeTags[][] = {{0.99},
				{iYSeparator,20,iYSeparator,20,iYSeparator}};
		jpTags.setLayout(new TableLayout(sizeTags));
		jcbDeepScan = new JCheckBox(Messages.getString("ParameterView.99"));  //$NON-NLS-1$
		jcbDeepScan.setToolTipText(Messages.getString("ParameterView.100")); //$NON-NLS-1$
		jcbUseParentDir = new JCheckBox(Messages.getString("ParameterView.101"));  //$NON-NLS-1$
		jcbUseParentDir.setToolTipText(Messages.getString("ParameterView.102")); //$NON-NLS-1$
		jpTags.add(jcbDeepScan,"0,1"); //$NON-NLS-1$
		jpTags.add(jcbUseParentDir,"0,3"); //$NON-NLS-1$
		
		//OK
		jpOKCancel = new JPanel();
		jpOKCancel.setLayout(new FlowLayout());
		jbOK = new JButton(Messages.getString("ParameterView.85")); //$NON-NLS-1$
		jbOK.addActionListener(this);
		jpOKCancel.add(jbOK);
		jbDefault = new JButton(Messages.getString("ParameterView.86")); //$NON-NLS-1$
		jbDefault.addActionListener(this);
		jpOKCancel.add(jbDefault);
		//global layout
		double size[][] = {{0.5,0.5},
					{0.35,iYSeparator,0.25,iYSeparator,0.20,iYSeparator,0.1}};
		setLayout(new TableLayout(size));
		add(jpHistory,"1,0"); //$NON-NLS-1$
		add(jpStart,"0,2"); //$NON-NLS-1$
		add(jpConfirmations,"0,4"); //$NON-NLS-1$
		add(jpOptions,"0,0"); //$NON-NLS-1$
		add(jpP2P,"1,2"); //$NON-NLS-1$
		add(jpTags,"1,4"); //$NON-NLS-1$
		add(jpOKCancel,"0,6"); //$NON-NLS-1$
		//update widgets state
		updateSelection();

	}
	
	/**
	 * 
	 */
	public ParameterView() {
		pv = this;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("ParameterView.87"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jbClearHistory){
			History.getInstance().clear();
			CommandJPanel.getInstance().clearHistoryBar();
		}
		else if (e.getSource() == jbOK){
			//**Read all parameters**
			//Options
			boolean bHiddenState = jcbDisplayUnmounted.isSelected(); 
			if ( bHiddenState != bHidden){ //check if this option changed to lauch a refresh if needed
				ConfigurationManager.setProperty(CONF_OPTIONS_HIDE_UNMOUNTED,Boolean.toString(bHiddenState));
				bHidden = bHiddenState;
				ObservationManager.notify(EVENT_DEVICE_REFRESH);
			}
			ConfigurationManager.setProperty(CONF_OPTIONS_RESTART,Boolean.toString(jcbRestart.isSelected()));
			ConfigurationManager.setProperty(CONF_OPTIONS_COVER,Boolean.toString(jcbCover.isSelected()));
			String sLocal = (String)Messages.getLocals().get(jcbLanguage.getSelectedIndex());
			if (!Messages.getLocal().equals(sLocal)){  //local has changed
				Messages.showInfoMessage(Messages.getString("ParameterView.103")); //$NON-NLS-1$
			}
			ConfigurationManager.setProperty(CONF_OPTIONS_LANGUAGE,sLocal);
			ConfigurationManager.setProperty(CONF_OPTIONS_LNF,(String)jcbLAF.getSelectedItem());
			if (!LNFManager.getCurrent().equals((String)jcbLAF.getSelectedItem())){  //Lnf has changed
				Messages.showInfoMessage(Messages.getString("ParameterView.104")); //$NON-NLS-1$
			}
			int iLogLevel = jcbLogLevel.getSelectedIndex(); 
			Log.setVerbosity(iLogLevel);
			ConfigurationManager.setProperty(CONF_OPTIONS_LOG_LEVEL,Integer.toString(iLogLevel));
			String sIntroPosition = jtfIntroPosition.getText();
			if (!jtfIntroPosition.equals("")){ //$NON-NLS-1$
				ConfigurationManager.setProperty(CONF_OPTIONS_INTRO_BEGIN,sIntroPosition);
			}
			String sIntroLength = jtfIntroLength.getText();
			if (!jtfIntroLength.equals("")){ //$NON-NLS-1$
				ConfigurationManager.setProperty(CONF_OPTIONS_INTRO_LENGTH,sIntroLength);
			}
			//startup
			if (jrbNothing.isSelected()){
				ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_NOTHING);
			}
			else if (jrbLast.isSelected()){
				ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_LAST);
			}
			else if (jrbShuffle.isSelected()){
				ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_SHUFFLE);
			}
			else if (jrbFile.isSelected()){
				ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_FILE);
			}
			//Confirmations
			ConfigurationManager.setProperty(CONF_CONFIRMATIONS_DELETE_FILE,Boolean.toString(jcbBeforeDelete.isSelected()));
			ConfigurationManager.setProperty(CONF_CONFIRMATIONS_EXIT,Boolean.toString(jcbBeforeExit.isSelected()));
			//history
			String sHistoryDuration = jtfHistory.getText();
			if (!sHistoryDuration.equals("")){ //$NON-NLS-1$
				ConfigurationManager.setProperty(CONF_HISTORY,sHistoryDuration);
			}
			//P2P
			ConfigurationManager.setProperty(CONF_OPTIONS_P2P_SHARE,Boolean.toString(jcbShare.isSelected()));
			ConfigurationManager.setProperty(CONF_OPTIONS_P2P_ADD_REMOTE_PROPERTIES,Boolean.toString(jcbAddRemoteProperties.isSelected()));
			ConfigurationManager.setProperty(CONF_OPTIONS_P2P_HIDE_LOCAL_PROPERTIES,Boolean.toString(jcbHideProperties.isSelected()));
			String sPass = jpfPasswd.getSelectedText();
			if (sPass!=null && !sPass.equals("")){ //$NON-NLS-1$
				ConfigurationManager.setProperty(CONF_OPTIONS_P2P_PASSWORD,MD5Processor.hash(sPass));
			}
			//tags
			ConfigurationManager.setProperty(CONF_TAGS_DEEP_SCAN,Boolean.toString(jcbDeepScan.isSelected()));
			ConfigurationManager.setProperty(CONF_TAGS_USE_PARENT_DIR,Boolean.toString(jcbUseParentDir.isSelected()));
			//cover
			if ( ConfigurationManager.getBoolean(CONF_OPTIONS_COVER)){
				ViewManager.notify(EVENT_VIEW_SHOW_REQUEST,CoverView.class);
				ViewManager.notify(EVENT_VIEW_REFRESH_REQUEST,CoverView.class);
			}
			else{
				ViewManager.notify(EVENT_VIEW_CLOSE_REQUEST,CoverView.class);
			}
			InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.109"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
			ConfigurationManager.commit();
			
		}
		else if (e.getSource() == jbDefault){
			ConfigurationManager.setDefaultProperties();
			ConfigurationManager.setProperty(CONF_FIRST_CON,FALSE);
			updateSelection();
			InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.110"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
			ConfigurationManager.commit();
		}
		
	}
	
	/**
	 * Set widgets to specified value
	 */
	private void updateSelection(){
		jtfHistory.setText(ConfigurationManager.getProperty(CONF_HISTORY));
		if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_FILE)){
			jrbFile.setSelected(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST)){
			jrbLast.setSelected(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOTHING)){
			jrbNothing.setSelected(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_SHUFFLE)){
			jrbShuffle.setSelected(true);
		}
		jcbBeforeDelete.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE));
		jcbBeforeExit.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_EXIT));
		jcbDisplayUnmounted.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED));
		jcbRestart.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_RESTART));
		jcbCover.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_COVER));
		jcbLanguage.setSelectedIndex(Messages.getLocals().indexOf(ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE)));
		jcbLAF.setSelectedItem(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
		jcbLogLevel.setSelectedIndex(Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_LOG_LEVEL)));
		jtfIntroLength.setText(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_LENGTH));
		jtfIntroPosition.setText(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_BEGIN));
		jcbShare.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_P2P_SHARE));
		jpfPasswd.setText(ConfigurationManager.getProperty(CONF_OPTIONS_P2P_PASSWORD));
		jcbAddRemoteProperties.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_P2P_ADD_REMOTE_PROPERTIES));
		bHidden = ConfigurationManager.getBoolean(CONF_OPTIONS_P2P_HIDE_LOCAL_PROPERTIES);
		jcbHideProperties.setSelected(bHidden);
		jcbDeepScan.setSelected(ConfigurationManager.getBoolean(CONF_TAGS_DEEP_SCAN));
		jcbUseParentDir.setSelected(ConfigurationManager.getBoolean(CONF_TAGS_USE_PARENT_DIR));
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.ParameterView"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()){
			SearchResult sr = (SearchResult)sbSearch.alResults.get(sbSearch.jlist.getSelectedIndex());
			sbSearch.setText(sr.getFile().getTrack().getName());
			ConfigurationManager.setProperty(CONF_STARTUP_FILE,sr.getFile().getId());
			sbSearch.popup.hide();
		}
	}


}
