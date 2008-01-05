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

import com.sun.java.help.impl.SwingWorker;
import com.vlsolutions.swing.docking.ShadowBorder;

import ext.services.lastfm.AudioScrobblerAlbum;
import ext.services.lastfm.AudioScrobblerService;
import ext.services.lastfm.AudioScrobblerTrack;

import java.awt.Color;
import java.awt.Toolkit;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Item;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.VerticalLayout;
import org.jvnet.substance.SubstanceLookAndFeel;

/**
 * Last.FM Album thumb represented as album cover + (optionally) others text
 * information display...
 */
public class AudioScrobblerAlbumThumbnail extends AbstractThumbnail {

  private static final long serialVersionUID = -804471264407148566L;

  /** Associated album */
  AudioScrobblerAlbum album;

  /** Popup thumbnail cache */
  File fThumb;

  /**
   * @param album :
   *          associated album
   */
  public AudioScrobblerAlbumThumbnail(AudioScrobblerAlbum album) {
    super(100);
    this.album = album;
  }

  public void populate() throws Exception {
    jlIcon = new JLabel();

    SwingWorker sw = new SwingWorker() {

      ImageIcon ii;

      @Override
      public Object construct() {
        try {
          // Download thumb
          URL remote = new URL(album.getCoverURL());
          // Download image and store file reference (to generate the
          // popup thumb for ie)
          fCover = DownloadManager.downloadCover(remote, Long.toString(System.currentTimeMillis()));
          fThumb = Util.getConfFileByPath(FILE_CACHE + "/" + System.currentTimeMillis() + '.'
              + Util.getExtension(fCover));
          // Create the image using Toolkit and not ImageIO API to be able to
          // flush all the image data
          ImageIcon downloadedImage = new ImageIcon(Toolkit.getDefaultToolkit().getImage(
              fCover.getAbsolutePath()));
          ii = Util.getScaledImage(downloadedImage, 100);
          // Free images memory
          downloadedImage.getImage().flush();
          ii.getImage().flush();
        } catch (Exception e) {
          Log.error(e);
        }
        return null;
      }

      @Override
      public void finished() {
        super.finished();
        postPopulate();
        jlIcon.setIcon(ii);
        setLayout(new VerticalLayout(2));
        // Use a panel to allow text to be bigger than image under it
        add(Util.getCentredPanel(jlIcon));
        JLabel jlTitle;
        if (AlbumManager.getInstance().getAlbumByName(album.getTitle()) != null) {
          // Album known in collection, display its name in bold
          jlTitle = new JLabel(Util.getLimitedString(album.getTitle(), 15), IconLoader.ICON_ALBUM,
              JLabel.CENTER);
          jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
        } else {
          jlTitle = new JLabel(Util.getLimitedString(album.getTitle(), 15));
          jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
        }
        jlTitle.setToolTipText(album.getTitle());
        add(jlTitle);
        jlIcon.setBorder(new ShadowBorder());
        // disable inadequate menu items
        jmenu.remove(jmiCDDBWizard);
        jmenu.remove(jmiGetCovers);
        if (getItem() == null) {
          jmenu.remove(jmiPlay);
          jmenu.remove(jmiPlayRepeat);
          jmenu.remove(jmiPlayShuffle);
          jmenu.remove(jmiPush);
          jmenu.remove(jmiProperties);
        }
        // Set URL to open
        jmiOpenLastFMSite.putClientProperty(DETAIL_CONTENT, album.getUrl());

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
      AudioScrobblerAlbum album = AudioScrobblerService.getInstance().getAlbum(
          this.album.getArtist(), this.album.getTitle());
      if (album != null) {
        this.album = album;
      }
    }
    Color bgcolor = SubstanceLookAndFeel.getActiveColorScheme().getUltraLightColor();
    Color fgcolor = SubstanceLookAndFeel.getActiveColorScheme().getForegroundColor();
    String sOut = "<html bgcolor='#" + Util.getHTMLColor(bgcolor) + "'><TABLE color='"
        + Util.getHTMLColor(fgcolor) + "'><TR><TD VALIGN='TOP'> <b>" + "<a href='file://" + XML_URL
        + '?' + album.getUrl() + "'>" + album.getTitle() + "</a>" + "</b><br><br>";
    // display cover
    sOut += "<img src='" + album.getCoverURL() + "'><br>";
    // Display author as global value only if it is a single author album
    // We use file://<item type>?<item id> as HTML hyperlink format
    sOut += "<br>" + Messages.getString("Property_author") + " : " + "<a href='file://" + XML_URL
        + '?' + album.getArtistUrl() + "'>" + album.getArtist() + "</a>";
    // Display year if available
    String year = album.getYear();
    if (!Util.isVoid(year)) {
      sOut += "<br>" + Messages.getString("Property_year") + " : " + year;
    }
    sOut += "</TD><TD>";
    // Show each track detail if available
    if (album.getTracks() != null) {
      for (AudioScrobblerTrack track : album.getTracks()) {
        sOut += "<b>" + "<a href='file://" + XML_URL + '?' + track.getUrl() + "'>"
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

}
