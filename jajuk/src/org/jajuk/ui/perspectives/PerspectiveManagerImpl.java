package org.jajuk.ui.perspectives;

import java.awt.BorderLayout;
import java.awt.Container;

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;

/**
 * Perspectives Manager
 * 
 * @author sgringoi
 * @version 1.0
 * @created 7 oct. 03
 */
public class PerspectiveManagerImpl implements IPerspectiveManager {
	/** Current perspective */
	private Perspective currentPerspective = null;
	/** Parent container of the perspective */
	private Container parentContainer = null;

	/**
	 * Constructor for PerspectiveManagerImpl.
	 */
	public PerspectiveManagerImpl() {
		super();
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getPerspectives()
	 */
	public Perspective[] getPerspectives() {
		return null;
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getCurrentPerspective()
	 */
	public Perspective getCurrentPerspective() throws JajukException{
		if (currentPerspective == null)
		{
			// Current perspective creation
			String perspName = ConfigurationManager.getProperty("jajuk.preference.perspective.default");
			try {
				setCurrentPerspective( (Perspective)Class.forName(perspName).newInstance() );
			} catch (Exception e) {
				JajukException je = new JajukException("jajuk0003", perspName, e);
				throw je;
			}
		}
		
		return currentPerspective;
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setMainWindow(Container)
	 */
	public void setMainWindow(Container pContainer) {
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setCurrentPerspective(Perspective)
	 */
	public void setCurrentPerspective(Perspective pCurPersp) {
		currentPerspective = pCurPersp;
System.out.println("setCurrentPerspective: " + parentContainer.toString() + " - " + currentPerspective.toString());
		parentContainer.add(currentPerspective, BorderLayout.CENTER);
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getPerspective(String)
	 */
	public Perspective getPerspective(String pName) {
		return null;
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setParentContainer(Container)
	 */
	public void setParentContainer(Container pContainer) {
		parentContainer = pContainer;
	}

}
