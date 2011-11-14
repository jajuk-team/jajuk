/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.ui.widgets;

import ext.DropDownButton;
import ext.scrollablepopupmenu.XCheckedButton;
import ext.scrollablepopupmenu.XJPopupMenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;

import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioHelper;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Factorizes code dealing with Web Radio button shared by command panel and slimbar.
 */
public class WebRadioButton extends DropDownButton {

  /** Associated popup. */
  private XJPopupMenu popupWebRadio;

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new web radio button.
   * @param icon : button icon
   */
  public WebRadioButton(ImageIcon icon) {
    super(icon);
    setText("");
    popupWebRadio = new XJPopupMenu(JajukMainWindow.getInstance());
    setAction(ActionManager.getAction(JajukActions.WEB_RADIO));
    // Force 16x16 icon 
    setIcon(icon);
    populateWebRadios();
  }

  /* (non-Javadoc)
   * @see ext.DropDownButton#getPopupMenu()
   */
  @Override
  protected JPopupMenu getPopupMenu() {
    return popupWebRadio;
  }

  /**
  * Populate webradios.
  */
  public void populateWebRadios() {
    try {
      // Update button tooltip
      setToolTipText(WebRadioHelper.getCurrentWebRadioTooltip());
      // Clear previous elements
      popupWebRadio.removeAll();
      for (final WebRadio radio : WebRadioManager.getInstance().getWebRadios()) {
        XCheckedButton jmi = new XCheckedButton(radio.getName());
        jmi.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Conf.setProperty(Const.CONF_DEFAULT_WEB_RADIO, radio.getName());
            // force to reselect the item
            populateWebRadios();
            // update action tooltip on main button with right item
            JajukAction action = ActionManager.getAction(JajukActions.WEB_RADIO);
            action.setShortDescription(Const.HTML + Messages.getString("CommandJPanel.25")
                + Const.P_B + radio.getName() + Const.B_P_HTML);
          }
        });
        jmi.setSelected(Conf.getString(Const.CONF_DEFAULT_WEB_RADIO).equals(radio.getName()));
        // Show the check icon
        jmi.setDisplayCheck(true);
        popupWebRadio.add(jmi);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

}
