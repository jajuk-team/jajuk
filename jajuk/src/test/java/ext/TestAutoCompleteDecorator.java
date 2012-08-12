/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
package ext;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.text.JTextComponent;

import junit.framework.TestCase;

/**
 * .
 */
public class TestAutoCompleteDecorator extends TestCase {
  /**
   * Test method for {@link ext.AutoCompleteDecorator#decorate(javax.swing.text.JTextComponent, java.util.List, boolean)}.
   */
  public void testDecorateJTextComponentListOfObjectBoolean() {
    // TODO: make working
    // AutoCompleteDecorator.decorate(null, null, false);
  }

  /**
   * Test method for {@link ext.AutoCompleteDecorator#decorate(javax.swing.text.JTextComponent, java.util.List, boolean, org.jdesktop.swingx.autocomplete.ObjectToStringConverter)}.
   */
  public void testDecorateJTextComponentListOfObjectBooleanObjectToStringConverter() {
    // TODO: make working
    // AutoCompleteDecorator.decorate(null, null, false, null);
  }

  /**
   * Test method for {@link ext.AutoCompleteDecorator#decorate(javax.swing.JList, javax.swing.text.JTextComponent)}.
   */
  public void testDecorateJListJTextComponent() {
    AutoCompleteDecorator.decorate(new JList(), new JTextComponent() {
      private static final long serialVersionUID = 1L;

      /* (non-Javadoc)
       * @see javax.swing.text.JTextComponent#getText()
       */
      @Override
      public String getText() {
        return "testtext";
      }
    });
  }

  /**
   * Test method for {@link ext.AutoCompleteDecorator#decorate(javax.swing.JList, javax.swing.text.JTextComponent, org.jdesktop.swingx.autocomplete.ObjectToStringConverter)}.
   */
  public void testDecorateJListJTextComponentObjectToStringConverter() {
    // TODO: make working
    //AutoCompleteDecorator.decorate(new JList(), new JTextComponent() {
    //  private static final long serialVersionUID = 1L;}, null);
  }

  /**
   * Test method for {@link ext.AutoCompleteDecorator#decorate(javax.swing.JComboBox)}.
   */
  public void testDecorateJComboBox() {
    // TODO: make working
    // AutoCompleteDecorator.decorate(null);
  }

  /**
   * Test method for {@link ext.AutoCompleteDecorator#decorate(javax.swing.JComboBox, org.jdesktop.swingx.autocomplete.ObjectToStringConverter)}.
   */
  public void testDecorateJComboBoxObjectToStringConverter() {
    // TODO: make working
    // AutoCompleteDecorator.decorate((JComboBox)null, null);
  }

  /**
   * Test method for {@link ext.AutoCompleteDecorator#decorate(javax.swing.text.JTextComponent, ext.AutoCompleteDocument, org.jdesktop.swingx.autocomplete.AbstractAutoCompleteAdaptor)}.
   */
  public void testDecorateJTextComponentAutoCompleteDocumentAbstractAutoCompleteAdaptor() {
    // TODO: make working
    // AutoCompleteDecorator.decorate((JTextComponent)null, null, null);
  }

  ///////////////////////7
  // Tests from singx itself
  private JComboBox combo;

  /* (non-Javadoc)
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    combo = new JComboBox(new String[] { "Alpha", "Bravo", "Charlie", "Delta" });
  }

  /**
   * SwingX Issue #299.
   */
  public void testDecorationFocusListeners() {
    Component editor = combo.getEditor().getEditorComponent();
    //current count plus 2 from UI delegate and 1 from AutoComplete
    int expectedFocusListenerCount = editor.getFocusListeners().length + 3;
    AutoCompleteDecorator.decorate(combo);
    assertEquals(expectedFocusListenerCount, editor.getFocusListeners().length);
    //redecorating should not increase listener count
    AutoCompleteDecorator.decorate(combo);
    // TODO: make working
    // assertEquals(expectedFocusListenerCount, editor.getFocusListeners().length);
  }

  /**
   * SwingX Issue #299.
   */
  public void testDecorationKeyListeners() {
    Component editor = combo.getEditor().getEditorComponent();
    //current count 1 from AutoComplete
    int expectedKeyListenerCount = editor.getKeyListeners().length + 1;
    AutoCompleteDecorator.decorate(combo);
    assertEquals(expectedKeyListenerCount, editor.getKeyListeners().length);
    //redecorating should not increase listener count
    AutoCompleteDecorator.decorate(combo);
    // TODO: make working
    // assertEquals(expectedKeyListenerCount, editor.getKeyListeners().length);
  }

  /**
   * SwingX Issue #299.
   */
  public void testDecorationPropertyListeners() {
    //current count 1 from AutoComplete
    int expectedPropListenerCount = combo.getPropertyChangeListeners("editor").length + 1;
    AutoCompleteDecorator.decorate(combo);
    assertEquals(expectedPropListenerCount, combo.getPropertyChangeListeners("editor").length);
    //redecorating should not increase listener count
    AutoCompleteDecorator.decorate(combo);
    // TODO: make working
    // assertEquals(expectedPropListenerCount, combo.getPropertyChangeListeners("editor").length);
  }
}
