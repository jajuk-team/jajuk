/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

package org.jajuk.ui.views;

import java.awt.Color;
import java.awt.Font;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;

import com.jgoodies.animation.Animation;
import com.jgoodies.animation.Animations;
import com.jgoodies.animation.Animator;
import com.jgoodies.animation.animations.BasicTextAnimation;
import com.jgoodies.animation.components.BasicTextLabel;

/**
 *  Animation-based view 
 *
 * @author     bflorat
 * @created    13 août 2004
 */
public class AnimationView extends ViewAdapter implements ITechnicalStrings,Observer {
	
	private static final int DEFAULT_FRAME_RATE = 30;
	private static final int DEFAULT_DURATION = 3000;
	
	private BasicTextLabel btl1;
	private JLabel jl1;
	private Animator animator;
	
	public AnimationView(){
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#getID()
	 */
	public String getID() {
		return getClass().getName(); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#getDesc()
	 */
	public String getDesc() {
		return "AnimationView.0";
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#populate()
	 */
	public void populate() {
		Font font = new Font("dialog", Font.BOLD, 40);
		btl1 = new BasicTextLabel(" ");
		btl1.setFont(font);
	    btl1.setOpaque(false);
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBackground(Color.white);
        add(btl1);
        ObservationManager.register(EVENT_INFORMATION_DISPLAY,this);
        ObservationManager.register(EVENT_ANIMATION_DISPLAY,this);
   }

	/**Set the text to be displayed**/
	public void setText(String sText){
		if ( animator!=null ){
			animator.stop();
		}
		Animation anim = BasicTextAnimation.defaultFade(btl1,DEFAULT_DURATION,sText,Color.darkGray);
		anim = Animations.repeat(Float.POSITIVE_INFINITY,anim);
		animator = new Animator(anim,DEFAULT_FRAME_RATE);
		animator.start();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject) {
		if (subject.equals(EVENT_INFORMATION_DISPLAY)){
			String s = InformationJPanel.getInstance().getMessage();
			if (s != null  && !s.trim().equals("")){
			    setText(s);   
			}
		}
	}
	
	private int getFont(String s){
	    //FONT 22, 10% = 1/3
	    StringTokenizer st = new StringTokenizer(s,"\n");
	    int iLines = st.countTokens();
	    return 0;
	}
	
}
