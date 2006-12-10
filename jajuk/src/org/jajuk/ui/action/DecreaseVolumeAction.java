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
 * Action class for decreasing the volume. Installed keystroke:
 * <code>CTRL + DOWN ARROW</code>.
 * 
 * @author Bart Cremers(Real Software)
 * @since 13-dec-2005
 */
public class DecreaseVolumeAction extends ActionBase {
	private static final long serialVersionUID = 1L;

	DecreaseVolumeAction() {
		super("decrease volume", "ctrl DOWN", true, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public void perform(ActionEvent evt) {
		int iOld = CommandJPanel.getInstance().getCurrentVolume();
		int iNew = iOld - 5;
		Player.setVolume(((float) iNew) / 100);
	}
	
	
}
