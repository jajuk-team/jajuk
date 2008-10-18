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
 *  $Revision$
 */

package org.jajuk.ui.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.table.DefaultTableModel;

import org.jajuk.base.Item;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;

/**
 * Jajuk table model, adds identifier to model
 */
public abstract class JajukTableModel extends DefaultTableModel {

  /** Column identifiers */
  volatile protected List<String> idList = new ArrayList<String>(10);

  /** Rows number */
  protected int iRowNum;

  /** Values table* */
  protected Object[][] oValues;

  // Play icon in cache
  protected static final ImageIcon PLAY_ICON = IconLoader.getIcon(JajukIcons.PLAY_TABLE);

  // Unmount Play icon in cache
  protected static final ImageIcon UNMOUNT_PLAY_ICON = IconLoader.getIcon(JajukIcons.UNKNOWN);

  /** Objects */
  protected Item[] oItems;

  /** Number of standard columns */
  protected int iNumberStandardCols;

  /** Cell editable flag */
  protected boolean[][] bCellEditable;

  /** Column names */
  protected Vector<String> vColNames = new Vector<String>(10);

  /** Last value used for undo */
  private Object oLast = null;

  /** Editable flag */
  boolean bEditable = false;

  /** Tree selection */
  protected Set<Item> treeSelection;

  /**
   * 
   * @param iNumberStandardCols
   *          Number of columns of this model (without custom properties)
   */
  public JajukTableModel(int iNumberStandardCols) {
    this.iNumberStandardCols = iNumberStandardCols;
  }

  /**
   * 
   * Default constructor
   */
  public JajukTableModel() {
    this.iNumberStandardCols = 0;
  }

  /**
   * @param sColName
   * @return Column identifier for a given column title
   */
  public String getIdentifier(String sColName) {
    return idList.get(vColNames.indexOf(sColName));
  }

  /**
   * Return item at given position
   * 
   * @param iRow
   * @return
   */
  public Item getItemAt(int iRow) {
    return oItems[iRow];
  }

  /**
   * Set item at given position
   * 
   * @param iRow
   * @param IPropertyabe
   *          item to set
   */
  public void setItemAt(int iRow, Item item) {
    oItems[iRow] = item;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    // We need to test this as UI may request it before table is populated
    if (oValues == null || oValues.length == 0 || rowIndex >= oValues.length) {
      return null;
    }
    return oValues[rowIndex][columnIndex];
  }

  @Override
  public void setValueAt(Object oValue, int rowIndex, int columnIndex) {
    oLast = oValues[rowIndex][columnIndex];
    oValues[rowIndex][columnIndex] = oValue;
    fireTableCellUpdated(rowIndex, columnIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return vColNames.size();
  }

  /**
   * Undo last change
   * 
   */
  public void undo(int rowIndex, int columnIndex) {
    if (oLast != null) {
      oValues[rowIndex][columnIndex] = oLast;
    }
  }

  @Override
  public String getColumnName(int column) {
    return vColNames.get(column);
  }

  public String getIdentifier(int column) {
    return idList.get(column);
  }

  @Override
  public int getRowCount() {
    // iRowNum is set in concrete classes
    return iRowNum;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return bEditable && bCellEditable[rowIndex][columnIndex];
  }

  @Override
  public Class<? extends Object> getColumnClass(int columnIndex) {
    Object o = getValueAt(0, columnIndex);
    if (o != null) {
      return o.getClass();
    } else {
      return null;
    }
  }

  /**
   * Fill model with data using an optional filter property and pattern
   * 
   * @param sProperty
   *          Property (column) to filter
   * @param sPattern
   *          pattern
   * @param columnsToShow
   *          List of elements to show in the table (liek files,hits...). This
   *          is useful for models for memory performances as model doesn't fill
   *          values for hidden columns
   */
  public abstract void populateModel(String sProperty, String sPattern, List<String> columnsToShow);

  /**
   * Fill model with data
   */
  public void populateModel(List<String> columnsToShow) {
    populateModel(null, null, columnsToShow);
  }

  /**
   * Set this model editable state
   * 
   * @param b
   *          whether model is editable or not
   */
  public void setEditable(boolean b) {
    this.bEditable = b;
  }

  public Set<Item> getTreeSelection() {
    return this.treeSelection;
  }

  public void setTreeSelection(Set<Item> treeSelection) {
    this.treeSelection = treeSelection;
  }

}
