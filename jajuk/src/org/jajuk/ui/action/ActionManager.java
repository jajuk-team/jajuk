/*
 *  Jajuk
 *  Copyright (C) 2005 Bart Cremers
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
 *  $$Revision$$
 */
package org.jajuk.ui.action;

import java.util.EnumMap;

import static org.jajuk.ui.action.JajukAction.*;

/**
 * Helper class used to create, store and lookup actions.
 *
 * @author Bart Cremers
 * @version 12-dec-2005
 */
public final class ActionManager {

    private static final EnumMap<JajukAction, ActionBase> map =
        new EnumMap<JajukAction, ActionBase>(JajukAction.class);

    static {

        // CommandJPanel: Mode Panel
        map.put(REPEAT_MODE_STATUS_CHANGE, new RepeatModeAction());
        map.put(SHUFFLE_MODE_STATUS_CHANGED, new ShuffleModeAction());
        map.put(CONTINUE_MODE_STATUS_CHANGED, new ContinueModeAction());
        map.put(INTRO_MODE_STATUS_CHANGED, new IntroModeAction());

        // CommandJPanel: Special Functions Panel
        map.put(SHUFFLE_GLOBAL, new GlobalRandomAction());
        map.put(BEST_OF, new BestOfAction());
        map.put(NOVELTIES, new NoveltiesAction());
        map.put(FINISH_ALBUM, new FinishAlbumAction());

        // CommandJPanel: Play Panel
        map.put(PREVIOUS_TRACK, new PreviousTrackAction());
        map.put(NEXT_TRACK, new NextTrackAction());
        map.put(PREVIOUS_ALBUM, new PreviousAlbumAction());
        map.put(NEXT_ALBUM, new NextAlbumAction());
        map.put(REWIND_TRACK, new RewindTrackAction());
        map.put(PLAY_PAUSE_TRACK, new PlayPauseAction());
        map.put(STOP_TRACK, new StopTrackAction());
        map.put(FAST_FORWARD_TRACK, new FastForwardTrackAction());

        // CommandJPanel: Volume control
        map.put(DECREASE_VOLUME, new DecreaseVolumeAction());
        map.put(INCREASE_VOLUME, new IncreaseVolumeAction());
        map.put(MUTE_STATE, new MuteAction());

        // JajukJMenuBar: Help Menu
        map.put(HELP_REQUIRED, new HelpRequiredAction());
        map.put(SHOW_ABOUT, new ShowAboutAction());
        map.put(WIZARD, new WizardAction());
        map.put(QUALITY, new QualityAction());
        map.put(TIP_OF_THE_DAY, new TipOfTheDayAction());
    }

    private ActionManager() {
        // Private constructor to disallow instantiation.
    }

    /**
     * @param action The <code>JajukAction</code> to get.
     * @return The <code>ActionBase</code> implementation linked to the <code>JajukAction</code>.
     */
    public static ActionBase getAction(JajukAction action) {
        ActionBase actionBase = map.get(action);
        if (actionBase == null) {
            throw new ExceptionInInitializerError("No action mapping found for " + action);
        }
        return actionBase;
    }
}