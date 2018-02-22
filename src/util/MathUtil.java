package util;

import java.util.Arrays;
import java.util.List;

public class MathUtil {

	public static double getAverageL(List<Long> list) {
		if (list.isEmpty()) {
			return -1;
		}
		return list.stream().mapToLong(x -> x).average().getAsDouble();
	}
	
	public static double getAverageD(List<Double> list) {
		return list.stream().mapToDouble(x -> x).average().getAsDouble();
	}

	public static double getAverageL(long[] array) {
		if (array.length == 0) {
			return 0;
		}
		return Arrays.stream(array).mapToDouble(x -> x).average().getAsDouble();
	}

	public static double getAverageD(double[] array) {
		if (array.length == 0) {
			return 0;
		}
		return Arrays.stream(array).average().getAsDouble();
	}
	
	public static int getIndexOfMinimum(long[] array) {
		int minIndex = -1;
		long d = Long.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < d) {
				d = array[i];
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	public static int getIndexOfMaximum(long[] array) {
		int maxIndex = -1;
		long d = Long.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > d) {
				d = array[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}
	
	public static int getIndexOfMinimum(double[] array) {
		int minIndex = -1;
		double d = Double.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < d) {
				d = array[i];
				minIndex = i;
			}
		}
		return minIndex;
	}
	
	public static int getIndexOfMaximum(double[] array) {
		int maxIndex = -1;
		double d = Double.MIN_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] > d) {
				d = array[i];
				maxIndex = i;
			}
		}
		return maxIndex;
	}


	public static long getMinimum(long[] array) {
		long minimum = Long.MAX_VALUE;
		for (int i = 0; i < array.length; i++) {
			if (array[i] < minimum) {
				minimum = array[i];
			}
		}
		return minimum;
	}
}
