/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
 *  http://jajuk.info
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *  $Revision$
 */

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

import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukJToolbar;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;

/**
 * DOCUMENT_ME.
 */
public abstract class DropDownButton extends JajukButton implements ChangeListener,
    PopupMenuListener, ActionListener, PropertyChangeListener, Const {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 9126200472940409277L;

  /** DOCUMENT_ME. */
  private final JButton arrowButton;

  /** DOCUMENT_ME. */
  private boolean popupVisible = false;

  /**
   * Instantiates a new drop down button.
   * 
   * @param icon DOCUMENT_ME
   */
  public DropDownButton(ImageIcon icon) {
    super(icon);
    if (icon.getIconWidth() < 20) {
      arrowButton = new JajukButton(IconLoader.getIcon(JajukIcons.DROP_DOWN_16X16));
    } else {
      arrowButton = new JajukButton(IconLoader.getIcon(JajukIcons.DROP_DOWN_32X32));
    }
    getModel().addChangeListener(this);
    arrowButton.getModel().addChangeListener(this);
    arrowButton.addActionListener(this);
    arrowButton.setBorder(null);
    arrowButton.setMargin(new Insets(1, 0, 1, 0));
    addPropertyChangeListener("enabled", this); // NOI18N
  }

  /*------------------------------[ PropertyChangeListener ]---------------------------------------------------*/

  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    arrowButton.setEnabled(isEnabled());
  }

  /*------------------------------[ ChangeListener ]---------------------------------------------------*/

  /* (non-Javadoc)
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == getModel()) {
      if (popupVisible && !getModel().isRollover()) {
        getModel().setRollover(true);
        return;
      }
      arrowButton.getModel().setRollover(getModel().isRollover());
      arrowButton.setSelected(getModel().isArmed() && getModel().isPressed());
    } else {
      if (popupVisible && !arrowButton.getModel().isSelected()) {
        arrowButton.getModel().setSelected(true);
        return;
      }
      getModel().setRollover(arrowButton.getModel().isRollover());
    }
  }

  /*------------------------------[ ActionListener ]---------------------------------------------------*/

  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent ae) {
    JPopupMenu popup = getPopupMenu();
    popup.addPopupMenuListener(this);
    popup.show(this, 0, getHeight());
  }

  /*------------------------------[ PopupMenuListener ]---------------------------------------------------*/

  /* (non-Javadoc)
   * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent)
   */
  @Override
  public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    popupVisible = true;
    getModel().setRollover(true);
    arrowButton.getModel().setSelected(true);
  }

  /* (non-Javadoc)
   * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
   */
  @Override
  public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    popupVisible = false;

    getModel().setRollover(false);
    arrowButton.getModel().setSelected(false);
    ((JPopupMenu) e.getSource()).removePopupMenuListener(this);
    // act as good programmer :)
  }

  /* (non-Javadoc)
   * @see javax.swing.event.PopupMenuListener#popupMenuCanceled(javax.swing.event.PopupMenuEvent)
   */
  @Override
  public void popupMenuCanceled(PopupMenuEvent e) {
    popupVisible = false;
  }

  /*------------------------------[ Other Methods ]---------------------------------------------------*/

  /**
   * Gets the popup menu.
   * 
   * @return the popup menu
   */
  protected abstract JPopupMenu getPopupMenu();

  /**
   * Adds the to tool bar.
   * DOCUMENT_ME
   * 
   * @param toolbar DOCUMENT_ME
   * 
   * @return the j button
   */
  public JButton addToToolBar(JToolBar toolbar) {
    JToolBar tempBar = new JajukJToolbar();
    tempBar.setAlignmentX(0.5f);
    tempBar.add(this);
    tempBar.add(arrowButton);
    toolbar.add(tempBar);
    return this;
  }
}
