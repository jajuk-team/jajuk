/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class RewindTrackAction extends ActionBase {

    private static final float JUMP_SIZE = 0.1f;

    RewindTrackAction() {
        super(Util.getIcon(ICON_REW), "ctrl alt B", false);
        setShortDescription(Messages.getString("CommandJPanel.10")); //$NON-NLS-1$

    }

    public void perform(ActionEvent evt) {
        if ((evt.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
            //replay the entire file
            Player.seek(0);
        } else {
            float fCurrentPosition = Player.getCurrentPosition();
            Player.seek(fCurrentPosition - JUMP_SIZE);
        }
    }
}
