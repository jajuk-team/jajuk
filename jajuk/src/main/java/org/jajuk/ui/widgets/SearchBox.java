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

package org.jajuk.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.base.SearchResult;
import org.jajuk.base.TrackManager;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Search combo box. Editable combo with search features. Comes with a default
 * selection implementation (see valueChanged() method) that could be changed
 */
public class SearchBox extends JTextField implements KeyListener, ListSelectionListener {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Do search panel need a search. */
  private boolean bNeedSearch = false;

  /** Default time in ms before launching a search automatically. */
  private static final int WAIT_TIME = 1000;

  /** Minimum number of characters to start a search. */
  private static final int MIN_CRITERIA_LENGTH = 2;

  /** Search result. */
  private List<SearchResult> alResults;

  /** Typed string. */
  private String sTyped;

  /** DOCUMENT_ME. */
  private Popup popup;

  /** DOCUMENT_ME. */
  private JList jlist;

  /** DOCUMENT_ME. */
  private long lDateTyped;

  /** Search when typing timer. */
  Timer timer = new Timer(100, new ActionListener() {
    public void actionPerformed(ActionEvent arg0) {
      if (bNeedSearch && (System.currentTimeMillis() - lDateTyped >= WAIT_TIME)) {
        search();
      }
    }
  });

  /**
   * Display results as a jlabel with an icon.
   */
  private static class SearchListRenderer extends JPanel implements ListCellRenderer {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 8975989658927794678L;

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing .JList,
     * java.lang.Object, int, boolean, boolean)
     */
    public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus) {
      SearchResult sr = (SearchResult) value;
      JPanel jp = new JPanel(new BorderLayout());
      JLabel jl = null;
      if (sr.getType() == SearchResultType.FILE) {
        jl = new JLabel(sr.getResu(), sr.getFile().getIconRepresentation(),
            SwingConstants.HORIZONTAL);
      } else if (sr.getType() == SearchResultType.WEBRADIO) {
        jl = new JLabel(sr.getResu(), IconLoader.getIcon(JajukIcons.WEBRADIO_16X16),
            SwingConstants.HORIZONTAL);
      }
      jp.add(jl, BorderLayout.WEST);
      return jp;
    }
  }

  /**
   * Constructor.
   */
  public SearchBox() {
    setMargin(new Insets(0, 20, 0, 0));
    timer.start();
    addKeyListener(this);
    setToolTipText(Messages.getString("SearchBox.0"));
    // We use a font whose size cannot change with font size selected by user
    // because the search box cannot be enlarged vertically
    setFont(FontManager.getInstance().getFont(JajukFont.SEARCHBOX));
    Color mediumGray = new Color(172, 172, 172);
    setForeground(mediumGray);
    installKeysrokes();
    // Double click empties the field
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        if (e.getClickCount() == 2) {
          setText("");
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e) {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  public void keyReleased(KeyEvent e) {
    if (e.getKeyChar() == KeyEvent.VK_ESCAPE && popup != null) {
      popup.hide();
      return;
    }
    bNeedSearch = false; // stop clock for auto-search
    sTyped = getText();
    if (sTyped.length() >= MIN_CRITERIA_LENGTH) {
      // perform automatic search only when user provide more than 5
      // letters
      if (e.getKeyChar() == KeyEvent.VK_ENTER) {
        search();
      } else {
        bNeedSearch = true;
        lDateTyped = System.currentTimeMillis();
      }
    } else if (popup != null) {
      popup.hide();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  public void keyTyped(KeyEvent e) {
    // required by interface, but nothing to do here...
  }

  /**
   * Perform a search when user stop to type in the search combo for 2 sec or
   * pressed enter.
   */
  private void search() {
    bNeedSearch = false;
    setEnabled(false); // no typing during search
    // second test to get sure user didn't
    // typed before entering this method
    if (sTyped.length() >= MIN_CRITERIA_LENGTH) {
      SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

        List<SearchResult> resu = null;

        @Override
        public Void doInBackground() {
          try {
            UtilGUI.waiting();
            resu = TrackManager.getInstance().search(sTyped);
            // Add web radio names
            resu.addAll(WebRadioManager.getInstance().search(sTyped));
            // Sort the whole list
            Collections.sort(resu);
          } catch (Exception e) {
            Log.error(e);
          }
          return null;
        }

        @Override
        public void done() {
          if (resu != null && resu.size() > 0) {
            DefaultListModel model = new DefaultListModel();
            SearchBox.this.alResults = resu;
            for (SearchResult sr : resu) {
              model.addElement(sr);
            }
            jlist = new JList(model);
            jlist.setLayoutOrientation(JList.VERTICAL);
            jlist.addListSelectionListener(SearchBox.this);
            jlist.setCellRenderer(new SearchListRenderer());
            PopupFactory factory = PopupFactory.getSharedInstance();
            JScrollPane jsp = new JScrollPane(jlist);
            int width = (int) ((float) Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.7f);
            jsp.setMinimumSize(new Dimension(width, 250));
            jsp.setPreferredSize(new Dimension(width, 250));
            jsp.setMaximumSize(new Dimension(width, 250));
            jlist.setSelectionMode(0);
            // For some reasons, we get the waiting cursor on the popup
            // sometimes, force it to default
            jlist.setCursor(UtilGUI.DEFAULT_CURSOR);
            jsp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            if (popup != null) {
              popup.hide();
            }
            // take upper-left point relative to the
            // textfield
            Point point = new Point(0, 0);
            // take absolute coordinates in the screen (popups works
            // only on absolute coordinates in opposition to swing
            // widgets)
            SwingUtilities.convertPointToScreen(point, SearchBox.this);
            if (((int) point.getY() > 300) && (((int) point.getX() + 500 - (width)) > 0)) {
              popup = factory.getPopup(null, jsp, (int) point.getX() + 500 - (width), (int) point
                  .getY() - 250);
            } else if (((int) point.getX() + 500 - (width)) > 0) {
              popup = factory.getPopup(null, jsp, (int) point.getX() + 500 - (width), (int) point
                  .getY() + 30);
            } else {
              popup = factory.getPopup(null, jsp, 10, (int) point.getY() + 30);
            }
            popup.show();
            jlist.addMouseListener(new MouseAdapter() {
              @Override
              public void mouseExited(MouseEvent e) {
                popup.hide();
              }
            });
          } else {
            if (popup != null) {
              popup.hide();
            }
          }
          requestFocusInWindow();
          setEnabled(true);
          UtilGUI.stopWaiting();
        }
      };
      sw.execute();
    }
  }

  /**
   * Gets the selected index.
   * 
   * @return the selected index
   */
  public int getSelectedIndex() {
    return jlist.getSelectedIndex();
  }

  /**
   * Gets the result or 
   * 
   * @param index item or null if none search already performed
   * 
   * @return the result 
   */
  public SearchResult getResult() {
    if (jlist == null){
      return null;
    }
    return alResults.get(getSelectedIndex());
  }

  /**
   * Hide popup. 
   */
  public void hidePopup() {
    popup.hide();
  }

  /**
   * Display the search icon inside the texfield.
   * 
   * @param g
   *          the graphics
   */
  @Override
  public void paint(Graphics g) {
    super.paint(g);
    g.drawImage(IconLoader.getIcon(JajukIcons.SEARCH).getImage(), 4, 3, 16, 16, null);
  }

  /**
   * Default list selection implementation (may be overwritten for different
   * behavior).
   * 
   * @param e
   *          DOCUMENT_ME
   */
  public void valueChanged(final ListSelectionEvent e) {
    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
      @Override
      public Void doInBackground() {
        if (!e.getValueIsAdjusting()) {
          SearchResult sr = getResult();
          try {
            // If user selected a file
            if (sr.getType() == SearchResultType.FILE) {
              QueueModel.push(new StackItem(sr.getFile(), Conf
                  .getBoolean(Const.CONF_STATE_REPEAT_ALL), true), Conf
                  .getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
            }
            // User selected a web radio
            else if (sr.getType() == SearchResultType.WEBRADIO) {
              QueueModel.launchRadio(sr.getWebradio());
            }
          } catch (JajukException je) {
            Log.error(je);
          }
        }
        return null;
      }

      @Override
      public void done() {
        if (!e.getValueIsAdjusting()) {
          hidePopup();
          requestFocusInWindow();
        }
      }
    };
    sw.execute();
  }

  /**
   * Free up resources, timers, ...
   * 
   * TODO: I could not find out any way to do this automatically! How can I
   * listen on some event that is sent when the enclosing dialog is closed?
   */
  public void close() {
    // stop the timer so it does not keep the element from garbage collection
    timer.stop();
  }

  /**
   * Search box specific keystrokes
   */
  private void installKeysrokes() {
    InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = getActionMap();
    inputMap.put(KeyStroke.getKeyStroke("ctrl F"), "search");
    // We don't create a JajukAction dedicated class for this very simple case 
    actionMap.put("search", new JajukAction("search", true) {
      private static final long serialVersionUID = 1L;

      @Override
      public void perform(ActionEvent evt) throws Exception {
        requestFocusInWindow();
      }
    });
  }
}
