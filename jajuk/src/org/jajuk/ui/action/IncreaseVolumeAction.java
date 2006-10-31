/*
 * Author: Bart Cremers (Real Software)
 * Date: 13-dec-2005
 * Time: 20:10:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;

import org.jajuk.base.Player;
import org.jajuk.ui.CommandJPanel;

/**
 * Action class for increasing the volume. Installed keystroke:
 * <code>CTRL + UP ARROW</code>.
 * 
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class IncreaseVolumeAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	IncreaseVolumeAction() {
		super("increase volume", "ctrl UP", true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void perform(ActionEvent evt) {
		int iOld = CommandJPanel.getInstance().getCurrentVolume();
		int iNew = iOld + 5;
		Player.setVolume(((float) iNew) / 100);
	}
}
