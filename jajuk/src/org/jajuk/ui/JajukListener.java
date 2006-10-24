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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.StackItem;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

/**
 *  General UI listener for Jajuk widgets
 * <p>Singleton
 *
 * @author     Bertrand Florat
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
		try{
 			//no thread, nothing requires long execution time and a SwingWorker is not adapted
            if (e.getActionCommand().equals(EventSubject.EVENT_REPEAT_MODE_STATUS_CHANGED.toString())) {
			    boolean b = ConfigurationManager.getBoolean(CONF_STATE_REPEAT);
			    ConfigurationManager.setProperty(CONF_STATE_REPEAT, Boolean.toString(!b));
			    JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(!b);
			    if (!b == true) { //enabled button
			    //    CommandJPanel.getInstance().jbRepeat.setBorder(BorderFactory.createLoweredBevelBorder());
			        //if FIFO is not void, repeat over current item
			        StackItem item = FIFO.getInstance().getCurrentItem();
			        if ( item != null && FIFO.getInstance().getIndex() == 0){ //only non-repeated items need to be set and in this case, index =0 or bug
			            item.setRepeat(true);    
			        }
			    }
			    else {//disable repeat mode
				//	CommandJPanel.getInstance().jbRepeat.setBorder(BorderFactory.createRaisedBevelBorder());
					//remove repeat mode to all items
					FIFO.getInstance().setRepeatModeToAll(false);
					//remove tracks before current position
					FIFO.getInstance().remove(0,FIFO.getInstance().getIndex()-1);
					FIFO.getInstance().setIndex(0); //select first track
				}
				//computes planned tracks
				FIFO.getInstance().computesPlanned(false);
			}
			else if (e.getActionCommand().equals(EventSubject.EVENT_SHUFFLE_MODE_STATUS_CHANGED.toString())) {
				boolean b = ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE);
				ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, Boolean.toString(!b));
				JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(!b);
				if (!b == true) { //enabled button
					CommandJPanel.getInstance().jbRandom.setBorder(BorderFactory.createLoweredBevelBorder());
					FIFO.getInstance().shuffle(); //shuffle current selection
					//now make sure we can't have a single repeated file after a non-repeated file (by design)
					if (FIFO.getInstance().containsRepeat() && !FIFO.getInstance().containsOnlyRepeat()){
						FIFO.getInstance().setRepeatModeToAll(false); //yes? un-repeat all
					}
				}
				else {
					CommandJPanel.getInstance().jbRandom.setBorder(BorderFactory.createRaisedBevelBorder());
				}
				//computes planned tracks
				FIFO.getInstance().computesPlanned(true);
			}
			else if (e.getActionCommand().equals(EventSubject.EVENT_CONTINUE_MODE_STATUS_CHANGED.toString())) {
				boolean b = ConfigurationManager.getBoolean(CONF_STATE_CONTINUE);
				ConfigurationManager.setProperty(CONF_STATE_CONTINUE, Boolean.toString(!b));
				JajukJMenuBar.getInstance().jcbmiContinue.setSelected(!b);
				if (!b == true) { //enabled button
					CommandJPanel.getInstance().jbContinue.setBorder(BorderFactory.createLoweredBevelBorder());
                    if (FIFO.isStopped()){
                        //if nothing playing, play next track if possible
                        StackItem item = FIFO.getInstance().getLastPlayed();
                        if ( item != null){
                            FIFO.getInstance().push(new StackItem(FileManager.getInstance().getNextFile(item.getFile())),false);    
                        }
                    }
				}
				else {
					CommandJPanel.getInstance().jbContinue.setBorder(BorderFactory.createRaisedBevelBorder());
				}
				//computes planned tracks
				FIFO.getInstance().computesPlanned(false);
			}
			else if (e.getActionCommand().equals(EventSubject.EVENT_INTRO_MODE_STATUS_CHANGED.toString())) {
				boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_INTRO)).booleanValue();
				ConfigurationManager.setProperty(CONF_STATE_INTRO, Boolean.toString(!b));
				JajukJMenuBar.getInstance().jcbmiIntro.setSelected(!b);
				if (!b == true) { //enabled button
					CommandJPanel.getInstance().jbIntro.setBorder(BorderFactory.createLoweredBevelBorder());
				}
				else {
					CommandJPanel.getInstance().jbIntro.setBorder(BorderFactory.createRaisedBevelBorder());
				}
			}else if (e.getActionCommand().equals(EventSubject.EVENT_VIEW_RESTORE_DEFAULTS.toString())) {
				//start in a thread to leave dispatcher thread in time
				new Thread(){
					public void run() {
						IPerspective perspective = PerspectiveManager.getCurrentPerspective();
						perspective.restoreDefaults();
					}
				}.start();
				
			}
			else if (EventSubject.EVENT_HELP_REQUIRED.equals(e.getActionCommand())){
				PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_HELP);		
			}
            else if(EventSubject.EVENT_WIZARD.equals(e.getActionCommand())){
                //First time wizard
                FirstTimeWizard fsw = new FirstTimeWizard();
                fsw.pack();
                fsw.setLocationRelativeTo(Main.getWindow());
                fsw.setVisible(true);
            }
            else if(EventSubject.EVENT_CREATE_PROPERTY.equals(e.getActionCommand())){
                NewPropertyWizard npw = new NewPropertyWizard();
                npw.pack();
                npw.setLocationRelativeTo(Main.getWindow());
                npw.setVisible(true);
            }
            else if(EventSubject.EVENT_DELETE_PROPERTY.equals(e.getActionCommand())){
                RemovePropertyWizard rpw = new RemovePropertyWizard();
                rpw.pack();
                rpw.setVisible(true);
            }
            else if(EventSubject.EVENT_QUALITY.equals(e.getActionCommand())){
                QualityFeedbackWizard qfw =  new QualityFeedbackWizard();
                qfw.pack();
                qfw.setLocationRelativeTo(Main.getWindow());
                qfw.setVisible(true);
            }
            else if(EventSubject.EVENT_TIP_OF_THE_DAY.equals(e.getActionCommand())) {
                TipOfTheDay tipsView = new TipOfTheDay();
                tipsView.setLocationRelativeTo(null);
                tipsView.setVisible(true);
            }
        }
		catch(Throwable e2){
			Log.error(e2);
		}
		finally{
			ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH)); //refresh playlist editor
		}
	}
	
}
