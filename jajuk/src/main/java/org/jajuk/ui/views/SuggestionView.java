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
 *  $$Revision: 2563 $$
 */

package org.jajuk.ui.views;

import com.sun.java.help.impl.SwingWorker;

import ext.FlowScrollPanel;
import ext.services.lastfm.AudioScrobblerAlbum;
import ext.services.lastfm.AudioScrobblerArtist;
import ext.services.lastfm.AudioScrobblerService;
import ext.services.lastfm.AudioScrobblerSimilarArtists;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.File;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.thumbnails.AbstractThumbnail;
import org.jajuk.ui.thumbnails.AudioScrobblerAlbumThumbnail;
import org.jajuk.ui.thumbnails.AudioScrobblerAuthorThumbnail;
import org.jajuk.ui.thumbnails.LocalAlbumThumbnail;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Show suggested albums based on current collection (bestof, novelties) and
 * LAstFM
 */
public class SuggestionView extends ViewAdapter implements ITechnicalStrings, Observer {

  private static final long serialVersionUID = 1L;

  private JTabbedPane tabs;

  private String author;

  private enum SuggestionType {
    BEST_OF, NEWEST, RARE, OTHERS_ALBUMS, SIMILAR_AUTHORS
  }

  JScrollPane jpBestof;

  JScrollPane jpNewest;

  JScrollPane jpRare;

  JScrollPane jpOthersAlbums;

  JScrollPane jpSimilarAuthors;

  private int comp = 0;

  List<Album> albumsNewest;
  List<Album> albumsPrefered;
  List<Album> albumsRare;

  /** Currently selected thumb */
  AbstractThumbnail selectedThumb;

  class ThumbMouseListener extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      AbstractThumbnail thumb = (AbstractThumbnail) ((JLabel) e.getSource()).getParent()
          .getParent();
      // remove red border on previous item if
      // different from this one
      if (selectedThumb != null && selectedThumb != thumb) {
        selectedThumb.setSelected(false);
      }
      // select the new selected thumb
      thumb.setSelected(true);
      selectedThumb = thumb;
    }
  }

  public SuggestionView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("SuggestionView.0");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#populate()
   */
  public void initUI() {
    tabs = new JTabbedPane();
    // Remove tab border, see
    // http://forum.java.sun.com/thread.jspa?threadID=260746&messageID=980405
    class MyTabbedPaneUI extends javax.swing.plaf.basic.BasicTabbedPaneUI {
      protected Insets getContentBorderInsets(int tabPlacement) {
        return new Insets(0, 0, 0, 0);
      }

      protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
      }
    }
    // Now use the new TabbedPaneUI
    tabs.setUI(new MyTabbedPaneUI());
    // Refresh tabs on demand only
    tabs.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent arg0) {
        refreshLastFMCollectionTabs();
      }
    });
    // Fill tabs with empty tabs
    tabs.addTab(Messages.getString("SuggestionView.1"), Util.getCentredPanel(new JLabel(Messages
        .getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.2"), Util.getCentredPanel(new JLabel(Messages
        .getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.5"), Util.getCentredPanel(new JLabel(Messages
        .getString("WikipediaView.3"))));
    tabs.addTab(Messages.getString("SuggestionView.3"), Util.getCentredPanel(new JLabel(Messages
        .getString("SuggestionView.7"))));
    tabs.addTab(Messages.getString("SuggestionView.4"), Util.getCentredPanel(new JLabel(Messages
        .getString("SuggestionView.7"))));
    // Add panels
    refreshLocalCollectionTabs(true);
    // Add tabs
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(tabs);
    // Look for events
    ObservationManager.register(this);
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_PARAMETERS_CHANGE);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
    eventSubjectSet.add(EventSubject.EVENT_COVER_REFRESH);
    return eventSubjectSet;
  }

  /**
   * Refresh local thumbs
   * 
   * @param search
   *          force searching new thumbs, if false, just UI refresh
   */
  private void refreshLocalCollectionTabs(final boolean search) {
    // Display a busy panel in the mean-time
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JXBusyLabel busy1 = new JXBusyLabel();
        busy1.setBusy(true);
        JXBusyLabel busy2 = new JXBusyLabel();
        busy2.setBusy(true);
        JXBusyLabel busy3 = new JXBusyLabel();
        busy3.setBusy(true);
        tabs.setComponentAt(0, Util.getCentredPanel(busy1));
        tabs.setComponentAt(1, Util.getCentredPanel(busy2));
        tabs.setComponentAt(3, Util.getCentredPanel(busy3));
      }
    });

    SwingWorker sw = new SwingWorker() {
      JScrollPane jsp1;
      JScrollPane jsp2;
      JScrollPane jsp3;

      @Override
      public Object construct() {
        jsp1 = getLocalSuggestionsPanel(SuggestionType.BEST_OF, search);
        jsp2 = getLocalSuggestionsPanel(SuggestionType.NEWEST, search);
        jsp3 = getLocalSuggestionsPanel(SuggestionType.RARE, search);
        return null;
      }

      @Override
      public void finished() {
        // If panel is void, add a void panel as a null object keeps
        // previous element
        tabs.setComponentAt(0, (jsp1 == null) ? new JPanel() : jsp1);
        tabs.setComponentAt(1, (jsp2 == null) ? new JPanel() : jsp2);
        tabs.setComponentAt(2, (jsp3 == null) ? new JPanel() : jsp3);
        super.finished();
      }
    };
    sw.start();
  }

  private void refreshLastFMCollectionTabs() {
    String newAuthor = null;
    File current = FIFO.getInstance().getCurrentFile();
    if (current != null) {
      newAuthor = current.getTrack().getAuthor().getName2();
    }
    // if none track playing
    if (current == null
    // Last.FM infos is disable
        || !ConfigurationManager.getBoolean(CONF_LASTFM_INFO)
        // If unknown author
        || (newAuthor == null || newAuthor.equals(Messages.getString(UNKNOWN_AUTHOR)))) {
      // Set empty panels
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          tabs.setComponentAt(3, Util.getCentredPanel(new JLabel(Messages
              .getString("SuggestionView.7"))));
          tabs.setComponentAt(4, Util.getCentredPanel(new JLabel(Messages
              .getString("SuggestionView.7"))));
        }
      });
      return;
    }
    // Check if author changed, otherwise, just leave
    if (newAuthor.equals(this.author)) {
      return;
    }
    // Save current author
    author = newAuthor;
    // Display a busy panel in the mean-time
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JXBusyLabel busy1 = new JXBusyLabel();
        busy1.setBusy(true);
        JXBusyLabel busy2 = new JXBusyLabel();
        busy2.setBusy(true);
        tabs.setComponentAt(3, Util.getCentredPanel(busy1));
        tabs.setComponentAt(4, Util.getCentredPanel(busy2));
      }
    });
    // Use a swing worker as construct takes a lot of time
    SwingWorker sw = new SwingWorker() {
      JScrollPane jsp1;

      JScrollPane jsp2;

      @Override
      public Object construct() {
        try {
          jsp1 = getLastFMSuggestionsPanel(SuggestionType.OTHERS_ALBUMS);
          jsp2 = getLastFMSuggestionsPanel(SuggestionType.SIMILAR_AUTHORS);
        } catch (Exception e) {
          Log.error(e);
        }
        return null;
      }

      @Override
      public void finished() {
        tabs.setComponentAt(3, (jsp1 == null) ? new JPanel() : jsp1);
        tabs.setComponentAt(4, (jsp2 == null) ? new JPanel() : jsp2);
        super.finished();
      }

    };
    sw.start();
  }

  private void clearLastFMPanels() {
    tabs.setComponentAt(3, new JPanel());
    tabs.setComponentAt(4, new JPanel());
  }

  private JScrollPane getLocalSuggestionsPanel(SuggestionType type, boolean search) {
    FlowScrollPanel out = new FlowScrollPanel();
    out.setLayout(new FlowLayout(FlowLayout.LEFT));
    JScrollPane jsp = new JScrollPane(out, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jsp.setBorder(null);
    out.setScroller(jsp);
    List<Album> albums = null;
    if (search) {
      if (type == SuggestionType.BEST_OF) {
        albumsPrefered = AlbumManager.getInstance().getBestOfAlbums(
            ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
      } else if (type == SuggestionType.NEWEST) {
        albumsNewest = AlbumManager.getInstance().getNewestAlbums(
            ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
      } else if (type == SuggestionType.RARE) {
        albumsRare = AlbumManager.getInstance().getRarelyListenAlbums(
            ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
      }
    }
    if (type == SuggestionType.BEST_OF) {
      albums = albumsPrefered;
    } else if (type == SuggestionType.NEWEST) {
      albums = albumsNewest;
    } else if (type == SuggestionType.RARE) {
      albums = albumsRare;
    }
    if (albums != null && albums.size() > 0) {
      for (Album album : albums) {
        // Try creating the thumbnail
        ThumbnailManager.refreshThumbnail(album, "100x100");
        LocalAlbumThumbnail thumb = new LocalAlbumThumbnail(album, 100, false);
        thumb.populate();
        thumb.jlIcon.addMouseListener(new ThumbMouseListener());
        out.add(thumb);
      }
    } else {
      out.add(Util.getCentredPanel(new JLabel(Messages.getString("WikipediaView.3"))));
    }
    return jsp;
  }

  private JScrollPane getLastFMSuggestionsPanel(SuggestionType type) throws Exception {
    FlowScrollPanel out = new FlowScrollPanel();
    JScrollPane jsp = new JScrollPane(out, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    jsp.setBorder(null);
    out.setScroller(jsp);
    out.setLayout(new FlowLayout(FlowLayout.LEFT));
    if (type == SuggestionType.OTHERS_ALBUMS) {
      List<AudioScrobblerAlbum> albums = AudioScrobblerService.getInstance().getAlbumList(author);
      if (albums != null && albums.size() > 0) {
        for (AudioScrobblerAlbum album : albums) {
          AudioScrobblerAlbumThumbnail thumb = new AudioScrobblerAlbumThumbnail(album);
          thumb.populate();
          thumb.jlIcon.addMouseListener(new ThumbMouseListener());
          out.add(thumb);
        }
      }
      // No result found
      else {
        out.add(Util.getCentredPanel(new JLabel(Messages.getString("SuggestionView.7"))));
      }
    } else if (type == SuggestionType.SIMILAR_AUTHORS) {
      AudioScrobblerSimilarArtists similar = AudioScrobblerService.getInstance().getSimilarArtists(
          author);
      if (similar != null) {
        List<AudioScrobblerArtist> authors = similar.getArtists();
        for (AudioScrobblerArtist similarAuthor : authors) {
          AudioScrobblerAuthorThumbnail thumb = new AudioScrobblerAuthorThumbnail(similarAuthor);
          thumb.populate();
          thumb.jlIcon.addMouseListener(new ThumbMouseListener());
          out.add(thumb);
        }
      }
      // No result found
      else {
        out.add(Util.getCentredPanel(new JLabel(Messages.getString("SuggestionView.7"))));
      }
    }
    return jsp;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(Event event) {
    EventSubject subject = event.getSubject();
    if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)) {
      comp++;
      // Change local collection suggestions every 10 track plays
      if (comp % 10 == 0) {
        refreshLocalCollectionTabs(true);
      }
      // update last.fm panels
      refreshLastFMCollectionTabs();
    } else if (subject.equals(EventSubject.EVENT_PARAMETERS_CHANGE) && isLastFMTabsVisible()) {
      // The show/hide unmounted may have changed, refresh local
      // collection panels
      refreshLastFMCollectionTabs();
    } else if (subject.equals(EventSubject.EVENT_PLAYER_STOP)) {
      author = null;
      clearLastFMPanels();
    } else if (subject.equals(EventSubject.EVENT_COVER_REFRESH)) {
      refreshLocalCollectionTabs(false);
    }
  }

  /**
   * [Perf]
   * 
   * @return whether LastFM tabs are visible or not
   */
  private boolean isLastFMTabsVisible() {
    // Refresh authors only if user selected similar authors or albums tab
    return (tabs.getSelectedIndex() == 3 || tabs.getSelectedIndex() == 4)
    // Check this view perspective is visible
        && PerspectiveManager.getCurrentPerspective().equals(this.getPerspective());
  }

  /**
   * Refresh lastFM tabs on perspective selection if tabs visible
   */
  public void onPerspectiveSelection() {
    refreshLastFMCollectionTabs();
  }
}
