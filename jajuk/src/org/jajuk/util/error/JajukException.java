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
 * Revision 1.1  2003/10/07 21:02:19  bflorat
 * Initial commit
 *
 */
package org.jajuk.util.error;

import org.jajuk.util.ConfigurationManager;

/**
 * JajukException
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public class JajukException extends Exception {
	/** Error code */
	private String code = null;
	/** Error message */
	private String message = null;
	
	/**
	 * JajukException constructor.
	 * 
	 * @param pCode Code of the current error.
	 */
	public JajukException(String pCode) {
		super();
		this.
		code = pCode;
	}

	/**
	 * JajukException constructor.
	 * 
	 * @param pCode Code of the current error.
	 * @param pCause Original exception of the error.
	 */
	public JajukException(String pCode, Throwable pCause) {
		super(pCause);
		
		code = pCode;
	}
	
	/**
	 * JajukException constructor.
	 * 
	 * @param pCode Code of the current error.
	 * @param pMessage Message.
	 * @param pCause Original exception of the error.
	 */
	public JajukException(String pCode, String pMessage, Throwable pCause) {
		super(pCause);
		
		code = pCode;
		message = pMessage;
	}
	
	/**
	 * Display the error to the user.
	 * 
	 * @return void 
	 */
	public void display() {
		ErrorWindow e = new ErrorWindow(this);
		e.setVisible(true);
	}
	
	/*
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage() {
		return ConfigurationManager.getErrorMessage(code);
	}
	/**
	 * @see java.lang.Throwable#getLocalizedMessage()
	 */
	public String getLocalizedMessage() {
		return getMessage() + message;
	}

}
