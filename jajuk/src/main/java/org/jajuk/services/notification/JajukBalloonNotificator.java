
package org.jajuk.services.notification;

import org.jajuk.ui.widgets.JajukTrackChangeBalloon;

public class JajukBalloonNotificator implements ISystemNotificator
{
	@Override
	public boolean isAvailable()
	{
		return true;
	}

	@Override
	public void notify(String title, String text)
	{System.err.println("NOTIFY");
		new JajukTrackChangeBalloon(text, 3000).display();
	}
}
