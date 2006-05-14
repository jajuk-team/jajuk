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

import static org.jajuk.ui.action.JajukAction.CONTINUE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.action.JajukAction.HELP_REQUIRED;
import static org.jajuk.ui.action.JajukAction.INTRO_MODE_STATUS_CHANGED;
import static org.jajuk.ui.action.JajukAction.QUALITY;
import static org.jajuk.ui.action.JajukAction.REPEAT_MODE_STATUS_CHANGE;
import static org.jajuk.ui.action.JajukAction.SHOW_ABOUT;
import static org.jajuk.ui.action.JajukAction.SHUFFLE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.action.JajukAction.TIP_OF_THE_DAY;
import static org.jajuk.ui.action.JajukAction.WIZARD;

import java.awt.FlowLayout;
import java.util.HashMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.ActionUtil;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

/**
 *  Jajuk menu bar
 * <p>Singleton
 * @author     Bertrand Florat
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
            public JCheckBoxMenuItem jcbmiRepeat;
            public JCheckBoxMenuItem jcbmiShuffle;
            public JCheckBoxMenuItem jcbmiContinue;
            public JCheckBoxMenuItem jcbmiIntro;
        JMenu help;
            JMenuItem jmiHelp;
            JMenuItem jmiTipOfTheDay;
            JMenuItem jmiAbout;
            JMenuItem jmiWizard;
            JMenuItem jmiQualityAgent;

        /**Hashmap JCheckBoxMenuItem -> associated view*/
        public HashMap hmCheckboxView = new HashMap(10);

    private JajukJMenuBar(){
        setAlignmentX(0.0f);
        //File menu
        file = new JMenu(Messages.getString("JajukJMenuBar.0")); //$NON-NLS-1$

        jmiFileExit = new JMenuItem(ActionManager.getAction(JajukAction.EXIT));
        file.add(jmiFileExit);

        //Properties menu
        properties = new JMenu(Messages.getString("JajukJMenuBar.5")); //$NON-NLS-1$
        jmiNewProperty = new JMenuItem(Messages.getString("JajukJMenuBar.6"),Util.getIcon(ICON_NEW)); //$NON-NLS-1$
        jmiNewProperty.addActionListener(JajukListener.getInstance());
        jmiNewProperty.setActionCommand(EVENT_CREATE_PROPERTY);
        JPanel jpNewProperty = new JPanel();
        jmiNewProperty.setLayout(new FlowLayout(FlowLayout.LEFT));
        jmiDeleteProperty = new JMenuItem(Messages.getString("JajukJMenuBar.7"),Util.getIcon(ICON_DELETE)); //$NON-NLS-1$
        jmiDeleteProperty.addActionListener(JajukListener.getInstance());
        jmiDeleteProperty.setActionCommand(EVENT_DELETE_PROPERTY);
        properties.add(jmiNewProperty);
        properties.add(jmiDeleteProperty);

        //View menu
        views = new JMenu(Messages.getString("JajukJMenuBar.8")); //$NON-NLS-1$
        jmiRestoreDefaultViews = new JMenuItem(Messages.getString("JajukJMenuBar.17"),Util.getIcon(ICON_REFRESH)); //$NON-NLS-1$
        jmiRestoreDefaultViews.addActionListener(JajukListener.getInstance());
        jmiRestoreDefaultViews.setActionCommand(EVENT_VIEW_RESTORE_DEFAULTS);
        views.add(jmiRestoreDefaultViews);

        //Mode menu
        String modeText = Messages.getString("JajukJMenuBar.9"); //$NON-NLS-1$
        mode = new JMenu(ActionUtil.strip(modeText));
        mode.setMnemonic(ActionUtil.getMnemonic(modeText));

        jcbmiRepeat = new JCheckBoxMenuItem(ActionManager.getAction(REPEAT_MODE_STATUS_CHANGE));
        jcbmiRepeat.setSelected(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));
        jcbmiShuffle = new JCheckBoxMenuItem(ActionManager.getAction(SHUFFLE_MODE_STATUS_CHANGED));
        jcbmiShuffle.setSelected(ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE));
        jcbmiContinue = new JCheckBoxMenuItem(ActionManager.getAction(CONTINUE_MODE_STATUS_CHANGED));
        jcbmiContinue.setSelected(ConfigurationManager.getBoolean(CONF_STATE_CONTINUE));
        jcbmiIntro = new JCheckBoxMenuItem(ActionManager.getAction(INTRO_MODE_STATUS_CHANGED));
        jcbmiIntro.setSelected(ConfigurationManager.getBoolean(CONF_STATE_INTRO));

        mode.add(jcbmiRepeat);
        mode.add(jcbmiShuffle);
        mode.add(jcbmiContinue);
        mode.add(jcbmiIntro);

        //Help menu
        String helpText = Messages.getString("JajukJMenuBar.14"); //$NON-NLS-1$
        help = new JMenu(ActionUtil.strip(helpText));
        help.setMnemonic(ActionUtil.getMnemonic(helpText));

        jmiHelp = new JMenuItem(ActionManager.getAction(HELP_REQUIRED));
        jmiAbout = new JMenuItem(ActionManager.getAction(SHOW_ABOUT));
        jmiWizard = new JMenuItem(ActionManager.getAction(WIZARD));
        jmiQualityAgent = new JMenuItem(ActionManager.getAction(QUALITY));
        jmiTipOfTheDay = new JMenuItem(ActionManager.getAction(TIP_OF_THE_DAY));

        help.add(jmiHelp);
        help.add(jmiTipOfTheDay);
        help.add(jmiWizard);
        help.add(jmiQualityAgent);
        help.add(jmiAbout);
        
        //add menus
        add(file);
        add(views);
        add(properties);
        add(mode);
        add(help);
    }

    static public synchronized JajukJMenuBar getInstance(){
        if (jjmb == null){
            jjmb = new JajukJMenuBar();
        }
        return jjmb;
    }
}
