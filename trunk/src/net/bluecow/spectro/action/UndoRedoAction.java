/*
 * Created on Aug 15, 2008
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.undo.UndoManager;

public class UndoRedoAction extends AbstractAction {

    public static UndoRedoAction createUndoInstance(UndoManager undoManager) {
        return new UndoRedoAction(undoManager, true);
    }
    
    public static UndoRedoAction createRedoInstance(UndoManager undoManager) {
        return new UndoRedoAction(undoManager, false);
    }

    private final boolean undo;
    private final UndoManager undoManager;

    public UndoRedoAction(UndoManager undoManager, boolean undo) {
        super(undo ? "Undo" : "Redo");
        this.undoManager = undoManager;
        this.undo = undo;
    }

    public void actionPerformed(ActionEvent e) {
        if (undo) {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } else {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        }
    }

}
