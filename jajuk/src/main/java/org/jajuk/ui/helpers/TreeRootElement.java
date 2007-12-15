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
 *  $$Revision: 2509 $$
 */
package org.jajuk.ui.helpers;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Flag class telling a tree node is a root element
 */
public class TreeRootElement extends DefaultMutableTreeNode {

  /**
   * 
   */
  private static final long serialVersionUID = -3183130517695923747L;

  /**
   * 
   */
  public TreeRootElement() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @param userObject
   * @param allowsChildren
   */
  public TreeRootElement(Object userObject, boolean allowsChildren) {
    super(userObject, allowsChildren);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param userObject
   */
  public TreeRootElement(Object userObject) {
    super(userObject);
    // TODO Auto-generated constructor stub
  }
}
