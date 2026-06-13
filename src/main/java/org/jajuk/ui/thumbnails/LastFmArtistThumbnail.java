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

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.Item;
import org.jajuk.services.lastfm.LastFmService;
import org.jajuk.services.lastfm.model.AlbumInfo;
import org.jajuk.services.lastfm.model.ArtistInfo;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.*;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.border.DropShadowBorder;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.List;

/**
 * Last.FM Album thumb represented as artists label + (optionally) others text
 * information display...
 */
public class LastFmArtistThumbnail extends AbstractThumbnail {
  /** Generated serialVersionUID. */
  @Serial
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
   * @param artist artist to display
   */
  public LastFmArtistThumbnail(ArtistInfo artist) {
    super(100);
    this.artist = artist;
    bKnown = (ArtistManager.getInstance().getArtistByName(artist.getName()) != null);
  }

  @Override
  public Item getItem() {
    return ArtistManager.getInstance().getArtistByName(artist.getName());
  }

  @Override
  public String getDescription() {
    // Cache color values to avoid repeated method calls
    String bgColorHex = UtilGUI.getHTMLColor(UtilGUI.getUltraLightColor());
    String fgColorHex = UtilGUI.getHTMLColor(UtilGUI.getForegroundColor());

    StringBuilder html = new StringBuilder();
    // Constrain popup width so very large artist images don't create huge dialogs
    html.append("<html>");
    html.append("<body bgcolor='#").append(bgColorHex).append("'>");
    //    html.append("<div style='min-width:250px;max-width:400px;'>");
    html.append("<div style='width:400px; height:400px; margin:0 auto; border:1px solid #ccc;'>");

    html.append("<table style='color:#").append(fgColorHex).append(";'>");
    html.append("<tr><td style='vertical-align: top;'><b>");
    html.append(createLink(artist.getName(), artist.getUrl()));
    html.append("</b><br><br>");

    // display picture -- prefer local cached file if available (preLoad())
    String imageUrl;
    if (fCover != null && fCover.exists()) {
      try {
        imageUrl = fCover.toURI().toURL().toString();
      } catch (Exception e) {
        // fallback to remote url
        imageUrl = artist.getImageUrl();
      }
    } else {
      imageUrl = artist.getImageUrl();
    }
    if (!StringUtils.isBlank(imageUrl)) {
      // do not escape the URL attribute value (some characters must remain as-is)
      // constrain image size to avoid overly large dialogs
      html.append("<img src='")
              .append(imageUrl)
              .append("' width='400'>"); // Fixed width
    }
    html.append("</td>");

    // Show each album for this Artist
    try {
      List<AlbumInfo> albums = LastFmService.getInstance()
              .getAlbumList(artist.getName(), true, 0).getAlbums();
      if (albums != null && !albums.isEmpty()) {
        html.append("<td>");
        for (AlbumInfo album : albums) {
          html.append("<b>");
          if (!StringUtils.isBlank(album.getYear())) {
            html.append(escapeHtml(album.getYear())).append(" ");
          }
          html.append(createLink(album.getTitle(), album.getUrl()));
          html.append("</b><br>");
        }
        html.append("</td>");
      }
    } catch (Exception e) {
      Log.warn("Error retrieving albums for artist: {{" + artist.getName() + "}}", e.getMessage());
    }
    html.append("</tr></table>");
    html.append("</div>");
    html.append("</body>");
    html.append("</html>");
    return html.toString();
  }

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
      // Check if artist url is null
      Image remote = LastFmService.getInstance().getImage(artist);
      if (remote == null) {
        return;
      }
      // Download thumb and create scaled icon
      int targetSize = isArtistView() ? 200 : 100;
      ii = createScaledIcon(remote, targetSize);
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
