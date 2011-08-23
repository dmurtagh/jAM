package be.hogent.tarsos.sampled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * This AudioProcessor can be used to sync events with sound. It uses a pattern
 * described in JavaFX Special Effects Taking Java RIA to the Extreme with
 * Animation, Multimedia, and Game Element Chapter 9 page 185: <blockquote><i>
 * The variable line is the Java Sound object that actually makes the sound. The
 * write method on line is interesting because it blocks until it is ready for
 * more data. </i></blockquote> If this AudioProcessor chained with other
 * AudioProcessors the others should be able to operate in real time or process
 * the signal on a separate thread.
 * 
 * @author Joren Six
 */
public final class BlockingAudioPlayer implements AudioProcessor {

	/**
	 * The line to send sound to. Is also used to keep everything in sync.
	 */
	private SourceDataLine line;

	/**
	 * The overlap and step size defined not in samples but in bytes. So it
	 * depends on the bit depth. Since the integer data type is used only
	 * 8,16,24,... bits or 1,2,3,... bytes are supported.
	 */
	private final int byteOverlap, byteStepSize;

	/**
	 * Creates a new BlockingAudioPlayer.
	 * 
	 * @param format
	 *            The AudioFormat of the buffer.
	 * @param bufferSize
	 *            The size of each buffer in samples (not in bytes).
	 * @param overlap
	 *            Defines how much consecutive buffers overlap in samples (not
	 *            in bytes).
	 * @throws LineUnavailableException
	 *             If no output line is available.
	 */
	public BlockingAudioPlayer(final AudioFormat format, final int bufferSize,
			final int overlap) throws LineUnavailableException {
		line = SampledAudioUtilities.getOpenLineFromConfiguredMixer(format);
		// overlap in samples * nr of bytes / sample = bytes overlap
		this.byteOverlap = overlap * format.getFrameSize();
		this.byteStepSize = bufferSize * format.getFrameSize() - byteOverlap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * be.hogent.tarsos.util.RealTimeAudioProcessor.AudioProcessor#processFull
	 * (float[], byte[])
	 */
	Thread thread;
	float[] audioFloatBuffer;
	byte[] audioByteBuffer;

	public void processFull(final float[] audioFloatBuffer,
			final byte[] audioByteBuffer) {
		// Play the first full buffer
		line.write(audioByteBuffer, 0, audioByteBuffer.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * be.hogent.tarsos.util.RealTimeAudioProcessor.AudioProcessor#proccess(
	 * float[], byte[])
	 */
	public void processOverlapping(final float[] audioFloatBuffer,
			final byte[] audioByteBuffer) {
		// Play only the audio that has not been played yet.
		line.write(audioByteBuffer, byteOverlap, byteStepSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see be.hogent.tarsos.util.RealTimeAudioProcessor.AudioProcessor#
	 * processingFinished()
	 */
	public void processingFinished() {
		// cleanup
		line.drain();
		line.close();
	}
}
