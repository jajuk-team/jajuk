/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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

/**
 * JajukException
 * 
 * @author bflorat
 * @created 06 oct. 2005
 */
public class CannotRenameException extends JajukException {

    private static final long serialVersionUID = 1L;

    /**
         * constructor.
         * 
         * @param pCode
         *                Code of the current error.
         */
    public CannotRenameException(String pCode) {
	super(pCode);
    }

    /**
         * JajukException constructor.
         * 
         * @param pCode
         *                Code of the current error.
         * @param pCause
         *                Original exception of the error.
         */
    public CannotRenameException(String pCode, Throwable pCause) {
	super(pCode, pCause);
    }

}
