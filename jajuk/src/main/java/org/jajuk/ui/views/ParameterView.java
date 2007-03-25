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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
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
import org.jajuk.base.Observer;
import org.jajuk.base.SearchResult;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.share.audioscrobbler.AudioScrobblerManager;
import org.jajuk.ui.DefaultMouseWheelListener;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukJPanel;
import org.jajuk.ui.LNFManager;
import org.jajuk.ui.PathSelector;
import org.jajuk.ui.PatternInputVerifier;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.SearchBox;
import org.jajuk.ui.SteppedComboBox;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * View used to set Jajuk paramers.
 * <p>
 * Configuration perspective *
 * <p>
 * Singleton
 * 
 * @author Bertrand Florat
 * @created 17 nov. 2003
 */
public class ParameterView extends ViewAdapter implements ActionListener, ListSelectionListener,
		ItemListener, ChangeListener, Observer {

	private static final long serialVersionUID = 1L;

	/** Self instance */
	private static ParameterView pv;

	JTabbedPane jtpMain;

	JajukJPanel jpHistory;

	JLabel jlHistory;

	JTextField jtfHistory;

	JButton jbClearHistory;

	JButton jbResetRatings;

	JajukJPanel jpStart;

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

	JajukJPanel jpConfirmations;

	JCheckBox jcbBeforeDelete;

	JCheckBox jcbBeforeExit;

	JCheckBox jcbBeforeRemoveDevice;

	JCheckBox jcbBeforeDeleteCover;

	JCheckBox jcbBeforeClearingHistory;

	JCheckBox jcbBeforeResetingRatings;

	JCheckBox jcbBeforeRefactorFiles;

	JajukJPanel jpOptions;

	JCheckBox jcbDisplayUnmounted;

	JCheckBox jcbSyncTableTree;

	JCheckBox jcbAudioScrobbler;

	JLabel jlASUser;

	JTextField jtfASUser;

	JLabel jlASPassword;

	JPasswordField jpfASPassword;

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

	JCheckBox jcbShowBaloon;

	JCheckBox jcbHotkeys;

	JajukJPanel jpP2P;

	JCheckBox jcbShare;

	JLabel jlPasswd;

	JPasswordField jpfPasswd;

	JCheckBox jcbAddRemoteProperties;

	JCheckBox jcbHideProperties;

	JajukJPanel jpTags;

	JCheckBox jcbUseParentDir;

	JLabel jlRefactorPattern;

	JFormattedTextField jtfRefactorPattern;

	JLabel jlAnimationPattern;

	JTextField jtfAnimationPattern;

	JajukJPanel jpAdvanced;

	JCheckBox jcbBackup;

	JLabel jlBackupSize;

	JSlider backupSize;

	JLabel jlCollectionEncoding;

	JComboBox jcbCollectionEncoding;

	JCheckBox jcbRegexp;

	JajukJPanel jpNetwork;

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

	JajukJPanel jpCovers;

	JCheckBox jcbAutoCover;

	JCheckBox jcbShuffleCover;

	JCheckBox jcbPreLoad;

	JCheckBox jcbLoadEachTrack;

	JLabel jlCoverSize;

	JComboBox jcbCoverSize;

	JLabel jlMPlayerArgs;

	JTextField jtfMPlayerArgs;

	JLabel jlEnvVariables;

	JTextField jtfEnvVariables;

	JLabel jlJajukWorkspace;

	PathSelector psJajukWorkspace;
	
	JajukJPanel jpOKCancel;

	JButton jbOK;

	JButton jbDefault;

	JajukJPanel jpModes;

	/** Previous value for hidden option, used to check if a refresh is need */
	boolean bHidden;

	/** Return self instance */
	public static synchronized ParameterView getInstance() {
		if (pv == null) {
			pv = new ParameterView();
		}
		return pv;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#display()
	 */
	public void initUI() {
		int iXSeparator = 5;
		int iYSeparator = 5;

		// --History
		jpHistory = new JajukJPanel();

		double sizeHistory[][] = {
				{ iXSeparator, TableLayout.PREFERRED, iXSeparator, TableLayout.PREFERRED },
				{ 20, 20, 10 * iYSeparator, 25, 10 * iYSeparator, 25 } };
		jpHistory.setLayout(new TableLayout(sizeHistory));
		jlHistory = new JLabel(Messages.getString("ParameterView.0")); //$NON-NLS-1$
		jtfHistory = new JTextField();
		jtfHistory.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try {
					int iValue = Integer.parseInt(sText);
					if (iValue < -1) {
						jbOK.setEnabled(false);
						return false;
					}
				} catch (Exception e) {
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
		jbClearHistory = new JButton(
				Messages.getString("ParameterView.3"), Util.getIcon(ICON_CLEAR)); //$NON-NLS-1$
		jbClearHistory.setToolTipText(Messages.getString("ParameterView.4")); //$NON-NLS-1$
		jbClearHistory.addActionListener(this);
		jbResetRatings = new JButton(
				Messages.getString("ParameterView.186"), Util.getIcon(ICON_CLEAR)); //$NON-NLS-1$
		jbResetRatings.setToolTipText(Messages.getString("ParameterView.187")); //$NON-NLS-1$
		jbResetRatings.addActionListener(this);
		jpHistory.add(jlHistory, "1,1"); //$NON-NLS-1$
		jpHistory.add(jtfHistory, "3,1"); //$NON-NLS-1$
		jpHistory.add(jbClearHistory, "3,3"); //$NON-NLS-1$
		jpHistory.add(jbResetRatings, "3,5"); //$NON-NLS-1$

		// --Startup
		jpStart = new JajukJPanel();
		double sizeStart[][] = {
				{ TableLayout.PREFERRED, iXSeparator, TableLayout.PREFERRED, iXSeparator,
						TableLayout.PREFERRED, iXSeparator },
				{ 20, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20,
						iYSeparator, 20, iYSeparator, 20 } };
		jpStart.setLayout(new TableLayout(sizeStart));
		jlStart = new JLabel(Messages.getString("ParameterView.9")); //$NON-NLS-1$
		bgStart = new ButtonGroup();
		jrbNothing = new JRadioButton(Messages.getString("ParameterView.10")); //$NON-NLS-1$
		jrbNothing.setOpaque(false);
		jrbNothing.setToolTipText(Messages.getString("ParameterView.11")); //$NON-NLS-1$
		jrbNothing.addItemListener(this);
		jrbLast = new JRadioButton(Messages.getString("ParameterView.12")); //$NON-NLS-1$
		jrbLast.setOpaque(false);
		jrbLast.setToolTipText(Messages.getString("ParameterView.13")); //$NON-NLS-1$
		jrbLast.addItemListener(this);
		jrbLastKeepPos = new JRadioButton(Messages.getString("ParameterView.135")); //$NON-NLS-1$
		jrbLastKeepPos.setOpaque(false);
		jrbLastKeepPos.setToolTipText(Messages.getString("ParameterView.136")); //$NON-NLS-1$
		jrbLastKeepPos.addItemListener(this);
		jrbShuffle = new JRadioButton(Messages.getString("ParameterView.14")); //$NON-NLS-1$
		jrbShuffle.setOpaque(false);
		jrbShuffle.setToolTipText(Messages.getString("ParameterView.15")); //$NON-NLS-1$
		jrbShuffle.addItemListener(this);
		jrbBestof = new JRadioButton(Messages.getString("ParameterView.131")); //$NON-NLS-1$
		jrbBestof.setOpaque(false);
		jrbBestof.setToolTipText(Messages.getString("ParameterView.132")); //$NON-NLS-1$
		jrbBestof.addItemListener(this);
		jrbNovelties = new JRadioButton(Messages.getString("ParameterView.133")); //$NON-NLS-1$
		jrbNovelties.setOpaque(false);
		jrbNovelties.setToolTipText(Messages.getString("ParameterView.134")); //$NON-NLS-1$
		jrbNovelties.addItemListener(this);
		jrbFile = new JRadioButton(Messages.getString("ParameterView.16")); //$NON-NLS-1$
		jrbFile.setOpaque(false);
		jrbFile.setToolTipText(Messages.getString("ParameterView.17")); //$NON-NLS-1$
		jrbFile.addItemListener(this);
		sbSearch = new SearchBox(this);
		// disabled by default, is enabled only if jrbFile is enabled
		sbSearch.setEnabled(false);
		// set chosen track in file selection
		String sFileId = ConfigurationManager.getProperty(CONF_STARTUP_FILE);
		if (!"".equals(sFileId)) { //$NON-NLS-1$
			File file = FileManager.getInstance().getFileByID(sFileId);
			if (file != null) {
				sbSearch.setText(file.getTrack().getName());
			} else {
				// the file exists no more, remove its id as startup file
				ConfigurationManager.setProperty(CONF_STARTUP_FILE, "");
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
		jpStart.add(jlStart, "0,3"); //$NON-NLS-1$
		jpStart.add(jrbNothing, "2,1"); //$NON-NLS-1$
		jpStart.add(jrbLast, "2,3"); //$NON-NLS-1$
		jpStart.add(jrbLastKeepPos, "2,5"); //$NON-NLS-1$
		jpStart.add(jrbShuffle, "2,7"); //$NON-NLS-1$
		jpStart.add(jrbBestof, "2,9"); //$NON-NLS-1$
		jpStart.add(jrbNovelties, "2,11"); //$NON-NLS-1$
		jpStart.add(jrbFile, "2,13"); //$NON-NLS-1$
		jpStart.add(sbSearch, "4,13"); //$NON-NLS-1$

		// --Confirmations
		jpConfirmations = new JajukJPanel();
		double sizeConfirmations[][] = {
				{ 0.99 },
				{ 20, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20,
						iYSeparator, 20, iYSeparator, 20, iYSeparator } };
		jpConfirmations.setLayout(new TableLayout(sizeConfirmations));

		jcbBeforeDelete = new JCheckBox(Messages.getString("ParameterView.27")); //$NON-NLS-1$
		jcbBeforeDelete.setOpaque(false);

		jcbBeforeDelete.setToolTipText(Messages.getString("ParameterView.28")); //$NON-NLS-1$
		jcbBeforeDelete.setOpaque(false);

		jcbBeforeExit = new JCheckBox(Messages.getString("ParameterView.29")); //$NON-NLS-1$
		jcbBeforeExit.setOpaque(false);
		jcbBeforeExit.setToolTipText(Messages.getString("ParameterView.30")); //$NON-NLS-1$
		jcbBeforeExit.setOpaque(false);

		jcbBeforeRemoveDevice = new JCheckBox(Messages.getString("ParameterView.164")); //$NON-NLS-1$
		jcbBeforeRemoveDevice.setOpaque(false);
		jcbBeforeRemoveDevice.setToolTipText(Messages.getString("ParameterView.165")); //$NON-NLS-1$

		jcbBeforeDeleteCover = new JCheckBox(Messages.getString("ParameterView.171")); //$NON-NLS-1$
		jcbBeforeDeleteCover.setOpaque(false);
		jcbBeforeDeleteCover.setToolTipText(Messages.getString("ParameterView.172")); //$NON-NLS-1$

		jcbBeforeClearingHistory = new JCheckBox(Messages.getString("ParameterView.188")); //$NON-NLS-1$
		jcbBeforeClearingHistory.setOpaque(false);
		jcbBeforeClearingHistory.setToolTipText(Messages.getString("ParameterView.188")); //$NON-NLS-1$

		jcbBeforeResetingRatings = new JCheckBox(Messages.getString("ParameterView.189")); //$NON-NLS-1$
		jcbBeforeResetingRatings.setOpaque(false);
		jcbBeforeResetingRatings.setToolTipText(Messages.getString("ParameterView.189")); //$NON-NLS-1$

		jcbBeforeRefactorFiles = new JCheckBox(Messages.getString("ParameterView.194")); //$NON-NLS-1$
		jcbBeforeRefactorFiles.setToolTipText(Messages.getString("ParameterView.194")); //$NON-NLS-1$
		jcbBeforeRefactorFiles.setOpaque(false);

		jpConfirmations.add(jcbBeforeDelete, "0,1"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeExit, "0,3"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeRemoveDevice, "0,5"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeDeleteCover, "0,7"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeClearingHistory, "0,9"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeResetingRatings, "0,11"); //$NON-NLS-1$
		jpConfirmations.add(jcbBeforeRefactorFiles, "0,13"); //$NON-NLS-1$

		// -Modes
		jpModes = new JajukJPanel();
		// Intro
		// intro position
		jlIntroPosition = new JLabel(Messages.getString("ParameterView.59")); //$NON-NLS-1$
		introPosition = new JSlider(0, 100, 0);
		introPosition.setOpaque(false);
		introPosition.setMajorTickSpacing(20);
		introPosition.setMinorTickSpacing(10);
		introPosition.setPaintTicks(true);
		introPosition.setPaintLabels(true);
		introPosition.setToolTipText(Messages.getString("ParameterView.60")); //$NON-NLS-1$
		introPosition.addMouseWheelListener(new DefaultMouseWheelListener(introPosition));

		// intro length
		jlIntroLength = new JLabel(Messages.getString("ParameterView.61")); //$NON-NLS-1$
		introLength = new JSlider(0, 30, 20);
		introLength.setOpaque(false);
		introLength.setMajorTickSpacing(10);
		introLength.setMinorTickSpacing(1);
		introLength.setPaintTicks(true);
		introLength.setPaintLabels(true);
		introLength.setToolTipText(Messages.getString("ParameterView.110")); //$NON-NLS-1$
		introLength.addMouseWheelListener(new DefaultMouseWheelListener(introLength));

		// best of size
		jlBestofSize = new JLabel(Messages.getString("ParameterView.111")); //$NON-NLS-1$
		jlBestofSize.setToolTipText(Messages.getString("ParameterView.112")); //$NON-NLS-1$
		jtfBestofSize = new JTextField(3);
		jtfBestofSize.setToolTipText(Messages.getString("ParameterView.112")); //$NON-NLS-1$
		jtfBestofSize.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try {
					int iValue = Integer.parseInt(sText);
					if (iValue < 1 || iValue > 100) {
						jbOK.setEnabled(false);
						return false;
					}
				} catch (Exception e) {
					return false;
				}
				jbOK.setEnabled(true);
				return true;
			}

			public boolean shouldYieldFocus(JComponent input) {
				return verify(input);
			}
		});
		// novelties age
		jlNoveltiesAge = new JLabel(Messages.getString("ParameterView.129")); //$NON-NLS-1$
		jlNoveltiesAge.setToolTipText(Messages.getString("ParameterView.130")); //$NON-NLS-1$
		jtfNoveltiesAge = new JTextField(3);
		jtfNoveltiesAge.setToolTipText(Messages.getString("ParameterView.130")); //$NON-NLS-1$
		jtfNoveltiesAge.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try {
					int iValue = Integer.parseInt(sText);
					if (iValue < 0) { // if adding age =0, it mean today, no
						// max limit
						jbOK.setEnabled(false);
						return false;
					}
				} catch (Exception e) {
					return false;
				}
				jbOK.setEnabled(true);
				return true;
			}

			public boolean shouldYieldFocus(JComponent input) {
				return verify(input);
			}
		});
		// number of visible tracks
		jlVisiblePlanned = new JLabel(Messages.getString("ParameterView.177")); //$NON-NLS-1$
		jlVisiblePlanned.setToolTipText(Messages.getString("ParameterView.178")); //$NON-NLS-1$
		jtfVisiblePlanned = new JTextField(3);
		jtfVisiblePlanned.setToolTipText(Messages.getString("ParameterView.178")); //$NON-NLS-1$
		jtfVisiblePlanned.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try {
					int iValue = Integer.parseInt(sText);
					// number of planned tracks between 0 and 100
					if (iValue < 0 || iValue > 100) {
						return false;
					}
				} catch (Exception e) {
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
		crossFadeDuration = new JSlider(0, 30, 0);
		crossFadeDuration.setOpaque(false);
		crossFadeDuration.setMajorTickSpacing(10);
		crossFadeDuration.setMinorTickSpacing(1);
		crossFadeDuration.setPaintTicks(true);
		crossFadeDuration.setPaintLabels(true);
		crossFadeDuration.setToolTipText(Messages.getString("ParameterView.191")); //$NON-NLS-1$
		crossFadeDuration.addMouseWheelListener(new DefaultMouseWheelListener(crossFadeDuration));

		// add panels
		double sizeIntro[][] = {
				{ 0.50, 0.45 },
				{ 20, TableLayout.PREFERRED, iYSeparator, TableLayout.PREFERRED, iYSeparator,
						TableLayout.PREFERRED, iYSeparator, TableLayout.PREFERRED, iYSeparator,
						TableLayout.PREFERRED, iYSeparator, TableLayout.PREFERRED, iYSeparator } };
		jpModes.setLayout(new TableLayout(sizeIntro));
		jpModes.add(jlIntroPosition, "0,1"); //$NON-NLS-1$
		jpModes.add(introPosition, "1,1"); //$NON-NLS-1$
		jpModes.add(jlIntroLength, "0,3"); //$NON-NLS-1$
		jpModes.add(introLength, "1,3"); //$NON-NLS-1$
		jpModes.add(jlCrossFadeDuration, "0,5"); //$NON-NLS-1$
		jpModes.add(crossFadeDuration, "1,5"); //$NON-NLS-1$
		jpModes.add(jlBestofSize, "0,7"); //$NON-NLS-1$
		jpModes.add(jtfBestofSize, "1,7"); //$NON-NLS-1$
		jpModes.add(jlNoveltiesAge, "0,9"); //$NON-NLS-1$
		jpModes.add(jtfNoveltiesAge, "1,9"); //$NON-NLS-1$
		jpModes.add(jlVisiblePlanned, "0,11"); //$NON-NLS-1$
		jpModes.add(jtfVisiblePlanned, "1,11"); //$NON-NLS-1$

		// --Options
		jpOptions = new JajukJPanel();
		jcbDisplayUnmounted = new JCheckBox(Messages.getString("ParameterView.34")); //$NON-NLS-1$
		jcbDisplayUnmounted.setToolTipText(Messages.getString("ParameterView.35")); //$NON-NLS-1$
		jcbDisplayUnmounted.setOpaque(false);

		jcbSyncTableTree = new JCheckBox(Messages.getString("ParameterView.183")); //$NON-NLS-1$
		jcbSyncTableTree.setToolTipText(Messages.getString("ParameterView.184")); //$NON-NLS-1$
		jcbSyncTableTree.setOpaque(false);

		jcbDefaultActionClick = new JCheckBox(Messages.getString("ParameterView.179")); //$NON-NLS-1$
		jcbDefaultActionClick.setToolTipText(Messages.getString("ParameterView.180")); //$NON-NLS-1$
		jcbDefaultActionClick.setOpaque(false);

		jcbDefaultActionDrop = new JCheckBox(Messages.getString("ParameterView.181")); //$NON-NLS-1$
		jcbDefaultActionDrop.setToolTipText(Messages.getString("ParameterView.182")); //$NON-NLS-1$
		jcbDefaultActionDrop.setOpaque(false);

		jcbShowBaloon = new JCheckBox(Messages.getString("ParameterView.185")); //$NON-NLS-1$
		jcbShowBaloon.setOpaque(false);
		jcbShowBaloon.setToolTipText(Messages.getString("ParameterView.185")); //$NON-NLS-1$

		jcbHotkeys = new JCheckBox(Messages.getString("ParameterView.196")); //$NON-NLS-1$
		jcbHotkeys.setOpaque(false);
		jcbHotkeys.addActionListener(this);
		jcbHotkeys.setToolTipText(Messages.getString("ParameterView.197")); //$NON-NLS-1$
		// Disable this option if not under windows
		jcbHotkeys.setEnabled(Util.isUnderWindows());

		JPanel jpCombos = new JPanel();
		jpCombos.setOpaque(false);
		double sizeCombos[][] = { { 0.50, 0.45 }, { 20, iYSeparator, 20, iYSeparator, 20 } };
		jpCombos.setLayout(new TableLayout(sizeCombos));
		jlLanguage = new JLabel(Messages.getString("ParameterView.38")); //$NON-NLS-1$
		scbLanguage = new SteppedComboBox();
		Iterator itDescs = Messages.getDescs().iterator();
		while (itDescs.hasNext()) {
			String sDesc = (String) itDescs.next();
			scbLanguage.addItem(Messages.getString(sDesc));
		}
		scbLanguage.setToolTipText(Messages.getString("ParameterView.42")); //$NON-NLS-1$
		jlLAF = new JLabel(Messages.getString("ParameterView.43")); //$NON-NLS-1$
		jlLAF.setToolTipText(Messages.getString("ParameterView.44")); //$NON-NLS-1$
		scbLAF = new SteppedComboBox();
		Iterator it = LNFManager.getSupportedLNF().iterator();
		while (it.hasNext()) {
			scbLAF.addItem(it.next());
		}
		scbLAF.setToolTipText(Messages.getString("ParameterView.45")); //$NON-NLS-1$
		jpCombos.add(jlLanguage, "0,0"); //$NON-NLS-1$
		jpCombos.add(scbLanguage, "1,0"); //$NON-NLS-1$
		jpCombos.add(jlLAF, "0,2"); //$NON-NLS-1$
		jpCombos.add(scbLAF, "1,2"); //$NON-NLS-1$

		double sizeOptions[][] = {
				{ TableLayout.PREFERRED },
				{ 20, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20,
						iYSeparator, 20, iYSeparator, TableLayout.PREFERRED, iYSeparator } };
		jpOptions.setLayout(new TableLayout(sizeOptions));

		jpOptions.add(jcbDisplayUnmounted, "0,1"); //$NON-NLS-1$
		jpOptions.add(jcbDefaultActionClick, "0,3"); //$NON-NLS-1$
		jpOptions.add(jcbDefaultActionDrop, "0,5"); //$NON-NLS-1$
		jpOptions.add(jcbSyncTableTree, "0,7"); //$NON-NLS-1$
		jpOptions.add(jcbShowBaloon, "0,9"); //$NON-NLS-1$
		jpOptions.add(jcbHotkeys, "0,11"); //$NON-NLS-1$
		jpOptions.add(jpCombos, "0,13"); //$NON-NLS-1$

		// --P2P
		jpP2P = new JajukJPanel();
		double sizeP2P[][] = { { 0.6, 0.3, 0.1 },
				{ 20, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator } };
		jpP2P.setLayout(new TableLayout(sizeP2P));
		jcbShare = new JCheckBox(Messages.getString("ParameterView.72")); //$NON-NLS-1$
		jcbShare.setEnabled(false); // TBI
		jcbShare.setToolTipText(Messages.getString("ParameterView.73")); //$NON-NLS-1$
		jlPasswd = new JLabel(Messages.getString("ParameterView.74")); //$NON-NLS-1$
		jlPasswd.setEnabled(false); // TBI
		jpfPasswd = new JPasswordField();
		jpfPasswd.setEnabled(false); // TBI
		jpfPasswd.setToolTipText(Messages.getString("ParameterView.75")); //$NON-NLS-1$
		jcbAddRemoteProperties = new JCheckBox(Messages.getString("ParameterView.76")); //$NON-NLS-1$
		jcbAddRemoteProperties.setEnabled(false); // TBI
		jcbAddRemoteProperties.setToolTipText(Messages.getString("ParameterView.77")); //$NON-NLS-1$
		jcbHideProperties = new JCheckBox(Messages.getString("ParameterView.78")); //$NON-NLS-1$
		jcbHideProperties.setToolTipText(Messages.getString("ParameterView.79")); //$NON-NLS-1$
		jcbHideProperties.setEnabled(false); // TBI
		jpP2P.add(jcbShare, "0,1"); //$NON-NLS-1$
		jpP2P.add(jlPasswd, "0,3"); //$NON-NLS-1$
		jpP2P.add(jpfPasswd, "1,3"); //$NON-NLS-1$
		jpP2P.add(jcbAddRemoteProperties, "0,5"); //$NON-NLS-1$
		jpP2P.add(jcbHideProperties, "0,7"); //$NON-NLS-1$

		// --Tags
		jpTags = new JajukJPanel();
		double sizeTags[][] = { { 0.5, 0.45 },
				{ 20, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator } };
		jpTags.setLayout(new TableLayout(sizeTags));
		jcbUseParentDir = new JCheckBox(Messages.getString("ParameterView.101")); //$NON-NLS-1$
		jcbUseParentDir.setOpaque(false);
		jcbUseParentDir.setToolTipText(Messages.getString("ParameterView.102")); //$NON-NLS-1$
		jlRefactorPattern = new JLabel(Messages.getString("ParameterView.192")); //$NON-NLS-1$
		jlRefactorPattern.setToolTipText(Messages.getString("ParameterView.193")); //$NON-NLS-1$
		jtfRefactorPattern = new JFormattedTextField();
		jtfRefactorPattern.setToolTipText(Messages.getString("ParameterView.193")); //$NON-NLS-1$
		jtfRefactorPattern.setInputVerifier(new PatternInputVerifier());
		jlAnimationPattern = new JLabel(Messages.getString("ParameterView.195")); //$NON-NLS-1$
		jlAnimationPattern.setToolTipText(Messages.getString("ParameterView.193")); //$NON-NLS-1$
		jtfAnimationPattern = new JTextField();
		jtfAnimationPattern.setToolTipText(Messages.getString("ParameterView.193")); //$NON-NLS-1$
		jpTags.add(jcbUseParentDir, "0,1"); //$NON-NLS-1$
		jpTags.add(jlRefactorPattern, "0,3"); //$NON-NLS-1$
		jpTags.add(jtfRefactorPattern, "1,3"); //$NON-NLS-1$
		jpTags.add(jlAnimationPattern, "0,5"); //$NON-NLS-1$
		jpTags.add(jtfAnimationPattern, "1,5"); //$NON-NLS-1$

		// --Advanced
		jpAdvanced = new JajukJPanel();
		jcbBackup = new JCheckBox(Messages.getString("ParameterView.116")); //$NON-NLS-1$
		jcbBackup.setOpaque(false);
		jcbBackup.addActionListener(this);
		jcbBackup.setToolTipText(Messages.getString("ParameterView.117")); //$NON-NLS-1$
		jlBackupSize = new JLabel(Messages.getString("ParameterView.118")); //$NON-NLS-1$
		jlBackupSize.setToolTipText(Messages.getString("ParameterView.119")); //$NON-NLS-1$
		backupSize = new JSlider(0, 100);
		backupSize.setOpaque(false);
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
		jcbRegexp.setOpaque(false);
		jcbRegexp.setSelected(ConfigurationManager.getBoolean(CONF_REGEXP));//$NON-NLS-1$
		jcbRegexp.setToolTipText(Messages.getString("ParameterView.114")); //$NON-NLS-1$
		jcbCollectionEncoding.addItem("UTF-8"); //$NON-NLS-1$
		jcbCollectionEncoding.addItem("UTF-16"); //$NON-NLS-1$
		jlLogLevel = new JLabel(Messages.getString("ParameterView.46")); //$NON-NLS-1$
		scbLogLevel = new SteppedComboBox();
		scbLogLevel.addItem(Messages.getString("ParameterView.47")); //$NON-NLS-1$
		scbLogLevel.addItem(Messages.getString("ParameterView.48")); //$NON-NLS-1$
		scbLogLevel.addItem(Messages.getString("ParameterView.49")); //$NON-NLS-1$
		scbLogLevel.addItem(Messages.getString("ParameterView.50")); //$NON-NLS-1$
		scbLogLevel.addItem(Messages.getString("ParameterView.51")); //$NON-NLS-1$
		scbLogLevel.setToolTipText(Messages.getString("ParameterView.52")); //$NON-NLS-1$
		jlMPlayerArgs = new JLabel(Messages.getString("ParameterView.205"));
		jlMPlayerArgs.setToolTipText(Messages.getString("ParameterView.206"));
		jtfMPlayerArgs = new JTextField();
		jtfMPlayerArgs.setToolTipText(Messages.getString("ParameterView.206"));
		jlEnvVariables = new JLabel(Messages.getString("ParameterView.219"));
		jlEnvVariables.setToolTipText(Messages.getString("ParameterView.220"));
		jtfEnvVariables = new JTextField();
		jtfEnvVariables.setToolTipText(Messages.getString("ParameterView.220"));
		jlJajukWorkspace = new JLabel(Messages.getString("ParameterView.207"));
		jlJajukWorkspace.setToolTipText(Messages.getString("ParameterView.208"));
		// Directory selection
		psJajukWorkspace = new PathSelector(new JajukFileFilter(JajukFileFilter.DirectoryFilter
				.getInstance()), Main.workspace);
		psJajukWorkspace.setToolTipText(Messages.getString("ParameterView.208"));
		double sizeAdvanced[][] = {
				{ 0.5, 0.45 },
				{ 20, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayout.PREFERRED, TableLayout.PREFERRED,TableLayout.PREFERRED } };
		TableLayout layout = new TableLayout(sizeAdvanced);
		layout.setVGap(15);
		jpAdvanced.setLayout(layout);
		jpAdvanced.add(jcbRegexp, "0,1");//$NON-NLS-1$
		jpAdvanced.add(jlCollectionEncoding, "0,2");//$NON-NLS-1$
		jpAdvanced.add(jcbCollectionEncoding, "1,2");//$NON-NLS-1$
		jpAdvanced.add(jcbBackup, "0,3");//$NON-NLS-1$
		jpAdvanced.add(jlBackupSize, "0,4");//$NON-NLS-1$
		jpAdvanced.add(backupSize, "1,4");//$NON-NLS-1$        
		jpAdvanced.add(jlLogLevel, "0,5");//$NON-NLS-1$        
		jpAdvanced.add(scbLogLevel, "1,5");//$NON-NLS-1$
		jpAdvanced.add(jlMPlayerArgs, "0,6");//$NON-NLS-1$        
		jpAdvanced.add(jtfMPlayerArgs, "1,6");//$NON-NLS-1$
		jpAdvanced.add(jlEnvVariables, "0,7");//$NON-NLS-1$        
		jpAdvanced.add(jtfEnvVariables, "1,7");//$NON-NLS-1$
		jpAdvanced.add(jlJajukWorkspace, "0,8");//$NON-NLS-1$
		jpAdvanced.add(psJajukWorkspace, "1,8");//$NON-NLS-1$

		// - Network
		jpNetwork = new JajukJPanel();
		double sizeNetwork[][] = {
				{ 0.5, 0.45 },
				{ 20, TableLayout.PREFERRED, iYSeparator, TableLayout.PREFERRED, iYSeparator,
						TableLayout.PREFERRED, iYSeparator, TableLayout.PREFERRED, iYSeparator,
						TableLayout.PREFERRED, iYSeparator, TableLayout.PREFERRED, iYSeparator,
						TableLayout.PREFERRED, iYSeparator, TableLayout.PREFERRED, iYSeparator,
						TableLayout.PREFERRED, iYSeparator } };
		jpNetwork.setLayout(new TableLayout(sizeNetwork));
		jcbProxy = new JCheckBox(Messages.getString("ParameterView.140")); //$NON-NLS-1$
		jcbProxy.setOpaque(false);
		jcbProxy.setToolTipText(Messages.getString("ParameterView.141")); //$NON-NLS-1$
		jcbProxy.addActionListener(this);
		jlProxyHostname = new JLabel(Messages.getString("ParameterView.144")); //$NON-NLS-1$
		jlProxyHostname.setToolTipText(Messages.getString("ParameterView.145")); //$NON-NLS-1$
		jtfProxyHostname = new JTextField();
		jtfProxyHostname.setToolTipText(Messages.getString("ParameterView.145")); //$NON-NLS-1$
		jlProxyPort = new JLabel(Messages.getString("ParameterView.146")); //$NON-NLS-1$
		jlProxyPort.setToolTipText(Messages.getString("ParameterView.147")); //$NON-NLS-1$
		jtfProxyPort = new JTextField();
		jtfProxyPort.setToolTipText(Messages.getString("ParameterView.147")); //$NON-NLS-1$
		jtfProxyPort.setInputVerifier(new InputVerifier() {
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try {
					int iValue = Integer.parseInt(sText);
					if (iValue < 0 || iValue > 65535) {
						// port is between 0 and 65535
						jbOK.setEnabled(false);
						return false;
					}
				} catch (Exception e) {
					return false;
				}
				jbOK.setEnabled(true);
				return true;
			}

			public boolean shouldYieldFocus(JComponent input) {
				return verify(input);
			}
		});
		jlProxyLogin = new JLabel(Messages.getString("ParameterView.142")); //$NON-NLS-1$
		jlProxyLogin.setToolTipText(Messages.getString("ParameterView.143")); //$NON-NLS-1$
		jtfProxyLogin = new JTextField();
		jtfProxyLogin.setToolTipText(Messages.getString("ParameterView.143")); //$NON-NLS-1$
		InputVerifier verifier = new InputVerifier() { // verifier for TO
			public boolean verify(JComponent input) {
				JTextField tf = (JTextField) input;
				String sText = tf.getText();
				try {
					int iValue = Integer.parseInt(sText);
					if (iValue <= 0) { // time out must be > 0
						jbOK.setEnabled(false);
						return false;
					}
				} catch (Exception e) {
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

		jcbAudioScrobbler = new JCheckBox(Messages.getString("ParameterView.199"));
		jcbAudioScrobbler.setToolTipText(Messages.getString("ParameterView.200")); //$NON-NLS-1$
		jcbAudioScrobbler.setOpaque(false);
		jcbAudioScrobbler.addActionListener(this);

		jlASUser = new JLabel(Messages.getString("ParameterView.201"));
		jlASUser.setOpaque(false);

		jtfASUser = new JTextField();
		jtfASUser.setToolTipText(Messages.getString("ParameterView.202"));

		jlASPassword = new JLabel(Messages.getString("ParameterView.203"));
		jlASPassword.setOpaque(false);

		jpfASPassword = new JPasswordField();
		jpfASPassword.setToolTipText(Messages.getString("ParameterView.204"));

		jlConnectionTO = new JLabel(Messages.getString("ParameterView.160")); //$NON-NLS-1$
		jlConnectionTO.setToolTipText(Messages.getString("ParameterView.160")); //$NON-NLS-1$
		connectionTO = new JSlider(0, 60);
		connectionTO.setOpaque(false);
		connectionTO.setMajorTickSpacing(10);
		connectionTO.setMinorTickSpacing(5);
		connectionTO.setPaintTicks(true);
		connectionTO.setPaintLabels(true);
		connectionTO.setToolTipText(Messages.getString("ParameterView.161")); //$NON-NLS-1$)
		connectionTO.addMouseWheelListener(new DefaultMouseWheelListener(connectionTO));
		jlTransfertTO = new JLabel(Messages.getString("ParameterView.162")); //$NON-NLS-1$
		jlTransfertTO.setToolTipText(Messages.getString("ParameterView.163")); //$NON-NLS-1$
		transfertTO = new JSlider(0, 60);
		transfertTO.setOpaque(false);
		transfertTO.setMajorTickSpacing(10);
		transfertTO.setMinorTickSpacing(5);
		transfertTO.setPaintTicks(true);
		transfertTO.setPaintLabels(true);
		transfertTO.setToolTipText(Messages.getString("ParameterView.163")); //$NON-NLS-1$)
		transfertTO.addMouseWheelListener(new DefaultMouseWheelListener(transfertTO));
		jpNetwork.add(jlConnectionTO, "0,1"); //$NON-NLS-1$
		jpNetwork.add(connectionTO, "1,1"); //$NON-NLS-1$
		jpNetwork.add(jlTransfertTO, "0,3"); //$NON-NLS-1$
		jpNetwork.add(transfertTO, "1,3"); //$NON-NLS-1$
		jpNetwork.add(jcbProxy, "0,5"); //$NON-NLS-1$
		jpNetwork.add(jlProxyHostname, "0,7"); //$NON-NLS-1$
		jpNetwork.add(jtfProxyHostname, "1,7"); //$NON-NLS-1$
		jpNetwork.add(jlProxyPort, "0,9"); //$NON-NLS-1$
		jpNetwork.add(jtfProxyPort, "1,9"); //$NON-NLS-1$
		jpNetwork.add(jlProxyLogin, "0,11"); //$NON-NLS-1$
		jpNetwork.add(jtfProxyLogin, "1,11"); //$NON-NLS-1$
		jpNetwork.add(jcbAudioScrobbler, "0,13");
		jpNetwork.add(jlASUser, "0,15");
		jpNetwork.add(jtfASUser, "1,15");
		jpNetwork.add(jlASPassword, "0,17");
		jpNetwork.add(jpfASPassword, "1,17");

		// - Cover
		jpCovers = new JajukJPanel();
		double sizeCover[][] = {
				{ 0.5, 0.45 },
				{ 20, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20,
						iYSeparator } };
		jpCovers.setLayout(new TableLayout(sizeCover));
		jcbAutoCover = new JCheckBox(Messages.getString("ParameterView.148")); //$NON-NLS-1$
		jcbAutoCover.setOpaque(false);
		jcbAutoCover.setToolTipText(Messages.getString("ParameterView.149")); //$NON-NLS-1$
		jcbAutoCover.addActionListener(this);
		jcbShuffleCover = new JCheckBox(Messages.getString("ParameterView.166")); //$NON-NLS-1$
		jcbShuffleCover.setOpaque(false);
		jcbShuffleCover.setToolTipText(Messages.getString("ParameterView.167")); //$NON-NLS-1$
		jcbShuffleCover.addActionListener(this);
		jcbPreLoad = new JCheckBox(Messages.getString("ParameterView.169")); //$NON-NLS-1$
		jcbPreLoad.setOpaque(false);
		jcbPreLoad.setToolTipText(Messages.getString("ParameterView.170")); //$NON-NLS-1$
		jcbLoadEachTrack = new JCheckBox(Messages.getString("ParameterView.175")); //$NON-NLS-1$
		jcbLoadEachTrack.setOpaque(false);
		jcbLoadEachTrack.setToolTipText(Messages.getString("ParameterView.176")); //$NON-NLS-1$
		jlCoverSize = new JLabel(Messages.getString("ParameterView.150")); //$NON-NLS-1$
		jlCoverSize.setToolTipText(Messages.getString("ParameterView.151")); //$NON-NLS-1$
		jcbCoverSize = new JComboBox();
		jcbCoverSize.setToolTipText(Messages.getString("ParameterView.151")); //$NON-NLS-1$
		jcbCoverSize.addItem(Messages.getString("ParameterView.211"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.212"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.213"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.214"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.215"));
		jpCovers.add(jcbShuffleCover, "0,1"); //$NON-NLS-1$
		jpCovers.add(jcbLoadEachTrack, "1,1"); //$NON-NLS-1$
		jpCovers.add(jcbAutoCover, "0,3"); //$NON-NLS-1$
		jpCovers.add(jcbPreLoad, "0,5"); //$NON-NLS-1$
		jpCovers.add(jlCoverSize, "0,7"); //$NON-NLS-1$
		jpCovers.add(jcbCoverSize, "1,7"); //$NON-NLS-1$

		// --OK/cancel panel
		Dimension dim = new Dimension(200, 20);
		jpOKCancel = new JajukJPanel();
		jpOKCancel.setLayout(new FlowLayout());
		jbOK = new JButton(Messages.getString("ParameterView.85"), Util.getIcon(ICON_OK)); //$NON-NLS-1$
		jbOK.setPreferredSize(dim);
		jbOK.addActionListener(this);
		jpOKCancel.add(jbOK);
		jbDefault = new JButton(
				Messages.getString("ParameterView.86"), Util.getIcon(ICON_DEFAULTS_BIG)); //$NON-NLS-1$
		jbDefault.setPreferredSize(dim);
		jbDefault.addActionListener(this);
		jpOKCancel.add(jbDefault);

		// --Global layout
		double size[][] = { { 0.99 }, { 0.9, 0.10 } };
		setLayout(new TableLayout(size));
		// add main panels
		jtpMain = new JTabbedPane();
		jtpMain.addTab(Messages.getString("ParameterView.33"), jpOptions); //$NON-NLS-1$
		// TODO change label
		jtpMain.addTab(Messages.getString("JajukJMenuBar.9"), jpModes); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.19"), jpStart); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.98"), jpTags); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.8"), jpHistory); //$NON-NLS-1$
		// TODO jtpMain.addTab(Messages.getString("ParameterView.71"),jpP2P);
		jtpMain.addTab(Messages.getString("ParameterView.159"), jpCovers); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.26"), jpConfirmations); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.139"), jpNetwork); //$NON-NLS-1$
		jtpMain.addTab(Messages.getString("ParameterView.115"), jpAdvanced); //$NON-NLS-1$
		try {
			// Reload stored selected index
			jtpMain.setSelectedIndex(ConfigurationManager.getInt(CONF_OPTIONS_TAB));
		} catch (Exception e) {
			// an error can occur if a new release brings or remove tabs
			Log.error(e);
			jtpMain.setSelectedIndex(0);
		}
		jtpMain.addChangeListener(this);
		add(jtpMain, "0,0"); //$NON-NLS-1$
		add(jpOKCancel, "0,1"); //$NON-NLS-1$
		// update widgets state
		updateSelection();
		ObservationManager.register(this);
	}
	
	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_PARAMETERS_CHANGE);
		return eventSubjectSet;
	}

	/**
	 * 
	 */
	public ParameterView() {
		pv = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "ParameterView.87"; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		new Thread() {
			public void run() {
				if (e.getSource() == jbClearHistory) {
					// show confirmation message if required
					if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_CLEAR_HISTORY)) {
						int iResu = Messages
								.getChoice(
										Messages.getString("Confirmation_clear_history"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (iResu != JOptionPane.YES_OPTION) {
							return;
						}
					}
					ObservationManager.notify(new Event(EventSubject.EVENT_CLEAR_HISTORY));
				} else if (e.getSource() == jbResetRatings) {
					// show confirmation message if required
					if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_RESET_RATINGS)) {
						int iResu = Messages
								.getChoice(
										Messages.getString("Confirmation_reset_ratings"), JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (iResu != JOptionPane.YES_OPTION) {
							return;
						}
					}
					if (!DeviceManager.getInstance().isAnyDeviceRefreshing()) {
						// make sure none device is refreshing
						Iterator it = TrackManager.getInstance().getTracks().iterator();
						while (it.hasNext()) {
							Track track = (Track) it.next();
							track.setRate(0);
						}
						ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
					} else {
						Messages.showErrorMessage("120"); //$NON-NLS-1$
					}
				} else if (e.getSource() == jcbShuffleCover) {
					jcbLoadEachTrack.setEnabled(jcbShuffleCover.isSelected());
				} else if (e.getSource() == jbOK) {
					applyParameters();
				} else if (e.getSource() == jbDefault) {
					ConfigurationManager.setDefaultProperties();
					updateSelection();// update UI
					InformationJPanel.getInstance().setMessage(
							Messages.getString("ParameterView.110"), InformationJPanel.INFORMATIVE); //$NON-NLS-1$
					applyParameters();
				} else if (e.getSource() == jcbBackup) {
					// if backup option is unchecked, reset backup size
					if (jcbBackup.isSelected()) {
						backupSize.setEnabled(true);
						jlBackupSize.setEnabled(true);
						backupSize.setValue(ConfigurationManager.getInt(CONF_BACKUP_SIZE));
					} else {
						backupSize.setEnabled(false);
						jlBackupSize.setEnabled(false);
						backupSize.setValue(0); //$NON-NLS-1$
					}
				} else if (e.getSource() == jcbProxy) {
					if (jcbProxy.isSelected()) {
						jtfProxyHostname.setEnabled(true);
						jtfProxyPort.setEnabled(true);
						jtfProxyLogin.setEnabled(true);
						jlProxyHostname.setEnabled(true);
						jlProxyPort.setEnabled(true);
						jlProxyLogin.setEnabled(true);
					} else {
						jtfProxyHostname.setEnabled(false);
						jtfProxyPort.setEnabled(false);
						jtfProxyLogin.setEnabled(false);
						jlProxyHostname.setEnabled(false);
						jlProxyPort.setEnabled(false);
						jlProxyLogin.setEnabled(false);
					}
				} else if (e.getSource() == jcbAutoCover) {
					if (jcbAutoCover.isSelected()) {
						jcbCoverSize.setEnabled(true);
						jlCoverSize.setEnabled(true);
						jcbPreLoad.setEnabled(true);
					} else {
						jlCoverSize.setEnabled(false);
						jcbCoverSize.setEnabled(false);
						jcbPreLoad.setEnabled(false);
					}
				} else if (e.getSource() == jcbAudioScrobbler) {
					if (jcbAudioScrobbler.isSelected()) {
						jlASUser.setEnabled(true);
						jtfASUser.setEnabled(true);
						jlASPassword.setEnabled(true);
						jpfASPassword.setEnabled(true);
					} else {
						jlASUser.setEnabled(false);
						jtfASUser.setEnabled(false);
						jlASPassword.setEnabled(false);
						jpfASPassword.setEnabled(false);
					}
					Messages.showInfoMessage(Messages.getString("ParameterView.198"));
				} else if (e.getSource() == jtfASUser || e.getSource() == jpfASPassword) {
					AudioScrobblerManager.getInstance().handshake(jtfASUser.getText(),
							jpfASPassword.getText());
				} else if (e.getSource() == scbLAF) {
					ConfigurationManager.setProperty(CONF_OPTIONS_LNF, (String) scbLAF
							.getSelectedItem());
					if (!LNFManager.getCurrent().equals(scbLAF.getSelectedItem())) {
						// Lnf has changed
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								LNFManager.setLookAndFeel(ConfigurationManager
										.getProperty(CONF_OPTIONS_LNF));
								SwingUtilities.updateComponentTreeUI(Main.getWindow());
								// force the perspective panel to refresh
								PerspectiveBarJPanel.getInstance().setActivated(
										PerspectiveManager.getCurrentPerspective());

							}
						});
					}
				} else if (e.getSource() == scbLanguage) {
					String sLocal = Messages.getLocales().get(scbLanguage.getSelectedIndex());
					String sPreviousLocal = Messages.getInstance().getLocal();
					if (!sPreviousLocal.equals(sLocal)) {
						// local has changed
						ConfigurationManager.setProperty(CONF_OPTIONS_LANGUAGE, sLocal);
						Messages.showInfoMessage(Messages.getString("ParameterView.198")); //$NON-NLS-1$
					}
				} else if (e.getSource() == jcbHotkeys) {
					ConfigurationManager.setProperty(CONF_OPTIONS_HOTKEYS, Boolean
							.toString(jcbHotkeys.isSelected()));
					// Hotkey flag changes is taken into account only at next
					// startup
					Messages.showInfoMessage(Messages.getString("ParameterView.198"));
				}
			}
		}.start();
	}

	private void applyParameters() {
		// **Read all parameters**
		// Options
		boolean bHiddenState = jcbDisplayUnmounted.isSelected();
		if (bHiddenState != bHidden) { // check if this option changed to
			// launch a refresh if needed
			bHidden = bHiddenState;
			ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
		}
		ConfigurationManager.setProperty(CONF_OPTIONS_HIDE_UNMOUNTED, Boolean
				.toString(bHiddenState));
		ConfigurationManager.setProperty(CONF_OPTIONS_DEFAULT_ACTION_CLICK, Boolean
				.toString(jcbDefaultActionClick.isSelected()));
		ConfigurationManager.setProperty(CONF_OPTIONS_DEFAULT_ACTION_DROP, Boolean
				.toString(jcbDefaultActionDrop.isSelected()));
		ConfigurationManager.setProperty(CONF_OPTIONS_SYNC_TABLE_TREE, Boolean
				.toString(jcbSyncTableTree.isSelected()));
		ConfigurationManager.setProperty(CONF_OPTIONS_SHOW_POPUP, Boolean.toString(jcbShowBaloon
				.isSelected()));
		ConfigurationManager.setProperty(CONF_OPTIONS_HOTKEYS, Boolean.toString(jcbHotkeys
				.isSelected()));
		ConfigurationManager.setProperty(CONF_OPTIONS_AUDIOSCROBBLER, Boolean
				.toString(jcbAudioScrobbler.isSelected()));
		if (jcbAudioScrobbler.isSelected()) {
			Log.debug(new String(jpfASPassword.getPassword()));
			ConfigurationManager.setProperty(CONF_OPTIONS_AUDIOSCROBBLER_USER, jtfASUser.getText());
			ConfigurationManager.setProperty(CONF_OPTIONS_AUDIOSCROBBLER_PASSWORD, new String(
					jpfASPassword.getPassword()));
		}
		int iLogLevel = scbLogLevel.getSelectedIndex();
		Log.setVerbosity(iLogLevel);
		ConfigurationManager.setProperty(CONF_OPTIONS_LOG_LEVEL, Integer.toString(iLogLevel));
		ConfigurationManager.setProperty(CONF_OPTIONS_INTRO_BEGIN, Integer.toString(introPosition
				.getValue()));
		ConfigurationManager.setProperty(CONF_OPTIONS_INTRO_LENGTH, Integer.toString(introLength
				.getValue()));
		String sBestofSize = jtfBestofSize.getText();
		if (!sBestofSize.equals("")) { //$NON-NLS-1$
			ConfigurationManager.setProperty(CONF_BESTOF_SIZE, sBestofSize);
		}
		// force refresh of bestof files
		FileManager.getInstance().setRateHasChanged(true);
		String sNoveltiesAge = jtfNoveltiesAge.getText();
		if (!sNoveltiesAge.equals("")) { //$NON-NLS-1$
			ConfigurationManager.setProperty(CONF_OPTIONS_NOVELTIES_AGE, sNoveltiesAge);
		}
		String sVisiblePlanned = jtfVisiblePlanned.getText();
		if (!sVisiblePlanned.equals("")) { //$NON-NLS-1$
			ConfigurationManager.setProperty(CONF_OPTIONS_VISIBLE_PLANNED, sVisiblePlanned);
		}
		int oldDuration = ConfigurationManager.getInt(CONF_FADE_DURATION);
		// Show an hidable message if user set cross fade under linux for sound
		// server information
		if (Util.isUnderLinux() && oldDuration == 0 && oldDuration != crossFadeDuration.getValue()) {
			Messages.showHideableWarningMessage(Messages.getString("ParameterView.210"),
					CONF_NOT_SHOW_AGAIN_CROSS_FADE);
		}
		ConfigurationManager.setProperty(CONF_FADE_DURATION, Integer.toString(crossFadeDuration
				.getValue()));
		// Startup
		if (jrbNothing.isSelected()) {
			ConfigurationManager.setProperty(CONF_STARTUP_MODE, STARTUP_MODE_NOTHING);
		} else if (jrbLast.isSelected()) {
			ConfigurationManager.setProperty(CONF_STARTUP_MODE, STARTUP_MODE_LAST);
		} else if (jrbLastKeepPos.isSelected()) {
			ConfigurationManager.setProperty(CONF_STARTUP_MODE, STARTUP_MODE_LAST_KEEP_POS);
		} else if (jrbShuffle.isSelected()) {
			ConfigurationManager.setProperty(CONF_STARTUP_MODE, STARTUP_MODE_SHUFFLE);
		} else if (jrbFile.isSelected()) {
			ConfigurationManager.setProperty(CONF_STARTUP_MODE, STARTUP_MODE_FILE);
		} else if (jrbBestof.isSelected()) {
			ConfigurationManager.setProperty(CONF_STARTUP_MODE, STARTUP_MODE_BESTOF);
		} else if (jrbNovelties.isSelected()) {
			ConfigurationManager.setProperty(CONF_STARTUP_MODE, STARTUP_MODE_NOVELTIES);
		}
		// Confirmations
		ConfigurationManager.setProperty(CONF_CONFIRMATIONS_DELETE_FILE, Boolean
				.toString(jcbBeforeDelete.isSelected()));
		ConfigurationManager.setProperty(CONF_CONFIRMATIONS_EXIT, Boolean.toString(jcbBeforeExit
				.isSelected()));
		ConfigurationManager.setProperty(CONF_CONFIRMATIONS_REMOVE_DEVICE, Boolean
				.toString(jcbBeforeRemoveDevice.isSelected()));
		ConfigurationManager.setProperty(CONF_CONFIRMATIONS_DELETE_COVER, Boolean
				.toString(jcbBeforeDeleteCover.isSelected()));
		ConfigurationManager.setProperty(CONF_CONFIRMATIONS_CLEAR_HISTORY, Boolean
				.toString(jcbBeforeClearingHistory.isSelected()));
		ConfigurationManager.setProperty(CONF_CONFIRMATIONS_RESET_RATINGS, Boolean
				.toString(jcbBeforeResetingRatings.isSelected()));
		// History
		String sHistoryDuration = jtfHistory.getText();
		if (!sHistoryDuration.equals("")) { //$NON-NLS-1$
			ConfigurationManager.setProperty(CONF_HISTORY, sHistoryDuration);
		}
		// P2P
		ConfigurationManager.setProperty(CONF_P2P_SHARE, Boolean.toString(jcbShare.isSelected()));
		ConfigurationManager.setProperty(CONF_P2P_ADD_REMOTE_PROPERTIES, Boolean
				.toString(jcbAddRemoteProperties.isSelected()));
		ConfigurationManager.setProperty(CONF_P2P_HIDE_LOCAL_PROPERTIES, Boolean
				.toString(jcbHideProperties.isSelected()));
		String sPass = jpfPasswd.getSelectedText();
		if (sPass != null && !sPass.equals("")) { //$NON-NLS-1$
			ConfigurationManager.setProperty(CONF_P2P_PASSWORD, MD5Processor.hash(sPass));
		}
		// tags
		ConfigurationManager.setProperty(CONF_TAGS_USE_PARENT_DIR, Boolean.toString(jcbUseParentDir
				.isSelected()));
		// Get and check reorg pattern
		String sPattern = jtfRefactorPattern.getText();
		ConfigurationManager.setProperty(CONF_REFACTOR_PATTERN, sPattern);
		ConfigurationManager.setProperty(CONF_ANIMATION_PATTERN, jtfAnimationPattern.getText());
		// Advanced
		ConfigurationManager.setProperty(CONF_BACKUP_SIZE, Integer.toString(backupSize.getValue()));
		ConfigurationManager.setProperty(CONF_COLLECTION_CHARSET, jcbCollectionEncoding
				.getSelectedItem().toString());
		ConfigurationManager.setProperty(CONF_REGEXP, Boolean.toString(jcbRegexp.isSelected()));
		ConfigurationManager.setProperty(CONF_MPLAYER_ARGS, jtfMPlayerArgs.getText());
		ConfigurationManager.setProperty(CONF_ENV_VARIABLES, jtfEnvVariables.getText());
		// If jajuk home changes, write new path in bootstrap file
		if (Main.workspace != null && !Main.workspace.equals(psJajukWorkspace.getUrl())) {
			// Check workspace directory
			if (!psJajukWorkspace.getUrl().trim().equals("")) {
				if (!new java.io.File(psJajukWorkspace.getUrl()).canRead()) {
					Messages.showErrorMessage("165");
					return;
				}
			}
			try {
				java.io.File bootstrap = new java.io.File(FILE_BOOTSTRAP);
				BufferedWriter bw = new BufferedWriter(new FileWriter(bootstrap));
				bw.write(psJajukWorkspace.getUrl());
				bw.flush();
				bw.close();

				// Request user to move the .jajuk directory and to restart
				// Jajuk
				Messages.showInfoMessage(Messages.getString("ParameterView.209"));
			} catch (Exception e) {
				Messages.showErrorMessage("024");
				Log.debug("Cannot write bootstrap file");
			}
		}

		// Network
		ConfigurationManager.setProperty(CONF_NETWORK_USE_PROXY, Boolean.toString(jcbProxy
				.isSelected()));
		ConfigurationManager.setProperty(CONF_NETWORK_PROXY_HOSTNAME, jtfProxyHostname.getText());
		ConfigurationManager.setProperty(CONF_NETWORK_PROXY_PORT, jtfProxyPort.getText());
		ConfigurationManager.setProperty(CONF_NETWORK_PROXY_LOGIN, jtfProxyLogin.getText());
		ConfigurationManager.setProperty(CONF_NETWORK_CONNECTION_TO, Integer.toString(connectionTO
				.getValue()));
		ConfigurationManager.setProperty(CONF_NETWORK_TRANSFERT_TO, Integer.toString(transfertTO
				.getValue()));
		// Covers
		ConfigurationManager.setProperty(CONF_COVERS_AUTO_COVER, Boolean.toString(jcbAutoCover
				.isSelected()));
		ConfigurationManager.setProperty(CONF_COVERS_SHUFFLE, Boolean.toString(jcbShuffleCover
				.isSelected()));
		ConfigurationManager.setProperty(CONF_COVERS_PRELOAD, Boolean.toString(jcbPreLoad
				.isSelected()));
		ConfigurationManager.setProperty(CONF_COVERS_CHANGE_AT_EACH_TRACK, Boolean
				.toString(jcbLoadEachTrack.isSelected()));
		ConfigurationManager.setProperty(CONF_COVERS_SIZE, Integer.toString(jcbCoverSize
				.getSelectedIndex()));
		// configuration
		ConfigurationManager.commit();
		// notify playlist editor (useful for novelties)
		ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
		// display a message
		InformationJPanel.getInstance().setMessage(
				Messages.getString("ParameterView.109"), InformationJPanel.INFORMATIVE); //$NON-NLS-1$
	}

	/**
	 * Set widgets to specified value in options
	 */
	private void updateSelection() {
		jtfHistory.setText(ConfigurationManager.getProperty(CONF_HISTORY));
		if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_FILE)) {
			jrbFile.setSelected(true);
			sbSearch.setEnabled(true);
		} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_LAST)) {
			jrbLast.setSelected(true);
		} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
				STARTUP_MODE_LAST_KEEP_POS)) {
			jrbLastKeepPos.setSelected(true);
		} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_NOTHING)) {
			jrbNothing.setSelected(true);
		} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_SHUFFLE)) {
			jrbShuffle.setSelected(true);
		} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(STARTUP_MODE_BESTOF)) {
			jrbBestof.setSelected(true);
		} else if (ConfigurationManager.getProperty(CONF_STARTUP_MODE).equals(
				STARTUP_MODE_NOVELTIES)) {
			jrbNovelties.setSelected(true);
		}
		// Confirmations
		jcbBeforeDelete
				.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_DELETE_FILE));
		jcbBeforeExit.setSelected(ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_EXIT));
		jcbBeforeRemoveDevice.setSelected(ConfigurationManager
				.getBoolean(CONF_CONFIRMATIONS_REMOVE_DEVICE));
		jcbBeforeDeleteCover.setSelected(ConfigurationManager
				.getBoolean(CONF_CONFIRMATIONS_DELETE_COVER));
		jcbBeforeClearingHistory.setSelected(ConfigurationManager
				.getBoolean(CONF_CONFIRMATIONS_CLEAR_HISTORY));
		jcbBeforeResetingRatings.setSelected(ConfigurationManager
				.getBoolean(CONF_CONFIRMATIONS_RESET_RATINGS));
		jcbBeforeRefactorFiles.setSelected(ConfigurationManager
				.getBoolean(CONF_CONFIRMATIONS_REFACTOR_FILES));
		// options
		boolean bHidden = ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED);
		jcbDisplayUnmounted.setSelected(bHidden);
		this.bHidden = bHidden;
		jcbDefaultActionClick.setSelected(ConfigurationManager
				.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
		jcbDefaultActionDrop.setSelected(ConfigurationManager
				.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_DROP));
		jcbShowBaloon.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_SHOW_POPUP));
		jcbHotkeys.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_HOTKEYS));

		jcbSyncTableTree.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE));
		scbLanguage.setSelectedIndex(Messages.getLocales().indexOf(
				ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE)));
		scbLanguage.addActionListener(this);
		scbLAF.setSelectedItem(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
		scbLAF.addActionListener(this);
		scbLogLevel.setSelectedIndex(Integer.parseInt(ConfigurationManager
				.getProperty(CONF_OPTIONS_LOG_LEVEL)));
		introLength.setValue(ConfigurationManager.getInt(CONF_OPTIONS_INTRO_LENGTH));
		introPosition.setValue(ConfigurationManager.getInt(CONF_OPTIONS_INTRO_BEGIN));
		jtfBestofSize.setText(ConfigurationManager.getProperty(CONF_BESTOF_SIZE));
		jtfNoveltiesAge.setText(ConfigurationManager.getProperty(CONF_OPTIONS_NOVELTIES_AGE));
		jtfVisiblePlanned.setText(ConfigurationManager.getProperty(CONF_OPTIONS_VISIBLE_PLANNED));
		crossFadeDuration.setValue(ConfigurationManager.getInt(CONF_FADE_DURATION));
		jcbShare.setSelected(ConfigurationManager.getBoolean(CONF_P2P_SHARE));
		jpfPasswd.setText(ConfigurationManager.getProperty(CONF_P2P_PASSWORD));
		jcbAddRemoteProperties.setSelected(ConfigurationManager
				.getBoolean(CONF_P2P_ADD_REMOTE_PROPERTIES));
		bHidden = ConfigurationManager.getBoolean(CONF_P2P_HIDE_LOCAL_PROPERTIES);
		jcbHideProperties.setSelected(bHidden);
		jcbUseParentDir.setSelected(ConfigurationManager.getBoolean(CONF_TAGS_USE_PARENT_DIR));
		// advanced
		int iBackupSize = ConfigurationManager.getInt(CONF_BACKUP_SIZE);
		if (iBackupSize <= 0) { // backup size =0 means no backup
			jcbBackup.setSelected(false);
			backupSize.setEnabled(false);
			jlBackupSize.setEnabled(false);
		} else {
			jcbBackup.setSelected(true);
			backupSize.setEnabled(true);
			jlBackupSize.setEnabled(true);
		}
		backupSize.setValue(iBackupSize);
		jcbCollectionEncoding.setSelectedItem(ConfigurationManager
				.getProperty(CONF_COLLECTION_CHARSET));
		jtfRefactorPattern.setText(ConfigurationManager.getProperty(CONF_REFACTOR_PATTERN));
		jtfAnimationPattern.setText(ConfigurationManager.getProperty(CONF_ANIMATION_PATTERN));
		jtfMPlayerArgs.setText(ConfigurationManager.getProperty(CONF_MPLAYER_ARGS));
		jtfEnvVariables.setText(ConfigurationManager.getProperty(CONF_ENV_VARIABLES));
		// network
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
		// Covers
		jcbAutoCover.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jlCoverSize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jcbCoverSize.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jcbCoverSize.setSelectedIndex(ConfigurationManager.getInt(CONF_COVERS_SIZE));
		jcbShuffleCover.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_SHUFFLE));
		jcbPreLoad.setSelected(ConfigurationManager.getBoolean(CONF_COVERS_PRELOAD));
		jcbPreLoad.setEnabled(ConfigurationManager.getBoolean(CONF_COVERS_AUTO_COVER));
		jcbLoadEachTrack.setSelected(ConfigurationManager
				.getBoolean(CONF_COVERS_CHANGE_AT_EACH_TRACK));
		// this mode requires shuffle mode
		jcbLoadEachTrack.setEnabled(jcbShuffleCover.isSelected() && jcbShuffleCover.isEnabled());

		jcbAudioScrobbler.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_AUDIOSCROBBLER));
		if (ConfigurationManager.getBoolean(CONF_OPTIONS_AUDIOSCROBBLER)) {
			Log.debug(ConfigurationManager.getProperty(CONF_OPTIONS_AUDIOSCROBBLER_PASSWORD));
			jtfASUser.setText(ConfigurationManager.getProperty(CONF_OPTIONS_AUDIOSCROBBLER_USER));
			jpfASPassword.setText(ConfigurationManager
					.getProperty(CONF_OPTIONS_AUDIOSCROBBLER_PASSWORD));
		} else {
			jlASUser.setEnabled(false);
			jtfASUser.setEnabled(false);
			jlASPassword.setEnabled(false);
			jpfASPassword.setEnabled(false);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			SearchResult sr = sbSearch.alResults.get(sbSearch.jlist.getSelectedIndex());
			sbSearch.setText(sr.getFile().getTrack().getName());
			ConfigurationManager.setProperty(CONF_STARTUP_FILE, sr.getFile().getId());
			sbSearch.popup.hide();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == jrbFile) { // jrbFile has been selected or
			// deselected
			sbSearch.setEnabled(jrbFile.isSelected());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		// when changing tab, store it for futur jajuk sessions
		ConfigurationManager.setProperty(CONF_OPTIONS_TAB, Integer.toString(jtpMain
				.getSelectedIndex()));
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
	 */
	public void update(Event event) {
		EventSubject subject = event.getSubject();
		if (EventSubject.EVENT_PARAMETERS_CHANGE.equals(subject)){
			updateSelection();
		}
	}

}
