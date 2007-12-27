/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 **/

package org.jajuk.base;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage authors
 */
public class AuthorManager extends ItemManager {
  /** Self instance */
  private static AuthorManager singleton;

  /* List of all known authors */
  public static Vector<String> authorsList;

  /**
   * No constructor available, only static access
   */
  private AuthorManager() {
    super();
    // register properties
    // ID
    registerProperty(new PropertyMetaInformation(XML_ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new PropertyMetaInformation(XML_NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new PropertyMetaInformation(XML_EXPANDED, false, false, false, false, true,
        Boolean.class, false));
    // create author list
    authorsList = new Vector<String>(100);
  }

  /**
   * @return singleton
   */
  public static AuthorManager getInstance() {
    if (singleton == null) {
      singleton = new AuthorManager();
    }
    return singleton;
  }

  /**
   * Register an author
   * 
   * @param sName
   */
  public Author registerAuthor(String sName) {
    String sId = createID(sName);
    return registerAuthor(sId, sName);
  }

  /**
   * Return hashcode for this item
   * 
   * @param sName
   *          item name
   * @return ItemManager ID
   */
  protected static String createID(String sName) {
    return MD5Processor.hash(sName);
  }

  /**
   * Register an author with a known id
   * 
   * @param sName
   */
  public Author registerAuthor(String sId, String sName) {
    synchronized (AuthorManager.getInstance().getLock()) {
      Author author = (Author) hmItems.get(sId);
      if (author != null) {
        return author;
      }
      author = new Author(sId, sName);
      hmItems.put(sId, author);
      // add it in styles list if new
      if (!authorsList.contains(sName)) {
        authorsList.add(author.getName2());
      }
      // Sort items ignoring case
      Collections.sort(authorsList, new Comparator<String>() {

        public int compare(String o1, String o2) {
          return o1.compareToIgnoreCase(o2);
        }

      });
      return author;
    }
  }

  /**
   * Change the item name
   * 
   * @param old
   * @param sNewName
   * @return new album
   */
  public Author changeAuthorName(Author old, String sNewName) throws JajukException {
    synchronized (TrackManager.getInstance().getLock()) {
      // check there is actually a change
      if (old.getName2().equals(sNewName)) {
        return old;
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
      if (FIFO.getInstance().getCurrentFile() != null
          && FIFO.getInstance().getCurrentFile().getTrack().getAuthor().equals(old)) {
        ObservationManager.notify(new Event(EventSubject.EVENT_AUTHOR_CHANGED));
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
   * -All in lower cas expect first letter of first word
   * <p>
   * exemple: "My author"
   * 
   * @param sName
   * @return
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
  public String getLabel() {
    return XML_AUTHORS;
  }

  /**
   * 
   * @return authors as a string list (used for authors combos)
   */
  public static synchronized Vector<String> getAuthorsList() {
    synchronized (getInstance().getLock()) {
      return authorsList;
    }
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Author getAuthorByID(String sID) {
    synchronized (getLock()) {
      return (Author) hmItems.get(sID);
    }
  }

  /**
   * 
   * @return albums list
   */
  public Set<Author> getAuthors() {
    Set<Author> authorSet = new LinkedHashSet<Author>();
    synchronized (getLock()) {
      for (Item item : getItems()) {
        authorSet.add((Author) item);
      }
    }
    return authorSet;
  }

  /**
   * Get authors associated with this item
   * 
   * @param item
   * @return
   */
  public Set<Author> getAssociatedAuthors(Item item) {
    synchronized (AuthorManager.getInstance().getLock()) {
      Set<Author> out = new TreeSet<Author>();
      // If item is a track, return Authors containing this track
      if (item instanceof Track) {
        // we can return as a track has only one Author
        if (item != null) {
          out.add(((Track) item).getAuthor());
        }
      } else {
        Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item);
        for (Track track : tracks) {
          out.add(track.getAuthor());
        }
      }
      return out;
    }
  }

  /**
   * @param name
   * @return associated author (case insensitive) or null if no match
   */
  public Author getAuthorByName(String name) {
    Author out = null;
    for (Author author : getAuthors()) {
      if (author.getName().trim().toLowerCase().matches(name.trim().toLowerCase())) {
        out = author;
        break;
      }
    }
    return out;
  }

}
