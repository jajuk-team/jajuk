/*
 *  Jajuk
 *  Copyright (C) 2006 Bertrand Florat
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
 *  $$Revision$$
 */

package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import org.jajuk.base.FIFO;
import org.jajuk.base.StyleManager;
import org.jajuk.dj.DigitalDJ;
import org.jajuk.dj.DigitalDJManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

/**
 * @author Bertrand florat
 * @since 14-may-2006
 */
public class DJAction extends ActionBase {

    DJAction() {
        super(Messages.getString("CommandJPanel.16"),Util.getIcon(ICON_DIGITAL_DJ), true); //$NON-NLS-1$
        setShortDescription(Messages.getString("CommandJPanel.18")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) throws JajukException {
        if (StyleManager.getInstance().getItems().size() == 0){
            Messages.showErrorMessage("156"); //void collection error
        }
        else{
            new Thread(){
                public void run(){
                    
                    DigitalDJ dj = DigitalDJManager.getInstance().getDJByID(ConfigurationManager.getProperty(CONF_DEFAULT_DJ));
                    if (dj != null){
                        ConfigurationManager.setProperty(CONF_FADE_DURATION,Integer.toString(dj.getFadingDuration()));
                        ArrayList al = dj.generatePlaylist();
                        if (al.size() == 0){ //DJ constraints cannot be respected
                            Messages.showErrorMessage("158");
                            return;
                        }
                        FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(al),
                            ConfigurationManager.getBoolean(CONF_STATE_REPEAT), false), false);
                    }
                    else{
                        Messages.showErrorMessage("157");
                    }
                }
            }.start();
        }
    }
}
