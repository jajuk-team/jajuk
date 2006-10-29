package ext;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

/**
 * Copyright santhosh kumar
 * 
 * @author santhosh kumar - santhosh@in.fiorano.com Drop down button
 */
public abstract class DropDownButton extends JButton implements ChangeListener,
		PopupMenuListener, ActionListener, PropertyChangeListener,
		ITechnicalStrings {
	private final JButton mainButton = this;

	private final JButton arrowButton = new JButton(Util
			.getIcon(ICON_DROP_DOWN));

	private boolean popupVisible = false;

	public DropDownButton(ImageIcon icon) {
		mainButton.getModel().addChangeListener(this);
		mainButton.setIcon(icon);
		arrowButton.getModel().addChangeListener(this);
		arrowButton.addActionListener(this);
		arrowButton.setMargin(new Insets(1, 0, 1, 0));
		mainButton.addPropertyChangeListener("enabled", this); // NOI18N
		// //$NON-NLS-1$
	}

	/*------------------------------[ PropertyChangeListener ]---------------------------------------------------*/

	public void propertyChange(PropertyChangeEvent evt) {
		arrowButton.setEnabled(mainButton.isEnabled());
	}

	/*------------------------------[ ChangeListener ]---------------------------------------------------*/

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == mainButton.getModel()) {
			if (popupVisible && !mainButton.getModel().isRollover()) {
				mainButton.getModel().setRollover(true);
				return;
			}
			arrowButton.getModel().setRollover(
					mainButton.getModel().isRollover());
			arrowButton.setSelected(mainButton.getModel().isArmed()
					&& mainButton.getModel().isPressed());
		} else {
			if (popupVisible && !arrowButton.getModel().isSelected()) {
				arrowButton.getModel().setSelected(true);
				return;
			}
			mainButton.getModel().setRollover(
					arrowButton.getModel().isRollover());
		}
	}

	/*------------------------------[ ActionListener ]---------------------------------------------------*/

	public void actionPerformed(ActionEvent ae) {
		JPopupMenu popup = getPopupMenu();
		popup.addPopupMenuListener(this);
		popup.show(mainButton, 0, mainButton.getHeight());
	}

	/*------------------------------[ PopupMenuListener ]---------------------------------------------------*/

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		popupVisible = true;
		mainButton.getModel().setRollover(true);
		arrowButton.getModel().setSelected(true);
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		popupVisible = false;

		mainButton.getModel().setRollover(false);
		arrowButton.getModel().setSelected(false);
		((JPopupMenu) e.getSource()).removePopupMenuListener(this); // act
		// as
		// good
		// programmer
		// :)
	}

	public void popupMenuCanceled(PopupMenuEvent e) {
		popupVisible = false;
	}

	/*------------------------------[ Other Methods ]---------------------------------------------------*/

	protected abstract JPopupMenu getPopupMenu();

	public JButton addToToolBar(JToolBar toolbar) {
		JToolBar tempBar = new JToolBar();
		tempBar.setAlignmentX(0.5f);
		tempBar.setRollover(true);
		tempBar.add(mainButton);
		tempBar.add(arrowButton);
		tempBar.setFloatable(false);
		toolbar.add(tempBar);
		return mainButton;
	}
}