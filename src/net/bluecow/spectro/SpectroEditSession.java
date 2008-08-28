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
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.bluecow.spectro.action.PlayPauseAction;
import net.bluecow.spectro.action.RewindAction;
import net.bluecow.spectro.action.SaveAction;
import net.bluecow.spectro.action.UndoRedoAction;
import net.bluecow.spectro.tool.ToolboxPanel;

public class SpectroEditSession {

    /**
     * The undo manager that keeps track of changes in this session, including
     * the clip data and the state of various tools.
     */
    private final UndoManager undoManager = new UndoManager();

    private final PlayerThread playerThread;

    private final ClipPanel clipPanel;
    
    protected SpectroEditSession(Clip c) throws LineUnavailableException {
        clipPanel = ClipPanel.newInstance(c);
        clipPanel.addUndoableEditListener(undoManager);
        
        playerThread = new PlayerThread(c);
        playerThread.start();

        final JFrame f = new JFrame("Spectro-Edit " + Version.VERSION);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setTopComponent(new ToolboxPanel(this).getPanel());
        splitPane.setBottomComponent(new JScrollPane(clipPanel));
        f.add(splitPane, BorderLayout.CENTER);

        JToolBar toolbar = new JToolBar();
        toolbar.add(new SaveAction(c, f));
        toolbar.add(UndoRedoAction.createUndoInstance(undoManager));
        toolbar.add(UndoRedoAction.createRedoInstance(undoManager));
        toolbar.addSeparator();
        toolbar.add(new PlayPauseAction(playerThread));
        toolbar.add(new RewindAction(playerThread));
        f.add(toolbar, BorderLayout.NORTH);
        
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        f.setSize(
                Math.min(screenSize.width - 50, f.getWidth()),
                Math.min(screenSize.height - 50, f.getHeight()));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
    
    public static SpectroEditSession createSession(File wavFile) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Clip c = new Clip(wavFile);
        SpectroEditSession session = new SpectroEditSession(c);
        c.addUndoableEditListener(session.undoManager);
        return session;
    }
    
    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(SpectroEditSession.class.getResourceAsStream("LogManager.properties"));
        final JFrame f = new JFrame("Dummy frame for owning dialogs");
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
                    createSession(wavFile);
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
    
    public void undo() {
        undoManager.undo();
    }
    
    public void redo() {
        undoManager.redo();
    }

    public ClipPanel getClipPanel() {
        return clipPanel;
    }
    
    public UndoManager getUndoManager() {
        return undoManager;
    }
}
