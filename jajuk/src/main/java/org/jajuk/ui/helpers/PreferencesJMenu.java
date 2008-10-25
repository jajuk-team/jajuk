/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.jajuk.base.Item;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;

/**
 * Preference menu item including ban/unban and preference choice
 */
public class PreferencesJMenu extends JMenu {

  private static final long serialVersionUID = -4041513018474249903L;

  private JMenuItem jbBan;
  private JMenuItem jbUnBan;
  private JMenuItem jbAdore;
  private JMenuItem jbLove;
  private JMenuItem jbLike;
  private JMenuItem jbAverage;
  private JMenuItem jbPoor;
  private JMenuItem jbHate;

  /**
   * Constructs a Preference menu
   * 
   * @param item
   */
  public PreferencesJMenu(Item item) {
    super(Messages.getString("Preference.7"));
    List<Item> selection = new ArrayList<Item>(1);
    selection.add(item);
    initUI(selection);
  }

  /**
   * Constructs a Preference menu
   * 
   * @param selection
   *          selection list bound to the actions
   */
  public PreferencesJMenu(List<? extends Item> selection) {
    super(Messages.getString("Preference.7"));
    initUI(selection);
  }

  /**
   * initUI
   * @param selection
   */
  private void initUI(List<? extends Item> selection) {
    jbBan = new JMenuItem(ActionManager.getAction(JajukActions.BAN_SELECTION));
    jbBan.putClientProperty(Const.DETAIL_SELECTION, selection);

    jbUnBan = new JMenuItem(ActionManager.getAction(JajukActions.UN_BAN_SELECTION));
    jbUnBan.putClientProperty(Const.DETAIL_SELECTION, selection);

    jbAdore = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_ADORE));
    jbAdore.putClientProperty(Const.DETAIL_SELECTION, selection);

    jbLove = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_LOVE));
    jbLove.putClientProperty(Const.DETAIL_SELECTION, selection);

    jbLike = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_LIKE));
    jbLike.putClientProperty(Const.DETAIL_SELECTION, selection);

    jbAverage = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_AVERAGE));
    jbAverage.putClientProperty(Const.DETAIL_SELECTION, selection);

    jbPoor = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_POOR));
    jbPoor.putClientProperty(Const.DETAIL_SELECTION, selection);

    jbHate = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_HATE));
    jbHate.putClientProperty(Const.DETAIL_SELECTION, selection);

    add(jbBan);
    add(jbUnBan);
    addSeparator();
    add(jbAdore);
    add(jbLove);
    add(jbLike);
    add(jbAverage);
    add(jbPoor);
    add(jbHate);
  }

}
