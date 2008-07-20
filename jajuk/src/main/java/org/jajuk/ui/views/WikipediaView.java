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

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.actions.ActionBase;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.widgets.JajukHtmlPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;

/**
 * Wikipedia view
 */
public class WikipediaView extends ViewAdapter implements Const, Observer,
    ActionListener {

  private static final long serialVersionUID = 1L;

  JLabel jlLanguage;

  JComboBox jcbLanguage;

  /** Cobra web browser */
  JajukHtmlPanel browser;

  JButton jbCopy;

  JButton jbLaunchInExternalBrowser;

  JToggleButton jbAuthorSearch;

  JToggleButton jbAlbumSearch;

  JToggleButton jbTrackSearch;

  /** Language index */
  int indexLang = 0;

  enum Type {
    AUTHOR, ALBUM, TRACK
  }

  Type type = Type.AUTHOR;

  /** Current search */
  String search = null;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("WikipediaView.0");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#populate()
   */
  public void initUI() {
    jlLanguage = new JLabel(Messages.getString("WikipediaView.1"));
    jcbLanguage = new JComboBox();
    for (String sDesc : Messages.getDescs()) {
      jcbLanguage.addItem(sDesc);
    }
    // get stored language
    jcbLanguage.setSelectedItem(Messages.getDescForLocal(Conf
        .getString(CONF_WIKIPEDIA_LANGUAGE)));
    jcbLanguage.addActionListener(this);
    // Buttons
    ActionBase aCopy = ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD);
    jbCopy = new JButton(aCopy);
    jbLaunchInExternalBrowser = new JButton(ActionManager.getAction(JajukActions.LAUNCH_IN_BROWSER));
    // Remove text inside the buttons
    jbLaunchInExternalBrowser.setText(null);
    jbCopy.setText(null);
    ButtonGroup bg = new ButtonGroup();
    jbAuthorSearch = new JToggleButton(IconLoader.ICON_AUTHOR, false);
    jbAuthorSearch.setToolTipText(Messages.getString("WikipediaView.5"));
    // Select author search (default)
    jbAuthorSearch.setSelected(true);
    jbAuthorSearch.addActionListener(this);
    jbAlbumSearch = new JToggleButton(IconLoader.ICON_ALBUM, true);
    jbAlbumSearch.setToolTipText(Messages.getString("WikipediaView.6"));
    jbAlbumSearch.addActionListener(this);
    jbTrackSearch = new JToggleButton(IconLoader.ICON_TRACK, false);
    jbTrackSearch.setToolTipText(Messages.getString("WikipediaView.7"));
    jbTrackSearch.addActionListener(this);
    // Group this three mutual exclusive buttons
    bg.add(jbAuthorSearch);
    bg.add(jbAlbumSearch);
    bg.add(jbTrackSearch);

    JToolBar jtb = new JToolBar();
    jtb.setFloatable(false);
    jtb.setRollover(true);
    jtb.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    // Add items
    jtb.add(jbAuthorSearch);
    jtb.add(jbAlbumSearch);
    jtb.add(jbTrackSearch);
    jtb.addSeparator();
    jtb.add(jbCopy);
    jtb.add(jbLaunchInExternalBrowser);
    jtb.addSeparator();
    jtb.add(jcbLanguage);

    JPanel jpCommand = new JPanel();
    jpCommand.setBorder(BorderFactory.createEtchedBorder());
    jpCommand.setLayout(new FlowLayout(FlowLayout.LEFT));
    jpCommand.add(jtb);

    // global layout
    double size[][] = { { 2, TableLayout.FILL, 5 }, { TableLayout.PREFERRED, 5, TableLayout.FILL } };
    setLayout(new TableLayout(size));
    browser = new JajukHtmlPanel();
    add(jpCommand, "1,0");
    add(browser, "1,2");

    // Display default page at startup is none track launch
    // avoid to launch this if a track is playing
    // to avoid thread concurrency
    if (FIFO.getCurrentFile() == null) {
      reset();
    }

    // subscriptions to events
    ObservationManager.register(WikipediaView.this);

    // force event
    update(new Event(JajukEvents.EVENT_FILE_LAUNCHED, ObservationManager
        .getDetailsLastOccurence(JajukEvents.EVENT_FILE_LAUNCHED)));
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.EVENT_ZERO);
    eventSubjectSet.add(JajukEvents.EVENT_AUTHOR_CHANGED);
    eventSubjectSet.add(JajukEvents.EVENT_ALBUM_CHANGED);
    eventSubjectSet.add(JajukEvents.EVENT_TRACK_CHANGED);
    eventSubjectSet.add(JajukEvents.EVENT_PERPECTIVE_CHANGED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(Event event) {
    JajukEvents subject = event.getSubject();
    // Make a search after a stop period
    if (subject.equals(JajukEvents.EVENT_FILE_LAUNCHED)
        || subject.equals(JajukEvents.EVENT_PERPECTIVE_CHANGED)) {
      // If current state is stopped, reset page
      if (FIFO.getCurrentFile() == null) {
        reset();
        return;
      }
      // Launch search
      launchSearch(false);
    }
    // Reset the page when stopping
    else if (subject.equals(JajukEvents.EVENT_ZERO)) {
      reset();
    }
    // User changed current track tags, so we have to reload
    // new author wikipedia page
    else if (subject.equals(JajukEvents.EVENT_AUTHOR_CHANGED)
        || subject.equals(JajukEvents.EVENT_ALBUM_CHANGED)
        || subject.equals(JajukEvents.EVENT_TRACK_CHANGED)) {
      update(new Event(JajukEvents.EVENT_FILE_LAUNCHED));
    }
  }

  /**
   * Perform wikipedia search
   * 
   * @param bForceReload
   *          force the page display
   */
  private void launchSearch(final boolean bForceReload) {
    Thread t = new Thread() {
      @Override
      public void run() {
        try {
          String lSearch = null;
          if (FIFO.getCurrentFile() != null) {
            if (type == Type.AUTHOR) {
              lSearch = FIFO.getCurrentFile().getTrack().getAuthor().getName2();
              // don't display page if item is unknown
              if (Messages.getString(UNKNOWN_AUTHOR).equals(lSearch)) {
                lSearch = null;
              }
            } else if (type == Type.ALBUM) {
              lSearch = FIFO.getCurrentFile().getTrack().getAlbum().getName2();
              // don't display page if item is unknown
              if (Messages.getString(UNKNOWN_ALBUM).equals(lSearch)) {
                lSearch = null;
              }
            } else if (type == Type.TRACK) {
              lSearch = FIFO.getCurrentFile().getTrack().getName();
            }
          }
          // If search is still null, display an nothing found page
          if (lSearch == null) {
            browser.setUnknow();
            return;
          }
          // Avoid reloading an existing page
          if (!bForceReload && lSearch.equals(WikipediaView.this.search)) {
            return;
          }
          // Store the search to avoid future identical searches
          WikipediaView.this.search = lSearch;

          URL url = new URL(("http://"
              + Messages.getLocalForDesc((String) jcbLanguage.getSelectedItem())
              + ".wikipedia.org/wiki/" + lSearch).replaceAll(" ", "_"));
          Log.debug("Wikipedia search: " + url);
          UtilFeatures.setCopyData(url.toString());
          jbLaunchInExternalBrowser.putClientProperty(DETAIL_CONTENT, url.toExternalForm());
          browser.setURL(url);
        } catch (Exception e) {
          Log.error(e);
        }

      }
    };
    t.setPriority(Thread.MIN_PRIORITY);
    t.start();
  }

  /*
   * Reset view
   */
  private void reset() {
    // Reset current search
    this.search = null;
    // Display jajuk page (in a thread to avoid freezing UI)
    new Thread() {
      @Override
      public void run() {
        if (browser != null) {
          try {
            browser.clearDocument();
          } catch (Exception e) {
            Log.error(e);
          }
        }
      }
    }.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void actionPerformed(ActionEvent arg0) {
    if (arg0.getSource() == jcbLanguage) {
      // update index
      Conf.setProperty(CONF_WIKIPEDIA_LANGUAGE, Messages
          .getLocalForDesc((String) jcbLanguage.getSelectedItem()));
      // force launch wikipedia search for this language
      launchSearch(true);
    } else if (arg0.getSource() == jbAlbumSearch) {
      type = Type.ALBUM;
      // force event
      launchSearch(true);
    } else if (arg0.getSource() == jbAuthorSearch) {
      type = Type.AUTHOR;
      // force event
      launchSearch(true);
    } else if (arg0.getSource() == jbTrackSearch) {
      type = Type.TRACK;
      // force event
      launchSearch(true);
    }
  }
}
