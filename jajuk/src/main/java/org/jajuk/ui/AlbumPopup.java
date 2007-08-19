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

package org.jajuk.ui;

import org.jajuk.base.Album;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.FIFO;
import org.jajuk.base.Item;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.YearManager;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jdesktop.swingx.JXPanel;
import org.jvnet.substance.SubstanceLookAndFeel;

import info.clearthought.layout.TableLayout;

import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import ext.services.lastfm.AudioScrobblerAlbum;

/**
 * HTML popup displayed over a thumbnail, it details album informations and tracks
 * <p>It is displayed nicely from provided jlabel position</p>
 */
public class AlbumPopup extends JDialog implements ITechnicalStrings {

	private static final long serialVersionUID = -8131528719972829954L;
	
	JXPanel jp;

	/**
	 * 
	 * @param album album to detail
	 * @param jlIcon album thumb jlabel
	 */
	public AlbumPopup(Album album,JLabel jlIcon) {
		super();
		initUI();
		final JEditorPane text = new JEditorPane("text/html", album.getAdvancedDescription());
		text.setEditable(false);
		text.setBackground(SubstanceLookAndFeel.getActiveColorScheme().getUltraLightColor());
		text.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == EventType.ACTIVATED) {
					URL url = e.getURL();
					if (XML_AUTHOR.equals(url.getHost())) {
						ArrayList<Item> items = new ArrayList<Item>(1);
						items.add(AuthorManager.getInstance().getItemByID(url.getQuery()));
						new PropertiesWizard(items);
					} else if (XML_STYLE.equals(url.getHost())) {
						ArrayList<Item> items = new ArrayList<Item>(1);
						items.add(StyleManager.getInstance().getItemByID(url.getQuery()));
						new PropertiesWizard(items);
					} else if (XML_YEAR.equals(url.getHost())) {
						ArrayList<Item> items = new ArrayList<Item>(1);
						items.add(YearManager.getInstance().getItemByID(url.getQuery()));
						new PropertiesWizard(items);
					} else if (XML_TRACK.equals(url.getHost())) {
						ArrayList<Item> items = new ArrayList<Item>(1);
						Track track = (Track) TrackManager.getInstance()
								.getItemByID(url.getQuery());
						items.add(track);
						ArrayList<org.jajuk.base.File> toPlay = new ArrayList<org.jajuk.base.File>(
								1);
						toPlay.add(track.getPlayeableFile(true));
						FIFO.getInstance().push(
								Util.createStackItems(Util.applyPlayOption(toPlay),
										ConfigurationManager.getBoolean(CONF_STATE_REPEAT), true),
								false);
					}
				}
				// change cursor on entering or leaving
				// hyperlinks
				// This doesn't work under JRE 1.5 (at least
				// under Linux), Sun issue ?
				else if (e.getEventType() == EventType.ENTERED) {
					text.setCursor(Util.LINK_CURSOR);
				} else if (e.getEventType() == EventType.EXITED) {
					text.setCursor(Util.DEFAULT_CURSOR);
				}
			}
		});
		final JScrollPane jspText = new JScrollPane(text);
		jspText.getVerticalScrollBar().setValue(0);
		jp.add(jspText, "0,0");
		setContentPane(jp);
		// compute dialog position ( note that setRelativeTo
		// is buggy and that we need more advanced positioning)
		int x = (int) jlIcon.getLocationOnScreen().getX() + (int) (0.6 * jlIcon.getWidth());
		// set position at 60 % of the picture
		int y = (int) jlIcon.getLocationOnScreen().getY() + (int) (0.6 * jlIcon.getHeight());
		int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		// Adjust position if details are located outside
		// the screen
		// in x-axis
		if ((x + 500) > screenWidth) {
			x = screenWidth - 510;
		}
		if ((y + 400) > screenHeight) {
			x = (int) jlIcon.getLocationOnScreen().getX() + (int) (0.6 * jlIcon.getWidth());
			if ((x + 500) > screenWidth) {
				x = screenWidth - 510;
			}
			y = (int) jlIcon.getLocationOnScreen().getY() + (int) (0.4 * jlIcon.getHeight()) - 400;
		}
		setLocation(x, y);
		setSize(500, 400);
		setVisible(true);
		// Force scrollbar to stay on top
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jspText.getVerticalScrollBar().setValue(0);
			}
		});
	}
	
	/**
	 * 
	 * @param album audio scrobber album to detail
	 * @param jlIcon album thumb jlabel
	 */
	public AlbumPopup(AudioScrobblerAlbum album,JLabel jlIcon) {
		/*super();
		initUI();
		final JEditorPane text = new JEditorPane("text/html", album.getAdvancedDescription());
		text.setEditable(false);
		text.setBackground(SubstanceLookAndFeel.getActiveColorScheme().getUltraLightColor());
		text.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == EventType.ACTIVATED) {
					URL url = e.getURL();
					if (XML_AUTHOR.equals(url.getHost())) {
						ArrayList<Item> items = new ArrayList<Item>(1);
						items.add(AuthorManager.getInstance().getItemByID(url.getQuery()));
						new PropertiesWizard(items);
					} else if (XML_STYLE.equals(url.getHost())) {
						ArrayList<Item> items = new ArrayList<Item>(1);
						items.add(StyleManager.getInstance().getItemByID(url.getQuery()));
						new PropertiesWizard(items);
					} else if (XML_YEAR.equals(url.getHost())) {
						ArrayList<Item> items = new ArrayList<Item>(1);
						items.add(YearManager.getInstance().getItemByID(url.getQuery()));
						new PropertiesWizard(items);
					} else if (XML_TRACK.equals(url.getHost())) {
						ArrayList<Item> items = new ArrayList<Item>(1);
						Track track = (Track) TrackManager.getInstance()
								.getItemByID(url.getQuery());
						items.add(track);
						ArrayList<org.jajuk.base.File> toPlay = new ArrayList<org.jajuk.base.File>(
								1);
						toPlay.add(track.getPlayeableFile(true));
						FIFO.getInstance().push(
								Util.createStackItems(Util.applyPlayOption(toPlay),
										ConfigurationManager.getBoolean(CONF_STATE_REPEAT), true),
								false);
					}
				}
				// change cursor on entering or leaving
				// hyperlinks
				// This doesn't work under JRE 1.5 (at least
				// under Linux), Sun issue ?
				else if (e.getEventType() == EventType.ENTERED) {
					text.setCursor(Util.LINK_CURSOR);
				} else if (e.getEventType() == EventType.EXITED) {
					text.setCursor(Util.DEFAULT_CURSOR);
				}
			}
		});
		final JScrollPane jspText = new JScrollPane(text);
		jspText.getVerticalScrollBar().setValue(0);
		jp.add(jspText, "0,0");
		setContentPane(jp);
		// compute dialog position ( note that setRelativeTo
		// is buggy and that we need more advanced positioning)
		int x = (int) jlIcon.getLocationOnScreen().getX() + (int) (0.6 * jlIcon.getWidth());
		// set position at 60 % of the picture
		int y = (int) jlIcon.getLocationOnScreen().getY() + (int) (0.6 * jlIcon.getHeight());
		int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		// Adjust position if details are located outside
		// the screen
		// in x-axis
		if ((x + 500) > screenWidth) {
			x = screenWidth - 510;
		}
		if ((y + 400) > screenHeight) {
			x = (int) jlIcon.getLocationOnScreen().getX() + (int) (0.6 * jlIcon.getWidth());
			if ((x + 500) > screenWidth) {
				x = screenWidth - 510;
			}
			y = (int) jlIcon.getLocationOnScreen().getY() + (int) (0.4 * jlIcon.getHeight()) - 400;
		}
		setLocation(x, y);
		setSize(500, 400);
		setVisible(true);
		// Force scrollbar to stay on top
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				jspText.getVerticalScrollBar().setValue(0);
			}
		});*/
	}
	
	private void initUI(){
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.NONE);
		jp = new JXPanel();
		jp.setAlpha(0.8f);
		double[][] size = { { TableLayout.FILL }, { TableLayout.FILL } };
		jp.setLayout(new TableLayout(size));
	}
}
