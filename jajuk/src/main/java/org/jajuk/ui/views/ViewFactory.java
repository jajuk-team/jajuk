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

import org.jajuk.ui.IPerspective;
import org.jajuk.ui.IView;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * View Factory, creates view item and manages their ID
 */
public class ViewFactory {

	/** Maps view class -> view instances set */
	private static HashMap<Class, Set<IView>> hmClassesInstances = new HashMap<Class, Set<IView>>();

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
	public static IView createView(Class className, IPerspective perspective) {
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
		view.setID(className.getName() + '/' + (int) (Integer.MAX_VALUE * Math.random()));
		view.setPerspective(perspective);
		// store the new view
		views.add(view);
		return view;
	}

	/**
	 * 
	 * @return All known views sorted by name
	 * @TODO Refactor this, the known views should be get by reflection (from the default perspectives XML file ?) 
	 */
	@SuppressWarnings("unchecked")
	public static Set<Class> getKnownViews() {
		Set<Class> out = new LinkedHashSet<Class>();
		// Take one instance of each set of view instances mapped to each view
		// classname
		out.add(AboutView.class);
		out.add(AnimationView.class);
		out.add(CatalogView.class);
		out.add(CDScanView.class);
		out.add(CoverView.class);
		out.add(DeviceView.class);
		out.add(HelpView.class);
		out.add(LogicalTableView.class);
		out.add(LogicalTreeView.class);
		out.add(PlaylistEditorView.class);
		out.add(LogicalPlaylistRepositoryView.class);
		out.add(LyricsView.class);
		out.add(ParameterView.class);
		out.add(PhysicalPlaylistRepositoryView.class);
		out.add(PhysicalTableView.class);
		out.add(PhysicalTreeView.class);
		out.add(StatView.class);
		out.add(SuggestionView.class);
		out.add(WikipediaView.class);
		return out;
	}

}
