package be.hogent.tarsos.sampled.filters;

/**
 * Four stage low pass filter.
 *
 */
public class LowPassFS extends IIRFilter{
	
	public LowPassFS(float freq, float sampleRate, int overlap) {
		//minimum frequency is 60Hz!
		super(freq>60?freq:60, sampleRate, overlap);
	}

	@Override
	protected void calcCoeff() {
		float freqFrac = getFrequency() / getSampleRate();
		float x = (float) Math.exp(-14.445 * freqFrac);
		a = new float[] { (float) Math.pow(1 - x, 4) };
		b = new float[] { 4 * x, -6 * x * x, 4 * x * x * x, -x * x * x * x };
	}
}
