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
import org.jajuk.util.log.Log;

/**
 * Action class for jumping to the previous album. Installed keystroke:
 * <code>CTRL + SHIFT + LEFT ARROW</code>.
 *
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class PreviousAlbumAction extends ActionBase {

    PreviousAlbumAction() {
        super("previous album", "ctrl shift LEFT", false); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void perform(ActionEvent evt) {
        synchronized (MUTEX) {
            new Thread() {
                public void run() {
                    try {
                        FIFO.getInstance().playPreviousAlbum();
                    } catch (Exception e) {
                        Log.error(e);
                    }
                }
            }.start();
            if (Player.isPaused()) {  //player was paused, reset pause button when changing of track
                Player.setPaused(false);
                ObservationManager.notify(new Event(EVENT_PLAYER_RESUME));  //notify of this event
            }
        }
    }
}
