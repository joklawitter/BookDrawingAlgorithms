package util;

/**
 * Utility class for strings.
 * 
 * @author Henning Schulz
 *
 */
public final class StringUtils {

	private StringUtils() {
		// Utility class. Should not be instantiated.
	}

	/**
	 * Formats the passed milliseconds into h min s ms.
	 * 
	 * @param millis
	 *            milliseconds to format
	 * @return formatted string
	 */
	public static String formatTimeMillis(long millis) {
		long s = millis / 1000;
		millis = millis - s * 1000;
		long min = s / 60;
		s = s - 60 * min;
		long h = min / 60;
		min = min - 60 * h;

		StringBuilder builder = new StringBuilder();
		return builder.append(h).append(" h ").append(min).append(" min ").append(s).append(" s ").append(millis)
				.append(" ms").toString();
	}

}
