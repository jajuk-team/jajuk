/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 8:43:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 * @author Bart Cremers(Real Software)
 * @since 4-jan-2006
 */
public class MuteAction extends ActionBase {
    MuteAction() {
        super(Util.getIcon(ICON_MUTE), "F8", true); //$NON-NLS-1$
        setShortDescription(Messages.getString("CommandJPanel.7")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) {
        Player.mute();
    }
}
