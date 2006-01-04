/*
 * Author: Bart Cremers (Real Software)
 * Date: 4-jan-2006
 * Time: 08:22:46
 */
package org.jajuk.ui.action;

import java.awt.event.ActionEvent;
import java.util.Properties;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.base.*;

/**
 * @author Bart Cremers
 * @since 4-jan-2006
 */
public class FinishAlbumAction extends ActionBase {

    FinishAlbumAction() {
        super(Util.getIcon(ICON_MODE_NORMAL), FIFO.getInstance().getCurrentItem() != null);
        setShortDescription(Messages.getString("CommandJPanel.17")); //$NON-NLS-1$
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
