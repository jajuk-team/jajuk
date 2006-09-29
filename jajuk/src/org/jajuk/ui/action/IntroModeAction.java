/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class IntroModeAction extends ActionBase {
    private static final long serialVersionUID = 1L;

    IntroModeAction() {
        super(Messages.getString("JajukJMenuBar.13"), Util.getIcon(ICON_INTRO), true); //$NON-NLS-1$
        setShortDescription(Messages.getString("CommandJPanel.4")); //$NON-NLS-1$
    }

   public void perform(ActionEvent evt) {
       boolean b = Boolean.valueOf(ConfigurationManager.getProperty(CONF_STATE_INTRO));
       ConfigurationManager.setProperty(CONF_STATE_INTRO, Boolean.toString(!b));
       JajukJMenuBar.getInstance().jcbmiIntro.setSelected(!b);
       CommandJPanel.getInstance().jbIntro.setSelected(!b);
    }
}
