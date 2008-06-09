/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 * $Revision$
 */

package org.jajuk.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionListener;

import org.jajuk.base.SearchResult;
import org.jajuk.base.TrackManager;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;

/**
 * Search combo box. Editable combo with search features
 */
public class SearchBox extends JTextField implements KeyListener {

  private static final long serialVersionUID = 1L;

  /** Do search panel need a search */
  private boolean bNeedSearch = false;

  /** Default time in ms before launching a search automatically */
  private static final int WAIT_TIME = 1000;

  /** Minimum number of characters to start a search */
  private static final int MIN_CRITERIA_LENGTH = 2;

  /** Search result */
  public List<SearchResult> alResults;

  /** Typed string */
  private String sTyped;

  public Popup popup;

  public JList jlist;

  private long lDateTyped;

  /** Listener to handle selections */
  private ListSelectionListener lsl;

  /** Search when typing timer */
  Timer timer = new Timer(100, new ActionListener() {
    public void actionPerformed(ActionEvent arg0) {
      if (bNeedSearch && (System.currentTimeMillis() - lDateTyped >= WAIT_TIME)) {
        new Thread() {
          @Override
          public void run() {
            search();
          }
        }.start();
      }
    }
  });

  /**
   * Display results as a jlabel with an icon
   */
  class SearchListRenderer extends JPanel implements ListCellRenderer {
    private static final long serialVersionUID = 8975989658927794678L;

    public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus) {
      SearchResult sr = (SearchResult) value;
      JPanel jp = new JPanel(new BorderLayout());
      JLabel jl = null;
      if (sr.getType() == SearchResultType.FILE) {
        jl = new JLabel(sr.getResu(), sr.getFile().getIconRepresentation(),
            SwingConstants.HORIZONTAL);
      } else if (sr.getType() == SearchResultType.WEBRADIO) {
        jl = new JLabel(sr.getResu(), IconLoader.ICON_WEBRADIO_16x16, SwingConstants.HORIZONTAL);
      }
      jp.add(jl, BorderLayout.WEST);
      return jp;
    }
  }

  /**
   * Constructor
   * 
   * @param lsl
   */
  public SearchBox(ListSelectionListener lsl) {
    this.lsl = lsl;
    timer.start();
    addKeyListener(this);
    setToolTipText(Messages.getString("SearchBox.0"));
    setBorder(BorderFactory.createEtchedBorder());
    setFont(FontManager.getInstance().getFont(JajukFont.SEARCHBOX));
    Color mediumGray = new Color(172, 172, 172);
    setForeground(mediumGray);
    setBorder(BorderFactory.createLineBorder(Color.BLUE));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  public void keyPressed(KeyEvent e) {
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
  }

  /**
   * Perform a search when user stop to type in the search combo for 2 sec or
   * pressed enter
   */
  private void search() {
    try {
      bNeedSearch = false;
      setEnabled(false); // no typing during search
      if (sTyped.length() >= MIN_CRITERIA_LENGTH) {
        // second test to get sure user didn't
        // typed before entering this method
        TreeSet<SearchResult> tsResu = TrackManager.getInstance().search(sTyped.toString());
        // Add web radio names
        tsResu.addAll(WebRadioManager.getInstance().search(sTyped.toString()));
        if (tsResu.size() > 0) {
          DefaultListModel model = new DefaultListModel();
          alResults = new ArrayList<SearchResult>();
          alResults.addAll(tsResu);
          for (SearchResult sr : tsResu) {
            model.addElement(sr);
          }
          jlist = new JList(model);
          jlist.setLayoutOrientation(JList.VERTICAL);
          jlist.setCellRenderer(new SearchListRenderer());
          PopupFactory factory = PopupFactory.getSharedInstance();
          JScrollPane jsp = new JScrollPane(jlist);
          int width = (int) ((float) Toolkit.getDefaultToolkit().getScreenSize().getWidth() * 0.7f);
          jsp.setMinimumSize(new Dimension(width, 250));
          jsp.setPreferredSize(new Dimension(width, 250));
          jsp.setMaximumSize(new Dimension(width, 250));
          jlist.setSelectionMode(0);
          jlist.addListSelectionListener(lsl);
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
          // take absolute coordonates in the screen (popups works
          // only on absolute coordonates in opposition to swing
          // widgets)
          SwingUtilities.convertPointToScreen(point, this);
          if (((int) point.getY() > 300) && (((int) point.getX() + 500 - (width)) > 0)) {
            popup = factory.getPopup(this, jsp, (int) point.getX() + 500 - (width), (int) point
                .getY() - 250);
          } else if (((int) point.getX() + 500 - (width)) > 0) {
            popup = factory.getPopup(this, jsp, (int) point.getX() + 500 - (width), (int) point
                .getY() + 30);
          } else {
            popup = factory.getPopup(this, jsp, 10, (int) point.getY() + 30);
          }
          popup.show();
        } else {
          if (popup != null) {
            popup.hide();
          }
        }
      }
      requestFocusInWindow();
    } catch (Exception e) {
      Log.error(e);
    } finally { // make sure to enable search box in all cases
      setEnabled(true);
    }
  }

}
