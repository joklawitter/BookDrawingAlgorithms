package model;

import java.util.Arrays;
import java.util.Comparator;

import algorithms.crossingCalculation.DivideAndConquerCrossingsCalculator;
import algorithms.crossingCalculation.ICrossingCalculator;
import algorithms.crossingCalculation.OpenEdgesCrossingCalculatorGS;
import algorithms.crossingCalculation.SimpleCrossingsCalculator;

/**
 * An Embedding describes a solution for a {@link Problem} by specifying a spine
 * and an edge distribution to the pages.
 * 
 * It also stores the current number of crossings, if known, and provides some
 * operations to work on itself.
 * 
 * @author Jonathan Klawitter
 * @see Graph
 * @see Problem
 * @see <a href="https://en.wikipedia.org/wiki/Book_embedding">Book
 *      Embeddings</a>
 */
public class Embedding {

	/** The problem for which this embedding wants to be a solution. */
	private final Problem p;

	/**
	 * Stores at position i the index of the vertex. spine[3] = 2 means at
	 * position 3 is vertex 2. The numbering begins with zero.
	 */
	private final int[] spine;
	/**
	 * Stores at position i the position of the vertex on the spine.
	 * vertexOnSpine[3] = 2 means that vertex 3 is on position 2 on the spine.
	 * The numbering begins with zero.
	 */
	private final int[] vertexOnSpine;

	/**
	 * Stores for edge i on which page it is: distribution[3] = 2 means
	 * g.edge[3] is on page 2.
	 */
	private int[] distribution;

	private long crossings = -1;

	private ICrossingCalculator crossingCalculator;

	/**
	 * Constructor for the Embedding class using solely the problem. Sets
	 * CrossingCalculator to {@link DivideAndConquerCrossingsCalculator}.
	 * 
	 * @param p
	 *            Problem for which embedding stands
	 */
	public Embedding(Problem p) {
		this.p = p;
		Graph g = p.getGraph();
		this.spine = new int[g.getN()];
		this.vertexOnSpine = new int[g.getN()];
		this.distribution = new int[g.getM()];

		// initialize the spine with trivial layout
		for (int i = 0; i < spine.length; i++) {
			spine[i] = i;
			vertexOnSpine[i] = i;
		}

		this.crossingCalculator = new DivideAndConquerCrossingsCalculator();
	}

	public Embedding(Problem p, int[] spine) {
		this(p);

		setSpine(spine);
	}

	public Embedding(Problem p, int[] spine, int[] distribution) {
		this(p, spine);

		this.distribution = distribution;
	}

	/**
	 * Constructor which clones the given embedding.
	 * 
	 * @param embedding
	 *            old embedding to clone
	 */
	public Embedding(Embedding embedding) {
		p = embedding.p;
		this.vertexOnSpine = embedding.vertexOnSpine.clone();
		this.spine = embedding.spine.clone();
		distribution = embedding.getDistribution().clone();
		crossings = embedding.crossings;
		crossingCalculator = embedding.crossingCalculator;
	}

	// --- CHECKS --
	/**
	 * Checks whether the embedding is valid with
	 * {@link Embedding#isSpineValid()} and
	 * {@link Embedding#isDistributionValid()}.
	 *
	 * @return whether the embedding is valid
	 */
	public boolean isValid() {
		return isSpineValid() && isDistributionValid();
	}

	/**
	 * Checks whether the spine is a valid. This means that
	 * <ul>
	 * <li>it has the correct length</li>
	 * <li>each vertex appears exactly once</li>
	 * <li>{@link Embedding#getPositionOnSpine(int)} and
	 * {@link Embedding#getVertexAtPosition(int)} are inverse to each other</li>
	 * </ul>
	 * 
	 * @return whether the spine is a valid
	 */
	public boolean isSpineValid() {
		int n = getN();
		if (spine.length != n || vertexOnSpine.length != n) {
			return false;
		}

		// spine
		int[] permCheck = new int[spine.length];
		Arrays.fill(permCheck, 0);

		for (int i = 0; i < spine.length; ++i) {
			if (spine[i] < 0 && spine[i] >= spine.length)
				return false;
			permCheck[spine[i]]++;
		}

		for (int i = 0; i < permCheck.length; ++i) {
			if (permCheck[i] != 1) {
				return false;
			}
		}

		// vertexOnSpine
		Arrays.fill(permCheck, 0);

		for (int i = 0; i < vertexOnSpine.length; ++i) {
			if (vertexOnSpine[i] < 0 && vertexOnSpine[i] >= vertexOnSpine.length) {
				return false;
			}
			permCheck[vertexOnSpine[i]]++;
		}

		for (int i = 0; i < permCheck.length; ++i) {
			if (permCheck[i] != 1) {
				return false;
			}
		}

		// both
		for (int i = 0; i < n; i++) {
			if (i != vertexOnSpine[spine[i]]) {
				System.out.println("Error, spine not inverse of vertexOnSpine.");
				return false;
			}
		}

		for (int i = 0; i < spine.length; i++) {
			if (this.getPositionOnSpine(this.getVertexAtPosition(i)) != i) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether the edge distribution is a valid. This means that
	 * <ul>
	 * <li>it has the correct length</li>
	 * <li>each edge is assigned to a valid page</li>
	 * </ul>
	 * 
	 * @return whether the edge distribution is a valid
	 */
	public boolean isDistributionValid() {
		int m = this.getM();
		if (m != distribution.length) {
			System.err.println("Distribution has illegal length: " + distribution.length + " not "
					+ m);
			return false;
		}

		for (int d : distribution) {
			if (d < 0 || d >= this.getK()) {
				return false;
			}
		}

		return true;
	}

	// --- SETTERS - CLONE ---
	/**
	 * Updates the spine of this embedding to the values of the given spine
	 * array. Also sets the inverse of the spine.
	 * 
	 * @param spine
	 *            new spine values
	 */
	public void setSpine(int[] spine) {
		if (this.spine == spine) {
			// do nothing
		} else {
			for (int i = 0; i < spine.length; i++) {
				this.spine[i] = spine[i];
			}
		}
		// create inverse of spine
		for (int i = 0; i < spine.length; i++) {
			vertexOnSpine[spine[i]] = i;
		}

		invalidateNumberOfCrossings();
	}

	/**
	 * Updates the vertex on spine (inverse of spine) of this embedding to the
	 * values of the given array. Also sets the inverse, the spine.
	 * 
	 * @param vertexOnSpine
	 *            new vertexOnSpine, inverse of spine, values
	 */
	public void setVertexOnSpine(int[] vertexOnSpine) {
		if (this.vertexOnSpine != vertexOnSpine) {
			for (int i = 0; i < vertexOnSpine.length; i++) {
				this.vertexOnSpine[i] = vertexOnSpine[i];
			}
		}
		// update inverse of vertexOnSpine
		for (int i = 0; i < vertexOnSpine.length; i++) {
			spine[vertexOnSpine[i]] = i;
		}

		invalidateNumberOfCrossings();
	}

	/**
	 * Sets the distribution to the given distribution.
	 * 
	 * @param distribution
	 *            the distribution to set
	 */
	public void setDistribution(int[] distribution) {
		this.distribution = distribution;
		invalidateNumberOfCrossings();
	}

	/**
	 * Sets the number of crossing for this embedding.
	 * 
	 * @param numberOfCrossingsComputed
	 *            the number of crossing to set
	 */
	public void setNumberOfCrossings(long numberOfCrossingsComputed) {
		this.crossings = numberOfCrossingsComputed;
	}

	/**
	 * Clones the values of the given embedding into this embedding.
	 * 
	 * @param embedding
	 *            the embedding cloned into this one
	 */
	public void cloneInPlace(Embedding embedding) {
		if (this.p != embedding.p) {
			throw new IllegalArgumentException("Problem isn't equal!");
		}
		System.arraycopy(embedding.spine, 0, spine, 0, spine.length);
		System.arraycopy(embedding.distribution, 0, distribution, 0, distribution.length);
		crossings = embedding.crossings;
	}

	/**
	 * Sets the CrossingCalculator used in this embedding to a new given one.
	 * 
	 * @param newCrossingCalculator
	 *            new {@link ICrossingCalculator}
	 */
	public void setCrossingCalculator(ICrossingCalculator newCrossingCalculator) {
		this.crossingCalculator = newCrossingCalculator;
	}

	// --- GENERAL GETTERS ---
	public Problem getProblem() {
		return p;
	}

	public Graph getGraph() {
		return p.getGraph();
	}

	public int getN() {
		return p.getN();
	}

	public int getM() {
		return p.getM();
	}

	public int getK() {
		// alias to getMaxPages
		return p.getK();
	}

	@Override
	public String toString() {
		return p.getGraph().toString() // graph
				+ ",\nspine(v->p)\t" + Arrays.toString(vertexOnSpine)
				+ ",\nspine(p->v)\t"
				+ Arrays.toString(spine) + ",\ndistribution\t" + Arrays.toString(distribution);
	}

	// --- SPINE GETTERS ---
	/**
	 * Returns which vertex (by index) is at the given position on the spine.
	 * 
	 * @param position
	 *            the position on the spine for which the vertex index is
	 *            requested
	 * @return the vertex index on the spine at given position
	 */
	public int getVertexAtPosition(int position) {
		return spine[position];
	}

	public int[] getSpine() {
		return spine;
	}

	/**
	 * Returns the position on the spine of the given vertex (by index).
	 * 
	 * @param vertexIndex
	 *            the index of the vertex for which the position is requested
	 * @return the position on the spine of the given vertex
	 */
	public int getPositionOnSpine(int vertexIndex) {
		return vertexOnSpine[vertexIndex];
	}

	/**
	 * Returns the position on the spine of the given vertex.
	 * 
	 * @param vertex
	 *            the vertex for which the position is requested
	 * @return the position on the spine of the given vertex
	 */
	public int getPositionOnSpine(Vertex vertex) {
		return getPositionOnSpine(vertex.getIndex());
	}

	/**
	 * Returns the inverse of the spine, the vertex on spine array.
	 * 
	 * @return the inverse of the spine, the vertex on spine array
	 */
	public int[] getVertexOnSpineArray() {
		return vertexOnSpine;
	}

	// --- DISTRIBUTION GETTERS ---
	/**
	 * Returns the page of the given edge (by index).
	 * 
	 * @param edgeIndex
	 *            the index of the edge for which the page is requested
	 * @return the page of the given edge
	 */
	public int getPageOfEdge(int edgeIndex) {
		return distribution[edgeIndex];
	}

	/**
	 * Returns the page of the given edge.
	 * 
	 * @param edgeIndex
	 *            the edge for which the page is requested
	 * @return the page of the given edge
	 */
	public int getPageOfEdge(Edge edge) {
		return distribution[edge.getIndex()];
	}

	public int[] getDistribution() {
		return distribution;
	}

	/**
	 * Returns a new array filled with edge-indices of given page p. The array
	 * is sorted.
	 * 
	 * @param pageIndex
	 *            the page
	 * @return the array
	 */
	public int[] getAllEdgeIndicesAtPage(int pageIndex) {
		int sum = 0;
		for (int aDistribution : this.distribution) {
			if (aDistribution == pageIndex) {
				sum++;
			}
		}
		int[] result = new int[sum];
		int pos = 0;
		for (int i = 0; i < this.distribution.length; i++) {
			if (distribution[i] == pageIndex) {
				result[pos] = i;
				pos++;
			}
		}
		return result;
	}

	/**
	 * Returns a new array with edges of the given page pageIndex. The array is
	 * sorted.
	 * 
	 * @param pageIndex
	 *            the page
	 * @return the array
	 */
	public Edge[] getAllEdgesAtPage(int pageIndex) {
		Graph g = getGraph();
		Edge[] result = new Edge[countEdgesOfPage(pageIndex)];

		int pos = 0;
		for (int i = 0; i < this.distribution.length; i++) {
			if (distribution[i] == pageIndex) {
				result[pos] = g.getEdgeByIndex(i);
				pos++;
			}
		}
		return result;
	}

	public int countEdgesOfPage(int pageIndex) {
		int sum = 0;
		for (int aDistribution : this.distribution) {
			if (aDistribution == pageIndex) {
				sum++;
			}
		}
		return sum;
	}

	/**
	 * Groups the edges that are on the same page.
	 * 
	 * @return an array where each subarray at position i contains the edges the
	 *         are on page i.
	 */
	public Edge[][] getEdgesGroupedByPage() {
		Edge[] edges = this.getGraph().getEdges();
		int[] edgeDistribution = this.getDistribution();

		int[] edgesOnPage = new int[this.getK()];
		for (int page : edgeDistribution) {
			edgesOnPage[page]++;
		}

		Edge[][] result = new Edge[this.getK()][];
		int[] indices = new int[this.getK()];
		for (int i = 0; i < this.getK(); i++) {
			result[i] = new Edge[edgesOnPage[i]];
		}

		for (Edge e : edges) {
			int page = this.getPageOfEdge(e.getIndex());
			int index = indices[page]++;
			result[page][index] = e;
		}

		return result;
	}

	/**
	 * Compares the two edges by their end points' position on the spine.
	 * 
	 * @param first
	 *            the first edge
	 * @param second
	 *            the second edge
	 * @return 1 if the second edge is larger, -1 if the second one is smaller,
	 *         0 if the edges are equal.
	 */
	public int compareEdges(Edge first, Edge second) {
		int firstStart = this.getPositionOfSmallerEndpoint(first);
		int secondStart = this.getPositionOfSmallerEndpoint(second);
		int firstEnd = this.getPositionOfLargerEndpoint(first);
		int secondEnd = this.getPositionOfLargerEndpoint(second);

//		System.out.println(firstStart + "," + firstEnd + "," + secondStart+ "," + secondEnd);	
		
		if (firstStart < secondStart)
			return -1;
		if (firstStart > secondStart)
			return 1;

		if (firstEnd < secondEnd)
			return -1;
		if (firstEnd > secondEnd)
			return 1;
		
		return 0;
	}

	/**
	 * Compares the two edges by their end points' position on the spine. If
	 * they share the starting point, they are sorted in order of the embedding
	 * around the vertex.
	 * 
	 * @param first
	 *            the first edge
	 * @param second
	 *            the second edge
	 * @return 1 if the second edge is larger, -1 if the second one is smaller,
	 *         0 if the edges are equal.
	 */
	public int compareEdgesOutgoingAsEmbedded(Edge first, Edge second) {
		int firstStart = this.getPositionOfSmallerEndpoint(first);
		int secondStart = this.getPositionOfSmallerEndpoint(second);
		if (firstStart < secondStart)
			return -1;
		if (firstStart > secondStart)
			return 1;

		int firstEnd = this.getPositionOfLargerEndpoint(first);
		int secondEnd = this.getPositionOfLargerEndpoint(second);
		if (firstEnd < secondEnd)
			return 1;
		if (firstEnd > secondEnd)
			return -1;
		return 0;
	}

	public int compareEdgesSharingEndpoint(Edge first, Edge second, int endpointPosition) {
		int firstStart = this.getPositionOfSmallerEndpoint(first);
		int secondStart = this.getPositionOfSmallerEndpoint(second);
		if ((firstStart < endpointPosition) && (secondStart < endpointPosition)) {
			if (firstStart < secondStart)
				return 1;
			if (firstStart > secondStart)
				return -1;
		} else if ((firstStart < endpointPosition) || (secondStart < endpointPosition)) {
			if (firstStart < secondStart)
				return -1;
			if (firstStart > secondStart)
				return 1;
		} else {
			// both start at endpointPosition
			int firstEnd = this.getPositionOfLargerEndpoint(first);
			int secondEnd = this.getPositionOfLargerEndpoint(second);
			if (firstEnd < secondEnd)
				return 1;
			if (firstEnd > secondEnd)
				return -1;
		}
		return 0;
	}

	/**
	 * Returns the minimum of the positions of the end points of the edge on the
	 * spine.
	 * 
	 * @param edge
	 *            the edge
	 * @return the minimum of the end point positions.
	 */
	public int getPositionOfSmallerEndpoint(Edge edge) {
		return Math.min(this.getPositionOnSpine(edge.getStart().getIndex()),
				this.getPositionOnSpine(edge.getTarget().getIndex()));
	}

	/**
	 * Returns the maximum of the positions of the end points of the edge on the
	 * spine.
	 * 
	 * @param edge
	 *            the edge
	 * @return the maximum of the end point positions.
	 */
	public int getPositionOfLargerEndpoint(Edge edge) {
		return Math.max(this.getPositionOnSpine(edge.getStart().getIndex()),
				this.getPositionOnSpine(edge.getTarget().getIndex()));
	}

	/**
	 * Returns the length of an edge fur the current spine.
	 * 
	 * @param edge
	 *            the edge the length of is requested
	 * @return length of given edge
	 */
	public int getLengthOfEdge(Edge edge) {
		return (getPositionOfLargerEndpoint(edge) - getPositionOfSmallerEndpoint(edge));
	}

	/**
	 * Returns the length of an edge fur the current spine as chord, i.e. the
	 * length is at most n/2.
	 * 
	 * @param edge
	 *            the edge the length of is requested
	 * @return length of given edge as chord
	 */
	public int getLengthOfEdgeAsChord(Edge edge) {
		int lengthSpine = getLengthOfEdge(edge);
		return (lengthSpine > getN() / 2) ? getN() - lengthSpine : lengthSpine;
	}

	// --- CROSSINGS ---
	/**
	 * Returns the number of crossings in this embedding. If at the moment of
	 * request, the number of crossings is not known, it gets computed.
	 * 
	 * @return the number of crossings in this embedding
	 */
	public long getNumberOfCrossings() {
		if (crossings < 0) {
			calculateNumberOfCrossings();
		}
		return crossings;
	}

	/**
	 * Calculates and returns the number of crossings for this embedding.
	 * 
	 * @return the number of crossings in this embedding
	 */
	public long calculateNumberOfCrossings() {
		long outcome = crossingCalculator.calculateNumberOfCrossings(this);
		if (outcome < 0) {
			
			System.out.println(crossingCalculator.toString());
			
			
			System.out.println("is graph valid: " + getGraph().isValid());
			System.out.println("is embedding valid: " + this.isValid());

			System.out.println("scc crossings: "
					+ new SimpleCrossingsCalculator().calculateNumberOfCrossings(this));
			System.out.println("oec crossings: "
					+ new OpenEdgesCrossingCalculatorGS().calculateNumberOfCrossings(this));
			System.out.println("dcc crossings: "
					+ new DivideAndConquerCrossingsCalculator().calculateNumberOfCrossings(this));
			System.out.println("scc crossings: "
					+ new SimpleCrossingsCalculator().calculateNumberOfCrossings(this));
			System.out.println("oec crossings: "
					+ new OpenEdgesCrossingCalculatorGS().calculateNumberOfCrossings(this));
			System.out.println("dcc crossings: "
					+ new DivideAndConquerCrossingsCalculator().calculateNumberOfCrossings(this));
			System.out.println("scc crossings: "
					+ new SimpleCrossingsCalculator().calculateNumberOfCrossings(this));
			System.out.println("oec crossings: "
					+ new OpenEdgesCrossingCalculatorGS().calculateNumberOfCrossings(this));
			System.out.println("dcc crossings: "
					+ new DivideAndConquerCrossingsCalculator().calculateNumberOfCrossings(this));
			System.out.println("scc crossings: "
					+ new SimpleCrossingsCalculator().calculateNumberOfCrossings(this));
			System.out.println("oec crossings: "
					+ new OpenEdgesCrossingCalculatorGS().calculateNumberOfCrossings(this));
			System.out.println("dcc crossings: "
					+ new DivideAndConquerCrossingsCalculator().calculateNumberOfCrossings(this));
			
						
			throw new IllegalStateException("Calculated crossings is less than 0 for embedding: "
					+ outcome);
		}
		setNumberOfCrossings(outcome);

		return getNumberOfCrossings();
	}

	/**
	 * Calculates and returns the number of crossings for this embedding on the
	 * given page.
	 * 
	 * @param pageIndex
	 *            page for which the number of crossings is requested
	 * @return the number of crossings in this embedding on the given page
	 */
	public long calculateNumberOfCrossingsOnPage(int pageIndex) {
		return crossingCalculator.calculateNumberOfCrossingsOnPage(this, pageIndex);
	}

	/**
	 * Invalidates the number of crossings, which should be done, when spine or
	 * distribution is altered.
	 */
	public void invalidateNumberOfCrossings() {
		crossings = -1;
	}

	/**
	 * Returns whether the number of crossings for this embedding is already
	 * computed. Only works if correctly set invalid.
	 * 
	 * @return whether the number of crossings for this embedding is already
	 *         computed
	 */
	public boolean isNumberOfCrossingsValid() {
		return crossings >= 0;
	}

	/**
	 * Returns whether to edges can cross based on the positions of their end
	 * vertices not their pages.
	 * 
	 * @param edge1
	 *            first edge index
	 * @param edge2
	 *            second edge index
	 * @return whether the given edges can cross based on the positions of their
	 *         vertices
	 */
	public boolean canEdgesCross(int indexEdge1, int indexEdge2) {
		return canEdgesCross(getGraph().getEdgeByIndex(indexEdge1),
				getGraph().getEdgeByIndex(indexEdge2));
	}

	/**
	 * Returns whether to edges can cross based on the positions of their end
	 * vertices not their pages.
	 * 
	 * @param edge1
	 *            first edge
	 * @param edge2
	 *            second edge
	 * @return whether the given edges can cross based on the positions of their
	 *         vertices
	 */
	public boolean canEdgesCross(Edge edge1, Edge edge2) {
		int u = vertexOnSpine[edge1.getStart().getIndex()];
		int v = vertexOnSpine[edge1.getTarget().getIndex()];
		int x = vertexOnSpine[edge2.getStart().getIndex()];
		int y = vertexOnSpine[edge2.getTarget().getIndex()];

		try {
			return canEdgesCross(u, v, x, y);
		} catch (Exception e) {
			System.out.println("g valid" + getGraph().isValid());
			System.out.println("emb valid" + this.isValid());
		}
		throw new IllegalStateException();
	}

	/**
	 * Returns whether edge with vertices at position u,v and edge with vertices
	 * at position x,y can cross. (So positions are on the spine, not Vertex
	 * indices.) Returns false if both positions of an edge are negative.
	 * 
	 * @param u
	 *            start vertex index of edge 1
	 * @param v
	 *            target vertex index of edge 1
	 * @param x
	 *            start vertex index of edge 2
	 * @param y
	 *            target vertex index of edge 2
	 * @return whether the given edges uv and xy can cross
	 */
	public static boolean canEdgesCross(int u, int v, int x, int y) {
		if (u < v && x < y) {
			return (u < x && x < v && v < y) || (x < u && u < y && y < v);
		} else if (u > v) {
			return canEdgesCross(v, u, x, y);
		} else if (x > y) {
			return canEdgesCross(u, v, y, x);
		} else if ((u < 0) || (x < 0)) {
			return false;
		}
		throw new IllegalArgumentException("u: " + u + ", v: " + v + ", x: " + x + ", y: " + y);
	}

	/**
	 * Returns a {@link Comparator} on {@link Embedding}s based on their number
	 * of crossings.
	 * 
	 * @return comparator based on number of crossings
	 */
	public static Comparator<Embedding> getCrossingComparator() {
		return (Embedding x, Embedding y) -> Long.compare(x.getNumberOfCrossings(),
				y.getNumberOfCrossings());
	}

	// -- OPERATIONS --
	public void moveEdgeToPage(int edgeIndex, int newPage) {
		distribution[edgeIndex] = newPage;
		this.invalidateNumberOfCrossings();
	}

	public void moveVertexTo(int oldPosition, int newPosition) {
		if (newPosition < oldPosition) {
			for (int i = oldPosition; i > newPosition; i--) {
				int a = spine[i], b = spine[i - 1];
				swapVertices(a, b);
			}
		} else {
			for (int i = oldPosition; i < newPosition; i++) {
				int a = spine[i], b = spine[i + 1];
				swapVertices(a, b);
			}
		}
		invalidateNumberOfCrossings();
	}

	public void swapVertices(int vertex1Index, int vertex2Index) {
		int tmp = vertexOnSpine[vertex1Index];
		vertexOnSpine[vertex1Index] = vertexOnSpine[vertex2Index];
		vertexOnSpine[vertex2Index] = tmp;
		spine[vertexOnSpine[vertex1Index]] = vertex1Index;
		spine[vertexOnSpine[vertex2Index]] = vertex2Index;
		invalidateNumberOfCrossings();
	}

	public void swapPositions(int positionVertex1, int positionVertex2) {
		swapVertices(spine[positionVertex1], spine[positionVertex2]);
	}

	public void printEdge(Edge edge) {
		int start = this.getPositionOfSmallerEndpoint(edge);
		int end = this.getPositionOfLargerEndpoint(edge);
		System.out.println("e[" + start + "/" + end + "](" + this.getPageOfEdge(edge) + ")");
	}

}
