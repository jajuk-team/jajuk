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
 * $Revision$
 *
 */
package org.jajuk.util.error;

import org.jajuk.i18n.Messages;

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
	
	
	public String getCode(){
		return this.code;
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
	 * @see java.lang.Throwable#getLocalizedMessage()
	 */
	public String getLocalizedMessage() {
		return getMessage() + message;
	}

}
