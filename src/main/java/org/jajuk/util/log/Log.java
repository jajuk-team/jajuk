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
package org.jajuk.util.log;

import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Centralized logging facade migrated from Log4j 1.x to Logback/SLF4J.
 * Maintains backward compatibility with existing Jajuk codebase while providing
 * modern logging features including better performance and flexibility.
 *
 * @author Jajuk Team
 * @since Logback Migration 2026
 */
public final class Log {

  private static final Logger LOGGER = LoggerFactory.getLogger(Log.class);
  private static volatile boolean initialized = false;
  // In-memory log buffer for UI feedback display (equivalent to old spool)
  private static final java.util.List<String> LOG_BUFFER = new java.util.LinkedList<>();
  private static final int MAX_BUFFER_SIZE = 1000; // Limit buffer size to avoid memory issues

  /** The Constant FATAL.   */
  public static final int FATAL = 0;
  /** The Constant ERROR.   */
  public static final int ERROR = 1;
  /** The Constant WARNING.   */
  public static final int WARNING = 2;
  /** The Constant INFO.   */
  public static final int INFO = 3;
  /** The Constant DEBUG.   */
  public static final int DEBUG = 4;

  /** The Constant FONT_END. */
  private static final String FONT_END = "</font>";

  /**
   * Private constructor to prevent instantiation.
   */
  private Log() {
  }

  /**
   * Initialize the logging system.
   * Loads logback.xml from the classpath automatically.
   */
  public static void init() {
    if (initialized) {
      return;
    }

    try {
      // Logback auto-configures when it finds logback.xml on classpath
      // No programmatic configuration needed - relies on logback.xml discovery
      initialized = true;
      // message for logging system start
      Log.info("******************JAJUK******************");
      Log.info("Version: " + Const.JAJUK_VERSION);
    } catch (Exception e) {
      System.err.println("[ERROR] Failed to initialize logging: " + e.getMessage());
      e.printStackTrace(System.err);
    }
  }

  /**
   * Log an INFO level message.
   *
   * @param message Message to log
   */
  public static void info(String message) {
    if (!initialized) {
      fallbackPrint("[INFO]", message);
      return;
    }
    spool("<font color='blue'>[INFO] " + message + FONT_END);
    LOGGER.info(message);
  }

  /**
   * Log an INFO level message with formatting placeholders.
   *
   * @param pattern   Message pattern with {} placeholders
   * @param arguments Values to substitute into placeholders
   */
  public static void info(String pattern, Object... arguments) {
    if (!initialized) {
      fallbackPrint("[INFO]", formatMessage(pattern, arguments));
      return;
    }
    spool("<font color='blue'>[INFO] " + formatMessage(pattern, arguments) + FONT_END);
    LOGGER.info(pattern, arguments);
  }

  /**
   * Log a DEBUG level message.
   *
   * @param message Message to log
   */
  public static void debug(String message) {
    if (!initialized) {
      // Don't print debug by default in fallback mode
      return;
    }
    spool("[DEBUG] " + message);
    LOGGER.debug(message);
  }

  /**
   * Log a DEBUG level message with formatting placeholders.
   *
   * @param pattern   Message pattern with {} placeholders
   * @param arguments Values to substitute into placeholders
   */
  public static void debug(String pattern, Object... arguments) {
    if (!initialized) {
      return;
    }
    spool("[DEBUG] " + formatMessage(pattern, arguments));
    LOGGER.debug(pattern, arguments);
  }

  /**
   * Debug.
   *
   * @param t Exception to include
   */
  public static void debug(Throwable t) {
    debug("", t);
  }

  /**
   * Check if debug logging is currently enabled.
   *
   * @return True if DEBUG level is enabled
   */
  public static boolean isDebugEnabled() {
    if (!initialized) {
      return false;
    }
    return LOGGER.isDebugEnabled();
  }

  /**
   * Log a WARNING level message.
   *
   * @param message Message to log
   */
  public static void warn(String message) {
    if (!initialized) {
      fallbackPrint("[WARN]", message);
      return;
    }
    spool("<font color='orange'>[WARN] " + message + FONT_END);
    LOGGER.warn(message);
  }

  /**
   * Log a WARNING level message with formatting placeholders.
   *
   * @param pattern   Message pattern with {} placeholders
   * @param arguments Values to substitute into placeholders
   */
  public static void warn(String pattern, Object... arguments) {
    if (!initialized) {
      fallbackPrint("[WARN]", formatMessage(pattern, arguments));
      return;
    }
    spool("<font color='orange'>[WARN] " + formatMessage(pattern, arguments) + FONT_END);
    LOGGER.warn(pattern, arguments);
  }

  /**
   * Log a WARNING level message with exception.
   *
   * @param message Message to log
   * @param t       Exception to include
   */
  public static void warn(String message, Throwable t) {
    if (!initialized) {
      fallbackPrint("[WARN]", message);
      if (t != null) {
        t.printStackTrace(System.err);
      }
      return;
    }
    spool("<font color='orange'>[WARN] " + message + FONT_END);
    LOGGER.warn(message, t);
  }

  /**
   * Log a warning-level message with error code and context..
   *
   * @param code     Error code for internationalized error messages
   * @param sInfosup Contextual information about the error
   * @param t        The exception that caused the error
   */
  public static void warn(int code, String sInfosup, Throwable t) {
    if (!initialized) {
      // Fallback: print to stderr if logging not yet initialized
      System.err.println("[WARN] (" + code + ") " + sInfosup);
      if (t != null) {
        t.printStackTrace(System.err);
      }
    }
    StringBuilder sOut = new StringBuilder();
    sOut.append("(").append(code).append(") ");
    sOut.append(Messages.getErrorMessage(code));
    if (sInfosup != null && !sInfosup.isEmpty()) {
      sOut.append(": ").append(sInfosup);
    }

    String finalMessage = sOut.toString();

    spool("<font color='orange'>[WARN] " + finalMessage + FONT_END);
    if (t != null) {
      spool(getStackTrace(t));
    }

    LOGGER.warn(finalMessage, t);
  }

  /**
   * Log an ERROR level message.
   *
   * @param message Message to log
   */
  public static void error(String message) {
    if (!initialized) {
      fallbackPrint("[ERROR]", message);
      return;
    }
    spool("<font color='red'>[ERROR] " + message + FONT_END);
    LOGGER.error(message);
  }

  /**
   * Log an ERROR level message with formatting placeholders.
   *
   * @param pattern   Message pattern with {} placeholders
   * @param arguments Values to substitute into placeholders
   */
  public static void error(String pattern, Object... arguments) {
    if (!initialized) {
      fallbackPrint("[ERROR]", formatMessage(pattern, arguments));
      return;
    }
    spool("<font color='red'>[ERROR] " + formatMessage(pattern, arguments) + FONT_END);
    LOGGER.error(pattern, arguments);
  }

  /**
   * Log an ERROR level message with exception.
   *
   * @param message Message to log
   * @param t       Exception to include
   */
  public static void error(String message, Throwable t) {
    if (!initialized) {
      fallbackPrint("[ERROR]", message);
      if (t != null) {
        t.printStackTrace(System.err);
      }
      return;
    }
    spool("<font color='red'>[ERROR] " + message + FONT_END);
    LOGGER.error(message, t);
  }

  /**
   * Log an error-level message.
   *
   * @param code error code
   */
  public static void error(int code) {
    error("{}", code);
  }

  /**
   * Log a FATAL level message.
   * Note: Logback treats FATAL same as ERROR level
   *
   * @param message Message to log
   */
  public static void fatal(String message) {
    if (!initialized) {
      fallbackPrint("[FATAL]", message);
      return;
    }
    spool("<font color='red'><b>[FATAL] " + message + "</b>" + FONT_END);
    LOGGER.error("[FATAL] {}", message);
  }

  /**
   * Log an exception trace only.
   *
   * @param t Exception to log
   */
  public static void error(Throwable t) {
    if (!initialized) {
      if (t != null) {
        t.printStackTrace(System.err);
      }
      return;
    }
    if (t != null) {
      spool(getStackTrace(t));
      LOGGER.error(t.getMessage(), t);
    } else {
      spool("<font color='red'>[ERROR] Unknown error occurred</font>");
      LOGGER.error("Unknown error occurred");
    }
  }

  /**
   * Log an error-level message with error code and context.
   * Maintains backward compatibility with Jajuk's error code system.
   *
   * @param code     Error code for internationalized error messages
   * @param sInfosup Contextual information about the error
   * @param t        The exception that caused the error
   */
  public static void error(int code, String sInfosup, Throwable t) {
    if (!initialized) {
      // Fallback: print to stderr if logging not yet initialized
      System.err.println("[ERROR] (" + code + ") " + sInfosup);
      if (t != null) {
        t.printStackTrace(System.err);
      }
      return;
    }

    StringBuilder sOut = new StringBuilder();
    sOut.append("(").append(code).append(") ");

    // Try to get human-readable error message from Messages system if available
    try {
      Class<?> messagesClass = Class.forName("org.jajuk.util.Messages");
      java.lang.reflect.Method isInitMethod = messagesClass.getMethod("isInitialized");
      boolean messagesInitialized = (Boolean) isInitMethod.invoke(null);

      if (messagesInitialized) {
        java.lang.reflect.Method getErrorMsgMethod = messagesClass.getMethod("getErrorMessage", int.class);
        String errorMsg = (String) getErrorMsgMethod.invoke(null, code);
        sOut.append(errorMsg);
      } else {
        sOut.append("Error ").append(code);
      }
    } catch (Exception e) {
      // Messages system not available - fall back to simple error code display
      sOut.append("Error ").append(code);
    }

    if (sInfosup != null && !sInfosup.isEmpty()) {
      sOut.append(": ").append(sInfosup);
    }

    String finalMessage = sOut.toString();

    // Write to spool buffer (for UI feedback panel)
    spool("<font color='red'>[ERROR] " + finalMessage + FONT_END);
    if (t != null) {
      spool(getStackTrace(t));
    }

    // Log via SLF4J/Logback
    LOGGER.error(finalMessage, t);
  }

  /**
   * Log an error-level message.
   *
   * @param code Error code for internationalized error messages
   * @param t    The exception that caused the error
   */
  public static void error(int code, Throwable t) {
    error(code, null, t);
  }

  /**
   * Log an error-level message.
   *
   * @param s message
   */
  public static synchronized void error(String s) {
    // Just display the message if Log is not yet enabled
    if (logger == null) {
      System.out.println("[ERROR] " + s);
      return;
    }
    spool("<font color='red'>[ERROR] " + s + FONT_END);
    logger.log(FULL_QUALIFIED_CLASS_NAME, Level.DEBUG, s, null);
  }

  /**
   * Format a message pattern with arguments into actual string.
   * Used for fallback printing when Logback isn't initialized yet.
   *
   * @param pattern   Pattern with {} placeholders
   * @param arguments Arguments to fill placeholders
   * @return Formatted string
   */
  private static String formatMessage(String pattern, Object[] arguments) {
    if (pattern == null || arguments == null || arguments.length == 0) {
      return pattern;
    }

    StringBuilder sb = new StringBuilder();
    int cursor = 0;
    for (Object argument : arguments) {
      int nextPlaceholder = pattern.indexOf("{}", cursor);
      if (nextPlaceholder == -1) {
        sb.append(pattern.substring(cursor));
        break;
      }
      sb.append(pattern, cursor, nextPlaceholder);
      sb.append(argument != null ? argument.toString() : "null");
      cursor = nextPlaceholder + 2;
    }
    if (cursor < pattern.length()) {
      sb.append(pattern.substring(cursor));
    }
    return sb.toString();
  }

  /**
   * Fallback logging when Logback hasn't been initialized yet.
   * Writes to stderr/stdout directly.
   *
   * @param prefix  Log level prefix
   * @param message Actual message content
   */
  private static void fallbackPrint(String prefix, String message) {
    StringBuilder buf = new StringBuilder();
    buf.append(prefix).append(' ').append(message).append('\n');

    if (prefix.equals("[ERROR]") || prefix.equals("[FATAL]")) {
      System.err.print(buf);
    } else {
      System.out.print(buf);
    }
  }

  /* ============================================
   UTILITY & SUPPORT METHODS
   ============================================ */

  /**
   * Get string representation of throwable's stack trace.
   * Used for spool buffering without writing multiple lines.
   *
   * @param t Exception to convert
   * @return Formatted stack trace string
   */
  private static String getStackTrace(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();
    return sw.toString();
  }

  /**
   * Get current logging verbosity level.
   *
   * @return Current level as integer constant (see setVerbosity for mapping)
   */
  public static int getVerbosity() {
    if (!initialized) {
      return 3; // Default INFO level
    }

      ch.qos.logback.classic.Logger rootLogger =
              ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME));

    ch.qos.logback.classic.Level level = rootLogger.getLevel();

    if (level == null || level.toInt() <= ch.qos.logback.classic.Level.TRACE_INT) {
      return 5; // TRACE
    } else if (level.toInt() <= ch.qos.logback.classic.Level.DEBUG_INT) {
      return 4; // DEBUG
    } else if (level.toInt() <= ch.qos.logback.classic.Level.INFO_INT) {
      return 3; // INFO
    } else if (level.toInt() <= ch.qos.logback.classic.Level.WARN_INT) {
      return 2; // WARN
    } else if (level.toInt() <= ch.qos.logback.classic.Level.ERROR_INT) {
      return 1; // ERROR
    } else {
      return 0; // OFF/FATAL
    }
  }

  /**
   * Set the logging verbosity level dynamically at runtime.
   * Compatible with Jajuk's legacy verbosity constants.
   *
   * @param newVerbosity Desired log level:
   *                     0 = FATAL only
   *                     1 = ERROR
   *                     2 = WARN
   *                     3 = INFO
   *                     4 = DEBUG
   */
  public static void setVerbosity(int newVerbosity) {
    if (!initialized) {
      return; // Can't configure before initialization
    }

    // Convert numeric verbosity to Logback Level
    ch.qos.logback.classic.Level level;

    switch (newVerbosity) {
      case 0: // FATAL / OFF
        level = ch.qos.logback.classic.Level.OFF;
        break;
      case 1: // ERROR
        level = ch.qos.logback.classic.Level.ERROR;
        break;
      case 2: // WARNING/WARN
        level = ch.qos.logback.classic.Level.WARN;
        break;
      case 3: // INFO
        level = ch.qos.logback.classic.Level.INFO;
        break;
      case 4: // DEBUG (most verbose)
        level = ch.qos.logback.classic.Level.DEBUG;
        break;
      case 5: // TRACE (extra verbose)
        level = ch.qos.logback.classic.Level.TRACE;
        break;
      default:
        // Invalid level - fall back to WARN
        level = ch.qos.logback.classic.Level.WARN;
        warn("Invalid verbosity level " + newVerbosity + ", defaulted to WARN");
    }

    try {
      // Get Logback LoggerContext and apply level to root logger
      ch.qos.logback.classic.LoggerContext context =
              (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();

      // Apply to root logger (affects all packages)
      ch.qos.logback.classic.Logger rootLogger = context.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
      rootLogger.setLevel(level);

      // Also apply to our specific Log class logger
      ((ch.qos.logback.classic.Logger) LOGGER).setLevel(level);

      info("Verbosity changed to " + level);
    } catch (Exception e) {
      // Fallback: Log via SLF4J if cast fails
      warn("Failed to set verbosity level programmatically", e);
    }
  }

  /**
   * Add entry to in-memory log buffer for UI display.
   * Buffer size limited to avoid memory issues in long-running sessions.
   *
   * @param message Message to store in buffer
   */
  public static void spool(String message) {
    if (message == null) {
      return;
    }

    synchronized (LOG_BUFFER) {
      LOG_BUFFER.add(message);

      // Maintain max size by removing oldest entries
      while (LOG_BUFFER.size() > MAX_BUFFER_SIZE) {
        LOG_BUFFER.remove(0);
      }
    }
  }

  /**
   * Retrieve all buffered log messages from spool.
   *
   * @param anonymized If true, attempts to mask sensitive data wrapped in {{...}}
   * @return Copy of current buffer contents as new ArrayList
   */
  public static java.util.List<String> getSpool(boolean anonymized) {
    synchronized (LOG_BUFFER) {
      java.util.List<String> result = new java.util.ArrayList<>(LOG_BUFFER);

      if (anonymized) {
        // Apply anonymization to each entry if requested
        result.replaceAll(Log::anonymize);
      }

      return result;
    }
  }

  /**
   * Clear all buffered log messages from spool.
   * Called on reset or UI refresh operations.
   */
  public static void clearSpool() {
    synchronized (LOG_BUFFER) {
      LOG_BUFFER.clear();
    }
  }

  /**
   * Convert Throwable to string representation suitable for spool storage.
   * Captures full stack trace in formatted string.
   *
   * @param t Exception to convert
   */
  public static void stack(Throwable t) {
    if (t == null) {
      return;
    }

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.flush();

    spool(sw.toString());
  }

  /**
   * Mark text for potential anonymization processing.
   * Wraps content with {{ }} brackets which can be detected and redacted later.
   *
   * @param message Text containing potentially sensitive information
   * @return Same text (anonymization happens during retrieval in getSpool(true))
   */
  public static String protect(String message) {
    if (message == null) {
      return null;
    }
    // Annotation format preserved for downstream anonymization
    return "{{" + message + "}}";
  }

  /**
   * Simple anonymization routine for spool output.
   * Removes or replaces content enclosed in {{...}} markers.
   *
   * @param text Original text possibly containing {{sensitive}} sections
   * @return Sanitized text with protected portions replaced
   */
  private static String anonymize(String text) {
    if (text == null || !text.contains("{{")) {
      return text;
    }

    // Replace {{protected-content}} with ***
    StringBuilder result = new StringBuilder();
    int cursor = 0;
    int pos;

    while ((pos = text.indexOf("{{", cursor)) != -1) {
      int endPos = text.indexOf("}}", pos);

      if (endPos == -1) {
        // Malformed marker - keep everything unchanged
        result.append(text.substring(cursor));
        break;
      }

      // Append content before the marker
      result.append(text, cursor, pos);

      // Skip the protected content entirely (or replace with ***)
      result.append("***");

      cursor = endPos + 2; // Move past closing marker
    }

    if (cursor < text.length()) {
      result.append(text.substring(cursor));
    }

    return result.toString();
  }

}