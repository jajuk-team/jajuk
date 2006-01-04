/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.base.FIFO;
import java.awt.event.ActionEvent;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class StopTrackAction extends ActionBase {
    StopTrackAction() {
        super(Util.getIcon(ICON_STOP), "ctrl S", false);
        setShortDescription(Messages.getString("CommandJPanel.12")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) {
        FIFO.getInstance().stopRequest();
    }
}
