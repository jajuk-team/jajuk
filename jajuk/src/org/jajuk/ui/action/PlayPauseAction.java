/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.base.Player;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Event;
import java.awt.event.ActionEvent;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class PlayPauseAction extends ActionBase {

    PlayPauseAction() {
        super(Util.getIcon(ICON_PAUSE), "ctrl P", false);
        setShortDescription(Messages.getString("CommandJPanel.11")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) {
        if (Player.isPaused()) {  //player was paused, resume it
            Player.resume();
            ObservationManager.notify(new Event(EVENT_PLAYER_RESUME));  //notify of this event
        } else { //player is not paused, pause it
            Player.pause();
            ObservationManager.notify(new Event(EVENT_PLAYER_PAUSE));  //notify of this event
        }
    }
}
