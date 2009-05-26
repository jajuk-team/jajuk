package org.qdwizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Action panel
 * <p>
 * contains a problem area where problems are displayed and a buttons area
 * (Previous, Next, Finish, Cancel)
 * 
 * @author Bertrand Florat
 * @created 1 may 2006
 */
class ActionsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** Problem text area */
	JLabel jlProblem;

	JButton jbPrevious;

	JButton jbNext;

	JButton jbFinish;

	JButton jbCancel;

	private Color backgroundColor;

	private Color backgroundColorProblem;

	/** Associated action listener */
	ActionListener al;

	/**
	 * @param al
	 *            associated action listener
	 */
	public ActionsPanel(ActionListener al) {
	  backgroundColor = Color.WHITE;
		backgroundColorProblem = Color.WHITE;

		// Problem panel
		jlProblem = new JLabel();
		jlProblem.setForeground(Color.RED);
		jlProblem.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
		jlProblem.setHorizontalAlignment(JLabel.CENTER);

		// Action buttons
		JPanel jpButtons = new JPanel();
		jpButtons.setLayout(new BoxLayout(jpButtons, BoxLayout.X_AXIS));
		Dimension dimButtons = new Dimension(150, 20);
		jbPrevious = new JButton("< " + Langpack.getMessage("Previous"));
		jbPrevious.setPreferredSize(dimButtons);
		jbPrevious.addActionListener(al);
		jbPrevious.setActionCommand("Prev"); //$NON-NLS-1$

		jbNext = new JButton(Langpack.getMessage("Next") + " >");
		jbNext.addActionListener(al);
		jbNext.setActionCommand("Next"); //$NON-NLS-1$
		jbNext.setPreferredSize(dimButtons);

		jbFinish = new JButton(Langpack.getMessage("Finish"));
		jbFinish.addActionListener(al);
		jbFinish.setActionCommand("Finish"); //$NON-NLS-1$
		jbFinish.setPreferredSize(dimButtons);

		jbCancel = new JButton(Langpack.getMessage("Cancel"));
		jbCancel.addActionListener(al);
		jbCancel.setActionCommand("Cancel"); //$NON-NLS-1$
		jbCancel.setPreferredSize(dimButtons);

		jpButtons.add(Box.createHorizontalStrut(10)); //$NON-NLS-1$
		jpButtons.add(Box.createHorizontalGlue()); //$NON-NLS-1$
		jpButtons.add(jbPrevious); //$NON-NLS-1$
		jpButtons.add(Box.createHorizontalStrut(5)); //$NON-NLS-1$
		jpButtons.add(jbNext); //$NON-NLS-1$
		jpButtons.add(Box.createHorizontalStrut(10)); //$NON-NLS-1$
		jpButtons.add(jbFinish); //$NON-NLS-1$
		jpButtons.add(Box.createHorizontalStrut(20)); //$NON-NLS-1$
		jpButtons.add(jbCancel); //$NON-NLS-1$
		jpButtons.add(Box.createHorizontalStrut(10)); //$NON-NLS-1$
		jpButtons.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

		jpButtons.setOpaque(false);
		jlProblem.setOpaque(false);
		setOpaque(false);

		// Main panel
		setLayout(new GridLayout(2, 1));
		add(jlProblem); //$NON-NLS-1$
		add(jpButtons); //$NON-NLS-1$
	}

	/**
	 * Set buttons states
	 * 
	 * @param bNext
	 * @param bFinish
	 */
	void setState(boolean bPrevious, boolean bNext, boolean bFinish, boolean bCancel) {
		jbPrevious.setEnabled(bPrevious);
		jbFinish.setEnabled(bFinish);
		jbNext.setEnabled(bNext);
		jbCancel.setEnabled(bCancel);
	}

	void setProblem(String problem) {
		String sProblem = problem;
		jlProblem.setText(sProblem);
	}

	
	/**
	 * Set the background color of the ActionPanel
	 * 
	 * @param color
	 */
	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}

	/**
	 * Set the background color of the ActionPanel's Problem notification area
	 * 
	 * @param color
	 */
	public void setProblemBackgroundColor(Color color) {
		this.backgroundColorProblem = color;
	}

	/**
	 * Set the background color of the ActionPanel's Problem notification area
	 * 
	 * @param color
	 */
	public void setProblemTextColor(Color color) {
		jlProblem.setForeground(color);
	}

	@Override
    public void paint(java.awt.Graphics g) {
		Graphics2D g2D = (Graphics2D) g;
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		java.awt.Rectangle rect = getBounds();
		g2D.setColor(backgroundColor);
		g2D.fillRect(0, 0, rect.width, rect.height);

		if ((jlProblem != null) && (jlProblem.getText() != null)
				&& jlProblem.getText().length() > 0) {
			rect = jlProblem.getBounds();
			g2D.setColor(backgroundColorProblem);
			g2D.fillRect(rect.x, rect.y, rect.width, rect.height);
		}

		super.paint(g);

		g2D.setColor(java.awt.Color.LIGHT_GRAY);
		g2D.drawLine(rect.x, 0, rect.width, 0);
	}
}
