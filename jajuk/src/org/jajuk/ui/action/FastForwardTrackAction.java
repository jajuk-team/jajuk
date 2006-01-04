/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.base.Player;
import java.awt.event.ActionEvent;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class FastForwardTrackAction extends ActionBase {

    private static final float JUMP_SIZE = 0.1f;

    FastForwardTrackAction() {
        super(Util.getIcon(ICON_FWD), "ctrl alt F", false);
        setShortDescription(Messages.getString("CommandJPanel.13")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) {
        float fCurrentPosition = Player.getCurrentPosition();
        Player.seek(fCurrentPosition + JUMP_SIZE);
    }
}
