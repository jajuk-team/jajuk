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

package org.jajuk.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jajuk.ui.perspectives.IPerspective;

/**
 * View Factory, creates view item and manages their ID.
 */
public final class ViewFactory {

  /**
   * No instantiation *.
   */
  private ViewFactory() {
  }

  /**
   * Create a new view instance.
   * 
   * @param className view class
   * @param perspective view perspective
   * @param id integer id used as vldocking key id
   * 
   * @return the created view
   */
  public static IView createView(Class<?> className, IPerspective perspective, int id) {
    IView view;
    try {
      view = (IView) className.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    view.setID(className.getName() + '/' + id);
    view.setPerspective(perspective);

    return view;
  }

  /**
   * Gets the known views.
   * 
   * @return All known views sorted by name
   * 
   * @TODO Refactor this, the known views should be get by reflection (from the
   * default perspectives XML file ?)
   */
  @SuppressWarnings("unchecked")
  public static List<Class<? extends IView>> getKnownViews() {
    List<Class<? extends IView>> out = new ArrayList<Class<? extends IView>>();
    // Take one instance of each set of view instances mapped to each view
    // classname
    out.add(AnimationView.class);
    out.add(CatalogView.class);
    out.add(CDScanView.class);
    out.add(CoverView.class);
    out.add(DeviceView.class);
    out.add(TracksTableView.class);
    out.add(TracksTreeView.class);
    out.add(PlaylistView.class);
    out.add(LyricsView.class);
    out.add(ParameterView.class);
    out.add(FilesTableView.class);
    out.add(FilesTreeView.class);
    out.add(StatView.class);
    out.add(SuggestionView.class);
    out.add(WikipediaView.class);
    out.add(AlbumsTableView.class);
    out.add(QueueView.class);
    out.add(ArtistView.class);
    out.add(WebRadioView.class);
    Collections.sort(out, new Comparator() {
      @Override
      public int compare(Object view1, Object view2) {
        String s1;
        String s2;
        try {
          s1 = ((IView) ((Class) view1).newInstance()).getDesc();
          s2 = ((IView) ((Class) view2).newInstance()).getDesc();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        return s1.compareTo(s2);
      }
    });
    return out;
  }
}
