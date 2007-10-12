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
 *  $$Revision$$
 */
package org.jajuk.util.log;

import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

/**
 * Logging utility class, facade to logging system
 * <p>
 * Singleton
 */
public class Log implements ITechnicalStrings {
	// verbosity consts
	public static final int FATAL = 0;

	public static final int ERROR = 1;

	public static final int WARNING = 2;

	public static final int INFO = 3;

	public static final int DEBUG = 4;

	/**
	 * Verbosity level of the logger( between 1 and 5 )
	 * <p>
	 * Default used at statup is INFO
	 */
	private static int verbosity = INFO;

	/** Self instance used for singleton pattern */
	private static Log log = null;

	// Root logger
	private static Logger loggerRoot;

	// Jajuk logger
	private static Logger logger;

	// Http client logger
	private static Logger loggerHttp;

	/** Debug traces spool */
	private static ArrayList<String> alSpool;

	/**
	 * Constructor for the Log object
	 */
	private Log() {
		try {
			// set env variable used in the log4j conf file
			System.setProperty("jajuk.log", Util.getConfFileByPath(FILE_LOGS).getAbsolutePath());
			DOMConfigurator.configure(FILE_LOG4j_CONF);
			loggerRoot = Logger.getRootLogger();
			logger = Logger.getLogger(Log.class.getName());
			loggerHttp = Logger.getLogger("org.apache.commons.httpclient"); 
			alSpool = new ArrayList<String>(FEEDBACK_LINES);
			// message for logging system start
			Log.info("******************JAJUK******************"); 
			Log.info("Version: " + JAJUK_VERSION); 
		} catch (Exception e) {
			Log.stack(e);
		}
	}

	/**
	 * Return a self instance
	 * <p>
	 * Implementation of the singleton pattern
	 */
	public static synchronized Log getInstance() {
		if (Log.log == null) {
			Log.log = new Log();
		}
		return Log.log;
	}

	/**
	 * Log a debug-level message
	 */
	public static void debug(String s) {
		// Just display the message if Log is not yet enabled
		if (log == null) {
			System.out.println("[DEBUG] " + s);
			return;
		}
		spool("[DEBUG] " + s); 
		logger.debug(s);
	}

	/**
	 * Log a info-level message
	 */
	public static void info(String s) {
		spool("<font color='blue'>[INFO] " + s + "</font>");
		logger.info(s);
	}

	/**
	 * Log a warning-level message
	 */
	public static void warn(String s) {
		// Just display the message if Log is not yet enabled
		if (log == null) {
			System.out.println("[WARN] " + s);
			return;
		}
		spool("<font color='orange'>[WARN] " + s + "</font>");
		logger.warn(s);
	}

	/**
	 * Log a warning-level message with info sup
	 */
	public static void warn(String s, String sInfoSup) {
		String sOut = s + ": " + sInfoSup; 
		// Just display the message if Log is not yet enabled
		if (log == null) {
			System.out.println("[WARN] " + sOut);
			return;
		}
		spool("<font color='orange'>[INFO] " + sOut + "</font>");
		logger.warn(sOut);
	}

	/**
	 * Log an warning-level message
	 * 
	 * @param code
	 *            error code
	 * @param sInfosup :
	 *            error context information
	 * @param t
	 *            the exception itself
	 */
	public static void warn(int code, String sInfosup, Throwable t) {
		String sOut;
		if (Messages.isInitialized()) {
			sOut = '('
					+ code
					+ ") " + Messages.getErrorMessage(code) + ((sInfosup == null) ? "" : ":" + sInfosup);   
		} else {
			sOut = '(' + code + ") " + ((sInfosup == null) ? "" : ":" + sInfosup);   
		}
		// Just display the message if Log is not yet enabled
		if (log == null) {
			System.out.println("[WARN] " + sOut);
			t.printStackTrace();
			return;
		}
		spool("<font color='orange'>[WARN] " + sOut + "</font>");
		spool(t);
		logger.warn(sOut, t);
	}

	/**
	 * Log an error-level message
	 * 
	 * @param code
	 *            error code
	 * @param sInfosup :
	 *            error context information
	 * @param t
	 *            the exception itself
	 */
	public static void error(int code, String sInfosup, Throwable t) {
		// Just make a print stake trace if Log is not yet enabled (example:
		// collection commit problem in initialCheckups)
		if (log == null) {
			System.out.println("[ERROR] " + code + " / " + sInfosup);
			t.printStackTrace();
			return;
		}
		String sOut;
		if (Messages.isInitialized()) {
			sOut = '('
					+ code
					+ ") " + Messages.getErrorMessage(code) + ((sInfosup == null) ? "" : ": " + sInfosup);   
		} else {
			sOut = '(' + code + ") " + ((sInfosup == null) ? "" : ":" + sInfosup);   
		}
		spool("<font color='red'>[ERROR] " + sOut + "</font>");
		if (t != null) {
			spool(t);
		}
		logger.error(sOut, t);
	}

	/**
	 * Log an error-level message
	 * 
	 * @param code
	 *            error code
	 */
	public static void error(int code) {
		String sOut;
		if (Messages.isInitialized()) {
			sOut = '(' + code + ") " + Messages.getErrorMessage(code);   
		} else {
			sOut = '(' + code + ") ";   
		}
		// Just make a print stake trace if Log is not yet enabled (example:
		// collection commit problem in initialCheckups)
		if (log == null) {
			System.out.println("[ERROR] " + sOut);
			return;
		}
		spool("<font color='red'>[ERROR] " + sOut + "</font>");
		logger.error(sOut);
	}

	/**
	 * Log an error-level message
	 * 
	 * @param t
	 *            the exception itself
	 */
	public static void error(Throwable t) {
		// Just make a print stake trace if Log is not yet enabled (example:
		// collection commit problem in initialCheckups)
		if (log == null) {
			t.printStackTrace();
			return;
		}
		spool(t);
		logger.error(t.getMessage() + " / " + t.getCause(), t); 
	}

	/**
	 * Log an error-level message
	 * 
	 * @param sInfosup
	 * @param t
	 */
	public static void error(int code, Throwable t) {
		error(code, null, t);
	}

	/**
	 * Log an error-level message
	 * 
	 * @param sInfosup
	 * @param je
	 */
	public static void error(String sInfosup, JajukException je) {
		error(je.getCode(), sInfosup, je);
	}

	/**
	 * Log an error-level message
	 * 
	 * @param je
	 */
	public static void error(JajukException je) {
		error(je.getCode(), null, je);
	}

	/**
	 * Log a fatal error message
	 */
	public static void fatal(String s) {
		// Just make a print stake trace if Log is not yet enabled (example:
		// collection commit problem in initialCheckups)
		if (log == null) {
			System.out.println("[FATAL] " + s);
			return;
		}
		spool("<font color='red'><b>[FATAL] " + s + "</b></font>");
		logger.fatal(s);
	}

	/**
	 * Returns the verbosity.
	 * 
	 * @return int
	 */
	public int getVerbosity() {
		return verbosity;
	}

	/**
	 * Sets the verbosity.
	 * 
	 * @param verbosity
	 *            The verbosity to set
	 */
	public static void setVerbosity(int newVerbosity) {
		verbosity = newVerbosity;
		switch (newVerbosity) {
		case DEBUG:
			logger.setLevel(Level.DEBUG);
			loggerHttp.setLevel(Level.WARN);
			loggerRoot.setLevel(Level.WARN);
			break;
		case INFO:
			logger.setLevel(Level.INFO);
			loggerHttp.setLevel(Level.WARN);
			loggerRoot.setLevel(Level.WARN);
			break;
		case WARNING:
			logger.setLevel(Level.WARN);
			loggerHttp.setLevel(Level.WARN);
			loggerRoot.setLevel(Level.WARN);
			break;
		case ERROR:
			logger.setLevel(Level.ERROR);
			loggerHttp.setLevel(Level.ERROR);
			loggerRoot.setLevel(Level.ERROR);
			break;
		case FATAL:
			logger.setLevel(Level.FATAL);
			loggerHttp.setLevel(Level.FATAL);
			loggerRoot.setLevel(Level.FATAL);
			break;
		}
	}

	/**
	 * Convenient method to display stacks properly
	 */
	public static void stack(Exception e) {
		e.printStackTrace();
	}

	/**
	 * Return whether Log are in debug mode
	 * 
	 * @return
	 */
	public static boolean isDebugEnabled() {
		if (verbosity == Log.DEBUG) {
			return true;
		}
		return false;
	}

	/**
	 * Add this message in the memory spool
	 * 
	 * @param sMessage
	 */
	private static void spool(String sMessage) {
		if (alSpool.size() == FEEDBACK_LINES) { // we have to make some room
			alSpool.remove(0);
		}
		try {
			// anonymize standard labels (with {{xxx}})
			String sAnonymizedMessage = sMessage.replaceAll("\\{\\{.*\\}\\}", "***");  
			// anonymize Basic Player logs
			if (sAnonymizedMessage.indexOf("Player state changed: OPENING") != -1) { 
				sAnonymizedMessage = sAnonymizedMessage.substring(0, 40);
			}
			alSpool.add(sAnonymizedMessage);
		} catch (Exception e) { // make sure to avoid looping tracing
			System.out.print("Spooling error:" + e); 
		}
	}

	/**
	 * Spool an exception with stack traces
	 * 
	 * @param e
	 */
	private static void spool(Throwable e) {
		spool("[ERROR] " + e.getClass() + " / " 
				+ e.getMessage() + " / " + e.getCause()); 
		StackTraceElement[] ste = e.getStackTrace();
		for (int i = 0; i < ste.length; i++) {
			spool("<font color='red'>" + ste[i].toString() + "</font>");
		}
	}

	/**
	 * @return Spool traces
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<String> getSpool() {
		return (ArrayList<String>)alSpool.clone();
	}

}
