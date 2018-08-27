package ats.metro;

import java.util.Arrays;

public class MetroNoteEvent extends MetroEvent {
	final int outputPortNo;
	byte[] data;
	public MetroNoteEvent( double offset, int outputPortNo, byte[] data ) {
		super( offset );
		this.outputPortNo = outputPortNo;
		this.data = data;
	}
	public final int getOutputPortNo() {
		return outputPortNo;
	}
	public byte[] getData() {
		return data;
	}
	public void dumpProc( String prefix, StringBuilder sb ) {
		MetroNoteEvent e = this;
		sb.append(prefix).append( "      outputPortNo: " + e.outputPortNo ).append( "\n" );
		sb.append(prefix).append( "              data: " + Arrays.toString( e.data ) ).append( "\n" );
	}


//	public static void main(String[] args) {
//		MetroMidiEvent event = new MetroMidiEvent(1, 0.0d, new byte[] {} );
//		event.calcInFrames(48000);
//		boolean b = event.between(-1, 4);
//		System.out.println( b );
//	}
}