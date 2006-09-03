/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:21:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;

import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.dj.Ambience;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

/**
 * @author Bart Cremers
 * @since 4-jan-2006
 */
public class NoveltiesAction extends ActionBase {

    NoveltiesAction() {
        super(Messages.getString("JajukWindow.15"), Util.getIcon(ICON_NOVELTIES), true); //$NON-NLS-1$
        setShortDescription(Messages.getString("JajukWindow.31")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) throws JajukException {
        Ambience ambience = AmbienceManager.getInstance().getDefaultAmbience();
        ArrayList alToPlay = Util.filterByAmbience(FileManager.
            getInstance().getGlobalNoveltiesPlaylist(),ambience);
        if (alToPlay != null && alToPlay.size() != 0) {
            Collections.shuffle(alToPlay);//shuffle the selection
        }
        if (alToPlay != null && alToPlay.size() > 0) {
            FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alToPlay),
                                                          ConfigurationManager.getBoolean(
                                                              CONF_STATE_REPEAT), false), false);
        } else { //none novelty found
            Messages.showWarningMessage(Messages.getString("Error.127")); //$NON-NLS-1$
        }
    }
}
