/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:21:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.base.FileManager;
import org.jajuk.base.FIFO;

/**
 * @author Bart Cremers
 * @since 4-jan-2006
 */
public class NoveltiesAction extends ActionBase {

    NoveltiesAction() {
        super(Util.getIcon(ICON_NOVELTIES), true);
        setShortDescription(Messages.getString("CommandJPanel.16")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) throws JajukException {
        ArrayList alToPlay = FileManager.getInstance().getGlobalNoveltiesPlaylist();
        if (alToPlay != null && alToPlay.size() != 0) {
            Collections.shuffle(alToPlay);//shuffle the selection
        }
        if (alToPlay != null) {
            FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alToPlay),
                                                          ConfigurationManager.getBoolean(
                                                              CONF_STATE_REPEAT), false), false);
        } else { //none novelty found
            Messages.showWarningMessage(Messages.getString("Error.127")); //$NON-NLS-1$
        }
    }
}
