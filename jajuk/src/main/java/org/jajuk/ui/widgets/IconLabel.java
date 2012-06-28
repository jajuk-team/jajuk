/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.ui.widgets;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Encapsulates a label with a text and an icon, used for tables.
 */
public class IconLabel extends ImageIcon implements Comparable<IconLabel> {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 3794262035337113611L;
  /** Text. */
  private final String sText;
  /** Background color. */
  private Color cBackground;
  /** Foreground color. */
  private Color cForeground;
  /** Font. */
  private Font font;
  /** Tooltip. */
  private String sTooltip;
  private static Map<JajukIcons, IconLabel> cachedIcons = new HashMap<JajukIcons, IconLabel>();

  /**
   * Constructor.
   * 
   * @param icon 
   * @param sText 
   * @param cBackground 
   * @param cForeground 
   * @param font 
   * @param sTooltip 
   */
  public IconLabel(ImageIcon icon, String sText, Color cBackground, Color cForeground, Font font,
      String sTooltip) {
    super(icon.getImage());
    this.sText = sText;
    this.cBackground = cBackground;
    this.cForeground = cForeground;
    this.font = font;
    this.sTooltip = sTooltip;
  }

  /**
   * Instantiates a new icon label.
   * 
   * @param icon 
   * @param sText 
   */
  private IconLabel(ImageIcon icon, String sText) {
    super(icon.getImage());
    this.sText = sText;
  }

  /**
   * Gets the text.
   * 
   * @return Returns the sText.
   */
  public String getText() {
    return sText;
  }

  /**
   * Gets the background.
   * 
   * @return Returns the cBackground.
   */
  public Color getBackground() {
    return cBackground;
  }

  /**
   * Gets the foreground.
   * 
   * @return Returns the cForeground.
   */
  public Color getForeground() {
    return cForeground;
  }

  /**
   * Gets the font.
   * 
   * @return Returns the font.
   */
  public Font getFont() {
    return font;
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return sText;
  }

  /**
   * Gets the tooltip.
   * 
   * @return Returns the sTooltip.
   */
  public String getTooltip() {
    return sTooltip;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(T)
   */
  @Override
  public int compareTo(IconLabel ilOther) {
    // if no integer value then simply compare tooltip strings
    if (ilOther.getTooltip() != null && this.getTooltip() != null) {
      return ilOther.getTooltip().compareTo(getTooltip());
    } else {
      return 0;
    }
  }

  /**
   * Gets an IconLabel from a cache for memory saving reasons.
   * Note that this doesn't handle StarIconLabel that owns its own cache.
   * 
   * @param icon 
   * 
   * @return the icon
   */
  public static IconLabel getIconLabel(JajukIcons icon) {
    if (icon == JajukIcons.TRACK_FIFO_PLANNED) {
      if (!cachedIcons.containsKey(icon)) {
        cachedIcons.put(
            icon,
            new IconLabel(IconLoader.getIcon(JajukIcons.TRACK_FIFO_PLANNED), "", null, null,
                FontManager.getInstance().getFont(JajukFont.PLANNED), Messages
                    .getString("AbstractPlaylistEditorView.20")));
      }
    } else if (icon == JajukIcons.TRACK_FIFO_REPEAT) {
      if (!cachedIcons.containsKey(icon)) {
        cachedIcons.put(icon, new IconLabel(IconLoader.getIcon(icon), "", null, null, null,
            Messages.getString("AbstractPlaylistEditorView.19")));
      }
    } else if (icon == JajukIcons.TRACK_FIFO_PLAYING) {
      if (!cachedIcons.containsKey(icon)) {
        cachedIcons.put(icon, new IconLabel(IconLoader.getIcon(icon), "", null, null, null,
            Messages.getString("AbstractPlaylistEditorView.29")));
      }
    } else if (icon == JajukIcons.TRACK_FIFO_NORM) {
      if (!cachedIcons.containsKey(icon)) {
        cachedIcons.put(icon, new IconLabel(IconLoader.getIcon(icon), "", null, null, null,
            Messages.getString("AbstractPlaylistEditorView.18")));
      }
    } else {
      Log.warn("Unsupported icon requested in IconLabel.getIcon(): " + icon.toString());
      return null;
    }
    return cachedIcons.get(icon);
  }
}
