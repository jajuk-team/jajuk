package org.jajuk.test;

import java.util.HashMap;
import java.util.HashSet;

import org.jajuk.Main;
import org.jajuk.base.DigitalDJManager;
import org.jajuk.base.ProportionDigitalDJ;
import org.jajuk.base.StyleManager;

import junit.framework.TestCase;

public class ProportionDigitalDJTest extends TestCase {

	/*
	 * Test method for 'org.jajuk.base.ProportionDigitalDJ.toXML()'
	 */
	public void testToXML() {
        Main.main(new String[]{"-ide","-test"});
		ProportionDigitalDJ dj1 = new ProportionDigitalDJ("test prop");
        dj1.setFadingDuration(10);
        dj1.setRatingLevel(3);
        dj1.setUseRatings(true);
        HashSet<org.jajuk.base.Style> styles1 = new HashSet();
        styles1.add(StyleManager.getInstance().registerStyle("123","ROCK"));
        styles1.add(StyleManager.getInstance().registerStyle("454","POP"));
        HashSet<org.jajuk.base.Style> styles2 = new HashSet();
        styles2.add(StyleManager.getInstance().registerStyle("854","CLASSICAL"));
        styles2.add(StyleManager.getInstance().registerStyle("1547","JAZZ"));
        HashMap<HashSet<org.jajuk.base.Style>,Float> hm = new HashMap();
        hm.put(styles1,0.2f);
        hm.put(styles2,0.4f);
       // dj1.setProportions(hm);
        DigitalDJManager.getInstance().commit(dj1);
	}

}
