package model;

/**
 * A Problem describes an book embedding problem and consists of a {@link Graph}
 * and a maximal number of pages. It can also contain the minimal number of
 * crossings possible for this problem.
 * 
 * @see Graph
 * @see Embedding
 */
public class Problem {

	/** The graph on which this problem is based */
	private final Graph graph;
	/** The number of pages in the problem, also known as <i>k</i>. */
	private int numberOfPages;

	/** The minimal number of crossings possible, if known. */
	private final long crossingNumber;
	public static int UNKNOWN_NUMBER_OF_CROSSINGS = -1337;

	public Problem(Graph graph, int numberOfPages) {
		this(graph, numberOfPages, -1);
	}

	public Problem(Graph graph, int numberOfPages, long crossingNumber) {
		this.graph = graph;
		setK(numberOfPages);
		this.crossingNumber = crossingNumber;
	}

	public Graph getGraph() {
		return graph;
	}

	public int getK() {
		return numberOfPages;
	}

	/**
	 * Reset the number of pages <i>k</i>. Use with caution!
	 * 
	 * @param k
	 *            new number of pages
	 */
	public void setK(int k) {
		if (k < 1) {
			throw new IllegalArgumentException("k in problem to small.");
		}
		this.numberOfPages = k;
	}

	public int getM() {
		return graph.getM();
	}

	public int getN() {
		return graph.getN();
	}

	public boolean isCrossingNumberKnown() {
		return (getCrossingNumber() >= 0);
	}

	public long getCrossingNumber() {
		return crossingNumber;
	}

	@Override
	public String toString() {
		return "Problem n = " + getN() + ", m = " + getM() + ", k = " + getK() + " Graph: "
				+ graph.getName();
	}

}
