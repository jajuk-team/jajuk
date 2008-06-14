/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.jajuk.ui.perspectives.IPerspective;

/**
 * View Factory, creates view item and manages their ID
 */
public final class ViewFactory {

  /** Maps view class -> view instances set */
  private static Map<Class<?>, Set<IView>> hmClassesInstances = new HashMap<Class<?>, Set<IView>>();

  private static Random random = new Random();

  /**
   * No instantiation *
   */
  private ViewFactory() {
  }

  /**
   * Create a new view instance
   * 
   * @param className
   * @return
   */
  public static IView createView(Class<?> className, IPerspective perspective) {
    Set<IView> views = hmClassesInstances.get(className);
    if (views == null) {
      views = new LinkedHashSet<IView>();
      hmClassesInstances.put(className, views);
    }
    IView view;
    try {
      view = (IView) className.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    // Set ID using a random number to discriminate same views (same view in
    // the same perspective are in different perspectives)
    // do not use sequential numbers as the serialization views order is not
    // deterministic
    // and it may conduct VLDocking to ignore some views if XXX/3 is parsed
    // before XXX/2 for ie
    view.setID(className.getName() + '/' + (int) (Integer.MAX_VALUE * random.nextDouble()));
    view.setPerspective(perspective);
    // store the new view
    views.add(view);
    return view;
  }

  /**
   * 
   * @return All known views sorted by name
   * @TODO Refactor this, the known views should be get by reflection (from the
   *       default perspectives XML file ?)
   */
  @SuppressWarnings("unchecked")
  public static List<Class> getKnownViews() {
    List<Class> out = new ArrayList<Class>();
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
    Collections.sort(out, new Comparator() {
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
