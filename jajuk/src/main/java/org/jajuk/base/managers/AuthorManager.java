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

package org.jajuk.base.managers;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ItemType;
import org.jajuk.base.MetaProperty;
import org.jajuk.base.items.Author;
import org.jajuk.base.items.Item;
import org.jajuk.base.items.Track;
import org.jajuk.util.EventSubject;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.Resources.XML;
import org.jajuk.util.error.JajukException;

/**
 * Convenient class to manage authors
 */
public class AuthorManager extends ItemManager<Author> {
  /** Self instance */
  private static AuthorManager singleton;

  /* List of all known authors */
  public static Vector<String> authorsList;

  /**
   * No constructor available, only static access
   */
  public AuthorManager() {
    super();
    // register properties
    // ID
    registerProperty(new MetaProperty(XML.ID, false, true, false, false, false,
        String.class, null));
    // Name
    registerProperty(new MetaProperty(XML.NAME, false, true, true, true, false,
        String.class, null));
    // Expand
    registerProperty(new MetaProperty(XML.EXPANDED, false, false, false, false, true,
        Boolean.class, false));
    // create author list
    authorsList = new Vector<String>(100);
  }

  /**
   * Register an author
   *
   * @param sName
   */
  public Author registerAuthor(final String sName) {
    final String sId = createID(sName);
    return registerAuthor(sId, sName);
  }

  /**
   * Return hashcode for this item
   *
   * @param sName
   *          item name
   * @return ItemManager ID
   */
  public static String createID(final String sName) {
    return MD5Processor.hash(sName);
  }

  /**
   * Register an author with a known id
   *
   * @param sName
   */
  public Author registerAuthor(final String sId, final String sName) {
    synchronized (getLock()) {
      Author author = getAuthorByID(sId);

      if (author == null) {
        author = new Author(sId, sName);
        getItems().put(sId, author);
        // add it in styles list if new
        if (!authorsList.contains(sName)) {
          authorsList.add(author.getName2());
        }
        // Sort items ignoring case
        Collections.sort(authorsList, new Comparator<String>() {

          public int compare(final String o1, final String o2) {
            return o1.compareToIgnoreCase(o2);
          }

        });
      }
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
  public Author changeAuthorName(final Author old, final String sNewName) throws JajukException {
    synchronized (((TrackManager) ItemType.Track.getManager()).getLock()) {
      // check there is actually a change
      if (old.getName2().equals(sNewName)) {
        return old;
      }
      final Author newItem = registerAuthor(sNewName);
      // re apply old properties from old item
      newItem.cloneProperties(old);
      // update tracks
      for (final Track track : ((TrackManager) ItemType.Track.getManager()).getCarbonItems()) {
        if (track.getAuthor().equals(old)) {
          ((TrackManager) ItemType.Track.getManager()).changeTrackAuthor(track, sNewName, null);
        }
      }
      // if current track author name is changed, notify it
      if ((FIFO.getInstance().getCurrentFile() != null)
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
  public static String format(final String sName) {
    String sOut;
    sOut = sName.trim(); // suppress spaces at the begin and the end
    sOut = sOut.replace('-', ' '); // move - to space
    sOut = sOut.replace('_', ' '); // move _ to space
    final char c = sOut.charAt(0);
    final StringBuilder sb = new StringBuilder(sOut);
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
    return XML.AUTHORS;
  }

  /**
   *
   * @return authors as a string list (used for authors combos)
   */
  public static synchronized Vector<String> getAuthorsList() {
    synchronized (ItemType.Author.getManager().getLock()) {
      return authorsList;
    }
  }

  /**
   * @param sID
   *          Item ID
   * @return Element
   */
  public Author getAuthorByID(final String sID) {
    synchronized (getLock()) {
      return getItems().get(sID);
    }
  }

  /**
   * Get authors associated with this item
   *
   * @param item
   * @return
   */
  public Set<Author> getAssociatedAuthors(final Item item) {
    synchronized (getLock()) {
      final Set<Author> out = new TreeSet<Author>();
      // If item is a track, return Authors containing this track
      if (item instanceof Track) {
        // we can return as a track has only one Author
        if (item != null) {
          out.add(((Track) item).getAuthor());
        }
      } else {
        final Set<Track> tracks = ((TrackManager) ItemType.Track.getManager()).getAssociatedTracks(item);
        for (final Track track : tracks) {
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
  public Author getAuthorByName(final String name) {
    Author out = null;
    for (final Author author : getCarbonItems()) {
      if (author.getName().trim().toLowerCase().matches(name.trim().toLowerCase())) {
        out = author;
        break;
      }
    }
    return out;
  }

}
