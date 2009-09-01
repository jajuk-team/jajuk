/*
 * Adapted by Jajuk team
 * Copyright (C) 2003-2009 the Jajuk Team
 * http://jajuk.info
 * 
 * aTunes 1.14.0
 * Copyright (C) 2006-2009 Alex Aranda, Sylvain Gaudard, Thomas Beckers and contributors
 *
 * See http://www.atunes.org/wiki/index.php?title=Contributing for information about contributors
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package org.jajuk.ui.substance;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import javax.swing.AbstractButton;
import javax.swing.border.Border;

import org.jvnet.substance.SubstanceButtonUI;
import org.jvnet.substance.shaper.StandardButtonShaper;
import org.jvnet.substance.shaper.SubstanceButtonShaper;
import org.jvnet.substance.utils.border.SubstanceButtonBorder;

/*
 * based on code from Xtreme Media Player
 */
/**
 * The Class ButtonShaper.
 */
public abstract class ButtonShaper implements SubstanceButtonShaper {

    @Override
    public Shape getButtonOutline(AbstractButton button) {
        return getButtonOutline(button, null, button.getWidth(), button.getHeight(), true);
    }

    @Override
    public GeneralPath getButtonOutline(AbstractButton button, Insets insets) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeneralPath getButtonOutline(AbstractButton button, Insets insets, int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dimension getPreferredSize(AbstractButton button, Dimension uiPreferredSize) {
        if (button.getClientProperty(SubstanceButtonUI.BORDER_COMPUTED) == null) {
            boolean isBorderComputing = (button.getClientProperty(SubstanceButtonUI.BORDER_COMPUTING) != null);
            Border border = button.getBorder();
            int uiw = uiPreferredSize.width;
            int uih = uiPreferredSize.height;
            Insets bi = border.getBorderInsets(button);
            if (!isBorderComputing) {
                button.setBorder(null);
            }
            uiPreferredSize.setSize(uiw - bi.left - bi.right, uih - bi.top - bi.bottom);

            if (!isBorderComputing) {
                button.setBorder(this.getButtonBorder(button));
                button.putClientProperty(SubstanceButtonUI.BORDER_COMPUTED, "");
            }
        }
        return uiPreferredSize;
    }

    @Override
    public Border getButtonBorder(AbstractButton button) {
        return new SubstanceButtonBorder(StandardButtonShaper.class) {
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(0, 0, 0, 0);
            }
        };
    }

    @Override
    public boolean isProportionate() {
        return true;
    }
}
