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
 *  $Revision$
 */
package ext.scrollablepopupmenu;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicButtonUI;

import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;

/**
 * The Class XCheckedButton.
 * 
 * @author balajihe from
 * http://www.beginner-java-tutorial.com/scrollable-jpopupmenu.html
 */
public class XCheckedButton extends JButton {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 6665536733427576873L;

  // Icon to be used to for the Checked Icon of the Button
  /** DOCUMENT_ME. */
  private ImageIcon checkedIcon;

  /** Requires the icon to be always displayed, even when the item is unselected. */
  private boolean iconAlwaysVisible = false;

  /** These colors are required in order to simulate the JMenuItem's L&F. */
  public static final Color MENU_HIGHLIGHT_BG_COLOR = UIManager
      .getColor("MenuItem.selectionBackground");

  /** The Constant MENU_HIGHLIGHT_FG_COLOR.  DOCUMENT_ME */
  public static final Color MENU_HIGHLIGHT_FG_COLOR = UIManager
      .getColor("MenuItem.selectionForeground");

  /** The Constant MENUITEM_BG_COLOR.  DOCUMENT_ME */
  public static final Color MENUITEM_BG_COLOR = UIManager.getColor("MenuItem.background");

  /** The Constant MENUITEM_FG_COLOR.  DOCUMENT_ME */
  public static final Color MENUITEM_FG_COLOR = UIManager.getColor("MenuItem.foreground");

  // This property if set to false, will result in the checked Icon not being
  // displayed

  // when the button is selected
  /** DOCUMENT_ME. */
  private boolean displayCheck = true;

  /**
   * Instantiates a new x checked button.
   */
  public XCheckedButton() {
    super();
    init();

  }

  /**
   * Instantiates a new x checked button.
   * 
   * @param a DOCUMENT_ME
   */
  public XCheckedButton(Action a) {
    super(a);
    init();
  }

  /**
   * Instantiates a new x checked button.
   * 
   * @param icon DOCUMENT_ME
   */
  public XCheckedButton(Icon icon) {
    super(icon);
    init();
  }

  /**
   * Instantiates a new x checked button.
   * 
   * @param text DOCUMENT_ME
   * @param icon DOCUMENT_ME
   */
  public XCheckedButton(String text, Icon icon) {
    super(text, icon);
    init();
  }

  /**
   * Instantiates a new x checked button.
   * 
   * @param text DOCUMENT_ME
   */
  public XCheckedButton(String text) {
    super(text);
    init();
  }

  /**
   * Initialize component LAF and add Listeners.
   */
  private void init() {
    MouseAdapter mouseAdapter = getMouseAdapter();

    // Basically JGoodies LAF UI for JButton does not allow Background color
    // to be set.
    // So we need to set the default UI,
    ComponentUI ui = BasicButtonUI.createUI(this);
    this.setUI(ui);
    setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 2));
    setMenuItemDefaultColors();
    // setContentAreaFilled(false);
    setHorizontalTextPosition(SwingConstants.RIGHT);
    setHorizontalAlignment(SwingConstants.LEFT);
    // setModel(new JToggleButton.ToggleButtonModel());
    setModel(new XCheckedButtonModel());
    setSelected(false);
    this.addMouseListener(mouseAdapter);

  }

  /**
   * Sets the menu item default colors.
   * DOCUMENT_ME
   */
  private void setMenuItemDefaultColors() {
    XCheckedButton.this.setBackground(MENUITEM_BG_COLOR);
    XCheckedButton.this.setForeground(MENUITEM_FG_COLOR);
  }

  /**
   * Gets the mouse adapter.
   * 
   * @return the mouse adapter
   */
  private MouseAdapter getMouseAdapter() {
    return new MouseAdapter() {
      // For static menuitems, the background color remains the
      // highlighted color, if this is not overridden
      @Override
      public void mousePressed(MouseEvent e) {
        setMenuItemDefaultColors();
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        XCheckedButton.this.setBackground(MENU_HIGHLIGHT_BG_COLOR);
        XCheckedButton.this.setForeground(MENU_HIGHLIGHT_FG_COLOR);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        setMenuItemDefaultColors();
      }

    };
  }

  /**
   * Display icon.
   * 
   * @param checkedFlag DOCUMENT_ME
   */
  public void displayIcon(boolean checkedFlag) {
    if (checkedFlag && isDisplayCheck()) {
      if (checkedIcon == null) {
        checkedIcon = IconLoader.getIcon(JajukIcons.OK);
      }
      this.setIcon(checkedIcon);
    } else {
      this.setIcon(IconLoader.getIcon(JajukIcons.EMPTY));
    }
    this.repaint();
  }

  /**
   * DOCUMENT_ME.
   */
  private class XCheckedButtonModel extends JToggleButton.ToggleButtonModel {

    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 4628990599914525833L;

    /*
     * Need to Override keeping the super code, else the check mark won't come
     */
    /* (non-Javadoc)
     * @see javax.swing.JToggleButton.ToggleButtonModel#setSelected(boolean)
     */
    @Override
    public void setSelected(final boolean b) {
      boolean set = b;
      ButtonGroup group = getGroup();
      if (group != null) {
        // use the group model instead
        group.setSelected(this, set);
        set = group.isSelected(this);
      }

      if (isSelected() == set) {
        return;
      }

      if (set) {
        stateMask |= SELECTED;
      } else {
        stateMask &= ~SELECTED;
      }

      // Send ChangeEvent
      fireStateChanged();

      // Send ItemEvent
      fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, this
          .isSelected() ? ItemEvent.SELECTED : ItemEvent.DESELECTED));

      XCheckedButton.this.displayIcon(set | iconAlwaysVisible);

    }

  }

  // Returns true if Button will display Checked Icon on Click. Default
  // Behaviour is to display a Checked Icon

  /**
   * Checks if is display check.
   * 
   * @return true, if is display check
   */
  public boolean isDisplayCheck() {
    return displayCheck;
  }

  /**
   * Sets the property which determines whether a checked Icon should be
   * displayed or not Setting to false, makes this button display like a normal
   * button.
   * 
   * @param displayCheck DOCUMENT_ME
   */
  public void setDisplayCheck(boolean displayCheck) {
    this.displayCheck = displayCheck;
  }

  /**
   * Sets the checked icon.
   * 
   * @param checkedIcon the new checked icon
   */
  public void setCheckedIcon(ImageIcon checkedIcon) {
    this.checkedIcon = checkedIcon;
  }

  /**
   * Sets the icon always visible.
   * 
   * @param iconAlwaysVisible the new icon always visible
   */
  public void setIconAlwaysVisible(boolean iconAlwaysVisible) {
    this.iconAlwaysVisible = iconAlwaysVisible;
  }

}
