/*
 * Created on Aug 21, 2008
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

import java.util.logging.Logger;

import javax.swing.event.UndoableEditEvent;

public class UndoManager extends javax.swing.undo.UndoManager {

    private static final Logger logger = Logger.getLogger(UndoManager.class.getName());
    
    public UndoManager() {
        super();
        setLimit(1000);
    }
    
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        logger.finest("Got undoable edit: " + e.getEdit());
        super.undoableEditHappened(e);
        logger.fine("Added edit " + edits.size() + "/" + getLimit());
    }

}
