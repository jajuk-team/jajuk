/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

package org.jajuk.ui.wizard;

import ext.AutoCompleteDecorator;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.AlbumArtistManager;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.GenreManager;
import org.jajuk.base.Item;
import org.jajuk.base.ItemManager;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.widgets.CopyableLabel;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.VerticalLayout;

/**
 * ItemManager properties dialog for any jajuk item.
 */
public class PropertiesDialog extends JajukJDialog implements ActionListener {

  /** The Constant PROPERTIES_WIZARD_6. DOCUMENT_ME */
  private static final String PROPERTIES_WIZARD_6 = "PropertiesWizard.6";

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /* Main panel */
  /** DOCUMENT_ME. */
  private JPanel jpMain;

  /** OK/Cancel panel. */
  private OKCancelPanel okc;

  /** Items. */
  private List<Item> alItems;

  /** Items2. */
  private List<Item> alItems2;

  /** Files filter. */
  private Set<File> filter = null;

  /** number of editable items (all panels). */
  private int iEditable = 0;

  /** First property panel. */
  private PropertiesPanel panel1;

  /** Second property panel. */
  private PropertiesPanel panel2;

  /** Did user chnaged something ?. */
  private boolean changes = false;

  /**
   * Constructor for normal wizard with only one wizard panel and n items to
   * display.
   * 
   * @param alItems items to display
   */
  public PropertiesDialog(List<Item> alItems) {
    super();

    // windows title: name of the element if there is
    // only one item, or "selection" word otherwise
    if (alItems.size() == 1) {
      setTitle(alItems.get(0).getDesc());
    } else {
      setTitle(Messages.getString(PROPERTIES_WIZARD_6));
    }
    this.alItems = alItems;
    boolean bMerged = false;
    if (alItems.size() > 1) {
      bMerged = true;
    }
    panel1 = new PropertiesPanel(alItems, alItems.size() == 1 ? UtilString.getLimitedString(alItems
        .get(0).getDesc(), 50) : Messages.getString(PROPERTIES_WIZARD_6) + " [" + alItems.size()
        + "]", bMerged);
    // OK/Cancel buttons
    okc = new OKCancelPanel(PropertiesDialog.this, Messages.getString("Apply"), Messages
        .getString("Close"));
    // Add items
    jpMain = new JPanel(new MigLayout("insets 5,gapx 5,gapy 5", "[grow]"));
    jpMain.add(panel1, "grow,wrap");
    jpMain.add(okc, "span,right");
    display();
  }

  /**
   * Constructor for file wizard for ie with 2 wizard panels and n items to
   * display.
   * 
   * @param alItems1 items to display in the first wizard panel (file for ie)
   * @param alItems2 items to display in the second panel (associated track for ie )
   */
  public PropertiesDialog(List<Item> alItems1, List<Item> alItems2) {
    super();

    // windows title: name of the element of only one item, or "selection"
    // word otherwise
    setTitle(alItems1.size() == 1 ? alItems1.get(0).getDesc() : Messages
        .getString(PROPERTIES_WIZARD_6));
    this.alItems = alItems1;
    this.alItems2 = alItems2;
    if (alItems1.size() > 0) {
      // computes filter
      refreshFileFilter();
      if (alItems1.size() == 1) {
        panel1 = new PropertiesPanel(alItems1, UtilString.getLimitedString(alItems1.get(0)
            .getDesc(), 50), false);
      } else {
        panel1 = new PropertiesPanel(alItems1, UtilString.formatPropertyDesc(Messages
            .getString(PROPERTIES_WIZARD_6)
            + " [" + alItems.size() + "]"), true);
      }
      panel1.setBorder(BorderFactory.createEtchedBorder());
    }
    if (alItems2.size() > 0) {
      if (alItems2.size() == 1) {
        panel2 = new PropertiesPanel(alItems2, UtilString.getLimitedString(alItems2.get(0)
            .getDesc(), 50), false);
      } else {
        panel2 = new PropertiesPanel(alItems2, UtilString.formatPropertyDesc(alItems2.size() + " "
            + Messages.getHumanPropertyName(Const.XML_TRACKS)), true);
      }
      panel2.setBorder(BorderFactory.createEtchedBorder());
    }
    // OK/Cancel buttons
    okc = new OKCancelPanel(this, Messages.getString("Apply"), Messages.getString("Close"));

    // Add items
    jpMain = new JPanel(new MigLayout("insets 5,gapx 5,gapy 5", "[grow][grow]"));
    jpMain.add(panel1, "grow");
    // panel2 can be null for a void directory for instance
    if (panel2 != null) {
      jpMain.add(panel2, "grow,wrap");
    }
    // Use cell tag because the wrap is not done if panel2 is void
    jpMain.add(okc, "cell 0 1 1 1,span,right");

    display();
  }

  /**
   * Refresh the file filter used to update only selected files even
   * if the associated track is changed and can map several files.
   * Note that this method should be called after a file panel save
   * because files may have changed then (if user changed the file name).
   */
  private void refreshFileFilter() {
    if (alItems.get(0) instanceof Directory) {
      filter = new HashSet<File>(alItems.size() * 10);
      for (Item item : alItems) {
        Directory dir = (Directory) item;
        filter.addAll(dir.getFilesRecursively());
      }
    } else if (alItems.get(0) instanceof File) {
      filter = new HashSet<File>(alItems.size());
      for (Item item : alItems) {
        filter.add((File) item);
      }
    } else {
      filter = null;
    }
  }

  /**
   * Display. DOCUMENT_ME
   */
  private void display() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // If none editable item, save button is disabled
        if (iEditable == 0) {
          okc.getOKButton().setEnabled(false);
        }
        getRootPane().setDefaultButton(okc.getOKButton());
        getContentPane().add(new JScrollPane(jpMain));
        pack();
        setLocationRelativeTo(JajukMainWindow.getInstance());
        setVisible(true);
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okc.getCancelButton()) {
      dispose();
    } else if (e.getSource().equals(okc.getOKButton())) {
      dispose(); // close window, otherwise you will have some issues if
      // fields are not updated with changes
      Thread t = new Thread("Properties Wizard Action Thread") {
        @Override
        public void run() {
          try {
            panel1.save();
            if (panel2 != null) {
              // refresh the file filter
              refreshFileFilter();
              panel2.save();
            }
          } catch (Exception ex) {
            Messages.showErrorMessage(104, ex.getMessage());
            Log.error(104, ex.getMessage(), ex);
          } finally {
            // -UI refresh-
            if (changes) {
              ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
            }
          }
        }
      };
      // Set min priority to allow EDT to be able to refresh UI between 2 tag
      // changes
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
    }
  }

  /**
   * Tells whether a link button should be shown for a given property.
   * 
   * @param meta DOCUMENT_ME
   * 
   * @return true, if checks if is linkable
   */
  public boolean isLinkable(PropertyMetaInformation meta) {
    String sKey = meta.getName();
    return sKey.equals(Const.XML_DEVICE) || sKey.equals(Const.XML_TRACK)
        || sKey.equals(Const.XML_DEVICE) || sKey.equals(Const.XML_TRACK)
        || sKey.equals(Const.XML_ALBUM) || sKey.equals(Const.XML_ARTIST)
        || sKey.equals(Const.XML_YEAR) || sKey.equals(Const.XML_GENRE)
        || sKey.equals(Const.XML_DIRECTORY) || sKey.equals(Const.XML_FILE)
        || sKey.equals(Const.XML_PLAYLIST) || sKey.equals(Const.XML_PLAYLIST_FILE)
        || sKey.equals(Const.XML_FILES) || sKey.equals(Const.XML_PLAYLIST_FILES)
        // avoid confusing between music types and device types
        || (sKey.equals(Const.XML_TYPE) && !(alItems.get(0) instanceof Device));
  }

  /**
   * A properties panel.
   */
  class PropertiesPanel extends JPanel implements ActionListener {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** Properties panel. */
    JPanel jpProperties;

    /** ItemManager description. */
    JLabel jlDesc;

    /** All dynamic widgets. */
    JComponent[][] widgets;

    /** Properties to display. */
    List<PropertyMetaInformation> alToDisplay;

    /** Items. */
    List<Item> alItems;

    /** Changed properties. */
    Map<PropertyMetaInformation, Object> hmPropertyToChange = new HashMap<PropertyMetaInformation, Object>();

    /** Merge flag. */
    boolean bMerged = false;

    /**
     * Property panel for single types elements.
     * 
     * @param alItems items to display
     * @param sDesc Description (title)
     * @param bMerged : whether this panel contains merged values
     */
    PropertiesPanel(List<Item> alItems, String sDesc, boolean bMerged) {
      super();

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
              // item is not an unmounted playlist
              && !(pa instanceof Playlist && !((Playlist) pa).isReady());
        }
        if (bEditable) {
          iEditable++;
          if (meta.getType().equals(Date.class)) {
            final JXDatePicker jdp = new JXDatePicker();
            jdp.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent arg0) {
                Object oValue = jdp.getDate();
                hmPropertyToChange.put(meta, oValue);
              }
            });
            if (bAllEquals) {
              // If several items, take first value found
              jdp.setDate(new Date(pa.getDateValue(meta.getName()).getTime()));
            } else {
              // Make sure to set default date to 1970, not today to allow user
              // to set date to today for multiple selection and to allow jajuk
              // to detect a change
              jdp.setDate(new Date(0));
            }
            widgets[index][1] = jdp;
          } else if (meta.getType().equals(Boolean.class)) {
            // for a boolean, value is a checkbox
            final JCheckBox jcb = new JCheckBox();
            jcb.addActionListener(new ActionListener() {
              @Override
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
              @Override
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
            widgets[index][1] = jtfValue;
          } else if (meta.getType().equals(String.class)
          // for genres
              && meta.getName().equals(Const.XML_GENRE)) {
            Vector<String> genres = GenreManager.getInstance().getGenresList();
            final JComboBox jcb = new JComboBox(genres);
            jcb.setEditable(true);
            AutoCompleteDecorator.decorate(jcb);
            // set current genre to combo
            int i = -1;
            int comp = 0;
            String sCurrentGenre = pa.getHumanValue(Const.XML_GENRE);
            for (String s : genres) {
              if (s.equals(sCurrentGenre)) {
                i = comp;
                break;
              }
              comp++;
            }
            jcb.setSelectedIndex(i);
            // if different genre, don't show anything
            if (!bAllEquals) {
              jcb.setSelectedIndex(-1);
            }
            jcb.addActionListener(new ActionListener() {
              @Override
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
          } else if (meta.getType().equals(String.class)
              && (Const.XML_ARTIST.equals(meta.getName()) || Const.XML_ALBUM_ARTIST.equals(meta
                  .getName()))) {
            // for artists or album-artists
            Vector<String> artists = null;
            // This string is the artist or the album artist value, used to find combo box index to set
            String valueToCheck = null;
            if (Const.XML_ARTIST.equals(meta.getName())) {
              artists = ArtistManager.getArtistsList();
              valueToCheck = pa.getHumanValue(Const.XML_ARTIST);
            } else if (Const.XML_ALBUM_ARTIST.equals(meta.getName())) {
              artists = AlbumArtistManager.getAlbumArtistsList();
              valueToCheck = pa.getHumanValue(Const.XML_ALBUM_ARTIST);
            }
            final JComboBox jcb = new JComboBox(artists);
            jcb.setEditable(true);
            AutoCompleteDecorator.decorate(jcb);
            // set current genre to combo
            int i = -1;
            int comp = 0;
            for (String s : artists) {
              if (s.equals(valueToCheck)) {
                i = comp;
                break;
              }
              comp++;
            }
            jcb.setSelectedIndex(i);
            // if different artist, don't show anything
            if (!bAllEquals) {
              jcb.setSelectedIndex(-1);
            }
            jcb.addActionListener(new ActionListener() {
              @Override
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
              @Override
              public void keyReleased(KeyEvent arg0) {
                String value = jtfValue.getText();
                hmPropertyToChange.put(meta, value);
              }
            });
            if (bAllEquals) {
              // If several items, take first value found
              jtfValue.setText(pa.getHumanValue(meta.getName()));
            }
            widgets[index][1] = jtfValue;
          }
        } else {
          CopyableLabel jl = null;
          if (meta.getName().equals(Const.XML_ALBUM_DISC_ID)) {
            // Specific rendering : the album disc id should be translated from decimal to hex
            jl = new CopyableLabel((Long.toString(((Long) pa.getValue(meta.getName())), 16)));
          } else {
            // Regular un-editable item rendering
            jl = new CopyableLabel(pa.getHumanValue(meta.getName()));
          }
          // If several items, take first value found
          if (bAllEquals) {
            String s = pa.getHumanValue(meta.getName());
            if (s.indexOf(',') != -1) {
              String[] sTab = s.split(",");
              StringBuilder sb = new StringBuilder();
              sb.append("<html>");
              for (String element : sTab) {
                sb.append("<p>").append(element).append("</p>");
              }
              sb.append("</html>");
              jl.setToolTipText(sb.toString());
            } else {
              jl.setToolTipText(s);
            }
          }
          widgets[index][1] = jl;

        }
        // Link
        if (isLinkable(meta)) {
          JButton jbLink = new JButton(IconLoader.getIcon(JajukIcons.PROPERTIES));
          jbLink.addActionListener(this);
          jbLink.setActionCommand("link");
          jbLink.setToolTipText(Messages.getString("PropertiesWizard.12"));
          widgets[index][2] = jbLink;
        }
        index++;
      }

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

      jpProperties = new JPanel(new MigLayout("insets 10,gapx 5,gapy 10", "[][grow][]"));
      jpProperties.add(jlName);
      jpProperties.add(jlValue, "grow");
      jpProperties.add(jlLink, "wrap");
      // Add widgets
      int i = 0;
      int j = 4;
      // for (PropertyMetaInformation meta : alToDisplay) {
      for (int k = 0; k < alToDisplay.size(); k++) {
        jpProperties.add(widgets[i][0]);
        if (widgets[i][2] == null) { // link widget can be null
          jpProperties.add(widgets[i][1], "grow,width 200:200, wrap");
        } else {
          jpProperties.add(widgets[i][1], "grow,width 200:200");
          jpProperties.add(widgets[i][2], "wrap");
        }
        i++;
        j += 2;
      }
      setLayout(new VerticalLayout(10));
      // desc
      jlDesc = new JLabel(UtilString.formatPropertyDesc(sDesc));
      add(jlDesc);
      add(jpProperties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
      // Link
      if (ae.getActionCommand().equals("link")) {
        PropertyMetaInformation meta = alToDisplay.get(getWidgetIndex((JComponent) ae.getSource()));
        String sProperty = meta.getName();
        if (Const.XML_FILES.equals(sProperty)) {
          Track track = (Track) alItems.get(0);
          List<Item> alFiles = new ArrayList<Item>(1);
          alFiles.addAll(track.getFiles());
          // show properties window for this item
          new PropertiesDialog(alFiles);
        } else if (Const.XML_PLAYLIST_FILES.equals(sProperty)) {
          // can only be a set a files
          String sValue = alItems.get(0).getStringValue(sProperty);
          StringTokenizer st = new StringTokenizer(sValue, ",");
          List<Item> items = new ArrayList<Item>(3);
          while (st.hasMoreTokens()) {
            String sPlf = st.nextToken();
            Item pa = PlaylistManager.getInstance().getPlaylistByID(sPlf);
            if (pa != null) {
              items.add(pa);
            }
          }
          new PropertiesDialog(items);
          // show properties window for this item
        } else {
          String sValue = alItems.get(0).getStringValue(sProperty);
          // can be only an ID
          Item pa = ItemManager.getItemManager(sProperty).getItemByID(sValue);
          if (pa != null) {
            List<Item> items = new ArrayList<Item>(1);
            items.add(pa);
            // show properties window for this item
            new PropertiesDialog(items);
          }
        }
      }
    }

    /**
     * Save changes in tags.
     */
    protected void save() {
      try {
        UtilGUI.waiting();
        // We remove autocommit state to group commit to tags for several tags
        // of a single file
        TrackManager.getInstance().setAutocommit(false);
        Object oValue = null;
        Item newItem = null;
        // list of actually changed tracks (used by out message)
        List<PropertyMetaInformation> alChanged = new ArrayList<PropertyMetaInformation>(2);
        // none change, leave
        if (hmPropertyToChange.size() == 0) {
          return;
        }
        // Computes all items to change
        // contains items to be changed
        List<Item> itemsToChange = new ArrayList<Item>(alItems);
        // Items in error
        List<Item> alInError = new ArrayList<Item>(itemsToChange.size());
        // details for errors
        String sDetails = "";

        // Check typed value format, display error message only once per
        // property
        for (PropertyMetaInformation meta : hmPropertyToChange.keySet()) {
          // New value
          oValue = hmPropertyToChange.get(meta);
          // Check it is not null for non custom properties. Note that
          // we also allow void values for comments
          if (oValue == null || (oValue.toString().trim().length() == 0)
              && !(meta.isCustom() || meta.getName().equals(Const.XML_TRACK_COMMENT))) {
            Log.error(137, '{' + meta.getName() + '}', null);
            Messages.showErrorMessage(137, '{' + meta.getName() + '}');
            return;
          }
        }

        // Now we have all items to consider, write tags for each
        // property to change
        for (int i = 0; i < itemsToChange.size(); i++) {
          // Note that item object can be changed during the next for loop, so
          // do not declare it there
          Item item = null;
          for (PropertyMetaInformation meta : hmPropertyToChange.keySet()) {
            item = itemsToChange.get(i);

            // New value
            oValue = hmPropertyToChange.get(meta);
            // Old value
            String sOldValue = item.getHumanValue(meta.getName());
            if (!UtilString.format(oValue, meta, true).equals(sOldValue)) {
              try {
                newItem = ItemManager.changeItem(item, meta.getName(), oValue, filter);
                changes = true;
              }
              // none accessible file for this track, for this error,
              // we display an error and leave completely
              catch (NoneAccessibleFileException none) {
                Log.error(none);
                Messages.showErrorMessage(none.getCode(), item.getHumanValue(Const.XML_NAME));
                // close window to avoid reseting all properties to
                // old values
                dispose();
                return;
              }
              // cannot rename file, for this error, we display an
              // error and leave completely
              catch (CannotRenameException cre) {
                Log.error(cre);
                Messages.showErrorMessage(cre.getCode());
                dispose();
                return;
              }
              // probably error writing a tag, store track reference
              // and continue
              catch (JajukException je) {
                Log.error(je);
                if (!alInError.contains(item)) {
                  alInError.add(item);
                  // create details label with 3 levels deep
                  sDetails += buidlDetailsString(je);
                }
                continue;
              }
              // if this item was element of property panel elements,
              // update it
              if (alItems.contains(item)) {
                alItems.remove(item);
                alItems.add(newItem);
              }
              // Update itemsToChange to replace the item. Indeed, if we change
              // several properties to the same item, the item itself must
              // change
              itemsToChange.set(i, newItem);
              // if individual item, change title in case of
              // constructor element change
              if (!bMerged) {
                jlDesc.setText(UtilString.formatPropertyDesc(newItem.getDesc()));
              }
              // note this property have been changed
              if (!alChanged.contains(meta)) {
                alChanged.add(meta);
              }
            }
          }
          // Require full commit for all changed tags on the current file
          try {
            TrackManager.getInstance().commit();
          } catch (Exception e) {
            Log.error(e);
            if (!alInError.contains(item)) {
              alInError.add(item);
              // create details label with 3 levels deep
              sDetails += buidlDetailsString(e);
            }
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
            sInfo += "\n" + item.getHumanValue(Const.XML_NAME);
            index++;
          }
          Messages.showDetailedErrorMessage(104, sInfo, sDetails);
        }

        // display a message if user changed at least one property
        if (alChanged.size() > 0) {
          StringBuilder sbChanged = new StringBuilder();
          sbChanged.append("{ ");
          for (PropertyMetaInformation meta : alChanged) {
            sbChanged.append(meta.getHumanName()).append(' ');
          }
          sbChanged.append('}');
          InformationJPanel.getInstance().setMessage(
              alChanged.size() + " " + Messages.getString("PropertiesWizard.10") + ": "
                  + sbChanged.toString(), InformationJPanel.MessageType.INFORMATIVE);
        }
      } finally {
        UtilGUI.stopWaiting();
        // Reset auto-commit state
        TrackManager.getInstance().setAutocommit(true);
        // Force files resorting to ensure the sorting consistency, indeed,
        // files are sorted by name *and* track order and we need to force a
        // files resort after an order change (this is already done in case of
        // file name change)
        FileManager.getInstance().forceSorting();
      }
    }

    /**
     * Build the errors details message.
     * 
     * @param e the exception
     * 
     * @return the errors details message
     */
    private String buidlDetailsString(Exception e) {
      String sDetails = e.getMessage();
      if (e.getCause() != null) {
        sDetails += "\nCaused by:" + e.getCause();
        if (e.getCause().getCause() != null) {
          sDetails += "\nCaused by:" + e.getCause().getCause();
          if (e.getCause().getCause().getCause() != null) {
            sDetails += "\nCaused by:" + e.getCause().getCause().getCause();
          }
        }
      }
      sDetails += "\n\n";
      return sDetails;
    }

    /**
     * Gets the widget index.
     * 
     * @param widget DOCUMENT_ME
     * 
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

}
