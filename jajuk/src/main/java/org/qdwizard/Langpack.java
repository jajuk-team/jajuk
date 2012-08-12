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
  /** static set of strings, can contain non-ISO8859 chars * */
  static {
    strings.put(new Locale("ca"), Arrays.asList("Finalitzar", "Cancelar", "Anterior", "Següent"));
    strings.put(new Locale("cs"), Arrays.asList("Dokončit", "Storno", "Předchozí", "Další"));
    strings.put(new Locale("de"), Arrays.asList("Fertig", "Abbrechen", "Zurück", "Weiter"));
    strings.put(new Locale("en"), defaults);
    strings.put(new Locale("es"), Arrays.asList("Finalizar", "Cancelar", "Anterior", "Siguiente"));
    strings.put(new Locale("el"), Arrays.asList("Τέλος", "Ακύρωση", "Προηγούμενο", "Επόμενο"));
    strings.put(new Locale("fr"), Arrays.asList("Terminé", "Annuler", "Précédent", "Suivant"));
    strings.put(new Locale("gl"), Arrays.asList("Rematar", "Cancelar", "Anterior", "Seguinte"));
    strings.put(new Locale("nl"),
        Arrays.asList("Afgerond", "Annuleren", "Vooropgaand", "Aanstaande"));
    strings.put(new Locale("ru"), Arrays.asList("Готово", "Отмена", "Назад", "Дальше"));
    strings.put(new Locale("pt"), Arrays.asList("Terminar", "Cancelar", "Anterior", "Seguinte"));
  }
  /** Used locale for the wizard buttons, use English as a default *. */
  private static Locale locale = new Locale("en");

  /**
   * private constructor for utility class with only static methods.
   */
  private Langpack() {
    super();
  }

  /**
   * Set the QDwizard locale.
   * 
   * @param locale 
   */
  public static void setLocale(Locale locale) {
    Langpack.locale = locale;
  }

  /**
   * Return label for given key or null if not matching key is found.
   * 
   * @param key the key as a string using the default locale
   * 
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
