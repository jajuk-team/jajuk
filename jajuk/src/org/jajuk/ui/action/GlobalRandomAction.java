/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:17:46
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
public class GlobalRandomAction extends ActionBase {

    GlobalRandomAction() {
        super(Messages.getString("JajukWindow.6"), Util.getIcon(ICON_SHUFFLE_GLOBAL), true); //$NON-NLS-1$
        String sTooltip = Messages.getString("JajukWindow.23"); //$NON-NLS-1$
        Ambience ambience = AmbienceManager.getInstance().getAmbience(ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE));
        if (ambience != null){
          String sAmbience = ambience.getName();
          sTooltip = "<html>"+Messages.getString("JajukWindow.23")+"<p><b>"+sAmbience+"</b></p></html>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        setShortDescription(sTooltip); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) throws JajukException {
        Ambience ambience = AmbienceManager.getInstance().getDefaultAmbience();
        ArrayList alToPlay = Util.filterByAmbience(FileManager.
            getInstance().getGlobalShufflePlaylist(),ambience);
        FIFO.getInstance().push(Util.createStackItems(alToPlay, ConfigurationManager.getBoolean(
            CONF_STATE_REPEAT), false), false);
    }
}
