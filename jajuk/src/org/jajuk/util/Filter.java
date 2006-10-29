/*
 *  Jajuk
 *  Copyright (C) 2004 bflorat
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

package org.jajuk.util;

import org.jajuk.base.PropertyMetaInformation;

/**
 * Filter on meta information
 * 
 * @author Bertrand Florat
 * @created 7 déc. 2005
 */
public class Filter {

    /** Property */
    PropertyMetaInformation meta;

    /** Value* */
    String sValue;

    /** Human* */
    boolean bHuman = false;

    /** Exact* */
    boolean bExact = false;

    /**
         * Filter constructor
         * 
         * @param meta
         *                meta property
         * @param sValue
         *                value
         * @param bHuman
         *                is the filter apply value itself or its human
         *                representation if different ?
         * @param bExact
         *                is the filter should match exactly the value ?
         */
    public Filter(PropertyMetaInformation meta, String sValue, boolean bHuman,
	    boolean bExact) {
	this.meta = meta;
	this.sValue = sValue;
	this.bHuman = bHuman;
	this.bExact = bExact;
    }

    public boolean isExact() {
	return bExact;
    }

    public boolean isHuman() {
	return bHuman;
    }

    public PropertyMetaInformation getProperty() {
	return meta;
    }

    public String getValue() {
	return sValue;
    }
}
