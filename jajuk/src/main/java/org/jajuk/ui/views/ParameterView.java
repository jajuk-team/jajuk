/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

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
import org.jajuk.services.lastfm.LastFmManager;
import org.jajuk.ui.DefaultMouseWheelListener;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.PathSelector;
import org.jajuk.ui.PatternInputVerifier;
import org.jajuk.ui.SearchBox;
import org.jajuk.ui.SteppedComboBox;
import org.jajuk.ui.ToggleLink;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.VerticalLayout;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.theme.ThemeInfo;
import org.jvnet.substance.watermark.WatermarkInfo;

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
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * View used to set Jajuk parameters.
 * <p>
 * Configuration perspective *
 * <p>
 * Singleton
 */
public class ParameterView extends ViewAdapter implements ActionListener, ListSelectionListener,
		ItemListener, ChangeListener, Observer {

	private static final long serialVersionUID = 1L;

	/** Self instance */
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

	JCheckBox jcbAudioScrobbler;

	JLabel jlASUser;

	JTextField jtfASUser;

	JLabel jlASPassword;

	JPasswordField jpfASPassword;

	JLabel jlLanguage;

	SteppedComboBox scbLanguage;

	JLabel jlLAF;

	SteppedComboBox scbLAF;

	JLabel jlWatermarks;

	JLabel jlWatermarkImage;

	SteppedComboBox scbWatermarks;

	PathSelector pathWatermarkFile;

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

	JPanel jpP2P;

	JCheckBox jcbShare;

	JLabel jlPasswd;

	JPasswordField jpfPasswd;

	JCheckBox jcbAddRemoteProperties;

	JCheckBox jcbHideProperties;

	JPanel jpTags;

	JCheckBox jcbUseParentDir;

	JLabel jlRefactorPattern;

	JFormattedTextField jtfRefactorPattern;

	JLabel jlAnimationPattern;

	JTextField jtfAnimationPattern;

	JPanel jpAdvanced;

	JCheckBox jcbBackup;

	JSlider backupSize;

	JLabel jlCollectionEncoding;

	JComboBox jcbCollectionEncoding;

	JCheckBox jcbRegexp;

	JPanel jpNetwork;

	ButtonGroup bgProxy;

	JCheckBox jcbProxyNone;

	JCheckBox jcbProxyHttp;

	JCheckBox jcbProxySocks;

	JLabel jlProxyHostname;

	JTextField jtfProxyHostname;

	JLabel jlProxyPort;

	JTextField jtfProxyPort;

	JLabel jlProxyLogin;

	JTextField jtfProxyLogin;

	JLabel jlProxyPwd;

	JPasswordField jtfProxyPwd;

	JLabel jlConnectionTO;

	JSlider connectionTO;

	JPanel jpCovers;

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

	JLabel jlCatalogPages;

	JSlider jsCatalogPages;

	JCheckBox jcbShowCatalogPopups;

	JPanel jpUI;

	JLabel jlFonts;

	JSlider jsFonts;

	JCheckBox jcbVisibleAtStartup;

	JPanel jpLastFM;

	JPanel jpOKCancel;

	JButton jbOK;

	JButton jbDefault;

	JPanel jpModes;

	JCheckBox jcbCheckUpdates;

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
		double p = TableLayout.PREFERRED;
		int iXSeparator = 15;
		int iYSeparator = 15;

		// --History
		jpHistory = new JPanel();

		double sizeHistory[][] = { { p, p }, { p, p } };
		TableLayout layoutHistory = new TableLayout(sizeHistory);
		layoutHistory.setHGap(iXSeparator);
		layoutHistory.setVGap(iYSeparator);
		jpHistory.setLayout(layoutHistory);
		jlHistory = new JLabel(Messages.getString("ParameterView.0"));
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
		jtfHistory.setToolTipText(Messages.getString("ParameterView.2"));
		jbClearHistory = new JButton(Messages.getString("ParameterView.3"), IconLoader.ICON_CLEAR);
		jbClearHistory.setToolTipText(Messages.getString("ParameterView.4"));
		jbClearHistory.addActionListener(this);
		jbResetRatings = new JButton(Messages.getString("ParameterView.186"), IconLoader.ICON_CLEAR);
		jbResetRatings.setToolTipText(Messages.getString("ParameterView.187"));
		jbResetRatings.addActionListener(this);
		jpHistory.add(jlHistory, "0,0");
		jpHistory.add(jtfHistory, "1,0");
		jpHistory.add(jbClearHistory, "0,1");
		jpHistory.add(jbResetRatings, "1,1");

		// --Startup
		jpStart = new JPanel();
		double sizeStart[][] = { { p, p }, { p, p, p, p, p, p, p, p } };
		TableLayout layoutStartup = new TableLayout(sizeStart);
		layoutStartup.setVGap(iYSeparator);
		layoutStartup.setHGap(iXSeparator);

		jpStart.setLayout(new TableLayout(sizeStart));
		jlStart = new JLabel(Messages.getString("ParameterView.9"));
		bgStart = new ButtonGroup();
		jrbNothing = new JRadioButton(Messages.getString("ParameterView.10"));
		jrbNothing.setToolTipText(Messages.getString("ParameterView.11"));
		jrbNothing.addItemListener(this);
		jrbLast = new JRadioButton(Messages.getString("ParameterView.12"));
		jrbLast.setToolTipText(Messages.getString("ParameterView.13"));
		jrbLast.addItemListener(this);
		jrbLastKeepPos = new JRadioButton(Messages.getString("ParameterView.135"));
		jrbLastKeepPos.setToolTipText(Messages.getString("ParameterView.136"));
		jrbLastKeepPos.addItemListener(this);
		jrbShuffle = new JRadioButton(Messages.getString("ParameterView.14"));
		jrbShuffle.setToolTipText(Messages.getString("ParameterView.15"));
		jrbShuffle.addItemListener(this);
		jrbBestof = new JRadioButton(Messages.getString("ParameterView.131"));
		jrbBestof.setToolTipText(Messages.getString("ParameterView.132"));
		jrbBestof.addItemListener(this);
		jrbNovelties = new JRadioButton(Messages.getString("ParameterView.133"));
		jrbNovelties.setToolTipText(Messages.getString("ParameterView.134"));
		jrbNovelties.addItemListener(this);
		jrbFile = new JRadioButton(Messages.getString("ParameterView.16"));
		jrbFile.setToolTipText(Messages.getString("ParameterView.17"));
		jrbFile.addItemListener(this);
		sbSearch = new SearchBox(this);
		// disabled by default, is enabled only if jrbFile is enabled
		sbSearch.setEnabled(false);
		// set chosen track in file selection
		String sFileId = ConfigurationManager.getProperty(CONF_STARTUP_FILE);
		if (!"".equals(sFileId)) {
			File file = FileManager.getInstance().getFileByID(sFileId);
			if (file != null) {
				sbSearch.setText(file.getTrack().getName());
			} else {
				// the file exists no more, remove its id as startup file
				ConfigurationManager.setProperty(CONF_STARTUP_FILE, "");
			}
		}
		sbSearch.setToolTipText(Messages.getString("ParameterView.18"));
		bgStart.add(jrbNothing);
		bgStart.add(jrbLast);
		bgStart.add(jrbLastKeepPos);
		bgStart.add(jrbShuffle);
		bgStart.add(jrbBestof);
		bgStart.add(jrbNovelties);
		bgStart.add(jrbFile);
		jpStart.add(jlStart, "0,0,1,0");
		jpStart.add(jrbNothing, "0,1,1,1");
		jpStart.add(jrbLast, "0,2,1,2");
		jpStart.add(jrbLastKeepPos, "0,3,1,3");
		jpStart.add(jrbShuffle, "0,4,1,4");
		jpStart.add(jrbBestof, "0,5,1,5");
		jpStart.add(jrbNovelties, "0,6,1,6");
		jpStart.add(jrbFile, "0,7");
		jpStart.add(sbSearch, "1,7");

		// --Confirmations
		jpConfirmations = new JPanel();
		double sizeConfirmations[][] = { { p }, { p, p, p, p, p, p, p } };

		TableLayout layoutConfirmation = new TableLayout(sizeConfirmations);
		layoutConfirmation.setVGap(iYSeparator);
		layoutConfirmation.setHGap(iXSeparator);
		jpConfirmations.setLayout(layoutConfirmation);

		jcbBeforeDelete = new JCheckBox(Messages.getString("ParameterView.27"));

		jcbBeforeDelete.setToolTipText(Messages.getString("ParameterView.28"));

		jcbBeforeExit = new JCheckBox(Messages.getString("ParameterView.29"));
		jcbBeforeExit.setToolTipText(Messages.getString("ParameterView.30"));

		jcbBeforeRemoveDevice = new JCheckBox(Messages.getString("ParameterView.164"));
		jcbBeforeRemoveDevice.setToolTipText(Messages.getString("ParameterView.165"));

		jcbBeforeDeleteCover = new JCheckBox(Messages.getString("ParameterView.171"));
		jcbBeforeDeleteCover.setToolTipText(Messages.getString("ParameterView.172"));

		jcbBeforeClearingHistory = new JCheckBox(Messages.getString("ParameterView.188"));
		jcbBeforeClearingHistory.setToolTipText(Messages.getString("ParameterView.188"));

		jcbBeforeResetingRatings = new JCheckBox(Messages.getString("ParameterView.189"));
		jcbBeforeResetingRatings.setToolTipText(Messages.getString("ParameterView.189"));

		jcbBeforeRefactorFiles = new JCheckBox(Messages.getString("ParameterView.194"));
		jcbBeforeRefactorFiles.setToolTipText(Messages.getString("ParameterView.194"));

		jpConfirmations.add(jcbBeforeDelete, "0,0");
		jpConfirmations.add(jcbBeforeExit, "0,1");
		jpConfirmations.add(jcbBeforeRemoveDevice, "0,2");
		jpConfirmations.add(jcbBeforeDeleteCover, "0,3");
		jpConfirmations.add(jcbBeforeClearingHistory, "0,4");
		jpConfirmations.add(jcbBeforeResetingRatings, "0,5");
		jpConfirmations.add(jcbBeforeRefactorFiles, "0,6");

		// -Modes
		jpModes = new JPanel();
		// Intro
		// intro position
		jlIntroPosition = new JLabel(Messages.getString("ParameterView.59"));
		introPosition = new JSlider(0, 100, 0);
		introPosition.setMajorTickSpacing(20);
		introPosition.setMinorTickSpacing(10);
		introPosition.setPaintTicks(true);
		introPosition.setPaintLabels(true);
		introPosition.setToolTipText(Messages.getString("ParameterView.60"));
		introPosition.addMouseWheelListener(new DefaultMouseWheelListener(introPosition));

		// intro length
		jlIntroLength = new JLabel(Messages.getString("ParameterView.61"));
		introLength = new JSlider(0, 30, 20);
		introLength.setMajorTickSpacing(10);
		introLength.setMinorTickSpacing(1);
		introLength.setPaintTicks(true);
		introLength.setPaintLabels(true);
		introLength.setToolTipText(Messages.getString("ParameterView.110"));
		introLength.addMouseWheelListener(new DefaultMouseWheelListener(introLength));

		// best of size
		jlBestofSize = new JLabel(Messages.getString("ParameterView.111"));
		jlBestofSize.setToolTipText(Messages.getString("ParameterView.112"));
		jtfBestofSize = new JTextField(3);
		jtfBestofSize.setToolTipText(Messages.getString("ParameterView.112"));
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
		jlNoveltiesAge = new JLabel(Messages.getString("ParameterView.129"));
		jlNoveltiesAge.setToolTipText(Messages.getString("ParameterView.130"));
		jtfNoveltiesAge = new JTextField(3);
		jtfNoveltiesAge.setToolTipText(Messages.getString("ParameterView.130"));
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
		jlVisiblePlanned = new JLabel(Messages.getString("ParameterView.177"));
		jlVisiblePlanned.setToolTipText(Messages.getString("ParameterView.178"));
		jtfVisiblePlanned = new JTextField(3);
		jtfVisiblePlanned.setToolTipText(Messages.getString("ParameterView.178"));
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
		jlCrossFadeDuration = new JLabel(Messages.getString("ParameterView.190"));
		jlCrossFadeDuration.setToolTipText(Messages.getString("ParameterView.191"));
		crossFadeDuration = new JSlider(0, 30, 0);
		crossFadeDuration.setMajorTickSpacing(10);
		crossFadeDuration.setMinorTickSpacing(1);
		crossFadeDuration.setPaintTicks(true);
		crossFadeDuration.setPaintLabels(true);
		crossFadeDuration.setToolTipText(Messages.getString("ParameterView.191"));
		crossFadeDuration.addMouseWheelListener(new DefaultMouseWheelListener(crossFadeDuration));

		// add panels
		double sizeIntro[][] = { { p, p }, { p, p, p, p, p, p } };
		TableLayout layoutModes = new TableLayout(sizeIntro);
		layoutModes.setVGap(iYSeparator);
		layoutModes.setHGap(iXSeparator);
		jpModes.setLayout(layoutModes);
		jpModes.add(jlIntroPosition, "0,0");
		jpModes.add(introPosition, "1,0");
		jpModes.add(jlIntroLength, "0,1");
		jpModes.add(introLength, "1,1");
		jpModes.add(jlCrossFadeDuration, "0,2");
		jpModes.add(crossFadeDuration, "1,2");
		jpModes.add(jlBestofSize, "0,3");
		jpModes.add(jtfBestofSize, "1,3");
		jpModes.add(jlNoveltiesAge, "0,4");
		jpModes.add(jtfNoveltiesAge, "1,4");
		jpModes.add(jlVisiblePlanned, "0,5");
		jpModes.add(jtfVisiblePlanned, "1,5");

		// --Options
		jpOptions = new JPanel();
		jcbDisplayUnmounted = new JCheckBox(Messages.getString("ParameterView.34"));
		jcbDisplayUnmounted.setToolTipText(Messages.getString("ParameterView.35"));

		jcbSyncTableTree = new JCheckBox(Messages.getString("ParameterView.183"));
		jcbSyncTableTree.setToolTipText(Messages.getString("ParameterView.184"));

		jcbDefaultActionClick = new JCheckBox(Messages.getString("ParameterView.179"));
		jcbDefaultActionClick.setToolTipText(Messages.getString("ParameterView.180"));

		jcbDefaultActionDrop = new JCheckBox(Messages.getString("ParameterView.181"));
		jcbDefaultActionDrop.setToolTipText(Messages.getString("ParameterView.182"));

		jcbHotkeys = new JCheckBox(Messages.getString("ParameterView.196"));
		jcbHotkeys.addActionListener(this);
		jcbHotkeys.setToolTipText(Messages.getString("ParameterView.197"));
		// Disable this option if not under windows
		jcbHotkeys.setEnabled(Util.isUnderWindows());

		jlLanguage = new JLabel(Messages.getString("ParameterView.38"));
		scbLanguage = new SteppedComboBox();
		Iterator<String> itDescs = Messages.getDescs().iterator();
		while (itDescs.hasNext()) {
			String sDesc = itDescs.next();
			scbLanguage.addItem(Messages.getString(sDesc));
		}
		scbLanguage.setToolTipText(Messages.getString("ParameterView.42"));
		JPanel language = new JPanel(new HorizontalLayout(iXSeparator));
		language.add(jlLanguage);
		language.add(scbLanguage);

		double sizeOptions[][] = { { p }, { p, p, p, p, p, 20 } };
		TableLayout layoutOption = new TableLayout(sizeOptions);
		layoutOption.setHGap(iXSeparator);
		layoutOption.setVGap(iYSeparator);
		jpOptions.setLayout(layoutOption);

		jpOptions.add(jcbDisplayUnmounted, "0,0");
		jpOptions.add(jcbDefaultActionClick, "0,1");
		jpOptions.add(jcbDefaultActionDrop, "0,2");
		jpOptions.add(jcbSyncTableTree, "0,3");
		jpOptions.add(jcbHotkeys, "0,4");
		jpOptions.add(language, "0,5");

		// --P2P
		jpP2P = new JPanel();
		double sizeP2P[][] = { { 0.6, 0.3, 0.1 },
				{ 20, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator, 20, iYSeparator } };
		jpP2P.setLayout(new TableLayout(sizeP2P));
		jcbShare = new JCheckBox(Messages.getString("ParameterView.72"));
		jcbShare.setEnabled(false); // TBI
		jcbShare.setToolTipText(Messages.getString("ParameterView.73"));
		jlPasswd = new JLabel(Messages.getString("ParameterView.74"));
		jlPasswd.setEnabled(false); // TBI
		jpfPasswd = new JPasswordField();
		jpfPasswd.setEnabled(false); // TBI
		jpfPasswd.setToolTipText(Messages.getString("ParameterView.75"));
		jcbAddRemoteProperties = new JCheckBox(Messages.getString("ParameterView.76"));
		jcbAddRemoteProperties.setEnabled(false); // TBI
		jcbAddRemoteProperties.setToolTipText(Messages.getString("ParameterView.77"));
		jcbHideProperties = new JCheckBox(Messages.getString("ParameterView.78"));
		jcbHideProperties.setToolTipText(Messages.getString("ParameterView.79"));
		jcbHideProperties.setEnabled(false); // TBI
		jpP2P.add(jcbShare, "0,1");
		jpP2P.add(jlPasswd, "0,3");
		jpP2P.add(jpfPasswd, "1,3");
		jpP2P.add(jcbAddRemoteProperties, "0,5");
		jpP2P.add(jcbHideProperties, "0,7");

		// --Tags
		jpTags = new JPanel();
		double sizeTags[][] = { { p, p }, { p, p, p } };
		TableLayout layoutTags = new TableLayout(sizeTags);
		layoutTags.setHGap(iXSeparator);
		layoutTags.setVGap(iYSeparator);
		jpTags.setLayout(layoutOption);
		jcbUseParentDir = new JCheckBox(Messages.getString("ParameterView.101"));
		jcbUseParentDir.setToolTipText(Messages.getString("ParameterView.102"));
		jlRefactorPattern = new JLabel(Messages.getString("ParameterView.192"));
		jlRefactorPattern.setToolTipText(Messages.getString("ParameterView.193"));
		jtfRefactorPattern = new JFormattedTextField();
		jtfRefactorPattern.setToolTipText(Messages.getString("ParameterView.193"));
		jtfRefactorPattern.setInputVerifier(new PatternInputVerifier());
		jlAnimationPattern = new JLabel(Messages.getString("ParameterView.195"));
		jlAnimationPattern.setToolTipText(Messages.getString("ParameterView.193"));
		jtfAnimationPattern = new JTextField();
		jtfAnimationPattern.setToolTipText(Messages.getString("ParameterView.193"));
		jpTags.add(jcbUseParentDir, "0,0");
		jpTags.add(jlRefactorPattern, "0,1");
		jpTags.add(jtfRefactorPattern, "1,1");
		jpTags.add(jlAnimationPattern, "0,2");
		jpTags.add(jtfAnimationPattern, "1,2");

		// --Advanced
		jpAdvanced = new JPanel();
		jcbBackup = new JCheckBox(Messages.getString("ParameterView.116"));
		jcbBackup.addActionListener(this);
		jcbBackup.setToolTipText(Messages.getString("ParameterView.117"));
		backupSize = new JSlider(0, 100);
		backupSize.setMajorTickSpacing(10);
		backupSize.setMinorTickSpacing(10);
		backupSize.setPaintTicks(true);
		backupSize.setPaintLabels(true);
		backupSize.setToolTipText(Messages.getString("ParameterView.119"));
		backupSize.addMouseWheelListener(new DefaultMouseWheelListener(backupSize));
		jlCollectionEncoding = new JLabel(Messages.getString("ParameterView.120"));
		jlCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121"));
		jcbCollectionEncoding = new JComboBox();
		jcbCollectionEncoding.setToolTipText(Messages.getString("ParameterView.121"));
		jcbRegexp = new JCheckBox(Messages.getString("ParameterView.113"));
		jcbRegexp.setSelected(ConfigurationManager.getBoolean(CONF_REGEXP));
		jcbRegexp.setToolTipText(Messages.getString("ParameterView.114"));
		jcbCollectionEncoding.addItem("UTF-8");
		jcbCollectionEncoding.addItem("UTF-16");
		jlLogLevel = new JLabel(Messages.getString("ParameterView.46"));
		scbLogLevel = new SteppedComboBox();
		scbLogLevel.addItem(Messages.getString("ParameterView.47"));
		scbLogLevel.addItem(Messages.getString("ParameterView.48"));
		scbLogLevel.addItem(Messages.getString("ParameterView.49"));
		scbLogLevel.addItem(Messages.getString("ParameterView.50"));
		scbLogLevel.addItem(Messages.getString("ParameterView.51"));
		scbLogLevel.setToolTipText(Messages.getString("ParameterView.52"));
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
		jcbCheckUpdates = new JCheckBox(Messages.getString("ParameterView.234"));
		jcbCheckUpdates.setToolTipText(Messages.getString("ParameterView.234"));
		jcbCheckUpdates.setSelected(ConfigurationManager.getBoolean(CONF_CHECK_FOR_UPDATE));

		double sizeAdvanced[][] = { { p, p }, { p, p, p, p, p, p, p, p } };
		TableLayout layoutAdvanced = new TableLayout(sizeAdvanced);
		layoutAdvanced.setHGap(iXSeparator);
		layoutAdvanced.setVGap(iYSeparator);
		jpAdvanced.setLayout(layoutAdvanced);
		jpAdvanced.add(jcbRegexp, "0,0");
		jpAdvanced.add(jcbBackup, "0,1");
		jpAdvanced.add(backupSize, "1,1");
		jpAdvanced.add(jlCollectionEncoding, "0,2");
		jpAdvanced.add(jcbCollectionEncoding, "1,2");
		jpAdvanced.add(jlLogLevel, "0,3");
		jpAdvanced.add(scbLogLevel, "1,3");
		jpAdvanced.add(jlMPlayerArgs, "0,4");
		jpAdvanced.add(jtfMPlayerArgs, "1,4");
		jpAdvanced.add(jlEnvVariables, "0,5");
		jpAdvanced.add(jtfEnvVariables, "1,5");
		jpAdvanced.add(jlJajukWorkspace, "0,6");
		jpAdvanced.add(psJajukWorkspace, "1,6");
		jpAdvanced.add(jcbCheckUpdates, "0,7");

		// - Network
		jpNetwork = new JPanel();
		double sizeNetwork[][] = { { p, p }, { p, p, p, p, p, p, p, p } };
		TableLayout layoutNetwork = new TableLayout(sizeNetwork);
		layoutNetwork.setHGap(iXSeparator);
		layoutNetwork.setVGap(iYSeparator);
		jpNetwork.setLayout(layoutNetwork);
		bgProxy = new ButtonGroup();
		jcbProxyNone = new JCheckBox(Messages.getString("ParameterView.236"));
		jcbProxyNone.setToolTipText(Messages.getString("ParameterView.236"));
		jcbProxyNone.addActionListener(this);
		jcbProxyHttp = new JCheckBox(Messages.getString("ParameterView.237"));
		jcbProxyHttp.setToolTipText(Messages.getString("ParameterView.237"));
		jcbProxyHttp.addActionListener(this);
		jcbProxySocks = new JCheckBox(Messages.getString("ParameterView.238"));
		jcbProxySocks.setToolTipText(Messages.getString("ParameterView.238"));
		jcbProxySocks.addActionListener(this);
		bgProxy.add(jcbProxyNone);
		bgProxy.add(jcbProxyHttp);
		bgProxy.add(jcbProxySocks);
		jlProxyHostname = new JLabel(Messages.getString("ParameterView.144"));
		jlProxyHostname.setToolTipText(Messages.getString("ParameterView.145"));
		jtfProxyHostname = new JTextField();
		jtfProxyHostname.setToolTipText(Messages.getString("ParameterView.145"));
		jlProxyPort = new JLabel(Messages.getString("ParameterView.146"));
		jlProxyPort.setToolTipText(Messages.getString("ParameterView.147"));
		jtfProxyPort = new JTextField();
		jtfProxyPort.setToolTipText(Messages.getString("ParameterView.147"));
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
		jlProxyLogin = new JLabel(Messages.getString("ParameterView.142"));
		jlProxyLogin.setToolTipText(Messages.getString("ParameterView.143"));
		jtfProxyLogin = new JTextField();
		jtfProxyLogin.setToolTipText(Messages.getString("ParameterView.143"));
		jlProxyPwd = new JLabel(Messages.getString("ParameterView.239"));
		jlProxyPwd.setToolTipText(Messages.getString("ParameterView.239"));
		jtfProxyPwd = new JPasswordField();
		jtfProxyPwd.setToolTipText(Messages.getString("ParameterView.239"));

		jlConnectionTO = new JLabel(Messages.getString("ParameterView.160"));
		jlConnectionTO.setToolTipText(Messages.getString("ParameterView.160"));
		connectionTO = new JSlider(0, 60);
		connectionTO.setMajorTickSpacing(10);
		connectionTO.setMinorTickSpacing(5);
		connectionTO.setPaintTicks(true);
		connectionTO.setPaintLabels(true);
		connectionTO.setToolTipText(Messages.getString("ParameterView.161"));
		connectionTO.addMouseWheelListener(new DefaultMouseWheelListener(connectionTO));
		// Add items
		jpNetwork.add(jcbProxyNone, "0,0");
		jpNetwork.add(jcbProxyHttp, "0,1");
		jpNetwork.add(jcbProxySocks, "0,2");
		jpNetwork.add(jlProxyHostname, "0,3");
		jpNetwork.add(jtfProxyHostname, "1,3");
		jpNetwork.add(jlProxyPort, "0,4");
		jpNetwork.add(jtfProxyPort, "1,4");
		jpNetwork.add(jlProxyLogin, "0,5");
		jpNetwork.add(jtfProxyLogin, "1,5");
		jpNetwork.add(jlProxyPwd, "0,6");
		jpNetwork.add(jtfProxyPwd, "1,6");
		jpNetwork.add(jlConnectionTO, "0,7");
		jpNetwork.add(connectionTO, "1,7");

		// - Last.FM
		jpLastFM = new JPanel();
		double sizeLastFM[][] = { { p, 200 }, { p, p, p } };
		TableLayout layoutLastFM = new TableLayout(sizeLastFM);
		layoutLastFM.setHGap(iXSeparator);
		layoutLastFM.setVGap(iYSeparator);
		jpLastFM.setLayout(layoutLastFM);
		jcbAudioScrobbler = new JCheckBox(Messages.getString("ParameterView.199"));
		jcbAudioScrobbler.setToolTipText(Messages.getString("ParameterView.200"));
		jcbAudioScrobbler.addActionListener(this);
		jlASUser = new JLabel(Messages.getString("ParameterView.201"));
		jtfASUser = new JTextField();
		jtfASUser.setToolTipText(Messages.getString("ParameterView.202"));
		jlASPassword = new JLabel(Messages.getString("ParameterView.203"));
		jpfASPassword = new JPasswordField();
		jpfASPassword.setToolTipText(Messages.getString("ParameterView.204"));
		// Add items
		jpLastFM.add(jcbAudioScrobbler, "0,0");
		jpLastFM.add(jlASUser, "0,1");
		jpLastFM.add(jtfASUser, "1,1");
		jpLastFM.add(jlASPassword, "0,2");
		jpLastFM.add(jpfASPassword, "1,2");

		// - Cover
		jpCovers = new JPanel();
		double sizeCover[][] = { { p, p }, { p, p, p, p } };
		TableLayout layoutCover = new TableLayout(sizeCover);
		layoutCover.setVGap(iYSeparator);
		layoutCover.setHGap(iXSeparator);
		jpCovers.setLayout(layoutCover);
		jcbAutoCover = new JCheckBox(Messages.getString("ParameterView.148"));
		jcbAutoCover.setToolTipText(Messages.getString("ParameterView.149"));
		jcbAutoCover.addActionListener(this);
		jcbShuffleCover = new JCheckBox(Messages.getString("ParameterView.166"));
		jcbShuffleCover.setToolTipText(Messages.getString("ParameterView.167"));
		jcbShuffleCover.addActionListener(this);
		jcbPreLoad = new JCheckBox(Messages.getString("ParameterView.169"));
		jcbPreLoad.setToolTipText(Messages.getString("ParameterView.170"));
		jcbLoadEachTrack = new JCheckBox(Messages.getString("ParameterView.175"));
		jcbLoadEachTrack.setToolTipText(Messages.getString("ParameterView.176"));
		jlCoverSize = new JLabel(Messages.getString("ParameterView.150"));
		jlCoverSize.setToolTipText(Messages.getString("ParameterView.151"));
		jcbCoverSize = new JComboBox();
		jcbCoverSize.setToolTipText(Messages.getString("ParameterView.151"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.211"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.212"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.213"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.214"));
		jcbCoverSize.addItem(Messages.getString("ParameterView.215"));
		// Add items
		jpCovers.add(jcbShuffleCover, "0,0");
		jpCovers.add(jcbLoadEachTrack, "1,0");
		jpCovers.add(jcbAutoCover, "0,1");
		jpCovers.add(jcbPreLoad, "0,2");
		jpCovers.add(jlCoverSize, "0,3");
		jpCovers.add(jcbCoverSize, "1,3");

		// -- User interface --
		jpUI = new JPanel();
		double sizeUI[][] = { { p, p }, { p, p, p, p, p, p, p, p } };
		TableLayout layoutUI = new TableLayout(sizeUI);
		layoutUI.setHGap(iXSeparator);
		layoutUI.setVGap(iYSeparator);
		jpUI.setLayout(layoutUI);
		// Catalog view
		jlCatalogPages = new JLabel(Messages.getString("ParameterView.221"));
		jlCatalogPages.setToolTipText(Messages.getString("ParameterView.222"));
		jsCatalogPages = new JSlider(0, 1000, ConfigurationManager.getInt(CONF_CATALOG_PAGE_SIZE));
		jsCatalogPages.setMinorTickSpacing(100);
		jsCatalogPages.setMajorTickSpacing(200);
		jsCatalogPages.setPaintTicks(true);
		jsCatalogPages.setPaintLabels(true);
		jsCatalogPages.setToolTipText(Messages.getString("ParameterView.222"));
		jcbShowCatalogPopups = new JCheckBox(Messages.getString("ParameterView.228"));
		JXCollapsiblePane catalogView = new JXCollapsiblePane();
		catalogView.setLayout(new VerticalLayout(10));
		catalogView.setCollapsed(true);
		ToggleLink toggle = new ToggleLink(Messages.getString("ParameterView.229"), catalogView);
		JPanel jpCatalogSize = new JPanel();
		jpCatalogSize.setLayout(new HorizontalLayout());
		jpCatalogSize.add(jlCatalogPages);
		jpCatalogSize.add(jsCatalogPages);
		catalogView.add(jpCatalogSize);
		catalogView.add(jcbShowCatalogPopups);

		// Font selector
		jlFonts = new JLabel(Messages.getString("ParameterView.223"));
		jsFonts = new JSlider(6, 20, ConfigurationManager.getInt(CONF_FONTS_SIZE));
		jsFonts.setSnapToTicks(true);
		jsFonts.setMajorTickSpacing(2);
		jsFonts.setMinorTickSpacing(1);
		jsFonts.setPaintTicks(true);
		jsFonts.setPaintLabels(true);
		jsFonts.setToolTipText(Messages.getString("ParameterView.224"));
		// Visible at startup
		jcbVisibleAtStartup = new JCheckBox(Messages.getString("JajukWindow.8"));
		jcbVisibleAtStartup.setToolTipText(Messages.getString("JajukWindow.25"));
		// Use this common action listener for UI options that need to launch
		// event
		ActionListener alUI = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Store configuration
				ConfigurationManager.setProperty(CONF_UI_SHOW_AT_STARTUP, Boolean
						.toString(jcbVisibleAtStartup.isSelected()));
				ConfigurationManager.setProperty(CONF_UI_SHOW_BALLOON, Boolean
						.toString(jcbShowBaloon.isSelected()));
				// Launch an event that can be trapped by the tray to
				// synchronize the state
				Properties details = new Properties();
				details.put(DETAIL_ORIGIN, ParameterView.this);
				ObservationManager.notify(new Event(EventSubject.EVENT_PARAMETERS_CHANGE, details));
			}

		};
		jcbVisibleAtStartup.addActionListener(alUI);
		// Show Balloon
		jcbShowBaloon = new JCheckBox(Messages.getString("ParameterView.185"));
		jcbShowBaloon.setToolTipText(Messages.getString("ParameterView.185"));
		jcbShowBaloon.addActionListener(alUI);
		// LaF
		jlLAF = new JLabel(Messages.getString("ParameterView.43"));
		jlLAF.setToolTipText(Messages.getString("ParameterView.44"));
		scbLAF = new SteppedComboBox();
		Map<String, ThemeInfo> map = SubstanceLookAndFeel.getAllThemes();
		// Use a tree set to sort themes alphabeticaly
		TreeSet<String> themes = new TreeSet<String>(map.keySet());
		// Add each theme to the combo box
		for (String theme : themes) {
			scbLAF.addItem(theme);
		}
		scbLAF.setToolTipText(Messages.getString("ParameterView.44"));
		scbLAF.addActionListener(this);
		// Watermarks
		jlWatermarks = new JLabel(Messages.getString("ParameterView.230"));
		jlWatermarks.setToolTipText(Messages.getString("ParameterView.231"));
		jlWatermarkImage = new JLabel(Messages.getString("ParameterView.232"));
		jlWatermarkImage.setToolTipText(Messages.getString("ParameterView.232"));
		scbWatermarks = new SteppedComboBox();
		Map<String, WatermarkInfo> mapWatermarks = SubstanceLookAndFeel.getAllWatermarks();
		// Use a tree set to sort watermarks alphabeticaly
		TreeSet<String> watermarks = new TreeSet<String>(mapWatermarks.keySet());
		// Add image watermark that is not included by default for unknown
		// reason
		watermarks.add(LNF_WATERMARK_IMAGE);
		// Add each watermark to the combo box
		for (String watermark : watermarks) {
			scbWatermarks.addItem(watermark);
		}
		scbWatermarks.setToolTipText(Messages.getString("ParameterView.231"));
		scbWatermarks.addActionListener(this);
		// Watermark file selection
		JajukFileFilter filter = new JajukFileFilter(JajukFileFilter.ImageFilter.getInstance());
		filter.setAcceptDirectories(true);
		pathWatermarkFile = new PathSelector(filter, ConfigurationManager
				.getProperty(CONF_OPTIONS_WATERMARK_IMAGE)) {

			private static final long serialVersionUID = 1L;

			public void performOnURLChange() {
				ConfigurationManager.setProperty(CONF_OPTIONS_WATERMARK_IMAGE, pathWatermarkFile
						.getUrl());
				Util.setWatermark((String) scbWatermarks.getSelectedItem());
			}
		};

		// Add items
		jpUI.add(jcbVisibleAtStartup, "0,0");
		jpUI.add(jcbShowBaloon, "0,1");
		jpUI.add(jlFonts, "0,2");
		jpUI.add(jsFonts, "1,2");
		jpUI.add(jlLAF, "0,3");
		jpUI.add(scbLAF, "1,3");
		jpUI.add(jlWatermarks, "0,4");
		jpUI.add(scbWatermarks, "1,4");
		jpUI.add(jlWatermarkImage, "0,5");
		jpUI.add(pathWatermarkFile, "1,5");
		jpUI.add(toggle, "0,6");
		jpUI.add(catalogView, "0,7,1,7");

		// --OK/cancel panel
		Dimension dim = new Dimension(200, 20);
		jpOKCancel = new JPanel();
		jpOKCancel.setLayout(new FlowLayout());
		jbOK = new JButton(Messages.getString("ParameterView.85"), IconLoader.ICON_OK);
		jbOK.setPreferredSize(dim);
		jbOK.addActionListener(this);
		jpOKCancel.add(jbOK);
		jbDefault = new JButton(Messages.getString("ParameterView.86"),
				IconLoader.ICON_DEFAULTS_BIG);
		jbDefault.setPreferredSize(dim);
		jbDefault.addActionListener(this);
		jpOKCancel.add(jbDefault);

		// --Global layout
		double size[][] = { { 0.99 }, { 0.9, 0.10 } };
		setLayout(new TableLayout(size));
		// add main panels
		jtpMain = new JTabbedPane(JTabbedPane.LEFT);
		jtpMain.addTab(Messages.getString("ParameterView.33"), new JScrollPane(jpOptions));
		jtpMain.addTab(Messages.getString("ParameterView.226"), new JScrollPane(jpModes));
		jtpMain.addTab(Messages.getString("ParameterView.225"), new JScrollPane(jpUI));
		jtpMain.addTab(Messages.getString("ParameterView.19"), new JScrollPane(jpStart));
		jtpMain.addTab(Messages.getString("ParameterView.98"), new JScrollPane(jpTags));
		jtpMain.addTab(Messages.getString("ParameterView.8"), new JScrollPane(jpHistory));
		// TODO jtpMain.addTab(Messages.getString("ParameterView.71"),jpP2P);
		jtpMain.addTab(Messages.getString("ParameterView.235"), new JScrollPane(jpLastFM));
		jtpMain.addTab(Messages.getString("ParameterView.159"), new JScrollPane(jpCovers));
		jtpMain.addTab(Messages.getString("ParameterView.26"), new JScrollPane(jpConfirmations));
		jtpMain.addTab(Messages.getString("ParameterView.139"), new JScrollPane(jpNetwork));
		jtpMain.addTab(Messages.getString("ParameterView.115"), new JScrollPane(jpAdvanced));
		try {
			// Reload stored selected index
			jtpMain.setSelectedIndex(ConfigurationManager.getInt(CONF_OPTIONS_TAB));
		} catch (Exception e) {
			// an error can occur if a new release brings or remove tabs
			Log.error(e);
			jtpMain.setSelectedIndex(0);
		}
		jtpMain.addChangeListener(this);
		add(jtpMain, "0,0");
		add(jpOKCancel, "0,1");
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
		return Messages.getString("ParameterView.87");
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
						int iResu = Messages.getChoice(Messages
								.getString("Confirmation_clear_history"),
								JOptionPane.WARNING_MESSAGE);
						if (iResu != JOptionPane.YES_OPTION) {
							return;
						}
					}
					ObservationManager.notify(new Event(EventSubject.EVENT_CLEAR_HISTORY));
				} else if (e.getSource() == jbResetRatings) {
					// show confirmation message if required
					if (ConfigurationManager.getBoolean(CONF_CONFIRMATIONS_RESET_RATINGS)) {
						int iResu = Messages.getChoice(Messages
								.getString("Confirmation_reset_ratings"),
								JOptionPane.WARNING_MESSAGE);
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
						Messages.showErrorMessage(120);
					}
				} else if (e.getSource() == jcbShuffleCover) {
					jcbLoadEachTrack.setEnabled(jcbShuffleCover.isSelected());
				} else if (e.getSource() == jbOK) {
					applyParameters();
				} else if (e.getSource() == jbDefault) {
					ConfigurationManager.setDefaultProperties();
					updateSelection();// update UI
					InformationJPanel.getInstance().setMessage(
							Messages.getString("ParameterView.110"), InformationJPanel.INFORMATIVE);
					applyParameters();
				} else if (e.getSource() == jcbBackup) {
					// if backup option is unchecked, reset backup size
					if (jcbBackup.isSelected()) {
						backupSize.setEnabled(true);
						backupSize.setValue(ConfigurationManager.getInt(CONF_BACKUP_SIZE));
					} else {
						backupSize.setEnabled(false);
						backupSize.setValue(0);
					}
				} else if (e.getSource() == jcbProxyNone || e.getSource() == jcbProxyHttp
						|| e.getSource() == jcbProxySocks) {
					boolean bUseProxy = !jcbProxyNone.isSelected();
					jtfProxyHostname.setEnabled(bUseProxy);
					jtfProxyPort.setEnabled(bUseProxy);
					jtfProxyLogin.setEnabled(bUseProxy);
					jtfProxyPwd.setEnabled(bUseProxy);
					jlProxyHostname.setEnabled(bUseProxy);
					jlProxyPort.setEnabled(bUseProxy);
					jlProxyLogin.setEnabled(bUseProxy);
					jlProxyPwd.setEnabled(bUseProxy);
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
					ConfigurationManager.setProperty(CONF_AUDIOSCROBBLER_ENABLE, Boolean
							.toString(jcbAudioScrobbler.isSelected()));
				} else if (e.getSource() == scbLAF) {
					String oldTheme = ConfigurationManager.getProperty(CONF_OPTIONS_LNF);
					ConfigurationManager.setProperty(CONF_OPTIONS_LNF, (String) scbLAF
							.getSelectedItem());
					if (!oldTheme.equals(scbLAF.getSelectedItem())) {
						Util.setLookAndFeel((String) scbLAF.getSelectedItem());
						// refresh all components
						Util.updateAllUIs();
						Messages.showHideableWarningMessage(
								Messages.getString("ParameterView.233"),
								CONF_NOT_SHOW_AGAIN_LAF_CHANGE);
					}
				} else if (e.getSource() == scbWatermarks) {
					String oldWatermark = ConfigurationManager.getProperty(CONF_OPTIONS_WATERMARK);
					ConfigurationManager.setProperty(CONF_OPTIONS_WATERMARK, (String) scbWatermarks
							.getSelectedItem());
					String watermark = (String) scbWatermarks.getSelectedItem();
					// Enable image selection if image watermark
					jlWatermarkImage.setEnabled(watermark.equals(LNF_WATERMARK_IMAGE));
					pathWatermarkFile.setEnabled(watermark.equals(LNF_WATERMARK_IMAGE));
					if (!oldWatermark.equals(watermark)) {
						Util.setWatermark(watermark);
						// refresh all components
						Util.updateAllUIs();
						Messages.showHideableWarningMessage(
								Messages.getString("ParameterView.233"),
								CONF_NOT_SHOW_AGAIN_LAF_CHANGE);
					}
				} else if (e.getSource() == scbLanguage) {
					String sLocal = Messages.getLocales().get(scbLanguage.getSelectedIndex());
					String sPreviousLocal = Messages.getInstance().getLocale();
					if (!sPreviousLocal.equals(sLocal)) {
						// local has changed
						ConfigurationManager.setProperty(CONF_OPTIONS_LANGUAGE, sLocal);
						Messages.showInfoMessage(Messages.getString("ParameterView.198"));
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
		ConfigurationManager.setProperty(CONF_OPTIONS_HOTKEYS, Boolean.toString(jcbHotkeys
				.isSelected()));
		ConfigurationManager.setProperty(CONF_AUDIOSCROBBLER_ENABLE, Boolean
				.toString(jcbAudioScrobbler.isSelected()));
		if (jcbAudioScrobbler.isSelected()) {
			ConfigurationManager.setProperty(CONF_AUDIOSCROBBLER_USER, jtfASUser.getText());
			ConfigurationManager.setProperty(CONF_AUDIOSCROBBLER_PASSWORD, Util.rot13(new String(jpfASPassword
					.getPassword())));
		}
		int iLogLevel = scbLogLevel.getSelectedIndex();
		Log.setVerbosity(iLogLevel);
		ConfigurationManager.setProperty(CONF_OPTIONS_LOG_LEVEL, Integer.toString(iLogLevel));
		ConfigurationManager.setProperty(CONF_OPTIONS_INTRO_BEGIN, Integer.toString(introPosition
				.getValue()));
		ConfigurationManager.setProperty(CONF_OPTIONS_INTRO_LENGTH, Integer.toString(introLength
				.getValue()));
		String sBestofSize = jtfBestofSize.getText();
		if (!sBestofSize.equals("")) {
			ConfigurationManager.setProperty(CONF_BESTOF_SIZE, sBestofSize);
		}
		// force refresh of bestof files
		FileManager.getInstance().setRateHasChanged(true);
		String sNoveltiesAge = jtfNoveltiesAge.getText();
		if (!sNoveltiesAge.equals("")) {
			ConfigurationManager.setProperty(CONF_OPTIONS_NOVELTIES_AGE, sNoveltiesAge);
		}
		String sVisiblePlanned = jtfVisiblePlanned.getText();
		if (!sVisiblePlanned.equals("")) {
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
		if (!sHistoryDuration.equals("")) {
			ConfigurationManager.setProperty(CONF_HISTORY, sHistoryDuration);
		}
		// P2P
		ConfigurationManager.setProperty(CONF_P2P_SHARE, Boolean.toString(jcbShare.isSelected()));
		ConfigurationManager.setProperty(CONF_P2P_ADD_REMOTE_PROPERTIES, Boolean
				.toString(jcbAddRemoteProperties.isSelected()));
		ConfigurationManager.setProperty(CONF_P2P_HIDE_LOCAL_PROPERTIES, Boolean
				.toString(jcbHideProperties.isSelected()));
		String sPass = jpfPasswd.getSelectedText();
		if (sPass != null && !sPass.equals("")) {
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
		ConfigurationManager.setProperty(CONF_CHECK_FOR_UPDATE, Boolean.toString(jcbCheckUpdates
				.isSelected()));
		ConfigurationManager.setProperty(CONF_MPLAYER_ARGS, jtfMPlayerArgs.getText());
		ConfigurationManager.setProperty(CONF_ENV_VARIABLES, jtfEnvVariables.getText());
		// UI
		ConfigurationManager.setProperty(CONF_CATALOG_PAGE_SIZE, Integer.toString(jsCatalogPages
				.getValue()));
		ConfigurationManager.setProperty(CONF_CATALOG_SHOW_POPUPS, Boolean
				.toString(jcbShowCatalogPopups.isSelected()));
		ConfigurationManager.setProperty(CONF_CATALOG_SHOW_POPUPS, Boolean
				.toString(jcbShowCatalogPopups.isSelected()));
		ConfigurationManager.setProperty(CONF_OPTIONS_WATERMARK_IMAGE, pathWatermarkFile.getUrl());
		int oldFont = ConfigurationManager.getInt(CONF_FONTS_SIZE);
		// Display a message if font changed
		if (oldFont != jsFonts.getValue()) {
			Messages.showInfoMessage(Messages.getString("ParameterView.227"));
		}
		ConfigurationManager.setProperty(CONF_FONTS_SIZE, Integer.toString(jsFonts.getValue()));

		// If jajuk home changes, write new path in bootstrap file
		if (Main.workspace != null && !Main.workspace.equals(psJajukWorkspace.getUrl())) {
			// Check workspace directory
			if (!psJajukWorkspace.getUrl().trim().equals("")) {
				if (!new java.io.File(psJajukWorkspace.getUrl()).canRead()) {
					Messages.showErrorMessage(165);
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
				Messages.showErrorMessage(24);
				Log.debug("Cannot write bootstrap file");
			}
		}

		// Network
		ConfigurationManager.setProperty(CONF_NETWORK_USE_PROXY, Boolean.toString(!jcbProxyNone
				.isSelected()));
		if (jcbProxyHttp.isSelected()) {
			ConfigurationManager.setProperty(CONF_NETWORK_PROXY_TYPE, PROXY_TYPE_HTTP);
		} else if (jcbProxySocks.isSelected()) {
			ConfigurationManager.setProperty(CONF_NETWORK_PROXY_TYPE, PROXY_TYPE_SOCKS);
		}
		ConfigurationManager.setProperty(CONF_NETWORK_PROXY_HOSTNAME, jtfProxyHostname.getText());
		ConfigurationManager.setProperty(CONF_NETWORK_PROXY_PORT, jtfProxyPort.getText());
		ConfigurationManager.setProperty(CONF_NETWORK_PROXY_LOGIN, jtfProxyLogin.getText());
		ConfigurationManager.setProperty(CONF_NETWORK_PROXY_PWD, Util.rot13(new String(jtfProxyPwd
				.getPassword())));
		ConfigurationManager.setProperty(CONF_NETWORK_CONNECTION_TO, Integer.toString(connectionTO
				.getValue()));
		//Force global reload of proxy variables
		DownloadManager.setDefaultProxySettings();
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
		//Force LastFM manager configuration reload
		LastFmManager.getInstance().configure();
		
		// configuration
		ConfigurationManager.commit();
		// notify playlist editor (useful for novelties)
		ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
		// Force a full refresh (useful for catalog view for instance)
		ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
		// display a message
		InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.109"),
				InformationJPanel.INFORMATIVE);
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
		jcbHotkeys.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_HOTKEYS));

		jcbSyncTableTree.setSelected(ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE));
		scbLanguage.setSelectedIndex(Messages.getLocales().indexOf(
				ConfigurationManager.getProperty(CONF_OPTIONS_LANGUAGE)));
		scbLanguage.addActionListener(this);
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
		} else {
			jcbBackup.setSelected(true);
			backupSize.setEnabled(true);
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
		jcbProxyNone.setSelected(bUseProxy);
		jtfProxyHostname.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME));
		jtfProxyHostname.setEnabled(bUseProxy);
		jlProxyHostname.setEnabled(bUseProxy);
		jtfProxyPort.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_PORT));
		jtfProxyPort.setEnabled(bUseProxy);
		jlProxyPort.setEnabled(bUseProxy);
		jtfProxyLogin.setText(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN));
		jtfProxyLogin.setEnabled(bUseProxy);
		jlProxyLogin.setEnabled(bUseProxy);
		jtfProxyPwd.setText(Util.rot13(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_PWD)));
		jtfProxyPwd.setEnabled(bUseProxy);
		jlProxyPwd.setEnabled(bUseProxy);
		connectionTO.setValue(ConfigurationManager.getInt(CONF_NETWORK_CONNECTION_TO));
		if (!ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)){
			jcbProxyNone.setSelected(true);
		}
		else if (PROXY_TYPE_HTTP.equals(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_TYPE))){
			jcbProxyHttp.setSelected(true);
		}
		else if (PROXY_TYPE_SOCKS.equals(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_TYPE))){
			jcbProxySocks.setSelected(true);
		}
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

		jcbAudioScrobbler.setSelected(ConfigurationManager.getBoolean(CONF_AUDIOSCROBBLER_ENABLE));
		if (ConfigurationManager.getBoolean(CONF_AUDIOSCROBBLER_ENABLE)) {
			Log.debug(ConfigurationManager.getProperty(CONF_AUDIOSCROBBLER_PASSWORD));
			jtfASUser.setText(ConfigurationManager.getProperty(CONF_AUDIOSCROBBLER_USER));
			jpfASPassword.setText(Util.rot13(ConfigurationManager.getProperty(CONF_AUDIOSCROBBLER_PASSWORD)));
		} else {
			jlASUser.setEnabled(false);
			jtfASUser.setEnabled(false);
			jlASPassword.setEnabled(false);
			jpfASPassword.setEnabled(false);
		}
		// UI
		jcbVisibleAtStartup.setSelected(ConfigurationManager.getBoolean(CONF_UI_SHOW_AT_STARTUP));
		jcbShowBaloon.setSelected(ConfigurationManager.getBoolean(CONF_UI_SHOW_BALLOON));
		jcbShowCatalogPopups.setSelected(ConfigurationManager.getBoolean(CONF_CATALOG_SHOW_POPUPS));
		// Enable image selection if image watermark
		jlWatermarkImage.setEnabled(ConfigurationManager.getProperty(CONF_OPTIONS_WATERMARK)
				.equals(LNF_WATERMARK_IMAGE));
		pathWatermarkFile.setEnabled(ConfigurationManager.getProperty(CONF_OPTIONS_WATERMARK)
				.equals(LNF_WATERMARK_IMAGE));
		scbLAF.removeActionListener(this);
		scbLAF.setSelectedItem(ConfigurationManager.getProperty(CONF_OPTIONS_LNF));
		scbLAF.addActionListener(this);
		scbWatermarks.removeActionListener(this);
		scbWatermarks.setSelectedItem(ConfigurationManager.getProperty(CONF_OPTIONS_WATERMARK));
		scbWatermarks.addActionListener(this);

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
		// when changing tab, store it for future jajuk sessions
		ConfigurationManager.setProperty(CONF_OPTIONS_TAB, Integer.toString(jtpMain
				.getSelectedIndex()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
	 */
	public void update(Event event) {
		EventSubject subject = event.getSubject();
		Object origin = event.getDetails().get(DETAIL_ORIGIN);
		// Ignore this event is thrown by this view itself (to avoid loosing
		// already set options)
		if (origin.equals(this)) {
			return;
		}
		if (EventSubject.EVENT_PARAMETERS_CHANGE.equals(subject)) {
			updateSelection();
		}
	}

}
