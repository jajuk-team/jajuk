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

  private JMenuItem jmiBan;
  private JMenuItem jmiUnBan;
  private JMenuItem jmiAdore;
  private JMenuItem jmiLove;
  private JMenuItem jmiLike;
  private JMenuItem jmiUnset;
  private JMenuItem jmiAverage;
  private JMenuItem jmiPoor;
  private JMenuItem jmiHate;

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
    jmiBan = new JMenuItem(ActionManager.getAction(JajukActions.BAN_SELECTION));
    jmiBan.putClientProperty(Const.DETAIL_SELECTION, selection);

    jmiUnBan = new JMenuItem(ActionManager.getAction(JajukActions.UN_BAN_SELECTION));
    jmiUnBan.putClientProperty(Const.DETAIL_SELECTION, selection);

    jmiAdore = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_ADORE));
    jmiAdore.putClientProperty(Const.DETAIL_SELECTION, selection);

    jmiLove = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_LOVE));
    jmiLove.putClientProperty(Const.DETAIL_SELECTION, selection);

    jmiLike = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_LIKE));
    jmiLike.putClientProperty(Const.DETAIL_SELECTION, selection);

    jmiUnset = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_UNSET));
    jmiUnset.putClientProperty(Const.DETAIL_SELECTION, selection);

    jmiAverage = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_AVERAGE));
    jmiAverage.putClientProperty(Const.DETAIL_SELECTION, selection);

    jmiPoor = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_POOR));
    jmiPoor.putClientProperty(Const.DETAIL_SELECTION, selection);

    jmiHate = new JMenuItem(ActionManager.getAction(JajukActions.PREFERENCE_HATE));
    jmiHate.putClientProperty(Const.DETAIL_SELECTION, selection);

    add(jmiBan);
    add(jmiUnBan);
    addSeparator();
    add(jmiAdore);
    add(jmiLove);
    add(jmiLike);
    add(jmiUnset);
    add(jmiAverage);
    add(jmiPoor);
    add(jmiHate);
  }

}
