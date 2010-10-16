/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

package org.jajuk.ui.views;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.lyrics.LyricsService;
import org.jajuk.services.lyrics.providers.GenericWebLyricsProvider;
import org.jajuk.services.lyrics.providers.ILyricsProvider;
import org.jajuk.services.lyrics.providers.JajukLyricsProvider;
import org.jajuk.services.lyrics.providers.TagLyricsProvider;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukJToolbar;
import org.jajuk.ui.widgets.JajukToggleButton;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Lyrics view
 * <p>
 * Data comes from the Tag of the file or a txt file if present; otherwise from
 * www.lyrc.com.ar, lyrics.wikia.com or lyricsfly.com
 * </p>
 */
public class LyricsView extends ViewAdapter {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 2229941034734574056L;

  /** DOCUMENT_ME. */
  private JTextArea textarea = null;

  /** DOCUMENT_ME. */
  private JScrollPane jsp = null;

  /** DOCUMENT_ME. */
  private JLabel jlTitle = null;

  /** DOCUMENT_ME. */
  private String sURL = null;

  /** DOCUMENT_ME. */
  private Track track = null;

  /** DOCUMENT_ME. */
  private String lyrics = null;

  /** DOCUMENT_ME. */
  private JMenuItem jmiCopyToClipboard = null;

  /** DOCUMENT_ME. */
  private JMenuItem jmiLaunchInBrowser = null;

  /** DOCUMENT_ME. */
  private JPanel p;

  /** DOCUMENT_ME. */
  private JajukButton jbSave = null;

  /** DOCUMENT_ME. */
  private JajukButton jbDelete = null;

  /** DOCUMENT_ME. */
  private JajukToggleButton jtbEdit = null;

  /** DOCUMENT_ME. */
  private JajukLyricsProvider jajukLyricsProvider = null;

  /**
   * Instantiates a new lyrics view.
   */
  public LyricsView() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#initUI()
   */
  @Override
  public void initUI() {
    final JTextArea ta = getTextArea();
    final JLabel title = getJlTitle();
    final JScrollPane jspLyrics = getJsp();
    final FontManager fmgr = FontManager.getInstance();

    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    ta.setEditable(false);
    ta.setMargin(new Insets(10, 10, 10, 10));
    ta.setFont(fmgr.getFont(JajukFont.BOLD));
    ta.addMouseListener(new JajukMouseAdapter() {

      @Override
      public void handlePopup(final MouseEvent e) {
        final JPopupMenu menu = new JPopupMenu();
        menu.add(getJmiCopyToClipboard());
        if (UtilSystem.isBrowserSupported()) {
          getJmiLaunchInBrowser().putClientProperty(Const.DETAIL_CONTENT, sURL);
          getJmiCopyToClipboard().putClientProperty(Const.DETAIL_CONTENT, sURL);
          menu.add(getJmiLaunchInBrowser());
        }
        menu.show(getTextArea(), e.getX(), e.getY());
      }
    });

    title.setFont(fmgr.getFont(JajukFont.PLAIN_XL));
    ta.setFont(fmgr.getFont(JajukFont.PLAIN));
    initEditUI();

    //Create a toolbar to group edition commands
    JToolBar toolbarEdit = new JajukJToolbar();
    jtbEdit.add(toolbarEdit);
    jtbEdit.add(jbSave);
    jtbEdit.add(jbDelete);
    
    // Add items
    p = new JPanel(new MigLayout("insets 5,gapx 3, gapy 5,filly", "[][grow]", "[][grow]"));
    p.add(jtbEdit, "left");
    p.add(jlTitle, "center,wrap");
    p.add(jspLyrics, "span,grow");

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(p);
    ObservationManager.register(this);
    // Force initial message refresh
    UtilFeatures.updateStatus(this);
  }

  /**
   * Initializes the UI of edit lyrics mode.
   */
  public void initEditUI() {
    jtbEdit = getJtbEdit();
    jtbEdit.setToolTipText(Messages.getString("LyricsView.2"));
    jtbEdit.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent ev) {
        if (jtbEdit.isSelected()) {
          editLyrics(QueueModel.getPlayingFile());
          jtbEdit.setToolTipText(Messages.getString("LyricsView.3"));
        } else {
          exitEditLyrics(true);
          jtbEdit.setToolTipText(Messages.getString("LyricsView.2"));
        }
      }
    });

    jbSave = getJbSave();
    jbSave.setToolTipText(Messages.getString("LyricsView.4"));
    jbSave.setVisible(false);
    jbSave.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent ev) {
        jajukLyricsProvider.setLyrics(textarea.getText());
        try {
          LyricsService.commitLyrics(jajukLyricsProvider);
        } catch (IOException e) {
          Log.error(e);
        }
        exitEditLyrics(false);
      }
    });

    jbDelete = getJbDelete();
    jbDelete.setToolTipText(Messages.getString("LyricsView.5"));
    jbDelete.setVisible(false);
    jbDelete.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent ev) {
        try {
          LyricsService.deleteLyrics(jajukLyricsProvider);
        } catch (IOException e) {
          Log.error(e);
        }
        exitEditLyrics(true);
      }
    });
  }

  /**
   * Switch from lyrics view to edit mode.
   * 
   * @param file
   *          The file to edit lyrics for
   */
  public void editLyrics(final File file) {
    jajukLyricsProvider = getJajukLyricsProvider();
    jajukLyricsProvider.setFile(file);

    jbSave.setVisible(true);
    textarea.setEditable(true);
    // If lyrics already exist in tag or txt show delete button
    if (!(LyricsService.getCurrentProvider() instanceof GenericWebLyricsProvider)) {
      jbDelete.setVisible(true);
    }
  }

  /**
   * Switch from lyrics edit to view mode.
   * 
   * @param callUpdate
   *          Whether to call an update after switching
   */
  public void exitEditLyrics(boolean callUpdate) {
    textarea.setEditable(false);
    jbSave.setVisible(false);
    jbDelete.setVisible(false);
    jtbEdit.setSelected(false);
    if (callUpdate) {
      update(new JajukEvent(JajukEvents.FILE_LAUNCHED));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    final Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();

    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.LYRICS_DOWNLOADED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  @Override
  public void update(final JajukEvent event) {
    if (jtbEdit.isSelected()) {
      return;
    }
    final JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      final File file = QueueModel.getPlayingFile();
      // file is null is view started with no playing track (the event is
      // simulated in initUI())
      if (file == null) {
        return;
      }
      track = QueueModel.getPlayingFile().getTrack();

      exitEditLyrics(false);
      // If Internet access is allowed, download lyrics
      if (Conf.getBoolean(CONF_NETWORK_NONE_INTERNET_ACCESS)) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            removeAll();
            add(p);
            jlTitle.setText(track.getName());
            jtbEdit.setVisible(true);
            textarea.setText(Messages.getString("LyricsView.1"));
            jsp.setVisible(true);
            sURL = "<none>";
            revalidate();
            repaint();
          }
        });
        return;
      }

      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          removeAll();
          final JXBusyLabel busy = new JXBusyLabel();
          busy.setBusy(true);
          add(UtilGUI.getCentredPanel(busy, BoxLayout.X_AXIS));
          revalidate();
          repaint();
        }
      });
      new Thread("Lyrics Update Thread-" + track.getArtist().getName2() + "-" + track.getName()) {
        @Override
        public void run() {
          // Launch lyrics service asynchronously and out of the
          // AWT dispatcher thread
          lyrics = LyricsService.getLyrics(file);
          if (lyrics != null) {
            ILyricsProvider provider = LyricsService.getCurrentProvider();
            if (provider instanceof GenericWebLyricsProvider) {
              sURL = ((GenericWebLyricsProvider) provider).getWebURL(track.getArtist().getName2(),
                  track.getName()).toString();
            } else if (provider instanceof TagLyricsProvider) {
              sURL = "<Tag>";
            } else {
              sURL = "<Txt>";
            }

          } else {
            sURL = "<none>";
          }
          // Notify to make UI changes
          ObservationManager.notify(new JajukEvent(JajukEvents.LYRICS_DOWNLOADED));
        }
      }.start();

    } else if (JajukEvents.ZERO.equals(subject) || JajukEvents.PLAYER_STOP.equals(subject)) {
      reset();
    } else if (subject.equals(JajukEvents.WEBRADIO_LAUNCHED)) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          final WebRadio radio = (WebRadio) event.getDetails().get(Const.DETAIL_CONTENT);
          if (radio != null) {
            jlTitle.setText(radio.getName());
            jtbEdit.setVisible(false);
            jsp.setVisible(false);
            revalidate();
            repaint();
          }
        }
      });
    } else if (subject.equals(JajukEvents.LYRICS_DOWNLOADED)) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          removeAll();
          add(p);
          textarea.setToolTipText(sURL);
          if ((lyrics != null) && (lyrics.length() > 0)) {
            textarea.setText(lyrics);
          } else {
            textarea.setText(Messages.getString("WikipediaView.3"));
          }
          // Make sure to display the begin of the text (must be
          // done in a thread to be executed when textarea display
          // is actually finished)
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              jsp.getVerticalScrollBar().setValue(0);
            }
          });
          jlTitle.setText(track.getName());
          jsp.setVisible(true);
          jtbEdit.setVisible(true);
          revalidate();
          repaint();
        }
      });
    }
  }

  /**
   * Hide lyrics scrollable text and display a "Ready to play" message.
   */
  private void reset() {
    SwingUtilities.invokeLater(new Runnable() {

      @Override
      public void run() {
        jsp.setVisible(false);
        jtbEdit.setVisible(false);
        jlTitle.setText(Messages.getString("JajukWindow.18"));
      }

    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("LyricsView.0");
  }

  /**
   * Gets the text area.
   * 
   * @return the text area
   */
  private JTextArea getTextArea() {
    if (textarea == null) {
      textarea = new JTextArea();
    }
    return textarea;
  }

  /**
   * Gets the jsp.
   * 
   * @return the jsp
   */
  private JScrollPane getJsp() {
    if (jsp == null) {
      jsp = new JScrollPane(getTextArea());
    }
    return jsp;
  }

  /**
   * Gets the jl title.
   * 
   * @return the jl title
   */
  private JLabel getJlTitle() {
    if (jlTitle == null) {
      jlTitle = new JLabel();
    }
    return jlTitle;
  }


  /**
   * Gets the jb save.
   * 
   * @return the jb save
   */
  private JajukButton getJbSave() {
    if (jbSave == null) {
      jbSave = new JajukButton(IconLoader.getIcon(JajukIcons.SAVE));
    }
    return jbSave;
  }

  /**
   * Gets the jb delete.
   * 
   * @return the jb delete
   */
  private JajukButton getJbDelete() {
    if (jbDelete == null) {
      jbDelete = new JajukButton(IconLoader.getIcon(JajukIcons.DELETE));
    }
    return jbDelete;
  }

  /**
   * Gets the jtb edit.
   * 
   * @return the jtb edit
   */
  private JajukToggleButton getJtbEdit() {
    if (jtbEdit == null) {
      jtbEdit = new JajukToggleButton(IconLoader.getIcon(JajukIcons.EDIT));
      jtbEdit.setEnabled(true);
    }
    return jtbEdit;
  }

  /**
   * Gets the jmi copy to clipboard.
   * 
   * @return the jmi copy to clipboard
   */
  private JMenuItem getJmiCopyToClipboard() {
    if (jmiCopyToClipboard == null) {
      jmiCopyToClipboard = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    }
    return jmiCopyToClipboard;
  }

  /**
   * Gets the jmi launch in browser.
   * 
   * @return the jmi launch in browser
   */
  private JMenuItem getJmiLaunchInBrowser() {
    if (jmiLaunchInBrowser == null) {
      jmiLaunchInBrowser = new JMenuItem(ActionManager.getAction(JajukActions.LAUNCH_IN_BROWSER));
    }
    return jmiLaunchInBrowser;
  }

  /**
   * Gets the jajuk lyrics provider.
   * 
   * @return the jajuk lyrics provider
   */
  private JajukLyricsProvider getJajukLyricsProvider() {
    if (jajukLyricsProvider == null) {
      jajukLyricsProvider = new JajukLyricsProvider();
    }
    return jajukLyricsProvider;
  }

}
