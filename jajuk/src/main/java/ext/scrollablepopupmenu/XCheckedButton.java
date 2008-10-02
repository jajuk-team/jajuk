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
 * @author balajihe from
 *         http://www.beginner-java-tutorial.com/scrollable-jpopupmenu.html
 * 
 */
public class XCheckedButton extends JButton {

  private static final long serialVersionUID = 6665536733427576873L;

  // Icon to be used to for the Checked Icon of the Button
  private ImageIcon checkedIcon;

  /** Requires the icon to be always displayed, even when the item is unselected */
  private boolean iconAlwaysVisible = false;

  /**
   * These colors are required in order to simulate the JMenuItem's L&F
   */
  public static final Color MENU_HIGHLIGHT_BG_COLOR = UIManager
      .getColor("MenuItem.selectionBackground");

  public static final Color MENU_HIGHLIGHT_FG_COLOR = UIManager
      .getColor("MenuItem.selectionForeground");

  public static final Color MENUITEM_BG_COLOR = UIManager.getColor("MenuItem.background");

  public static final Color MENUITEM_FG_COLOR = UIManager.getColor("MenuItem.foreground");

  // This property if set to false, will result in the checked Icon not being
  // displayed

  // when the button is selected
  private boolean displayCheck = true;

  public XCheckedButton() {
    super();
    init();

  }

  public XCheckedButton(Action a) {
    super(a);
    init();
  }

  public XCheckedButton(Icon icon) {
    super(icon);
    init();
  }

  public XCheckedButton(String text, Icon icon) {
    super(text, icon);
    init();
  }

  public XCheckedButton(String text) {
    super(text);
    init();
  }

  /**
   * Initialize component LAF and add Listeners
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

  private void setMenuItemDefaultColors() {
    XCheckedButton.this.setBackground(MENUITEM_BG_COLOR);
    XCheckedButton.this.setForeground(MENUITEM_FG_COLOR);
  }

  /**
   * @return
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
   * @param checkedFlag
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

  private class XCheckedButtonModel extends JToggleButton.ToggleButtonModel {
    private static final long serialVersionUID = 4628990599914525833L;

    /*
     * Need to Override keeping the super code, else the check mark won't come
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

  public boolean isDisplayCheck() {
    return displayCheck;
  }

  /**
   * Sets the property which determines whether a checked Icon should be
   * displayed or not Setting to false, makes this button display like a normal
   * button
   * 
   * @param displayCheck
   */
  public void setDisplayCheck(boolean displayCheck) {
    this.displayCheck = displayCheck;
  }

  public void setCheckedIcon(ImageIcon checkedIcon) {
    this.checkedIcon = checkedIcon;
  }

  public void setIconAlwaysVisible(boolean iconAlwaysVisible) {
    this.iconAlwaysVisible = iconAlwaysVisible;
  }

}
