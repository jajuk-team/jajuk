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

package org.jajuk.ui.widgets;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jajuk.base.Item;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukCellRenderer;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.views.IView;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
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
 * Bring a menu displayed on right click.
 */
public class JajukTable extends JXTable implements Observer, TableColumnModelListener,
    ListSelectionListener {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private final String sConf;

  /** User Selection*. */
  private final List<Item> selection;

  /** DOCUMENT_ME. */
  private final JPopupMenu jmenu;

  /** Specific action on double click. */
  private ILaunchCommand command;

  /** Model refreshing flag. */
  private volatile boolean acceptColumnsEvents = false;

  /** The Constant FORMATTER. DOCUMENT_ME */
  private static final DateFormat FORMATTER = UtilString.getLocaleDateFormatter();

  /** Stores the last index of column move to*. */
  private int lastToIndex = 0;

  /** Mouse draging flag */
  private boolean isMouseDragging;

  /** The Jajuk table mouse adapter used to handle click events. */
  JajukMouseAdapter ma = new JajukMouseAdapter() {

    @Override
    public void handlePopup(MouseEvent e) {
      int iSelectedRow = rowAtPoint(e.getPoint());
      // right click on a selected node set if none or 1 node is
      // selected, a right click on another node
      // select it if more than 1, we keep selection and display a
      // popup for them
      if (getSelectedRowCount() < 2) {
        getSelectionModel().setSelectionInterval(iSelectedRow, iSelectedRow);
      }
      // Use getMenu() here, do not use jmenu directly as we want to enable all
      // items before though getMenu() method
      getMenu().show(JajukTable.this, e.getX(), e.getY());
    }

    @Override
    public void handleAction(final MouseEvent e) {
      command.launch(e.getClickCount());
    }
  };

  /**
   * Return drop row 
   * @return drop row
   */
  @SuppressWarnings("cast")
  public int getDropRow() {
    JTable.DropLocation dl = (JTable.DropLocation) getDropLocation();
    return dl.getRow();
  }

  /**
   * Constructor.
   * 
   * @param model : model to use
   * @param bSortable : is this table sortable
   * @param sConf DOCUMENT_ME
   * 
   * @sConf: configuration variable used to store columns conf
   */
  public JajukTable(TableModel model, boolean bSortable, String sConf) {
    // Note that JTable automatically create a default ListSelectionModel
    // executing this.valueChanged() at selection changes
    // so don't add a new listener to avoid double events consumption
    super(model);
    acceptColumnsEvents = true;
    this.sConf = sConf;
    selection = new ArrayList<Item>();
    jmenu = new JPopupMenu();
    setShowGrid(false);
    init(bSortable);

    // Listen for clicks
    addMouseListener(ma);

    //Let Laf handle drag gesture recognition (don't remove it or
    // a mouse clik disable multiple selection)
    setDragEnabled(true);

    // Add the Alternate Highlighter
    addHighlighter(UtilGUI.getAlternateHighlighter());
    // Register itself to incoming events
    ObservationManager.register(this);
  }

  /**
   * Constructor.
   * 
   * @param model : model to use
   * @param sConf DOCUMENT_ME
   * 
   * @sConf: configuration variable used to store columns conf
   */
  public JajukTable(TableModel model, String sConf) {
    this(model, true, sConf);
  }

  /**
   * Inits the. DOCUMENT_ME
   * 
   * @param bSortable DOCUMENT_ME
   */
  private void init(boolean bSortable) {
    super.setSortable(bSortable);
    super.setColumnControlVisible(true);
  }

  /**
   * Select columns to show colsToShow list of columns id to keep.
   * 
   * @param colsToShow DOCUMENT_ME
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
    // Force to use Jajuk cell render for all columns
    for (TableColumn col : getColumns()) {
      col.setCellRenderer(new JajukCellRenderer());
    }
    acceptColumnsEvents = acceptColumnsEventsSave;
  }

  /*
   * Reorder columns order according to given conf
   */
  private void reorderColumns() {
    // Build the index array
    List<String> index = new ArrayList<String>(10);
    StringTokenizer st = new StringTokenizer(Conf.getString(this.sConf), ",");
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

    // set stored column width

    // disable auto-resize temporary to set stored sizes
    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    String tableID = getTableId();

    for (int currentColumnIndex = 0; currentColumnIndex < getColumnModel().getColumnCount(); currentColumnIndex++) {
      String identifier = ((JajukTableModel) getModel())
          .getIdentifier(convertColumnIndexToModel(currentColumnIndex));
      String confId = tableID + "." + identifier + ".width";

      if (Conf.containsProperty(confId)) {
        getColumnModel().getColumn(currentColumnIndex).setPreferredWidth(Conf.getInt(confId));
      }
    }
    setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

    // must be done here and not before we add columns
    if (Conf.containsProperty(getConfKeyForIsHorizontalScrollable())) {
      setHorizontalScrollEnabled(Conf.getBoolean(getConfKeyForIsHorizontalScrollable()));
    }
  }

  /**
   * Gets the columns conf.
   * 
   * @return list of visible columns names as string
   */
  public List<String> getColumnsConf() {
    List<String> alOut = new ArrayList<String>(10);
    String value = Conf.getString(sConf);
    StringTokenizer st = new StringTokenizer(value, ",");
    while (st.hasMoreTokens()) {
      alOut.add(st.nextToken());
    }
    return alOut;
  }

  /**
   * Add a new property into columns conf.
   * 
   * @param property DOCUMENT_ME
   */
  public void addColumnIntoConf(String property) {
    if (sConf == null) {
      return;
    }
    List<String> alOut = getColumnsConf();
    if (!alOut.contains(property)) {
      String value = Conf.getString(sConf);
      Conf.setProperty(sConf, value + "," + property);
    }
  }

  /**
   * Remove a property from columns conf.
   * 
   * @param property DOCUMENT_ME
   */
  public void removeColumnFromConf(String property) {
    if (sConf == null) {
      return;
    }
    List<String> alOut = getColumnsConf();
    alOut.remove(property);
    Conf.setProperty(sConf, getColumnsConf(alOut));
  }

  /**
   * Column change. DOCUMENT_ME
   */
  private void columnChange() {
    // ignore this column change when reloading
    // model
    if (acceptColumnsEvents) {
      // If a property is given to store the column, create the new columns
      // configuration
      if (this.sConf != null) {
        createColumnsConf();
      }
      // Force table rebuilding
      Properties details = new Properties();
      details.put(Const.DETAIL_CONTENT, this);
      ObservationManager.notify(new JajukEvent(JajukEvents.VIEW_REFRESH_REQUEST, details));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JTable#columnAdded(javax.swing.event.TableColumnModelEvent)
   */
  @Override
  public void columnAdded(TableColumnModelEvent evt) {
    super.columnAdded(evt);
    columnChange();
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.jdesktop.swingx.JXTable#columnRemoved(javax.swing.event. TableColumnModelEvent)
   */
  @Override
  public void columnRemoved(TableColumnModelEvent evt) {
    super.columnRemoved(evt);
    columnChange();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JTable#columnMoved(javax.swing.event.TableColumnModelEvent)
   */
  @Override
  public void columnMoved(TableColumnModelEvent evt) {
    super.columnMoved(evt);
    /*
     * We ignore events if last to index is still the same for performances reasons (this event
     * doesn't come with a isAdjusting() method)
     */
    if (acceptColumnsEvents && evt.getToIndex() != lastToIndex) {
      lastToIndex = evt.getToIndex();
      if (this.sConf != null) {
        createColumnsConf();
      }
    }
  }

  /**
   * Create the jtable visible columns conf.
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
    Conf.setProperty(sConf, value);
  }

  /**
   * Gets the columns conf.
   * 
   * @param alCol DOCUMENT_ME
   * 
   * @return columns configuration from given list of columns identifiers
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
   * add tooltips to each cell.
   * 
   * @param e DOCUMENT_ME
   * 
   * @return the tool tip text
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
   * Select a list of rows.
   * 
   * @param indexes list of row indexes to be selected
   */
  public void setSelectedRows(int[] indexes) {
    for (int element : indexes) {
      addRowSelectionInterval(element, element);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.JTable#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    JajukTableModel model = (JajukTableModel) getModel();
    selection.clear();
    int[] rows = getSelectedRows();
    for (int element : rows) {
      Item o = model.getItemAt(convertRowIndexToModel(element));
      selection.add(o);
    }
    // throw a table selection changed event providing the current perspective, view and
    // selection (used for tree/table sync)
    Properties properties = new Properties();
    properties.put(Const.DETAIL_SELECTION, getSelection());
    properties.put(Const.DETAIL_PERSPECTIVE, PerspectiveManager.getCurrentPerspective().getID());
    // Test parent view nullity to avoid NPE
    IView parentView = UtilGUI.getParentView(this);
    if (parentView != null) {
      properties.put(Const.DETAIL_VIEW, parentView);
    }
    ObservationManager.notify(new JajukEvent(JajukEvents.TABLE_SELECTION_CHANGED, properties));

  }

  /**
   * Gets the selection.
   * 
   * @return the selection
   */
  public List<Item> getSelection() {
    return this.selection;
  }

  /**
   * Return generic popup menu for items in a table. <br>
   * All items are forced to enable state
   * 
   * @return generic popup menu for items in a table
   * 
   * @TODO : this is probably not a good idea to force menu items to enable
   */
  public JPopupMenu getMenu() {
    Component[] components = this.jmenu.getComponents();
    for (Component component2 : components) {
      component2.setEnabled(true);
    }
    return this.jmenu;
  }

  /**
   * Return generic popup menu for items in a table. <br>
   * The provided list allow to disable some items
   * 
   * @param indexToDisable list of integer of indexes of items to disable
   * 
   * @return generic popup menu for items in a table with filter
   */
  public JPopupMenu getMenu(List<Integer> indexToDisable) {
    Component[] components = this.jmenu.getComponents();
    int index = 0;
    for (Component component2 : components) {
      if (component2 instanceof JMenuItem || component2 instanceof JMenu) {
        // disable the item if its index is in the index list to disable
        component2.setEnabled(!indexToDisable.contains(index));
        index++;
      }
    }
    return this.jmenu;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(@SuppressWarnings("unused") MouseEvent e) {
    // nothing to do here for now
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(@SuppressWarnings("unused") MouseEvent e) {
    // nothing to do here for now
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(@SuppressWarnings("unused") MouseEvent e) {
    // nothing to do here for now
  }

  /**
   * Gets the command.
   * 
   * @return the command
   */
  public ILaunchCommand getCommand() {
    return this.command;
  }

  /**
   * Sets the command.
   * 
   * @param command the new command
   */
  public void setCommand(ILaunchCommand command) {
    this.command = command;
  }

  /**
   * Sets the accept columns events.
   * 
   * @param acceptColumnsEvents the new accept columns events
   */
  public void setAcceptColumnsEvents(boolean acceptColumnsEvents) {
    this.acceptColumnsEvents = acceptColumnsEvents;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EXITING);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.EXITING.equals(subject)) {
      Conf.setProperty(getConfKeyForIsHorizontalScrollable(), Boolean
          .toString(isHorizontalScrollEnabled()));

      // store column margin
      String tableID = getTableId();

      for (int currentColumnIndex = 0; currentColumnIndex < getColumnModel().getColumnCount(); currentColumnIndex++) {

        String width = Integer.toString(getColumnModel().getColumn(currentColumnIndex).getWidth());
        String identifier = ((JajukTableModel) getModel())
            .getIdentifier(convertColumnIndexToModel(currentColumnIndex));

        Conf.setProperty(tableID + "." + identifier + ".width", width);
      }
    }
  }

  /**
   * Gets the conf key for is horizontal scrollable.
   * 
   * @return the conf key for is horizontal scrollable
   */
  private String getConfKeyForIsHorizontalScrollable() {
    return getTableId() + ".is_horizontal_scrollable";
  }

  /**
   * Gets the table id.
   * 
   * @return the table id
   */
  private String getTableId() {
    String tableID = sConf;
    if (tableID == null) {
      tableID = "jajuk.table";
    }
    return tableID;
  }

  /**
   * Remove previous alternate highlighter and add a new one
   * It is required because after theme change, the alternate
   * highlighter colors are no more valid
   * 
   * @see org.jdesktop.swingx.JXTable#updateUI()
   */
  public void updateUI() {
    for (Highlighter highlighter : getHighlighters()) {
      if (highlighter instanceof CompoundHighlighter) {
        if (UtilGUI.isAlternateColorHighlighter(highlighter)) {
          removeHighlighter(highlighter);
          UtilGUI.resetAlternateColorHighlighter();
          addHighlighter(UtilGUI.getAlternateHighlighter());
        }
      }
    }
    super.updateUI();
  }

  // Fix for a JRE issue,see   :
  // During a single adjusting ListSelectionEvent, several rows can be selected
  // before the drag actually begins (except when using SINGLE_SELECTION selection mode). 
  // For instance, select row 1 and release mouse
  // then select row 2 without releasing the mouse and begin to drag from the top to the bottom :
  // in some cases, when dragging quickly, rows 2 AND 3 (and even row 4 sometimes) are selected.
  // Fix thanks jeffsabin  in http://forums.sun.com/thread.jspa?threadID=5436355

  @Override
  protected void processMouseEvent(MouseEvent e) {
    isMouseDragging = (e.getID() == MouseEvent.MOUSE_DRAGGED);
    super.processMouseEvent(e);
  }

  @Override
  protected void processMouseMotionEvent(MouseEvent e) {
    isMouseDragging = (e.getID() == MouseEvent.MOUSE_DRAGGED);
    super.processMouseMotionEvent(e);
  }

  @Override
  public void setRowSelectionInterval(int index0, int index1) {
    if (!isMouseDragging) {
      super.setRowSelectionInterval(index0, index1);
    }
  }

  @Override
  public void setColumnSelectionInterval(int index0, int index1) {
    if (!isMouseDragging) {
      super.setColumnSelectionInterval(index0, index1);
    }
  }

  @Override
  public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
    if (!isMouseDragging) {
      super.changeSelection(rowIndex, columnIndex, toggle, extend);
    }
  }

}
