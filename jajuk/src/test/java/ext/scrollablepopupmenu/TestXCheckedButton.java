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
 *  $Revision: 3132 $
 */
package ext.scrollablepopupmenu;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.lang.ArrayUtils;
import org.jajuk.JajukTestCase;

/**
 * 
 */
public class TestXCheckedButton extends JajukTestCase {

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#XCheckedButton()}.
   */
  public final void testXCheckedButton() {
    new XCheckedButton();
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#XCheckedButton(javax.swing.Action)}
   * .
   */
  public final void testXCheckedButtonAction() {
    new XCheckedButton(new Action() {

      @Override
      public void addPropertyChangeListener(PropertyChangeListener listener) {
      }

      @Override
      public Object getValue(String key) {

        return null;
      }

      @Override
      public boolean isEnabled() {

        return false;
      }

      @Override
      public void putValue(String key, Object value) {
      }

      @Override
      public void removePropertyChangeListener(PropertyChangeListener listener) {
      }

      @Override
      public void setEnabled(boolean b) {
      }

      @Override
      public void actionPerformed(ActionEvent e) {
      }
    });
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#XCheckedButton(javax.swing.Icon)}
   * .
   */
  public final void testXCheckedButtonIcon() {
    new XCheckedButton(new DummyIcon());
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#XCheckedButton(java.lang.String, javax.swing.Icon)}
   * .
   */
  public final void testXCheckedButtonStringIcon() {
    new XCheckedButton("testtext", new DummyIcon());
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#XCheckedButton(java.lang.String)}
   * .
   */
  public final void testXCheckedButtonString() {
    new XCheckedButton("testtext");
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#displayIcon(boolean)}.
   */
  public final void testDisplayIcon() {
    XCheckedButton button = new XCheckedButton("testtext", new DummyIcon());
    button.displayIcon(false);

    button.displayIcon(true);
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#isDisplayCheck()}.
   */
  public final void testIsAndSetDisplayCheck() {
    XCheckedButton button = new XCheckedButton("testtext", new DummyIcon());
    assertTrue(button.isDisplayCheck());
    button.setDisplayCheck(false);
    assertFalse(button.isDisplayCheck());
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#setDisplayCheck(boolean)}.
   */
  public final void testSetDisplayCheck() {
    // tested above
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#setCheckedIcon(javax.swing.ImageIcon)}
   * .
   */
  public final void testSetCheckedIcon() {
    XCheckedButton button = new XCheckedButton("testtext", new DummyIcon());
    button.setCheckedIcon(new ImageIcon());
  }

  /**
   * Test method for
   * {@link ext.scrollablepopupmenu.XCheckedButton#setIconAlwaysVisible(boolean)}
   * .
   */
  public final void testSetIconAlwaysVisible() {
    XCheckedButton button = new XCheckedButton("testtext", new DummyIcon());
    button.setIconAlwaysVisible(true);
    button.setIconAlwaysVisible(false);
  }

  public class DummyIcon implements Icon {
    @Override
    public int getIconHeight() {

      return 0;
    }

    @Override
    public int getIconWidth() {

      return 0;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
    }
  }

  public void testMouseAdapter() {
    XCheckedButton button = new XCheckedButton("testtext", new DummyIcon());
    // we should have at least one a mouse listener on the button
    assertTrue(ArrayUtils.toString(button.getMouseListeners()),
        button.getMouseListeners().length > 0);

    // none of them looks at the actual event right now...
    button.getMouseListeners()[1].mousePressed(null);
    button.getMouseListeners()[1].mouseEntered(null);
    button.getMouseListeners()[1].mouseExited(null);
  }

  public void testModel() {
    XCheckedButton button = new XCheckedButton("testtext", new DummyIcon());
    assertNotNull(button.getModel());

    button.setSelected(true);
    button.setSelected(false);

    ((DefaultButtonModel) button.getModel()).setGroup(new ButtonGroup());

    button.setSelected(true);
    button.setSelected(false);
  }
}
