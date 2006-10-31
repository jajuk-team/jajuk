/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Action class for jumping to the next track. Installed keystroke:
 * <code>CTRL + RIGHT ARROW</code>.
 * 
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class NextTrackAction extends ActionBase {
	private static final long serialVersionUID = 1L;

	NextTrackAction() {
		super(
				Messages.getString("JajukWindow.14"), Util.getIcon(ICON_NEXT), "ctrl RIGHT", false); //$NON-NLS-1$ //$NON-NLS-2$
		setShortDescription(Messages.getString("JajukWindow.30")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) {
		int mod = evt.getModifiers();

		if ((mod & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
			ActionManager.getAction(JajukAction.NEXT_ALBUM)
					.actionPerformed(evt);
		} else {
			synchronized (MUTEX) {
				new Thread() {
					public void run() {
						try {
							FIFO.getInstance().playNext();
						} catch (Exception e) {
							Log.error(e);
						}
					}
				}.start();

				// Player was paused, reset pause button when changing of track
				if (Player.isPaused()) {
					Player.setPaused(false);
					ObservationManager.notify(new Event(
							EventSubject.EVENT_PLAYER_RESUME)); // notify of
					// this event
				}
			}
		}
	}
}
