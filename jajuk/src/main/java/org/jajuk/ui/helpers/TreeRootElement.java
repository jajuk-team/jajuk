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
 *  
 */
package org.jajuk.ui.helpers;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Flag class telling a tree node is a root element.
 */
public class TreeRootElement extends DefaultMutableTreeNode {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -3183130517695923747L;

  /**
   * Instantiates a new tree root element.
   */
  public TreeRootElement() {
    super();
  }

  /**
   * The Constructor.
   * 
   * @param userObject DOCUMENT_ME
   */
  public TreeRootElement(Object userObject) {
    super(userObject);
  }
}
