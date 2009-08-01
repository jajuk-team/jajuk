/*
 *  QDwizard
 *  Copyright (C) 2009 The QDwizard Team
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
 *  $Revision: 3132 $
 */
package org.qdwizard;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class includes all langpacks strings. Note that we don't use
 * ResourceBundle here to ease translation tasks (properties doesn't support
 * non-latin1 characters) and because QDwizard comes with very few strings
 */
public class Langpack {

  private static List<String> defaults = Arrays.asList("Finish", "Cancel", "Previous", "Next");

  // Strings content : "Finish", "Cancel", "Previous", "Next"
  static private Map<Locale, List<String>> strings = new HashMap<Locale, List<String>>(4);
  static {
    /* replaced strings with the unicode-characters replaced as a workaround to 
     * make building on Hudson work again, currently it failes because the characterset on the 
     * build machine seems to be ASCII
    */

    strings.put(new Locale("ca"), Arrays.asList("Finalitzar", "Cancelar", "Anterior", "Seg\u00fcent"));
    strings.put(new Locale("cs"), Arrays.asList("Dokon\u010dit", "Storno", "P\u0159edchoz\u00ed", "Dal\u0161\u00ed"));
    strings.put(new Locale("de"), Arrays.asList("Fertig", "Abbrechen", "Zur\u00fcck", "Weiter"));
    strings.put(new Locale("en"), defaults);
    strings.put(new Locale("es"), Arrays.asList("Finalizar", "Cancelar", "Anterior", "Siguiente"));
    strings.put(new Locale("el"), Arrays.asList("\u03a4\u03ad\u03bb\u03bf\u03c2", "\u0391\u03ba\u03cd\u03c1\u03c9\u03c3\u03b7", "\u03a0\u03c1\u03bf\u03b7\u03b3\u03bf\u03cd\u03bc\u03b5\u03bd\u03bf", "\u0395\u03c0\u03cc\u03bc\u03b5\u03bd\u03bf"));
    strings.put(new Locale("fr"), Arrays.asList("Termin\u00e9", "Annuler", "Pr\u00e9c\u00e9dent", "Suivant"));
    strings.put(new Locale("gl"), Arrays.asList("Rematar", "Cancelar", "Anterior", "Seguinte"));
    strings.put(new Locale("nl"), Arrays.asList("Afgerond", "Annuleren", "Vooropgaand",
        "Aanstaande"));
    strings.put(new Locale("ru"), Arrays.asList("\u0413\u043e\u0442\u043e\u0432\u043e", "\u041e\u0442\u043c\u0435\u043d\u0430", "\u041d\u0430\u0437\u0430\u0434", "\u0414\u0430\u043b\u044c\u0448\u0435"));
  }

  /** Used locale for the wizard buttons, use English as a default * */
  private static Locale locale = new Locale("en");

  /**
   * private constructor for utility class with only static methods 
   */
  private Langpack() {
    super();
  }

  /**
   * Set the QDwizard locale
   * 
   * @param locale
   */
  public static void setLocale(Locale locale) {
    Langpack.locale = locale;
  }

  /**
   * Return label for given key or null if not matching key is found
   * 
   * @param key
   *          the key as a string using the default locale
   * @return label for given key or null if not matching key is found
   */
  public static String getMessage(String key) {
    List<String> labels = strings.get(locale);
    // If the local is unknown, use default one
    if (labels == null) {
      labels = defaults;
    }
    int index = defaults.indexOf(key);
    return labels.get(index);
  }
}
