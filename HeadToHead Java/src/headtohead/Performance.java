package headtohead;

import java.util.ArrayList;
import java.util.List;

public abstract class Performance {
	
	private static List<Long> measuredTimes;
	private static List<String> labels;
	private static List<Double> averageTimes;
	
	static {
		measuredTimes = new ArrayList<Long>();
		labels = new ArrayList<String>();
		averageTimes = new ArrayList<Double>();
	}
	
	private Performance() {}
	
	/**
	 * Clears all fields.
	 */
	public static void clear() {
		measuredTimes.clear();
		labels.clear();
		averageTimes.clear();
	}
	
	public static void measure(String label) {
		measuredTimes.add(Long.valueOf(System.nanoTime()));
		if (measuredTimes.size() > labels.size()) {
			labels.add(label);
		}
		if (measuredTimes.size() > averageTimes.size()) {
			averageTimes.add(Double.valueOf(0d));
		}
	}
	
	public static void report() {
		for (int i = 0; i < measuredTimes.size(); i++) {
			// Print the label
			System.out.print(labels.get(i) + (i != 0 ? "\t" : "\n"));
			
			if (i == 0) continue;
			
			// Add the measured time to the average
			long elapsedNanos = measuredTimes.get(i).longValue() -
					measuredTimes.get(i - 1).longValue();
			double newAverage = smooth(averageTimes.get(i).doubleValue(), elapsedNanos);
			averageTimes.set(i, Double.valueOf(newAverage));
			
			// Print the average elapsed time
			System.out.println(elapsedNanos + "\t" + newAverage);
		}
		
		// Calculate total elapsed time
		long totalElapsedNanos = measuredTimes.get(measuredTimes.size() - 1).longValue() -
				measuredTimes.get(0).longValue();
		double newTotalAverage = smooth(averageTimes.get(0).doubleValue(), totalElapsedNanos);
		averageTimes.set(0, Double.valueOf(newTotalAverage));
		
		// Print the total elapsed time
		System.out.println("total\t\t" + totalElapsedNanos + "\t" + newTotalAverage);
		
		// Clear the measurements
		measuredTimes.clear();
	}
	
	private static double smooth(double oldValue, double newValue) {
		final double smoothing = 0.95d;
		return smoothing * oldValue + (1d - smoothing) * newValue;
	}
}
