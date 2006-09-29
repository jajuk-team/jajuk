/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;

import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class ContinueModeAction extends ActionBase {

    private static final long serialVersionUID = 1L;
    
    ContinueModeAction() {
        super(Messages.getString("JajukJMenuBar.12"), Util.getIcon(ICON_CONTINUE), //$NON-NLS-1$
              true); //$NON-NLS-1$
        setShortDescription(Messages.getString("CommandJPanel.3")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) throws JajukException {
        boolean b = ConfigurationManager.getBoolean(CONF_STATE_CONTINUE);
        ConfigurationManager.setProperty(CONF_STATE_CONTINUE, Boolean.toString(!b));

        JajukJMenuBar.getInstance().jcbmiContinue.setSelected(!b);
        CommandJPanel.getInstance().jbContinue.setSelected(!b);

        if (!b) { //enabled button
            CommandJPanel.getInstance().jbContinue.setBorder(
                BorderFactory.createLoweredBevelBorder());
            if (FIFO.isStopped()) {
                //if nothing playing, play next track if possible
                StackItem item = FIFO.getInstance().getLastPlayed();
                if (item != null) {
                    FIFO.getInstance().push(new StackItem(FileManager.getInstance().getNextFile(
                        item.getFile())), false);
                }
            }
        }
        //computes planned tracks
        FIFO.getInstance().computesPlanned(false);
    }
}
