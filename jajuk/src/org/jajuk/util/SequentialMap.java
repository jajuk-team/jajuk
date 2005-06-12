/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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

package org.jajuk.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 *  Non synchonized map whose key interator folow parameters entry order
 *
 * @author     Bertrand Florat
 * @created    7 juin 2005
 */
public class SequentialMap {

    private ArrayList alKeys;

    private ArrayList alValues;
    
     
    
    public SequentialMap(){
        alKeys = new ArrayList(10);
        alValues = new ArrayList(10);
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#size()
     */
    public int size() {
        return alKeys.size();
    }

    /* (non-Javadoc)
     * @see java.util.Map#clear()
     */
    public void clear() {
        alKeys.clear();
        alValues.clear();
    }

    /* (non-Javadoc)
     * @see java.util.Map#isEmpty()
     */
    public boolean isEmpty() {
        return alKeys.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public boolean containsKey(Object key) {
        return alKeys.contains(key);
    }

    /* (non-Javadoc)
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public boolean containsValue(Object value) {
        return alValues.contains(value);
    }

    /* (non-Javadoc)
     * @see java.util.Map#values()
     */
    public Collection values() {
        return values();
    }

       /* (non-Javadoc)
     * @see java.util.Map#keySet()
     */
    public Collection keys() {
        return alKeys;
    }

    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key) {
        int index = alKeys.indexOf(key);
        if (index >= 0){
            return alValues.get(index);
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see java.util.Map#get(java.lang.Object)
     */
    public String getProperty(String sKey) {
        return (String)get(sKey);
    }

    /* (non-Javadoc)
     * @see java.util.Map#remove(java.lang.Object)
     */
    public Object remove(Object key) {
        int index = alKeys.indexOf(key);
        if (index >= 0){
            alKeys.remove(index);
            alValues. remove(index);
            return key;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public Object put(Object key, Object value) {
        if (alKeys.contains(key)){
          int index = alKeys.indexOf(key);
          alValues.set(index,value);
        }
        else{
            alKeys.add(key);
            alValues.add(value);    
        }
        return key;
    }

}
