/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.ui.wizard.ambience;

import java.util.List;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.LocaleManager;
import org.jajuk.util.Messages;
import org.qdwizard.Wizard;

/**
 * Ambiences management wizard.
 */
public class AmbienceWizard extends Wizard {
  /** Ambiences*. */
  static List<Ambience> ambiences;

  /**
   * Instantiates a new ambience wizard.
   */
  public AmbienceWizard() {
    super(new Wizard.Builder(Messages.getString("DigitalDJWizard.56"), AmbienceScreen.class,
        JajukMainWindow.getInstance()).hSize(600).vSize(500).locale(LocaleManager.getLocale())
        .icon(IconLoader.getIcon(JajukIcons.AMBIENCE)));
  }

  @Override
  public void finish() {
    for (final Ambience ambience : AmbienceWizard.ambiences) {
      AmbienceManager.getInstance().registerAmbience(ambience);
    }
    // commit it to avoid it is lost before the app close
    AmbienceManager.getInstance().commit();
    // Refresh UI
    ObservationManager.notify(new JajukEvent(JajukEvents.AMBIENCES_CHANGE));
    InformationJPanel.getInstance().setMessage(Messages.getString("Success"),
        InformationJPanel.MessageType.INFORMATIVE);
  }

  @Override
  public Class<? extends org.qdwizard.Screen> getNextScreen(
      final Class<? extends org.qdwizard.Screen> screen) {
    return null;
  }

  @Override
  public Class<? extends org.qdwizard.Screen> getPreviousScreen(
      final Class<? extends org.qdwizard.Screen> screen) {
    return null;
  }
}
