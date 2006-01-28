/*
 * Author: Bart Cremers
 * Date: 13-dec-2005
 * Time: 8:43:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 * @author Bart Cremers
 * @since 4-jan-2006
 */
public class ExitAction extends ActionBase {
    ExitAction() {
        super(Messages.getString("JajukWindow.4"), Util.getIcon(ICON_EXIT), "alt X", true); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
        setShortDescription(Messages.getString("JajukWindow.21")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) {
        if (Main.getWindow() != null){
            //Hide window ASAP
            Main.getWindow().setVisible(false);
        }
        Main.exit(0);
    }
}
