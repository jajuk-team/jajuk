/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
package org.jajuk.ui.thumbnails;

import ext.services.lastfm.AlbumInfo;
import ext.services.lastfm.ArtistInfo;
import ext.services.lastfm.LastFmService;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.Item;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 * Last.FM Album thumb represented as artists label + (optionally) others text
 * information display...
 */
public class LastFmArtistThumbnail extends AbstractThumbnail {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -804471264407148566L;
  /** Associated artist. */
  private final ArtistInfo artist;
  /** Is this artist known in collection ?. */
  private final boolean bKnown;
  /** Thumb associated image *. */
  private ImageIcon ii;

  /**
   * The Constructor.
   * 
   * @param artist 
   */
  public LastFmArtistThumbnail(ArtistInfo artist) {
    super(100);
    this.artist = artist;
    bKnown = (ArtistManager.getInstance().getArtistByName(artist.getName()) != null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getItem()
   */
  @Override
  public Item getItem() {
    org.jajuk.base.Artist item = ArtistManager.getInstance().getArtistByName(artist.getName());
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
        + Const.XML_URL + '?' + artist.getUrl() + "'>" + artist.getName() + "</a>" + "</b><br><br>";
    // display picture
    sOut += "<img src='" + artist.getImageUrl() + "'></TD>";
    // Show each album for this Artist
    List<AlbumInfo> albums = LastFmService.getInstance().getAlbumList(artist.getName(), true, 0)
        .getAlbums();
    if (albums != null && albums.size() > 0) {
      sOut += "<TD>";
      for (AlbumInfo album : albums) {
        sOut += "<b>";
        if (!StringUtils.isBlank(album.getYear())) {
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
      // Play the artist
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
      // Check if artist is null
      String artistUrl = artist.getImageUrl();
      if (StringUtils.isBlank(artistUrl)) {
        return;
      }
      // Download thumb
      URL remote = new URL(artistUrl);
      // Download the picture and store file reference (to
      // generate the popup thumb for ie)
      fCover = DownloadManager.downloadToCache(remote);
      if (fCover == null) {
        Log.warn("Could not read remote file: {{" + remote.toString() + "}}");
        return;
      }
      BufferedImage image = ImageIO.read(fCover);
      if (image == null) {
        Log.warn("Could not read image data in file: {{" + fCover + "}}");
        return;
      }
      ImageIcon downloadedImage = new ImageIcon(image);
      // In artist view, do not reduce artist picture
      if (isArtistView()) {
        ii = downloadedImage;
      } else {
        ii = UtilGUI.getScaledImage(downloadedImage, 100);
      }
      // Free images memory
      downloadedImage.getImage().flush();
      image.flush();
    } catch (IIOException e) {
      // report IIOException only as warning here as we can expect this to
      // happen frequently with images on the net
      Log.warn("Could not read image: {{" + artist.getImageUrl() + "}} Cache: {{" + fCover + "}}",
          e.getMessage());
    } catch (UnknownHostException e) {
      Log.warn("Could not contact host for loading images: {{" + e.getMessage() + "}}");
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
    // Use a panel to allow text to be bigger than image under it
    add(jlIcon, "center,wrap");
    int textLength = 15;
    // In artist view, we have plenty of free space
    if (isArtistView()) {
      textLength = 50;
    }
    JLabel jlTitle = new JLabel(UtilString.getLimitedString(artist.getName(), textLength));
    jlTitle.setToolTipText(artist.getName());
    jlIcon.setToolTipText(artist.getName());
    if (bKnown && !isArtistView()) {
      // Artist known in collection, display its name in bold
      jlTitle.setIcon(IconLoader.getIcon(JajukIcons.ARTIST));
      jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    } else {
      jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
    }
    if (isArtistView()) {
      add(jlTitle, "center");
    } else {
      add(jlTitle, "left");
    }
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
      jmiOpenLastFMSite.putClientProperty(Const.DETAIL_CONTENT, artist.getUrl());
    }
  }
}
