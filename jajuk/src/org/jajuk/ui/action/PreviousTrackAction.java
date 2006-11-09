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
 * Action class for jumping to the previous track. Installed keystroke:
 * <code>CTRL + LEFT ARROW</code>.
 * 
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class PreviousTrackAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	PreviousTrackAction() {
		super(Messages.getString("JajukWindow.13"),
				Util.getIcon(ICON_PREVIOUS), "F9", false, true); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
		setShortDescription(Messages.getString("JajukWindow.29")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) {
		// check modifiers to see if it is a movement inside track, between
		// tracks or between albums
		if (evt != null &&
				//evt == null when using hotkeys
			(evt.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
			ActionManager.getAction(JajukAction.PREVIOUS_ALBUM)
					.actionPerformed(evt);
		} else {
			synchronized (MUTEX) {
				new Thread() {
					public void run() {
						try {
							FIFO.getInstance().playPrevious();
						} catch (Exception e) {
							Log.error(e);
						}
					}
				}.start();

				// Player was paused, reset pause button when changing of track
				if (Player.isPaused()) {
					Player.setPaused(false);
					ObservationManager.notify(new Event(
							EventSubject.EVENT_PLAYER_RESUME)); 
				}
			}
		}
	}
}
