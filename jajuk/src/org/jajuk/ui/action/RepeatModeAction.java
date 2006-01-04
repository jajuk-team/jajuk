/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import org.jajuk.base.FIFO;
import org.jajuk.base.StackItem;
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
public class RepeatModeAction extends ActionBase {
    RepeatModeAction() {
        super(Messages.getString("JajukJMenuBar.10"), Util.getIcon(ICON_REPEAT), "ctrl T", true); //$NON-NLS-1$ //$NON-NLS-2$
        setShortDescription(Messages.getString("CommandJPanel.1")); //$NON-NLS-1$
    }

    /**
     * Invoked when an action occurs.
     * @param evt
     */
    public void perform(ActionEvent evt) {

        boolean b = ConfigurationManager.getBoolean(CONF_STATE_REPEAT);
        ConfigurationManager.setProperty(CONF_STATE_REPEAT, Boolean.toString(!b));

        JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(!b);
        CommandJPanel.getInstance().jbRepeat.setSelected(!b);

        if (!b) { //enabled button
            //if FIFO is not void, repeat over current item
            StackItem item = FIFO.getInstance().getCurrentItem();
            if ( item != null && FIFO.getInstance().getIndex() == 0){ //only non-repeated items need to be set and in this case, index =0 or bug
                item.setRepeat(true);
            }
        }
        else {//disable repeat mode
            //remove repeat mode to all items
            FIFO.getInstance().setRepeatModeToAll(false);
            //remove tracks before current position
            FIFO.getInstance().remove(0,FIFO.getInstance().getIndex()-1);
            FIFO.getInstance().setIndex(0); //select first track
        }
        //computes planned tracks
        FIFO.getInstance().computesPlanned(false);
    }
}
