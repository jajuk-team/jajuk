/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $Revision$
 */

package org.jajuk.ui.views;

import ext.SwingWorker;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JMenuItem;

import org.jajuk.base.Album;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.AlbumsTableModel;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.thumbnails.LocalAlbumThumbnail;
import org.jajuk.ui.thumbnails.ThumbnailPopup;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;

/**
 * List collection albums as a table
 */
public class AlbumsTableView extends AbstractTableView {

  private static final long serialVersionUID = 7576455252866971945L;

  private static ThumbnailPopup popup = null;

  public AlbumsTableView() {
    super();
    columnsConf = CONF_ALBUMS_TABLE_COLUMNS;
    editableConf = CONF_ALBUMS_TABLE_EDITION;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("AlbumsTableView.0");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#initUI()
   */
  public void initUI() {
    SwingWorker sw = new SwingWorker() {
      @Override
      public Object construct() {
        AlbumsTableView.super.construct();
        JMenuItem jmiShowAlbumDetails = new JMenuItem(ActionManager
            .getAction(JajukActions.SHOW_ALBUM_DETAILS));
        jmiShowAlbumDetails.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
        JMenuItem jmiReport = new JMenuItem(ActionManager.getAction(JajukActions.CREATE_REPORT));
        jmiReport.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
        // Add this generic menu item manually to ensure it's the last one in
        // the list for GUI reasons
        jtable.getMenu().add(jmiDelete);
        jtable.getMenu().addSeparator();
        jtable.getMenu().add(jmiReport);
        jtable.getMenu().add(jmiShowAlbumDetails);
        jtable.getMenu().addSeparator();
        jtable.getMenu().add(jmiBookmark);
        jtable.getMenu().add(pjmTracks);
        jtable.getMenu().addSeparator();
        jtable.getMenu().add(jmiProperties);
       
        // Add popup feature when mouse rolls over cells
        jtable.addMouseMotionListener(new MouseMotionListener() {
          Album current = null;

          public void mouseMoved(MouseEvent e) {
            if (!Conf.getBoolean(Const.CONF_SHOW_POPUPS)) {
              return;
            }
            // Do not use getLocationOnScreen() method to support JRE 1.5
            java.awt.Point p = MouseInfo.getPointerInfo().getLocation();
            int rowIndex = jtable.rowAtPoint(e.getPoint());
            if (rowIndex < 0) {
              return;
            }
            JajukTableModel model = (JajukTableModel) jtable.getModel();
            rowIndex = jtable.convertRowIndexToModel(rowIndex);
            Album album = (Album) model.getItemAt(rowIndex);
            if (album != null && current != album) {
              current = album;
              String description = new LocalAlbumThumbnail(album, 200, true).getDescription();
              // Close any previous popup
              if (popup != null) {
                popup.dispose();
              }
              popup = new ThumbnailPopup(description, new Rectangle(p, new Dimension(20, -50)),
                  true);
            }
          }

          public void mouseDragged(MouseEvent e) {
          }

        });

        // Add another listener on view borders (outside the table itself) to
        // close popups when leaving the table
        jtable.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseExited(MouseEvent e) {
            // TODO Auto-generated method stub
            super.mouseExited(e);
            // Do not hide popup if still in the table to allow user to mouse
            // mouse over the popup (in this case, a table exit event is thrown)
            if (popup != null
                && !UtilGUI.isOver(jtable.getLocationOnScreen(), jtable
                    .getPreferredScrollableViewportSize())) {
              popup.dispose();
            }
          }
        });
        return null;
      }

      @Override
      public void finished() {
        AlbumsTableView.super.finished();
      }
    };
    sw.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTableView#initTable()
   */
  @Override
  void initTable() {
    jtbEditable.setSelected(Conf.getBoolean(Const.CONF_ALBUMS_TABLE_EDITION));
    // Disable edit button, edition not yet implemented
    jtbEditable.setVisible(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTableView#populateTable()
   */
  @Override
  JajukTableModel populateTable() {
    // model creation
    return new AlbumsTableModel();
  }

}
