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
 * Music type 
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class Type {
	
	private String sName;
	private String sExtension;
	private IPlayerImpl playerImpl;
	
	
	public Type(String sName,String sExtension, String sPlayerImpl) throws Exception{
		this.sExtension = sExtension;
		this.sName = sName;
		this.playerImpl = (IPlayerImpl)Class.forName(sPlayerImpl).newInstance();
	}


	/**
	 * @return
	 */
	public IPlayerImpl getPlayerImpl() {
		return playerImpl;
	}

	/**
	 * @return
	 */
	public String getExtension() {
		return sExtension;
	}

	/**
	 * @return
	 */
	public String getName() {
		return sName;
	}

	
	/**
	 * @param string
	 */
	public void setExtension(String string) {
		sExtension = string;
	}

	/**
	 * @param string
	 */
	public void setSName(String string) {
		sName = string;
	}
	
	public String toString(){
			return "Type="+getName()+ " ; Extension="+sExtension;	
		}

}
