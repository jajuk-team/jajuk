/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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

import java.util.List;

import javax.swing.ImageIcon;

import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.tags.ITagImpl;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Music type.
 */
public class Type extends PhysicalItem implements Comparable<Type> {

  /** Type extension ex:mp3,ogg. */
  private final String sExtension;

  /** Player impl. */
  private final Class<ITagImpl> cTagImpl;

  /** Player class. */
  private final Class<IPlayerImpl> cPlayerImpl;

  /**
   * Constructor.
   *
   * @param sId type id if given
   * @param sName type name
   * @param sExtension type file extension (.mp3...)
   * @param cPlayerImpl DOCUMENT_ME
   * @param cTagImpl DOCUMENT_ME
   */
  Type(final String sId, final String sName, final String sExtension,
      final Class<IPlayerImpl> cPlayerImpl, final Class<ITagImpl> cTagImpl) {
    super(sId, sName);
    this.cPlayerImpl = cPlayerImpl;
    this.sExtension = sExtension;
    setProperty(Const.XML_TYPE_EXTENSION, sExtension);
    setProperty(Const.XML_TYPE_PLAYER_IMPL, cPlayerImpl);
    this.cTagImpl = cTagImpl;
    if (cTagImpl != null) { // can be null for playlists
      setProperty(Const.XML_TYPE_TAG_IMPL, cTagImpl);
    }
  }

  /**
   * Alphabetical comparator used to display ordered lists.
   * 
   * @param other item to be compared
   * 
   * @return comparison result
   */
  @Override
  public int compareTo(final Type other) {
    return toString().compareTo(other.toString());
  }

  /**
   * Get item description.
   * 
   * @return the desc
   */
  @Override
  public String getDesc() {
    return Messages.getString("Type") + " : " + getName();
  }

  /**
   * Gets the extension.
   * 
   * @return the extension
   */
  public String getExtension() {
    return sExtension;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIconRepresentation()
   */
  @Override
  public ImageIcon getIconRepresentation() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.Item#getIdentifier()
   */
  @Override
  public final String getXMLTag() {
    return XML_TYPE;
  }

  /**
   * Gets the player class.
   * 
   * @return Player class for this type
   */
  public Class<IPlayerImpl> getPlayerClass() {
    return cPlayerImpl;
  }

  /**
   * Gets the tagger class.
   * 
   * @return Tagger class for this type
   */
  public Class<ITagImpl> getTaggerClass() {
    return cTagImpl;
  }

  /**
   * Gets the tag impl.
   * 
   * @return Returns the tagImpl.
   */
  public ITagImpl getTagImpl() {
    try {
      if (cTagImpl == null) {
        return null;
      }
      return cTagImpl.newInstance();
    } catch (final Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return "Type[ID=" + getID() + " Name=" + getName() + " ; Extension=" + sExtension + "]";
  }

  /**
   * Return an array of supported file extensions for given types.
   * 
   * @param types the types
   * 
   * @return Return an array of supported file extensions
   */
  public static String[] getExtensionsFromTypes(final List<Type> types) {
    String[] extensions = {};

    if (types != null) {
      final int typesSize = types.size();
      final Type[] typesArray = types.toArray(new Type[typesSize]);

      extensions = new String[typesSize];
      for (int i = 0; i < typesSize; i++) {
        extensions[i] = typesArray[i].getExtension();
      }
    }
    return extensions;
  }

  /**
   * Return whether this type is video.
   * 
   * @return whether this type is video
   */
  public boolean isVideo() {
    return Messages.getString(Const.TYPE_VIDEO).equals(getName());
  }

}
