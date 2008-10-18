/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision: 2523 $$
 */

package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import org.jajuk.services.players.FIFO;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

public class WebRadioAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  WebRadioAction() {
    super(Messages.getString("CommandJPanel.25"), IconLoader.getIcon(JajukIcons.WEBRADIO), true);
    setShortDescription(WebRadioManager.getCurrentWebRadioTooltip());
  }

  @Override
  public void perform(ActionEvent evt) throws JajukException {
    new Thread() {
      @Override
      public void run() {
        WebRadio radio = WebRadioManager.getInstance().getWebRadioByName(
            Conf.getString(Const.CONF_DEFAULT_WEB_RADIO));
        FIFO.launchRadio(radio);
      }
    }.start();
  }

}
