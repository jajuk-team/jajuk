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
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;

import org.jajuk.Main;
import org.jajuk.base.BasicFile;
import org.jajuk.base.BasicPlaylistFile;
import org.jajuk.base.FIFO;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

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
	
	public static synchronized JajukListener getInstance() {
		if (jlistener == null) {
			jlistener = new JajukListener();
		}
		return jlistener;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		//no thread, nothing requires long execution time and a SwingWorker is not adapted
		if (e.getActionCommand().equals(EVENT_EXIT)) {
			Main.exit(0);
		}
		else if (e.getActionCommand().equals(EVENT_OPEN_FILE)) {
			JajukFileChooser jfchooser = new JajukFileChooser();
			int returnVal = jfchooser.showOpenDialog(Main.getWindow());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File[] files = jfchooser.getSelectedFiles();
				FIFO.getInstance().clear(); //stop all currently played tracks
				ArrayList alFiles = new ArrayList();
				for (int i = 0; i < files.length; i++) {
					if ( Util.getExtension(files[i]).equals(EXT_PLAYLIST)){ 
						BasicPlaylistFile bplf = new BasicPlaylistFile(files[i]);
						try{
							alFiles.addAll(bplf.getBasicFiles());
						}
						catch(JajukException je){
							Log.error(je);
						}
					}
					else{
						alFiles.add(new BasicFile(files[i]));	
					}
				}
				FIFO.getInstance().push(alFiles, false);
			}
		}
		else if (e.getActionCommand().equals(EVENT_REPEAT_MODE_STATUS_CHANGED)) {
			boolean bContinue = ConfigurationManager.getBoolean(CONF_STATE_CONTINUE);
			if (bContinue){
				//Repeat and continue can't be set together, so deselect repeat mode
				ConfigurationManager.setProperty(CONF_STATE_CONTINUE, Boolean.toString(!bContinue));
				JajukJMenuBar.getInstance().jcbmiContinue.setSelected(false);
				CommandJPanel.getInstance().jbContinue.setBorder(BorderFactory.createRaisedBevelBorder());
			}
			boolean b = ConfigurationManager.getBoolean(CONF_STATE_REPEAT);
			ConfigurationManager.setProperty(CONF_STATE_REPEAT, Boolean.toString(!b));
			JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(!b);
			if (!b == true) { //enabled button
				CommandJPanel.getInstance().jbRepeat.setBorder(BorderFactory.createLoweredBevelBorder());
				//set the forced repeat track to repeat over it even with others tracks in the fifo
				FIFO.getInstance().forceRepeat(FIFO.getInstance().getCurrentFile());
			}
			else {//disable repeat mode
			    CommandJPanel.getInstance().jbRepeat.setBorder(BorderFactory.createRaisedBevelBorder());
			    FIFO.getInstance().forceRepeat(null); //reset forced
			}
		}
		else if (e.getActionCommand().equals(EVENT_SHUFFLE_MODE_STATUS_CHANGED)) {
			boolean b = ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE);
			ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, Boolean.toString(!b));
			JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(!b);
			if (!b == true) { //enabled button
				CommandJPanel.getInstance().jbRandom.setBorder(BorderFactory.createLoweredBevelBorder());
				FIFO.getInstance().shuffle(); //shuffle current selection
			}
			else {
				CommandJPanel.getInstance().jbRandom.setBorder(BorderFactory.createRaisedBevelBorder());
			}
		}
		else if (e.getActionCommand().equals(EVENT_CONTINUE_MODE_STATUS_CHANGED)) {
			boolean bRepeat = ConfigurationManager.getBoolean(CONF_STATE_REPEAT);
			if (bRepeat){
				//Repeat and continue can't be set together, so deselect repeat mode
				ConfigurationManager.setProperty(CONF_STATE_REPEAT, Boolean.toString(!bRepeat));
				JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(false);
				CommandJPanel.getInstance().jbRepeat.setBorder(BorderFactory.createRaisedBevelBorder()); 
			}
			boolean b = ConfigurationManager.getBoolean(CONF_STATE_CONTINUE);
			ConfigurationManager.setProperty(CONF_STATE_CONTINUE, Boolean.toString(!b));
			JajukJMenuBar.getInstance().jcbmiContinue.setSelected(!b);
			if (!b == true) { //enabled button
				CommandJPanel.getInstance().jbContinue.setBorder(BorderFactory.createLoweredBevelBorder());
			}
			else {
				CommandJPanel.getInstance().jbContinue.setBorder(BorderFactory.createRaisedBevelBorder());
			}
		}
		else if (e.getActionCommand().equals(EVENT_INTRO_MODE_STATUS_CHANGED)) {
			boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_INTRO)).booleanValue();
			ConfigurationManager.setProperty(CONF_STATE_INTRO, Boolean.toString(!b));
			JajukJMenuBar.getInstance().jcbmiIntro.setSelected(!b);
			if (!b == true) { //enabled button
				CommandJPanel.getInstance().jbIntro.setBorder(BorderFactory.createLoweredBevelBorder());
			}
			else {
				CommandJPanel.getInstance().jbIntro.setBorder(BorderFactory.createRaisedBevelBorder());
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
		else if (EVENT_HELP_REQUIRED.equals(e.getActionCommand())){
			PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_HELP);		
		}
	}
}
