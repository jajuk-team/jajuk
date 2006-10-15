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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.Main;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.SearchResult;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.DefaultMouseWheelListener;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.LNFManager;
import org.jajuk.ui.PatternInputVerifier;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.SearchBox;
import org.jajuk.ui.SteppedComboBox;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
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
    
	private static final long serialVersionUID = 1L;

	/**Self instance*/
    private static ParameterView pv;
    
    JTabbedPane jtpMain;
    JPanel jpHistory;
    JLabel jlHistory;
    JTextField jtfHistory;
    JButton jbClearHistory;
    JButton jbResetRatings;
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
    JCheckBox jcbBeforeClearingHistory;
    JCheckBox jcbBeforeResetingRatings;
    JCheckBox jcbBeforeRefactorFiles;
    
    JPanel jpOptions;
    JCheckBox jcbDisplayUnmounted;
    JCheckBox jcbSyncTableTree;
    JLabel jlLanguage;
    SteppedComboBox scbLanguage;
    JLabel jlLAF;
    SteppedComboBox scbLAF;
    JLabel jlLogLevel;
    SteppedComboBox scbLogLevel;
    JLabel jlIntroPosition;
    JSlider introPosition;
    JLabel jlIntroLength;
    JSlider introLength;
    JLabel jlBestofSize;
    JTextField jtfBestofSize;
    JLabel jlNoveltiesAge;
    JTextField jtfNoveltiesAge;
    JLabel jlVisiblePlanned;
    JTextField jtfVisiblePlanned;
    JLabel jlCrossFadeDuration;
    JSlider crossFadeDuration;
    JCheckBox jcbDefaultActionClick;
    JCheckBox jcbDefaultActionDrop;
    JCheckBox jcbShowPopup;
    
    JPanel jpP2P;
    JCheckBox jcbShare;
    JLabel jlPasswd;
    JPasswordField jpfPasswd;
    JCheckBox jcbAddRemoteProperties;
    JCheckBox jcbHideProperties;
    
    JPanel jpTags;
    JCheckBox jcbUseParentDir;    
    JLabel jlRefactorPattern;
    JTextField jtfRefactorPattern;
    JLabel jlAnimationPattern;
    JTextField jtfAnimationPattern;
    
    JPanel jpAdvanced;
    JCheckBox jcbBackup;
    JLabel jlBackupSize;
    JSlider backupSize;
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
    JSlider connectionTO;
    JLabel jlTransfertTO;
    JSlider transfertTO;
    JPanel jpCovers;
    JCheckBox jcbAutoCover;
    JCheckBox jcbShuffleCover;
    JCheckBox jcbPreLoad;
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
    public void initUI(){
        int iXSeparator = 5;
        int iYSeparator = 5;
        
        //--History
        jpHistory = new JPanel();
        double sizeHistory[][] = {{iXSeparator,TableLayout.PREFERRED,iXSeparator,TableLayout.PREFERRED},
                {5*iYSeparator,20,10*iYSeparator,25,10*iYSeparator,25}};
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
        jbResetRatings = new JButton(Messages.getString("ParameterView.186"),Util.getIcon(ICON_CLEAR)); //$NON-NLS-1$
        jbResetRatings.setToolTipText(Messages.getString("ParameterView.187")); //$NON-NLS-1$
        jbResetRatings.addActionListener(this);
        jpHistory.add(jlHistory,"1,1"); //$NON-NLS-1$
        jpHistory.add(jtfHistory,"3,1"); //$NON-NLS-1$
        jpHistory.add(jbClearHistory,"3,3"); //$NON-NLS-1$
        jpHistory.add(jbResetRatings,"3,5"); //$NON-NLS-1$
        
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
            File file = FileManager.getInstance().getFileByID(sFileId);
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
        double sizeConfirmations[][] = {{0.99},
                {iYSeparator,20,iYSeparator,20,iYSeparator,20,
                iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
        jpConfirmations.setLayout(new TableLayout(sizeConfirmations));
        jcbBeforeDelete = new JCheckBox(Messages.getString("ParameterView.27")); //$NON-NLS-1$
        jcbBeforeDelete.setToolTipText(Messages.getString("ParameterView.28")); //$NON-NLS-1$
        jcbBeforeExit = new JCheckBox(Messages.getString("ParameterView.29")); //$NON-NLS-1$
        jcbBeforeExit.setToolTipText(Messages.getString("ParameterView.30")); //$NON-NLS-1$
        jcbBeforeRemoveDevice = new JCheckBox(Messages.getString("ParameterView.164")); //$NON-NLS-1$
        jcbBeforeRemoveDevice.setToolTipText(Messages.getString("ParameterView.165")); //$NON-NLS-1$
        jcbBeforeDeleteCover = new JCheckBox(Messages.getString("ParameterView.171")); //$NON-NLS-1$
        jcbBeforeDeleteCover.setToolTipText(Messages.getString("ParameterView.172")); //$NON-NLS-1$
        jcbBeforeClearingHistory = new JCheckBox(Messages.getString("ParameterView.188")); //$NON-NLS-1$
        jcbBeforeClearingHistory.setToolTipText(Messages.getString("ParameterView.188")); //$NON-NLS-1$
        jcbBeforeResetingRatings = new JCheckBox(Messages.getString("ParameterView.189")); //$NON-NLS-1$
        jcbBeforeResetingRatings.setToolTipText(Messages.getString("ParameterView.189")); //$NON-NLS-1$
        jcbBeforeRefactorFiles = new JCheckBox(Messages.getString("ParameterView.194")); //$NON-NLS-1$
        jcbBeforeRefactorFiles.setToolTipText(Messages.getString("ParameterView.194")); //$NON-NLS-1$
        
        jpConfirmations.add(jcbBeforeDelete,"0,1"); //$NON-NLS-1$
        jpConfirmations.add(jcbBeforeExit,"0,3"); //$NON-NLS-1$
        jpConfirmations.add(jcbBeforeRemoveDevice,"0,5"); //$NON-NLS-1$
        jpConfirmations.add(jcbBeforeDeleteCover,"0,7"); //$NON-NLS-1$
        jpConfirmations.add(jcbBeforeClearingHistory,"0,9"); //$NON-NLS-1$
        jpConfirmations.add(jcbBeforeResetingRatings,"0,11"); //$NON-NLS-1$
        jpConfirmations.add(jcbBeforeRefactorFiles,"0,13"); //$NON-NLS-1$
        
        //--Options
        jpOptions = new JPanel();
        jcbDisplayUnmounted = new JCheckBox(Messages.getString("ParameterView.34")); //$NON-NLS-1$
        jcbDisplayUnmounted.setToolTipText(Messages.getString("ParameterView.35")); //$NON-NLS-1$
        jcbSyncTableTree = new JCheckBox(Messages.getString("ParameterView.183")); //$NON-NLS-1$
        jcbSyncTableTree.setToolTipText(Messages.getString("ParameterView.184")); //$NON-NLS-1$
        jcbDefaultActionClick = new JCheckBox(Messages.getString("ParameterView.179")); //$NON-NLS-1$
        jcbDefaultActionClick.setToolTipText(Messages.getString("ParameterView.180")); //$NON-NLS-1$
        jcbDefaultActionDrop = new JCheckBox(Messages.getString("ParameterView.181")); //$NON-NLS-1$
        jcbDefaultActionDrop.setToolTipText(Messages.getString("ParameterView.182")); //$NON-NLS-1$
        jcbShowPopup = new JCheckBox(Messages.getString("ParameterView.185")); //$NON-NLS-1$
        jcbShowPopup.setToolTipText(Messages.getString("ParameterView.185")); //$NON-NLS-1$
        JPanel jpCombos = new JPanel();
        double sizeCombos[][] = {{0.50,0.45},
                {20,iYSeparator,20,iYSeparator,20}};
        jpCombos.setLayout(new TableLayout(sizeCombos));
        jlLanguage = new JLabel(Messages.getString("ParameterView.38")); //$NON-NLS-1$
        scbLanguage = new SteppedComboBox();
        Iterator itDescs = Messages.getInstance().getDescs().iterator();
        while (itDescs.hasNext()){
            String sDesc = (String)itDescs.next();
            scbLanguage.addItem(Messages.getString(sDesc));
        }
        scbLanguage.setToolTipText(Messages.getString("ParameterView.42")); //$NON-NLS-1$
        jlLAF = new JLabel(Messages.getString("ParameterView.43")); //$NON-NLS-1$
        jlLAF.setToolTipText(Messages.getString("ParameterView.44")); //$NON-NLS-1$
        scbLAF = new SteppedComboBox();
        Iterator it = LNFManager.getSupportedLNF().iterator();
        while (it.hasNext()){
            scbLAF.addItem(it.next());
        }
        scbLAF.setToolTipText(Messages.getString("ParameterView.45")); //$NON-NLS-1$
        jlLogLevel = new JLabel(Messages.getString("ParameterView.46")); //$NON-NLS-1$
        scbLogLevel = new SteppedComboBox();
        scbLogLevel.addItem(Messages.getString("ParameterView.47")); //$NON-NLS-1$
        scbLogLevel.addItem(Messages.getString("ParameterView.48")); //$NON-NLS-1$
        scbLogLevel.addItem(Messages.getString("ParameterView.49")); //$NON-NLS-1$
        scbLogLevel.addItem(Messages.getString("ParameterView.50")); //$NON-NLS-1$
        scbLogLevel.addItem(Messages.getString("ParameterView.51")); //$NON-NLS-1$
        scbLogLevel.setToolTipText(Messages.getString("ParameterView.52")); //$NON-NLS-1$
        jpCombos.add(jlLanguage,"0,0"); //$NON-NLS-1$
        jpCombos.add(scbLanguage,"1,0"); //$NON-NLS-1$
        jpCombos.add(jlLAF,"0,2"); //$NON-NLS-1$
        jpCombos.add(scbLAF,"1,2"); //$NON-NLS-1$
        jpCombos.add(jlLogLevel,"0,4"); //$NON-NLS-1$
        jpCombos.add(scbLogLevel,"1,4"); //$NON-NLS-1$
        //Intro
        JPanel jp = new JPanel();
        //intro position
        jlIntroPosition = new JLabel(Messages.getString("ParameterView.59")); //$NON-NLS-1$
        introPosition = new JSlider(0,100,0);
        introPosition.setMajorTickSpacing(10);
        introPosition.setMinorTickSpacing(10);
        introPosition.setPaintTicks(true);
        introPosition.setPaintLabels(true);
        introPosition.setToolTipText(Messages.getString("ParameterView.60") ); //$NON-NLS-1$
        introPosition.addMouseWheelListener(new DefaultMouseWheelListener(introPosition));
        
        //intro length
        jlIntroLength = new JLabel(Messages.getString("ParameterView.61")); //$NON-NLS-1$
        introLength = new JSlider(0,30,20);
        introLength.setMajorTickSpacing(10);
        introLength.setMinorTickSpacing(1);
        introLength.setPaintTicks(true);
        introLength.setPaintLabels(true);
        introLength.setToolTipText(Messages.getString("ParameterView.110") ); //$NON-NLS-1$
        introLength.addMouseWheelListener(new DefaultMouseWheelListener(introLength));
        
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
        jtfVisiblePlanned.setToolTipText(Messages.getString("ParameterView.178")); //$NON-NLS-1$
        jtfVisiblePlanned.setInputVerifier(new InputVerifier(){
            public boolean verify(JComponent input) {
                JTextField tf = (JTextField) input;
                String sText = tf.getText();
                try{
                    int iValue = Integer.parseInt(sText);
                    if (iValue < 0 || iValue > 100){ //number of planned tracks between 0 and 100 
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
        jlCrossFadeDuration = new JLabel(Messages.getString("ParameterView.190")); //$NON-NLS-1$
        jlCrossFadeDuration.setToolTipText(Messages.getString("ParameterView.191")); //$NON-NLS-1$
        crossFadeDuration = new JSlider(0,30,0);
        crossFadeDuration.setMajorTickSpacing(10);
        crossFadeDuration.setMinorTickSpacing(1);
        crossFadeDuration.setPaintTicks(true);
        crossFadeDuration.setPaintLabels(true);
        crossFadeDuration.setToolTipText(Messages.getString("ParameterView.191")); //$NON-NLS-1$
        crossFadeDuration.addMouseWheelListener(new DefaultMouseWheelListener(crossFadeDuration));
        
        //add panels
        double sizeIntro[][] = {{0.50,0.45},
                {TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator,
            TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator}};
        jp.setLayout(new TableLayout(sizeIntro));
        jp.add(jlIntroPosition,"0,0"); //$NON-NLS-1$
        jp.add(introPosition,"1,0"); //$NON-NLS-1$
        jp.add(jlIntroLength,"0,2"); //$NON-NLS-1$
        jp.add(introLength,"1,2"); //$NON-NLS-1$
        jp.add(jlCrossFadeDuration,"0,4"); //$NON-NLS-1$
        jp.add(crossFadeDuration,"1,4"); //$NON-NLS-1$
        jp.add(jlBestofSize,"0,6"); //$NON-NLS-1$
        jp.add(jtfBestofSize,"1,6"); //$NON-NLS-1$
        jp.add(jlNoveltiesAge,"0,8"); //$NON-NLS-1$
        jp.add(jtfNoveltiesAge,"1,8"); //$NON-NLS-1$
        jp.add(jlVisiblePlanned,"0,10"); //$NON-NLS-1$
        jp.add(jtfVisiblePlanned,"1,10"); //$NON-NLS-1$
        
        double sizeOptions[][] = {{0.99},
                {iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator}};
        jpOptions.setLayout(new TableLayout(sizeOptions));
        
        jpOptions.add(jcbDisplayUnmounted,"0,1"); //$NON-NLS-1$
        jpOptions.add(jcbDefaultActionClick,"0,3"); //$NON-NLS-1$
        jpOptions.add(jcbDefaultActionDrop,"0,5"); //$NON-NLS-1$
        jpOptions.add(jcbSyncTableTree,"0,7"); //$NON-NLS-1$
        jpOptions.add(jcbShowPopup,"0,9"); //$NON-NLS-1$
        jpOptions.add(jpCombos,"0,11"); //$NON-NLS-1$
        jpOptions.add(jp,"0,13"); //$NON-NLS-1$
        
        //--P2P
        jpP2P = new JPanel();
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
        double sizeTags[][] = {{0.5,0.45},
                {iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
        jpTags.setLayout(new TableLayout(sizeTags));
        jcbUseParentDir = new JCheckBox(Messages.getString("ParameterView.101"));  //$NON-NLS-1$
        jcbUseParentDir.setToolTipText(Messages.getString("ParameterView.102")); //$NON-NLS-1$
        jlRefactorPattern = new JLabel(Messages.getString("ParameterView.192")); //$NON-NLS-1$
        jlRefactorPattern.setToolTipText(Messages.getString("ParameterView.193")); //$NON-NLS-1$
        jtfRefactorPattern = new JTextField();
        jtfRefactorPattern.setToolTipText(Messages.getString("ParameterView.193")); //$NON-NLS-1$
        jtfRefactorPattern.setInputVerifier(new PatternInputVerifier());
        jlAnimationPattern = new JLabel(Messages.getString("ParameterView.195")); //$NON-NLS-1$
        jlAnimationPattern.setToolTipText(Messages.getString("ParameterView.193")); //$NON-NLS-1$
        jtfAnimationPattern = new JTextField();
        jtfAnimationPattern.setToolTipText(Messages.getString("ParameterView.193")); //$NON-NLS-1$
        jpTags.add(jcbUseParentDir,"0,1"); //$NON-NLS-1$
        jpTags.add(jlRefactorPattern,"0,3"); //$NON-NLS-1$
        jpTags.add(jtfRefactorPattern,"1,3"); //$NON-NLS-1$
        jpTags.add(jlAnimationPattern,"0,5"); //$NON-NLS-1$
        jpTags.add(jtfAnimationPattern,"1,5"); //$NON-NLS-1$
        
        //--Advanced
        jpAdvanced = new JPanel();
        jcbBackup = new JCheckBox(Messages.getString("ParameterView.116"));  //$NON-NLS-1$
        jcbBackup.addActionListener(this);
        jcbBackup.setToolTipText(Messages.getString("ParameterView.117")); //$NON-NLS-1$
        jlBackupSize = new JLabel(Messages.getString("ParameterView.118")); //$NON-NLS-1$
        jlBackupSize.setToolTipText(Messages.getString("ParameterView.119")); //$NON-NLS-1$
        backupSize = new JSlider(0,100);
        backupSize.setMajorTickSpacing(10);
        backupSize.setMinorTickSpacing(10);
        backupSize.setPaintTicks(true);
        backupSize.setPaintLabels(true);
        backupSize.setToolTipText(Messages.getString("ParameterView.119")); //$NON-NLS-1$
        backupSize.addMouseWheelListener(new DefaultMouseWheelListener(backupSize));
        jlCollectionEncoding = new JLabel(Messages.getString("ParameterView.120")); //$NON-NLS-1$
        jlCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121")); //$NON-NLS-1$
        jcbCollectionEncoding = new JComboBox();
        jcbCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121")); //$NON-NLS-1$
        jcbRegexp = new JCheckBox(Messages.getString("ParameterView.113")); //$NON-NLS-1$
        jcbRegexp.setSelected(ConfigurationManager.getBoolean(CONF_REGEXP));//$NON-NLS-1$
        jcbRegexp.setToolTipText(Messages.getString("ParameterView.114")); //$NON-NLS-1$
        jcbCollectionEncoding.addItem("UTF-8"); //$NON-NLS-1$
        jcbCollectionEncoding.addItem("UTF-16"); //$NON-NLS-1$
        
        double sizeAdvanced[][] = {{0.5,0.45},
                {iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator,
            TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator}};
        jpAdvanced.setLayout(new TableLayout(sizeAdvanced));
        jpAdvanced.add(jcbRegexp,"0,1");//$NON-NLS-1$
        jpAdvanced.add(jlCollectionEncoding,"0,3");//$NON-NLS-1$
        jpAdvanced.add(jcbCollectionEncoding,"1,3");//$NON-NLS-1$
        jpAdvanced.add(jcbBackup,"0,5");//$NON-NLS-1$
        jpAdvanced.add(jlBackupSize,"0,7");//$NON-NLS-1$
        jpAdvanced.add(backupSize,"1,7");//$NON-NLS-1$        
        
        //- Network
        jpNetwork = new JPanel();
        double sizeNetwork[][] = {{0.5,0.45},
                {iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED
            ,iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator,TableLayout.PREFERRED,iYSeparator}};
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
        jtfProxyLogin.setInputVerifier(verifier);
        jlConnectionTO = new JLabel(Messages.getString("ParameterView.160"));  //$NON-NLS-1$
        jlConnectionTO.setToolTipText(Messages.getString("ParameterView.160"));  //$NON-NLS-1$
        connectionTO = new JSlider(0,60);
        connectionTO.setMajorTickSpacing(10);
        connectionTO.setMinorTickSpacing(5);
        connectionTO.setPaintTicks(true);
        connectionTO.setPaintLabels(true);
        connectionTO.setToolTipText(Messages.getString("ParameterView.161"));  //$NON-NLS-1$)
        connectionTO.addMouseWheelListener(new DefaultMouseWheelListener(connectionTO));
        jlTransfertTO = new JLabel(Messages.getString("ParameterView.162"));  //$NON-NLS-1$
        jlTransfertTO.setToolTipText(Messages.getString("ParameterView.163"));  //$NON-NLS-1$
        transfertTO = new JSlider(0,60);
        transfertTO.setMajorTickSpacing(10);
        transfertTO.setMinorTickSpacing(5);
        transfertTO.setPaintTicks(true);
        transfertTO.setPaintLabels(true);
        transfertTO.setToolTipText(Messages.getString("ParameterView.163"));  //$NON-NLS-1$)
        transfertTO.addMouseWheelListener(new DefaultMouseWheelListener(transfertTO));
        jpNetwork.add(jlConnectionTO,"0,1"); //$NON-NLS-1$
        jpNetwork.add(connectionTO,"1,1"); //$NON-NLS-1$
        jpNetwork.add(jlTransfertTO,"0,3"); //$NON-NLS-1$
        jpNetwork.add(transfertTO,"1,3"); //$NON-NLS-1$
        jpNetwork.add(jcbProxy,"0,5"); //$NON-NLS-1$
        jpNetwork.add(jlProxyHostname,"0,7"); //$NON-NLS-1$
        jpNetwork.add(jtfProxyHostname,"1,7"); //$NON-NLS-1$
        jpNetwork.add(jlProxyPort,"0,9"); //$NON-NLS-1$
        jpNetwork.add(jtfProxyPort,"1,9"); //$NON-NLS-1$
        jpNetwork.add(jlProxyLogin,"0,11"); //$NON-NLS-1$
        jpNetwork.add(jtfProxyLogin,"1,11"); //$NON-NLS-1$
        
        
        //- Cover
        jpCovers = new JPanel();
        double sizeCover[][] = {{0.5,0.45},
                {iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator,20,iYSeparator}};
        jpCovers.setLayout(new TableLayout(sizeCover));
        jcbAutoCover = new JCheckBox(Messages.getString("ParameterView.148")); //$NON-NLS-1$
        jcbAutoCover.setToolTipText(Messages.getString("ParameterView.149")); //$NON-NLS-1$
        jcbAutoCover.addActionListener(this);
        jcbShuffleCover = new JCheckBox(Messages.getString("ParameterView.166")); //$NON-NLS-1$
        jcbShuffleCover.setToolTipText(Messages.getString("ParameterView.167")); //$NON-NLS-1$
        jcbShuffleCover.addActionListener(this);
        jcbPreLoad = new JCheckBox(Messages.getString("ParameterView.169")); //$NON-NLS-1$
        jcbPreLoad.setToolTipText(Messages.getString("ParameterView.170")); //$NON-NLS-1$
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
        jpCovers.add(jlMinSize,"0,7"); //$NON-NLS-1$
        jpCovers.add(jtfMinSize,"1,7"); //$NON-NLS-1$
        jpCovers.add(jlMaxSize,"0,9"); //$NON-NLS-1$
        jpCovers.add(jtfMaxSize,"1,9"); //$NON-NLS-1$
        
        //--OK/cancel panel
        Dimension dim = new Dimension(200,20);
        jpOKCancel = new JPanel();
        jpOKCancel.setLayout(new FlowLayout());
        jbOK = new JButton(Messages.getString("ParameterView.85"),Util.getIcon(ICON_OK)); //$NON-NLS-1$
        jbOK.setPreferredSize(dim);
        jbOK.addActionListener(this);
        jpOKCancel.add(jbOK);
        jbDefault = new JButton(Messages.getString("ParameterView.86"),Util.getIcon(ICON_DEFAULTS_BIG)); //$NON-NLS-1$
        jbDefault.setPreferredSize(dim);
        jbDefault.addActionListener(this);
        jpOKCancel.add(jbDefault);
        
        //--Global layout
        double size[][] = {{0.99},
                {0.9,0.10}};
        setLayout(new TableLayout(size));
        //add main panels
        jtpMain = new JTabbedPane();
        jtpMain.addTab(Messages.getString("ParameterView.33"),new JScrollPane(jpOptions)); //$NON-NLS-1$
        jtpMain.addTab(Messages.getString("ParameterView.19"),jpStart); //$NON-NLS-1$
        jtpMain.addTab(Messages.getString("ParameterView.98"),jpTags); //$NON-NLS-1$
        jtpMain.addTab(Messages.getString("ParameterView.8"),jpHistory); //$NON-NLS-1$
        //TBI jtpMain.addTab(Messages.getString("ParameterView.71"),jpP2P); //$NON-NLS-1$
        jtpMain.addTab(Messages.getString("ParameterView.159"),jpCovers); //$NON-NLS-1$
        jtpMain.addTab(Messages.getString("ParameterView.26"),jpConfirmations); //$NON-NLS-1$
        jtpMain.addTab(Messages.getString("ParameterView.139"),jpNetwork); //$NON-NLS-1$
        jtpMain.addTab(Messages.getString("ParameterView.115"),jpAdvanced); //$NON-NLS-1$
        try{
            jtpMain.setSelectedIndex(ConfigurationManager.getInt(CONF_OPTIONS_TAB));  //Reload stored selected index
        }
        catch(Exception e){ //an error can occur if a new release brings or remove tabs
            Log.error(e);
            jtpMain.setSelectedIndex(0);
        }
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
                    //show confirmation message if required
                    if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_CLEAR_HISTORY)){
                        int iResu = Messages.getChoice(Messages.getString("Confirmation_clear_history"),JOptionPane.WARNING_MESSAGE);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if (iResu != JOptionPane.YES_OPTION){
                            return;
                        }
                    }
                    ObservationManager.notify(new Event(EventSubject.EVENT_CLEAR_HISTORY));
                }
                else if (e.getSource() == jbResetRatings){
                    //show confirmation message if required
                    if ( ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_RESET_RATINGS)){
                        int iResu = Messages.getChoice(Messages.getString("Confirmation_reset_ratings"),JOptionPane.WARNING_MESSAGE);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        if (iResu != JOptionPane.YES_OPTION){
                            return;
                        }
                    }
                    if (!DeviceManager.getInstance().isAnyDeviceRefreshing()){ //make sure none device is refreshing
                        synchronized(TrackManager.getInstance().getLock()){
                            Iterator it  = TrackManager.getInstance().getTracks().iterator();
                            while (it.hasNext()){
                                Track track = (Track)it.next();
                                track.setRate(0);
                            }
                        }
                        ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
                    }
                    else{
                        Messages.showErrorMessage("120"); //$NON-NLS-1$
                    }
                }
                else if (e.getSource() == jcbShuffleCover){
                    jcbLoadEachTrack.setEnabled(jcbShuffleCover.isSelected());
                }
                else if (e.getSource() == jbOK){
                    applyParameters();
                }
                else if (e.getSource() == jbDefault){
                    ConfigurationManager.setDefaultProperties();
                    ConfigurationManager.setProperty(CONF_FIRST_CON,FALSE);//not first connection
                    updateSelection();//update UI
                    InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.110"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
                    applyParameters();
                }
                else if (e.getSource() == jcbBackup){ //if backup option is unchecked, reset backup size
                    if ( jcbBackup.isSelected()){
                        backupSize.setEnabled(true);
                        jlBackupSize.setEnabled(true);
                        backupSize.setValue(ConfigurationManager.getInt(CONF_BACKUP_SIZE));
                    }
                    else{
                        backupSize.setEnabled(false);
                        jlBackupSize.setEnabled(false);
                        backupSize.setValue(0); //$NON-NLS-1$
                    }
                }
                else if (e.getSource() == jcbProxy){
                    if ( jcbProxy.isSelected()){
                        jtfProxyHostname.setEnabled(true);
                        jtfProxyPort.setEnabled(true);
                        jtfProxyLogin.setEnabled(true);
                        jlProxyHostname.setEnabled(true);
                        jlProxyPort.setEnabled(true);
                        jlProxyLogin.setEnabled(true);
                    }
                    else{
                        jtfProxyHostname.setEnabled(false);
                        jtfProxyPort.setEnabled(false);
                        jtfProxyLogin.setEnabled(false);
                        jlProxyHostname.setEnabled(false);
                        jlProxyPort.setEnabled(false);
                        jlProxyLogin.setEnabled(false);
                    }
                }
                else if (e.getSource() == jcbAutoCover){
                    if ( jcbAutoCover.isSelected()){
                        jtfMinSize.setEnabled(true);
                        jlMinSize.setEnabled(true);
                        jtfMaxSize.setEnabled(true);
                        jlMaxSize.setEnabled(true);
                        jcbPreLoad.setEnabled(true);
                    }
                    else{
                        jtfMinSize.setEnabled(false);
                        jlMinSize.setEnabled(false);
                        jtfMaxSize.setEnabled(false);
                        jlMaxSize.setEnabled(false);
                        jcbPreLoad.setEnabled(false);
                    }
                }
                else if (e.getSource() == scbLAF){
                    ConfigurationManager.setProperty(CONF_OPTIONS_LNF,(String)scbLAF.getSelectedItem());
                    if (!LNFManager.getCurrent().equals(scbLAF.getSelectedItem())){  //Lnf has changed
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                LNFManager.setLookAndFeel(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
                                SwingUtilities.updateComponentTreeUI(Main.getWindow());
                                PerspectiveBarJPanel.getInstance().setActivated(PerspectiveManager. getCurrentPerspective());  //force the perspective panel to refresh
                            }
                        });
                    }
                }
                else if (e.getSource() == scbLanguage){
                    String sLocal = Messages.getInstance().getLocals().get(scbLanguage.getSelectedIndex());
                    String sPreviousLocal = Messages.getInstance().getLocal(); 
                    if (!sPreviousLocal.equals(sLocal)){  //local has changed
                        ConfigurationManager.setProperty(CONF_OPTIONS_LANGUAGE,sLocal);
                        Messages.showInfoMessage(Messages.getString("ParameterView.103")); //$NON-NLS-1$
                    }
                }
            }
        }.start();
    }
    
    
    private void applyParameters(){
        //**Read all parameters**
        //Options
        boolean bHiddenState = jcbDisplayUnmounted.isSelected(); 
        if ( bHiddenState != bHidden){ //check if this option changed to launch a refresh if needed
            bHidden = bHiddenState;
            ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
        }
        ConfigurationManager.setProperty(CONF_OPTIONS_HIDE_UNMOUNTED,Boolean.toString(bHiddenState));
        ConfigurationManager.setProperty(CONF_OPTIONS_DEFAULT_ACTION_CLICK,Boolean.toString(jcbDefaultActionClick.isSelected()));
        ConfigurationManager.setProperty(CONF_OPTIONS_DEFAULT_ACTION_DROP,Boolean.toString(jcbDefaultActionDrop.isSelected()));
        ConfigurationManager.setProperty(CONF_OPTIONS_SYNC_TABLE_TREE,Boolean.toString(jcbSyncTableTree.isSelected()));
        ConfigurationManager.setProperty(CONF_OPTIONS_SHOW_POPUP,Boolean.toString(jcbShowPopup.isSelected()));
        int iLogLevel = scbLogLevel.getSelectedIndex(); 
        Log.setVerbosity(iLogLevel);
        ConfigurationManager.setProperty(CONF_OPTIONS_LOG_LEVEL,Integer.toString(iLogLevel));
        ConfigurationManager.setProperty(CONF_OPTIONS_INTRO_BEGIN,
            Integer.toString(introPosition.getValue()));
        ConfigurationManager.setProperty(CONF_OPTIONS_INTRO_LENGTH,
                Integer.toString(introLength.getValue()));
        String sBestofSize = jtfBestofSize.getText();
        if (!sBestofSize.equals("")){ //$NON-NLS-1$
            ConfigurationManager.setProperty(CONF_BESTOF_SIZE,sBestofSize);
        }
        FileManager.getInstance().setRateHasChanged(true); //force refresh of bestof files
        String sNoveltiesAge = jtfNoveltiesAge.getText();
        if (!sNoveltiesAge.equals("")){ //$NON-NLS-1$
            ConfigurationManager.setProperty(CONF_OPTIONS_NOVELTIES_AGE,sNoveltiesAge);
        }
        String sVisiblePlanned = jtfVisiblePlanned.getText();
        if (!sVisiblePlanned.equals("")){ //$NON-NLS-1$
            ConfigurationManager.setProperty(CONF_OPTIONS_VISIBLE_PLANNED,sVisiblePlanned);
        }
        ConfigurationManager.setProperty(CONF_FADE_DURATION,
                Integer.toString(crossFadeDuration.getValue()));
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
        ConfigurationManager.setProperty(CONF_CONFIRMATIONS_CLEAR_HISTORY,Boolean.toString(jcbBeforeClearingHistory.isSelected()));
        ConfigurationManager.setProperty(CONF_CONFIRMATIONS_RESET_RATINGS,Boolean.toString(jcbBeforeResetingRatings.isSelected()));
        //History
        String sHistoryDuration = jtfHistory.getText();
        if (!sHistoryDuration.equals("")){ //$NON-NLS-1$
            ConfigurationManager.setProperty(CONF_HISTORY,sHistoryDuration);
        }
        //P2P
        ConfigurationManager.setProperty(CONF_P2P_SHARE,Boolean.toString(jcbShare.isSelected()));
        ConfigurationManager.setProperty(CONF_P2P_ADD_REMOTE_PROPERTIES,Boolean.toString(jcbAddRemoteProperties.isSelected()));
        ConfigurationManager.setProperty(CONF_P2P_HIDE_LOCAL_PROPERTIES,Boolean.toString(jcbHideProperties.isSelected()));
        String sPass = jpfPasswd.getSelectedText();
        if (sPass!=null && !sPass.equals("")){ //$NON-NLS-1$
            ConfigurationManager.setProperty(CONF_P2P_PASSWORD,MD5Processor.hash(sPass));
        }
        //tags
        ConfigurationManager.setProperty(CONF_TAGS_USE_PARENT_DIR,Boolean.toString(jcbUseParentDir.isSelected()));
        ConfigurationManager.setProperty(CONF_REFACTOR_PATTERN,jtfRefactorPattern.getText());
        ConfigurationManager.setProperty(CONF_ANIMATION_PATTERN,jtfAnimationPattern.getText());
        //Advanced
        ConfigurationManager.setProperty(CONF_BACKUP_SIZE,Integer.toString(backupSize.getValue()));
        ConfigurationManager.setProperty(CONF_COLLECTION_CHARSET,jcbCollectionEncoding.getSelectedItem().toString());
        ConfigurationManager.setProperty(CONF_REGEXP,Boolean.toString(jcbRegexp.isSelected()));
        //Network
        ConfigurationManager.setProperty(CONF_NETWORK_USE_PROXY,Boolean.toString(jcbProxy.isSelected()));
        ConfigurationManager.setProperty(CONF_NETWORK_PROXY_HOSTNAME,jtfProxyHostname.getText());
        ConfigurationManager.setProperty(CONF_NETWORK_PROXY_PORT,jtfProxyPort.getText());
        ConfigurationManager.setProperty(CONF_NETWORK_PROXY_LOGIN,jtfProxyLogin.getText());
        ConfigurationManager.setProperty(CONF_NETWORK_CONNECTION_TO,Integer.toString(connectionTO.getValue()));
        ConfigurationManager.setProperty(CONF_NETWORK_TRANSFERT_TO,Integer.toString(transfertTO.getValue()));
        //Covers
        ConfigurationManager.setProperty(CONF_COVERS_AUTO_COVER,Boolean.toString(jcbAutoCover.isSelected()));
        ConfigurationManager.setProperty(CONF_COVERS_SHUFFLE,Boolean.toString(jcbShuffleCover.isSelected()));
        ConfigurationManager.setProperty(CONF_COVERS_PRELOAD,Boolean.toString(jcbPreLoad.isSelected()));
        ConfigurationManager.setProperty(CONF_COVERS_CHANGE_AT_EACH_TRACK,Boolean.toString(jcbLoadEachTrack.isSelected()));
        ConfigurationManager.setProperty(CONF_COVERS_MIN_SIZE,jtfMinSize.getText());
        ConfigurationManager.setProperty(CONF_COVERS_MAX_SIZE,jtfMaxSize.getText());//commit configuration
        ConfigurationManager.commit();
        //notify playlist editor (usefull for novelties)
        ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
        //Notify tables
        ObservationManager.notify(new Event(EventSubject.EVENT_SYNC_TREE_TABLE));
        //display a message
        InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.109"),InformationJPanel.INFORMATIVE); //$NON-NLS-1$
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
        jcbBeforeClearingHistory.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_CLEAR_HISTORY));
        jcbBeforeResetingRatings.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_RESET_RATINGS));
        jcbBeforeRefactorFiles.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_REFACTOR_FILES));
        //options
        boolean bHidden = ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED);
        jcbDisplayUnmounted.setSelected(bHidden);
        this.bHidden = bHidden; 
        jcbDefaultActionClick.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
        jcbDefaultActionDrop.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_DROP));
        jcbShowPopup.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_SHOW_POPUP));
        jcbSyncTableTree.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE));
        scbLanguage.setSelectedIndex(Messages.getInstance().getLocals().indexOf(ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE)));
        scbLanguage.addActionListener(this);
        scbLAF.setSelectedItem(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
        scbLAF.addActionListener(this);
        scbLogLevel.setSelectedIndex(Integer.parseInt(ConfigurationManager.getProperty(CONF_OPTIONS_LOG_LEVEL)));
        introLength.setValue(ConfigurationManager.getInt(CONF_OPTIONS_INTRO_LENGTH));
        introPosition.setValue(ConfigurationManager.getInt(CONF_OPTIONS_INTRO_BEGIN));
        jtfBestofSize.setText(ConfigurationManager.getProperty(CONF_BESTOF_SIZE));
        jtfNoveltiesAge.setText(ConfigurationManager.getProperty(CONF_OPTIONS_NOVELTIES_AGE));
        jtfVisiblePlanned.setText(ConfigurationManager.getProperty(CONF_OPTIONS_VISIBLE_PLANNED));
        crossFadeDuration.setValue(ConfigurationManager.getInt(CONF_FADE_DURATION));
        jcbShare.setSelected(ConfigurationManager.getBoolean(CONF_P2P_SHARE));
        jpfPasswd.setText(ConfigurationManager.getProperty(CONF_P2P_PASSWORD));
        jcbAddRemoteProperties.setSelected(ConfigurationManager.getBoolean(CONF_P2P_ADD_REMOTE_PROPERTIES));
        bHidden = ConfigurationManager.getBoolean(CONF_P2P_HIDE_LOCAL_PROPERTIES);
        jcbHideProperties.setSelected(bHidden);
        jcbUseParentDir.setSelected(ConfigurationManager.getBoolean(CONF_TAGS_USE_PARENT_DIR));
        //advanced
        int iBackupSize = ConfigurationManager.getInt(CONF_BACKUP_SIZE);
        if (iBackupSize<=0){ //backup size =0 means no backup
            jcbBackup.setSelected(false);   
            backupSize.setEnabled(false);
            jlBackupSize.setEnabled(false);
        }
        else{
            jcbBackup.setSelected(true);
            backupSize.setEnabled(true);
            jlBackupSize.setEnabled(true);
        }
        backupSize.setValue(iBackupSize);
        jcbCollectionEncoding.setSelectedItem(ConfigurationManager.getProperty(CONF_COLLECTION_CHARSET));
        jtfRefactorPattern.setText(ConfigurationManager.getProperty(CONF_REFACTOR_PATTERN));
        jtfAnimationPattern.setText(ConfigurationManager.getProperty(CONF_ANIMATION_PATTERN));
        //network
        boolean bUseProxy = ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY);
        jcbProxy.setSelected(bUseProxy);
        jtfProxyHostname.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME));
        jtfProxyHostname.setEnabled(bUseProxy);
        jlProxyHostname.setEnabled(bUseProxy);
        jtfProxyPort.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_PORT));
        jtfProxyPort.setEnabled(bUseProxy);
        jlProxyPort.setEnabled(bUseProxy);
        jtfProxyLogin.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN));
        jtfProxyLogin.setEnabled(bUseProxy);
        jlProxyLogin.setEnabled(bUseProxy);
        connectionTO.setValue(ConfigurationManager.getInt(CONF_NETWORK_CONNECTION_TO));
        transfertTO.setValue(ConfigurationManager.getInt(CONF_NETWORK_TRANSFERT_TO));
        //Covers
        jcbAutoCover.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
        jtfMinSize.setText(ConfigurationManager.getProperty(CONF_COVERS_MIN_SIZE));
        jlMinSize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
        jtfMinSize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
        jtfMaxSize.setText(ConfigurationManager.getProperty(CONF_COVERS_MAX_SIZE));
        jtfMaxSize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
        jlMaxSize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
        jcbShuffleCover.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE));
        jcbPreLoad.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_PRELOAD));
        jcbPreLoad.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
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
