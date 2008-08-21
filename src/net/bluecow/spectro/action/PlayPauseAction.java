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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.bluecow.spectro.PlayerThread;

public class PlayPauseAction extends AbstractAction {

    private final PlayerThread playerThread;

    public PlayPauseAction(PlayerThread playerThread) {
        super("Play");
        this.playerThread = playerThread;
    }

    public void actionPerformed(ActionEvent e) {
        if ("Play".equals(getValue(NAME))) {
            playerThread.startPlaying();
            putValue(NAME, "Pause");
        } else if ("Pause".equals(getValue(NAME))) {
            playerThread.stopPlaying();
            putValue(NAME, "Play");
        }
    }
}
