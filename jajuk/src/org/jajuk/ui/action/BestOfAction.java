/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:20:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

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
public class BestOfAction extends ActionBase {

    private static final long serialVersionUID = 1L;
    
    
    BestOfAction() {
        super(Messages.getString("JajukWindow.7"), Util.getIcon(ICON_BESTOF), true); //$NON-NLS-1$
        setShortDescription(Messages.getString("JajukWindow.24")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) throws JajukException {
        Ambience ambience = AmbienceManager.getInstance().getDefaultAmbience();
        ArrayList alToPlay = Util.filterByAmbience(FileManager.
            getInstance().getGlobalBestofPlaylist(),ambience);
        FIFO.getInstance().push(Util.createStackItems(alToPlay, ConfigurationManager.getBoolean(
            CONF_STATE_REPEAT), false), false);
    }
}
