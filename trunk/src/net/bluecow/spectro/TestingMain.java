/*
 * Created on Jul 16, 2008
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
package net.bluecow.spectro;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import net.bluecow.spectro.tool.ToolboxPanel;

public class TestingMain {

    public static void main(String[] args) throws Exception {
//        Logger.getLogger("").setLevel(Level.FINER);
        final JFrame f = new JFrame("Spectro-Edit " + Version.VERSION);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    FileDialog fd = new FileDialog(f, "Choose a 16-bit mono WAV file");
                    fd.setVisible(true);
                    String dir = fd.getDirectory();
                    String file = fd.getFile();
                    if (dir == null || file == null) {
                        JOptionPane.showMessageDialog(f, "Ok, maybe next time");
                        System.exit(0);
                    }
                    File wavFile = new File(dir, file);
                    Clip c = new Clip(wavFile);
                    ClipPanel cp = new ClipPanel(c);
                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    f.setLayout(new BorderLayout());
                    f.add(new JScrollPane(cp), BorderLayout.CENTER);
                    f.add(new ToolboxPanel(cp).getPanel(), BorderLayout.SOUTH);


                    f.pack();
                    f.setLocationRelativeTo(null);
                    f.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(f,
                            "Sorry, couldn't read your sample:\n" +
                            e.getMessage() +
                            "\nBe sure your file is 16-bit mono!");
                    System.exit(0);
                }
            }
        });
    }
}
