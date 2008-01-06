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
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jajuk.base.Item;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukCellRender;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.TableTransferHandler;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
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
  private ArrayList<Item> selection;

  private JPopupMenu jmenu;

  private ILaunchCommand command;
  
  /** Model refreshing flag */
  public volatile boolean acceptColumnsEvents = false;
  
  private static final DateFormat formatter = Util.getLocaleDateFormatter();


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
      col.setCellRenderer(new JajukCellRender());
    }
    // Listen for row selection
    getSelectionModel().addListSelectionListener(this);
    //Listen for clicks
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
  public void showColumns(ArrayList<String> colsToShow) {
    Iterator it = ((DefaultTableColumnModelExt) getColumnModel()).getColumns(false).iterator();
    while (it.hasNext()) {
      TableColumnExt col = (TableColumnExt) it.next();
      if (!colsToShow.contains(((JajukTableModel) getModel()).getIdentifier(col.getModelIndex()))) {
        col.setVisible(false);
      }
    }
  }

  /**
   * 
   * @return list of visible columns names as string
   * @param Name
   *          of the configuration key giving configuration
   */
  public ArrayList<String> getColumnsConf() {
    ArrayList<String> alOut = new ArrayList<String>(10);
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
    ArrayList alOut = getColumnsConf();
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
    ArrayList alOut = getColumnsConf();
    alOut.remove(property);
    ConfigurationManager.setProperty(sConf, getColumnsConf(alOut));
  }

  private void columnChange() {
    if (acceptColumnsEvents) { // ignore this column change when reloading
      // model
      createColumnsConf();
    }
  }

  public void columnAdded(TableColumnModelEvent arg0) {
    super.columnAdded(arg0);
    columnChange();
  }

  public void columnRemoved(TableColumnModelEvent arg0) {
    super.columnRemoved(arg0);
    columnChange();
  }

  public void columnMoved(TableColumnModelEvent arg0) {
    super.columnMoved(arg0);
  }

  public void columnMarginChanged(ChangeEvent arg0) {
    super.columnMarginChanged(arg0);
  }

  public void columnSelectionChanged(ListSelectionEvent arg0) {
    super.columnSelectionChanged(arg0);
  }
  
  /**
   * 
   * Create the jtable visible columns conf
   * 
   */
  public void createColumnsConf() {
    StringBuilder sb = new StringBuilder();
    Iterator it = ((DefaultTableColumnModelExt) getColumnModel()).getColumns(true).iterator();
    while (it.hasNext()) {
      TableColumnExt col = (TableColumnExt) it.next();
      String sIdentifier = ((JajukTableModel) getModel()).getIdentifier(col.getModelIndex());
      if (col.isVisible()) {
        sb.append(sIdentifier + ",");
      }
    }
    String value;
    // remove last coma
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
  private String getColumnsConf(ArrayList alCol) {
    StringBuilder sb = new StringBuilder();
    Iterator it = alCol.iterator();
    while (it.hasNext()) {
      sb.append((String) it.next() + ",");
    }
    // remove last coma
    if (sb.length() > 0) {
      return sb.substring(0, sb.length() - 1);
    } else {
      return sb.toString();
    }
  }

  /**
   * add tooltips to each cell
   */
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
      return formatter.format((Date) o);
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
    for (int i = 0; i < indexes.length; i++) {
      addRowSelectionInterval(indexes[i], indexes[i]);
    }
  }

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
    for (int i = 0; i < rows.length; i++) {
      Object o = model.getItemAt(convertRowIndexToModel(rows[i]));
      // Make sure the model contains jajuk items
      if (!(o instanceof Item)) {
        return;
      }
      selection.add((Item) o);
    }
  }

  public ArrayList<Item> getSelection() {
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
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {
    // TODO Auto-generated method stub

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

}
