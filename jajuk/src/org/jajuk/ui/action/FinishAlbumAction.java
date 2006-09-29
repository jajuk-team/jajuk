/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:22:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.jajuk.base.Directory;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.StackItem;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

/**
 * @author Bart Cremers
 * @since 4-jan-2006
 */
public class FinishAlbumAction extends ActionBase {

    private static final long serialVersionUID = 1L;

    FinishAlbumAction() {
        super(Messages.getString("JajukWindow.16"), Util.getIcon(ICON_MODE_NORMAL), //$NON-NLS-1$
              FIFO.getInstance().getCurrentItem() != null);
        setShortDescription(Messages.getString("JajukWindow.32")); //$NON-NLS-1$
    }

    public void perform(ActionEvent evt) throws JajukException {
        StackItem item = FIFO.getInstance().getCurrentItem();//stores current item
        FIFO.getInstance().clear(); //clear fifo
        Directory dir = item.getFile().getDirectory();
        FIFO.getInstance().push(Util.createStackItems(dir.getFilesFromFile(item.getFile()),
                                                      item.isRepeat(), item.isUserLaunch()),
                                true); //then re-add current item
        FIFO.getInstance().computesPlanned(true); //update planned list
        Properties properties = new Properties();
        properties.put(DETAIL_ORIGIN, DETAIL_SPECIAL_MODE_NORMAL);
        ObservationManager.notify(new Event(EVENT_SPECIAL_MODE, properties));
    }
}
