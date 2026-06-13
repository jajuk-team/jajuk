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
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Item;
import org.jajuk.services.lastfm.LastFmInvalidKeyException;
import org.jajuk.services.lastfm.LastFmService;
import org.jajuk.services.lastfm.model.AlbumInfo;
import org.jajuk.services.lastfm.model.TrackInfo;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.*;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.border.DropShadowBorder;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

/**
 * Last.FM Album thumb represented as album cover + (optionally) others text
 * information display...
 */
public class LastFmAlbumThumbnail extends AbstractThumbnail {
  /** Generated serialVersionUID. */
  @Serial
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
   * @param album : associated album
   */
  public LastFmAlbumThumbnail(AlbumInfo album) {
    super(100);
    this.album = album;
    bKnown = (AlbumManager.getInstance().getAlbumByName(album.getTitle()) != null);
  }

  @Override
  public String getDescription() {
    // Populate album detail with tracks if not already loaded
    if (album.getTracks() == null) {
      try {
        AlbumInfo lAlbum = LastFmService.getInstance().getAlbum(this.album.getArtist(),
                this.album.getTitle());
        if (lAlbum != null) {
          this.album = lAlbum;
        }
      } catch (LastFmInvalidKeyException e) {
        Log.error(e);
      }
    }

    // Cache color values to avoid repeated method calls
    String bgColorHex = UtilGUI.getHTMLColor(UtilGUI.getUltraLightColor());
    String fgColorHex = UtilGUI.getHTMLColor(UtilGUI.getForegroundColor());

    // Wrap content in a container with constrained width so very large images don't blow up the dialog
    StringBuilder html = new StringBuilder();
    html.append("<html>");
    html.append("<body bgcolor='#").append(bgColorHex).append("'>");
    html.append("<div style='width:400px; height:400px; margin:0 auto; border:1px solid #ccc;'>");
    html.append("<table style='color:#").append(fgColorHex).append(";'>");
    html.append("<tr><td style='vertical-align: top;'>");

    // Album title as link
    html.append("<b>").append(createLink(album.getTitle(), album.getUrl())).append("</b>");
    html.append("<br><br>");

    // Album cover (constrained)
    String coverUrl = album.getBigCoverURL();
    if (StringUtils.isNotBlank(coverUrl)) {
      html.append("<img src='")
              .append(coverUrl)
              .append("' width='400'>") // Fixed width
              .append("<br>");
    }

    // Artist information
    html.append("<br>").append(Messages.getHumanPropertyName(Const.XML_ARTIST)).append(" : ");
    html.append(createLink(album.getArtist(), album.getArtistUrl()));

    // Year information (if available)
    String year = album.getYear();
    if (StringUtils.isNotBlank(year)) {
      html.append("<br>").append(Messages.getHumanPropertyName(Const.XML_YEAR)).append(" : ")
              .append(year);
    }

    // Track listing
    html.append("</td><td>");
    if (album.getTracks() != null && !album.getTracks().isEmpty()) {
      for (TrackInfo track : album.getTracks()) {
        html.append("<b>").append(createLink(track.getTitle(), track.getUrl())).append("</b><br>");
      }
    }
    html.append("</td></tr></table>");
    html.append("</div>");
    html.append("</body>");
    html.append("</html>");
    return html.toString();
  }

  @Override
  public Item getItem() {
    return AlbumManager.getInstance().getAlbumByName(album.getTitle());
  }

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
      Image remote = LastFmService.getInstance().getImage(album);
      if (remote == null) {
        // No remote cover available: use bundled no-cover icon sized to the thumbnail
        ii = IconLoader.getNoCoverIcon(100);
        return;
      }
      // Download thumb and create scaled icon
      ii = createScaledIcon(remote, 100);
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

    // Album Icon
    if (ii != null) {
      jlIcon = new JLabel();
      jlIcon.setIcon(ii);
      setLayout(new MigLayout("ins 0,gapy 2"));
      add(jlIcon, "center,wrap");
      jlIcon.setToolTipText(album.getTitle());
      jlIcon.setBorder(new DropShadowBorder(Color.BLACK, 5, 0.5f, 5, false, true, false, true));
    }
    postPopulate();

    // Title
    JLabel jlTitle;
    String fullTitle = album.getTitle();
    // Add year if available
    String releaseDate = album.getReleaseDateString();
    if (StringUtils.isNotBlank(releaseDate)) {
      fullTitle += " (" + releaseDate + ")";
    }
    // Increase default text length and allow wrapping on multiple lines using HTML.
    // We keep a larger limit for artist view.
    int textLength = 30;
    if (isArtistView()) {
      textLength = 100;
    }
    // Use HTML with a fixed width so Swing will wrap the text into multiple lines
    // (not strictly limited to 2 lines but will naturally wrap).
    int labelWidthPx = isArtistView() ? 300 : 140;
    String labelText = "<html><div style='text-align:center;width:" + labelWidthPx + "px;'>"
            + UtilString.getLimitedString(fullTitle, textLength) + "</div></html>";
    if (bKnown) {
      // Album known in collection, display its name in bold with album icon
      // Place the text below the icon and center both to reduce horizontal gap
      jlTitle = new JLabel(labelText, IconLoader.getIcon(JajukIcons.ALBUM), SwingConstants.CENTER);
      jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      jlTitle.setHorizontalTextPosition(SwingConstants.CENTER);
      jlTitle.setVerticalTextPosition(SwingConstants.BOTTOM);
      jlTitle.setIconTextGap(4);
    } else {
      jlTitle = new JLabel(labelText, SwingConstants.CENTER);
      jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
    }
    jlTitle.setToolTipText(album.getTitle());
    add(jlTitle, "center");

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
