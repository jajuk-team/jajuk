/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
package org.jajuk.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Manages locales used in jajuk.
 */
public class LocaleManager {

  /**
   * private constructor for utility class with only static methods.
   */
  private LocaleManager() {
    super();
  }

  /** Supported Locals. */
  private static List<Locale> supportedLocales = Arrays.asList(new Locale[] { Locale.ENGLISH,
      Locale.FRENCH, Locale.GERMAN, new Locale("nl"), new Locale("es"), new Locale("ca"),
      new Locale("ko"), new Locale("el"), new Locale("ru"), new Locale("gl"), new Locale("cs"),
      new Locale("pt") });

  /** Local ( language) to be used, default is English. */
  private static Locale locale = getNativeLocale();

  /**
   * Gets the native locale.
   * 
   * @return current default native locale or English if the native is not
   * supported by Jajuk
   */
  public static Locale getNativeLocale() {
    Locale nativeLocale = new Locale(System.getProperty("user.language"));
    if (supportedLocales.contains(nativeLocale)) {
      return nativeLocale;
    } else { // user language is unknown, take English as a default,
      // user will be able to change it later anyway
      return Locale.ENGLISH;
    }
  }

  /**
   * Change current local.
   * 
   * @param locale to set
   */
  public static void setLocale(final Locale locale) {
    Conf.setProperty(Const.CONF_OPTIONS_LANGUAGE, locale.getLanguage());
    Messages.properties = null; // make sure to reinitialize cached strings
    LocaleManager.locale = locale;
    // Set JVM locale
    Locale.setDefault(locale);
    Messages.bInitialized = true;
  }

  /**
   * Gets the locale.
   * 
   * @return Returns the current locale.
   */
  public static Locale getLocale() {
    return locale;
  }

  /**
   * Return list of available locale descriptions.
   * 
   * @return the locales descs
   */
  public static List<String> getLocalesDescs() {
    final List<String> alDescs = new ArrayList<String>(10);
    for (final Locale loc : supportedLocales) {
      alDescs.add(Messages.getString("Language_desc_" + loc.getLanguage()));
    }
    Collections.sort(alDescs);
    return alDescs;
  }

  /**
   * Return Description for a given local id.
   * 
   * @param sLocal DOCUMENT_ME
   * 
   * @return localized description
   */
  public static String getDescForLocale(final String sLocal) {
    return Messages.getString("Language_desc_" + sLocal);
  }

  /**
   * Return local for a given description.
   * 
   * @param sDesc DOCUMENT_ME
   * 
   * @return local
   */
  public static Locale getLocaleForDesc(final String sDesc) {
    for (final Locale loc : supportedLocales) {
      if (getDescForLocale(loc.getLanguage()).equals(sDesc)) {
        return loc;
      }
    }
    return null;
  }

}
