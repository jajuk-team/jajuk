/*
 * Author: Bart Cremers
 * Date: 13-dec-2005
 * Time: 8:43:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.util.Util;

/**
 * @author Bart Cremers
 * @since 4-jan-2006
 */
public class MuteAction extends ActionBase {
    private static final long serialVersionUID = 1L;

    MuteAction() {
	super(
		Messages.getString("JajukWindow.2"), Util.getIcon(ICON_MUTE), "F8", true); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
	setShortDescription(Messages.getString("JajukWindow.19")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) {
	Player.mute();
	if (Player.isMuted()) {
	    setName(Messages.getString("JajukWindow.1")); //$NON-NLS-1$
	    setIcon(Util.getIcon(ICON_UNMUTE));
	    CommandJPanel.getInstance().jbMute.setSelected(true);
	} else {
	    setName(Messages.getString("JajukWindow.2")); //$NON-NLS-1$
	    setIcon(Util.getIcon(ICON_MUTE));
	    CommandJPanel.getInstance().jbMute.setSelected(false);
	}
    }
}
