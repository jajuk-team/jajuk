/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.wizard;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ext.AutoCompleteDecorator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.VerticalLayout;

/**
 * ItemManager properties wizard for any jajuk item
 */
public class PropertiesWizard extends JajukJDialog implements ITechnicalStrings, ActionListener {

  private static final long serialVersionUID = 1L;

  /* Main panel */
  JPanel jpMain;

  /** OK/Cancel panel */
  OKCancelPanel okc;

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
   *          items to display
   */
  public PropertiesWizard(ArrayList<Item> alItems) {
    // windows title: name of the element of only one item, or "selection"
    // word otherwise
    setTitle(alItems.size() == 1 ? (alItems.get(0)).getDesc() : Messages
        .getString("PropertiesWizard.6"));
    this.alItems = alItems;
    boolean bMerged = false;
    if (alItems.size() > 1) {
      bMerged = true;
    }
    panel1 = new PropertiesPanel(alItems, alItems.size() == 1 ? Util.getLimitedString((alItems
        .get(0)).getDesc(), 50) : Messages.getString("PropertiesWizard.6"), bMerged);
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,p:grow,5dlu",
        "1dlu,fill:p,5dlu,p,3dlu"));
    builder.add(panel1, cc.xy(2, 2));
    // OK/Cancel buttons
    okc = new OKCancelPanel(PropertiesWizard.this, Messages.getString("Apply"), Messages
        .getString("Close"));
    builder.add(okc, cc.xy(2, 4));
    jpMain = builder.getPanel();
    display();
  }

  /**
   * Constructor for file wizard for ie with 2 wizard panels and n items to
   * display
   * 
   * @param alItems1
   *          items to display in the first wizard panel (file for ie)
   * @param alItems2
   *          items to display in the second panel (associated track for ie )
   */
  public PropertiesWizard(ArrayList<Item> alItems1, ArrayList<Item> alItems2) {
    // windows title: name of the element of only one item, or "selection"
    // word otherwise
    setTitle(alItems1.size() == 1 ? (alItems1.get(0)).getDesc() : Messages
        .getString("PropertiesWizard.6"));
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
    if (alItems1.size() > 0) {
      if (alItems1.size() == 1) {
        panel1 = new PropertiesPanel(alItems1,
            Util.getLimitedString(alItems1.get(0).getDesc(), 50), false);
      } else {
        panel1 = new PropertiesPanel(alItems1, Util.formatPropertyDesc(Messages
            .getString("PropertiesWizard.6")), true);
      }
      panel1.setBorder(BorderFactory.createEtchedBorder());
    }
    if (alItems2.size() > 0) {
      if (alItems2.size() == 1) {
        panel2 = new PropertiesPanel(alItems2,
            Util.getLimitedString(alItems2.get(0).getDesc(), 50), false);
      } else {
        panel2 = new PropertiesPanel(alItems2, Util.formatPropertyDesc(alItems2.size() + " "
            + Messages.getString("Property_tracks")), true);
      }
      panel2.setBorder(BorderFactory.createEtchedBorder());
    }
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "2dlu,p:grow(0.5),5dlu,p:grow(0.5),2dlu", "1dlu,fill:p,5dlu,p,3dlu"));
    CellConstraints cc = new CellConstraints();
    builder.add(panel1, cc.xy(2, 2));
    // panel2 can be null for a void directory for instance
    if (panel2 != null) {
      builder.add(panel2, cc.xy(4, 2));
    }
    // OK/Cancel buttons
    okc = new OKCancelPanel(this, Messages.getString("Apply"), Messages.getString("Close"));
    builder.add(okc, cc.xy(2, 4));
    jpMain = builder.getPanel();
    display();
  }

  private void display() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // If none editable item, save button is disabled
        if (iEditable == 0) {
          okc.getOKButton().setEnabled(false);
        }
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
            Messages.showErrorMessage(104, ex.getMessage());
            Log.error(104, ex.getMessage(), ex);
          } finally {
            // -UI refresh-
            // clear tables selection
            ObservationManager.notify(new Event(EventSubject.EVENT_TABLE_CLEAR_SELECTION));
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
        || sKey.equals(XML_YEAR) || sKey.equals(XML_STYLE) || sKey.equals(XML_DIRECTORY)
        || sKey.equals(XML_FILE) || sKey.equals(XML_PLAYLIST) || sKey.equals(XML_PLAYLIST_FILE)
        || sKey.equals(XML_FILES) || sKey.equals(XML_PLAYLIST_FILES)
        // avoid confusing between music types and device types
        || (sKey.equals(XML_TYPE) && !(alItems.get(0) instanceof Device));
  }

  /**
   * 
   * A properties panel
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
     *          items to display
     * @param sDesc
     *          Description (title)
     * @param bMerged :
     *          whether this panel contains merged values
     */
    PropertiesPanel(ArrayList<Item> alItems, String sDesc, boolean bMerged) {
      this.alItems = alItems;
      this.bMerged = bMerged;
      Item pa = alItems.get(0);
      // first item Process properties to display
      alToDisplay = new ArrayList<PropertyMetaInformation>(10);
      for (PropertyMetaInformation meta : ItemManager.getItemManager(pa.getClass()).getProperties()) {
        // add only editable and non constructor properties
        if (meta.isVisible() && (bMerged ? meta.isMergeable() : true)) {
          // if more than one item to display, show only mergeable
          // properties
          alToDisplay.add(meta);
        }
      }
      // contains widgets for properties
      // Varname | value | link
      widgets = new JComponent[alToDisplay.size()][4];
      String horiz = "3dlu, p:grow(0.2), 5dlu, p:grow(0.8), 5dlu, p, 3dlu";
      // *2n+1 rows for spaces + 2 rows for title
      String vert = "5dlu,20"; // Y space + title
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
        vert += ",5dlu,20";
        Dimension dim = new Dimension(200, 20);
        // -Set widgets-
        // Property name
        String sName = meta.getHumanName();
        JLabel jlName = new JLabel(sName + " :");
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
              && !(pa instanceof Directory && !((Directory) pa).getDevice().isMounted())
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
          } else if (meta.getType().equals(Double.class) || meta.getType().equals(Integer.class)
              || meta.getType().equals(Long.class)) {
            // Note : we manage field validation by ourself, and we
            // don't use formatted textfields because they display
            // numbers with comas (this is wrong to display
            // years for instance)
            final JTextField jtfValue;
            jtfValue = new JTextField();
            jtfValue.addKeyListener(new KeyAdapter() {
              public void keyReleased(KeyEvent arg0) {
                if (jtfValue.getText().length() == 0) {
                  hmPropertyToChange.remove(meta);
                  return;
                }
                Object oValue = null;
                try {
                  if (meta.getType().equals(Long.class)) {
                    oValue = Long.parseLong(jtfValue.getText());
                  } else if (meta.getType().equals(Double.class)) {
                    oValue = Double.parseDouble(jtfValue.getText());
                  } else if (meta.getType().equals(Integer.class)) {
                    oValue = Integer.parseInt(jtfValue.getText());
                  }
                } catch (Exception e) {
                  Log.error(137, meta.getName(), null);
                  jtfValue.setText("");
                  Messages.showErrorMessage(137, meta.getName());
                  hmPropertyToChange.remove(meta);
                  return;
                }
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
              if (s.equals(sCurrentStyle)) {
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
                  Log.error(137, meta.getName(), null);
                  Messages.showErrorMessage(137, meta.getName());
                  return;
                }
                hmPropertyToChange.put(meta, oValue);
              }
            });
            widgets[index][1] = jcb;
          } else if (meta.getType().equals(String.class) && meta.getName().equals(XML_AUTHOR)) {
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
              if (s.equals(sCurrentAuthor)) {
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
                  Log.error(137, meta.getName(), null);
                  Messages.showErrorMessage(137, meta.getName());
                  return;
                }
                hmPropertyToChange.put(meta, oValue);
              }
            });
            widgets[index][1] = jcb;
          } else { // for all others formats (string, class)
            final JTextField jtfValue = new JTextField();
            jtfValue.addKeyListener(new KeyAdapter() {
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
            if (s.indexOf(",") != -1) {
              String[] sTab = s.split(",");
              StringBuilder sb = new StringBuilder();
              sb.append("<html>");
              for (int i = 0; i < sTab.length; i++) {
                sb.append("<p>").append(sTab[i]).append("</p>");
              }
              sb.append("</html>");
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
          JButton jbLink = new JButton(IconLoader.ICON_PROPERTIES);
          jbLink.addActionListener(this);
          jbLink.setActionCommand("link");
          jbLink.setToolTipText(Messages.getString("PropertiesWizard.12"));
          widgets[index][2] = jbLink;
        }
        index++;
      }
      // last row is a separator
      vert += ",5dlu";

      // Create layout
      PanelBuilder builder = new PanelBuilder(new FormLayout(horiz, vert));
      CellConstraints cc = new CellConstraints();
      // construct properties panel
      // Add title
      JLabel jlName = new JLabel("<html><b>" + Messages.getString("PropertiesWizard.1")
          + "</b></html>");
      jlName.setToolTipText(Messages.getString("PropertiesWizard.1"));
      JLabel jlValue = new JLabel("<html><b>" + Messages.getString("PropertiesWizard.2")
          + "</b></html>");
      jlValue.setToolTipText(Messages.getString("PropertiesWizard.2"));
      JLabel jlLink = new JLabel("<html><b>" + Messages.getString("PropertiesWizard.4")
          + "</b></html>");
      jlLink.setToolTipText(Messages.getString("PropertiesWizard.4"));

      builder.add(jlName, cc.xy(2, 2));
      builder.add(jlValue, cc.xy(4, 2));
      builder.add(jlLink, cc.xy(6, 2));
      // Add widgets
      int i = 0;
      int j = 4;
      for (PropertyMetaInformation meta : alToDisplay) {
        builder.add(widgets[i][0], cc.xy(2, j));
        builder.add(widgets[i][1], cc.xy(4, j));
        if (widgets[i][2] != null) { // link widget can be null
          builder.add(widgets[i][2], cc.xy(6, j));
        }
        i++;
        j += 2;
      }
      jpProperties = builder.getPanel();

      setLayout(new VerticalLayout(10));
      // desc
      jlDesc = new JLabel(Util.formatPropertyDesc(sDesc));
      add(jlDesc);
      add(jpProperties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
      // Link
      if (ae.getActionCommand().equals("link")) {
        PropertyMetaInformation meta = alToDisplay.get(getWidgetIndex((JComponent) ae.getSource()));
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
          StringTokenizer st = new StringTokenizer(sValue, ",");
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
      // TODO refactor this using LinkedHashset for ie
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
      String sDetails = "";
      // Now we have all items to consider, write tags for each
      // property to change
      for (PropertyMetaInformation meta : hmPropertyToChange.keySet()) {
        ArrayList<Item> alIntermediate = new ArrayList<Item>(alItemsToCheck.size());
        for (Item item : alItemsToCheck) {
          // New value
          oValue = hmPropertyToChange.get(meta);
          // Check it is not null for non custom properties. Note that
          // we also allow void values for comments
          if (oValue == null || (oValue.toString().trim().length() == 0)
              && !(meta.isCustom() || meta.getName().equals(XML_TRACK_COMMENT))) {
            Log.error(137, '{' + meta.getName() + '}', null);
            Messages.showErrorMessage(137, '{' + meta.getName() + '}');
            return;
          }
          // Old value
          String sOldValue = item.getHumanValue(meta.getName());
          if ((sOldValue != null && !Util.format(oValue, meta, true).equals(sOldValue))) {
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
              Messages.showErrorMessage(cre.getCode());
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
                  sDetails += "\nCaused by:" + je.getCause();
                  if (je.getCause().getCause() != null) {
                    sDetails += "\nCaused by:" + je.getCause().getCause();
                    if (je.getCause().getCause().getCause() != null) {
                      sDetails += "\nCaused by:" + je.getCause().getCause().getCause();
                    }
                  }
                }
                sDetails += "\n\n";
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
         * Display a warning message if some files not updated if multifile mode
         * note that this message will appear only for first item in failure,
         * after, current track will have changed and will no more contain
         * unmounted files
         */
        if (!isMonoFile() && TrackManager.getInstance().isChangePbm()) {
          Messages.showWarningMessage(Messages.getString("Error.138"));
        }
      }
      // display a message for file write issues
      if (alInError.size() > 0) {
        String sInfo = "";
        int index = 0;
        for (Item item : alInError) {
          // limit number of errors
          if (index == 15) {
            sInfo += "\n...";
            break;
          }
          sInfo += "\n" + item.getHumanValue(XML_NAME);
          index++;
        }
        Messages.showDetailedErrorMessage(104, sInfo, sDetails);
      }

      // display a message if user changed at least one property
      if (alChanged.size() > 0) {
        StringBuilder sbChanged = new StringBuilder();
        sbChanged.append("{");
        for (PropertyMetaInformation meta : alChanged) {
          sbChanged.append(meta.getHumanName()).append(' ');
        }
        sbChanged.append('}');
        InformationJPanel.getInstance().setMessage(
            alChanged.size() + " " + Messages.getString("PropertiesWizard.10") + ": "
                + sbChanged.toString(), InformationJPanel.INFORMATIVE);
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
