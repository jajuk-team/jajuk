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
 *  $Revision$
 */
package org.jajuk.util.log;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;

/**
 * Logging utility class, facade to logging system
 *<p>Singleton
 * @author     bflorat
 * @created    8 oct. 2003
 */
public class Log  implements ITechnicalStrings{
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
    // Root logger
    private static Logger loggerRoot = Logger.getRootLogger();
    //Jajuk logger
    private static Logger logger = Logger.getLogger(Log.class.getName());
    //Http client logger
    private static Logger loggerHttp;
    
    /**
     *  Constructor for the Log object
     */
    private   Log () {
        try {
           loggerHttp = Logger.getLogger("org.apache.commons.httpclient");
            //message for logging system start
            Log.info("******************JAJUK******************"); //$NON-NLS-1$
            Log.info("Version: "+JAJUK_VERSION);  //$NON-NLS-1$
        } catch (Exception e) {
            Log.stack(e);
        }
    }
    
    /**
     * Return a self instance
     * <p>Implementation of the singleton pattern
     */
    public static synchronized Log getInstance(){
        if ( Log.log == null){
            Log.log = new Log();     	
        }	
        return Log.log;
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
     * @param sCode error code
     * @param sInfosup : error context information
     * @param t the exception itself
     **/
    public static void error(String sCode,String sInfosup,Throwable t){
        String sOut;
        if ( Messages.isInitialized()){
            sOut = '('+sCode+") "+Messages.getErrorMessage(sCode)+ ((sInfosup==null)?"":":"+sInfosup); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        else{
            sOut = '('+sCode+") "+ ((sInfosup==null)?"":":"+sInfosup); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        logger.error(sOut);
    }
    
    /**
     * Log an error-level  message
     * @param t the exception itself
     **/
    public static void error(Throwable t){
        logger.error("",t); //$NON-NLS-1$
    }
    
    /**
     * Log an error-level  message
     * @param sInfosup
     * @param t
     **/
    public static void error(String sCode,Throwable t){
        error(sCode,null,t);
    }
    
    
    /**
     * Log an error-level  message
     * @param sInfosup
     * @param je
     **/
    public static void error(String sInfosup,JajukException je){
        error(je.getCode(),sInfosup,je);
    }
    
    /**
     * Log an error-level  message
     * @param je
     **/
    public static void error(JajukException je){
        error(je.getCode(),null,je);
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
        switch(newVerbosity){
        case DEBUG:
            logger.setLevel(Level.DEBUG);
            loggerHttp.setLevel(Level.DEBUG);
            loggerRoot.setLevel(Level.DEBUG);
            break;
        case INFO:
            logger.setLevel(Level.INFO);
            loggerHttp.setLevel(Level.INFO);
            loggerRoot.setLevel(Level.INFO);
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
    public static void stack(Exception e){
        e.printStackTrace();
        System.out.println();
    }
    
    /**
     * Return whether Log are in debug mode
     * @return
     */
    public static boolean isDebugEnabled(){
        if ( verbosity == Log.DEBUG ){
            return true;
        }
        return false;
    }
}	



