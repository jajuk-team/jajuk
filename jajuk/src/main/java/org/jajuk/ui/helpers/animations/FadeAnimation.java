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

public class FadeAnimation extends AbstractAnimation
{
	private Direction opacity;

	public FadeAnimation(Window window, Direction opacity)
	{
		super(window);
		this.opacity = opacity;
	}

	@Override
	public void animate(final int animationTime)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					for (int i = 0; i < 20; i++)
					{
						final float progress = i / 20.0f;
						java.awt.EventQueue.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								AWTUtilities.setWindowOpacity(window, opacity.getOpacity(progress));
							}
						});
						Thread.sleep(animationTime / 20);
						java.awt.EventQueue.invokeLater(new Runnable()
						{
							@Override
							public void run()
							{
								AWTUtilities.setWindowOpacity(window, opacity.getOpacity(1));
							}
						});
					}
				}
				catch (Exception ex)
				{

				}
				animationCompleted();
			}
		}).start();
	}

	public interface Direction
	{
		float getOpacity(float progress);
	}

	public enum Directions implements Direction
	{
		IN
		{
			@Override
			public float getOpacity(float progress)
			{
				return progress;
			}
		},
		OUT
		{
			@Override
			public float getOpacity(float progress)
			{
				return 1 - progress;
			}
		};
	}
}
