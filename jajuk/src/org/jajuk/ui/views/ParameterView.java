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

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.Main;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.SearchResult;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.LNFManager;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.SearchBox;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  View used to set Jajuk paramers. 
 * <p>Configuration perspective
 *  * <p>Singleton
 * @author     Bertrand Florat
 * @created    17 nov. 2003
 */
public class ParameterView extends ViewAdapter implements ActionListener,ListSelectionListener,ItemListener,ChangeListener {
	
	/**Self instance*/
	private static ParameterView pv;
	
	JTabbedPane jtpMain;
	JPanel jpHistory;
	JLabel jlHistory;
	JTextField jtfHistory;
	JButton jbClearHistory;
	JPanel jpStart;
	JLabel jlStart;
	ButtonGroup bgStart;
	JRadioButton jrbNothing;
	JRadioButton jrbLast;
	JRadioButton jrbLastKeepPos;
	JRadioButton jrbShuffle;
	JRadioButton jrbBestof;
	JRadioButton jrbNovelties;
	JRadioButton jrbFile;
	SearchBox sbSearch;		
	JPanel jpConfirmations;
	JCheckBox jcbBeforeDelete;
	JCheckBox jcbBeforeExit;
	JCheckBox jcbBeforeRemoveDevice;
	JCheckBox jcbBeforeDeleteCover;
	JPanel jpOptions;
	JCheckBox jcbDisplayUnmounted;
	JCheckBox jcbRestart;
	JCheckBox jcbSearchUnmounted;
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
	JLabel jlBestofSize;
	JTextField jtfBestofSize;
	JLabel jlNoveltiesAge;
	JTextField jtfNoveltiesAge;
	JLabel jlVisiblePlanned;
	JTextField jtfVisiblePlanned;
	JPanel jpP2P;
	JCheckBox jcbShare;
	JLabel jlPasswd;
	JPasswordField jpfPasswd;
	JCheckBox jcbAddRemoteProperties;
	JCheckBox jcbHideProperties;
	JPanel jpTags;
	JCheckBox jcbDeepScan;
	JCheckBox jcbUseParentDir;
	JPanel jpAdvanced;
	JCheckBox jcbBackup;
	JLabel jlBackupSize;
	JTextField jtfBackupSize;
	JLabel jlCollectionEncoding;
	JComboBox jcbCollectionEncoding;
	JCheckBox jcbRegexp;
	JPanel jpNetwork;
	JCheckBox jcbProxy;
	JLabel jlProxyHostname;
	JTextField jtfProxyHostname;
	JLabel jlProxyPort;
	JTextField jtfProxyPort;
	JLabel jlProxyLogin;
	JTextField jtfProxyLogin;
	JLabel jlConnectionTO;
	JTextField jtfConnectionTO;
	JLabel jlTransfertTO;
	JTextField jtfTransfertTO;
	JPanel jpCovers;
	JCheckBox jcbAutoCover;
	JCheckBox jcbShuffleCover;
	JCheckBox jcbPreLoad;
	JCheckBox jcbResize;
	JCheckBox jcbLoadEachTrack;
	JLabel jlMinSize;
	JTextField jtfMinSize;
	JLabel jlMaxSize;
	JTextField jtfMaxSize;
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
	public void populate(){
		int iXSeparator = 5;
		int iYSeparator = 5;
		
		//--History
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
		jbClearHistory = new JButton(Messages.getString("ParameterView.3"),Util.getIcon(ICON_CLEAR)); //$NON-NLS-1$
		jbClearHistory.setToolTipText(Messages.getString("ParameterView.4")); //$NON-NLS-1$
		jbClearHistory.addActionListener(this);
		jpHistory.add(jlHistory,"0,0"); //$NON-NLS-1$
		jpHistory.add(jtfHistory,"2,0"); //$NON-NLS-1$
		jpHistory.add(jbClearHistory,"0,2"); //$NON-NLS-1$
		jpHistory.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.8"))); //$NON-NLS-1$
		
		//--Startup
		jpStart = new JPanel();
		double sizeStart[][] = {{0.15,iXSeparator,0.4,iXSeparator,0.3,iXSeparator},
				{20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20}};
		jpStart.setLayout(new TableLayout(sizeStart));
		jlStart = new JLabel(Messages.getString("ParameterView.9")); //$NON-NLS-1$
		bgStart = new ButtonGroup();
		jrbNothing = new JRadioButton(Messages.getString("ParameterView.10")); //$NON-NLS-1$
		jrbNothing.setToolTipText(Messages.getString("ParameterView.11")); //$NON-NLS-1$
		jrbNothing.addItemListener(this);
		jrbLast = new JRadioButton(Messages.getString("ParameterView.12")); //$NON-NLS-1$
		jrbLast.setToolTipText(Messages.getString("ParameterView.13")); //$NON-NLS-1$
		jrbLast.addItemListener(this);
		jrbLastKeepPos = new JRadioButton(Messages.getString("ParameterView.135")); //$NON-NLS-1$
		jrbLastKeepPos.setToolTipText(Messages.getString("ParameterView.136")); //$NON-NLS-1$
		jrbLastKeepPos.addItemListener(this);
		jrbShuffle = new JRadioButton(Messages.getString("ParameterView.14")); //$NON-NLS-1$
		jrbShuffle.setToolTipText(Messages.getString("ParameterView.15")); //$NON-NLS-1$
		jrbShuffle.addItemListener(this);
		jrbBestof = new JRadioButton(Messages.getString("ParameterView.131")); //$NON-NLS-1$
		jrbBestof.setToolTipText(Messages.getString("ParameterView.132")); //$NON-NLS-1$
		jrbBestof.addItemListener(this);
		jrbNovelties = new JRadioButton(Messages.getString("ParameterView.133")); //$NON-NLS-1$
		jrbNovelties.setToolTipText(Messages.getString("ParameterView.134")); //$NON-NLS-1$
		jrbNovelties.addItemListener(this);
		jrbFile = new JRadioButton(Messages.getString("ParameterView.16")); //$NON-NLS-1$
		jrbFile.setToolTipText(Messages.getString("ParameterView.17")); //$NON-NLS-1$
		jrbFile.addItemListener(this);
		sbSearch = new SearchBox(this);
		sbSearch.setEnabled(false); //disabled by default, is enabled only if jrbFile is enabled
		//set choosen track in file selection
		String sFileId = ConfigurationManager.getProperty(CONF_STARTUP_FILE);
		if ( !"".equals(sFileId)){ //$NON-NLS-1$
			File file = FileManager.getFile(sFileId);
			if (file != null){
			    sbSearch.setText(file.getTrack().getName());
			}
			else{
			    ConfigurationManager.setProperty(CONF_STARTUP_FILE,""); //the file exists no more, remove its id as startup file //$NON-NLS-1$
			}
		}
		sbSearch.setToolTipText(Messages.getString("ParameterView.18")); //$NON-NLS-1$
		bgStart.add(jrbNothing);
		bgStart.add(jrbLast);
		bgStart.add(jrbLastKeepPos);
		bgStart.add(jrbShuffle);
		bgStart.add(jrbBestof);
		bgStart.add(jrbNovelties);
		bgStart.add(jrbFile);
		jpStart.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.19"))); //$NON-NLS-1$
		jpStart.add(jlStart,"0,2"); //$NON-NLS-1$
		jpStart.add(jrbNothing,"2,0"); //$NON-NLS-1$
		jpStart.add(jrbLast,"2,2"); //$NON-NLS-1$
		jpStart.add(jrbLastKeepPos,"2,4"); //$NON-NLS-1$
		jpStart.add(jrbShuffle,"2,6"); //$NON-NLS-1$
		jpStart.add(jrbBestof,"2,8"); //$NON-NLS-1$
		jpStart.add(jrbNovelties,"2,10"); //$NON-NLS-1$
		jpStart.add(jrbFile,"2,12"); //$NON-NLS-1$
		jpStart.add(sbSearch,"4,12"); //$NON-NLS-1$
		
		//--Confirmations
		jpConfirmations = new JPanel();
		jpConfirmations.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.26"))); //$NON-NLS-1$
		double sizeConfirmations[][] = {{0.99},
				{iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
		jpConfirmations.setLayout(new TableLayout(sizeConfirmations));
		jcbBeforeDelete = new JCheckBox(Messages.getString("ParameterView.27")); //$NON-NLS-1$
		jcbBeforeDelete.setToolTipText(Messages.getString("ParameterView.28")); //$NON-NLS-1$
		jcbBeforeExit = new JCheckBox(Messages.getString("ParameterView.29")); //$NON-NLS-1$
		jcbBeforeExit.setToolTipText(Messages.getString("ParameterView.30")); //$NON-NLS-1$
		jcbBeforeRemoveDevice = new JCheckBox(Messages.getString("ParameterView.164")); //$NON-NLS-1$
		jcbBeforeRemoveDevice.setToolTipText(Messages.getString("ParameterView.165")); //$NON-NLS-1$
		jcbBeforeDeleteCover = new JCheckBox(Messages.getString("ParameterView.171")); //$NON-NLS-1$
		jcbBeforeDeleteCover.setToolTipText(Messages.getString("ParameterView.172")); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeDelete,"0,1"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeExit,"0,3"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeRemoveDevice,"0,5"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeDeleteCover,"0,7"); //$NON-NLS-1$
		
		//--Options
		jpOptions = new JPanel();
		jpOptions.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.33"))); //$NON-NLS-1$
		double sizeOptions[][] = {{0.99},
				{iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,60+2*iYSeparator,iYSeparator,130,iYSeparator}};
		jpOptions.setLayout(new TableLayout(sizeOptions));
		jcbDisplayUnmounted = new JCheckBox(Messages.getString("ParameterView.34")); //$NON-NLS-1$
		jcbDisplayUnmounted.setToolTipText(Messages.getString("ParameterView.35")); //$NON-NLS-1$
		jcbRestart = new JCheckBox(Messages.getString("ParameterView.36")); //$NON-NLS-1$
		jcbRestart.setToolTipText(Messages.getString("ParameterView.37")); //$NON-NLS-1$
		jcbSearchUnmounted = new JCheckBox(Messages.getString("ParameterView.127")); //$NON-NLS-1$
		jcbSearchUnmounted.setToolTipText(Messages.getString("ParameterView.128")); //$NON-NLS-1$
		JPanel jpCombos = new JPanel();
		double sizeCombos[][] = {{0.50,0.50},
				{20,iYSeparator,20,iYSeparator,20}};
		jpCombos.setLayout(new TableLayout(sizeCombos));
		jlLanguage = new JLabel(Messages.getString("ParameterView.38")); //$NON-NLS-1$
		jcbLanguage = new JComboBox();
		Iterator itDescs = Messages.getInstance().getDescs().iterator();
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
		//Intro
		JPanel jp = new JPanel();
		double sizeIntro[][] = {{0.50,0.50},
				{20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
		jp.setLayout(new TableLayout(sizeIntro));
		//intro position
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
		//intro length
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
		jtfIntroLength.setToolTipText(Messages.getString("ParameterView.110") ); //$NON-NLS-1$
		//best of size
		jlBestofSize = new JLabel(Messages.getString("ParameterView.111")); //$NON-NLS-1$
		jlBestofSize.setToolTipText(Messages.getString("ParameterView.112") ); //$NON-NLS-1$
		jtfBestofSize = new JTextField(3);
		jtfBestofSize.setToolTipText(Messages.getString("ParameterView.112") ); //$NON-NLS-1$
		jtfBestofSize.setInputVerifier(new InputVerifier(){
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try{
					int iValue = Integer.parseInt(sText);
					if (iValue < 1 || iValue>100){
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
		//novelties age
		jlNoveltiesAge = new JLabel(Messages.getString("ParameterView.129")); //$NON-NLS-1$
		jlNoveltiesAge.setToolTipText(Messages.getString("ParameterView.130")); //$NON-NLS-1$
		jtfNoveltiesAge = new JTextField(3);
		jtfNoveltiesAge.setToolTipText(Messages.getString("ParameterView.130")); //$NON-NLS-1$
		jtfNoveltiesAge.setInputVerifier(new InputVerifier(){
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try{
					int iValue = Integer.parseInt(sText);
					if (iValue < 0){ //if adding age =0, it mean today, no max limit 
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
		//number of visible tracks
		jlVisiblePlanned = new JLabel(Messages.getString("ParameterView.177")); //$NON-NLS-1$
		jlVisiblePlanned.setToolTipText(Messages.getString("ParameterView.178")); //$NON-NLS-1$
		jtfVisiblePlanned = new JTextField(3);
		jtfVisiblePlanned.setToolTipText(Messages.getString("ParameterView.130")); //$NON-NLS-1$
		jtfVisiblePlanned.setInputVerifier(new InputVerifier(){
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try{
					int iValue = Integer.parseInt(sText);
					if (iValue < 0 || iValue > 100){ //number of planned tracks between 0 and 100 
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
		//add panels
		jp.add(jlIntroPosition,"0,0"); //$NON-NLS-1$
		jp.add(jtfIntroPosition,"1,0"); //$NON-NLS-1$
		jp.add(jlIntroLength,"0,2"); //$NON-NLS-1$
		jp.add(jtfIntroLength,"1,2"); //$NON-NLS-1$
		jp.add(jlBestofSize,"0,4"); //$NON-NLS-1$
		jp.add(jtfBestofSize,"1,4"); //$NON-NLS-1$
		jp.add(jlNoveltiesAge,"0,6"); //$NON-NLS-1$
		jp.add(jtfNoveltiesAge,"1,6"); //$NON-NLS-1$
		jp.add(jlVisiblePlanned,"0,8"); //$NON-NLS-1$
		jp.add(jtfVisiblePlanned,"1,8"); //$NON-NLS-1$
		
		jpOptions.add(jcbDisplayUnmounted,"0,1"); //$NON-NLS-1$
		jpOptions.add(jcbRestart,"0,3"); //$NON-NLS-1$
		jpOptions.add(jcbSearchUnmounted,"0,5"); //$NON-NLS-1$
		jpOptions.add(jpCombos,"0,7"); //$NON-NLS-1$
		jpOptions.add(jp,"0,9"); //$NON-NLS-1$
				
		//--P2P
		jpP2P = new JPanel();
		jpP2P.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.71"))); //$NON-NLS-1$
		double sizeP2P[][] = {{0.6,0.3,0.1},
				{iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
		jpP2P.setLayout(new TableLayout(sizeP2P));
		jcbShare = new JCheckBox(Messages.getString("ParameterView.72")); //$NON-NLS-1$
		jcbShare.setEnabled(false); //TBI
		jcbShare.setToolTipText(Messages.getString("ParameterView.73")); //$NON-NLS-1$
		jlPasswd = new JLabel(Messages.getString("ParameterView.74")); //$NON-NLS-1$
		jlPasswd.setEnabled(false); //TBI
		jpfPasswd = new JPasswordField();
		jpfPasswd.setEnabled(false); //TBI
		jpfPasswd.setToolTipText(Messages.getString("ParameterView.75")); //$NON-NLS-1$
		jcbAddRemoteProperties = new JCheckBox(Messages.getString("ParameterView.76")); //$NON-NLS-1$
		jcbAddRemoteProperties.setEnabled(false); //TBI
		jcbAddRemoteProperties.setToolTipText(Messages.getString("ParameterView.77")); //$NON-NLS-1$
		jcbHideProperties = new JCheckBox(Messages.getString("ParameterView.78")); //$NON-NLS-1$
		jcbHideProperties.setToolTipText(Messages.getString("ParameterView.79")); //$NON-NLS-1$
		jcbHideProperties.setEnabled(false); //TBI
		jpP2P.add(jcbShare,"0,1"); //$NON-NLS-1$
		jpP2P.add(jlPasswd,"0,3"); //$NON-NLS-1$
		jpP2P.add(jpfPasswd,"1,3"); //$NON-NLS-1$
		jpP2P.add(jcbAddRemoteProperties,"0,5"); //$NON-NLS-1$
		jpP2P.add(jcbHideProperties,"0,7"); //$NON-NLS-1$
		
		//--Tags
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
		
		//--Advanced
		jpAdvanced = new JPanel();
		jpAdvanced.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.115")));  //$NON-NLS-1$
		double sizeAdvanced[][] = {{0.5,0.45},
				{iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
		jpAdvanced.setLayout(new TableLayout(sizeAdvanced));
		jcbBackup = new JCheckBox(Messages.getString("ParameterView.116"));  //$NON-NLS-1$
		jcbBackup.addActionListener(this);
		jcbBackup.setToolTipText(Messages.getString("ParameterView.117")); //$NON-NLS-1$
		jlBackupSize = new JLabel(Messages.getString("ParameterView.118")); //$NON-NLS-1$
		jlBackupSize.setToolTipText(Messages.getString("ParameterView.119")); //$NON-NLS-1$
		jtfBackupSize = new JTextField(4);
		jtfBackupSize.setToolTipText(Messages.getString("ParameterView.119")); //$NON-NLS-1$
		jlCollectionEncoding = new JLabel(Messages.getString("ParameterView.120")); //$NON-NLS-1$
		jlCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121")); //$NON-NLS-1$
		jcbCollectionEncoding = new JComboBox();
		jcbCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121")); //$NON-NLS-1$
		jcbRegexp = new JCheckBox(Messages.getString("ParameterView.113")); //$NON-NLS-1$
		jcbRegexp.setSelected(ConfigurationManager.getBoolean(CONF_REGEXP));//$NON-NLS-1$
		jcbRegexp.setToolTipText(Messages.getString("ParameterView.114")); //$NON-NLS-1$
		jcbCollectionEncoding.addItem("UTF-8"); //$NON-NLS-1$
		jcbCollectionEncoding.addItem("UTF-16"); //$NON-NLS-1$
		jpAdvanced.add(jcbBackup,"0,1");//$NON-NLS-1$
		jpAdvanced.add(jcbRegexp,"0,3");//$NON-NLS-1$
		jpAdvanced.add(jlBackupSize,"0,5");//$NON-NLS-1$
		jpAdvanced.add(jtfBackupSize,"1,5");//$NON-NLS-1$
		jpAdvanced.add(jlCollectionEncoding,"0,7");//$NON-NLS-1$
		jpAdvanced.add(jcbCollectionEncoding,"1,7");//$NON-NLS-1$
		
		//- Network
		jpNetwork = new JPanel();
		jpNetwork.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.139")));  //$NON-NLS-1$
		double sizeNetwork[][] = {{0.5,0.45},
				{iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
		jpNetwork.setLayout(new TableLayout(sizeNetwork));
		jcbProxy = new JCheckBox(Messages.getString("ParameterView.140"));  //$NON-NLS-1$
		jcbProxy.setToolTipText(Messages.getString("ParameterView.141"));  //$NON-NLS-1$
		jcbProxy.addActionListener(this);
		jlProxyHostname = new JLabel(Messages.getString("ParameterView.144"));  //$NON-NLS-1$
		jlProxyHostname.setToolTipText(Messages.getString("ParameterView.145"));  //$NON-NLS-1$
		jtfProxyHostname = new JTextField();
		jtfProxyHostname.setToolTipText(Messages.getString("ParameterView.145"));  //$NON-NLS-1$
		jlProxyPort = new JLabel(Messages.getString("ParameterView.146"));  //$NON-NLS-1$
		jlProxyPort.setToolTipText(Messages.getString("ParameterView.147"));  //$NON-NLS-1$
		jtfProxyPort = new JTextField();
		jtfProxyPort.setToolTipText(Messages.getString("ParameterView.147"));  //$NON-NLS-1$
		jtfProxyPort.setInputVerifier(new InputVerifier(){
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try{
					int iValue = Integer.parseInt(sText);
					if (iValue < 0 || iValue>65535){ //port is between 0 and 65535 
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
		jlProxyLogin = new JLabel(Messages.getString("ParameterView.142"));  //$NON-NLS-1$
		jlProxyLogin.setToolTipText(Messages.getString("ParameterView.143"));  //$NON-NLS-1$
		jtfProxyLogin = new JTextField();
		jtfProxyLogin.setToolTipText(Messages.getString("ParameterView.143"));  //$NON-NLS-1$
		InputVerifier verifier = new InputVerifier(){ //verifier for TO
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try{
					int iValue = Integer.parseInt(sText);
					if (iValue <= 0 ){ //time out must be > 0 
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
		};
		jlConnectionTO = new JLabel(Messages.getString("ParameterView.160"));  //$NON-NLS-1$
		jlConnectionTO.setToolTipText(Messages.getString("ParameterView.160"));  //$NON-NLS-1$
		jtfConnectionTO = new JTextField();
		jtfConnectionTO.setToolTipText(Messages.getString("ParameterView.161"));  //$NON-NLS-1$)
		jtfConnectionTO.addActionListener(this);
		jtfConnectionTO.setInputVerifier(verifier);
		jlTransfertTO = new JLabel(Messages.getString("ParameterView.162"));  //$NON-NLS-1$
		jlTransfertTO.setToolTipText(Messages.getString("ParameterView.163"));  //$NON-NLS-1$
		jtfTransfertTO = new JTextField();
		jtfTransfertTO.setToolTipText(Messages.getString("ParameterView.163"));  //$NON-NLS-1$)
		jtfTransfertTO.addActionListener(this);
		jtfTransfertTO.setInputVerifier(verifier);
		jpNetwork.add(jcbProxy,"0,1"); //$NON-NLS-1$
		jpNetwork.add(jlProxyHostname,"0,3"); //$NON-NLS-1$
		jpNetwork.add(jtfProxyHostname,"1,3"); //$NON-NLS-1$
		jpNetwork.add(jlProxyPort,"0,5"); //$NON-NLS-1$
		jpNetwork.add(jtfProxyPort,"1,5"); //$NON-NLS-1$
		jpNetwork.add(jlProxyLogin,"0,7"); //$NON-NLS-1$
		jpNetwork.add(jtfProxyLogin,"1,7"); //$NON-NLS-1$
		jpNetwork.add(jlConnectionTO,"0,9"); //$NON-NLS-1$
		jpNetwork.add(jtfConnectionTO,"1,9"); //$NON-NLS-1$
		jpNetwork.add(jlTransfertTO,"0,11"); //$NON-NLS-1$
		jpNetwork.add(jtfTransfertTO,"1,11"); //$NON-NLS-1$
				
		//- Cover
		jpCovers = new JPanel();
		jpCovers.setBorder(BorderFactory.createTitledBorder(Messages.getString("ParameterView.159")));  //$NON-NLS-1$
		double sizeCover[][] = {{0.5,0.45},
				{iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
		jpCovers.setLayout(new TableLayout(sizeCover));
		jcbAutoCover = new JCheckBox(Messages.getString("ParameterView.148")); //$NON-NLS-1$
		jcbAutoCover.setToolTipText(Messages.getString("ParameterView.149")); //$NON-NLS-1$
		jcbAutoCover.addActionListener(this);
		jcbShuffleCover = new JCheckBox(Messages.getString("ParameterView.166")); //$NON-NLS-1$
		jcbShuffleCover.setToolTipText(Messages.getString("ParameterView.167")); //$NON-NLS-1$
		jcbShuffleCover.addActionListener(this);
		jcbPreLoad = new JCheckBox(Messages.getString("ParameterView.169")); //$NON-NLS-1$
		jcbPreLoad.setToolTipText(Messages.getString("ParameterView.170")); //$NON-NLS-1$
		jcbResize = new JCheckBox(Messages.getString("ParameterView.173")); //$NON-NLS-1$
		jcbResize.setToolTipText(Messages.getString("ParameterView.174")); //$NON-NLS-1$
		jcbLoadEachTrack = new JCheckBox(Messages.getString("ParameterView.175")); //$NON-NLS-1$
		jcbLoadEachTrack.setToolTipText(Messages.getString("ParameterView.176")); //$NON-NLS-1$
		InputVerifier iverifier = new InputVerifier(){
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try{
					int iValue = Integer.parseInt(sText);
					if (iValue < 1 ){ //size should be > 0 
						jbOK.setEnabled(false);
						return false;
					}
					if (iValue > MAX_COVER_SIZE ){ //size should be > 0 
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
		};
		jlMinSize = new JLabel(Messages.getString("ParameterView.150")); //$NON-NLS-1$
		jlMinSize.setToolTipText(Messages.getString("ParameterView.151")); //$NON-NLS-1$
		jtfMinSize = new JTextField();
		jtfMinSize.setInputVerifier(iverifier);
		jtfMinSize.setToolTipText(Messages.getString("ParameterView.151")); //$NON-NLS-1$
		jlMaxSize = new JLabel(Messages.getString("ParameterView.152")); //$NON-NLS-1$
		jlMaxSize.setToolTipText(Messages.getString("ParameterView.153")); //$NON-NLS-1$
		jtfMaxSize = new JTextField();
		jtfMaxSize.setToolTipText(Messages.getString("ParameterView.153")); //$NON-NLS-1$
		jtfMaxSize.setInputVerifier(iverifier);
		jpCovers.add(jcbShuffleCover,"0,1"); //$NON-NLS-1$
		jpCovers.add(jcbLoadEachTrack,"1,1"); //$NON-NLS-1$
		jpCovers.add(jcbAutoCover,"0,3"); //$NON-NLS-1$
		jpCovers.add(jcbPreLoad,"0,5"); //$NON-NLS-1$
		jpCovers.add(jcbResize,"0,7"); //$NON-NLS-1$
		jpCovers.add(jlMinSize,"0,9"); //$NON-NLS-1$
		jpCovers.add(jtfMinSize,"1,9"); //$NON-NLS-1$
		jpCovers.add(jlMaxSize,"0,11"); //$NON-NLS-1$
		jpCovers.add(jtfMaxSize,"1,11"); //$NON-NLS-1$
				
		//--OK/cancel panel
		jpOKCancel = new JPanel();
		jpOKCancel.setLayout(new FlowLayout());
		jbOK = new JButton(Messages.getString("ParameterView.85")); //$NON-NLS-1$
		jbOK.addActionListener(this);
		jpOKCancel.add(jbOK);
		jbDefault = new JButton(Messages.getString("ParameterView.86")); //$NON-NLS-1$
		jbDefault.addActionListener(this);
		jpOKCancel.add(jbDefault);
		
		//--Global layout
		double size[][] = {{0.99},
				{0.9,0.10}};
		setLayout(new TableLayout(size));
		//add main panels
		jtpMain = new JTabbedPane();
		jtpMain.addTab(Messages.getString("ParameterView.33"),jpOptions); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.19"),jpStart); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.98"),jpTags); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.8"),jpHistory); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.71"),jpP2P); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.159"),jpCovers); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.26"),jpConfirmations); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.139"),jpNetwork); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.115"),jpAdvanced); //$NON-NLS-1$
		jtpMain.setSelectedIndex(ConfigurationManager.getInt(CONF_OPTIONS_TAB));  //Reload stored selected index
		jtpMain.addChangeListener(this);
		add(jtpMain,"0,0"); //$NON-NLS-1$
		add(jpOKCancel,"0,1"); //$NON-NLS-1$
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
		return "ParameterView.87"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		new Thread(){
			public void run(){
				if (e.getSource() == jbClearHistory){
					History.getInstance().clear();
					CommandJPanel.getInstance().clearHistoryBar();
				}
				else if (e.getSource() == jcbShuffleCover){
					jcbLoadEachTrack.setEnabled(jcbShuffleCover.isSelected());
				}
				else if (e.getSource() == jbOK){
					//**Read all parameters**
					//Options
					boolean bHiddenState = jcbDisplayUnmounted.isSelected(); 
					if ( bHiddenState != bHidden){ //check if this option changed to launch a refresh if needed
						bHidden = bHiddenState;
						ObservationManager.notify(EVENT_DEVICE_REFRESH);
					}
					ConfigurationManager.setProperty(CONF_OPTIONS_HIDE_UNMOUNTED,Boolean.toString(bHiddenState));
					ConfigurationManager.setProperty(CONF_OPTIONS_RESTART,Boolean.toString(jcbRestart.isSelected()));
					ConfigurationManager.setProperty(CONF_OPTIONS_SEARCH_UNMOUNTED,Boolean.toString(jcbSearchUnmounted.isSelected()));
					String sLocal = (String)Messages.getInstance().getLocals().get(jcbLanguage.getSelectedIndex());
					if (!Messages.getInstance().getLocal().equals(sLocal)){  //local has changed
						Messages.showInfoMessage(Messages.getString("ParameterView.103")); //$NON-NLS-1$
					}
					ConfigurationManager.setProperty(CONF_OPTIONS_LANGUAGE,sLocal);
					ConfigurationManager.setProperty(CONF_OPTIONS_LNF,(String)jcbLAF.getSelectedItem());
					if (!LNFManager.getCurrent().equals((String)jcbLAF.getSelectedItem())){  //Lnf has changed
						SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                Main.getWindow().setShown(false);
        						LNFManager.setLookAndFeel(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
        						PerspectiveBarJPanel.getInstance().setActivated(PerspectiveManager. getCurrentPerspective());  //force the perspective panel to refresh
        						SwingUtilities.updateComponentTreeUI(Main.getWindow());  //repaint all the application with the right LAF
        						Main.getWindow().setShown(true);
        					}
                        });
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
					String sBestofSize = jtfBestofSize.getText();
					if (!sBestofSize.equals("")){ //$NON-NLS-1$
						ConfigurationManager.setProperty(CONF_BESTOF_SIZE,sBestofSize);
					}
					FileManager.setRateHasChanged(true); //force refresh of bestof files
					String sNoveltiesAge = jtfNoveltiesAge.getText();
					if (!sNoveltiesAge.equals("")){ //$NON-NLS-1$
						ConfigurationManager.setProperty(CONF_OPTIONS_NOVELTIES_AGE,sNoveltiesAge);
					}
					String sVisiblePlanned = jtfVisiblePlanned.getText();
					if (!sVisiblePlanned.equals("")){ //$NON-NLS-1$
						ConfigurationManager.setProperty(CONF_OPTIONS_VISIBLE_PLANNED,sVisiblePlanned);
					}
					//startup
					if (jrbNothing.isSelected()){
						ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_NOTHING);
					}
					else if (jrbLast.isSelected()){
						ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_LAST);
					}
					else if (jrbLastKeepPos.isSelected()){
						ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_LAST_KEEP_POS);
					}
					else if (jrbShuffle.isSelected()){
						ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_SHUFFLE);
					}
					else if (jrbFile.isSelected()){
						ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_FILE);
					}
					else if (jrbBestof.isSelected()){
						ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_BESTOF);
					}
					else if (jrbNovelties.isSelected()){
						ConfigurationManager.setProperty(CONF_STARTUP_MODE,STARTUP_MODE_NOVELTIES);
					}
					//Confirmations
					ConfigurationManager.setProperty(CONF_CONFIRMATIONS_DELETE_FILE,Boolean.toString(jcbBeforeDelete.isSelected()));
					ConfigurationManager.setProperty(CONF_CONFIRMATIONS_EXIT,Boolean.toString(jcbBeforeExit.isSelected()));
					ConfigurationManager.setProperty(CONF_CONFIRMATIONS_REMOVE_DEVICE,Boolean.toString(jcbBeforeRemoveDevice.isSelected()));
					ConfigurationManager.setProperty(CONF_CONFIRMATIONS_DELETE_COVER,Boolean.toString(jcbBeforeDeleteCover.isSelected()));
					//History
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
					ConfigurationManager.setProperty(CONF_REGEXP,Boolean.toString(jcbRegexp.isSelected()));
					//Advanced
					ConfigurationManager.setProperty(CONF_BACKUP_SIZE,jtfBackupSize.getText());
					ConfigurationManager.setProperty(CONF_COLLECTION_CHARSET,jcbCollectionEncoding.getSelectedItem().toString());
					InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.109"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
					ConfigurationManager.commit();
					//Network
					ConfigurationManager.setProperty(CONF_NETWORK_USE_PROXY,Boolean.toString(jcbProxy.isSelected()));
					ConfigurationManager.setProperty(CONF_NETWORK_PROXY_HOSTNAME,jtfProxyHostname.getText());
					ConfigurationManager.setProperty(CONF_NETWORK_PROXY_PORT,jtfProxyPort.getText());
					ConfigurationManager.setProperty(CONF_NETWORK_PROXY_LOGIN,jtfProxyLogin.getText());
					ConfigurationManager.setProperty(CONF_NETWORK_CONNECTION_TO,jtfConnectionTO.getText());
					ConfigurationManager.setProperty(CONF_NETWORK_TRANSFERT_TO,jtfTransfertTO.getText());
					//Covers
					ConfigurationManager.setProperty(CONF_COVERS_AUTO_COVER,Boolean.toString(jcbAutoCover.isSelected()));
					ConfigurationManager.setProperty(CONF_COVERS_SHUFFLE,Boolean.toString(jcbShuffleCover.isSelected()));
					ConfigurationManager.setProperty(CONF_COVERS_PRELOAD,Boolean.toString(jcbPreLoad.isSelected()));
					ConfigurationManager.setProperty(CONF_COVERS_RESIZE,Boolean.toString(jcbResize.isSelected()));
					ConfigurationManager.setProperty(CONF_COVERS_CHANGE_AT_EACH_TRACK,Boolean.toString(jcbLoadEachTrack.isSelected()));
					ConfigurationManager.setProperty(CONF_COVERS_MIN_SIZE,jtfMinSize.getText());
					ConfigurationManager.setProperty(CONF_COVERS_MAX_SIZE,jtfMaxSize.getText());
				}
				else if (e.getSource() == jbDefault){
					ConfigurationManager.setDefaultProperties();
					ConfigurationManager.setProperty(CONF_FIRST_CON,FALSE);
					updateSelection();
					InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.110"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
					ConfigurationManager.commit();
				}
				else if (e.getSource() == jcbBackup){ //if backup option is unchecked, reset backup size
				    if ( jcbBackup.isSelected()){
				        jtfBackupSize.setEnabled(true);
				        jtfBackupSize.setText(ConfigurationManager.getProperty(CONF_BACKUP_SIZE));
				    }
				    else{
				        jtfBackupSize.setEnabled(false);
				        jtfBackupSize.setText("0"); //$NON-NLS-1$
				    }
				}
				else if (e.getSource() == jcbProxy){
				    if ( jcbProxy.isSelected()){
				        jtfProxyHostname.setEnabled(true);
				        jtfProxyPort.setEnabled(true);
				        jtfProxyLogin.setEnabled(true);
				    }
				    else{
				        jtfProxyHostname.setEnabled(false);
				        jtfProxyPort.setEnabled(false);
				        jtfProxyLogin.setEnabled(false);
				    }
				}
				else if (e.getSource() == jcbAutoCover){
				    if ( jcbAutoCover.isSelected()){
				        jtfMinSize.setEnabled(true);
				        jtfMaxSize.setEnabled(true);
				        jcbResize.setEnabled(true);
				        jcbPreLoad.setEnabled(true);
				    }
				    else{
				        jtfMinSize.setEnabled(false);
				        jtfMaxSize.setEnabled(false);
				        jcbResize.setEnabled(false);
				        jcbPreLoad.setEnabled(false);
				    }
				}
			}
		}.start();
	}
	
	/**
	 * Set widgets to specified value in options
	 */
	private void updateSelection(){
		jtfHistory.setText(ConfigurationManager.getProperty(CONF_HISTORY));
		if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_FILE)){
			jrbFile.setSelected(true);
			sbSearch.setEnabled(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST)){
			jrbLast.setSelected(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST_KEEP_POS)){
			jrbLastKeepPos.setSelected(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOTHING)){
			jrbNothing.setSelected(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_SHUFFLE)){
			jrbShuffle.setSelected(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_BESTOF)){
			jrbBestof.setSelected(true);
		}
		else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOVELTIES)){
			jrbNovelties.setSelected(true);
		}
		//Confirmations
		jcbBeforeDelete.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE));
		jcbBeforeExit.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_EXIT));
		jcbBeforeRemoveDevice.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_REMOVE_DEVICE));
		jcbBeforeDeleteCover.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_COVER));
		//options
		boolean bHidden = ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED);
		jcbDisplayUnmounted.setSelected(bHidden);
		this.bHidden = bHidden; 
		jcbRestart.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_RESTART));
		jcbSearchUnmounted.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_SEARCH_UNMOUNTED));
		jcbLanguage.setSelectedIndex(Messages.getInstance().getLocals().indexOf(ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE)));
		jcbLAF.setSelectedItem(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
		jcbLogLevel.setSelectedIndex(Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_LOG_LEVEL)));
		jtfIntroLength.setText(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_LENGTH));
		jtfIntroPosition.setText(ConfigurationManager.getProperty(CONF_OPTIONS_INTRO_BEGIN));
		jtfBestofSize.setText(ConfigurationManager.getProperty(CONF_BESTOF_SIZE));
		jtfNoveltiesAge.setText(ConfigurationManager.getProperty(CONF_OPTIONS_NOVELTIES_AGE));
		jtfVisiblePlanned.setText(ConfigurationManager.getProperty(CONF_OPTIONS_VISIBLE_PLANNED));
		jcbShare.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_P2P_SHARE));
		jpfPasswd.setText(ConfigurationManager.getProperty(CONF_OPTIONS_P2P_PASSWORD));
		jcbAddRemoteProperties.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_P2P_ADD_REMOTE_PROPERTIES));
		bHidden = ConfigurationManager.getBoolean(CONF_OPTIONS_P2P_HIDE_LOCAL_PROPERTIES);
		jcbHideProperties.setSelected(bHidden);
		jcbDeepScan.setSelected(ConfigurationManager.getBoolean(CONF_TAGS_DEEP_SCAN));
		jcbUseParentDir.setSelected(ConfigurationManager.getBoolean(CONF_TAGS_USE_PARENT_DIR));
		//advanced
		int iBackupSize = ConfigurationManager.getInt(CONF_BACKUP_SIZE);
		if (iBackupSize<=0){ //backup size =0 means no backup
		    jcbBackup.setSelected(false);   
		    jtfBackupSize.setEnabled(false);
		}
		else{
		    jcbBackup.setSelected(true);
		    jtfBackupSize.setEnabled(true);
		}
		jtfBackupSize.setText(Integer.toString(iBackupSize));
		jcbCollectionEncoding.setSelectedItem(ConfigurationManager.getProperty(CONF_COLLECTION_CHARSET)); 
		//network
		boolean bUseProxy = ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY);
		jcbProxy.setSelected(bUseProxy);
		jtfProxyHostname.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME));
		jtfProxyHostname.setEnabled(bUseProxy);
		jtfProxyPort.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_PORT));
		jtfProxyPort.setEnabled(bUseProxy);
		jtfProxyLogin.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN));
		jtfProxyLogin.setEnabled(bUseProxy);
		jtfConnectionTO.setText(ConfigurationManager.getProperty(CONF_NETWORK_CONNECTION_TO));
		jtfTransfertTO.setText(ConfigurationManager.getProperty(CONF_NETWORK_TRANSFERT_TO));
		//Covers
		jcbAutoCover.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jtfMinSize.setText(ConfigurationManager.getProperty(CONF_COVERS_MIN_SIZE));
		jtfMinSize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jtfMaxSize.setText(ConfigurationManager.getProperty(CONF_COVERS_MAX_SIZE));
		jtfMaxSize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jcbShuffleCover.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE));
		jcbPreLoad.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_PRELOAD));
		jcbPreLoad.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jcbResize.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_RESIZE));
		jcbResize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jcbLoadEachTrack.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_CHANGE_AT_EACH_TRACK));
		jcbLoadEachTrack.setEnabled(jcbShuffleCover.isSelected() && jcbShuffleCover.isEnabled()); //this mode requires shuffle mode
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getID()
	 */
	public String getID() {
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

       /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
	public void itemStateChanged(ItemEvent e) {
	    if (e.getSource() == jrbFile){  //jrbFile has been selected or deselected
	        sbSearch.setEnabled(jrbFile.isSelected());
	    }  
	}

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
       //when changing tab, store it for futur jajuk sessions
        ConfigurationManager.setProperty(CONF_OPTIONS_TAB,Integer.toString(jtpMain.getSelectedIndex()));
    }
	
}
