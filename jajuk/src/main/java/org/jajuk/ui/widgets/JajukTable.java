/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 * $Revision$
 */

package org.jajuk.ui.widgets;

import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jajuk.base.Item;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukCellRenderer;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.TableTransferHandler;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.UtilString;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 * JXTable with following features:
 * <p>
 * Remembers columns visibility
 * <p>
 * Tooltips on each cell
 * <p>
 * Maintain a table of selected rows
 * <p>
 * Bring a menu displayed on right click
 */
public class JajukTable extends JXTable implements ITechnicalStrings, ListSelectionListener,
    java.awt.event.MouseListener {

  private static final long serialVersionUID = 1L;

  private String sConf;

  /** User Selection* */
  private List<Item> selection;

  private JPopupMenu jmenu;

  /** Specific action on double click */
  private ILaunchCommand command;

  /** Model refreshing flag */
  private volatile boolean acceptColumnsEvents = false;

  private static final DateFormat FORMATTER = UtilString.getLocaleDateFormatter();

  /**
   * Constructor
   * 
   * @param model :
   *          model to use
   * @param bSortable :
   *          is this table sortable
   * @sConf: configuration variable used to store columns conf
   */
  public JajukTable(TableModel model, boolean bSortable, String sConf) {
    super(model);
    acceptColumnsEvents = true;
    this.sConf = sConf;
    selection = new ArrayList<Item>();
    jmenu = new JPopupMenu();
    setShowGrid(false);
    init(bSortable);
    // Force to use Jajuk cell render for all columns, except for boolean
    // that should use default renderer (checkbox)
    for (TableColumn col : getColumns()) {
      col.setCellRenderer(new JajukCellRenderer());
    }
    // Listen for row selection
    getSelectionModel().addListSelectionListener(this);
    // Listen for clicks
    addMouseListener(this);
  }

  /**
   * Constructor
   * 
   * @param model :
   *          model to use
   * @sConf: configuration variable used to store columns conf
   */
  public JajukTable(TableModel model, String sConf) {
    this(model, true, sConf);
  }

  private void init(boolean bSortable) {
    super.setSortable(bSortable);
    super.setColumnControlVisible(true);
    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
  }

  /**
   * Select columns to show colsToShow list of columns id to keep
   */
  @SuppressWarnings("unchecked")
  public void showColumns(List<String> colsToShow) {
    boolean acceptColumnsEventsSave = acceptColumnsEvents;
    // Ignore columns event during these actions
    acceptColumnsEvents = false;
    Iterator it = ((DefaultTableColumnModelExt) getColumnModel()).getColumns(false).iterator();
    while (it.hasNext()) {
      TableColumnExt col = (TableColumnExt) it.next();
      if (!colsToShow.contains(((JajukTableModel) getModel()).getIdentifier(col.getModelIndex()))) {
        col.setVisible(false);
      }
    }
    reorderColumns();
    acceptColumnsEvents = acceptColumnsEventsSave;
  }

  /*
   * Reorder columns order according to given conf
   */
  private void reorderColumns() {
    // Build the index array
    List<String> index = new ArrayList<String>(10);
    StringTokenizer st = new StringTokenizer(ConfigurationManager.getProperty(this.sConf), ",");
    while (st.hasMoreTokens()) {
      index.add(st.nextToken());
    }
    // Now reorder the columns: remove all columns and re-add them according the
    // new order
    JajukTableModel model = (JajukTableModel) getModel();
    Map<String, TableColumn> map = new HashMap<String, TableColumn>();
    List<TableColumn> initialColumns = getColumns(true);
    for (TableColumn column : initialColumns) {
      map.put(model.getIdentifier(column.getModelIndex()), column);
      getColumnModel().removeColumn(column);
    }
    for (String sID : index) {
      TableColumn col = map.get(sID);
      if (col != null) {
        // Col can be null after user created a new custom property
        getColumnModel().addColumn(col);
      }
    }
    // Now add unvisible columns so they are available in table column selector
    // at after the visible ones
    for (TableColumn column : initialColumns) {
      if (!index.contains(model.getIdentifier(column.getModelIndex()))) {
        getColumnModel().addColumn(column);
      }
    }
  }

  /**
   * 
   * @return list of visible columns names as string
   * @param Name
   *          of the configuration key giving configuration
   */
  public List<String> getColumnsConf() {
    List<String> alOut = new ArrayList<String>(10);
    String value = ConfigurationManager.getProperty(sConf);
    StringTokenizer st = new StringTokenizer(value, ",");
    while (st.hasMoreTokens()) {
      alOut.add(st.nextToken());
    }
    return alOut;
  }

  /**
   * Add a new property into columns conf
   * 
   * @param property
   */
  public void addColumnIntoConf(String property) {
    if (sConf == null) {
      return;
    }
    List<String> alOut = getColumnsConf();
    if (!alOut.contains(property)) {
      String value = ConfigurationManager.getProperty(sConf);
      ConfigurationManager.setProperty(sConf, value + "," + property);
    }
  }

  /**
   * Remove a property from columns conf
   * 
   * @param property
   */
  public void removeColumnFromConf(String property) {
    if (sConf == null) {
      return;
    }
    List<String> alOut = getColumnsConf();
    alOut.remove(property);
    ConfigurationManager.setProperty(sConf, getColumnsConf(alOut));
  }

  private void columnChange() {
    if (acceptColumnsEvents) { // ignore this column change when reloading
      // model
      createColumnsConf();
      // Force table rebuilding
      Properties details = new Properties();
      details.put(DETAIL_CONTENT, this);
      ObservationManager.notify(new Event(JajukEvents.EVENT_VIEW_REFRESH_REQUEST, details));
    }
  }

  @Override
  public void columnAdded(TableColumnModelEvent arg0) {
    super.columnAdded(arg0);
    columnChange();
  }

  @Override
  public void columnRemoved(TableColumnModelEvent arg0) {
    super.columnRemoved(arg0);
    columnChange();
  }

  @Override
  public void columnMoved(TableColumnModelEvent arg0) {
    super.columnMoved(arg0);
    if (acceptColumnsEvents) {
      columnChange();
    }
  }

  /**
   * 
   * Create the jtable visible columns conf
   * 
   */
  public void createColumnsConf() {
    StringBuilder sb = new StringBuilder();
    int cols = getColumnCount(false);
    for (int i = 0; i < cols; i++) {
      String sIdentifier = ((JajukTableModel) getModel())
          .getIdentifier(convertColumnIndexToModel(i));
      sb.append(sIdentifier + ",");
    }
    String value;
    // remove last comma
    if (sb.length() > 0) {
      value = sb.substring(0, sb.length() - 1);
    } else {
      value = sb.toString();
    }
    ConfigurationManager.setProperty(sConf, value);
  }

  /**
   * 
   * @return columns configuration from given list of columns identifiers
   * 
   */
  private String getColumnsConf(List<String> alCol) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> it = alCol.iterator();
    while (it.hasNext()) {
      sb.append(it.next() + ",");
    }
    // remove last comma
    if (sb.length() > 0) {
      return sb.substring(0, sb.length() - 1);
    } else {
      return sb.toString();
    }
  }

  /**
   * add tooltips to each cell
   */
  @Override
  public String getToolTipText(MouseEvent e) {
    java.awt.Point p = e.getPoint();
    int rowIndex = rowAtPoint(p);
    int colIndex = columnAtPoint(p);
    if (rowIndex < 0 || colIndex < 0) {
      return null;
    }
    Object o = getModel().getValueAt(convertRowIndexToModel(rowIndex),
        convertColumnIndexToModel(colIndex));
    if (o == null) {
      return null;
    } else if (o instanceof IconLabel) {
      return ((IconLabel) o).getTooltip();
    } else if (o instanceof Date) {
      return FORMATTER.format((Date) o);
    } else {
      return o.toString();
    }
  }

  /**
   * Select a list of rows
   * 
   * @param indexes
   *          list of row indexes to be selected
   */
  public void setSelectedRows(int[] indexes) {
    for (int element : indexes) {
      addRowSelectionInterval(element, element);
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    // Ignore adjusting event
    if (e.getValueIsAdjusting()) {
      return;
    }
    // Make sure this table uses a Jajuk table model
    if (!(getModel() instanceof JajukTableModel)) {
      return;
    }
    JajukTableModel model = (JajukTableModel) getModel();
    selection.clear();
    int[] rows = getSelectedRows();
    for (int element : rows) {
      Object o = model.getItemAt(convertRowIndexToModel(element));
      // Make sure the model contains jajuk items
      if (!(o instanceof Item)) {
        return;
      }
      selection.add((Item) o);
    }
  }

  public List<Item> getSelection() {
    return this.selection;
  }

  public void handlePopup(final MouseEvent e) {
    int iSelectedRow = rowAtPoint(e.getPoint());
    // Store real row index
    TableTransferHandler.iSelectedRow = iSelectedRow;
    // right click on a selected node set if none or 1 node is
    // selected, a right click on another node
    // select it if more than 1, we keep selection and display a
    // popup for them
    if (getSelectedRowCount() < 2) {
      getSelectionModel().setSelectionInterval(iSelectedRow, iSelectedRow);
    }
    jmenu.show(this, e.getX(), e.getY());
  }

  public JPopupMenu getMenu() {
    return this.jmenu;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {
    // nothing to do here for now
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {
    // nothing to do here for now
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {
    // nothing to do here for now
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      handlePopup(e);
    } else if (command != null && (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
      command.launch(e.getClickCount());
      int iSelectedRow = rowAtPoint(e.getPoint());
      // Store real row index for drag and drop
      TableTransferHandler.iSelectedRow = iSelectedRow;
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      handlePopup(e);
    }
  }

  public ILaunchCommand getCommand() {
    return this.command;
  }

  public void setCommand(ILaunchCommand command) {
    this.command = command;
  }

  public void setAcceptColumnsEvents(boolean acceptColumnsEvents) {
    this.acceptColumnsEvents = acceptColumnsEvents;
  }

}
