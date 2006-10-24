/*
 *  Jajuk
 *  Copyright (C) 2006 bflorat
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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jajuk.ui.IPerspective;
import org.jajuk.ui.IView;

/**
 *  View Factory, creates view item and manages their ID
 *
 * @author     Bertrand Florat
 * @created    22 oct. 06
 */
public class ViewFactory {
    
    
    /**Maps view class -> view instances set*/
    private static HashMap<Class, Set<IView>> hmClassesInstances 
        = new HashMap<Class, Set<IView>>();

    
    /**
     * No instanciation  *
     */
    private ViewFactory(){
    }
    
    /**
     * Create a new view instance
     * @param className
     * @return
     */
    public static IView createView(Class className,IPerspective perspective){
        Set<IView> views = hmClassesInstances.get(className);
        if (views == null){
            views = new LinkedHashSet<IView>();
            hmClassesInstances.put(className,views);
        }
        int index = views.size(); //new view size is last index + 1
        IView view;
        try {
            view = (IView)className.newInstance();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        view.setID(className.getName() + '/' + index);
        view.setPerspective(perspective);
        //store the new view
        views.add(view);
        return view;
    }
    
}
