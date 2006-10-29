/*
 * Jajuk Copyright (C) 2003 Bertrand Florat
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA. 
 * $Revision$
 */

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jajuk.base.AuthorManager;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.StyleManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukCellRender;
import org.jajuk.ui.JajukTable;
import org.jajuk.ui.JajukTableModel;
import org.jajuk.ui.JajukToggleButton;
import org.jajuk.ui.TableTransferHandler;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

import ext.SwingWorker;

/**
 * Abstract table view : common implementation for both physical and logical
 * table views
 * 
 * @author Bertrand Florat
 * @created 13 dec. 2003
 */
public abstract class AbstractTableView extends ViewAdapter implements
		ActionListener, MouseListener, ItemListener, TableColumnModelListener,
		TableModelListener, ITechnicalStrings, Observer {

	/** The logical table */
	JajukTable jtable;

	JPanel jpControl;

	JajukToggleButton jtbEditable;

	JLabel jlFilter;

	JComboBox jcbProperty;

	JLabel jlEquals;

	JTextField jtfValue;

	JButton jbClearFilter;

	JButton jbAdvancedFilter;

	JMenuItem jmiProperties;

	/** Table model */
	JajukTableModel model;

	/** Currently applied filter */
	String sAppliedFilter = ""; //$NON-NLS-1$

	/** Currently applied criteria */
	String sAppliedCriteria;

	/** Do search panel need a search */
	private boolean bNeedSearch = false;

	/** Default time in ms before launching a search automaticaly */
	private static final int WAIT_TIME = 300;

	/** Date last key pressed */
	private long lDateTyped;

	/** Model refreshing flag */
	boolean bReloading = false;

	/** Associated conf key */
	String sConf = null;

	/** Constructor */
	public AbstractTableView() {
		if (AbstractTableView.this instanceof PhysicalTableView) {
			sConf = CONF_PHYSICAL_TABLE_COLUMNS;
		} else {
			sConf = CONF_LOGICAL_TABLE_COLUMNS;
		}
		// launches a thread used to perform dynamic filtering when user is
		// typing
		new Thread() {
			public void run() {
				while (true) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ie) {
						Log.error(ie);
					}
					if (bNeedSearch
							&& (System.currentTimeMillis() - lDateTyped >= WAIT_TIME)) {
						sAppliedFilter = jtfValue.getText();
						sAppliedCriteria = getApplyCriteria();
						applyFilter(sAppliedCriteria, sAppliedFilter);
						bNeedSearch = false;
					}
				}
			}
		}.start();
	}

	/**
	 * 
	 * @return Applied criteria
	 */
	private String getApplyCriteria() {
		int indexCombo = jcbProperty.getSelectedIndex();
		if (indexCombo == 0) { // first criteria is special: any
			sAppliedCriteria = XML_ANY;
		} else { // otherwise, take criteria from model
			sAppliedCriteria = model.getIdentifier(indexCombo);
		}
		return sAppliedCriteria;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#display()
	 */
	public void initUI() {
		SwingWorker sw = new SwingWorker() {
			public Object construct() {
				model = populateTable();
				return null;
			}

			public void finished() {
				// Control panel
				jpControl = new JPanel();
				jpControl.setBorder(BorderFactory.createEtchedBorder());
				jtbEditable = new JajukToggleButton(Util.getIcon(ICON_EDIT));
				jtbEditable.setToolTipText(Messages
						.getString("AbstractTableView.11")); //$NON-NLS-1$
				jtbEditable.addActionListener(AbstractTableView.this);
				jlFilter = new JLabel(Messages.getString("AbstractTableView.0")); //$NON-NLS-1$
				// properties combo box, fill with colums names expect ID
				jcbProperty = new JComboBox();
				// "any" criteria
				jcbProperty.addItem(Messages.getString("AbstractTableView.8")); //$NON-NLS-1$
				for (int i = 1; i < model.getColumnCount(); i++) {
					// Others columns except ID
					jcbProperty.addItem(model.getColumnName(i));
				}
				jcbProperty.setToolTipText(Messages
						.getString("AbstractTableView.1")); //$NON-NLS-1$
				jcbProperty.addItemListener(AbstractTableView.this);
				jlEquals = new JLabel(Messages.getString("AbstractTableView.7")); //$NON-NLS-1$
				jtfValue = new JTextField();
				jtfValue.addKeyListener(new KeyAdapter() {
					public void keyReleased(KeyEvent e) {
						bNeedSearch = true;
						lDateTyped = System.currentTimeMillis();
					}
				});
				jtfValue.setToolTipText(Messages
						.getString("AbstractTableView.3")); //$NON-NLS-1$
				// buttons
				jbClearFilter = new JButton(Util.getIcon(ICON_CLEAR_FILTER));
				jbClearFilter.addActionListener(AbstractTableView.this);
				jbAdvancedFilter = new JButton(Util
						.getIcon(ICON_ADVANCED_FILTER));
				jbAdvancedFilter.addActionListener(AbstractTableView.this);
				jbClearFilter.setToolTipText(Messages
						.getString("AbstractTableView.5")); //$NON-NLS-1$
				jbAdvancedFilter.setToolTipText(Messages
						.getString("AbstractTableView.6")); //$NON-NLS-1$
				jbAdvancedFilter.setEnabled(false); // TBI
				int iXspace = 5;
				double sizeControl[][] = {
						{ iXspace, 20, 3 * iXspace, TableLayout.FILL, iXspace,
								0.3, TableLayout.FILL, TableLayout.FILL,
								iXspace, 0.3, iXspace, 20, iXspace, 20, iXspace },
						{ 22 } };
				jpControl.setLayout(new TableLayout(sizeControl));
				jpControl.add(jtbEditable, "1,0"); //$NON-NLS-1$
				jpControl.add(jlFilter, "3,0"); //$NON-NLS-1$
				jpControl.add(jcbProperty, "5,0"); //$NON-NLS-1$
				jpControl.add(jlEquals, "7,0"); //$NON-NLS-1$
				jpControl.add(jtfValue, "9,0"); //$NON-NLS-1$
				jpControl.add(jbClearFilter, "11,0"); //$NON-NLS-1$
				jpControl.add(jbAdvancedFilter, "13,0"); //$NON-NLS-1$
				jpControl.setMinimumSize(new Dimension(0, 0)); // allow resing
				// with info
				// node
				// add
				double size[][] = { { 0.99 }, { 30, 0.99 } };
				setLayout(new TableLayout(size));
				add(jpControl, "0,0"); //$NON-NLS-1$
				if (AbstractTableView.this instanceof PhysicalTableView) {
					jtable = new JajukTable(model, true,
							CONF_PHYSICAL_TABLE_COLUMNS);
				} else {
					jtable = new JajukTable(model, true,
							CONF_LOGICAL_TABLE_COLUMNS);
				}
				jtable.getColumnModel().addColumnModelListener(
						AbstractTableView.this);
				setRenderers();
				add(new JScrollPane(jtable), "0,1"); //$NON-NLS-1$
				jtable.setDragEnabled(true);
				jtable.setTransferHandler(new TableTransferHandler(jtable));
				jtable.addMouseListener(AbstractTableView.this);
				jtable.showColumns(jtable.getColumnsConf());
				applyFilter(null, null);
				jtable.packTable(5);
				// Register on the list for subject we are interrested in
				ObservationManager.register(AbstractTableView.this);
				// refresh columns conf in case of some attributes been removed
				// or added before view instanciation
				Properties properties = ObservationManager
						.getDetailsLastOccurence(EventSubject.EVENT_CUSTOM_PROPERTIES_ADD);
				Event event = new Event(
						EventSubject.EVENT_CUSTOM_PROPERTIES_ADD, properties);
				update(event);
				initTable(); // perform type-specific init
			}
		};
		sw.start();
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_MOUNT);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_UNMOUNT);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
		eventSubjectSet.add(EventSubject.EVENT_SYNC_TREE_TABLE);
		eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_ADD);
		eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE);
		eventSubjectSet.add(EventSubject.EVENT_RATE_CHANGED);
		eventSubjectSet.add(EventSubject.EVENT_TABLE_CLEAR_SELECTION);
		return eventSubjectSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent e) {
		// not in a thread because it is always called inside a thread created
		// from sub-classes
		if (e.getSource() == jbClearFilter) { // remove all filters
			jtfValue.setText(""); // clear value textfield //$NON-NLS-1$
			this.sAppliedFilter = null;
			this.sAppliedCriteria = null;
			applyFilter(sAppliedCriteria, sAppliedFilter);
		} else if (e.getSource() == jbAdvancedFilter) {
			// TBI
		} else { // others events will be treated by child classes
			othersActionPerformed(e);
		}
	}

	/**
	 * Apply a filter, to be implemented by physical and logical tables, alter
	 * the model
	 */
	public void applyFilter(String sPropertyName, String sPropertyValue) {
		model.removeTableModelListener(AbstractTableView.this);
		model.populateModel(sPropertyName, sPropertyValue);
		model.fireTableDataChanged();
		model.addTableModelListener(AbstractTableView.this);
	}

	/**
	 * Child actions
	 * 
	 * @param ae
	 */
	abstract void othersActionPerformed(ActionEvent ae);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(final Event event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					bReloading = true; // flag reloading to avoid wrong column
					// events
					EventSubject subject = event.getSubject();
					if (EventSubject.EVENT_TABLE_CLEAR_SELECTION
							.equals(subject)) {
						jtable.clearSelection();
					}
					if (EventSubject.EVENT_DEVICE_MOUNT.equals(subject)
							|| EventSubject.EVENT_DEVICE_UNMOUNT
									.equals(subject)
							|| EventSubject.EVENT_SYNC_TREE_TABLE
									.equals(subject)) {
						jtable.clearSelection();
						applyFilter(sAppliedCriteria, sAppliedFilter); // force
						// filter
						// to
						// refresh
					} else if (EventSubject.EVENT_DEVICE_REFRESH
							.equals(subject)
							|| EventSubject.EVENT_RATE_CHANGED.equals(subject)) {
						applyFilter(sAppliedCriteria, sAppliedFilter); // force
						// filter
						// to
						// refresh
					} else if (EventSubject.EVENT_CUSTOM_PROPERTIES_ADD
							.equals(subject)) {
						Properties properties = event.getDetails();
						if (properties == null) { // can be null at view
							// populate
							return;
						}
						model = populateTable();// create a new model
						jtable.setModel(model);
						setRenderers();
						// add new item in configuration cols
						jtable.addColumnIntoConf((String) properties
								.get(DETAIL_CONTENT));
						jtable.showColumns(jtable.getColumnsConf());
						applyFilter(sAppliedCriteria, sAppliedFilter);
						jcbProperty.addItem(properties.get(DETAIL_CONTENT));
					} else if (EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE
							.equals(subject)) {
						Properties properties = event.getDetails();
						if (properties == null) { // can be null at view
							// populate
							return;
						}
						// remove item from configuration cols
						model = populateTable();// create a new model
						jtable.setModel(model);
						setRenderers();
						jtable.addColumnIntoConf((String) properties
								.get(DETAIL_CONTENT));
						jtable.showColumns(jtable.getColumnsConf());
						applyFilter(sAppliedCriteria, sAppliedFilter);
						jcbProperty.removeItem(properties.get(DETAIL_CONTENT));
					}
				} catch (Exception e) {
					Log.error(e);
				} finally {
					bReloading = false; // make sure to remove this flag
				}
			}
		});

	}

	/** Fill the table */
	abstract JajukTableModel populateTable();

	private void setRenderers() {
		Iterator it = ((DefaultTableColumnModelExt) jtable.getColumnModel())
				.getColumns(true).iterator();
		while (it.hasNext()) {
			TableColumnExt col = (TableColumnExt) it.next();
			String sIdentifier = model.getIdentifier(col.getModelIndex());
			// create a combo box for styles, note that we can't add new
			// styles dynamically
			if (XML_STYLE.equals(sIdentifier)) {
				JComboBox jcb = new JComboBox(StyleManager.getInstance()
						.getStylesList());
				jcb.setEditable(true);
				AutoCompleteDecorator.decorate(jcb);
				col.setCellEditor(new ComboBoxCellEditor(jcb));
			}
			// create a combo box for authors, note that we can't add new
			// authors dynamically
			if (XML_AUTHOR.equals(sIdentifier)) {
				JComboBox jcb = new JComboBox(AuthorManager.getAuthorsList());
				jcb.setEditable(true);
				AutoCompleteDecorator.decorate(jcb);
				col.setCellEditor(new ComboBoxCellEditor(jcb));
			}
			// create a button for playing
			else if (XML_PLAY.equals(sIdentifier)) {
				col.setCellRenderer(new JajukCellRender());
				col.setMinWidth(PLAY_COLUMN_SIZE);
				col.setMaxWidth(PLAY_COLUMN_SIZE);
			} else if (XML_TRACK_RATE.equals(sIdentifier)) {
				col.setCellRenderer(new JajukCellRender());
				col.setMinWidth(RATE_COLUMN_SIZE);
				col.setMaxWidth(RATE_COLUMN_SIZE);
			}
		}
	}

	/**
	 * Detect property change
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == jcbProperty) {
			sAppliedFilter = jtfValue.getText();
			sAppliedCriteria = getApplyCriteria();
			applyFilter(sAppliedCriteria, sAppliedFilter);
		}

	}

	private void columnChange() {
		if (!bReloading) { // ignore this column change when reloading
			// model
			jtable.createColumnsConf();
		}
	}

	public void columnAdded(TableColumnModelEvent arg0) {
		columnChange();
	}

	public void columnRemoved(TableColumnModelEvent arg0) {
		columnChange();
	}

	public void columnMoved(TableColumnModelEvent arg0) {
	}

	public void columnMarginChanged(ChangeEvent arg0) {
	}

	public void columnSelectionChanged(ListSelectionEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
		String sKey = model.getIdentifier(e.getColumn());
		Object oValue = model.getValueAt(e.getFirstRow(), e.getColumn());
		/* can be Boolean or String */
		Item item = model.getItemAt(e.getFirstRow());
		try {
			// file filter used by physical table view to change only the
			// file, not all files associated with the track
			HashSet<Item> filter = null;
			if (item instanceof File) {
				filter = new HashSet<Item>();
				filter.add(item);
			}
			Item itemNew = ItemManager.changeItem(item, sKey, oValue, filter);
			model.setItemAt(e.getFirstRow(), itemNew); // update model
			// user message
			itemNew.getMeta(sKey);
			InformationJPanel.getInstance().setMessage(
					Messages.getString("PropertiesWizard.8") + ": "
							+ ItemManager.getHumanType(sKey), //$NON-NLS-1$ //$NON-NLS-2$
					InformationJPanel.INFORMATIVE);
			ObservationManager.notify(new Event(
					EventSubject.EVENT_DEVICE_REFRESH)); // TBI see later
			// for a smarter
			// event
		} catch (NoneAccessibleFileException none) {
			Messages.showErrorMessage(none.getCode());
			((JajukTableModel) jtable.getModel()).undo(e.getFirstRow(), e
					.getColumn());
		} catch (CannotRenameException cre) {
			Messages.showErrorMessage(cre.getCode()); //$NON-NLS-1$
			((JajukTableModel) jtable.getModel()).undo(e.getFirstRow(), e
					.getColumn());
		} catch (JajukException je) {
			Log.error("104", je); //$NON-NLS-1$
			Messages.showErrorMessage("104", je.getMessage()); //$NON-NLS-1$
			((JajukTableModel) jtable.getModel()).undo(e.getFirstRow(), e
					.getColumn());
		}
	}

	/**
	 * Table initialization after table display
	 * 
	 */
	abstract void initTable();

}
