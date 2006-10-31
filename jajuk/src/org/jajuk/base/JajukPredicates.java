/*
 *  Jajuk
 *  Copyright (C) 2006 bflorat
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

package org.jajuk.base;

import java.util.Date;

import org.apache.commons.collections.Predicate;
import org.jajuk.util.ITechnicalStrings;

/**
 * List of Predicates (filter criteria)
 * <p>
 * Returns predicates used to decorate iterators
 * </p>
 * 
 * @author Bertrand Florat
 * @created 15 oct. 06
 */
public class JajukPredicates {

	/**
	 * 
	 * Age-filtering predicate Applied on tracks only
	 * 
	 * @author Bertrand Florat
	 * @created 15 oct. 06
	 */
	public static class AgePredicate implements Predicate {

		private int iAge = 0;

		public AgePredicate(int iAge) {
			this.iAge = iAge;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
		 */
		public boolean evaluate(Object o) {
			if (!(o instanceof Track)) {
				return false;
			}
			Track track = (Track) o;
			Date now = new Date();
			int iTrackAge = (int) ((now.getTime() - track.getAdditionDate()
					.getTime()) / ITechnicalStrings.MILLISECONDS_IN_A_DAY);
			if (iTrackAge <= iAge) {
				return true;
			}
			return false;
		}

	}

	/**
	 * 
	 * Ready (mounted) filering predicate Applied on files only
	 * 
	 * @author Bertrand Florat
	 * @created 15 oct. 06
	 */
	public static class ReadyFilePredicate implements Predicate {

		private int iAge = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
		 */
		public boolean evaluate(Object o) {
			if (!(o instanceof File)) {
				return false;
			}
			File file = (File) o;
			return (file != null && file.isReady());
		}

	}

}
