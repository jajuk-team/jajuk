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
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.lyrics.LyricsService;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
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
  private final JXBusyLabel busy = new JXBusyLabel();
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
    final JTextArea ta = getTextarea();
    final JLabel author = getJlAuthor();
    final JLabel title = getJlTitle();
    final JScrollPane jScrollPane = getJsp();
    final FontManager fmgr = FontManager.getInstance();

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    ta.setLineWrap(true);
    ta.setWrapStyleWord(true);
    ta.setEditable(false);
    ta.setMargin(new Insets(10, 10, 10, 10));
    ta.setFont(fmgr.getFont(JajukFont.BOLD));
    ta.addMouseListener(new MouseAdapter() {

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
    ta.setFont(fmgr.getFont(JajukFont.PLAIN));
    // Add items
    builder.add(jlTitle, cc.xy(2, 2));
    builder.add(jlAuthor, cc.xy(2, 4));
    builder.add(jScrollPane, cc.xy(2, 6));
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(p);
    ObservationManager.register(this);
    // Force initial message refresh
    UtilFeatures.updateStatus(this);
  }

  public void handlePopup(final MouseEvent e) {
    final JPopupMenu menu = new JPopupMenu();

    menu.add(getJmiCopyToClipboard());
    getJmiLaunchInBrowser().putClientProperty(Const.DETAIL_CONTENT, sURL);
    getJmiCopyToClipboard().putClientProperty(Const.DETAIL_CONTENT, sURL);
    menu.add(getJmiLaunchInBrowser());
    menu.show(getTextarea(), e.getX(), e.getY());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#getRegistrationKeys()
   */
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
  public void update(final JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
      // If Internet access is allowed, download lyrics
      if (Conf.getBoolean(CONF_NETWORK_NONE_INTERNET_ACCESS)) {
        reset();
        return;
      }
      final File file = QueueModel.getPlayingFile();
      // file is null is view started with no playing track (the event is
      // simulated in initUI())
      if (file == null) {
        return;
      }
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          removeAll();
          busy.setBusy(true);
          add(UtilGUI.getCentredPanel(busy, BoxLayout.X_AXIS));
          revalidate();
          repaint();
        }
      });
      new Thread() {
        @Override
        public void run() {
          track = QueueModel.getPlayingFile().getTrack();
          // Launch lyrics service asynchronously and out of the
          // AWT dispatcher thread
          lyrics = LyricsService.getLyrics(track.getAuthor().getName2(), track.getName());
          if (lyrics != null) {
            sURL = LyricsService.getCurrentProvider().getWebURL(track.getAuthor().getName2(),
                track.getName()).toString();
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

        public void run() {
          final WebRadio radio = (WebRadio) event.getDetails().get(Const.DETAIL_CONTENT);

          if (radio != null) {
            jlTitle.setText(radio.getName());
            jlAuthor.setText("");
            jsp.setVisible(false);
          }
        }

      });
    } else if (subject.equals(JajukEvents.LYRICS_DOWNLOADED)) {
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
      jmiCopyToClipboard = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    }
    return (jmiCopyToClipboard);
  }

  private JMenuItem getJmiLaunchInBrowser() {
    if (jmiLaunchInBrowser == null) {
      jmiLaunchInBrowser = new JMenuItem(ActionManager.getAction(JajukActions.LAUNCH_IN_BROWSER));
    }
    return (jmiLaunchInBrowser);
  }

}
