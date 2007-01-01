/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
package org.jajuk.util.error;

import org.jajuk.i18n.Messages;

/**
 * JajukException
 * 
 * @author sgringoi
 * @created 5 oct. 2003
 */
public class JajukException extends Exception {
	private static final long serialVersionUID = 1L;

	/** Error code */
	private String code = null;

	/**
	 * JajukException constructor.
	 * 
	 * @param pCode
	 *            Code of the current error.
	 */
	public JajukException(String pCode) {
		this(pCode, null, null);
	}

	/**
	 * JajukException constructor.
	 * 
	 * @param pCode
	 *            Code of the current error.
	 * @param pCause
	 *            Original exception of the error.
	 */
	public JajukException(String pCode, Throwable pCause) {
		this(pCode, null, pCause);
	}

	public String getCode() {
		return this.code;
	}

	/**
	 * JajukException constructor.
	 * 
	 * @param pCode
	 *            Code of the current error.
	 * @param pMessage
	 *            Message.
	 * @param pCause
	 *            Original exception of the error.
	 */
	public JajukException(String pCode, String pMessage, Throwable pCause) {
		super((pMessage != null && pMessage.length() > 0) ? Messages
				.getErrorMessage(pCode)
				+ " : " + pMessage : //$NON-NLS-1$
				Messages.getErrorMessage(pCode), pCause);
		code = pCode;
	}

}
