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
package org.jajuk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
;

/**
 *  Jajuk menu bar
 * <p>Singleton
 * @author     bflorat
 * @created    4 oct. 2003
 */
public class JajukJMenuBar extends JMenuBar implements ITechnicalStrings{

	static JajukJMenuBar jjmb;
		JMenu file;
			JMenuItem jmiFileOpen;
				JajukFileChooser jfchooser;
			JMenuItem jmiFileExit;
		JMenu views;
			JMenuItem jmiRestoreDefaultViews;
		JMenu properties;
			JMenuItem jmiNewProperty;
			JMenuItem jmiDeleteProperty;
		JMenu mode;
			 JCheckBoxMenuItem jcbmiRepeat;
			JCheckBoxMenuItem jcbmiShuffle;	
			JCheckBoxMenuItem jcbmiContinue;
			JCheckBoxMenuItem jcbmiIntro;
		JMenu help;
		
		/**Hashmap JCheckBoxMenuItem -> associated view*/
		public HashMap hmCheckboxView = new HashMap(10);
	
	private JajukJMenuBar(){
		setAlignmentX(0.0f);
		//File menu
		file = new JMenu(Messages.getString("JajukJMenuBar.0")); //$NON-NLS-1$
		jmiFileOpen = new JMenuItem(Messages.getString("JajukJMenuBar.1"),Util.getIcon(ICON_OPEN_FILE)); //$NON-NLS-1$
		jmiFileOpen.addActionListener(JajukListener.getInstance());
		jmiFileOpen.setActionCommand(EVENT_OPEN_FILE);
		jmiFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
		jmiFileOpen.getAccessibleContext().setAccessibleDescription("[ALT-F]"); //$NON-NLS-1$
		jmiFileExit = new JMenuItem(Messages.getString("JajukJMenuBar.3"),Util.getIcon(ICON_EXIT));  //$NON-NLS-1$
		jmiFileExit.addActionListener(JajukListener.getInstance());
		jmiFileExit.setActionCommand(EVENT_EXIT);
		jmiFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		jmiFileExit.getAccessibleContext().setAccessibleDescription("[ALT-X]");  //$NON-NLS-1$
		file.add(jmiFileOpen);
		file.add(jmiFileExit);
				
		//Properties menu
		properties = new JMenu(Messages.getString("JajukJMenuBar.5")); //$NON-NLS-1$
		jmiNewProperty = new JMenuItem(Messages.getString("JajukJMenuBar.6"),Util.getIcon(ICON_NEW)); //$NON-NLS-1$
		jmiNewProperty.setEnabled(false);
		jmiDeleteProperty = new JMenuItem(Messages.getString("JajukJMenuBar.7"),Util.getIcon(ICON_DELETE)); //$NON-NLS-1$
		jmiDeleteProperty.setEnabled(false);
		properties.add(jmiNewProperty);
		properties.add(jmiDeleteProperty);
		properties.addSeparator();
		
		//View menu
		views = new JMenu(Messages.getString("JajukJMenuBar.8")); //$NON-NLS-1$
		jmiRestoreDefaultViews = new JMenuItem(Messages.getString("JajukJMenuBar.17"),null);
		jmiRestoreDefaultViews.addActionListener(JajukListener.getInstance());
		jmiRestoreDefaultViews.setActionCommand(EVENT_VIEW_RESTORE_DEFAULTS);
		views.add(jmiRestoreDefaultViews);
		
		//Mode menu
		mode = new JMenu(Messages.getString("JajukJMenuBar.9")); //$NON-NLS-1$
		jcbmiRepeat = new JCheckBoxMenuItem(Messages.getString("JajukJMenuBar.10"), Util.getIcon(ICON_REPEAT_ON),true);  //$NON-NLS-1$
		jcbmiRepeat.setSelected(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));
		jcbmiRepeat.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.ALT_MASK));
		jcbmiRepeat.addActionListener(JajukListener.getInstance());
		jcbmiRepeat.setActionCommand(EVENT_REPEAT_MODE_STATUS_CHANGED);
		jcbmiShuffle = new JCheckBoxMenuItem(Messages.getString("JajukJMenuBar.11"),Util.getIcon(ICON_SHUFFLE_ON),true);  //$NON-NLS-1$
		jcbmiShuffle.setSelected(ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE));
		jcbmiShuffle.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
		jcbmiShuffle.addActionListener(JajukListener.getInstance());
		jcbmiShuffle.setActionCommand(EVENT_SHUFFLE_MODE_STATUS_CHANGED);
		jcbmiContinue = new JCheckBoxMenuItem(Messages.getString("JajukJMenuBar.12"),Util.getIcon(ICON_CONTINUE_ON),true);  //$NON-NLS-1$
		jcbmiContinue.setSelected(ConfigurationManager.getBoolean(CONF_STATE_CONTINUE));
		jcbmiContinue.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.ALT_MASK));
		jcbmiContinue.addActionListener(JajukListener.getInstance());
		jcbmiContinue.setActionCommand(EVENT_CONTINUE_MODE_STATUS_CHANGED);
		jcbmiIntro = new JCheckBoxMenuItem(Messages.getString("JajukJMenuBar.13"),Util.getIcon(ICON_INTRO_ON),true);  //$NON-NLS-1$
		jcbmiIntro.setSelected(ConfigurationManager.getBoolean(CONF_STATE_INTRO));
		jcbmiIntro.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
		jcbmiIntro.setActionCommand(EVENT_INTRO_MODE_STATUS_CHANGED);
		jcbmiIntro.addActionListener(JajukListener.getInstance());
		mode.add(jcbmiRepeat);
		mode.add(jcbmiShuffle);
		mode.add(jcbmiContinue);
		mode.add(jcbmiIntro);
				
		//Help menu
		help = new JMenu(Messages.getString("JajukJMenuBar.14")); //$NON-NLS-1$
		JMenuItem jmiHelp = new JMenuItem(Messages.getString("JajukJMenuBar.15"),Util.getIcon(ICON_INFO));  //$NON-NLS-1$
		jmiHelp.addActionListener(JajukListener.getInstance());
		jmiHelp.setActionCommand(EVENT_HELP_REQUIRED);
		JMenuItem jmiAbout = new JMenuItem(Messages.getString("JajukJMenuBar.16"),Util.getIcon(ICON_INFO)); //$NON-NLS-1$
		jmiAbout.addActionListener(JajukListener.getInstance());
		jmiAbout.setActionCommand(EVENT_HELP_REQUIRED);
		help.add(jmiHelp);
		help.add(jmiAbout);
		
		//add menus
		add(file);
		add(views);
		add(properties);
		add(mode);
		add(help);
	}
	
	
	/** Refresh views checjboxs for new perspective*/
	public void refreshViews(){
		views.removeAll();
		//		Views menu
		Iterator it = PerspectiveManager.getCurrentPerspective().getViews().iterator();
		while (it.hasNext()){
			IView view = (IView)it.next();
			JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem(Messages.getString(view.getDesc()), ViewManager.isVisible(view));
			jcbmi.addActionListener(JajukListener.getInstance());
			jcbmi.setActionCommand(EVENT_VIEW_SHOW_STATUS_CHANGED_REQUEST);
			hmCheckboxView.put(jcbmi,view);
			views.add(jcbmi);
		}
	}
	
	static public synchronized JajukJMenuBar getInstance(){
		if (jjmb == null){
			jjmb = new JajukJMenuBar();
		}
		return jjmb;
	}


}
