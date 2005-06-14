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
package org.jajuk.base;

import org.jajuk.i18n.Messages;
import org.jajuk.players.IPlayerImpl;
import org.jajuk.tag.ITagImpl;

/**
 * Music type 
 *
 * @author     Bertrand Florat
 * @created    12 oct. 2003
 */
public class Type extends PropertyAdapter{
	
	/**Type extension ex:mp3,ogg */
	private String sExtension;
	/**Type player implementation */
	private IPlayerImpl playerImpl;
	/**Type tag implementation, null if it is not a music type */
	private ITagImpl tagImpl;
   	
	/**
	 * Constructor
	 * @param sId type id if given
	 * @param sName type name 
	 * @param sExtension type file extension (.mp3...)
	 * @param sPlayerImpl Type player implementation class
	 * @param sTagImpl Type Tagger implementation class
	 * @throws Exception
	 */
	public Type(String sId, String sName,String sExtension, String sPlayerImpl,String sTagImpl) throws Exception{
        super(sId,sName);
        setExtension(sExtension);
        setPlayerImpl(sPlayerImpl);
        setTagImpl(sTagImpl);
    }

/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#getIdentifier()
     */
    public String getIdentifier() {
        return XML_TYPE;
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
	 * toString method
	 */
	public String toString(){
			return "Type[ID="+sId+" Name="+getName()+ " ; Extension="+sExtension+"]";	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
	 * hashcode ( used by the equals method )
	 */
	public int hashCode(){
		return getId().hashCode();
	}


	/**
	 * @return Returns the tagImpl.
	 */
	public ITagImpl getTagImpl() {
		return this.tagImpl;
	}

    /**
     * @param playerImpl The playerImpl to set.
     */
    public void setPlayerImpl(String sPlayerImpl) throws Exception{
        this.playerImpl = (IPlayerImpl)Class.forName(sPlayerImpl).newInstance();
        setProperty(XML_TYPE_PLAYER_IMPL,sPlayerImpl);
    }

    /**
     * @param extension The sExtension to set.
     */
    public void setExtension(String extension) {
        sExtension = extension;
        setProperty(XML_TYPE_EXTENSION,extension);
    }

    /**
     * @param id The sId to set.
     */
    public void setId(String id) {
        sId = id;
        setProperty(XML_ID,id);
    }

    /**
     * @param name The sName to set.
     */
    public void setName(String name) {
        sName = name;
        setProperty(XML_NAME,name);
    }

    /**
     * @param tagImpl The tagImpl to set.
     */
    public void setTagImpl(String sTagImpl) throws Exception{
        if (sTagImpl != null){  //can be null for playlists
            this.tagImpl = (ITagImpl)Class.forName(sTagImpl).newInstance();
        }
        setProperty(XML_TYPE_TAG_IMPL,sTagImpl);
    }

    /**
     * Get item description
     */
    public String getDesc(){
        return "<HTML><b>"+Messages.getString("Type")+" : "+getName()+"</b><HTML>";
    }

/* (non-Javadoc)
     * @see org.jajuk.base.IPropertyable#isPropertyEditable()
     */
    public boolean isPropertyEditable(String sProperty){
        if (XML_ID.equals(sProperty)){
            return false;
        }
        else if (XML_NAME.equals(sProperty)){
            return true;
        }
        else if (XML_TYPE_EXTENSION.equals(sProperty)){
            return false;
        }
        else if (XML_TYPE_PLAYER_IMPL.equals(sProperty)){
            return false;
        }
        else if (XML_TYPE_TAG_IMPL.equals(sProperty)){
            return false;
        }
        else if (XML_TYPE_ICON.equals(sProperty)){
            return true;
        }
        else if (XML_TYPE_SEEK_SUPPORTED.equals(sProperty)){
            return false;
        }
        else if (XML_TYPE_TECH_DESC.equals(sProperty)){
            return true;
        }
        else if (XML_TYPE_IS_MUSIC.equals(sProperty)){
            return false;
        }
        else{
            return true;
        }
    }    

    
}
