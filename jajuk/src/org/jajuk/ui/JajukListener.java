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
 * Revision 1.1  2003/10/17 20:43:56  bflorat
 * 17/10/2003
 *
 */
package org.jajuk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jajuk.Main;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.TechnicalStrings;
import org.jajuk.base.TypeManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  General UI listener for Jajuk widgets
 * <p>Singleton
 *
 * @author     bflorat
 * @created    15 oct. 2003
 */
public class JajukListener implements ActionListener, TechnicalStrings {

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
				File[] filestoPlay = new File[files.length];
				for (int i = 0; i < files.length; i++) {
					filestoPlay[i] = new File(files[i].getAbsolutePath(), TypeManager.getTypeByExtension(Util.getExtension(files[i])));
				}
				FIFO.push(filestoPlay, false);
			}
		}
		else if (e.getActionCommand().equals(EVENT_REPEAT_MODE_STATUS_CHANGED)) {
			boolean bContinue = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_CONTINUE)).booleanValue();
			if (bContinue){
				//Repeat and continue can't be set together, so deselect repeat mode
				ConfigurationManager.setProperty(CONF_STATE_CONTINUE, new Boolean(!bContinue).toString());
				JajukJMenuBar.getInstance().jcbmiContinue.setSelected(false);
				CommandJPanel.getInstance().jbContinue.setIcon(new ImageIcon(ICON_CONTINUE_OFF)); 
			}
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_REPEAT)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_REPEAT, new Boolean(!b).toString());
			JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(!b);
			if (!b == true) { //enabled button
				CommandJPanel.getInstance().jbRepeat.setIcon(new ImageIcon(ICON_REPEAT_ON));
			}
			else {
				CommandJPanel.getInstance().jbRepeat.setIcon(new ImageIcon(ICON_REPEAT_OFF));
			}
		}
		else if (e.getActionCommand().equals(EVENT_SHUFFLE_MODE_STATUS_CHANGED)) {
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_SHUFFLE)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, new Boolean(!b).toString());
			JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(!b);
			if (!b == true) { //enabled button
				CommandJPanel.getInstance().jbRandom.setIcon(new ImageIcon(ICON_SHUFFLE_ON));
			}
			else {
				CommandJPanel.getInstance().jbRandom.setIcon(new ImageIcon(ICON_SHUFFLE_OFF));
			}
		}
		else if (e.getActionCommand().equals(EVENT_CONTINUE_MODE_STATUS_CHANGED)) {
			boolean bRepeat = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_REPEAT)).booleanValue();
			if (bRepeat){
				//Repeat and continue can't be set together, so deselect repeat mode
				ConfigurationManager.setProperty(CONF_STATE_REPEAT, new Boolean(!bRepeat).toString());
				JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(false);
				CommandJPanel.getInstance().jbRepeat.setIcon(new ImageIcon(ICON_REPEAT_OFF)); 
			}
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_CONTINUE)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_CONTINUE, new Boolean(!b).toString());
			JajukJMenuBar.getInstance().jcbmiContinue.setSelected(!b);
			if (!b == true) { //enabled button
				CommandJPanel.getInstance().jbContinue.setIcon(new ImageIcon(ICON_CONTINUE_ON));
			}
			else {
				CommandJPanel.getInstance().jbContinue.setIcon(new ImageIcon(ICON_CONTINUE_OFF));
			}
		}
		else if (e.getActionCommand().equals(EVENT_INTRO_MODE_STATUS_CHANGED)) {
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_INTRO)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_INTRO, new Boolean(!b).toString());
			JajukJMenuBar.getInstance().jcbmiIntro.setSelected(!b);
			if (!b == true) { //enabled button
				CommandJPanel.getInstance().jbIntro.setIcon(new ImageIcon(ICON_INTRO_ON));
			}
			else {
				CommandJPanel.getInstance().jbIntro.setIcon(new ImageIcon(ICON_INTRO_OFF));
			}
		}

	}

}
