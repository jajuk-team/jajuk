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
package ext.scrollablepopupmenu;

import java.awt.HeadlessException;

import javax.swing.AbstractButton;

import org.jajuk.JajukTestCase;

/**
 * DOCUMENT_ME.
 */
public class TestXJPopupMenu extends JajukTestCase {

  /**
   * Test method for {@link ext.scrollablepopupmenu.XJPopupMenu#removeAll()}.
   */
  public void testRemoveAll() {
    try {
      XJPopupMenu menu = new XJPopupMenu(null);
      menu.removeAll();
    } catch (HeadlessException e) {
      // happens if run without GUI support
    }
  }

  /**
   * Test method for {@link ext.scrollablepopupmenu.XJPopupMenu#addSeparator()}.
   */
  public void testAddSeparator() {
    try {
      XJPopupMenu menu = new XJPopupMenu(null);
      menu.addSeparator();
    } catch (HeadlessException e) {
      // happens if run without GUI support
    }
  }

  /**
   * Test method for.
   *
   * {@link ext.scrollablepopupmenu.XJPopupMenu#XJPopupMenu(javax.swing.JFrame)}
   * .
   */
  public void testXJPopupMenu() {
    try {
      XJPopupMenu menu = new XJPopupMenu(null);
      assertNotNull(menu);
    } catch (HeadlessException e) {
      // happens if run without GUI support
    }
  }

  /**
   * Test method for.
   *
   * {@link ext.scrollablepopupmenu.XJPopupMenu#show(java.awt.Component, int, int)}
   * .
   */
  public void testShowComponentIntInt() {
    try {
      XJPopupMenu menu = new XJPopupMenu(null);
      menu.show(null, 0, 0);
    } catch (HeadlessException e) {
      // happens if run without GUI support
    }
  }

  /**
   * Test method for {@link ext.scrollablepopupmenu.XJPopupMenu#hidemenu()}.
   */
  public void testHidemenu() {
    try {
      XJPopupMenu menu = new XJPopupMenu(null);
      menu.hidemenu();
    } catch (HeadlessException e) {
      // happens if run without GUI support
    }
  }

  /**
   * Test method for.
   *
   * {@link ext.scrollablepopupmenu.XJPopupMenu#add(javax.swing.AbstractButton)}
   * .
   */
  public void testAddAbstractButton() {
    try {
      XJPopupMenu menu = new XJPopupMenu(null);
      AbstractButton button = null;
      menu.add(button);
    } catch (HeadlessException e) {
      // happens if run without GUI support
    }
  }

  /**
   * Test method for.
   *
   * {@link ext.scrollablepopupmenu.XJPopupMenu#actionPerformed(java.awt.event.ActionEvent)}
   * .
   */
  public void testActionPerformed() {
    try {
      XJPopupMenu menu = new XJPopupMenu(null);
      menu.actionPerformed(null);
    } catch (HeadlessException e) {
      // happens if run without GUI support
    }
  }

  /**
   * Test method for {@link ext.scrollablepopupmenu.XJPopupMenu#getComponents()}
   * .
   */
  public void testGetComponents() {
    try {
      XJPopupMenu menu = new XJPopupMenu(null);
      menu.getComponents();
    } catch (HeadlessException e) {
      // happens if run without GUI support
    }
  }

}
