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
package org.jajuk.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.GenreManager;
import org.jajuk.base.ItemManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.util.error.JajukException;

/**
 * Set of convenient classes for string manipulation.
 */
public final class UtilString {
  /** The list of characters that we need to escape in strings. */
  private final static String ESCAPE_CHARACTERS = "\\[](){}.*+?$^|-";
  private static final ThreadLocal<DateFormat> dateFormat = new ThreadLocal<DateFormat>();
  /** Constant date FORMATTER, one by thread for perfs, we need an instance by thread because this class is not thread safe. */
  private static final ThreadLocal<SimpleDateFormat> FORMATTER = new ThreadLocal<SimpleDateFormat>() {
    @Override
    protected SimpleDateFormat initialValue() {
      return new SimpleDateFormat(Const.ADDITION_DATE_FORMAT, Locale.getDefault());
    }
  };

  /**
   * private constructor to avoid instantiating utility class.
   */
  private UtilString() {
  }

  /**
   * Apply the Album pattern.
   *
   * @param file file to apply pattern to
   * @param sPattern 
   * @param bMandatory are all needed tags mandatory ?
   * @param normalize 
   * @param out 
   * @param track 
   *
   * @return the string
   *
   * @throws JajukException the jajuk exception
   */
  private static String applyAlbumPattern(final org.jajuk.base.File file, final String sPattern,
      final boolean bMandatory, final boolean normalize, final String out, final Track track)
      throws JajukException {
    String ret = out;
    String sValue;
    if (sPattern.contains(Const.PATTERN_ALBUM)) {
      sValue = track.getAlbum().getName();
      if (normalize) {
        sValue = UtilSystem.getNormalizedFilename(sValue);
      }
      if (!sValue.equals(Const.UNKNOWN_ALBUM)) {
        ret = ret.replace(Const.PATTERN_ALBUM, AlbumManager.format(sValue));
      } else {
        if (bMandatory) {
          throw new JajukException(149, file.getAbsolutePath());
        } else {
          ret = ret.replace(Const.PATTERN_ALBUM, Messages.getString(Const.UNKNOWN_ALBUM));
        }
      }
    }
    return ret;
  }

  /**
   * Apply the Year pattern.
   *
   * @param file file to apply pattern to
   * @param sPattern 
   * @param bMandatory are all needed tags mandatory ?
   * @param out 
   * @param track 
   *
   * @return the string
   *
   * @throws JajukException the jajuk exception
   */
  private static String applyYearPattern(final org.jajuk.base.File file, final String sPattern,
      final boolean bMandatory, final String out, final Track track) throws JajukException {
    String ret = out;
    if (sPattern.contains(Const.PATTERN_YEAR)) {
      if (track.getYear().getValue() != 0) {
        ret = ret.replace(Const.PATTERN_YEAR, track.getYear().getValue() + "");
      } else {
        if (bMandatory) {
          throw new JajukException(148, file.getAbsolutePath());
        } else {
          ret = ret.replace(Const.PATTERN_YEAR, "?");
        }
      }
    }
    return ret;
  }

  /**
   * Apply the Track pattern.
   *
   * @param sPattern 
   * @param normalize 
   * @param out 
   * @param track 
   *
   * @return the string
   */
  private static String applyTrackPattern(final String sPattern, final boolean normalize,
      final String out, final Track track) {
    String ret = out;
    String sValue;
    if (sPattern.contains(Const.PATTERN_TRACKNAME)) {
      sValue = track.getName();
      if (normalize) {
        sValue = UtilSystem.getNormalizedFilename(sValue);
      }
      ret = ret.replace(Const.PATTERN_TRACKNAME, sValue);
    }
    return ret;
  }

  /**
   * Apply the Track Order pattern.
   *
   * @param file file to apply pattern to
   * @param sPattern 
   * @param bMandatory are all needed tags mandatory ?
   * @param out 
   * @param track 
   *
   * @return the string
   *
   * @throws JajukException the jajuk exception
   */
  private static String applyTrackOrderPattern(final org.jajuk.base.File file,
      final String sPattern, final boolean bMandatory, final String out, final Track track)
      throws JajukException {
    if (sPattern.contains(Const.PATTERN_TRACKORDER)) {
      // override Order from filename if not set explicitly
      long lOrder = handleOrder(file, bMandatory, track);
      // prepend one digit numbers with "0"
      if (lOrder < 10) {
        return out.replace(Const.PATTERN_TRACKORDER, "0" + lOrder);
      } else {
        return out.replace(Const.PATTERN_TRACKORDER, lOrder + "");
      }
    }
    return out;
  }

  /**
   * Handle order.
   * 
   *
   * @param file 
   * @param bMandatory 
   * @param track 
   *
   * @return the long
   *
   * @throws JajukException the jajuk exception
   */
  private static long handleOrder(final org.jajuk.base.File file, final boolean bMandatory,
      final Track track) throws JajukException {
    long lOrder = track.getOrder();
    if (lOrder == 0) {
      final String sFilename = file.getName();
      if (Character.isDigit(sFilename.charAt(0))) {
        final String sTo = sFilename.substring(0, 3).trim().replaceAll("[^0-9]", "");
        for (final char c : sTo.toCharArray()) {
          if (!Character.isDigit(c)) {
            throw new JajukException(152, file.getAbsolutePath());
          }
        }
        lOrder = Long.parseLong(sTo);
      } else {
        if (bMandatory) {
          throw new JajukException(152, file.getAbsolutePath());
        } else {
          lOrder = 0;
        }
      }
    }
    return lOrder;
  }

  /**
   * Apply the Genre pattern.
   *
   * @param file file to apply pattern to
   * @param sPattern 
   * @param bMandatory are all needed tags mandatory ?
   * @param normalize 
   * @param out 
   * @param track 
   *
   * @return the string
   *
   * @throws JajukException the jajuk exception
   */
  private static String applyGenrePattern(final org.jajuk.base.File file, final String sPattern,
      final boolean bMandatory, final boolean normalize, final String out, final Track track)
      throws JajukException {
    String ret = out;
    String sValue;
    if (sPattern.contains(Const.PATTERN_GENRE)) {
      sValue = track.getGenre().getName();
      if (normalize) {
        sValue = UtilSystem.getNormalizedFilename(sValue);
      }
      if (!sValue.equals(Const.UNKNOWN_GENRE)) {
        ret = ret.replace(Const.PATTERN_GENRE, GenreManager.format(sValue));
      } else {
        if (bMandatory) {
          throw new JajukException(153, file.getAbsolutePath());
        } else {
          ret = ret.replace(Const.PATTERN_GENRE, Messages.getString(Const.UNKNOWN_GENRE));
        }
      }
    }
    return ret;
  }

  /**
   * Apply the Artist pattern.
   *
   * @param file file to apply pattern to
   * @param sPattern 
   * @param bMandatory are all needed tags mandatory ?
   * @param normalize 
   * @param out 
   * @param track 
   *
   * @return the string
   *
   * @throws JajukException the jajuk exception
   */
  private static String applyArtistPattern(final org.jajuk.base.File file, final String sPattern,
      final boolean bMandatory, final boolean normalize, final String out, final Track track)
      throws JajukException {
    String ret = out;
    String sValue;
    if (sPattern.contains(Const.PATTERN_ARTIST)) {
      sValue = track.getArtist().getName();
      if (normalize) {
        sValue = UtilSystem.getNormalizedFilename(sValue);
      }
      if (!sValue.equals(Const.UNKNOWN_ARTIST)) {
        ret = ret.replaceAll(Const.PATTERN_ARTIST, ItemManager.format(sValue));
      } else {
        if (bMandatory) {
          throw new JajukException(150, file.getAbsolutePath());
        } else {
          ret = ret.replaceAll(Const.PATTERN_ARTIST, Messages.getString(Const.UNKNOWN_ARTIST));
        }
      }
    }
    return ret;
  }

  /**
   * Apply a pattern. This replaces certain patterns in the provided Pattern
   * with information from the file and returns the result.
   *
   * @param file file to apply pattern to
   * @param sPattern 
   * @param bMandatory are all needed tags mandatory ?
   * @param normalize Remove characters non compatible with filenames in fil systems
   *
   * @return computed string
   * make sure the created string can be used as file name on target
   * file system
   *
   * @throws JajukException if some tags are missing
   */
  public static String applyPattern(final org.jajuk.base.File file, final String sPattern,
      final boolean bMandatory, final boolean normalize) throws JajukException {
    String out = sPattern;
    final Track track = file.getTrack();
    // Check Artist name
    out = UtilString.applyArtistPattern(file, sPattern, bMandatory, normalize, out, track);
    // Check Album artist, use artist name if no album artist
    out = UtilString.applyAlbumArtistPattern(sPattern, normalize, out, track);
    // Check Genre name
    out = UtilString.applyGenrePattern(file, sPattern, bMandatory, normalize, out, track);
    // Check Album Name
    out = UtilString.applyAlbumPattern(file, sPattern, bMandatory, normalize, out, track);
    // Check Track Order
    out = UtilString.applyTrackOrderPattern(file, sPattern, bMandatory, out, track);
    // Check Track name
    out = UtilString.applyTrackPattern(sPattern, normalize, out, track);
    // Check Year Value
    out = UtilString.applyYearPattern(file, sPattern, bMandatory, out, track);
    // Check Disc Value
    out = UtilString.applyDiscPattern(file, sPattern, bMandatory, out, track);
    // Check Custom Properties
    out = UtilString.applyCustomPattern(file, sPattern, normalize, out, track);
    return out;
  }

  /**
   * Apply Custom property pattern.
   *
   * @param sPattern 
   * @param normalize 
   * @param out 
   * @param track 
   * @return the string
   */
  private static String applyCustomPattern(final org.jajuk.base.File file, String sPattern,
      boolean normalize, String out, Track track) {
    String ret = out;
    String sValue;
    //Merge files and tracks properties. file wins in they both contain a custom property with the same name.
    Map<String, Object> properties = track.getProperties();
    properties.putAll(file.getProperties());
    Collection<PropertyMetaInformation> customProperties = FileManager.getInstance()
        .getCustomProperties();
    customProperties.addAll(TrackManager.getInstance().getCustomProperties());
    Iterator<PropertyMetaInformation> it2 = customProperties.iterator();
    while (it2.hasNext()) {
      PropertyMetaInformation meta = it2.next();
      if (sPattern.contains("%" + meta.getName())) {
        Object o = properties.get(meta.getName());
        if (o != null) {
          sValue = o.toString();
        } else {
          sValue = meta.getDefaultValue().toString();
        }
        if (normalize) {
          sValue = UtilSystem.getNormalizedFilename(sValue);
        }
        ret = ret.replaceAll("%" + meta.getName(), sValue);
      }
    }
    return ret;
  }

  /**
   * Apply album artist pattern.
   *
   * @param sPattern 
   * @param normalize 
   * @param out 
   * @param track 
   * @return the string
   */
  private static String applyAlbumArtistPattern(String sPattern, boolean normalize, String out,
      Track track) {
    String ret = out;
    String sValue;
    if (sPattern.contains(Const.PATTERN_ALBUM_ARTIST)) {
      sValue = track.getAlbumArtistOrArtist();
      if (normalize) {
        sValue = UtilSystem.getNormalizedFilename(sValue);
      }
      ret = ret.replaceAll(Const.PATTERN_ALBUM_ARTIST, ItemManager.format(sValue));
    }
    return ret;
  }

  /**
   * Apply disc pattern.
   *
   * @param file 
   * @param sPattern 
   * @param bMandatory 
   * @param out 
   * @param track 
   * @return the string
   * @throws JajukException the jajuk exception
   */
  private static String applyDiscPattern(File file, String sPattern, boolean bMandatory,
      String out, Track track) throws JajukException {
    if (sPattern.contains(Const.PATTERN_DISC)) {
      // override Order from filename if not set explicitly
      long lDiscNumber = handleDiscNumber(file, bMandatory, track);
      // prepend one digit numbers with "0"
      if (lDiscNumber < 10) {
        return out.replace(Const.PATTERN_DISC, "0" + lDiscNumber);
      } else {
        return out.replace(Const.PATTERN_DISC, lDiscNumber + "");
      }
    }
    return out;
  }

  /**
   * Handle disc number.
   * 
   *
   * @param file 
   * @param bMandatory 
   * @param track 
   *
   * @return the long
   *
   * @throws JajukException the jajuk exception
   */
  private static long handleDiscNumber(File file, boolean bMandatory, Track track)
      throws JajukException {
    long lDiscNumber = track.getDiscNumber();
    if (lDiscNumber == 0) {
      final String sFilename = file.getName();
      if (Character.isDigit(sFilename.charAt(0))) {
        final String sTo = sFilename.substring(0, 3).trim().replaceAll("[^0-9]", "");
        for (final char c : sTo.toCharArray()) {
          if (!Character.isDigit(c)) {
            throw new JajukException(152, file.getAbsolutePath());
          }
        }
        lDiscNumber = Long.parseLong(sTo);
      } else {
        if (bMandatory) {
          throw new JajukException(152, file.getAbsolutePath());
        } else {
          lDiscNumber = 0;
        }
      }
    }
    return lDiscNumber;
  }

  /**
   * Contains non digit or letters.
   *
   * @param s String to analyse
   *
   * @return whether the given string contains non digit or letter characters
   */
  public static boolean containsNonDigitOrLetters(final String s) {
    boolean bOK = false;
    for (int i = 0; i < s.length(); i++) {
      if (!Character.isLetterOrDigit(s.charAt(i))) {
        bOK = true;
        break;
      }
    }
    return bOK;
  }

  /**
   * Encode URLS.
   *
   * @param s 
   *
   * @return the string
   */
  public static String encodeURL(final String s) {
    return s.replaceAll(" +", "+");
  }

  /*
   * Escape (in the regexp sense) a string Source Search reserved: $ ( ) * + - . ? [ \ ] ^ { | }
   * http://mindprod.com/jgloss/regex.html
   */
  /**
   * Escape string.
   * 
   *
   * @param s 
   *
   * @return the string
   */
  public static String escapeString(String s) {
    int length = s.length();
    StringBuilder buffer = new StringBuilder(2 * length);
    for (int i = 0; i != length; i++) {
      char c = s.charAt(i);
      // if we have a character that needs to be escaped, we prepend backslash
      // before it
      if (ESCAPE_CHARACTERS.indexOf(c) != -1) {
        buffer.append('\\');
      }
      // now append the actual character
      buffer.append(c);
    }
    return buffer.toString();
  }

  /**
   * Format an object to a string.
   *
   * @param oValue 
   * @param meta 
   * @param bHuman is this string intended to be human-readable ?
   * @return the string
   */
  public static String format(final Object oValue, final PropertyMetaInformation meta,
      final boolean bHuman) {
    final Class<?> cType = meta.getType();
    // default (works for strings, long and double)
    String sValue = oValue.toString();
    if (cType.equals(Date.class)) {
      if (bHuman) {
        sValue = getLocaleDateFormatter().format((Date) oValue);
      } else {
        sValue = UtilString.getAdditionDateFormatter().format((Date) oValue);
      }
    } else if (cType.equals(Class.class)) {
      sValue = oValue.getClass().getName();
    }
    return sValue;
  }

  /**
   * Gets the locale date formatter.
   *
   * @return locale date FORMATTER instance
   */
  public static DateFormat getLocaleDateFormatter() {
    // store the dateFormat as ThreadLocal to avoid performance impact via the costly construction
    if (dateFormat.get() == null) {
      dateFormat.set(DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()));
    }
    return dateFormat.get();
  }

  /**
   * Formatter for properties dialog window.
   *
   * @param sDesc 
   *
   * @return the string
   */
  public static String formatPropertyDesc(final String sDesc) {
    return "<HTML><center><b><font size=+0 color=#000000>" + sDesc + "</font></b><HTML>";
  }

  /**
   * format genre: first letter uppercase and others lowercase.
   *
   * @param genre 
   *
   * @return the string
   */
  public static String formatGenre(final String genre) {
    if (genre.length() == 0) {
      return "";
    }
    if (genre.length() == 1) {
      return genre.substring(0, 1).toUpperCase(Locale.getDefault());
    }
    // construct string with first letter uppercase and rest lowercase
    return genre.substring(0, 1).toUpperCase(Locale.getDefault())
        + genre.toLowerCase(Locale.getDefault()).substring(1);
  }

  /**
   * Performs some cleanups for strings comming from tag libs.
   *
   * @param s 
   *
   * @return the string
   */
  public static String formatTag(final String s) {
    // we delete all non char characters to avoid parsing errors
    char c;
    final StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      c = s.charAt(i);
      if (UtilString.isChar(c)) {
        sb.append(c);
      }
    }
    return sb.toString().trim();
  }

  /**
   * Format a time from secs to a human readable format.
   *
   * @param lTime 
   *
   * @return the string
   */
  public static String formatTimeBySec(final long lTime) {
    // Convert time to int for performance reasons
    int l = (int) lTime;
    if (l == -1) { // means we are in repeat mode
      return "--:--";
    } else if (l < 0) {
      // make sure to to get negative values
      l = 0;
    }
    final int hours = l / 3600;
    final int mins = l / 60 - (hours * 60);
    final int secs = l - (hours * 3600) - (mins * 60);
    final StringBuilder sbResult = new StringBuilder(8);
    if (hours > 0) {
      sbResult.append(UtilString.padNumber(hours, 2)).append(":");
    }
    return sbResult.append(UtilString.padNumber(mins, 2)).append(":")
        .append(UtilString.padNumber(secs, 2)).toString();
  }

  /**
   * Format a string before XML write
   * <p>
   * see http://www.w3.org/TR/2000/REC-xml-20001006
   * <p>
   * substrings
   * <p> ' to &apos;
   * <p> " to &quot;
   * <p> < to &lt;
   * <p>> to &gt;
   * <p> & to &amp;
   *
   * @param s 
   *
   * @return the string
   */
  public static String formatXML(final String s) {
    String sOut = replaceReservedXMLChars(s);
    final StringBuilder sbOut = new StringBuilder(sOut.length());
    /*
     * Transform String to XML-valid characters. XML 1.0 specs ; Character Range
     * [2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] |
     * [#x10000-#x10FFFF] any Unicode character, excluding the surrogate blocks,
     * FFFE, and FFFF.
     */
    for (int i = 0; i < sOut.length(); i++) {
      final char c = sOut.charAt(i);
      if (UtilString.isChar(c)) {
        sbOut.append(c);
      }
    }
    return sbOut.toString();
  }

  /**
   * Replace reserved xml chars.
   *
   * @param s 
   *
   * @return the string
   */
  private static String replaceReservedXMLChars(final String s) {
    String sOut = s;
    if (s.contains("&")) {
      sOut = sOut.replaceAll("&", "&amp;");
    }
    if (s.contains("\'")) {
      sOut = sOut.replaceAll("\'", "&apos;");
    }
    if (s.contains("\"")) {
      sOut = sOut.replaceAll("\"", "&quot;");
    }
    if (s.contains("<")) {
      sOut = sOut.replaceAll("<", "&lt;");
    }
    if (s.contains(">")) {
      sOut = sOut.replaceAll(">", "&gt;");
    }
    return sOut;
  }

  /**
   * Gets the addition date formatter.
   *
   * @return Thread-safe addition date simple format instance
   */
  public static DateFormat getAdditionDateFormatter() {
    return FORMATTER.get();
  }

  /**
   * Gets the anonymized jajuk properties.
   *
   * @return Anonymized Jajuk properties (for log or quality agent)
   */
  public static Properties getAnonymizedJajukProperties() {
    final Properties properties = (Properties) Conf.getProperties().clone();
    // We remove sensible data from logs
    properties.remove("jajuk.network.proxy_login");
    properties.remove("jajuk.network.proxy_port");
    properties.remove("jajuk.network.proxy_hostname");
    properties.remove("jajuk.options.p2p.password");
    return properties;
  }

  /**
   * Gets the anonymized system properties.
   *
   * @return Anonymized System properties (for log or quality agent)
   */
  public static Properties getAnonymizedSystemProperties() {
    final Properties properties = (Properties) System.getProperties().clone();
    // We remove sensible data from logs
    /*
     * can contain external program paths
     */
    properties.remove("java.library.path");
    properties.remove("java.class.path");
    // user name is private
    properties.remove("user.name");
    properties.remove("java.ext.dirs");
    properties.remove("sun.boot.class.path");
    properties.remove("deployment.user.security.trusted.certs");
    properties.remove("deployment.user.security.trusted.clientauthcerts");
    properties.remove("jajuk.log");
    return properties;
  }

  /**
   * Make sure to reduce a string to the given size.
   *
   * @param sIn Input string, example: blabla
   * @param iSize max size, example: 3
   *
   * @return bla...
   */
  public static String getLimitedString(final String sIn, final int iSize) {
    String sOut = sIn;
    if (sIn.length() > iSize) {
      sOut = sIn.substring(0, iSize) + "...";
    }
    return sOut;
  }

  /**
   * Checks if is char.
   *
   * @param ucs4char char to test
   *
   * @return whether the char is valid, code taken from Apache sax
   * implementation
   */
  public static boolean isChar(final int ucs4char) {
    return ((ucs4char >= 32) && (ucs4char <= 55295)) || (ucs4char == 10) || (ucs4char == 9)
        || (ucs4char == 13) || ((ucs4char >= 57344) && (ucs4char <= 65533))
        || ((ucs4char >= 0x10000) && (ucs4char <= 0x10ffff));
  }

  /**
   * Checks if is xml valid.
   *
   * @param s 
   *
   * @return whether given string is XML-valid
   */
  public static boolean isXMLValid(final String s) {
    // check invalid chars
    for (int i = 0; i < s.length(); i++) {
      final char c = s.charAt(i);
      // check reserved chars
      if (-1 != "&\'\"<>".indexOf(c)) {
        return false;
      }
      if (!UtilString.isChar(c)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Return whether a string is null or void
   * @param str the string to test
   * @return whether a string is null or void
   */
  public static boolean isEmpty(String str) {
    return StringUtils.isEmpty(str);
  }

  /**
   * Return whether a string is neither null nor void
   * @param str the string to test
   * @return whether a string is neither null nor void
   */
  public static boolean isNotEmpty(String str) {
    return !StringUtils.isEmpty(str);
  }

  /**
   * Pad an int with zeros.
   *
   * @param l the number to be padded
   * @param size the targeted size
   *
   * @return the string
   */
  public static String padNumber(final long l, final int size) {
    final StringBuilder sb = new StringBuilder(Long.toString(l));
    while (sb.length() < size) {
      sb.insert(0, '0');
    }
    return sb.toString();
  }

  /**
   * Parse a string to an object.
   *
   * @param sValue 
   * @param cType 
   *
   * @return parsed item
   *
   * @throws ParseException the parse exception
   * @throws ClassNotFoundException the class not found exception
   */
  public static Object parse(final String sValue, final Class<?> cType) throws ParseException,
      ClassNotFoundException {
    Object oDefaultValue = sValue; // String by default
    if (cType.equals(Boolean.class)) {
      oDefaultValue = handleBoolean(sValue);
    } else if (cType.equals(Date.class)) {
      oDefaultValue = getAdditionDateFormatter().parseObject(sValue);
    } else if (cType.equals(Long.class)) {
      oDefaultValue = Long.parseLong(sValue);
    } else if (cType.equals(Double.class)) {
      oDefaultValue = Double.parseDouble(sValue);
    } else if (cType.equals(Class.class)) {
      oDefaultValue = Class.forName(sValue);
    }
    return oDefaultValue;
  }

  /**
   * Handle boolean.
   * 
   *
   * @param sValue 
   *
   * @return the boolean
   */
  private static Boolean handleBoolean(final String sValue) {
    Boolean oValue;
    // "y" and "n" is an old boolean
    // attribute notation prior to 1.0
    if ("y".equals(sValue)) {
      oValue = true;
    } else if ("n".equals(sValue)) {
      oValue = false;
    } else {
      oValue = fastBooleanParser(sValue);
    }
    return oValue;
  }

  /**
   * Fast long parser, low level check, replacement of Long.parseLong()
   *
   * CAUTION : do not use if the value can be negative or you will get
   * unexpected results
   *
   * @param in must be a set of digits with a size > 0 and be positive
   *
   * @return the long
   */
  public static long fastLongParser(String in) {
    int length = in.length();
    if (length == 1) {
      return in.charAt(0) - 48;
    }
    long out = 0;
    int length2 = length - 1;
    for (int i = 0; i < length; i++) {
      int digit = in.charAt(i) - 48;
      out += digit * Math.pow(10, (length2 - i));
    }
    return out;
  }

  /**
   * Fast Boolean parser, low level check, replacement of Boolean.parseBoolean()
   *
   * @param in must be a string beginning by true or false (lower case)
   *
   * @return true, if fast boolean parser
   */
  public static boolean fastBooleanParser(String in) {
    return (in.charAt(0) == 't');
  }

  /**
   * Rot13 encode/decode,
   * <p>
   * Thx http://www.idevelopment.info/data/Programming/java/security/
   * java_cryptography_extension/rot13.java
   * </p>
   *
   * @param in text to encode / decode in rote 13
   *
   * @return encoded /decoded text
   */
  public static String rot13(final String in) {
    if (StringUtils.isBlank(in)) {
      return "";
    }
    int abyte = 0;
    final StringBuilder tempReturn = new StringBuilder();
    for (int i = 0; i < in.length(); i++) {
      abyte = in.charAt(i);
      int cap = abyte & 32;
      abyte &= ~cap;
      abyte = ((abyte >= 'A') && (abyte <= 'Z') ? ((abyte - 'A' + 13) % 26 + 'A') : abyte) | cap;
      tempReturn.append((char) abyte);
    }
    return tempReturn.toString();
  }

  /**
   * Matches ignore case and order.
   *
   * @param tested the string to be tested
   * @param key the search criteria, can be several words separated by a space
   *
   * @return whether the given tested string matches the key
   */
  public static boolean matchesIgnoreCaseAndOrder(final String tested, final String key) {
    String testedLower = tested.toLowerCase(Locale.getDefault());
    String keyLower = key.toLowerCase(Locale.getDefault());
    StringTokenizer st = new StringTokenizer(testedLower, " ");
    while (st.hasMoreTokens()) {
      String token = st.nextToken();
      if (keyLower.indexOf(token) == -1) {
        return false;
      }
    }
    return true;
  }

  /**
   * Encode a string to unicode representation (ie \\uxxxx\\uyyyyy...)
   *
   * @param in string to encode
   *
   * @return encoded string
   */
  public static String encodeToUnicode(String in) {
    StringBuilder sb = new StringBuilder(in.length() * 5);
    for (int i = 0; i < in.length(); i++) {
      char c = in.charAt(i);
      byte hi = (byte) (c >>> 8);
      byte lo = (byte) (c & 0xff);
      sb.append("\\u");
      sb.append(byteToHex(hi) + byteToHex(lo));
    }
    return sb.toString();
  }

  /**
   * Convert byte to hexadecimal representation.
   *
   * @param b 
   *
   * @return the string
   */
  public static String byteToHex(byte b) {
    char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
        'f' };
    char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
    return new String(array);
  }

  /**
   * Returns a concatenation of argument array.
   *
   * @param strings strings to be concatened
   *
   * @return concatenation of given strings
   */
  public static String concat(Object... strings) {
    StringBuilder sb = new StringBuilder();
    for (Object element : strings) {
      sb.append(element);
    }
    return sb.toString();
  }

  /**
   * Code token from aTunes 1.14.0 *Copyright (C) 2006-2009 Alex Aranda, Sylvain
   * Gaudard, Thomas Beckers and contributors Returns list of text between
   * specified chars. Both chars are included in result elements. Returns empty
   * list if chars are not found in string in given order For example given
   * string "ab cd (ef) gh (ij)" and chars '(' and ')' will return a list with
   * two strings: "(ef)" and "(ij)"
   *
   * @param string 
   * @param beginChar 
   * @param endChar 
   *
   * @return the text between chars
   */
  public static final List<String> getTextBetweenChars(String string, char beginChar, char endChar) {
    List<String> result = new ArrayList<String>();
    if (string == null || string.indexOf(beginChar) == -1 || string.indexOf(endChar) == -1) {
      return result;
    }
    String auxStr = string;
    int beginIndex = auxStr.indexOf(beginChar);
    int endIndex = auxStr.indexOf(endChar);
    while (beginIndex != -1 && endIndex != -1) {
      if (beginIndex < endIndex) {
        result.add(auxStr.substring(beginIndex, endIndex + 1));
      }
      auxStr = auxStr.substring(endIndex + 1);
      beginIndex = auxStr.indexOf(beginChar);
      endIndex = auxStr.indexOf(endChar);
    }
    return result;
  }
}
