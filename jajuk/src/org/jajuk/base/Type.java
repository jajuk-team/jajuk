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
 * Revision 1.3  2003/10/21 17:51:43  bflorat
 * 21/10/2003
 *
 * Revision 1.2  2003/10/17 20:36:45  bflorat
 * 17/10/2003
 *
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
	
	private String sId;
	private String sName;
	private String sExtension;
	private IPlayerImpl playerImpl;
	
	
	public Type(String sId, String sName,String sExtension, String sPlayerImpl) throws Exception{
		this.sExtension = sExtension;
		this.sName = sName;
		this.playerImpl = (IPlayerImpl)Class.forName(sPlayerImpl).newInstance();
		this.sId = sId;
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
	 * toString method
	 */
	public String toString(){
			return "Type[ID="+sId+" Name="+getName()+ " ; Extension="+sExtension+"]";	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * Return an XML representation of this item  
	 * @return
	 */
	public String toXml(){
		StringBuffer sb = new StringBuffer("\t\t<type id='" +sId);
		sb.append("' name='");
		sb.append(sName).append("' extension='");
		sb.append(sExtension).append("'/>\n");
		return sb.toString();
	}

	/**
	 * @return
	 */
	public String getId() {
		return sId;
	}
	
	/**
	 * Equal method to check two types are identical
	 * @param otherType
	 * @return
	 */
	public boolean equals(Type otherType){
		return sName.equals(otherType.getName());
	}	

}
