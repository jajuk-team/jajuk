/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
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
 */
package org.jajuk.ui.wizard.prepare_party;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Type;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.wizard.prepare_party.PreparePartyWizard.Variable;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.filters.DirectoryFilter;
import org.qdwizard.Screen;

/**
 * Panel for selecting the location in the filesystem.
 */
public class PreparePartyWizardPathSelectionScreen extends Screen implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -236180699495019177L;
  /** Button for file chooser dialog. */
  JButton jbFileSelection;
  /** The selected file. */
  JLabel jlSelectedFile;
  /** Selected directory. */
  private File fDir;

  @Override
  public void initUI() {
    JLabel jlFileSelection = new JLabel(Messages.getString("PreparePartyWizard.20"));
    jbFileSelection = new JButton(IconLoader.getIcon(JajukIcons.OPEN_DIR));
    jbFileSelection.addActionListener(this);
    JLabel jlSelectedFileText = new JLabel(Messages.getString("PreparePartyWizard.21"));
    jlSelectedFile = new JLabel(Messages.getString("FirstTimeWizard.9"));
    jlSelectedFile.setBorder(new BevelBorder(BevelBorder.LOWERED));
    // previous value if available
    if (data.containsKey(Variable.DEST_PATH)) {
      jlSelectedFile.setText((String) data.get(Variable.DEST_PATH));
      // we also can finish the dialog
      setCanFinish(true);
    } else {
      setProblem(Messages.getString("PreparePartyWizard.22"));
      // now we can not finish the dialog
      setCanFinish(false);
    }
    // Add items
    setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[][grow]"));
    add(jlFileSelection);
    add(jbFileSelection, "wrap,center");
    add(jlSelectedFileText);
    add(jlSelectedFile, "grow,wrap");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // display a FileChooser
    if (e.getSource() == jbFileSelection) {
      JajukFileChooser jfc = new JajukFileChooser(
          new JajukFileFilter(DirectoryFilter.getInstance()), fDir);
      jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      jfc.setDialogTitle(Messages.getString("PreparePartyWizard.22"));
      jfc.setMultiSelectionEnabled(false);
      final int returnVal = jfc.showOpenDialog(this);
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        // retrieve selected directory and update it in all necessary places
        fDir = jfc.getSelectedFile();
        jlSelectedFile.setText(fDir.getAbsolutePath());
        data.put(Variable.DEST_PATH, fDir.getAbsolutePath());
        // we can finish the wizard now
        setProblem(null);
        // now we can finish the dialog
        setCanFinish(true);
      }
    }
  }

  @Override
  public String getDescription() {
    return Messages.getString("PreparePartyWizard.19");
  }

  @Override
  public String getName() {
    return Messages.getString("PreparePartyWizard.18");
  }
}

/**
 * Compare two types.
 */
final class TypeComparator implements Comparator<Type> {
  @Override
  public int compare(Type o1, Type o2) {
    // handle null, always equal
    if (o1 == null || o2 == null) {
      return 0;
    }
    // otherwise sort on extension here
    return o1.getExtension().compareTo(o2.getExtension());
  }
}
