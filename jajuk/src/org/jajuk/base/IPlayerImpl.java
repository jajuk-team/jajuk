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
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.base;

/**
 *  Minimum methods required for all Player implementations
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public interface IPlayerImpl {
		
		public void play(String sReference) throws Exception;

		public void stop() throws Exception;
		
		public boolean isComplete() throws Exception;
}
