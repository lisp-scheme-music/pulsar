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

package metro;

import java.util.Comparator;
import java.util.List;

/**
 * TODO this comment needs to be updated.
 * TODO 2 moved from DefaultMetroEvent (Thu, 07 Nov 2019 05:37:14 +0900) 
 * 
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

/**
 * This interface provides common methods for objects that represents every
 * notes in a bar. Bars are usually generated by the tracks. This interface is
 * strongly related to {@link MetroTrack#progressCursor(Metro, long, long, List, List, List, List, List) } method;
 * refer the {@linkplain MetroTrack#progressCursor(Metro, long, long, List, List, List, List, List) method} for
 * further information.
 * <p>
 * These methods are called as a callback of JACKAudio processing; these method
 * should return as soon as possible. The heavy processing that blocks for
 * longer time than the current setting of JACK's frame rate causes JACK to
 * XRUN.
 * 
 * @author Ats Oka
 */
public interface MetroEvent extends MetroEventProcess, MetroEventOutput, MetroEventInFrames, MetroEventDumper {
    boolean isBetween(double from, double to);
    void setBarOffset(double barOffset);
    void prepareBarOffset(int barLengthInFrames);
    double getBarOffset();

    public static final Comparator<MetroEvent> BAR_OFFSET_COMPARATOR = new Comparator<MetroEvent>() {
        @Override
        public int compare( MetroEvent o1, MetroEvent o2) {
            int i;
            i = (int) Math.signum( o1.getBarOffset() - o2.getBarOffset() );
            if (i != 0 )
                return i;
            
            if ( o1 instanceof MetroMidiEvent &&  o2 instanceof MetroMidiEvent ) {
                return MetroMidiEvent.compare((MetroMidiEvent)o1, (MetroMidiEvent)o2 );
            } else {
                return 0;
            }
        }
    };
}
