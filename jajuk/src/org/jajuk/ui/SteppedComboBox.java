package org.jajuk.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

import com.birosoft.liquid.LiquidComboBoxUI;

public class SteppedComboBox extends JComboBox {
  protected int popupWidth;

  public SteppedComboBox() {
    super();
    init();
  }

  public SteppedComboBox(ComboBoxModel aModel) {
    super(aModel);
    init();
  }

  public SteppedComboBox(final Object[] items) {
    super(items);
    init();
  }

  public SteppedComboBox(Vector items) {
    super(items);
    init();
  }

  public void setPopupWidth(int width) {
    popupWidth = width;
  }

  public Dimension getPopupSize() {
    Dimension size = getSize();
    if (popupWidth < 1) popupWidth = size.width;
    return new Dimension(popupWidth, size.height);
  }

  protected void init() {
    setUI(new SteppedComboBoxUI());
    popupWidth = 0;
  }
}

class SteppedComboBoxUI extends LiquidComboBoxUI {
  protected ComboPopup createPopup() {
    BasicComboPopup popup = new BasicComboPopup( comboBox ) {

      public void show() {
        Dimension popupSize = ((SteppedComboBox)comboBox).getPopupSize();
        popupSize.setSize( popupSize.width,
          getPopupHeightForRowCount( comboBox.getMaximumRowCount() ) );
        Rectangle popupBounds = computePopupBounds( 0,
          comboBox.getBounds().height, popupSize.width, popupSize.height);
        scroller.setMaximumSize( popupBounds.getSize() );
        scroller.setPreferredSize( popupBounds.getSize() );
        scroller.setMinimumSize( popupBounds.getSize() );
        list.invalidate();
        int selectedIndex = comboBox.getSelectedIndex();
        if ( selectedIndex == -1 ) {
          list.clearSelection();
        } else {
          list.setSelectedIndex( selectedIndex );
        }
        list.ensureIndexIsVisible( list.getSelectedIndex() );
        setLightWeightPopupEnabled( comboBox.isLightWeightPopupEnabled() );

        show( comboBox, popupBounds.x, popupBounds.y );
      }
    };
    popup.getAccessibleContext().setAccessibleParent(comboBox);
    return popup;
  }
}

