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
package org.jajuk.base;

import org.jajuk.util.Util;

/**
 * Music type 
 *
 * @author     bflorat
 * @created    12 oct. 2003
 */
public class Type extends PropertyAdapter{
	
	/**Type id*/
	private String sId;
	/**Type name ( description)*/
	private String sName;
	/**Type extension ex:mp3,ogg */
	private String sExtension;
	/**Type player implementation */
	private IPlayerImpl playerImpl;
	/**Type tag implementation, null if it is not a music type */
	private ITagImpl tagImpl;
	/**True if the type is a music files and false if it is a playlist-type file*/
	private boolean bIsMusic;
	
	
	public Type(String sId, String sName,String sExtension, String sPlayerImpl,String sTagImpl,boolean bIsMusic) throws Exception{
		this.sExtension = sExtension;
		this.sName = sName;
		this.playerImpl = (IPlayerImpl)Class.forName(sPlayerImpl).newInstance();
		if (sTagImpl != null){  //can be null for playlists
			this.tagImpl = (ITagImpl)Class.forName(sTagImpl).newInstance();
		}
		this.sId = sId;
		this.bIsMusic = bIsMusic;
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
		sb.append(Util.formatXML(sName)).append("' extension='");
		sb.append(sExtension).append("' player_impl='");
		sb.append(playerImpl.getClass().getName()).append("' tag_impl='");
		sb.append((tagImpl==null)?"":tagImpl.getClass().getName()).append("' music='");
		sb.append(bIsMusic).append("'/>\n");
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
	public boolean equals(Object otherType){
		return this.getId().equals(((Type)otherType).getId() );
	}	

	/**
	 * @return Returns the bIsMusic.
	 */
	public boolean isMusic() {
		return bIsMusic;
	}

	/**
	 * @return Returns the tagImpl.
	 */
	public ITagImpl getTagImpl() {
		return tagImpl;
	}

}
