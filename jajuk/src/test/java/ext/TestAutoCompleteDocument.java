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

import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import junit.framework.TestCase;

import org.jdesktop.swingx.autocomplete.AbstractAutoCompleteAdaptor;
import org.jdesktop.swingx.autocomplete.ComboBoxAdaptor;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import org.jdesktop.swingx.autocomplete.TextComponentAdaptor;

/**
 * .
 */
public class TestAutoCompleteDocument extends TestCase {
  /**
   * Test method for {@link ext.AutoCompleteDocument#remove(int, int)}.
   *
   * @throws Exception the exception
   */
  public void testRemove() throws Exception {
    String[] items = new String[] { "exact", "exacter", "exactest" };
    JTextComponent textComponent = new JTextField();
    TextComponentAdaptor adaptor = new TextComponentAdaptor(textComponent, Arrays.asList(items));
    Document document = new AutoCompleteDocument(adaptor, true);
    document.insertString(0, "test", null);
    // TODO: this does not work for some reason....
    // document.remove(0, 2);
  }

  /**
   * Test method for.
   *
   * {@link ext.AutoCompleteDocument#AutoCompleteDocument(org.jdesktop.swingx.autocomplete.AbstractAutoCompleteAdaptor, boolean, org.jdesktop.swingx.autocomplete.ObjectToStringConverter)}
   * .
   */
  public void testAutoCompleteDocumentAbstractAutoCompleteAdaptorBooleanObjectToStringConverter() {
    new AutoCompleteDocument(new ComboBoxAdaptor(new JComboBox()), false, null);
  }

  /**
   * Test auto complete document abstract auto complete adaptor boolean object to string converter selected.
   * 
   */
  public void testAutoCompleteDocumentAbstractAutoCompleteAdaptorBooleanObjectToStringConverterSelected() {
    AbstractAutoCompleteAdaptor adaptor = new ComboBoxAdaptor(new JComboBox(new Object[] {
        "string1", "string2", "string3" }));
    adaptor.setSelectedItem("string2");
    adaptor.setSelectedItemAsString("string3");
    assertNotNull(adaptor.getSelectedItem());
    assertNotNull(adaptor.getSelectedItemAsString());
    new AutoCompleteDocument(adaptor, false, new ObjectToStringConverter() {
      @Override
      public String getPreferredStringForItem(Object obj) {
        return null;
      }
    });
  }

  /**
   * Test method for.
   *
   * {@link ext.AutoCompleteDocument#AutoCompleteDocument(org.jdesktop.swingx.autocomplete.AbstractAutoCompleteAdaptor, boolean)}
   * .
   */
  public void testAutoCompleteDocumentAbstractAutoCompleteAdaptorBoolean() {
    new AutoCompleteDocument(new ComboBoxAdaptor(new JComboBox()), false);
  }

  /**
   * Test method for {@link ext.AutoCompleteDocument#isStrictMatching()}.
   */
  public void testIsStrictMatching() {
    AutoCompleteDocument document = new AutoCompleteDocument(new ComboBoxAdaptor(new JComboBox()),
        false);
    assertFalse(document.isStrictMatching());
    document = new AutoCompleteDocument(new ComboBoxAdaptor(new JComboBox()), true);
    assertTrue(document.isStrictMatching());
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link ext.AutoCompleteDocument#insertString(int, java.lang.String, javax.swing.text.AttributeSet)}
   * .
   */
  public void testInsertStringIntStringAttributeSetStrictMatching() throws Exception {
    String[] items = new String[] { "exact", "exacter", "exactest" };
    JTextComponent textComponent = new JTextField();
    TextComponentAdaptor adaptor = new TextComponentAdaptor(textComponent, Arrays.asList(items));
    Document document = new AutoCompleteDocument(adaptor, true);
    document.insertString(0, "test", null);
  }

  /**
   * Test insert string int string attribute set.
   * 
   *
   * @throws Exception the exception
   */
  public void testInsertStringIntStringAttributeSet() throws Exception {
    String[] items = new String[] { "exact", "exacter", "exactest" };
    JTextComponent textComponent = new JTextField("012345");
    TextComponentAdaptor adaptor = new TextComponentAdaptor(textComponent, Arrays.asList(items));
    Document document = new AutoCompleteDocument(adaptor, false);
    document.insertString(0, "test", null);
  }

  /**
   * Test prefer exact match over currently selected.
   * 
   *
   * @throws Exception the exception
   */
  public void testPreferExactMatchOverCurrentlySelected() throws Exception {
    String[] items = new String[] { "exact", "exacter", "exactest" };
    JTextComponent textComponent = new JTextField();
    TextComponentAdaptor adaptor = new TextComponentAdaptor(textComponent, Arrays.asList(items));
    Document document = new AutoCompleteDocument(adaptor, true);
    textComponent.setDocument(document);
    textComponent.setText("exacter");
    assertTrue(adaptor.getSelectedItem().equals("exacter"));
    document.remove(4, 3);
    assertTrue(adaptor.getSelectedItem().equals("exacter"));
    document.insertString(4, "t", null);
    assertTrue(adaptor.getSelectedItem().equals("exact"));
  }
}
