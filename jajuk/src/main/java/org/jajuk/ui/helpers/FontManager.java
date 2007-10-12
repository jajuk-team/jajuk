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

import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;

import java.awt.Font;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.UIManager;

/**
 * Manages Jajuk fonts, stores or update them
 */
public class FontManager implements ITechnicalStrings, Observer {

	public enum JajukFont {
		DEFAULT, PLAIN, PLAIN_S, PLAIN_L, PLAIN_XL, BOLD, BOLD_L, BOLD_XL, BOLD_XXL, BOLD_TITLE, PERSPECTIVES,PLANNED,SEARCHBOX, SPLASH
	}

	private static HashMap<JajukFont, Font> fontCache = new HashMap<JajukFont, Font>(10);

	private static FontManager self;
	
	// No instantiation
	private FontManager() {
		registerFonts();
		ObservationManager.register(this);
	}
	
	public static FontManager getInstance(){
		if (self == null){
			self = new FontManager();
		}
		return self;
	}

	private void registerFonts() {
		// static fonts
		fontCache.put(JajukFont.BOLD_TITLE, new Font("verdana", Font.PLAIN, 20));
		fontCache.put(JajukFont.PERSPECTIVES, new Font("verdana", Font.BOLD, 10));
		fontCache.put(JajukFont.SEARCHBOX, new Font("verdana", Font.BOLD, 18));
		fontCache.put(JajukFont.SPLASH, new Font("verdana", Font.PLAIN, 12));
		// Bold
		fontCache.put(JajukFont.BOLD, new Font("verdana", Font.BOLD, ConfigurationManager
				.getInt(CONF_FONTS_SIZE)));
		fontCache.put(JajukFont.BOLD_L, new Font("verdana", Font.BOLD, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) + 2));
		fontCache.put(JajukFont.BOLD_XL, new Font("verdana", Font.BOLD, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) + 4));
		fontCache.put(JajukFont.BOLD_XXL, new Font("verdana", Font.BOLD, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) + 6));
		// Plain
		fontCache.put(JajukFont.DEFAULT, new Font("verdana", Font.BOLD, ConfigurationManager
				.getInt(CONF_FONTS_SIZE)));
		fontCache.put(JajukFont.PLAIN, new Font("verdana", Font.PLAIN, ConfigurationManager
				.getInt(CONF_FONTS_SIZE)));
		fontCache.put(JajukFont.PLAIN_S, new Font("verdana", Font.PLAIN, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) - 2));
		fontCache.put(JajukFont.PLAIN_L, new Font("verdana", Font.PLAIN, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) + 2));
		fontCache.put(JajukFont.PLAIN_XL, new Font("verdana", Font.PLAIN, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) + 4));
		// Italic
		fontCache.put(JajukFont.PLANNED, new Font("serif", Font.ITALIC, ConfigurationManager
				.getInt(CONF_FONTS_SIZE)));

	}

	public Font getFont(JajukFont font) {
		return fontCache.get(font);
	}

	/**
	 * Sets the default font for all Swing components Thx
	 * http://www.rgagnon.com/javadetails/java-0335.html
	 */
	public static void setDefaultFont() {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, fontCache.get(JajukFont.DEFAULT));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#getRegistrationKeys()
	 */
	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> subjects = new HashSet<EventSubject>(2);
		// Register parameter changes to check new font size
		subjects.add(EventSubject.EVENT_PARAMETERS_CHANGE);
		return subjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
	 */
	public void update(Event event) {
		EventSubject subject = event.getSubject();
		if (EventSubject.EVENT_PARAMETERS_CHANGE.equals(subject)) {
			// force to register again all fonts to get new sizes
			registerFonts();
		}
	}

}
