/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class PlayPauseAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	PlayPauseAction() {
		super(
				Messages.getString("JajukWindow.10"), Util.getIcon(ICON_PAUSE), "ctrl P", false); //$NON-NLS-1$ //$NON-NLS-2$
		setShortDescription(Messages.getString("JajukWindow.26")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) {
		if (Player.isPaused()) { // player was paused, resume it
			Player.resume();
			ObservationManager.notify(new Event(
					EventSubject.EVENT_PLAYER_RESUME)); // notify of this event
			setIcon(Util.getIcon(ICON_PAUSE));
			setName(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
		} else { // player is not paused, pause it
			Player.pause();
			ObservationManager
					.notify(new Event(EventSubject.EVENT_PLAYER_PAUSE)); // notify
			// of
			// this
			// event
			setIcon(Util.getIcon(ICON_PLAY));
			setName(Messages.getString("JajukWindow.12")); //$NON-NLS-1$
		}
	}
}
