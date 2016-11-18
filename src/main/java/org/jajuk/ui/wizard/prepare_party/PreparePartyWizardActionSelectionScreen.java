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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.services.bookmark.Bookmarks;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.wizard.prepare_party.PreparePartyWizard.Mode;
import org.jajuk.ui.wizard.prepare_party.PreparePartyWizard.Variable;
import org.jajuk.util.Messages;
import org.qdwizard.ClearPoint;
import org.qdwizard.Screen;

import net.miginfocom.swing.MigLayout;

/**
 * First Panel of the Wizard, it shows a selection of sources where the user can choose one, e.g. DJs, Ambiences, ...
 */
@ClearPoint
public class PreparePartyWizardActionSelectionScreen extends Screen implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -6981770030816500259L;
  /** The group for the various sources. */
  private ButtonGroup bgActions;
  /** DJ. */
  private JRadioButton jrbDJ;
  /** DJ. */
  @SuppressWarnings("rawtypes")
  private JComboBox jcbDJ;
  /** Ambience. */
  private JRadioButton jrbAmbience;
  /** Ambience. */
  @SuppressWarnings("rawtypes")
  private JComboBox jcbAmbience;
  /** Playlist. */
  private JRadioButton jrbPlaylist;
  /** Playlist. */
  @SuppressWarnings("rawtypes")
  private JComboBox jcbPlaylist;
  /** Shuffle. */
  private JRadioButton jrbShuffle;
  /** Shuffle. */
  private JRadioButton jrbBestOf;
  /** Novelties. */
  private JRadioButton jrbNovelties;
  /** Queue. */
  private JRadioButton jrbQueue;
  /** Bookmarks. */
  private JRadioButton jrbBookmark;

  /**
   * Create panel UI.
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void initUI() {
    bgActions = new ButtonGroup();
    jrbDJ = new JRadioButton(Messages.getString("PreparePartyWizard.6"));
    jrbDJ.addActionListener(this);
    // populate DJs
    List<DigitalDJ> djs = DigitalDJManager.getInstance().getDJsSorted();
    jcbDJ = new JComboBox();
    for (DigitalDJ dj : djs) {
      jcbDJ.addItem(dj.getName());
    }
    jcbDJ.addActionListener(this);
    jrbAmbience = new JRadioButton(Messages.getString("PreparePartyWizard.7"));
    jrbAmbience.addActionListener(this);
    List<Ambience> ambiences = AmbienceManager.getInstance().getAmbiences();
    jcbAmbience = new JComboBox();
    for (Ambience amb : ambiences) {
      jcbAmbience.addItem(amb.getName());
    }
    jcbAmbience.addActionListener(this);
    jrbPlaylist = new JRadioButton(Messages.getString("PreparePartyWizard.8"));
    jrbPlaylist.addActionListener(this);
    jcbPlaylist = new JComboBox();
    Playlist tempPlaylist = (Playlist) data.get(Variable.TEMP_PLAYLIST);
    if (tempPlaylist != null) {
      // check if this is a "temporary" playlist that is provided by the
      // PlaylistView (i.e. not yet stored in PlaylistManager)
      jcbPlaylist.addItem(tempPlaylist.getName());
    }
    List<Playlist> playlists = PlaylistManager.getInstance().getPlaylists();
    for (Playlist pl : playlists) {
      jcbPlaylist.addItem(pl.getName());
    }
    jcbPlaylist.addActionListener(this);
    jrbShuffle = new JRadioButton(Messages.getString("PreparePartyWizard.9"));
    jrbShuffle.addActionListener(this);
    jrbBestOf = new JRadioButton(Messages.getString("PreparePartyWizard.24"));
    jrbBestOf.addActionListener(this);
    jrbNovelties = new JRadioButton(Messages.getString("PreparePartyWizard.25"));
    jrbNovelties.addActionListener(this);
    jrbQueue = new JRadioButton(Messages.getString("PreparePartyWizard.32"));
    jrbQueue.addActionListener(this);
    jrbBookmark = new JRadioButton(Messages.getString("PreparePartyWizard.33"));
    jrbBookmark.addActionListener(this);
    bgActions.add(jrbDJ);
    bgActions.add(jrbAmbience);
    bgActions.add(jrbPlaylist);
    bgActions.add(jrbBestOf);
    bgActions.add(jrbNovelties);
    bgActions.add(jrbQueue);
    bgActions.add(jrbBookmark);
    bgActions.add(jrbShuffle);
    // populate items from the stored static data
    readData();
    // populate the screen
    setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[][grow]"));
    add(jrbDJ, "left");
    add(jcbDJ, "grow,wrap");
    add(jrbAmbience, "left");
    add(jcbAmbience, "grow,wrap");
    add(jrbPlaylist, "left");
    add(jcbPlaylist, "grow,wrap");
    add(jrbBestOf, "left,wrap");
    add(jrbNovelties, "left,wrap");
    add(jrbQueue, "left,wrap");
    add(jrbBookmark, "left,wrap");
    add(jrbShuffle, "left,wrap");
    // store initial values, done here as well to have them stored if "next"
    // is pressed immediately
    // and there was no data stored before (an hence nothing was read in
    // readData())
    updateData();
  }

  /**
   * Initialize the UI items of the panel with values from the data
   * object.
   */
  private void readData() {
    if (data.containsKey(Variable.MODE)) {
      // read values set before
      switch ((Mode) data.get(Variable.MODE)) {
      case DJ:
        bgActions.setSelected(jrbDJ.getModel(), true);
        jcbDJ.setSelectedItem(data.get(Variable.ITEM));
        break;
      case Ambience:
        bgActions.setSelected(jrbAmbience.getModel(), true);
        jcbAmbience.setSelectedItem(data.get(Variable.ITEM));
        break;
      case Playlist:
      case ProvidedPlaylist: // we did a "PrepareParty" from a Playlist
        // before, in this case show the Playlist again
        // here
        bgActions.setSelected(jrbPlaylist.getModel(), true);
        jcbPlaylist.setSelectedItem(data.get(Variable.ITEM));
        break;
      case Shuffle:
        bgActions.setSelected(jrbShuffle.getModel(), true);
        // no combo box for shuffle...
        break;
      case BestOf:
        bgActions.setSelected(jrbBestOf.getModel(), true);
        // no combo box for bestof...
        break;
      case Novelties:
        bgActions.setSelected(jrbNovelties.getModel(), true);
        // no combo box for novelties...
        break;
      case Queue:
        bgActions.setSelected(jrbQueue.getModel(), true);
        // no combo box for queue...
        break;
      case Bookmarks:
        bgActions.setSelected(jrbBookmark.getModel(), true);
        // no combo box for bookmarks...
        break;
      default:
        throw new IllegalArgumentException("Unexpected value in switch!");
      }
    } else {
      // no values set yet, select a useful radio button at least
      // select Ambience as default selection if there is no DJ available
      if (jcbDJ.getItemCount() == 0) {
        bgActions.setSelected(jrbAmbience.getModel(), true);
      } else {
        // otherwise select DJ as default option
        bgActions.setSelected(jrbDJ.getModel(), true);
      }
    }
    // finally disable some items if there is nothing in there
    if (jcbDJ.getItemCount() == 0) {
      jrbDJ.setEnabled(false);
      jcbDJ.setEnabled(false);
    }
    // disable Playlist UI if there is no Playlist-Mode already selected by
    // the incoming data...
    if (jcbPlaylist.getItemCount() == 0
        && !(Mode.Playlist.equals(data.get(Variable.MODE)) || Mode.ProvidedPlaylist.equals(data
            .get(Variable.MODE)))) {
      jrbPlaylist.setEnabled(false);
      jcbPlaylist.setEnabled(false);
    }
    // check if we have queue-entries or bookmarks
    if (QueueModel.getQueue().isEmpty()) {
      jrbQueue.setEnabled(false);
    }
    if (Bookmarks.getInstance().getFiles().isEmpty()) {
      jrbBookmark.setEnabled(false);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // Update all the values that are needed later
    updateData();
  }

  /**
   * Store the current values from the UI items into the data object.
   */
  private void updateData() {
    // depending on the selected radio button read the combo box value and set
    // the selected MODE
    if (jrbDJ.isSelected()) {
      data.put(Variable.MODE, Mode.DJ);
      data.put(Variable.ITEM, jcbDJ.getSelectedItem());
    } else if (jrbAmbience.isSelected()) {
      data.put(Variable.MODE, Mode.Ambience);
      data.put(Variable.ITEM, jcbAmbience.getSelectedItem());
    } else if (jrbPlaylist.isSelected()) {
      data.put(Variable.MODE, Mode.Playlist);
      data.put(Variable.ITEM, jcbPlaylist.getSelectedItem());
    } else if (jrbShuffle.isSelected()) {
      data.put(Variable.MODE, Mode.Shuffle);
      data.remove(Variable.ITEM);
    } else if (jrbBestOf.isSelected()) {
      data.put(Variable.MODE, Mode.BestOf);
      data.remove(Variable.ITEM);
    } else if (jrbNovelties.isSelected()) {
      data.put(Variable.MODE, Mode.Novelties);
      data.remove(Variable.ITEM);
    } else if (jrbQueue.isSelected()) {
      data.put(Variable.MODE, Mode.Queue);
      data.remove(Variable.ITEM);
    } else if (jrbBookmark.isSelected()) {
      data.put(Variable.MODE, Mode.Bookmarks);
      data.remove(Variable.ITEM);
    }
  }

  @Override
  public String getDescription() {
    return Messages.getString("PreparePartyWizard.3");
  }

  @Override
  public String getName() {
    return Messages.getString("PreparePartyWizard.2");
  }
}
