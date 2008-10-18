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

package org.jajuk.ui.helpers;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.UIDefaults;
import javax.swing.plaf.FontUIResource;

import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.fonts.FontPolicy;
import org.jvnet.substance.fonts.FontSet;

/**
 * Manages Jajuk fonts, stores or update them
 */
public final class FontManager implements Observer {

  public enum JajukFont {
    DEFAULT, PLAIN, PLAIN_S, PLAIN_L, PLAIN_XL, BOLD, BOLD_L, BOLD_XL, BOLD_XXL, BOLD_TITLE, PERSPECTIVES, PLANNED, SEARCHBOX, SPLASH, SPLASH_PROGRESS, VIEW_FONT
  }

  private static Map<JajukFont, Font> fontCache = new HashMap<JajukFont, Font>(10);

  private static FontManager self;

  private static final JLabel JL = new JLabel();

  // No instantiation
  private FontManager() {
    registerFonts();
    ObservationManager.register(this);
  }

  public static FontManager getInstance() {
    if (self == null) {
      self = new FontManager();
    }
    return self;
  }

  private void registerFonts() {
    // static fonts
    fontCache.put(JajukFont.BOLD_TITLE, new Font("verdana", Font.PLAIN, 20));
    fontCache.put(JajukFont.PERSPECTIVES, new Font("verdana", Font.BOLD, 9));
    fontCache.put(JajukFont.SEARCHBOX, new Font("verdana", Font.BOLD, 12));
    fontCache.put(JajukFont.SPLASH, new Font("verdana", Font.PLAIN, 12));
    fontCache.put(JajukFont.SPLASH_PROGRESS, new Font("verdana", Font.BOLD, 12));
    // Bold
    fontCache.put(JajukFont.BOLD,
        new Font("verdana", Font.BOLD, Conf.getInt(Const.CONF_FONTS_SIZE)));
    fontCache.put(JajukFont.BOLD_L, new Font("verdana", Font.BOLD, Conf
        .getInt(Const.CONF_FONTS_SIZE) + 2));
    fontCache.put(JajukFont.BOLD_XL, new Font("verdana", Font.BOLD, Conf
        .getInt(Const.CONF_FONTS_SIZE) + 4));
    fontCache.put(JajukFont.BOLD_XXL, new Font("verdana", Font.BOLD, Conf
        .getInt(Const.CONF_FONTS_SIZE) + 6));
    // Plain
    fontCache.put(JajukFont.DEFAULT, new Font("verdana", Font.PLAIN, Conf
        .getInt(Const.CONF_FONTS_SIZE)));
    fontCache.put(JajukFont.PLAIN, new Font("verdana", Font.PLAIN, Conf
        .getInt(Const.CONF_FONTS_SIZE)));
    fontCache.put(JajukFont.PLAIN_S, new Font("verdana", Font.PLAIN, Conf
        .getInt(Const.CONF_FONTS_SIZE) - 2));
    fontCache.put(JajukFont.PLAIN_L, new Font("verdana", Font.PLAIN, Conf
        .getInt(Const.CONF_FONTS_SIZE) + 2));
    fontCache.put(JajukFont.PLAIN_XL, new Font("verdana", Font.PLAIN, Conf
        .getInt(Const.CONF_FONTS_SIZE) + 4));
    // Italic
    fontCache.put(JajukFont.PLANNED, new Font("serif", Font.ITALIC, Conf
        .getInt(Const.CONF_FONTS_SIZE)));
    Font font = new Font("verdana", Font.PLAIN, Conf.getInt(Const.CONF_FONTS_SIZE));
    fontCache.put(JajukFont.VIEW_FONT, font);
  }

  public Font getFont(JajukFont font) {
    return fontCache.get(font);
  }

  public void setDefaultFont() {
    // Create the wrapper font set
    FontPolicy newFontPolicy = new FontPolicy() {
      public FontSet getFontSet(String lafName, UIDefaults table) {
        return new CustomFontSet(fontCache.get(JajukFont.DEFAULT));
      }
    };
    try {
      // set the new font policy
      SubstanceLookAndFeel.setFontPolicy(newFontPolicy);
    } catch (Exception exc) {
      Log.error(exc);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> subjects = new HashSet<JajukEvents>(2);
    // Register parameter changes to check new font size
    subjects.add(JajukEvents.PARAMETERS_CHANGE);
    return subjects;
  }

  /**
   * 
   * This method return the number of characters of a given string that fits in
   * the given size in pixels
   * 
   * @param text
   * @param font
   * @param maxSize
   * @return
   */
  public static int getRowsForText(String text, Font font, int maxSize) {
    int resu = 0;
    int usedSize = 0;
    FontMetrics fm = JL.getFontMetrics(font);
    for (int i = 0; i < text.length() - 1; i++) {
      usedSize = fm.stringWidth(text.substring(0, i));
      resu++;
      if (usedSize >= maxSize) {
        break;
      }
    }
    return resu;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
   */
  public void update(Event event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
      // force to register again all fonts to get new sizes
      registerFonts();
    }
  }

  private static class CustomFontSet implements FontSet {
    protected FontUIResource font;

    public CustomFontSet(Font font) {
      this.font = new FontUIResource(font);
    }

    public FontUIResource getControlFont() {
      return this.font;
    }

    public FontUIResource getMenuFont() {
      return this.font;
    }

    public FontUIResource getMessageFont() {
      return this.font;
    }

    public FontUIResource getSmallFont() {
      return this.font;
    }

    public FontUIResource getTitleFont() {
      return this.font;
    }

    public FontUIResource getWindowTitleFont() {
      return this.font;
    }
  }
}
