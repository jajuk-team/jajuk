/*
 * JSplashLabel.java
 *
 * Copyright (c) 2004,2005 Gregory Kotsaftis
 * gregkotsaftis@yahoo.com
 * http://zeus-jscl.sourceforge.net/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ext;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.MediaTracker;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * Extends JLabel to provide support for custom text drawing inside image used
 * for JSplash component.
 * <p>
 * @author Gregory Kotsaftis
 * @since 1.06
 */
public final class JSplashLabel extends JLabel {
    
    /**
     * Used to draw the text string.
     */
    private String m_text = null;

    /**
     * Font to use when drawing the text.
     */
    private Font m_font = null;

    /**
     * Colour to use when drawing the text.
     */
    private Color m_color = null;
    
    
    /**
     * Constructor.
     * <p>
     * @param url   The location of the image (<b>it cannot be null</b>).
     * @param s     The string to draw (can be null).
     * @param f     The font to use (can be null).
     * @param c     The color to use (can be null).
     */
    public JSplashLabel(URL url, String s, Font f, Color c)
    {
        super();
        
        ImageIcon icon = new ImageIcon( url );
        if( icon.getImageLoadStatus()!=MediaTracker.COMPLETE )
        {
            System.err.println("Cannot load splash screen: " + url); //$NON-NLS-1$
            setText("Cannot load splash screen: " + url); //$NON-NLS-1$
        }
        else
        {
            setIcon( icon );
            m_text = s;
            m_font = f;
            m_color = c;
            
            if( m_font!=null )
            {
                setFont( m_font );
            }
        }
    }
    

    /**
     * Overrides paint in order to draw the version number on the splash screen.
     * <p>
     * @param g     The graphics context to use.
     */
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if( m_text!=null )
        {
            if( m_color!=null )
            {
                g.setColor( m_color );
            }            
            
            FontMetrics fm = g.getFontMetrics();
            int width = fm.stringWidth(m_text) + 20;
            int height = fm.getHeight();
            
            g.drawString(m_text, getWidth() - width, getHeight() - height);
        }
    }
    
}
