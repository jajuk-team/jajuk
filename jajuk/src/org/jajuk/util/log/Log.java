/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Log$
 * Revision 1.3  2003/10/21 20:43:07  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 * Revision 1.2  2003/10/17 20:43:56  bflorat
 * 17/10/2003
 *
 * Revision 1.1  2003/10/09 21:15:20  bflorat
 * Added log to version control
 *
 */
package org.jajuk.util.log;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;

/**
 * Logging utility class, facade to logging system
 *<p>Singleton
 * @author     bflorat
 * @created    8 oct. 2003
 */
public class Log implements ITechnicalStrings{
	private static String sLogFile;
		/** logger for non-persistant events ( output on console)*/
		private static Logger loggerTemp;	
		/** logger for persistent events ( output in a file )*/
		private static Logger logger;	
		//verbosity consts
		public static final int FATAL = 0;
		public static final int ERROR = 1;
		public static final int WARNING = 2;
		public static final int INFO = 3;
		public static final int DEBUG = 4;
		/**Verbosity level of the logger( between 1 and 5 )
		 * <p>Default used at statup  is INFO  */				
		private static int verbosity = INFO;  
		 /**Self instance used for signleton pattern */
		private static Log log = null;
	

		/**
		 *  Constructor for the Log object
		 */
		Log () {
			try {
				//--create console+file logger
				logger = Logger.getLogger("temp.file");  //this logger is a children of the temp logger to get console output by inherance //$NON-NLS-1$
				logger.setLevel(Level.DEBUG);  
				//add appenders: display message at the same time in the log file and on the console
				RollingFileAppender fileAppender = new RollingFileAppender(new PatternLayout(LOG_PATTERN),FILE_LOG,true);
				fileAppender.setMaxFileSize(LOG_FILE_SIZE);  //set log file maximum size
				logger.addAppender(fileAppender);
				logger.addAppender(new ConsoleAppender(new PatternLayout(LOG_PATTERN)));
				//message for logging system start
				Log.info(Messages.getString("Log.new_session")); //$NON-NLS-1$
				Log.info(Messages.getString("Log.Logging_system_correctly_started_4")); //$NON-NLS-1$
			} catch (Exception e) {
				Log.stack(e);
				System.out.println(Messages.getString("Log.Error_during_logging_system_startup_5")); //$NON-NLS-1$
			}
		}

		/**
		* Return a self instance
		* <p>Implementation of the singleton pattern
		*/
		public static Log getInstance(){
			 if ( Log.log != null){
				return Log.log;
			 }
			 else{
					Log.log = new Log();     	
					return Log.log;
			 }
		}

    
		/**
		 * Log a debug-level  message
		 */
		public static void debug(String s){
				logger.debug(s);
		}
    
		/**
		 * Log a info-level  message
		 */
		public static void info(String s){
				logger.info(s);
		}

		/**
		 * Log a warning-level  message
		 */
		public static void warn(String s){
				logger.warn(s);
		 }

		/**
		 * Log an error-level  message
		 * @param sInfosup
		 * @param t
		**/
		public static void error(String sInfosup,Throwable t){
				logger.error(sInfosup,t);
		 }

			/**
			 * Log an error-level  message
			 * @param sInfosup
			 * @param t
			**/
			public static void error(String sInfosup,JajukException je){
					logger.error('('+je.getCode()+") " +sInfosup,je);
			 }


		/**
		 * Log a fatal error message
		 */
	   public  static void fatal(String s){
				logger.fatal(s);
		}

    

		/**
		 * Returns the verbosity.
		 * @return int
		 */
		public int getVerbosity() {
			return verbosity;
		}

		/**
		 * Sets the verbosity.
		 * @param verbosity The verbosity to set
		 */
		public static void setVerbosity(int newVerbosity) {
			verbosity = newVerbosity;
			//associate log4j level with AMI int levels (1 to 5 )
			switch(newVerbosity){
				case DEBUG:
						logger.setLevel(Level.DEBUG);
					break;
				case INFO:
						logger.setLevel(Level.INFO);
					break;
				case WARNING:
						logger.setLevel(Level.WARN);
					break;
				case ERROR:
						logger.setLevel(Level.ERROR);
					break;
				case FATAL:
						logger.setLevel(Level.FATAL);
					break;
			}
		}
	
	
		/**
		 * Convenient method to display stacks properly
		 */
		public static void stack(Exception e){
			e.printStackTrace();
			System.out.println();
		}
}


