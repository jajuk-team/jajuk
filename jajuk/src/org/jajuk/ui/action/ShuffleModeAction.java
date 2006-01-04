/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import org.jajuk.base.FIFO;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import java.awt.event.ActionEvent;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class ShuffleModeAction extends ActionBase {
    ShuffleModeAction() {
        super(Messages.getString("JajukJMenuBar.11"), Util.getIcon(ICON_SHUFFLE), "ctrl H", true); //$NON-NLS-1$ //$NON-NLS-2$
        setShortDescription(Messages.getString("CommandJPanel.2")); //$NON-NLS-1$
    }

    /**
     * Invoked when an action occurs.
     * @param evt
     */
    public void perform(ActionEvent evt) {
        boolean b = ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE);
        ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, Boolean.toString(!b));

        JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(!b);
        CommandJPanel.getInstance().jbRandom.setSelected(!b);
        if (!b) { //enabled button
            FIFO.getInstance().shuffle(); //shuffle current selection
            //now make sure we can't have a single repeated file after a non-repeated file (by design)
            if (FIFO.getInstance().containsRepeat() && !FIFO.getInstance().containsOnlyRepeat()){
                FIFO.getInstance().setRepeatModeToAll(false); //yes? un-repeat all
            }
        }
        //computes planned tracks
        FIFO.getInstance().computesPlanned(true);
    }
}
