package org.qdwizard;

import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A wizard screen
 * <ul>
 * <li>For each wizard page, create a public Screen class. You have to
 * implement initUI(), getDescription() and getName() abstract methods.</li>
 * <li>getName() method should return the step name and getDescription() the
 * step description (return null if no description needed).</li>
 * <li>initUI() method contains graphical code for your screen. This method is
 * automatically called from screen constructor so don't call it.</li>
 * </ul>
 * 
 * @author Bertrand Florat
 * @created 1 may 2006
 */
public abstract class Screen extends JPanel {
  private static final long serialVersionUID = 1L;

  private final ScreenState state;

  public Map<String, Object> data;

  private Wizard wizard;

  /**
   * Construct a screen
   * 
   */
  public Screen() {
    data = Wizard.data;
    state = new ScreenState(true, true, true, false, null);
    initUI();
  }

  /**
   * Give here the step name.
   * 
   * @return screen name
   */
  @Override
  abstract public String getName();

  /**
   * Screen description (optional)
   * 
   * @return screen description
   */
  abstract public String getDescription();

  boolean canFinish() {
    // Can finish only if none problem
    return state.getCanFinish() && (state.getProblem() == null);
  }

  /**
   * Set whether the finish button should be enabled
   * 
   * @param b
   */
  public void setCanFinish(boolean b) {
    state.setCanFinish(b);
    notifyGUI();
  }

  boolean canGoNext() {
    // if screen is last one, cannot go further
    return state.getCanGoNext() && !state.getCanFinish() && (state.getProblem() == null);
  }

  public boolean canCancel() {
    return state.getCanCancel();
  }

  boolean canGoPrevious() {
    return state.getCanGoPrevious();
  }

  /**
   * Set whether the next button should be enabled
   * 
   * @param b
   */
  void setCanGoNext(boolean b) {
    state.setCanGoNext(b);
    notifyGUI();
  }

  /**
   * Set whether the previous button should be enabled
   * 
   * @param b
   */
  void setCanGoPrevious(boolean b) {
    state.setCanGoPrevious(b);
    notifyGUI();
  }

  /**
   * Set whether the cancel (or System menu close) button should be enabled
   * 
   * @param b
   */
  public void setCanCancel(boolean b) {
    state.setCanCancel(b);
    notifyGUI();
  }

  /**
   * Set a problem (set to null if problem is fixed)
   * 
   * @param sProblem
   *          Problem string or null if no more problem
   */
  public void setProblem(String sProblem) {
    state.setProblem(sProblem);
    notifyGUI();
  }

  /**
   * Get current problem
   * 
   * @return the current problem
   */
  public String getProblem() {
    return state.getProblem();
  }

  /** UI creation */
  abstract public void initUI();

  /**
   * Called by wizard before the screen is displayed. This happens only in
   * forward mode, which means onEnter won't be called when you return to a
   * screen via the previous button.
   * 
   */
  public void onEnter() {

  }

  /**
   * Called by wizard before the screen is left. This happens only in forward
   * mode, which means onLeave won't be called when you leave the screen via the
   * previous button.
   * <p>
   * 
   * @return return true if the Wizard should display the next screen
   * @return return false if the Wizard should stay on the current screen
   */
  public boolean onNext() {
    return true;
  }

  /**
   * Called by wizard when the wizard is being canceled. Use this function to
   * clean up (like stop any threads that this Screen might have created)
   */
  public void onCancelled() {
  }

  /**
   * Called by wizard when the wizard is closing because the Finish button was
   * pressed. Use this function to clean up (like stop any threads that this
   * Screen might have created)
   */
  public void onFinished() {
  }

  /**
   * access to wizard instance
   * 
   * @return
   */
  public Wizard getWizard() {
    return wizard;
  }

  protected void setWizard(Wizard wizard) {
    this.wizard = wizard;
  }

  private void notifyGUI() {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        Screen.this.wizard.updateGUI();
      }

    });
  }

}
