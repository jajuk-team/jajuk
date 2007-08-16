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

import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.FIFO;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.views.CoverView;
import org.jajuk.ui.wizard.CDDBWizard;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.TransferHandler;

/**
 * Album thumb represented as album cover + (optionally) others text information
 * and some features like dnd, menu item to play, search cover, album popup
 * display...
 */
public class AlbumThumb extends JPanel implements ITechnicalStrings, ActionListener, Transferable {

	private static final long serialVersionUID = 1L;

	/** Associated album */
	Album album;

	/** Size */
	int size;

	/** Associated file */
	File fCover;

	JPopupMenu jmenu;

	JMenuItem jmiAlbumPlay;

	JMenuItem jmiAlbumPush;

	JMenuItem jmiAlbumPlayShuffle;

	JMenuItem jmiAlbumPlayRepeat;

	JMenuItem jmiGetCovers;

	JMenuItem jmiAlbumCDDBWizard;

	JMenuItem jmiAlbumProperties;

	/** No cover flag */
	boolean bNoCover = false;

	public JLabel jlIcon;

	JTextArea jlAuthor;

	JTextArea jlAlbum;

	/** Dragging flag used to disable simple click behavior */
	private boolean bDragging = false;

	private boolean bShowText;

	static private long lDateLastMove;

	static private Point lastPosition;

	private boolean selected = false;

	/** Current details dialog */
	private static AlbumPopup details;

	private static AlbumThumb last;

	private static AlbumThumb mouseOverItem = null;

	/** Timer used to launch popup */
	static {
		Timer timerPopup = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					// Close popup ASAP when over none catalog item
					if (mouseOverItem == null) {
						if (details != null) {
							details.dispose();
						}
						last = null;
						// display a popup after n seconds only if item changed
					} else if ((System.currentTimeMillis() - lDateLastMove >= 1000)
							&& mouseOverItem != last) {
						// close popup if any visible
						if (details != null) {
							details.dispose();
						}
						// Store current item
						last = mouseOverItem;
						// Finally display the popup (Leave if user unselected
						// the option "Show catalog popups"
						if (ConfigurationManager.getBoolean(CONF_SHOW_POPUPS)) {
							mouseOverItem.displayPopup();
						}
					}
				} catch (Exception e) {
					// Make sure not to exit
					Log.error(e);
				}
			}
		});
		timerPopup.start();
	}

	/**
	 * Constructor
	 * 
	 * @param album :
	 *            associated album
	 * @param size :
	 *            size of the thumbnail
	 * @param bShowText:
	 *            Display album / author name under the icon or not ?
	 */
	public AlbumThumb(Album album, int size, boolean bShowText) {
		this.album = album;
		this.size = size;
		this.bShowText = bShowText;
		this.fCover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + 'x' + size + '/'
				+ album.getId() + '.' + EXT_THUMB);
	}

	/**
	 * display a popup over the catalog item
	 */
	public void displayPopup() {
		// don't show details if the contextual popup menu
		// is visible
		if (jmenu.isVisible()) {
			return;
		}
		AlbumPopup popup = new AlbumPopup(album, jlIcon);
		details = popup;
	}

	public void populate() {
		// create the thumbnail if it doesn't exist
		Util.refreshThumbnail(album, size + "x" + size);
		if (!fCover.exists() || fCover.length() == 0) {
			bNoCover = true;
			this.fCover = null;
		}
		double[][] dMain = null;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		jlIcon = new JLabel();
		ImageIcon ii = album.getThumbnail(size + "x" + size);
		if (!bNoCover) {
			ii.getImage().flush(); // flush image buffer to avoid JRE to
			// use old image
		}
		jlIcon.setIcon(ii);
		if (bShowText) {
			dMain = new double[][] { { TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL },
					{ size + 10, 10, TableLayout.PREFERRED, 5, TableLayout.PREFERRED } };
			setLayout(new TableLayout(dMain));
			int iRows = 7 + 3 * (size / 50 - 1);
			Font customFont = new Font("verdana", Font.BOLD, ConfigurationManager
					.getInt(CONF_FONTS_SIZE));
			Color mediumGray = new Color(172, 172, 172);

			Author author = AuthorManager.getInstance().getAssociatedAuthors(album).iterator()
					.next();
			jlAuthor = new JTextArea(author.getName2(), 1, iRows);
			jlAuthor.setLineWrap(true);
			jlAuthor.setWrapStyleWord(true);
			jlAuthor.setEditable(false);
			jlAuthor.setFont(customFont);
			jlAuthor.setForeground(mediumGray);
			jlAuthor.setBorder(null);

			jlAlbum = new JTextArea(album.getName2(), 1, iRows);
			jlAlbum.setLineWrap(true);
			jlAlbum.setWrapStyleWord(true);
			jlAlbum.setEditable(false);
			jlAuthor.setFont(new Font("Dialog", Font.BOLD, ConfigurationManager
					.getInt(CONF_FONTS_SIZE)));
			jlAlbum.setFont(new Font("Dialog", Font.BOLD, ConfigurationManager
					.getInt(CONF_FONTS_SIZE)));
			jlAlbum.setFont(customFont);
			jlAlbum.setForeground(mediumGray);
			jlAlbum.setBorder(null);
			add(jlIcon, "1,0,c,c");
			add(jlAuthor, "1,2");
			add(jlAlbum, "1,4");
		} else {
			dMain = new double[][] { { TableLayout.PREFERRED }, { TableLayout.PREFERRED } };
			setLayout(new TableLayout(dMain));
			add(jlIcon, "0,0");
		}
		//Keep this border as catalog view add a border by itself and it causes a lag
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		// Add dnd support
		jlIcon.setTransferHandler(new CatalogViewTransferHandler(this));

		// Album menu
		jmenu = new JPopupMenu();
		jmiAlbumPlay = new JMenuItem(Messages.getString("LogicalTreeView.15"),
				IconLoader.ICON_PLAY_16x16);
		jmiAlbumPlay.addActionListener(this);
		jmiAlbumPush = new JMenuItem(Messages.getString("LogicalTreeView.16"), IconLoader.ICON_PUSH);
		jmiAlbumPush.addActionListener(this);
		jmiAlbumPlayShuffle = new JMenuItem(Messages.getString("LogicalTreeView.17"),
				IconLoader.ICON_SHUFFLE);
		jmiAlbumPlayShuffle.addActionListener(this);
		jmiAlbumPlayRepeat = new JMenuItem(Messages.getString("LogicalTreeView.18"),
				IconLoader.ICON_REPEAT);
		jmiAlbumPlayRepeat.addActionListener(this);
		jmiGetCovers = new JMenuItem(Messages.getString("CatalogView.7"), IconLoader.ICON_COVER_16x16);
		jmiGetCovers.addActionListener(this);
		jmiAlbumCDDBWizard = new JMenuItem(Messages.getString("LogicalTreeView.34"),
				IconLoader.ICON_LIST);
		jmiAlbumCDDBWizard.addActionListener(this);
		jmiAlbumProperties = new JMenuItem(Messages.getString("LogicalTreeView.21"),
				IconLoader.ICON_PROPERTIES);
		jmiAlbumProperties.addActionListener(this);
		jmenu.add(jmiAlbumPlay);
		jmenu.add(jmiAlbumPush);
		jmenu.add(jmiAlbumPlayShuffle);
		jmenu.add(jmiAlbumPlayRepeat);
		jmenu.add(jmiAlbumCDDBWizard);
		jmenu.add(jmiGetCovers);
		jmenu.add(jmiAlbumProperties);

		jlIcon.addMouseMotionListener(new MouseMotionAdapter() {

			public void mouseDragged(MouseEvent e) {
				try {
					// Notify the mouse listener that we are dragging
					bDragging = true;
					JComponent c = (JComponent) e.getSource();
					TransferHandler handler = c.getTransferHandler();
					handler.exportAsDrag(c, e, TransferHandler.COPY);
				} finally {
					bDragging = false;
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				lDateLastMove = System.currentTimeMillis();
				lastPosition = e.getLocationOnScreen();
			}

		});

		jlIcon.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					handlePopup(e);
				} else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
					// Leave if already dragging
					if (bDragging) {
						return;
					}
					// Left click
					if (e.getButton() == MouseEvent.BUTTON1 && e.getSource() == jlIcon) {
						// if second click (item already selected), play
						if (selected) {
							play(false, false, false);
						}
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
				mouseOverItem = AlbumThumb.this;
			}

			public void mouseExited(MouseEvent e) {
				// Considere an exit only if mouse really moved to avoid
				// closing popup when popup appears over the mouse cursor
				// (then, a mouseExit event is thrown)
				if (!e.getLocationOnScreen().equals(lastPosition)) {
					mouseOverItem = null;
				}
			}

			public void mouseReleased(MouseEvent e) {
				// Leave if already dragging
				if (bDragging) {
					return;
				}
				if (e.isPopupTrigger()) {
					handlePopup(e);
				}
			}

			public void handlePopup(final MouseEvent e) {
				if (e.getSource() == jlIcon) {
					// Show contextual menu
					jmenu.show(jlIcon, e.getX(), e.getY());
					// Hide any details frame
					if (details != null) {
						details.dispose();
						details = null;
					}
				}
			}

		});
	}

	public boolean isNoCover() {
		return bNoCover;
	}

	/**
	 * 
	 * @param b
	 */
	public void setSelected(boolean b) {
		selected = b;
	}

	public void play(boolean bRepeat, boolean bShuffle, boolean bPush) {
		Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
		// compute selection
		ArrayList<org.jajuk.base.File> alFilesToPlay = new ArrayList<org.jajuk.base.File>(tracks
				.size());
		for (Track track : tracks) {
			org.jajuk.base.File file = track.getPlayeableFile(false);
			if (file != null) {
				alFilesToPlay.add(file);
			}
		}
		if (bShuffle) {
			Collections.shuffle(alFilesToPlay, new Random());
		}
		FIFO.getInstance().push(Util.createStackItems(alFilesToPlay, bRepeat, true), bPush);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		// Menu items
		if (e.getSource() == jmiAlbumPlay) {
			play(false, false, false);
		} else if (e.getSource() == jmiAlbumPlayRepeat) {
			play(true, false, false);
		} else if (e.getSource() == jmiAlbumPlayShuffle) {
			play(false, true, false);
		} else if (e.getSource() == jmiAlbumPush) {
			play(false, false, true);
		} else if (e.getSource() == jmiGetCovers) {
			new Thread() {
				public void run() {
					JDialog jd = new JDialog(Main.getWindow(), Messages.getString("CatalogView.18"));
					org.jajuk.base.File file = null;
					Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
					if (tracks.size() > 0) {
						Track track = tracks.iterator().next();
						file = track.getPlayeableFile(false);
					}
					CoverView cv = null;
					if (file != null) {
						cv = new CoverView(file);
						cv.setID("catalog/0");
						cv.initUI();
						jd.add(cv);
						jd.setAlwaysOnTop(true);
						jd.setSize(400, 450);
						jd.setLocationRelativeTo(null);
						jd.setVisible(true);
					} else {
						Messages.showErrorMessage(166);
					}
				}
			}.start();
		} else if (e.getSource() == jmiAlbumProperties) {
			ArrayList<Item> alAlbums = new ArrayList<Item>();
			alAlbums.add(album);
			// Show tracks infos to allow user to change year, rate...
			ArrayList<Item> alTracks = new ArrayList<Item>(TrackManager.getInstance()
					.getAssociatedTracks(album));
			new PropertiesWizard(alAlbums, alTracks);
		} else if (e.getSource() == jmiAlbumCDDBWizard) {
			ArrayList<Item> alTracks = new ArrayList<Item>(20);
			alTracks.addAll(TrackManager.getInstance().getAssociatedTracks(album));
			Util.waiting();
			new CDDBWizard(alTracks);
		}
	}

	public File getCoverFile() {
		return fCover;
	}

	public void setIcon(ImageIcon icon) {
		jlIcon.setIcon(icon);
		// !!! need to flush image because thy read image from a file
		// with same name
		// than previous image and a buffer would display the old image
		icon.getImage().flush();
	}

	/**
	 * @return the album
	 */
	public Album getAlbum() {
		return album;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return false;
	}

}
