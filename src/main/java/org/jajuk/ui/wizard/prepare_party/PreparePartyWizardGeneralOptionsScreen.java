/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 */
package org.jajuk.ui.wizard.prepare_party;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.ui.helpers.DefaultMouseWheelListener;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.wizard.prepare_party.PreparePartyWizard.Variable;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilPrepareParty;
import org.jajuk.util.log.Log;
import org.qdwizard.ClearPoint;
import org.qdwizard.Screen;

import net.miginfocom.swing.MigLayout;

/**
 * General options panel.
 */
@ClearPoint
public class PreparePartyWizardGeneralOptionsScreen extends Screen implements ActionListener,
    ChangeListener, MouseListener {
  /** Constant for MigLayout. */
  private static final String GROW = "grow";
  /** Constant for MigLayout. */
  private static final String GROW_TWO_COL = "[grow][]";
  /** Constant for MigLayout. */
  private static final String LABEL_WIDTH = "width 40:40:";
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** Empty value. */
  private static final String NO_VALUE = " ";
  /** Enable limit on number of tracks. */
  private JCheckBox jcbMaxTracks;
  /** The max. number of tracks */
  private JSlider jsMaxTracks;
  /** The max. number of tracks */
  private JLabel jnMaxTracks;
  /** Enable limit on max size. */
  private JCheckBox jcbMaxSize;
  /** Max size (in MB) of party. */
  private JSlider jsMaxSize;
  /** Max size (in MB) of party. */
  private JLabel jnMaxSize;
  /** Enable limit on max playing length. */
  private JCheckBox jcbMaxLength;
  /** Max playing length of party (in minutes). */
  private JSlider jsMaxLength;
  /** Max playing length of party (in minutes). */
  private JLabel jnMaxLength;
  /** Enable limit on specific audio type. */
  private JCheckBox jcbOneMedia;
  /** Limit to one type of audo file. */
  private JComboBox<String> jcbMedia;
  /** Enable conversion to the selected audio type. */
  private JCheckBox jcbConvertMedia;
  /** Audio conversion. */
  private JLabel jlConvertMedia;
  /** Button to configure audio conversion. */
  private JButton jbConvertConfig;
  /** The min. number of stars a track needs to have */
  private JSlider jsRatingLevel;
  /** Enable normalizing filenames so they can be stored on windows fileshares. */
  private JCheckBox jcbNormalizeFilename;

  /* (non-Javadoc)
   * @see org.qdwizard.Screen#getDescription()
   */
  @Override
  public String getDescription() {
    return Messages.getString("PreparePartyWizard.5");
  }

  /* (non-Javadoc)
   * @see org.qdwizard.Screen#getName()
   */
  @Override
  public String getName() {
    return Messages.getString("PreparePartyWizard.4");
  }

  /**
   * Create panel UI.
   */
  @Override
  public void initUI() {
    { // Max Tracks
      jcbMaxTracks = new JCheckBox(Messages.getString("PreparePartyWizard.10"));
      jcbMaxTracks.setToolTipText(Messages.getString("PreparePartyWizard.11"));
      jsMaxTracks = new JSlider(0, 1000, 100);
      jnMaxTracks = new JLabel(NO_VALUE);
      jnMaxTracks.setBorder(new BevelBorder(BevelBorder.LOWERED));
      jnMaxTracks.setHorizontalAlignment(SwingConstants.RIGHT);
      jsMaxTracks.setMajorTickSpacing(100);
      jsMaxTracks.setMinorTickSpacing(10);
      jsMaxTracks.setPaintTicks(true);
      jsMaxTracks.setPaintLabels(true);
      jsMaxTracks.setToolTipText(Messages.getString("PreparePartyWizard.11"));
    }
    { // Max Size
      jcbMaxSize = new JCheckBox(Messages.getString("PreparePartyWizard.12"));
      jcbMaxSize.setToolTipText(Messages.getString("PreparePartyWizard.13"));
      jsMaxSize = new JSlider(0, 10000, 100);
      jnMaxSize = new JLabel(NO_VALUE);
      jnMaxSize.setBorder(new BevelBorder(BevelBorder.LOWERED));
      jnMaxSize.setHorizontalAlignment(SwingConstants.RIGHT);
      jsMaxSize.setMajorTickSpacing(1000);
      jsMaxSize.setMinorTickSpacing(100);
      jsMaxSize.setPaintTicks(true);
      jsMaxSize.setPaintLabels(true);
      jsMaxSize.setToolTipText(Messages.getString("PreparePartyWizard.13"));
    }
    { // Max Length
      jcbMaxLength = new JCheckBox(Messages.getString("PreparePartyWizard.14"));
      jcbMaxLength.setToolTipText(Messages.getString("PreparePartyWizard.15"));
      jsMaxLength = new JSlider(0, 1000, 100);
      jnMaxLength = new JLabel(NO_VALUE);
      jnMaxLength.setBorder(new BevelBorder(BevelBorder.LOWERED));
      jnMaxLength.setHorizontalAlignment(SwingConstants.RIGHT);
      jsMaxLength.setMajorTickSpacing(100);
      jsMaxLength.setMinorTickSpacing(10);
      jsMaxLength.setPaintTicks(true);
      jsMaxLength.setPaintLabels(true);
      jsMaxLength.setToolTipText(Messages.getString("PreparePartyWizard.15"));
    }
    { // Choose Media
      jcbOneMedia = new JCheckBox(Messages.getString("PreparePartyWizard.16"));
      jcbOneMedia.setToolTipText(Messages.getString("PreparePartyWizard.17"));
      jcbMedia = new JComboBox<>();
      List<Type> types = TypeManager.getInstance().getTypes();

      // sort the list on extension here
      types.sort((o1, o2) -> {
        // handle null, always equal
        if (o1 == null || o2 == null) {
          return 0;
        }
        // otherwise sort on extension here
        return o1.getExtension().compareTo(o2.getExtension());
      });

      for (Type type : types) {
        // exclude playlists and web-radios from selection as we cannot copy
        // those.
        if (!type.getExtension().equals(Const.EXT_PLAYLIST)
            && !type.getExtension().equals(Const.EXT_RADIO)) {
          jcbMedia.addItem(type.getExtension());
        }
      }
      jcbMedia.setToolTipText(Messages.getString("PreparePartyWizard.17"));
      jcbConvertMedia = new JCheckBox(Messages.getString("PreparePartyWizard.34"));
      jcbConvertMedia.setToolTipText(Messages.getString("PreparePartyWizard.35"));
      // to show help and allow clicking for viewing the related web-page
      jlConvertMedia = new JLabel(Messages.getString("PreparePartyWizard.37"));
      jbConvertConfig = new JButton(Messages.getString("PreparePartyWizard.40"));
    }
    // Limit on rate of tracks.
    JLabel jlRatingLevel;
    { // Rating Level
      jlRatingLevel = new JLabel(Messages.getString("DigitalDJWizard.8"));
      jlRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
      jsRatingLevel = new JSlider(0, 4, 0);
      jsRatingLevel.setMajorTickSpacing(1);
      jsRatingLevel.setMinorTickSpacing(1);
      jsRatingLevel.setPaintTicks(true);
      jsRatingLevel.setSnapToTicks(true);
      jsRatingLevel.setPaintLabels(true);
      jsRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
    }
    jcbNormalizeFilename = new JCheckBox(Messages.getString("PreparePartyWizard.26"));
    jcbNormalizeFilename.setToolTipText(Messages.getString("PreparePartyWizard.27"));
    // populate the UI items with values from the data object
    readData();
    // add listeners after reading initial data to not overwrite them with
    // init-state-change actions
    // enable/disable slider depending on checkbox
    jcbMaxTracks.addActionListener(this);
    jsMaxTracks.addMouseWheelListener(new DefaultMouseWheelListener(jsMaxTracks));
    jsMaxTracks.addChangeListener(this);
    // enable/disable slider depending on checkbox
    jcbMaxSize.addActionListener(this);
    jsMaxSize.addMouseWheelListener(new DefaultMouseWheelListener(jsMaxSize));
    jsMaxSize.addChangeListener(this);
    // enable/disable slider depending on checkbox
    jcbMaxLength.addActionListener(this);
    jsMaxLength.addMouseWheelListener(new DefaultMouseWheelListener(jsMaxLength));
    jsMaxLength.addChangeListener(this);
    // enable/disable combobox depending on checkbox
    jcbOneMedia.addActionListener(this);
    jcbMedia.addActionListener(this);
    jcbConvertMedia.addActionListener(this);
    jlConvertMedia.addMouseListener(this);
    jbConvertConfig.addActionListener(this);
    // get informed about rating level slider changes
    jsRatingLevel.addMouseWheelListener(new DefaultMouseWheelListener(jsRatingLevel));
    jsRatingLevel.addChangeListener(this);
    jcbNormalizeFilename.addActionListener(this);
    setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[][grow]"));
    add(jcbMaxTracks);
    {
      JPanel panel = new JPanel();
      panel.setLayout(new MigLayout("", GROW_TWO_COL));
      panel.add(jsMaxTracks, GROW);
      panel.add(jnMaxTracks, LABEL_WIDTH);
      add(panel, "grow,wrap");
    }
    add(jcbMaxSize);
    {
      JPanel panel = new JPanel();
      panel.setLayout(new MigLayout("", GROW_TWO_COL));
      panel.add(jsMaxSize, GROW);
      panel.add(jnMaxSize, LABEL_WIDTH);
      add(panel, "grow,wrap");
    }
    add(jcbMaxLength);
    {
      JPanel panel = new JPanel();
      panel.setLayout(new MigLayout("", GROW_TWO_COL));
      panel.add(jsMaxLength, GROW);
      panel.add(jnMaxLength, LABEL_WIDTH);
      add(panel, "grow,wrap");
    }
    add(jcbOneMedia);
    add(jcbMedia, "grow,wrap");
    // dummy-Label to get the CheckBox for "convert" into the second column
    add(new JLabel());
    add(jcbConvertMedia, "grow,wrap");
    add(new JLabel());
    {
      JPanel panel = new JPanel();
      panel.setLayout(new MigLayout("", GROW_TWO_COL));
      panel.add(jlConvertMedia, GROW);
      panel.add(jbConvertConfig);
      add(panel, "grow,wrap");
    }
    add(jcbNormalizeFilename, "grow,wrap");
    add(jlRatingLevel);
    add(jsRatingLevel, "grow,wrap");
    // store initial values and adjust values
    updateData();
  }

  /**
   * Populate the UI items with values from the data object.
   */
  private void readData() {
    // set the values from the stored data
    // initially these are not set, so we need to query for "containsKey"...
    if (isTrue(Variable.MAXTRACKS_ENABLED)) {
      jsMaxTracks.setEnabled(true);
      jcbMaxTracks.setSelected(true);
    } else {
      jsMaxTracks.setEnabled(false);
      jcbMaxTracks.setSelected(false);
    }
    if (data.containsKey(Variable.MAXTRACKS)) {
      jsMaxTracks.setValue((Integer) data.get(Variable.MAXTRACKS));
    }
    if (isTrue(Variable.MAXSIZE_ENABLED)) {
      jsMaxSize.setEnabled(true);
      jcbMaxSize.setSelected(true);
    } else {
      jsMaxSize.setEnabled(false);
      jcbMaxSize.setSelected(false);
    }
    if (data.containsKey(Variable.MAXSIZE)) {
      jsMaxSize.setValue((Integer) data.get(Variable.MAXSIZE));
    }
    if (isTrue(Variable.MAXLENGTH_ENABLED)) {
      jsMaxLength.setEnabled(true);
      jcbMaxLength.setSelected(true);
    } else {
      jsMaxLength.setEnabled(false);
      jcbMaxLength.setSelected(false);
    }
    if (data.containsKey(Variable.MAXLENGTH)) {
      jsMaxLength.setValue((Integer) data.get(Variable.MAXLENGTH));
    }
    if (isTrue(Variable.ONE_MEDIA_ENABLED)) {
      jcbMedia.setEnabled(true);
      jcbOneMedia.setSelected(true);
      jcbConvertMedia.setEnabled(true);
    } else {
      jcbMedia.setEnabled(false);
      jcbOneMedia.setSelected(false);
      jcbConvertMedia.setEnabled(false);
    }
    // Check if pacpl can be used, do it every time the dialog starts as the
    // user might have installed it by now
    boolean bPACPLAvailable = UtilPrepareParty.checkPACPL((String) data
        .get(Variable.CONVERT_COMMAND));
    if (!bPACPLAvailable) {
      // disable media conversion if pacpl is not found
      jcbConvertMedia.setEnabled(false);
    }
    // don't set Convert to on from data if PACPL became unavailable
    jcbConvertMedia.setSelected(isTrue(Variable.CONVERT_MEDIA) && bPACPLAvailable);
    // default to MP3 initially
    jcbMedia.setSelectedItem(data.getOrDefault(Variable.ONE_MEDIA, "mp3"));
    if (data.containsKey(Variable.RATING_LEVEL)) {
      jsRatingLevel.setValue((Integer) data.get(Variable.RATING_LEVEL));
    }
    jcbNormalizeFilename.setSelected(isTrue(Variable.NORMALIZE_FILENAME));
  }

  /**
   * Return if the specified element is true in the data-map.
   *
   * @param key The key to look up in the data-object.
   *
   * @return true if the value was stored as boolean true, false otherwise.
   */
  private boolean isTrue(final Variable key) {
    return data.containsKey(key) && Boolean.TRUE.equals(data.get(key));
  }

  /**
   * Write the data from the UI items to the data object.
   */
  private void updateData() {
    // store if checkbox is enabled and update the label accordingly
    updateOneItem(jcbMaxTracks, jsMaxTracks, jnMaxTracks, Variable.MAXTRACKS,
        Variable.MAXTRACKS_ENABLED);
    updateOneItem(jcbMaxSize, jsMaxSize, jnMaxSize, Variable.MAXSIZE, Variable.MAXSIZE_ENABLED);
    updateOneItem(jcbMaxLength, jsMaxLength, jnMaxLength, Variable.MAXLENGTH,
        Variable.MAXLENGTH_ENABLED);
    if (jcbOneMedia.isSelected()) {
      data.put(Variable.ONE_MEDIA, jcbMedia.getSelectedItem());
      data.put(Variable.ONE_MEDIA_ENABLED, Boolean.TRUE);
    } else {
      // keep old value... data.remove(Variable.KEY_MEDIA);
      data.put(Variable.ONE_MEDIA_ENABLED, Boolean.FALSE);
    }
    data.put(Variable.CONVERT_MEDIA, jcbConvertMedia.isSelected());
    data.put(Variable.RATING_LEVEL, jsRatingLevel.getValue());
    data.put(Variable.NORMALIZE_FILENAME, jcbNormalizeFilename.isSelected());
  }

  /**
   * Helper to handle a checkbox/slider combination. It also updates an
   * associated Label with the value from the Slider.
   *
   * @param cb The checkbox to check for selected/deselected state
   * @param slider The slider to get the value from
   * @param label The Label to populate with the current value from the Slider.
   * @param key The key in the data object for the value of the Slider.
   * @param keyOn The key in the data object to store the enabled/disabled
   * state.
   */
  private void updateOneItem(JCheckBox cb, JSlider slider, JLabel label, Variable key,
      Variable keyOn) {
    if (cb.isSelected()) {
      if (!slider.getValueIsAdjusting()) {
        data.put(key, slider.getValue());
        data.put(keyOn, Boolean.TRUE);
      }
      label.setText(Integer.toString(slider.getValue()));
    } else {
      if (!slider.getValueIsAdjusting()) {
        // keep value... data.remove(key);
        data.put(keyOn, Boolean.FALSE);
      }
      label.setText(NO_VALUE);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent ae) {
    // if a checkbox is selected/deselected, enable/disable the
    // sliders/comboboxes accordingly
    if (ae.getSource() == jcbMaxTracks) {
      jsMaxTracks.setEnabled(jcbMaxTracks.isSelected());
    } else if (ae.getSource() == jcbMaxSize) {
      jsMaxSize.setEnabled(jcbMaxSize.isSelected());
    } else if (ae.getSource() == jcbMaxLength) {
      jsMaxLength.setEnabled(jcbMaxLength.isSelected());
    } else if (ae.getSource() == jcbOneMedia) {
      jcbMedia.setEnabled(jcbOneMedia.isSelected());
      jcbConvertMedia.setEnabled(jcbOneMedia.isSelected());
    } else if (ae.getSource() == jbConvertConfig) {
      // create the settings dialog, it will display itself and inform us when
      // the value is changed with "Ok"
      new PreparePartyConvertSettings(e -> {
        // no need for re-checking if the same command is chosen as before
        if (e.getSource().toString().equals(data.get(Variable.CONVERT_COMMAND))) {
          Log.debug("Same pacpl-command as before: " + e.getSource().toString());
          return;
        }
        Log.debug("New pacpl-command: " + e.getSource().toString());
        data.put(Variable.CONVERT_COMMAND, e.getSource().toString());
        // re-check if pacpl can be called now
        boolean bPACPLAvailable = UtilPrepareParty.checkPACPL((String) data
            .get(Variable.CONVERT_COMMAND));
        // disable media conversion if pacpl is not found
        if (bPACPLAvailable) {
          Log.debug("Updated settings for media conversion allow pacpl to be used.");
          jcbConvertMedia.setEnabled(true);
        } else {
          Log.warn("Updated settings for media conversion do not allow pacpl to be used!");
          jcbConvertMedia.setEnabled(false);
          jcbConvertMedia.setSelected(false);
        }
      }, (String) data.get(Variable.CONVERT_COMMAND), JajukMainWindow.getInstance());
    }
    updateData();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
   * )
   */
  @Override
  public void stateChanged(ChangeEvent ie) {
    // just update the stored data whenever we receive an interesting
    // event
    if (ie.getSource() == jsMaxTracks) {
      updateData();
    } else if (ie.getSource() == jsMaxSize) {
      updateData();
    } else if (ie.getSource() == jsMaxLength) {
      updateData();
    } else if (ie.getSource() == jcbMedia) {
      updateData();
    } else if (ie.getSource() == jsRatingLevel) {
      updateData();
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    if (e.getSource() == jlConvertMedia) {
      try {
        Desktop.getDesktop().browse(
            new URI("https://www.jajuk.info/manual/pacpl.html"));
      } catch (IOException | URISyntaxException ex) {
        Log.error(ex);
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    // nothing to do here...
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e) {
    // nothing to do here...
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    // nothing to do here...
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    // nothing to do here...
  }
}
