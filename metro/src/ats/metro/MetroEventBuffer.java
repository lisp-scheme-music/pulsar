package ats.metro;

import static ats.metro.Metro.DEBUG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaudiolibs.jnajack.JackClient;
import org.jaudiolibs.jnajack.JackException;
import org.jaudiolibs.jnajack.JackPosition;

public class MetroEventBuffer implements Iterable<MetroEvent>{
    static final Logger LOGGER = Logger.getLogger(MetroEventBuffer.class.getName());
	static void logError(String msg, Throwable e) {
		LOGGER.log(Level.SEVERE, msg, e);
	}
	static void logInfo(String msg) {
		// LOGGER.log(Level.INFO, msg);
		System.err.println(msg);
	}
	static void logWarn(String msg) {
		LOGGER.log(Level.WARNING, msg);
	}

	
	private double humanizeOffset_min = 0;
	private double humanizeOffset_max = 0;
	private double humanizeOffset_size = 0;
	private double humanizeVelocity_min = 0;
	private double humanizeVelocity_max = 0;
	private double humanizeVelocity_size = 0;

	public void setHumanizeOffset( double min, double max ) {
		this.humanizeOffset_min = min;
		this.humanizeOffset_max = max;
		humanizeOffset_size = this.humanizeOffset_max - this.humanizeOffset_min;
	}
	public void setHumanizeVelocity( double min, double max ) {
		this.humanizeVelocity_min = min;
		this.humanizeVelocity_max = max;
		this.humanizeVelocity_size = this.humanizeVelocity_max - this.humanizeVelocity_min;
	}

	
	/*
	 * NOT USED
	 */
	@Deprecated
	private double offset;
	private double length = 1.0d;
	private boolean prepared = false;
	private int barLengthInFrames=-1;
	private int lengthInFrames = -1;
	private final List<MetroEvent> list = new ArrayList<MetroEvent>(10);
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		if (DEBUG) logInfo( "setLength():" + length );
		this.length = length;
	}
	public double getActualLength() {
		double max = 0;
		for ( MetroEvent e : this )
			if ( max < e.barOffset ) 
				max = e.barOffset;
		
		return max;
	}
	/*
	 * NOT USED
	 */
	@Deprecated
	public void setOffset(double offset) {
		this.offset = offset;
	}
	/*
	 * NOT USED
	 */
	@Deprecated
	public double getOffset() {
		return offset;
	}
	public int getBarLengthInFrames() {
		if ( ! prepared )
			throw new RuntimeException("not prepared");
		return barLengthInFrames;
	}
	public int getLengthInFrames() {
		if ( ! prepared )
			throw new RuntimeException("not prepared");
		return lengthInFrames;
	}
	
	public void prepare( Metro metro, JackClient client, JackPosition position, boolean doSort ) throws JackException {
		if ( doSort )
			this.list.sort( MetroMidiEvent.comparator );
		int barInFrames = Metro.calcBarInFrames( metro, client, position );
		this.calcInFrames( barInFrames );
	}
	
	private void calcInFrames( int barLengthInFrames ) {
//		System.out.println("MetroMidiEventBuffer.calcInFrames() barInFrames="  + barInFrames );
		for ( MetroEvent e : this ) {
			e.calcInFrames( barLengthInFrames );
		}
//		System.out.println( "this.length " + this.length  );
		this.barLengthInFrames = barLengthInFrames;
		this.lengthInFrames = (int) (this.length * (double)barLengthInFrames);
		this.prepared = true;
		
		if ( DEBUG ) 
			logInfo( "MetroMidiEventBuffer.calcInFrames() barInFrames="  + barLengthInFrames + " / lengthInFrames=" + this.lengthInFrames  + "/ length=" + this.length);
	}
	
	@Override
	public Iterator<MetroEvent> iterator() {
		return this.list.iterator();
	}
	
	public int size() {
		return this.list.size();
	}

	public final void event( MetroEvent event ) {
		// Add it to the list.
		this.list.add(event);
	}
	
	public final void midiEvent( double offset, int outputPortNo, byte[] data ) {
		// Create an event object.
		MetroMidiEvent event = new MetroMidiEvent( offset, outputPortNo, data );
		
		// Add it to the list.
		this.list.add(event);
	}
	
	private void note(int outputPortNo, int midiEventValue, double offset, int channel, int note, double velocity) {
		/*
		 * DON'T CHECK MIN/MAX HERE
		 * The offset may go beyond the minimum/maximum; now 
		 * {@link MetroEventBuffer} can process the MIDI 
		 * signals which are beyond the region of the buffer.   
		 */
		// if ( offset < 0 )  offset=0;
		// if ( 127 < offset  ) offset=127;

		if ( velocity < 0 )  velocity =0d;
		if ( 1d < velocity ) velocity =1d;

		// Create an event object.
		MetroMidiEvent event = new MetroMidiEvent(
				offset,
				outputPortNo,
				new byte[] {
						(byte)( ( 0b11110000 & midiEventValue ) | ( 0b00001111 & channel ) ),
						(byte) note,
						(byte) (127d * velocity)
				}
		);
		
		// Add it to the list.
		this.list.add(event);
	}

	public void noteHit( double offset, int outputPortNo, int channel, int note, double velocity ) {
		noteHit( offset, outputPortNo, channel, note, velocity, -1 );
	}
	public void noteHit( double offset, int outputPortNo, int channel, int note, double velocity, double duration ) {
		if ( duration < 0 )
			duration = 0.0025d;
		
		noteOn(  offset,            outputPortNo, channel, note, velocity );
		noteOff( offset + duration, outputPortNo, channel, note, velocity );
	}

	MetroNoteInfoMap noteInfoMap = new MetroNoteInfoMap();
	public void noteOn( double offset, int outputPortNo, int channel, int note, double velocity ) {
		double humanizeOffset =  this.humanizeVelocity_min;
		double humanizeVelocity =  this.humanizeVelocity_min;
		if ( humanizeOffset_size != 0.0d ) {
			humanizeOffset =+ ( Math.random() * this.humanizeOffset_size );
		}
		if ( this.humanizeVelocity_size != 0.0d ) {
			humanizeVelocity =+ ( Math.random() *  this.humanizeVelocity_size );
		}
		
		offset   += humanizeOffset;
		velocity += humanizeVelocity;

		noteInfoMap.put(outputPortNo, channel, note, humanizeOffset, humanizeVelocity);

		note( outputPortNo, 0b10010000, offset, channel, note, velocity );
	}

	public void noteOff( double offset, int outputPortNo, int channel, int note, double velocity ) {
		MetroNoteInfoMap.Value value = noteInfoMap.get(outputPortNo, channel, note );

		note( outputPortNo, 0b10000000, offset + value.offset, channel, note, velocity + value.velocity );
	}
	public void exec( double offset, Runnable runnable ) {
		MetroMessageEvent event = new MetroMessageEvent( offset, runnable );

		this.list.add( event );
	}

//	public void length( double length ) {
//		this.length = length;
//	}
	public void dump() {
		logInfo( "length         : " + this.length        );
		logInfo( "lengthInFrames : " + this.lengthInFrames);
		int i = 0;
		for ( MetroEvent e : this ) {
			logInfo( "    No" + i);
			logInfo( e.dump( "    " ));
			i++;
		}
		logInfo( "    END");
	}
	
	@Deprecated
	public void noteHit( double offset, int outputPortNo, int channel, int note, int velocity ) {
	}
	@Deprecated
	public void noteHit( double offset, int outputPortNo, int channel, int note, int velocity, double duration ) {
	}
	@Deprecated
	public void noteOn( double offset, int outputPortNo, int channel, int note, int velocity ) {
	}
	@Deprecated
	public void noteOff( double offset, int outputPortNo, int channel, int note, int velocity ) {
	}
}


class MetroNoteInfoMap {
	static final Value ZERO_VALUE = new Value(0, 0);
	public static class Key {
		final int port;
		final int channel;
		final int note;
		public Key(int port, int channel, int note) {
			super();
			this.port = port;
			this.channel = channel;
			this.note = note;
		}
		@Override
		public int hashCode() {
			return ( 1 * channel * port * 256 + note * 65536 ) ;
		}
		@Override
		public boolean equals(Object obj) {
			if ( obj instanceof Key ) {
				Key k = (Key)obj;
				return 
						( k.port == this.port ) &&
						( k.channel == this.channel ) &&
						( k.note == this.note );
			} else {
				return false;
			}
		}
	}
	public static class Value {
		final double offset;
		final double velocity;
		public Value(double offset, double velocity) {
			super();
			this.offset = offset;
			this.velocity = velocity;
		}
	}
	
	final HashMap<Key,Value> map = new HashMap<>();
	public void put( int port, int channel, int note , double offset, double velocity ) {
		map.put( new Key(port, channel, note), new Value(offset, velocity) );
	}
	public boolean containsKey( int port, int channel, int note ) {
		return map.containsKey( new Key(port, channel, note) );
	}
	public MetroNoteInfoMap.Value get( int port, int channel, int note ) {
		Value value = map.get( new Key(port, channel, note) );
		return value == null  ? ZERO_VALUE : value;
	}
	public void clear() {
		this.map.clear();
	}
}

