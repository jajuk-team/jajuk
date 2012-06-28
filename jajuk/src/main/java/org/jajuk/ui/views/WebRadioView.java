/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
 *  $$Revision: 2510 $$
 */
package org.jajuk.ui.views;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jajuk.base.Item;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.services.webradio.WebRadioOrigin;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.PlayHighlighterPredicate;
import org.jajuk.ui.helpers.WebRadioTableModel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.ui.wizard.PropertiesDialog;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jdesktop.swingx.decorator.ColorHighlighter;

/**
 * Webradio view.
 * Show preselected webradios and allow filtering by tag. 
 * 
 */
public class WebRadioView extends AbstractTableView {
  private static final long serialVersionUID = 1L;
  /** Add a new web radio button */
  private JajukButton jbNewRadio;

  /**
   * Constructor
   * 
   */
  public WebRadioView() {
    super();
    columnsConf = CONF_WEBRADIO_COLUMNS;
    editableConf = CONF_WEBRADIO_TABLE_EDITION;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("WebRadioView.0");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#populate()
   */
  @Override
  public void initUI() {
    UtilGUI.populate(this);
  }

  /* (non-Javadoc)
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (JajukEvents.PLAYER_STOP.equals(event.getSubject())
            || JajukEvents.WEBRADIO_LAUNCHED.equals(event.getSubject())
            || JajukEvents.DEVICE_REFRESH.equals(event.getSubject())) {
          model.populateModel(jtable.getColumnsConf());
          model.fireTableDataChanged();
          // force filter to refresh
          applyFilter(sAppliedCriteria, sAppliedFilter);
        }
      }
    });
  }

  /*
  * (non-Javadoc)
  *
  * @see org.jajuk.events.Observer#getRegistrationKeys()
  */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    return eventSubjectSet;
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTableView#populateTable()
   */
  @Override
  synchronized JajukTableModel populateTable() {
    // model creation
    return new WebRadioTableModel();
  }

  /**
   * Code used in child class SwingWorker for display computations (used in
   * initUI()).
   *
   * @param in 
   */
  @Override
  public void shortCall(Object in) {
    jtable = new JajukTable(model, true, columnsConf);
    jbNewRadio = new JajukButton(IconLoader.getIcon(JajukIcons.ADD));
    jbNewRadio.setToolTipText(Messages.getString("WebRadioView.8"));
    // Open a Webradio Properties Dialog 
    jbNewRadio.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // Create a new and void webradio
        final WebRadio radio = WebRadioManager.getInstance().registerWebRadio("");
        radio.setProperty(XML_ORIGIN, WebRadioOrigin.CUSTOM);
        List<Item> webradios = new ArrayList<Item>();
        webradios.add(radio);
        PropertiesDialog dialog = new PropertiesDialog(webradios);
        dialog.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            // Drop the void webradio if user didn't set its name and URL in the dialog
            if (UtilString.isEmpty(radio.getName()) || UtilString.isEmpty(radio.getUrl())) {
              WebRadioManager.getInstance().removeItem(radio);
              ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
            }
          }
        });
      }
    });
    ColorHighlighter colorHighlighter = new ColorHighlighter(new PlayHighlighterPredicate(jtable),
        Color.ORANGE, null);
    jtable.addHighlighter(colorHighlighter);
    jmiDelete = new JMenuItem(ActionManager.getAction(JajukActions.DELETE));
    jmiDelete.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jmiFileCopyURL = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiFileCopyURL.putClientProperty(Const.DETAIL_CONTENT, jtable.getSelection());
    jmiProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    //Add menu items
    jtable.getMenu().add(jmiFileCopyURL);
    jtable.getMenu().add(jmiDelete);
    jtable.getMenu().addSeparator();
    jtable.getMenu().add(jmiProperties);
    // Set a default behavior for double click or click on the play column
    jtable.setCommand(new ILaunchCommand() {
      @Override
      public void launch(int nbClicks) {
        // Ignore event if several rows are selected
        if (jtable.getSelectedColumnCount() != 1) {
          return;
        }
        int iSelectedCol = jtable.getSelectedColumn();
        // Convert column selection as columns may have been moved
        iSelectedCol = jtable.convertColumnIndexToModel(iSelectedCol);
        // We launch the selection :
        // - In any case if user clicked on the play column (column 0)
        // - Or in case of double click on any column 
        if (iSelectedCol == 0 || // click on play icon
            // double click on any column and edition state false
            (nbClicks == 2 && !jtbEditable.isSelected())) {
          WebRadio radio = (WebRadio) model.getItemAt(jtable.convertRowIndexToModel(jtable
              .getSelectedRow()));
          // launch it
          QueueModel.launchRadio(radio);
        }
      }
    });
    // Control panel
    jpControl = new JPanel();
    jpControl.setBorder(BorderFactory.createEtchedBorder());
    // Call common code of AbstractTableView
    createGenericGUI(jbNewRadio);
    initTable();
  }
}
