package org.qdwizard;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * 
 * Screen Header
 * <p>
 * Contains a wizard title, a subtitle used to display the name of the current
 * screen and an optional background image
 * </p>
 * 
 * @author Bertrand Florat
 * @created 1 may 2006
 */
class Header extends JPanel {

	private static final long serialVersionUID = 1L;

	JPanel jta;

	Image backgroundImage;
	
	ImageIcon icon;

	String sTitleText;

	String sSubtitleText;

	/**
	 * 
	 * @param sTextID
	 *            I18N ID of header
	 */
	public Header() {
		setLayout(new GridLayout());
		jta = new JPanel();
		jta.setOpaque(true);
		jta.setPreferredSize(new Dimension(0, 70));
		add(jta); //$NON-NLS-1$
	}

	/**
	 * Set the header title text
	 * 
	 * @param sText
	 */
	public void setTitleText(String sText) {
		sTitleText = sText;
	}

	/**
	 * Set the header subtitle text
	 * 
	 * @param sText
	 */
	public void setSubtitleText(String sText) {
		sSubtitleText = sText;
	}

	/**
	 * Set the header Image
	 * 
	 * @param img
	 */
	public void setImage(Image img) {
		backgroundImage = img;
	}
	
	/**
	 * Set the header right-side icon
	 * 
	 * @param img
	 */
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	@Override
    public void paint(java.awt.Graphics g) {
		super.paint(g);
		Graphics2D g2D = (Graphics2D) g;
		java.awt.Rectangle rect = getBounds();
		g2D.setColor(java.awt.Color.WHITE);
		g2D.fillRect(rect.x, rect.y, rect.width, rect.height);

		if (backgroundImage != null){
			g2D.drawImage(backgroundImage, 0, 0, rect.width, rect.height, this);
		}
		
		if (icon != null){
			int h = icon.getIconHeight();
			int w = icon.getIconWidth();
			g2D.drawImage(icon.getImage(), rect.width-w - 10, (rect.height-h)/2, w, h, this);
		}

		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2D.setColor(java.awt.Color.BLACK);
		g2D.setFont(new Font("Dialog", Font.BOLD, 14));
		g2D.drawString(sTitleText, 20, 25);

		g2D.setFont(new Font("Dialog", Font.PLAIN, 13));
		g2D.drawString(sSubtitleText, 20, 50);

		g2D.setColor(java.awt.Color.BLACK);
		g2D.drawLine(rect.x, rect.height - 1, rect.width, rect.height - 1);
	}
}