/*
 *  Jajuk
 *  Copyright (C) 2004 The Jajuk Team
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

package org.jajuk.dj;

import org.jajuk.base.Style;

import java.util.HashSet;

/**
 * Represent a style proportion (used by digital DJs)
 */
public class Proportion {
	/** styles */
	private Ambience ambience;

	/** Proportion* */
	private float proportion;

	/**
	 * Constructor
	 * 
	 * @param style
	 *            styles
	 * @param proportion
	 *            style proportion in %. Ex: 0.1
	 */
	public Proportion(Ambience ambience, float proportion) {
		this.ambience = ambience;
		this.proportion = proportion;
	}

	/**
	 * Constructor for void proportion
	 */
	public Proportion() {
		this.ambience = new Ambience(Long.toString(System.currentTimeMillis()),
				""); //$NON-NLS-1$
		this.proportion = 0.2f;
	}

	/**
	 * equals method
	 * 
	 * @return whether two object are equals
	 */
	public boolean equals(Object other) {
		if (other == null || !(other instanceof Proportion)) {
			return false;
		}
		return getProportion() == ((Proportion) other).getProportion()
				&& getStyles().equals(((Proportion) other).getStyles());
	}

	/**
	 * @return Returns the styles
	 */
	public HashSet<Style> getStyles() {
		return this.ambience.getStyles();
	}

	/**
	 * Add a style
	 */
	public void addStyle(Style style) {
		ambience.addStyle(style);
	}

	/**
	 * @return String representation of this proportion
	 */
	public String toString() {
		return "" + proportion; //$NON-NLS-1$
	}

	/**
	 * From String, return style1,style2,...
	 */
	public String getStylesDesc() {
		String out = ""; //$NON-NLS-1$
		for (Style s : ambience.getStyles()) {
			out += s.getName2() + ',';
		}
		if (out.length() > 0) {
			out = out.substring(0, out.length() - 1); // remove trailling ,
		}
		return out;
	}

	/**
	 * 
	 * @return next style to be played or null if no idea
	 */
	public Style getNextStyle() {
		return null;
	}

	public float getProportion() {
		return this.proportion;
	}

	public void setStyle(Ambience ambience) {
		this.ambience = ambience;
	}

	public void setProportion(float proportion) {
		this.proportion = proportion;
	}
}
