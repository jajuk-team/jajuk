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

package org.jajuk.ui;

import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult;
import org.jajuk.i18n.Messages;
import org.jajuk.util.log.Log;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionListener;

/**
 * Search combo box. Editable combo with search features
 */
public class SearchBox extends JTextField implements KeyListener {

	private static final long serialVersionUID = 1L;

	/** Do search panel need a search */
	private boolean bNeedSearch = false;

	/** Default time in ms before launching a search automaticaly */
	private static final int WAIT_TIME = 500;

	/** Minimum number of characters to start a search */
	private static final int MIN_CRITERIA_LENGTH = 1;

	/** Search result */
	public ArrayList<SearchResult> alResults;

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
				search();
			}

		}

	});

	/**
	 * Constructor
	 * 
	 * @param lsl
	 */
	public SearchBox(ListSelectionListener lsl) {
		super(7);
		this.lsl = lsl;
		timer.start();
		addKeyListener(this);
		setToolTipText(Messages.getString("SearchBox.0")); 
		setBorder(BorderFactory.createEtchedBorder());
		setFont(new Font("dialog", Font.BOLD, 18)); 
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
				TreeSet<SearchResult> tsResu = FileManager.getInstance().search(sTyped.toString());
				if (tsResu.size() > 0) {
					DefaultListModel model = new DefaultListModel();
					alResults = new ArrayList<SearchResult>();
					alResults.addAll(tsResu);
					Iterator it = tsResu.iterator();
					while (it.hasNext()) {
						model.addElement(((SearchResult) it.next()).getResu());
					}
					jlist = new JList(model);
					PopupFactory factory = PopupFactory.getSharedInstance();
					JScrollPane jsp = new JScrollPane(jlist);
					jlist.setSelectionMode(0);
					jlist.addListSelectionListener(lsl);
					jsp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
					if (popup != null) {
						popup.hide();
					}
					// take upper-left point relative to the
					// textfield
					Point point = new Point(0, 0);
					// take absolute coordonates in the screen (popups works
					// only on absolute coordonates in oposition to swing
					// widgets)
					SwingUtilities.convertPointToScreen(point, this);
					popup = factory
							.getPopup(this, jsp, (int) point.getX(), (int) point.getY() + 25);
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
