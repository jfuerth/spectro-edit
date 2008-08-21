/*
 * Created on Aug 19, 2008
 *
 * Spectro-Edit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spectro-Edit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package net.bluecow.spectro.action;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import net.bluecow.spectro.Clip;

public class SaveAction extends AbstractAction {

    private final Clip clip;
    private final Component dialogOwner;

    public SaveAction(Clip clip, Component dialogOwner) {
        super("Save...");
        this.clip = clip;
        this.dialogOwner = dialogOwner;
        if (dialogOwner == null) {
            throw new NullPointerException(
                    "You have to specify an owning component for the save dialog");
        }
    }

    public void actionPerformed(ActionEvent e) {
        try {
            FileDialog fd;
            Window owner;
            if (dialogOwner instanceof Window) {
                owner = (Window) dialogOwner;
            } else {
                owner = SwingUtilities.getWindowAncestor(dialogOwner);
            }
            if (owner instanceof java.awt.Frame) {
                fd = new FileDialog((java.awt.Frame) owner, "Save sample as", FileDialog.SAVE);
            } else {
                fd = new FileDialog((Dialog) owner, "Save sample as", FileDialog.SAVE);
            }
            fd.setVisible(true);
            String dir = fd.getDirectory();
            String file = fd.getFile();
            if (file == null) return;
            if (!file.toLowerCase().endsWith(".wav")) {
                file += ".wav";
            }
            AudioSystem.write(
                    clip.getAudio(),
                    AudioFileFormat.Type.WAVE,
                    new File(dir, file));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
