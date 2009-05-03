package org.qdwizard;



/**
 * A screen state:
 * <ul>
 * <li>can cancel ?</li>
 * <li>can finish ?</li>
 * <li>can go next ?</li>
 * <li>can go previous ?</li>
 * </ul>
 */
public class ScreenState {
	private boolean bCanFinish;

	/** Can Go Next */
	private boolean bCanGoNext;

	/** Can Go Previous */
	private boolean bCanGoPrevious;

	/** Can Cancel */
	private boolean bCanCancel;

	/** Problem */
	private String sProblem;

	/**
	 * Construct a ScreenState
	 */
	public ScreenState() {
		this(false, false, false, false, null);
	}

	/**
	 * Construct a ScreenState
	 * 
	 * @param bCanGoNext
	 *            next button is enabled
	 * @param bCanGoPrevious
	 *            previous button is enabled
	 * @param bCanCancel
	 *            cancel button is enabled
	 * @param bCanFinish
	 *            cancel button is enabled
	 * @param sProblem
	 *            problem text
	 */
	public ScreenState(boolean bCanGoNext, boolean bCanGoPrevious, boolean bCanCancel,
			boolean bCanFinish, String sProblem) {
		this.bCanGoNext = bCanGoNext;
		this.bCanGoPrevious = bCanGoPrevious;
		this.bCanCancel = bCanCancel;
		this.bCanFinish = bCanFinish;
		this.sProblem = sProblem;
	}

	/**
	 * @return Finish button enabled
	 */
	public boolean getCanFinish() {
		return bCanFinish;
	}

	/**
	 * Set whether the finish button should be enabled
	 * 
	 * @param bCanFinish
	 */
	public void setCanFinish(boolean bCanFinish) {
		this.bCanFinish = bCanFinish;
	}

	/**
	 * @return Next button enabled
	 */
	public boolean getCanGoNext() {
		return bCanGoNext;
	}

	/**
	 * Set whether the next button should be enabled
	 * 
	 * @param bCanGoNext
	 */
	public void setCanGoNext(boolean bCanGoNext) {
		this.bCanGoNext = bCanGoNext;
	}

	/**
	 * @return Previous button enabled
	 */
	public boolean getCanGoPrevious() {
		return bCanGoPrevious;
	}

	/**
	 * Set whether the previous button should be enabled
	 * 
	 * @param bCanGoPrevious
	 */
	public void setCanGoPrevious(boolean bCanGoPrevious) {
		this.bCanGoPrevious = bCanGoPrevious;
	}

	/**
	 * @return Cancel button enabled
	 */
	public boolean getCanCancel() {
		return bCanCancel;
	}

	/**
	 * Set whether the cancel (or System menu close) button should be enabled
	 * 
	 * @param bCanCancel
	 */
	public void setCanCancel(boolean bCanCancel) {
		this.bCanCancel = bCanCancel;
	}

	/**
	 * @return Problem button enabled
	 */
	public String getProblem() {
		return sProblem;
	}

	/**
	 * Set a problem (set to null if problem is fixed)
	 * 
	 * @param sProblem
	 *            Problem string or null if no more problem
	 */
	public void setProblem(String sProblem) {
		this.sProblem = sProblem;
		setCanGoNext(sProblem == null);
	}
	
}
