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
 * Revision 1.2  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 */
package org.jajuk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import org.jajuk.Main;
import org.jajuk.base.Directory;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.TechnicalStrings;
import org.jajuk.base.Track;
import org.jajuk.base.TypeManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;

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
					//TODO read actual id tags
					String sId = "-1"; //no id for this file, can be outside collection
					String sName = files[i].getName();
					Directory directory = null;
					Track track = new Track(sId, sName,null,null,null,0,null,0,TypeManager.getTypeByExtension(Util.getExtension(files[i])),new File[0],0,null); //set actual id tags
					String size = Long.toString(files[i].length());
					String sQuality = null; //set actual quality
					filestoPlay[i] = new File(sId,sName,directory,track,size,sQuality);
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
