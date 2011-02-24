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
 *  $Revision$
 */
package org.jajuk.util.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

/**
 * Logging utility class, facade to logging system
 * <p>
 * Singleton.
 */
public final class Log {

  /** The Constant FONT_END.  DOCUMENT_ME */
  private static final String FONT_END = "</font>";

  /** The Constant LOGGER_APACHE_HTTPCLIENT.  DOCUMENT_ME */
  private static final String LOGGER_APACHE_HTTPCLIENT = "org.apache.commons.httpclient";

  // verbosity consts
  /** The Constant FATAL.  DOCUMENT_ME */
  public static final int FATAL = 0;

  /** The Constant ERROR.  DOCUMENT_ME */
  public static final int ERROR = 1;

  /** The Constant WARNING.  DOCUMENT_ME */
  public static final int WARNING = 2;

  /** The Constant INFO.  DOCUMENT_ME */
  public static final int INFO = 3;

  /** The Constant DEBUG.  DOCUMENT_ME */
  public static final int DEBUG = 4;

  /** Verbosity level of the logger( between 1 and 5 ) <p> Default used at statup is INFO. */
  private static int verbosity = INFO;

  /** Jajuk logger. */
  private static Logger logger;

  /** Debug traces spool. */
  private static List<String> alSpool;

  /** The Constant FULL_QUALIFIED_CLASS_NAME.  DOCUMENT_ME */
  private static final String FULL_QUALIFIED_CLASS_NAME = Log.class.getName();

  /**
   * Log system initialization.
   */
  public static void init() {
    try {
      // set env variable used in the log4j conf file
      System.setProperty("jajuk.log", SessionService.getConfFileByPath(Const.FILE_LOGS)
          .getAbsolutePath());
      DOMConfigurator.configure(Const.FILE_LOG4J_CONF);
    } catch (Exception e) {
      Log.stack(e);
    }

    logger = Logger.getLogger(Log.class.getName());
    alSpool = new ArrayList<String>(Const.FEEDBACK_LINES);
    // message for logging system start
    Log.info("******************JAJUK******************");
    Log.info("Version: " + Const.JAJUK_VERSION);
  }

  /**
   * Log a debug-level message.
   * 
   * @param s DOCUMENT_ME
   */
  public static void debug(String s) {
    // Just display the message if Log is not yet enabled
    if (logger == null) {
      System.out.println("[DEBUG] " + s);
      return;
    }
    spool("[DEBUG] " + s);
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.DEBUG, s, null);
  }

  /**
   * Debug.
   * DOCUMENT_ME
   * 
   * @param t DOCUMENT_ME
   */
  public static void debug(Throwable t) {
    debug("", t);
  }

  /**
   * Debug.
   * DOCUMENT_ME
   * 
   * @param sInfosup DOCUMENT_ME
   * @param t DOCUMENT_ME
   */
  public static void debug(String sInfosup, Throwable t) {
    // Just make a print stake trace if Log is not yet enabled (example:
    // collection commit problem in initialCheckups)
    if (logger == null) {
      System.out.println("[DEBUG] " + sInfosup);
      stack(t);
      return;
    }
    String sOut;
    if (Messages.isInitialized()) {
      sOut = ((sInfosup == null) ? "" : ": " + sInfosup);
    } else {
      sOut = ((sInfosup == null) ? "" : ":" + sInfosup);
    }
    spool("<font color='red'>[DEBUG] " + sOut + FONT_END);
    if (t != null) {
      spool(t);
    }
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.DEBUG, sOut, t);
  }

  /**
   * Log a info-level message.
   * 
   * @param s DOCUMENT_ME
   */
  public static void info(String s) {
    // Just display the message if Log is not yet enabled
    if (logger == null) {
      System.out.println("[INFO] " + s);
      return;
    }
    spool("<font color='blue'>[INFO] " + s + FONT_END);
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.INFO, s, null);
  }

  /**
   * Log a warning-level message.
   * 
   * @param s DOCUMENT_ME
   */
  public static void warn(String s) {
    // Just display the message if Log is not yet enabled
    if (logger == null) {
      System.out.println("[WARN] " + s);
      return;
    }
    spool("<font color='orange'>[WARN] " + s + FONT_END);
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.WARN, s, null);
  }

  /**
   * Log a warning-level message with info sup.
   * 
   * @param s DOCUMENT_ME
   * @param sInfoSup DOCUMENT_ME
   */
  public static void warn(String s, String sInfoSup) {
    String sOut = s + ": " + sInfoSup;
    // Just display the message if Log is not yet enabled
    if (logger == null) {
      System.out.println("[WARN] " + sOut);
      return;
    }
    spool("<font color='orange'>[INFO] " + sOut + FONT_END);
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.WARN, sOut, null);
  }

  /**
   * Log an warning-level message.
   * 
   * @param code error code
   * @param sInfosup : error context information
   * @param t the exception itself
   */
  public static void warn(int code, String sInfosup, Throwable t) {
    String sOut;
    if (Messages.isInitialized()) {
      sOut = "(" + code + ") " + Messages.getErrorMessage(code)
          + ((sInfosup == null) ? "" : ":" + sInfosup);
    } else {
      sOut = "(" + code + ") " + ((sInfosup == null) ? "" : ":" + sInfosup);
    }
    // Just display the message if Log is not yet enabled
    if (logger == null) {
      System.out.println("[WARN] " + sOut);
      stack(t);
      return;
    }
    spool("<font color='orange'>[WARN] " + sOut + FONT_END);
    spool(t);
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.WARN, sOut, t);
  }

  /**
   * Log an error-level message.
   * 
   * @param code error code
   * @param sInfosup : error context information
   * @param t the exception itself
   */
  public static void error(int code, String sInfosup, Throwable t) {
    // Just make a print stake trace if Log is not yet enabled (example:
    // collection commit problem in initialCheckups)
    if (logger == null) {
      System.out.println("[ERROR] " + code + " / " + sInfosup);
      stack(t);
      return;
    }
    String sOut;
    if (Messages.isInitialized()) {
      sOut = "(" + code + ") " + Messages.getErrorMessage(code)
          + ((sInfosup == null) ? "" : ": " + sInfosup);
    } else {
      sOut = "(" + code + ") " + ((sInfosup == null) ? "" : ":" + sInfosup);
    }
    spool("<font color='red'>[ERROR] " + sOut + FONT_END);
    if (t != null) {
      spool(t);
    }
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.ERROR, sOut, t);
  }

  /**
   * Log an error-level message.
   * 
   * @param code error code
   */
  public static void error(int code) {
    String sOut;
    if (Messages.isInitialized()) {
      sOut = "(" + code + ") " + Messages.getErrorMessage(code);
    } else {
      sOut = "(" + code + ") ";
    }
    // Just make a print stake trace if Log is not yet enabled (example:
    // collection commit problem in initialCheckups)
    if (logger == null) {
      System.out.println("[ERROR] " + sOut);
      return;
    }
    spool("<font color='red'>[ERROR] " + sOut + FONT_END);
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.ERROR, sOut, null);
  }

  /**
   * Log an error-level message.
   * 
   * @param t the exception itself
   */
  public static void error(Throwable t) {
    // Just make a print stake trace if Log is not yet enabled (example:
    // collection commit problem in initialCheckups)
    if (logger == null) {
      stack(t);
      return;
    }
    spool(t);
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.ERROR, t.getMessage() + " / " + t.getCause(), t);
  }

  /**
   * Log an error-level message.
   * 
   * @param t DOCUMENT_ME
   * @param code DOCUMENT_ME
   */
  public static void error(int code, Throwable t) {
    error(code, null, t);
  }

  /**
   * Log an error-level message.
   * 
   * @param sInfosup DOCUMENT_ME
   * @param je DOCUMENT_ME
   */
  public static void error(String sInfosup, JajukException je) {
    error(je.getCode(), sInfosup, je);
  }

  /**
   * Log an error-level message.
   * 
   * @param je DOCUMENT_ME
   */
  public static void error(JajukException je) {
    error(je.getCode(), null, je);
  }

  /**
   * Log a fatal error message.
   * 
   * @param s DOCUMENT_ME
   */
  public static void fatal(String s) {
    // Just make a print stake trace if Log is not yet enabled (example:
    // collection commit problem in initialCheckups)
    if (logger == null) {
      System.out.println("[FATAL] " + s);
      return;
    }
    spool("<font color='red'><b>[FATAL] " + s + "</b></font>");
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.FATAL, s, null);
  }

  /**
   * Returns the verbosity.
   * 
   * @return int
   */
  public static int getVerbosity() {
    return verbosity;
  }

  /**
   * Sets the verbosity.
   * 
   * @param newVerbosity DOCUMENT_ME
   */
  public static void setVerbosity(int newVerbosity) {
    verbosity = newVerbosity;
    switch (newVerbosity) {
    case DEBUG:
      logger.setLevel(Level.DEBUG);
      Logger.getLogger(LOGGER_APACHE_HTTPCLIENT).setLevel(Level.WARN);
      Logger.getRootLogger().setLevel(Level.WARN);
      break;
    case INFO:
      logger.setLevel(Level.INFO);
      Logger.getLogger(LOGGER_APACHE_HTTPCLIENT).setLevel(Level.WARN);
      Logger.getRootLogger().setLevel(Level.WARN);
      break;
    case WARNING:
      logger.setLevel(Level.WARN);
      Logger.getLogger(LOGGER_APACHE_HTTPCLIENT).setLevel(Level.WARN);
      Logger.getRootLogger().setLevel(Level.WARN);
      break;
    case ERROR:
      logger.setLevel(Level.ERROR);
      Logger.getLogger(LOGGER_APACHE_HTTPCLIENT).setLevel(Level.ERROR);
      Logger.getRootLogger().setLevel(Level.ERROR);
      break;
    case FATAL:
      logger.setLevel(Level.FATAL);
      Logger.getLogger(LOGGER_APACHE_HTTPCLIENT).setLevel(Level.FATAL);
      Logger.getRootLogger().setLevel(Level.FATAL);
      break;
    }
  }

  /**
   * Convenient method to display stacks properly.
   * 
   * @param e DOCUMENT_ME
   */
  public static void stack(Throwable e) {
    e.printStackTrace();
  }

  /**
   * Return whether Log are in debug mode.
   * 
   * @return true, if checks if is debug enabled
   */
  public static boolean isDebugEnabled() {
    if (verbosity == Log.DEBUG) {
      return true;
    }
    return false;
  }

  /**
   * Add this message in the memory spool.
   * 
   * @param sMessage DOCUMENT_ME
   */
  private synchronized static void spool(String sMessage) {
    // we maz have to make some room
    if (alSpool.size() >= Const.FEEDBACK_LINES) {
      alSpool.remove(0);
    }

    // anonymize standard labels (with {{xxx}})
    String sAnonymizedMessage = sMessage.replaceAll("\\{\\{.*\\}\\}", "***");

    // additionally anonymize Basic Player logs
    int pos = sAnonymizedMessage.indexOf("Player state changed: OPENING");
    if (pos != -1) {
      // cut away trailing stuff which is personal data
      sAnonymizedMessage = sAnonymizedMessage.substring(0, pos + 40);
    }
    alSpool.add(sAnonymizedMessage);
  }

  /**
   * Spool an exception with stack traces.
   * 
   * @param e DOCUMENT_ME
   */
  private static void spool(Throwable e) {
    spool("<font color='red'>" + "[ERROR] " + e.getClass() + " / {{" + e.getMessage() + "}} / "
        + e.getCause());
    StackTraceElement[] ste = e.getStackTrace();
    for (StackTraceElement element : ste) {
      spool(element.toString());
    }
    spool(FONT_END);
  }

  /**
   * Gets the spool.
   * 
   * @return Spool traces
   */
  @SuppressWarnings("unchecked")
  public static List<String> getSpool() {
    return (List<String>) ((ArrayList<String>) alSpool).clone();
  }

}
