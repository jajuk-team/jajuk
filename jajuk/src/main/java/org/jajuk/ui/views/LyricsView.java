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
 *  
 */

package org.jajuk.ui.views;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.File;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.lyrics.LyricsService;
import org.jajuk.services.lyrics.providers.GenericWebLyricsProvider;
import org.jajuk.services.lyrics.providers.ILyricsProvider;
import org.jajuk.services.lyrics.providers.JajukLyricsProvider;
import org.jajuk.services.lyrics.providers.TxtLyricsProvider;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.helpers.JajukMouseAdapter;
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
import org.jajuk.util.error.LyricsPersistenceException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Lyrics view
 * <p>
 * Data comes from the Tag of the file or a txt file if present; otherwise from
 * www.lyrc.com.ar, lyrics.wikia.com or lyricsfly.com
 * </p>
 */
public class LyricsView extends ViewAdapter implements DocumentListener {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 2229941034734574056L;

  private JTextArea jtaLyrics;

  private JScrollPane jspLyrics;

  private JLabel jlTitle;

  private String sURL;

  /** Currently analyzed file. */
  private File file;

  private String lyrics;

  private JMenuItem jmiCopyToClipboard;

  private JMenuItem jmiLaunchInBrowser;

  private JPanel jpMain;

  private JajukButton jbSave;

  private JajukButton jbDelete;

  private JajukToggleButton jtbEdit;

  /** Edition toolbar. */
  private JToolBar toolbarEdit;

  private boolean changeDetected = false;

  /**
   * .
   */
  class LyricsUpdateThread extends Thread {

    /**
     * Instantiates a new lyrics update thread.
     */
    LyricsUpdateThread() {
      super("Lyrics Update Thread-" + file.getTrack().getArtist().getName2() + "-"
          + file.getTrack().getName());
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      // Launch lyrics service asynchronously and out of the
      // AWT dispatcher thread
      lyrics = LyricsService.getLyrics(file);
      if (lyrics != null) {
        ILyricsProvider provider = LyricsService.getCurrentProvider();
        sURL = provider.getSourceAddress();
      } else {
        sURL = "<none>";
      }
      // Notify to make UI changes
      ObservationManager.notify(new JajukEvent(JajukEvents.LYRICS_DOWNLOADED));
    }

  }

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
    final FontManager fmgr = FontManager.getInstance();
    jtaLyrics = new JTextArea();
    jtaLyrics.setFont(fmgr.getFont(JajukFont.PLAIN));
    jlTitle = new JLabel();
    jlTitle.setFont(fmgr.getFont(JajukFont.PLAIN_L));
    jspLyrics = new JScrollPane(jtaLyrics);

    jtaLyrics.setLineWrap(true);
    jtaLyrics.setWrapStyleWord(true);
    jtaLyrics.setEditable(false);
    jtaLyrics.setMargin(new Insets(10, 10, 10, 10));
    jtaLyrics.setFont(fmgr.getFont(JajukFont.BOLD));
    jtaLyrics.addMouseListener(new JajukMouseAdapter() {

      @Override
      public void handlePopup(final MouseEvent e) {
        final JPopupMenu menu = new JPopupMenu();
        menu.add(jmiCopyToClipboard);
        if (UtilSystem.isBrowserSupported()) {
          jmiLaunchInBrowser.putClientProperty(Const.DETAIL_CONTENT, sURL);
          jmiCopyToClipboard.putClientProperty(Const.DETAIL_CONTENT, sURL);
          menu.add(jmiLaunchInBrowser);
        }
        menu.show(jtaLyrics, e.getX(), e.getY());
      }
    });
    // Detect text area content change to enable save button on changes
    jtaLyrics.getDocument().addDocumentListener(this);
    initEditUI();

    //Create a toolbar to group edition commands
    toolbarEdit = new JajukJToolbar();
    toolbarEdit.add(jtbEdit);
    toolbarEdit.add(jbSave);
    toolbarEdit.add(jbDelete);

    // Menu items
    jmiCopyToClipboard = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    if (UtilSystem.isBrowserSupported()) {
      jmiLaunchInBrowser = new JMenuItem(ActionManager.getAction(JajukActions.LAUNCH_IN_BROWSER));
    }

    // Add items
    jpMain = new JPanel(new MigLayout("insets 5,gapx 3, gapy 5,filly", "[95][grow]", "[][grow]"));
    jpMain.add(jtbEdit, "left,split 3");
    jpMain.add(jbSave, "left");
    jpMain.add(jbDelete, "left");
    jpMain.add(jlTitle, "left,wrap");
    jpMain.add(jspLyrics, "span,grow");

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(jpMain);
    ObservationManager.register(this);

    // force initial buttons states
    updateButtonsState();

    // Force initial message refresh
    UtilFeatures.updateStatus(this);
  }

  /**
   * Initializes the UI of edit lyrics mode.
   */
  public void initEditUI() {
    jtbEdit = new JajukToggleButton(IconLoader.getIcon(JajukIcons.EDIT));
    jtbEdit.setToolTipText(Messages.getString("LyricsView.2"));
    jtbEdit.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent ev) {
        if (jtbEdit.isSelected()) {
          jtaLyrics.setEditable(true);
          // Don't keep "No result found" text
          if (jtaLyrics.getText().equals(Messages.getString("WikipediaView.3"))) {
            jtaLyrics.getDocument().removeDocumentListener(LyricsView.this);
            jtaLyrics.setText("");
            jtaLyrics.getDocument().addDocumentListener(LyricsView.this);
          }
          jtaLyrics.requestFocus();
          jtbEdit.setToolTipText(Messages.getString("LyricsView.3"));
        } else {
          exitEditLyrics(true);
          jtbEdit.setToolTipText(Messages.getString("LyricsView.2"));
        }
      }
    });

    jbSave = new JajukButton(IconLoader.getIcon(JajukIcons.SAVE));
    jbSave.setToolTipText(Messages.getString("LyricsView.4"));
    jbSave.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          JajukLyricsProvider provider = getJajukProvider();
          LyricsService.commitLyrics(provider);
        } catch (LyricsPersistenceException lpe) {
          Log.error(lpe);
          // Always the same i18n message : "Operation failed"
          Messages.showErrorMessage(136, lpe.getMessage());
        }
        exitEditLyrics(false);
      }
    });

    jbDelete = new JajukButton(IconLoader.getIcon(JajukIcons.DELETE));
    jbDelete.setToolTipText(Messages.getString("LyricsView.5"));
    jbDelete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        try {
          JajukLyricsProvider provider = getJajukProvider();
          LyricsService.deleteLyrics(provider);
        } catch (LyricsPersistenceException lpe) {
          Log.error(lpe);
          Messages.showErrorMessage(136, lpe.getMessage());
        }
        exitEditLyrics(true);
      }
    });
  }

  /**
   * Get the GUI provider.
   *
   * @return the jajuk provider
   */
  public JajukLyricsProvider getJajukProvider() {
    JajukLyricsProvider jajukLyricsProvider = new JajukLyricsProvider();
    jajukLyricsProvider.setAudioFile(file);
    jajukLyricsProvider.setLyrics(jtaLyrics.getText());
    return jajukLyricsProvider;
  }

  /**
   * Switch from lyrics edit to view mode.
   *
   * @param callUpdate Whether to call an update after switching
   */
  public void exitEditLyrics(boolean callUpdate) {
    changeDetected = false;
    jtaLyrics.setEditable(false);
    jtbEdit.setSelected(false);
    updateButtonsState();
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
    // Ignore any event while we are editing
    if (jtbEdit.isSelected()) {
      return;
    }
    final JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      file = QueueModel.getPlayingFile();
      // file is null is view started with no playing track (the event is
      // simulated in initUI())
      if (file == null) {
        return;
      }
      // If Internet access is allowed, download lyrics
      if (Conf.getBoolean(CONF_NETWORK_NONE_INTERNET_ACCESS)) {
        resetNoInternet();
      } else {
        showBuzyLabel();
        // Launch lyrics search asynchronously
        new LyricsUpdateThread().start();
      }
    } else if (JajukEvents.ZERO.equals(subject) || JajukEvents.PLAYER_STOP.equals(subject)) {
      reset();
      file = null;
    } else if (subject.equals(JajukEvents.WEBRADIO_LAUNCHED)) {
      resetWebradio((WebRadio) event.getDetails().get(Const.DETAIL_CONTENT));
      file = null;
    } else if (subject.equals(JajukEvents.LYRICS_DOWNLOADED)) {
      refreshLyrics();
    }
  }

  /**
   * Reset GUI in case of Internet disabled.
   */
  private void resetNoInternet() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        removeAll();
        add(jpMain);
        jlTitle.setText(file.getTrack().getName());
        jlTitle.setToolTipText(file.getTrack().getName());
        updateButtonsState();
        jtaLyrics.setText(Messages.getString("LyricsView.1"));
        jspLyrics.setEnabled(true);
        sURL = "<none>";
        revalidate();
        repaint();
      }
    });
  }

  /**
   * Show buzy label when searching lyrics.
   */
  private void showBuzyLabel() {
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
  }

  /**
   * Reset webradio.
   *
   * @param radio 
   */
  private void resetWebradio(final WebRadio radio) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (radio != null) {
          jlTitle.setText(radio.getName());
          updateButtonsState();
          jspLyrics.setEnabled(false);
          updateButtonsState();
          revalidate();
          repaint();
        }
      }
    });
  }

  /**
   * Compute buttons states.
   */
  private void updateButtonsState() {
    ILyricsProvider provider = LyricsService.getCurrentProvider();
    // Delete button
    jbDelete.setEnabled(file != null && provider != null
        && !(provider instanceof GenericWebLyricsProvider));

    // Save button : enabled only for changes in the text area or
    // if we just got lyrics from the web or form a txt file
    // (so user can try to commit it to the tag)
    jbSave.setEnabled((jtbEdit.isSelected() && changeDetected)
        || provider instanceof GenericWebLyricsProvider || provider instanceof TxtLyricsProvider);
  }

  /**
   * Refresh lyrics once downloaded.
   */
  private void refreshLyrics() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        removeAll();
        add(jpMain);
        if ((lyrics != null) && (lyrics.length() > 0)) {
          jtaLyrics.setText(lyrics);
        } else {
          jtaLyrics.setText(Messages.getString("WikipediaView.3"));
        }
        // Make sure to display the begin of the text (must be
        // done in a thread to be executed when textarea display
        // is actually finished)
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            jspLyrics.getVerticalScrollBar().setValue(0);
          }
        });
        jlTitle.setText(file.getTrack().getName());
        jlTitle.setToolTipText(sURL);
        jspLyrics.setEnabled(true);
        updateButtonsState();
        revalidate();
        repaint();
      }
    });
  }

  /**
   * Hide lyrics scrollable text and display a "Ready to play" message.
   */
  private void reset() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jspLyrics.setEnabled(false);
        updateButtonsState();
        jlTitle.setText(Messages.getString("JajukWindow.18"));
        jtaLyrics.getDocument().removeDocumentListener(LyricsView.this);
        jtaLyrics.setText("");
        jtaLyrics.getDocument().addDocumentListener(LyricsView.this);
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

  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
  public void removeUpdate(DocumentEvent e) {
    changeDetected = true;
    updateButtonsState();
  }

  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
  public void insertUpdate(DocumentEvent e) {
    changeDetected = true;
    updateButtonsState();
  }

  /* (non-Javadoc)
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
  public void changedUpdate(DocumentEvent e) {
    changeDetected = true;
    updateButtonsState();
  }

}
