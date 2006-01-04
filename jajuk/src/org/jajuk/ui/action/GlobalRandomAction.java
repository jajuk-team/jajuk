/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:17:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
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
public class GlobalRandomAction extends ActionBase {

    GlobalRandomAction() {
        super(Util.getIcon(ICON_SHUFFLE_GLOBAL), true);
        setShortDescription(Messages.getString("CommandJPanel.5")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) throws JajukException {
        ArrayList alToPlay = FileManager.getInstance().getGlobalShufflePlaylist();
        FIFO.getInstance().push(Util.createStackItems(alToPlay, ConfigurationManager.getBoolean(
            CONF_STATE_REPEAT), false), false);
    }
}
