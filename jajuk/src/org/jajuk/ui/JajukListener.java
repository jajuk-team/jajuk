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
 * $Revision$
 */
package org.jajuk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;

import org.jajuk.Main;
import org.jajuk.base.BasicFile;
import org.jajuk.base.FIFO;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.util.ConfigurationManager;

/**
 *  General UI listener for Jajuk widgets
 * <p>Singleton
 *
 * @author     bflorat
 * @created    15 oct. 2003
 */
public class JajukListener implements ActionListener, ITechnicalStrings {

	/**Self instance*/
	private static JajukListener jlistener;

	private JajukListener() {
	}

	public static JajukListener getInstance() {
		if (jlistener == null) {
			jlistener = new JajukListener();
		}
		return jlistener;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(EVENT_EXIT)) {
			Main.exit(0);
		}
		else if (e.getActionCommand().equals(EVENT_OPEN_FILE)) {
			JajukFileChooser jfchooser = new JajukFileChooser();
			int returnVal = jfchooser.showOpenDialog(Main.jframe);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File[] files = jfchooser.getSelectedFiles();
				FIFO.clear(); //stop all currently played tracks
				ArrayList alFiles = new ArrayList();
				for (int i = 0; i < files.length; i++) {
					alFiles.add(new BasicFile(files[i]));
				}
				FIFO.push(alFiles, false);
			}
		}
		else if (e.getActionCommand().equals(EVENT_REPEAT_MODE_STATUS_CHANGED)) {
			boolean bContinue = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_CONTINUE)).booleanValue();
			if (bContinue){
				//Repeat and continue can't be set together, so deselect repeat mode
				ConfigurationManager.setProperty(CONF_STATE_CONTINUE, Boolean.toString(!bContinue));
				JajukJMenuBar.getInstance().jcbmiContinue.setSelected(false);
				CommandJPanel.getInstance().jbContinue.setIcon(new ImageIcon(ICON_CONTINUE_OFF));
				ConfigurationManager.setProperty(CONF_ICON_CONTINUE,ICON_CONTINUE_OFF);
			}
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_REPEAT)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_REPEAT, Boolean.toString(!b));
			JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(!b);
			if (!b == true) { //enabled button
				ConfigurationManager.setProperty(CONF_ICON_REPEAT,ICON_REPEAT_ON);
				CommandJPanel.getInstance().jbRepeat.setIcon(new ImageIcon(ICON_REPEAT_ON));
			}
			else {
				ConfigurationManager.setProperty(CONF_ICON_REPEAT,ICON_REPEAT_OFF);
				CommandJPanel.getInstance().jbRepeat.setIcon(new ImageIcon(ICON_REPEAT_OFF));
			}
		}
		else if (e.getActionCommand().equals(EVENT_SHUFFLE_MODE_STATUS_CHANGED)) {
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_SHUFFLE)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, Boolean.toString(!b));
			JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(!b);
			if (!b == true) { //enabled button
				ConfigurationManager.setProperty(CONF_ICON_SHUFFLE,ICON_SHUFFLE_ON);
				CommandJPanel.getInstance().jbRandom.setIcon(new ImageIcon(ICON_SHUFFLE_ON));
			}
			else {
				ConfigurationManager.setProperty(CONF_ICON_SHUFFLE,ICON_SHUFFLE_OFF);
				CommandJPanel.getInstance().jbRandom.setIcon(new ImageIcon(ICON_SHUFFLE_OFF));
			}
		}
		else if (e.getActionCommand().equals(EVENT_CONTINUE_MODE_STATUS_CHANGED)) {
			boolean bRepeat = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_REPEAT)).booleanValue();
			if (bRepeat){
				//Repeat and continue can't be set together, so deselect repeat mode
				ConfigurationManager.setProperty(CONF_STATE_REPEAT, Boolean.toString(!bRepeat));
				JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(false);
				CommandJPanel.getInstance().jbRepeat.setIcon(new ImageIcon(ICON_REPEAT_OFF)); 
				ConfigurationManager.setProperty(CONF_ICON_REPEAT,ICON_REPEAT_OFF);
			}
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_CONTINUE)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_CONTINUE, Boolean.toString(!b));
			JajukJMenuBar.getInstance().jcbmiContinue.setSelected(!b);
			if (!b == true) { //enabled button
				ConfigurationManager.setProperty(CONF_ICON_CONTINUE,ICON_CONTINUE_ON);
				CommandJPanel.getInstance().jbContinue.setIcon(new ImageIcon(ICON_CONTINUE_ON));
			}
			else {
				ConfigurationManager.setProperty(CONF_ICON_CONTINUE,ICON_CONTINUE_OFF);
				CommandJPanel.getInstance().jbContinue.setIcon(new ImageIcon(ICON_CONTINUE_OFF));
			}
		}
		else if (e.getActionCommand().equals(EVENT_INTRO_MODE_STATUS_CHANGED)) {
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_INTRO)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_INTRO, Boolean.toString(!b));
			JajukJMenuBar.getInstance().jcbmiIntro.setSelected(!b);
			if (!b == true) { //enabled button
				ConfigurationManager.setProperty(CONF_ICON_INTRO,ICON_INTRO_ON);
				CommandJPanel.getInstance().jbIntro.setIcon(new ImageIcon(ICON_INTRO_ON));
			}
			else {
				ConfigurationManager.setProperty(CONF_ICON_INTRO,ICON_INTRO_OFF);
				CommandJPanel.getInstance().jbIntro.setIcon(new ImageIcon(ICON_INTRO_OFF));
			}
		}
		else if (e.getActionCommand().equals(EVENT_VIEW_SHOW_STATUS_CHANGED_REQUEST)) {
			if (((JCheckBoxMenuItem)e.getSource()).isSelected()){  //show view request
				ViewManager.notify(EVENT_VIEW_SHOW_REQUEST,(IView)JajukJMenuBar.getInstance().hmCheckboxView.get(e.getSource()));
			}
			else{
				ViewManager.notify(EVENT_VIEW_CLOSE_REQUEST,(IView)JajukJMenuBar.getInstance().hmCheckboxView.get(e.getSource()));
			}
		}
	}

}
