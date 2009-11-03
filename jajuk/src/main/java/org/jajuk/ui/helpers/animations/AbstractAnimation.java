/*
 *  Jajuk
 *  Copyright (C) 2009 The Jajuk Team
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
 * $Revision: 2921 $
 */

package org.jajuk.ui.helpers.animations;

import java.awt.Window;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 
 */
public abstract class AbstractAnimation implements Animation
{
	protected Window window;

	private CopyOnWriteArrayList<AnimationCompletedListener> listeners = new CopyOnWriteArrayList<AnimationCompletedListener>();
	
	protected AbstractAnimation(Window window)
	{
		this.window = window;
	}

	public Window getWindow()
	{
		return window;
	}

	public void addAnimationCompletedListener(AnimationCompletedListener listener)
	{
		listeners.add(listener);
	}

	public void removeAnimationCompletedListener(AnimationCompletedListener listener)
	{
		listeners.remove(listener);
	}

	protected void animationCompleted()
	{
		AnimationCompletedEvent event = new AnimationCompletedEvent(this, window);
		for (AnimationCompletedListener listener : listeners)
		{
			listener.animationCompleted(event);
		}
	}
}