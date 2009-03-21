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

package org.jajuk.ui.thumbnails;

import ext.SwingWorker;
import ext.services.lastfm.AudioScrobblerAlbum;
import ext.services.lastfm.AudioScrobblerArtist;
import ext.services.lastfm.AudioScrobblerService;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.jajuk.base.AuthorManager;
import org.jajuk.base.Item;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 * Last.FM Album thumb represented as artists label + (optionally) others text
 * information display...
 */
public class AudioScrobblerAuthorThumbnail extends AbstractThumbnail {

  private static final long serialVersionUID = -804471264407148566L;

  /** Associated author */
  private final AudioScrobblerArtist author;

  /** Is this author known in collection ? */
  private final boolean bKnown;

  /**
   * @param album :
   *          associated album
   */
  public AudioScrobblerAuthorThumbnail(AudioScrobblerArtist author) {
    super(100);
    this.author = author;
    bKnown = (AuthorManager.getInstance().getAuthorByName(author.getName()) != null);
  }

  @Override
  public void populate() {
    jlIcon = new JLabel();

    SwingWorker sw = new SwingWorker() {

      ImageIcon ii;

      @Override
      public Object construct() {
        try {
          // Check if author is null
          String authorUrl = author.getImageUrl();
          if (UtilString.isVoid(authorUrl)) {
            return null;
          }
          // Download thumb
          URL remote = new URL(authorUrl);
          // Download the picture and store file reference (to
          // generate the popup thumb for ie)
          fCover = DownloadManager.downloadToCache(remote);
          BufferedImage image = ImageIO.read(fCover);
          ImageIcon downloadedImage = new ImageIcon(image);
          ii = UtilGUI.getScaledImage(downloadedImage, 100);
          // Free images memory
          downloadedImage.getImage().flush();
          image.flush();
        } catch (Exception e) {
          Log.error(e);
        }
        return null;
      }

      @Override
      public void finished() {
        // Check if author is null
        if (ii == null) {
          return;
        }

        super.finished();
        postPopulate();
        jlIcon.setIcon(ii);
        setLayout(new VerticalLayout(2));
        // Use a panel to allow text to be bigger than image under it
        add(UtilGUI.getCentredPanel(jlIcon));
        JLabel jlTitle = new JLabel(UtilString.getLimitedString(author.getName(), 15));
        jlTitle.setToolTipText(author.getName());
        if (bKnown) {
          // Artist known in collection, display its name in bold
          jlTitle.setIcon(IconLoader.getIcon(JajukIcons.AUTHOR));
          jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
        } else {
          jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
        }
        add(jlTitle);
        jlIcon.setBorder(new DropShadowBorder(Color.BLACK, 5, 0.5f, 5, false, true, false, true));
        // disable inadequate menu items
        jmiCDDBWizard.setEnabled(false);
        jmiGetCovers.setEnabled(false);
        if (getItem() == null) {
          jmiDelete.setEnabled(false);
          jmiPlay.setEnabled(false);
          jmiPlayRepeat.setEnabled(false);
          jmiPlayShuffle.setEnabled(false);
          jmiPush.setEnabled(false);
          jmiProperties.setEnabled(false);
        }
        // Set URL to open
        jmiOpenLastFMSite.putClientProperty(Const.DETAIL_CONTENT, author.getUrl());
      }

    };
    sw.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getItem()
   */
  @Override
  public Item getItem() {
    org.jajuk.base.Author item = AuthorManager.getInstance().getAuthorByName(author.getName());
    if (item != null) {
      return item;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getDescription()
   */
  @Override
  public String getDescription() {
    Color bgcolor = UtilGUI.getUltraLightColor();
    Color fgcolor = UtilGUI.getForegroundColor();
    String sOut = "<html bgcolor='#" + UtilGUI.getHTMLColor(bgcolor) + "'><TABLE color='"
        + UtilGUI.getHTMLColor(fgcolor) + "'><TR><TD VALIGN='TOP'> <b>" + "<a href='file://"
        + Const.XML_URL + '?' + author.getUrl() + "'>" + author.getName() + "</a>" + "</b><br><br>";
    // display picture
    sOut += "<img src='" + author.getImageUrl() + "'></TD>";
    // Show each album for this Author
    List<AudioScrobblerAlbum> albums = AudioScrobblerService.getInstance().getAlbumList(
        author.getName());
    if (albums != null && albums.size() > 0) {
      sOut += "<TD>";
      for (AudioScrobblerAlbum album : albums) {
        sOut += "<b>";
        if (!UtilString.isVoid(album.getYear())) {
          sOut += album.getYear() + " ";
        }
        sOut += "<a href='file://" + Const.XML_URL + '?' + album.getUrl() + "'>" + album.getTitle()
            + "</a>" + "</b><br>";
      }
    }
    sOut += "</TD></TR></TABLE></html>";
    return sOut;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#launch()
   */
  @Override
  public void launch() {
    if (bKnown) {
      // Play the author
      jmiPlay.doClick();
    } else {
      // Open the last.FM page
      jmiOpenLastFMSite.doClick();
    }
  }

}
