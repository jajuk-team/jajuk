/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.base.FIFO;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

/**
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class StopTrackAction extends ActionBase {
	private static final long serialVersionUID = 1L;

	StopTrackAction() {
		super(
				Messages.getString("JajukWindow.11"), Util.getIcon(ICON_STOP), "ctrl S", false); //$NON-NLS-1$ //$NON-NLS-2$
		setShortDescription(Messages.getString("JajukWindow.27")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) {
		FIFO.getInstance().stopRequest();
	}
}
