/*
 *  Jajuk
 *  Copyright (C) 2005 bertrand florat
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

package org.jajuk.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

/**
 * ItemManager properties wizard for any jajuk item
 * 
 * @author Bertrand Florat
 * @created 6 juin 2005
 * @TODO Use sets instead of lists
 */
public class PropertiesWizard extends JDialog implements ITechnicalStrings, ActionListener {

	private static final long serialVersionUID = 1L;

	/* Main panel */
	JPanel jpMain;

	/** OK/Cancel panel */
	OKCancelPanel okc;

	/** Layout dimensions */
	double[][] dSize = { { 0, TableLayout.PREFERRED, 10 }, { 0, TableLayout.PREFERRED, 10, 20, 20 } };

	/** Items */
	ArrayList<Item> alItems;

	/** Items2 */
	ArrayList<Item> alItems2;

	/** Files filter */
	HashSet<File> filter = null;

	/** number of editable items (all panels) */
	int iEditable = 0;

	/** First property panel */
	PropertiesPanel panel1;

	/** Second property panel */
	PropertiesPanel panel2;

	/**
	 * Constructor for normal wizard with only one wizard panel and n items to
	 * display
	 * 
	 * @param alItems
	 *            items to display
	 */
	public PropertiesWizard(ArrayList<Item> alItems) {
		// windows title: name of the element of only one item, or "selection"
		// word otherwise
		super(Main.getWindow(), alItems.size() == 1 ? (alItems.get(0)).getDesc() : Messages
				.getString("PropertiesWizard.6"), true); // modal
		this.alItems = alItems;
		boolean bMerged = false;
		if (alItems.size() > 1) {
			bMerged = true;
		}
		panel1 = new PropertiesPanel(alItems, alItems.size() == 1 ? (alItems.get(0)).getDesc()
				: Messages.getString("PropertiesWizard.6"), bMerged); //$NON-NLS-1$
		jpMain = new JPanel();
		jpMain.setLayout(new TableLayout(dSize));
		jpMain.add(panel1, "1,1"); //$NON-NLS-1$
		display();
	}

	/**
	 * Constructor for file wizard for ie with 2 wizard panels and n items to
	 * display
	 * 
	 * @param alItems1
	 *            items to display in the first wizard panel (file for ie)
	 * @param alItems2
	 *            items to display in the second panel (associated track for ie )
	 */
	public PropertiesWizard(ArrayList<Item> alItems1, ArrayList<Item> alItems2) {
		// windows title: name of the element of only one item, or "selection"
		// word otherwise
		super(Main.getWindow(), alItems1.size() == 1 ? (alItems1.get(0)).getDesc() : Messages
				.getString("PropertiesWizard.6"), true); // modal
		this.alItems = alItems1;
		this.alItems2 = alItems2;
		// computes filter
		if (alItems1.get(0) instanceof Directory) {
			filter = new HashSet<File>(alItems1.size() * 10);
			for (Item item : alItems1) {
				Directory dir = (Directory) item;
				filter.addAll(dir.getFilesRecursively());
			}
		} else if (alItems1.get(0) instanceof File) {
			filter = new HashSet<File>(alItems1.size());
			for (Item item : alItems1) {
				filter.add((File) item);
			}
		} else {
			filter = null;
		}
		jpMain = new JPanel();
		jpMain.setLayout(new TableLayout(dSize));
		JPanel jpProperties = new JPanel();
		double[][] dPanels = { { TableLayout.PREFERRED, 20, TableLayout.PREFERRED },
				{ TableLayout.PREFERRED } };
		jpProperties.setLayout(new TableLayout(dPanels));
		if (alItems1.size() > 0) {
			if (alItems1.size() == 1) {
				panel1 = new PropertiesPanel(alItems1, alItems1.get(0).getDesc(), false);
			} else {
				panel1 = new PropertiesPanel(alItems1, Util.formatPropertyDesc(Messages
						.getString("PropertiesWizard.6")), true); //$NON-NLS-1$
			}
			panel1.setBorder(BorderFactory.createEtchedBorder());
			jpProperties.add(panel1, "0,0"); //$NON-NLS-1$
		}
		if (alItems2.size() > 0) {
			if (alItems2.size() == 1) {
				panel2 = new PropertiesPanel(alItems2, alItems2.get(0).getDesc(), false);
			} else {
				panel2 = new PropertiesPanel(alItems2, Util.formatPropertyDesc(alItems2.size()
						+ " " + Messages.getString("Property_tracks")), true); //$NON-NLS-1$ //$NON-NLS-2$
			}
			panel2.setBorder(BorderFactory.createEtchedBorder());
			jpProperties.add(panel2, "2,0"); //$NON-NLS-1$
		}
		jpMain.add(jpProperties, "1,1"); //$NON-NLS-1$
		display();
	}

	private void display() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// OK/Cancel buttons
				okc = new OKCancelPanel(PropertiesWizard.this,
						Messages.getString("Apply"), Messages.getString("Close")); //$NON-NLS-1$ //$NON-NLS-2$
				// If none editable item, save button is disabled
				if (iEditable == 0) {
					okc.getOKButton().setEnabled(false);
				}
				jpMain.add(okc, "1,3"); //$NON-NLS-1$
				getRootPane().setDefaultButton(okc.getOKButton());
				getContentPane().add(new JScrollPane(jpMain));
				pack();
				setLocationRelativeTo(Main.getWindow());
				setVisible(true);
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == okc.getCancelButton()) {
			dispose();
		} else if (e.getSource().equals(okc.getOKButton())) {
			dispose(); // close window, otherwise you will have some issues if
			// fields are not updated with changes
			new Thread() {
				public void run() {
					try {
						PropertiesWizard.this.panel1.save();
						if (panel2 != null) {
							PropertiesWizard.this.panel2.save();
						}
					} catch (Exception ex) {
						Messages.showErrorMessage("104", ex.getMessage()); //$NON-NLS-1$
						Log.error("104", ex.getMessage(), ex); //$NON-NLS-1$
						return;
					} finally {
						// -UI refresh-
						// clear tables selection
						ObservationManager.notify(new Event(
								EventSubject.EVENT_TABLE_CLEAR_SELECTION));
						ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
					}
				}
			}.start();
		}
	}

	/**
	 * Tells whether a link button should be shown for a given property
	 * 
	 * @param meta
	 * @return
	 */
	public boolean isLinkable(PropertyMetaInformation meta) {
		String sKey = meta.getName();
		return sKey.equals(XML_DEVICE) || sKey.equals(XML_TRACK) || sKey.equals(XML_DEVICE)
				|| sKey.equals(XML_TRACK) || sKey.equals(XML_ALBUM) || sKey.equals(XML_AUTHOR)
				|| sKey.equals(XML_STYLE) || sKey.equals(XML_DIRECTORY) || sKey.equals(XML_FILE)
				|| sKey.equals(XML_PLAYLIST) || sKey.equals(XML_PLAYLIST_FILE)
				|| sKey.equals(XML_FILES) || sKey.equals(XML_PLAYLIST_FILES)
				// avoid confusing between music types and device types
				|| (sKey.equals(XML_TYPE) && !(alItems.get(0) instanceof Device));
	}

	/**
	 * 
	 * A properties panel
	 * 
	 * @author Bertrand Florat
	 * 
	 */
	class PropertiesPanel extends JPanel implements ActionListener {

		private static final long serialVersionUID = 1L;

		/** Properties panel */
		JPanel jpProperties;

		/** ItemManager description */
		JLabel jlDesc;

		/** All dynamic widgets */
		JComponent[][] widgets;

		/** Properties to display */
		ArrayList<PropertyMetaInformation> alToDisplay;

		/** Items */
		ArrayList<Item> alItems;

		/** Changed properties */
		HashMap<PropertyMetaInformation, Object> hmPropertyToChange = new HashMap<PropertyMetaInformation, Object>();

		/** Merge flag */
		boolean bMerged = false;

		/**
		 * Property panel for single types elements
		 * 
		 * @param alItems
		 *            items to display
		 * @param sDesc
		 *            Description (title)
		 * @param bMerged :
		 *            whether this panel contains merged values
		 */
		PropertiesPanel(ArrayList<Item> alItems, String sDesc, boolean bMerged) {
			int iX_SEPARATOR = 5;
			int iY_SEPARATOR = 10;
			this.alItems = alItems;
			this.bMerged = bMerged;
			Item pa = alItems.get(0);
			// first item Process properties to display
			alToDisplay = new ArrayList<PropertyMetaInformation>(10);
			for (PropertyMetaInformation meta : ItemManager.getItemManager(pa.getClass())
					.getProperties()) {
				// add only editable and non constructor properties
				if (meta.isVisible() && (bMerged ? meta.isMergeable() : true)) {
					// if more than one item to display, show only mergeable
					// properties
					alToDisplay.add(meta);
				}
			}
			// contains widgets for properties
			// Varname | value | link | all album
			widgets = new JComponent[alToDisplay.size()][4];
			double p = TableLayout.PREFERRED;
			double[] dHoriz = { iX_SEPARATOR, p, iX_SEPARATOR, p, iX_SEPARATOR, p,iX_SEPARATOR };
			// *2n+1 rows for spaces + 2 rows for title
			double[] dVert = new double[(2 * alToDisplay.size()) + 3];
			dVert[0] = iY_SEPARATOR;
			dVert[1] = 20; // title
			int index = 0;
			for (final PropertyMetaInformation meta : alToDisplay) {
				// begin by checking if all items have the same value, otherwise
				// we show a void field
				boolean bAllEquals = true;
				Object oRef = pa.getValue(meta.getName());
				for (Item item : alItems) {
					if (!item.getValue(meta.getName()).equals(oRef)) {
						bAllEquals = false;
						break;
					}
				}
				// Set layout
				dVert[2 * index + 2] = iY_SEPARATOR;
				dVert[(2 * index) + 3] = 20;
				Dimension dim = new Dimension(200, 20);
				// -Set widgets-
				// Property name
				String sName = meta.getHumanName();
				JLabel jlName = new JLabel(sName + " :"); //$NON-NLS-1$
				// Check if property name is translated (for custom
				// properties));
				if (meta.isCustom()) {
					jlName.setForeground(Color.BLUE);
				}
				// Property value computes editable state
				widgets[index][0] = jlName;
				// property editable ?
				boolean bEditable = meta.isEditable();
				// Check meta-data is supported for the file type
				if (pa instanceof Track) {
					Track track = (Track) pa;
					// take any file mapping this track (note all files are of
					// the same type)
					File file = track.getFiles().get(0);
					if (file.getType().getTaggerClass() == null) {
						bEditable = false;
					}
				}
				if (!meta.isCustom()) {
					// (custom properties are always editable, even for offline
					// items)
					bEditable = bEditable
							&& !(pa instanceof Directory && !((Directory) pa).getDevice()
									.isMounted())
							// item is not an unmounted dir
							&& !(pa instanceof File
							// check item is not an unmounted file
							&& !((File) pa).isReady())
							// item is not an unmounted playlist file
							&& !(pa instanceof PlaylistFile && !((PlaylistFile) pa).isReady());
				}
				if (bEditable) {
					iEditable++;
					if (meta.getType().equals(Date.class)) {
						final JXDatePicker jdp = new JXDatePicker();
						jdp.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								Object oValue = jdp.getDate();
								hmPropertyToChange.put(meta, oValue);
							}
						});
						if (bAllEquals) {
							// If several items, take first value found
							jdp.setDate(new Date(pa.getDateValue(meta.getName()).getTime()));
						} else {
							jdp.setDate(new Date(System.currentTimeMillis()));
						}
						widgets[index][1] = jdp;
					} else if (meta.getType().equals(Boolean.class)) {
						// for a boolean, value is a checkbox
						final JCheckBox jcb = new JCheckBox();
						jcb.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								Object oValue = jcb.isSelected();
								hmPropertyToChange.put(meta, oValue);
							}
						});
						if (bAllEquals) {
							jcb.setSelected(pa.getBooleanValue(meta.getName()));
						}
						// if some elements are different, set opposite value of
						// first item to allow change
						else {
							jcb.setSelected(!pa.getBooleanValue(meta.getName()));
						}
						widgets[index][1] = jcb;
					} else if (meta.getType().equals(Double.class)
							|| meta.getType().equals(Long.class)) {
						// for a double, value is a formatted textfield
						final JFormattedTextField jtfValue;
						if (meta.getType().equals(Double.class)) {
							jtfValue = new JFormattedTextField(NumberFormat.getInstance());
						} else {
							jtfValue = new JFormattedTextField(NumberFormat.getIntegerInstance());
						}
						jtfValue.addKeyListener(new KeyListener() {
							public void keyTyped(KeyEvent arg0) {
							}

							public void keyPressed(KeyEvent arg0) {
							}

							public void keyReleased(KeyEvent arg0) {
								if (jtfValue.getText().length() == 0) {
									hmPropertyToChange.remove(meta);
									return;
								}
								try {
									jtfValue.commitEdit();
								} catch (ParseException e) {
									Log.error("137", meta.getName(), null); //$NON-NLS-1$
									// disable field to avoid that user
									// typing enter in error dialog
									// generate a new key event and creates
									// a looping error scheme
									jtfValue.setEnabled(false);
									Messages.showErrorMessage("137", meta.getName()); //$NON-NLS-1$
									jtfValue.setEnabled(true);
									hmPropertyToChange.remove(meta);
									return;
								}
								Object oValue = jtfValue.getValue();
								hmPropertyToChange.put(meta, oValue);
							}
						});
						if (bAllEquals) {
							jtfValue.setText(pa.getHumanValue(meta.getName()));
							// If several items, take first value found
						}
						jtfValue.setPreferredSize(dim);
						widgets[index][1] = jtfValue;
					} else if (meta.getType().equals(String.class)
					// for styles
							&& meta.getName().equals(XML_STYLE)) {
						Vector<String> styles = StyleManager.getInstance().getStylesList();
						final JComboBox jcb = new JComboBox(styles);
						jcb.setEditable(true);
						AutoCompleteDecorator.decorate(jcb);
						jcb.setPreferredSize(dim);
						// set current style to combo
						int i = -1;
						int comp = 0;
						String sCurrentStyle = pa.getHumanValue(XML_STYLE);
						for (String s : styles) {
							if (s.equalsIgnoreCase(sCurrentStyle)) {
								i = comp;
								break;
							}
							comp++;
						}
						jcb.setSelectedIndex(i);
						// if different style, don't show anything
						if (!bAllEquals) {
							jcb.setSelectedIndex(-1);
						}
						jcb.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								Object oValue = jcb.getSelectedItem();
								if (oValue == null || ((String) oValue).trim().length() == 0) {
									// can occur during ui interaction
									return;
								}
								// check that string length > 0
								if (((String) oValue).length() < 1) {
									jcb.setSelectedIndex(-1);
									Log.error("137", meta.getName(), null); //$NON-NLS-1$
									Messages.showErrorMessage("137", meta.getName()); //$NON-NLS-1$
									return;
								}
								hmPropertyToChange.put(meta, oValue);
							}
						});
						widgets[index][1] = jcb;
					} else if (meta.getType().equals(String.class)
							&& meta.getName().equals(XML_AUTHOR)) {
						// for authors
						Vector<String> authors = AuthorManager.getAuthorsList();
						final JComboBox jcb = new JComboBox(authors);
						jcb.setEditable(true);
						AutoCompleteDecorator.decorate(jcb);
						jcb.setPreferredSize(dim);
						// set current style to combo
						int i = -1;
						int comp = 0;
						String sCurrentAuthor = pa.getHumanValue(XML_AUTHOR);
						for (String s : authors) {
							if (s.equalsIgnoreCase(sCurrentAuthor)) {
								i = comp;
								break;
							}
							comp++;
						}
						jcb.setSelectedIndex(i);
						// if different author, don't show anything
						if (!bAllEquals) {
							jcb.setSelectedIndex(-1);
						}
						jcb.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								Object oValue = jcb.getSelectedItem();
								if (oValue == null || ((String) oValue).trim().length() == 0) {
									// can occur during ui interaction
									return;
								}
								// check that string length > 0
								if (((String) oValue).length() < 1) {
									jcb.setSelectedIndex(-1);
									Log.error("137", meta.getName(), null); //$NON-NLS-1$
									Messages.showErrorMessage("137", meta.getName()); //$NON-NLS-1$
									return;
								}
								hmPropertyToChange.put(meta, oValue);
							}
						});
						widgets[index][1] = jcb;
					} else { // for all others formats (string, class)
						final JTextField jtfValue = new JTextField();
						jtfValue.addKeyListener(new KeyListener() {

							public void keyTyped(KeyEvent arg0) {
							}

							public void keyPressed(KeyEvent arg0) {
							}

							public void keyReleased(KeyEvent arg0) {
								String value = jtfValue.getText();
								hmPropertyToChange.put(meta, value);
							}
						});
						if (bAllEquals) {
							// If several items, take first value found
							jtfValue.setText(pa.getHumanValue(meta.getName()));
						}
						jtfValue.setPreferredSize(dim);
						widgets[index][1] = jtfValue;
					}
				} else {
					JLabel jl = new JLabel(pa.getHumanValue(meta.getName()));
					// If several items, take first value found
					if (bAllEquals) {
						String s = pa.getHumanValue(meta.getName());
						if (s.indexOf(",") != -1) { //$NON-NLS-1$
							String[] sTab = s.split(","); //$NON-NLS-1$
							StringBuffer sb = new StringBuffer();
							sb.append("<html>"); //$NON-NLS-1$
							for (int i = 0; i < sTab.length; i++) {
								sb.append("<p>").append(sTab[i]).append("</p>"); //$NON-NLS-1$ //$NON-NLS-2$
							}
							sb.append("</html>"); //$NON-NLS-1$
							jl.setToolTipText(sb.toString());
						} else {
							jl.setToolTipText(s);
						}
					}
					jl.setPreferredSize(dim);
					widgets[index][1] = jl;

				}
				// Link
				if (isLinkable(meta)) {
					JButton jbLink = new JButton(Util.getIcon(ICON_PROPERTIES));
					jbLink.addActionListener(this);
					jbLink.setActionCommand("link"); //$NON-NLS-1$
					jbLink.setToolTipText(Messages.getString("PropertiesWizard.12")); //$NON-NLS-1$
					widgets[index][2] = jbLink;
				}
				index++;
			}
			if (dVert.length > 0) {
				dVert[dVert.length - 1] = iY_SEPARATOR;// last row is a
				// separator
			}

			// Create layout
			double[][] dSizeProperties = new double[][] { dHoriz, dVert };
			dSizeProperties[0] = dHoriz;
			dSizeProperties[1] = dVert;
			// construct properties panel
			jpProperties = new JPanel();
			jpProperties.setLayout(new TableLayout(dSizeProperties));
			// Add title
			JLabel jlName = new JLabel(
					"<html><b>" + Messages.getString("PropertiesWizard.1") + "</b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			jlName.setToolTipText(Messages.getString("PropertiesWizard.1")); //$NON-NLS-1$
			JLabel jlValue = new JLabel(
					"<html><b>" + Messages.getString("PropertiesWizard.2") + "</b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			jlValue.setToolTipText(Messages.getString("PropertiesWizard.2")); //$NON-NLS-1$
			JLabel jlLink = new JLabel(
					"<html><b>" + Messages.getString("PropertiesWizard.4") + "</b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			jlLink.setToolTipText(Messages.getString("PropertiesWizard.4")); //$NON-NLS-1$
			JLabel jlType = new JLabel(
					"<html><b>" + Messages.getString("PropertiesWizard.7") + "</b></html>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			jlType.setToolTipText(Messages.getString("PropertiesWizard.7")); //$NON-NLS-1$

			jpProperties.add(jlName, "1,1,c,c"); //$NON-NLS-1$
			jpProperties.add(jlValue, "3,1,c,c"); //$NON-NLS-1$
			jpProperties.add(jlLink, "5,1,c,c"); //$NON-NLS-1$
			jpProperties.add(jlType, "7,1,c,c"); //$NON-NLS-1$

			// Add widgets
			int i = 0;
			int j = 2;
			for (PropertyMetaInformation meta : alToDisplay) {
				j = (2 * i) + 3;
				jpProperties.add(widgets[i][0], "1," + j + ",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
				jpProperties.add(widgets[i][1], "3," + j); //$NON-NLS-1$
				if (widgets[i][2] != null) { // link widget can be null
					jpProperties.add(widgets[i][2], "5," + j + ",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				i++;
			}
			double[][] dSize = { { 0.99 }, { 20, iY_SEPARATOR, 0.99 } };
			setLayout(new TableLayout(dSize));
			// desc
			jlDesc = new JLabel(Util.formatPropertyDesc(sDesc));
			add(jlDesc, "0,0"); //$NON-NLS-1$
			add(jpProperties, "0,2"); //$NON-NLS-1$
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae) {
			// Link
			if (ae.getActionCommand().equals("link")) { //$NON-NLS-1$
				PropertyMetaInformation meta = alToDisplay.get(getWidgetIndex((JComponent) ae
						.getSource()));
				String sProperty = meta.getName();
				if (XML_FILES.equals(sProperty)) {
					Track track = (Track) alItems.get(0);
					ArrayList<Item> alFiles = new ArrayList<Item>(track.getFiles().size());
					alFiles.addAll(track.getFiles());
					// show properties window for this item
					new PropertiesWizard(alFiles);
				} else if (XML_PLAYLIST_FILES.equals(sProperty)) {
					// can only be a set a files
					String sValue = alItems.get(0).getStringValue(sProperty);
					StringTokenizer st = new StringTokenizer(sValue, ","); //$NON-NLS-1$
					ArrayList<Item> alItems = new ArrayList<Item>(3);
					while (st.hasMoreTokens()) {
						String sPlf = st.nextToken();
						Item pa = PlaylistFileManager.getInstance().getPlaylistFileByID(sPlf);
						if (pa != null) {
							alItems.add(pa);
						}
					}
					new PropertiesWizard(alItems);
					// show properties window for this item
				} else {
					String sValue = alItems.get(0).getStringValue(sProperty);
					// can be only an ID
					Item pa = ItemManager.getItemManager(sProperty).getItemByID(sValue);
					if (pa != null) {
						ArrayList<Item> alItems = new ArrayList<Item>(1);
						alItems.add(pa);
						// show properties window for this item
						new PropertiesWizard(alItems);
					}
				}
			}
		}

		/**
		 * Save changes in tags
		 */
		protected void save() throws Exception {
			Util.waiting();
			Object oValue = null;
			Item newItem = null;
			// list of really changed tracks (for message)
			ArrayList<PropertyMetaInformation> alChanged = new ArrayList<PropertyMetaInformation>(2);
			// none change, leave
			if (hmPropertyToChange.keySet().size() == 0) {
				return;
			}
			// Computes all items to change
			// contains items to be changed
			ArrayList<Item> alItemsToCheck = new ArrayList<Item>(alItems.size());
			for (Item item : alItems) {
				// avoid duplicates for perfs
				if (!alItemsToCheck.contains(item)) {
					// add item
					alItemsToCheck.add(item);
				}
			}
			ArrayList<Item> alInError = new ArrayList<Item>(alItemsToCheck.size());
			// details for errors
			String sDetails = ""; //$NON-NLS-1$
			// Now we have all items to considere, write tags for each
			// property to change
			for (PropertyMetaInformation meta : hmPropertyToChange.keySet()) {
				ArrayList<Item> alIntermediate = new ArrayList<Item>(alItemsToCheck.size());
				for (Item item : alItemsToCheck) {
					// New value
					oValue = hmPropertyToChange.get(meta);
					// Check it is not null for non custom properties
					if (oValue == null || (oValue.toString().trim().length() == 0)
							&& !meta.isCustom()) {
						Log.error("137", meta.getName(), null); //$NON-NLS-1$
						Messages.showErrorMessage("137", meta.getName()); //$NON-NLS-1$
						return;
					}
					// Old value
					String sOldValue = item.getHumanValue(meta.getName());
					if ((sOldValue != null && !Util.format(oValue, meta).equals(sOldValue))) {
						try {
							// if we change track properties for only one file
							newItem = ItemManager.changeItem(item, meta.getName(), oValue, filter);
						}
						// none accessible file for this track, for this error,
						// we display an error and leave completely
						catch (NoneAccessibleFileException none) {
							none.printStackTrace();
							Messages.showErrorMessage(none.getCode(), item.getHumanValue(XML_NAME));
							// close window to avoid reseting all properties to
							// old values
							dispose();
							return;
						}
						// cannot rename file, for this error, we display an
						// error and leave completely
						catch (CannotRenameException cre) {
							Messages.showErrorMessage(cre.getCode()); //$NON-NLS-1$
							return;
						}
						// probably error writing a tag, store track reference
						// and continue
						catch (JajukException je) {
							Log.error(je);
							if (!alInError.contains(item)) {
								alInError.add(item);
								// create details label with 3 levels deep
								sDetails += je.getMessage();
								if (je.getCause() != null) {
									sDetails += "\nCaused by:" + je.getCause(); //$NON-NLS-1$
									if (je.getCause().getCause() != null) {
										sDetails += "\nCaused by:" + je.getCause().getCause(); //$NON-NLS-1$
										if (je.getCause().getCause().getCause() != null) {
											sDetails += "\nCaused by:" + je.getCause().getCause().getCause(); //$NON-NLS-1$
										}
									}
								}
								sDetails += "\n\n"; //$NON-NLS-1$
							}
							continue;
						}
						// if this item was element of property panel elements,
						// update it
						if (alItems.contains(item)) {
							alItems.remove(item);
							alItems.add(newItem);
						}
						// add the new item in intermediate pool used for next
						// property change
						// note that if an error occurs in a property change,
						// the item will not be taken into account for next
						// property change
						alIntermediate.add(newItem);

						// if individual item, change title in case of
						// constructor element change
						if (!bMerged) {
							jlDesc.setText(Util.formatPropertyDesc(newItem.getDesc()));
						}
						// note this property have been changed
						if (!alChanged.contains(meta)) {
							alChanged.add(meta);
						}
					}
				}
				alItemsToCheck = alIntermediate;
				/*
				 * Display a warning message if some files not updated if
				 * multifile mode note that this message will appear only for
				 * first item in failure, after, current track will have changed
				 * and will no more contain unmounted files
				 */
				if (!isMonoFile() && TrackManager.getInstance().isChangePbm()) {
					Messages.showWarningMessage(Messages.getString("Error.138")); //$NON-NLS-1$
				}
			}
			// display a message for file write issues
			if (alInError.size() > 0) {
				String sInfo = ""; //$NON-NLS-1$
				int index = 0;
				for (Item item : alInError) {
					// limit number of errors
					if (index == 15) {
						sInfo += "\n..."; //$NON-NLS-1$
						break;
					}
					sInfo += "\n" + item.getHumanValue(XML_NAME); //$NON-NLS-1$
					index++;
				}
				Messages.showDetailedErrorMessage("104", sInfo, sDetails); //$NON-NLS-1$
			}

			// display a message if user changed at least one property
			if (alChanged.size() > 0) {
				StringBuffer sbChanged = new StringBuffer();
				sbChanged.append("{"); //$NON-NLS-1$
				for (PropertyMetaInformation meta : alChanged) {
					sbChanged.append(meta.getHumanName()).append(' ');
				}
				sbChanged.append('}');
				InformationJPanel
						.getInstance()
						.setMessage(
								alChanged.size()
										+ " " + Messages.getString("PropertiesWizard.10") + ": " + sbChanged.toString(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								InformationJPanel.INFORMATIVE);
			}
			Util.stopWaiting();
		}

		/**
		 * 
		 * @param widget
		 * @return index of a given widget in the widget table
		 */
		private int getWidgetIndex(JComponent widget) {
			int resu = -1;
			for (int row = 0; row < widgets.length; row++) {
				for (int col = 0; col < widgets[0].length; col++) {
					if (widget.equals(widgets[row][col])) {
						resu = row;
						break;
					}
				}

			}
			return resu;
		}
	}

	/**
	 * Tell if this wizard affects only one single file or logical items
	 * 
	 * @return Returns the bMultifile.
	 */
	private boolean isMonoFile() {
		// mono file mode for files and directories
		return alItems.get(0) instanceof File || alItems.get(0) instanceof Directory;
	}
}
