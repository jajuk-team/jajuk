/*
 *  Code modified from http://www.java2s.com/ExampleCode/Swing-JFC/AnexampleoftheJPopupMenuinaction.htm
 *  $Revision$
 */

package ext;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JSlider;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Menu item slidebar
 */
public class SliderMenuItem extends JSlider implements MenuElement {

  public SliderMenuItem(int iMin,int iMax,int iValue,String sTitle) {
    setBorder(new CompoundBorder(new TitledBorder(sTitle),
        new EmptyBorder(10, 10, 10, 10)));

    setMajorTickSpacing(8);
    setMinorTickSpacing(6);
    setMinimum(iMin);
    setMaximum(iMax);
    setValue(iValue);
  }

  public void processMouseEvent(MouseEvent e, MenuElement path[],
      MenuSelectionManager manager) {
      super.processMouseEvent(e);
  }

  public void processKeyEvent(KeyEvent e, MenuElement path[],
      MenuSelectionManager manager) {
      super.processKeyEvent(e);
  }

  public void menuSelectionChanged(boolean isIncluded) {
  }

  public MenuElement[] getSubElements() {
    return new MenuElement[0];
  }

  public Component getComponent() {
    return this;
  }
}