package org.jajuk.ui;

import java.awt.Dimension;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.plaf.ComboBoxUI;

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
    try{
    	ComboBoxUI cbui = LNFManager.getSteppedComboBoxClass();
    	if (cbui !=null){
    		setUI(cbui);
    		popupWidth = 0;
    	}
    }
    catch(Exception e){
    	e.printStackTrace();
    }
  	
  }
}

