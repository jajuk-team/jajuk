package org.jajuk.test;

import java.util.Iterator;

import junit.framework.TestCase;

import org.jajuk.Main;
import org.jajuk.base.DigitalDJ;
import org.jajuk.base.DigitalDJManager;

public class TestLoad extends TestCase {
	
	public void testLoad(){
		Main.main(new String[]{"-ide","-test"});
		DigitalDJManager.loadAllDJs();
		Iterator it = DigitalDJManager.getInstance().getDJs().iterator();
		while (it.hasNext()){
			DigitalDJ dj = (DigitalDJ)it.next();
			System.out.println(dj);
		}
	}

}
