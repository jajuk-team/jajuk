package org.jajuk.test;

import java.util.ArrayList;
import java.util.HashSet;

import junit.framework.TestCase;

import org.jajuk.Main;
import org.jajuk.base.DigitalDJManager;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Transition;
import org.jajuk.base.TransitionDigitalDJ;

public class TransitionDigitalDJTest extends TestCase {

	/*
	 * Test method for 'org.jajuk.base.TransitionDigitalDJ.toXML()'
	 */
	public void testToXML() {
		Main.main(new String[]{"-ide","-test"});
		TransitionDigitalDJ dj1 = new TransitionDigitalDJ("test transition");
        dj1.setFadingDuration(10);
        dj1.setRatingLevel(3);
        dj1.setUseRatings(true);
        HashSet from1 = new HashSet();
        from1.add(StyleManager.getInstance().registerStyle("123","ROCK"));
        from1.add(StyleManager.getInstance().registerStyle("454","POP"));
        HashSet to1 = new HashSet();
        to1.add(StyleManager.getInstance().registerStyle("854","CLASSICAL"));
        to1.add(StyleManager.getInstance().registerStyle("1547","JAZZ"));
        Transition transition1 = new Transition(from1,to1,2);
        
        HashSet from2 = new HashSet();
        from2.add(StyleManager.getInstance().registerStyle("123","ROCK"));
        from2.add(StyleManager.getInstance().registerStyle("47854","HOP"));
        HashSet to2 = new HashSet();
        to2.add(StyleManager.getInstance().registerStyle("854878","HOP2"));
        to2.add(StyleManager.getInstance().registerStyle("15414557","HOP3"));
        Transition transition2 = new Transition(from2,to2,3);
        
        ArrayList transitions = new ArrayList();
        transitions.add(transition1);
        transitions.add(transition2);
                
        dj1.setTransitions(transitions);
        dj1.setStartupStyle(StyleManager.getInstance().registerStyle("854878","HOP2"));
        DigitalDJManager.getInstance().commit(dj1);
	}

}
