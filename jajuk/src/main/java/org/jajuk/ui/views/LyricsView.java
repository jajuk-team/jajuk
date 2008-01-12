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
 *  $Revision$
 */

package org.jajuk.ui.views;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
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
import javax.swing.SwingUtilities;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.base.WebRadio;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.lyrics.LyricsService;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Lyrics view
 * <p>
 * Data comes currently from http://www.lyrc.com.ar
 * </p>
 */
public class LyricsView extends ViewAdapter implements Observer {

  private static final long serialVersionUID = 2229941034734574056L;

  private JTextArea textarea = null;
  JScrollPane jsp = null;
  private JLabel jlTitle = null;
  private JLabel jlAuthor = null;
  private String sURL = null;
  private Track track = null;
  private String lyrics = null;
  private JMenuItem jmiCopyToClipboard = null;
  private JMenuItem jmiLaunchInBrowser = null;
  private JXBusyLabel busy = new JXBusyLabel();
  private JPanel p;

  public LyricsView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#initUI()
   */
  public void initUI() {
    final FormLayout layout = new FormLayout(
    // --columns
        "3dlu,p:grow, 3dlu",
        // --rows
        "5dlu, p, 3dlu, p, 3dlu,fill:" + (getHeight() - 200) + ":grow,3dlu");
    final PanelBuilder builder = new PanelBuilder(layout);
    final CellConstraints cc = new CellConstraints();
    p = builder.getPanel();
    final JTextArea textarea = getTextarea();
    final JLabel author = getJlAuthor();
    final JLabel title = getJlTitle();
    final JScrollPane jsp = getJsp();
    final FontManager fmgr = FontManager.getInstance();

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    textarea.setLineWrap(true);
    textarea.setWrapStyleWord(true);
    textarea.setEditable(false);
    textarea.setMargin(new Insets(10, 10, 10, 10));
    textarea.setFont(fmgr.getFont(JajukFont.BOLD));
    textarea.addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(final MouseEvent e) {
        if (e.isPopupTrigger()) {
          handlePopup(e);
        }
      }

      @Override
      public void mouseReleased(final MouseEvent e) {
        if (e.isPopupTrigger()) {
          handlePopup(e);
        }
      }

    });
    author.setFont(fmgr.getFont(JajukFont.PLAIN_L));
    title.setFont(fmgr.getFont(JajukFont.PLAIN_XL));
    textarea.setFont(fmgr.getFont(JajukFont.PLAIN));
    // Add items
    builder.add(jlTitle, cc.xy(2, 2));
    builder.add(jlAuthor, cc.xy(2, 4));
    builder.add(jsp, cc.xy(2, 6));
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(p);
    ObservationManager.register(this);
    reset();
    // check if a track has already been launched
    update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
        .getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
  }

  public void handlePopup(final MouseEvent e) {
    final JPopupMenu menu = new JPopupMenu();

    menu.add(getJmiCopyToClipboard());
    getJmiLaunchInBrowser().putClientProperty(DETAIL_CONTENT, sURL);
    menu.add(getJmiLaunchInBrowser());
    menu.show(getTextarea(), e.getX(), e.getY());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#getRegistrationKeys()
   */
  public Set<EventSubject> getRegistrationKeys() {
    final HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();

    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_ZERO);
    eventSubjectSet.add(EventSubject.EVENT_WEBRADIO_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_LYRICS_DOWNLOADED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(final Event event) {
    final EventSubject subject = event.getSubject();

    Log.debug("updating lyrics view");
    if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)) {
      final File file = FIFO.getInstance().getCurrentFile();
      // file is null is view started with no playing track (the event is
      // simulated in initUI())
      if (file == null) {
        return;
      }
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          removeAll();
          busy.setBusy(true);
          add(Util.getCentredPanel(busy, BoxLayout.X_AXIS));
          revalidate();
          repaint();
        }
      });

      new Thread() {
        @Override
        public void run() {

          track = FIFO.getInstance().getCurrentFile().getTrack();
          // Launch lyrics service asynchronously and out of the
          // AWT dispatcher thread
          Log.debug("calling LyricsService");
          lyrics = LyricsService.getLyrics(track.getAuthor().getName2(), track.getName());
          if (lyrics != null) {
            sURL = LyricsService.getCurrentProvider().getQueryString(track.getAuthor().getName2(),
                track.getName());
          } else {
            sURL = "<none>";
          }
          // Notify to make UI changes
          ObservationManager.notify(new Event(EventSubject.EVENT_LYRICS_DOWNLOADED));
        }

      }.start();
    } else if (subject.equals(EventSubject.EVENT_ZERO)) {
      reset();
    } else if (subject.equals(EventSubject.EVENT_WEBRADIO_LAUNCHED)) {
      SwingUtilities.invokeLater(new Runnable() {

        public void run() {
          final WebRadio radio = (WebRadio) event.getDetails().get(DETAIL_CONTENT);

          if (radio != null) {
            jlTitle.setText(radio.getName());
            jlAuthor.setText("");
            jsp.setVisible(false);
          }
        }

      });
    } else if (subject.equals(EventSubject.EVENT_LYRICS_DOWNLOADED)) {
      SwingUtilities.invokeLater(new Runnable() {

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

            public void run() {
              jsp.getVerticalScrollBar().setValue(0);
            }

          });
          jlAuthor.setText(track.getAuthor().getName2());
          jlTitle.setText(track.getName());
          Util.copyData = sURL;
          jsp.setVisible(true);
          revalidate();
          repaint();
        }

      });

    }
  }

  /**
   * Hide lyrics scrollable text and display a "Ready to play" message
   */
  private void reset() {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        jsp.setVisible(false);
        jlAuthor.setText("");
        jlTitle.setText(Messages.getString("JajukWindow.18"));
      }

    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("LyricsView.0");
  }

  private JTextArea getTextarea() {
    if (textarea == null) {
      textarea = new JTextArea();
    }
    return (textarea);
  }

  private JScrollPane getJsp() {
    if (jsp == null) {
      jsp = new JScrollPane(getTextarea());
    }
    return (jsp);
  }

  private JLabel getJlTitle() {
    if (jlTitle == null) {
      jlTitle = new JLabel();
    }
    return (jlTitle);
  }

  private JLabel getJlAuthor() {
    if (jlAuthor == null) {
      jlAuthor = new JLabel();
    }
    return (jlAuthor);
  }

  private JMenuItem getJmiCopyToClipboard() {
    if (jmiCopyToClipboard == null) {
      jmiCopyToClipboard = new JMenuItem(ActionManager.getAction(JajukAction.COPY_TO_CLIPBOARD));
    }
    return (jmiCopyToClipboard);
  }

  private JMenuItem getJmiLaunchInBrowser() {
    if (jmiLaunchInBrowser == null) {
      jmiLaunchInBrowser = new JMenuItem(ActionManager.getAction(JajukAction.LAUNCH_IN_BROWSER));
    }
    return (jmiLaunchInBrowser);
  }

}
