/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

package org.jajuk.ui.thumbnails;

import ext.services.lastfm.AlbumInfo;
import ext.services.lastfm.LastFmService;
import ext.services.lastfm.TrackInfo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Item;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 * Last.FM Album thumb represented as album cover + (optionally) others text
 * information display...
 */
public class LastFmAlbumThumbnail extends AbstractThumbnail {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -804471264407148566L;

  /** Associated album. */
  private AlbumInfo album;

  /** Is this artist known in collection ?. */
  private final boolean bKnown;

  /** Thumb associated image *. */
  private ImageIcon ii;

  /**
   * The Constructor.
   * 
   * @param album :
   * associated album
   */
  public LastFmAlbumThumbnail(AlbumInfo album) {
    super(100);
    this.album = album;
    bKnown = (AlbumManager.getInstance().getAlbumByName(album.getTitle()) != null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getItem()
   */
  @Override
  public Item getItem() {
    Album item = AlbumManager.getInstance().getAlbumByName(album.getTitle());
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
    // populate album detail
    if (album.getTracks() == null) {
      AlbumInfo lAlbum = LastFmService.getInstance().getAlbum(this.album.getArtist(),
          this.album.getTitle());
      if (lAlbum != null) {
        this.album = lAlbum;
      }
    }
    Color bgcolor = UtilGUI.getUltraLightColor();
    Color fgcolor = UtilGUI.getForegroundColor();
    String sOut = "<html bgcolor='#" + UtilGUI.getHTMLColor(bgcolor) + "'><TABLE color='"
        + UtilGUI.getHTMLColor(fgcolor) + "'><TR><TD VALIGN='TOP'> <b>" + "<a href='file://"
        + Const.XML_URL + '?' + album.getUrl() + "'>" + album.getTitle() + "</a>" + "</b><br><br>";
    // display cover
    sOut += "<img src='" + album.getBigCoverURL() + "'><br>";
    // Display artist as global value only if it is a single artist album
    // We use file://<item type>?<item id> as HTML hyperlink format
    sOut += "<br>" + Messages.getHumanPropertyName(Const.XML_ARTIST) + " : " + "<a href='file://"
        + Const.XML_URL + '?' + album.getArtistUrl() + "'>" + album.getArtist() + "</a>";
    // Display year if available
    String year = album.getYear();
    if (!StringUtils.isBlank(year)) {
      sOut += "<br>" + Messages.getHumanPropertyName(Const.XML_YEAR) + " : " + year;
    }
    sOut += "</TD><TD>";
    // Show each track detail if available
    if (album.getTracks() != null) {
      for (TrackInfo track : album.getTracks()) {
        sOut += "<b>" + "<a href='file://" + Const.XML_URL + '?' + track.getUrl() + "'>"
            + track.getTitle() + "</a></b><br>";
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
    if (getItem() != null) {
      // play the album
      jmiPlay.doClick();
    } else {
      // Open the last.FM page
      jmiOpenLastFMSite.doClick();
    }
  }

  /**
   * Long part of the populating process. Longest parts (images download) should
   * have already been done by the caller outside the EDT. we only pop the image
   * from the cache here.
   */
  private void preLoad() {
    try {
      // Check if album image is null
      String albumUrl = album.getBigCoverURL();
      if (StringUtils.isBlank(albumUrl)) {
        return;
      }
      // Download thumb
      URL remote = new URL(albumUrl);
      // Download image and store file reference (to generate the
      // popup thumb for ie)
      fCover = DownloadManager.downloadToCache(remote);
      BufferedImage image = ImageIO.read(fCover);
      if (image == null) {
        Log.warn("Could not read cover from: {{" + fCover.getAbsolutePath() + "}}");
        return;
      }
      ImageIcon downloadedImage = new ImageIcon(image);
      ii = UtilGUI.getScaledImage(downloadedImage, 100);
      // Free images memory
      downloadedImage.getImage().flush();
      image.flush();
    } catch (FileNotFoundException e) {
      // only report a warning for FileNotFoundException and do not show a
      // stack trace in the logfile as it is happening frequently
      Log.warn("Could not load image, no content found at address: {{" + e.getMessage() + "}}");
    } catch (SocketTimeoutException e) {
      // only report a warning for FileNotFoundException and do not show a
      // stacktrace in the logfile as it is happening frequently
      Log.warn("Could not load image, timed out while reading address: {{" + e.getMessage() + "}}");
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Thumb populating done in EDT.
   */
  @Override
  public void populate() {
    preLoad();
    if (ii == null) {
      return;
    }
    jlIcon = new JLabel();
    postPopulate();
    jlIcon.setIcon(ii);
    setLayout(new MigLayout("ins 0,gapy 2"));
    add(jlIcon, "center,wrap");
    JLabel jlTitle;
    String fullTitle = album.getTitle();
    // Add year if available
    String releaseDate = album.getReleaseDateString();
    if (StringUtils.isNotBlank(releaseDate)) {
      fullTitle += " (" + releaseDate + ")";
    }
    int textLength = 15;
    if (isArtistView()) {
      textLength = 50;
    }
    if (bKnown) {
      // Album known in collection, display its name in bold
      jlTitle = new JLabel(UtilString.getLimitedString(fullTitle, textLength), IconLoader
          .getIcon(JajukIcons.ALBUM), SwingConstants.CENTER);
      jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    } else {
      jlTitle = new JLabel(UtilString.getLimitedString(fullTitle, textLength));
      jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
    }
    jlTitle.setToolTipText(album.getTitle());
    jlIcon.setToolTipText(album.getTitle());
    add(jlTitle, "center");
    jlIcon.setBorder(new DropShadowBorder(Color.BLACK, 5, 0.5f, 5, false, true, false, true));
    // disable inadequate menu items
    jmiCDDBWizard.setEnabled(false);
    jmiGetCovers.setEnabled(false);
    if (getItem() == null) {
      jmiDelete.setEnabled(false);
      jmiPlay.setEnabled(false);
      jmiPlayRepeat.setEnabled(false);
      jmiPlayShuffle.setEnabled(false);
      jmiFrontPush.setEnabled(false);
      jmiPush.setEnabled(false);
      jmiProperties.setEnabled(false);
    }
    // Set URL to open
    if (UtilSystem.isBrowserSupported()) {
      jmiOpenLastFMSite.putClientProperty(Const.DETAIL_CONTENT, album.getUrl());
    }

  }

}
