/*
 * Metro Musical Sequencing Framework written by Atsushi Oka 
 * Copyright 2018 Atsushi Oka
 *
 * This file is part of Metro Musical Sequencing Framework. 
 * 
 * Metro Musical Sequencing Framework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Metro Musical Sequencing Framework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Metro Musical Sequencing Framework.  If not, see <https://www.gnu.org/licenses/>.
 */

package ats.metro;

import java.util.List;

/**
 * This interface provides common methods for objects that represents every
 * notes in a bar. Bars are usually generated by the tracks. This interface is
 * strongly related to {@link MetroTrack#progressCursor(int, List) } method;
 * refer the {@linkplain MetroTrack#progressCursor(int, List) method} for
 * further information.
 * <p>
 * These methods are called as a callback of JACKAudio processing; these method
 * should return as soon as possible. The heavy processing that blocks for
 * longer time than the current setting of JACK's frame rate causes JACK to
 * XRUN.
 * 
 * @author Ats Oka
 */
public interface MetroAbstractEvent {
	/**
	 * Check if the position of this event is inside the duration specified in the
	 * parameter. See {@link MetroTrack#progressCursor(int, List) } for further
	 * information.
	 * 
	 * This methods is called as a callback of JACKAudio processing; this method
	 * should return as soon as possible. The heavy processing that blocks for
	 * longer time than the current setting of JACK's frame rate causes JACK to
	 * XRUN.
	 * 
	 * @param from
	 *            Specifies the beginning point of the duration to check. The value
	 *            is inclusive.
	 * @param to
	 *            Specifies the end point of the duration to check. The value is
	 *            exclusive.
	 * @return <code>true</code> if this event is inside the duration.
	 */
	boolean between(int from, int to);

	/**
	 * Defines the procedure to execute when this event is activated. This method is
	 * usually called when {@link #between(int, int)} returned <code>true</code>.
	 * See {@link MetroTrack#progressCursor(int, List) } for further information.
	 * 
	 * This methods is called as a callback of JACKAudio processing; this method
	 * should return as soon as possible. The heavy processing that blocks for
	 * longer time than the current setting of JACK's frame rate causes JACK to
	 * XRUN.
	 * 
	 * @param metro
	 *            The Metro instance which is the owner of this event.
	 * @param from
	 *            the value of <code>from</code> when {@link #between(int, int)}
	 *            returns <code>true</code>.
	 * @param to
	 *            the value of <code>to</code> when {@link #between(int, int)}
	 *            returns <code>true</code>.
	 * @param nframes
	 *            the current
	 * @param eventList
	 */
	void process(Metro metro, int from, int to, int nframes, List<MetroAbstractMidiEvent> eventList);
}