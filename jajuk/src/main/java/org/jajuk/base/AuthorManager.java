/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage authors.
 */
public final class AuthorManager extends ItemManager {
  
  /** Self instance. */
  private static AuthorManager singleton;

  /* List of all known authors */
  /** DOCUMENT_ME. */
  private static Vector<String> authorsList = new Vector<String>(100);

  /**
   * No constructor available, only static access.
   */
  private AuthorManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(Const.XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(Const.XML_EXPANDED, false, false, false, false,
        true, Boolean.class, false));
    // create author list
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static AuthorManager getInstance() {
    if (singleton == null) {
      singleton = new AuthorManager();
    }
    return singleton;
  }

  /**
   * Register an author.
   * 
   * @param sName DOCUMENT_ME
   * 
   * @return the author
   */
  public Author registerAuthor(String sName) {
    String sId = createID(sName);
    return registerAuthor(sId, sName);
  }

  /**
   * Return hashcode for this item.
   * 
   * @param sName item name
   * 
   * @return ItemManager ID
   */
  protected static String createID(String sName) {
    return MD5Processor.hash(sName);
  }

  /**
   * Register an author with a known id.
   * 
   * @param sName DOCUMENT_ME
   * @param sId DOCUMENT_ME
   * 
   * @return the author
   */
  public synchronized Author registerAuthor(String sId, String sName) {
    Author author = getAuthorByID(sId);
    if (author != null) {
      return author;
    }
    author = new Author(sId, sName);
    registerItem(author);
    // add it in styles list if new
    if (!authorsList.contains(sName)) {
      authorsList.add(author.getName2());
      // Sort items ignoring case
      Collections.sort(authorsList, new Comparator<String>() {
        public int compare(String o1, String o2) {
          return o1.compareToIgnoreCase(o2);
        }
      });
    }

    return author;
  }

  /**
   * Change the item name.
   * 
   * @param old DOCUMENT_ME
   * @param sNewName DOCUMENT_ME
   * 
   * @return new album
   * 
   * @throws JajukException the jajuk exception
   */
  public Author changeAuthorName(Author old, String sNewName) throws JajukException {
    synchronized (TrackManager.getInstance()) {
      // check there is actually a change
      if (old.getName2().equals(sNewName)) {
        return old;
      }

      // find out if the QueueModel is playing this track before we change the track!
      boolean queueNeedsUpdate = false;
      if (QueueModel.getPlayingFile() != null
          && QueueModel.getPlayingFile().getTrack().getAuthor().equals(old)) {
        queueNeedsUpdate = true;
      }
      
      Author newItem = registerAuthor(sNewName);
      // re apply old properties from old item
      newItem.cloneProperties(old);

      // update tracks
      for (Track track : TrackManager.getInstance().getTracks()) {
        if (track.getAuthor().equals(old)) {
          TrackManager.getInstance().changeTrackAuthor(track, sNewName, null);
        }
      }

      // if current track author name is changed, notify it
      if (queueNeedsUpdate) {
        ObservationManager.notify(new JajukEvent(JajukEvents.AUTHOR_CHANGED));
      }

      return newItem;
    }
  }

  /**
   * Format the author name to be normalized :
   * <p>
   * -no underscores or other non-ascii characters
   * <p>
   * -no spaces at the begin and the end
   * <p>
   * -All in lower case expect first letter of first word
   * <p>
   * exemple: "My author".
   * 
   * @param sName DOCUMENT_ME
   * 
   * @return the string
   * 
   * TODO: the "all lowercase" part is not done currently, should this be changed??
   */
  public static String format(String sName) {
    String sOut;
    sOut = sName.trim(); // suppress spaces at the begin and the end
    sOut = sOut.replace('-', ' '); // move - to space
    sOut = sOut.replace('_', ' '); // move _ to space
    char c = sOut.charAt(0);
    StringBuilder sb = new StringBuilder(sOut);
    sb.setCharAt(0, Character.toUpperCase(c));
    return sb.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.base.ItemManager#getIdentifier()
   */
  @Override
  public String getLabel() {
    return Const.XML_AUTHORS;
  }

  /**
   * Gets the authors list.
   * 
   * @return authors as a string list (used for authors combos)
   */
  public static Vector<String> getAuthorsList() {
    return authorsList;
  }

  /**
   * Gets the author by id.
   * 
   * @param sID Item ID
   * 
   * @return Element
   */
  public Author getAuthorByID(String sID) {
    return (Author) getItemByID(sID);
  }

  /**
   * Gets the authors.
   * 
   * @return ordered albums list
   */
  @SuppressWarnings("unchecked")
  public List<Author> getAuthors() {
    return (List<Author>) getItems();
  }

  /**
   * Gets the authors iterator.
   * 
   * @return authors iterator
   */
  @SuppressWarnings("unchecked")
  public synchronized ReadOnlyIterator<Author> getAuthorsIterator() {
    return new ReadOnlyIterator<Author>((Iterator<Author>) getItemsIterator());
  }

  /**
   * Get ordered list of authors associated with this item.
   * 
   * @param item DOCUMENT_ME
   * 
   * @return the associated authors
   */
  public synchronized List<Author> getAssociatedAuthors(Item item) {
    List<Author> out;
    if (item instanceof Track) {
      out = new ArrayList<Author>(1);
      out.add(((Track) item).getAuthor());
    } else {
      // [Perf] If item is a track, just return its author
      // Use a set to avoid dups
      Set<Author> authorSet = new HashSet<Author>();

      List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item, true);
      for (Track track : tracks) {
        authorSet.add(track.getAuthor());
      }
      out = new ArrayList<Author>(authorSet);
      Collections.sort(out);
    }
    return out;
  }

  /**
   * Gets the author by name.
   * 
   * @param name DOCUMENT_ME
   * 
   * @return associated author (case insensitive) or null if no match
   */
  public Author getAuthorByName(String name) {
    Author out = null;
    for (ReadOnlyIterator<Author> it = getAuthorsIterator(); it.hasNext();) {
      Author author = it.next();
      if (author.getName().equals(name)) {
        out = author;
        break;
      }
    }
    return out;
  }

}
