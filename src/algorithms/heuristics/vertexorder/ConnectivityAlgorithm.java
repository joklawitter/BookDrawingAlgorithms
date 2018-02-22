package algorithms.heuristics.vertexorder;

import java.util.BitSet;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.VertexOrderAlgorithm;

/**
 * A heuristic for the vertex placement that uses the number of placed and
 * unplaced vertices.
 * 
 * <p>
 * <b>Usage example:</b><br>
 * Connectivity with crossing minimization:<br>
 * <br>
 * {@code VertexOrderAlgorithm algo = new ConnectivityAlgorithm(VertexSelector.CONNECTIVITY, VertexSelector.CROSSINGS); }
 * <br>
 * {@code algo.computeVertexOrder(embedding); }
 * </p>
 * 
 * @author Matthias Wolf
 * @see Michael Baur and Ulrik Brandes.
 *      "Crossing Reduction in Circular Layouts", Proceedings of WG, 2004.
 *
 */
public class ConnectivityAlgorithm implements VertexOrderAlgorithm {

	protected final VertexSelector initialSelector;

	protected final VertexSelector selector;

	protected final VertexPlacer placer;

	/**
	 * The constructed connectivity algorithm uses
	 * {@linkplain VertexSelector#CONNECTIVITY} and
	 * {@linkplain VertexPlacer#CROSSINGS} as vertex selection and placement
	 * strategies.
	 * 
	 * <p>
	 * This constructor is intended to be used by the GUI. If you want to create
	 * a {@code ConnectivityAlgorithm} with other stratgies, use
	 * {@link #ConnectivityAlgorithm(VertexSelector, VertexPlacer)} or
	 * {@link #ConnectivityAlgorithm(VertexSelector, VertexSelector, VertexPlacer)}
	 * instead.
	 * </p>
	 */
	public ConnectivityAlgorithm() {
		this(VertexSelector.CONNECTIVITY, VertexPlacer.CROSSINGS);
	}

	/**
	 * Constructs a new {@code ConnectivityAlgorithm}. The first vertex is
	 * selected randomly.
	 * 
	 * @param selector
	 *            the strategy for vertex selection
	 * @param placer
	 *            the strategy for vertex placement
	 */
	public ConnectivityAlgorithm(VertexSelector selector, VertexPlacer placer) {
		this(selector, selector, placer);
	}

	/**
	 * Constructs a new {@code ConnectivityAlgorithm} with the given vertex
	 * placement and selection strategies.
	 * 
	 * @param initialSelector
	 *            the selection strategy for the first vertex
	 * @param selector
	 *            the selection strategy for all remaining vertices
	 * @param placer
	 *            the vertex placement strategy
	 */
	public ConnectivityAlgorithm(VertexSelector initialSelector, VertexSelector selector,
			VertexPlacer placer) {
		if (selector == null || placer == null)
			throw new IllegalArgumentException("argument is null");
		this.initialSelector = initialSelector;
		this.selector = selector;
		this.placer = placer;
	}

	/**
	 * Computes a vertex ordering. The embedding given as a parameter is
	 * ignored.
	 */
	@Override
	public void computeVertexOrder(Embedding embedding) {

		// initialize data structures
		PlacementState state = new PlacementState(embedding.getGraph());
		int n = embedding.getN();

		// place start vertex
		int startVertex = initialSelector.selectVertex(state);
		state.placeVertex(startVertex, VertexPlacement.END);

		// insert remaining vertices
		for (int i = 1; i < n; i++) {
			int vertex = selector.selectVertex(state);
			VertexPlacement placement = placer.determinePlacement(vertex, state);
			state.placeVertex(vertex, placement);
		}

		int[] spineArray = embedding.getSpine();
		int positionOnSpine = 0;
		for (int vertex : state.getSpine()) {
			spineArray[positionOnSpine] = vertex;
			positionOnSpine++;
		}

		embedding.setSpine(spineArray);
	}

	public static class PlacementState {
		Deque<Integer> spine = new LinkedList<>();
		int[] unplacedNeighbors;
		int[] placedNeighbors;
		BitSet placedVertices;
		Graph graph;

		public PlacementState(Graph graph) {
			this.graph = graph;
			int numberOfVertices = graph.getN();
			unplacedNeighbors = new int[numberOfVertices];
			placedNeighbors = new int[numberOfVertices];
			placedVertices = new BitSet(numberOfVertices);

			for (int i = 0; i < numberOfVertices; i++) {
				unplacedNeighbors[i] = graph.getVertexByIndex(i).getDegree();
			}
		}

		public Deque<Integer> getSpine() {
			return spine;
		}

		public Graph getGraph() {
			return graph;
		}

		public BitSet getPlacedVertices() {
			return placedVertices;
		}

		public void placeVertex(int vertexIndex, VertexPlacement placement) {
			placement.insert(vertexIndex, spine);
			Vertex vertex = graph.getVertexByIndex(vertexIndex);
			for (Edge edge : vertex.getEdges()) {
				int otherVertex = edge.getTarget().getIndex() == vertexIndex ? edge.getStart()
						.getIndex() : edge.getTarget().getIndex();
				placedNeighbors[otherVertex]++;
				unplacedNeighbors[otherVertex]--;
			}
			placedVertices.set(vertexIndex);
		}

		public int getNumberOfPlacedNeighbors(int vertex) {
			return placedNeighbors[vertex];
		}

		public int getNumberOfUnplacedNeighbors(int vertex) {
			return unplacedNeighbors[vertex];
		}

		public boolean isPlaced(int vertex) {
			return placedVertices.get(vertex);
		}
	}

	/**
	 * This enum contains different positions where a vertex can be placed.
	 * 
	 * @author Matthias Wolf
	 *
	 */
	public static enum VertexPlacement {

		/**
		 * The vertex will be placed at the beginning of the spine, i.e. its
		 * index on the spine is less than the one of all currently placed
		 * vertices.
		 */
		BEGINNING {
			@Override
			public void insert(int vertex, Deque<Integer> spine) {
				spine.addFirst(vertex);
			}
		},

		/**
		 * The vertex will be placed at the end of the spine, i.e. its index on
		 * the spine is greater than the one of all currently placed vertices.
		 */
		END {
			@Override
			public void insert(int vertex, Deque<Integer> spine) {
				spine.addLast(vertex);
			}
		};

		/**
		 * Inserts the vertex according to the vertex placement.
		 * 
		 * @param vertex
		 *            the vertex to insert
		 * @param spine
		 *            the spine
		 */
		public abstract void insert(int vertex, Deque<Integer> spine);
	}

	/**
	 * This enum contains the different strategies for selecting the next
	 * vertex.
	 * 
	 * @author Matthias Wolf
	 *
	 */
	public static enum VertexSelector {
		/**
		 * Selects the unplaced vertex with the smallest index.
		 */
		NEXT {
			@Override
			public int selectVertex(PlacementState state) {
				return state.getPlacedVertices().nextClearBit(0);
			}
		},
		/**
		 * Selects a random vertex.
		 */
		RANDOM {
			@Override
			public int selectVertex(PlacementState state) {
				if (state.getPlacedVertices().cardinality() == 0) {
					return ThreadLocalRandom.current().nextInt(state.getGraph().getN());
				} else {
					return selectRandomly(state);
				}
			}
		},
		/**
		 * Selects the vertex with the most placed neighbors.
		 */
		INCON {
			@Override
			public int selectVertex(PlacementState state) {
				return inwardConnectivity(state);
			}
		},
		/**
		 * Selects the vertex with the fewest unplaced neighbors.
		 */
		OUTCON {
			@Override
			public int selectVertex(PlacementState state) {
				return outwardConnectivity(state);
			}
		},
		/**
		 * Selects the vertex with the most placed neighbors. Ties are broken by
		 * the selecting the vertex with fewer unplaced neighbors.
		 */
		CONNECTIVITY {
			@Override
			public int selectVertex(PlacementState state) {
				return connectivity(state);
			}
		};

		public abstract int selectVertex(PlacementState state);
	}

	public static enum VertexPlacer {
		/**
		 * Always places the new vertex at the end.
		 */
		FIXED {
			@Override
			public VertexPlacement determinePlacement(int vertex, PlacementState state) {
				return VertexPlacement.END;
			}
		},
		/**
		 * Places the new vertex at the beginning or the end with equal
		 * probability.
		 */
		RANDOM {
			@Override
			public VertexPlacement determinePlacement(int vertex, PlacementState state) {
				return ThreadLocalRandom.current().nextDouble() < 0.5 ? VertexPlacement.END
						: VertexPlacement.BEGINNING;
			}
		},
		/**
		 * Places the new vertex such that the number of new crossings is
		 * minimized.
		 */
		CROSSINGS {
			@Override
			public VertexPlacement determinePlacement(int vertex, PlacementState state) {
				return minimizeCrossings(vertex, state);
			}
		},
		/**
		 * Places the vertex such that the total length of the new closed edges
		 * is minimized.
		 */
		ELEN {
			@Override
			public VertexPlacement determinePlacement(int vertex, PlacementState state) {
				return minimizeEdgeLength(vertex, state);
			}
		};

		/**
		 * Determines where the new vertex shall be placed.
		 * 
		 * @param vertex
		 *            the new vertex
		 * @param state
		 *            the current state
		 * @return where the new vertex shall be placed.
		 */
		public abstract VertexPlacement determinePlacement(int vertex, PlacementState state);

	}

	// VERTEX SELECTORS

	private static int selectRandomly(PlacementState state) {
		BitSet placedVertices = state.getPlacedVertices();
		int numberOfUnplacedVertices = state.getGraph().getN() - placedVertices.cardinality();
		int vertex = placedVertices.nextClearBit(0);
		for (int skip = ThreadLocalRandom.current().nextInt(numberOfUnplacedVertices); skip > 0; skip--) {
			vertex = placedVertices.nextClearBit(vertex + 1);
		}

		return vertex;
	}

	private static int inwardConnectivity(PlacementState state) {
		int bestVertex = -1;
		int mostPlacedNeighbors = -1;
		for (int v = 0; v < state.getGraph().getN(); v++) {
			if (!state.isPlaced(v) && state.getNumberOfPlacedNeighbors(v) > mostPlacedNeighbors) {
				mostPlacedNeighbors = state.getNumberOfPlacedNeighbors(v);
				bestVertex = v;
			}
		}

		return bestVertex;
	}

	private static int outwardConnectivity(PlacementState state) {
		int bestVertex = -1;
		int fewestUnplacedNeighbors = Integer.MAX_VALUE;
		for (int v = 0; v < state.getGraph().getN(); v++) {
			if (!state.isPlaced(v)
					&& state.getNumberOfUnplacedNeighbors(v) < fewestUnplacedNeighbors) {
				fewestUnplacedNeighbors = state.getNumberOfUnplacedNeighbors(v);
				bestVertex = v;
			}
		}

		return bestVertex;
	}

	private static int connectivity(PlacementState state) {
		int bestVertex = -1;
		int mostPlacedNeighbors = -1;
		int unplacedNeighbors = -1;
		for (int v = 0; v < state.getGraph().getN(); v++) {
			if (!state.isPlaced(v)) {
				int unplaced = state.getNumberOfUnplacedNeighbors(v);
				int placed = state.getNumberOfPlacedNeighbors(v);
				if (placed > mostPlacedNeighbors
						|| (placed == mostPlacedNeighbors && unplacedNeighbors > unplaced)) {
					mostPlacedNeighbors = placed;
					unplacedNeighbors = unplaced;
					bestVertex = v;
				}
			}
		}

		return bestVertex;
	}

	// VERTEX PLACER

	private static VertexPlacement minimizeCrossings(int vertex, PlacementState state) {

		long beginningCrossings = 0;
		long endCrossings = 0;
		Deque<Integer> spine = state.getSpine();
		Graph graph = state.getGraph();

		BitSet neighbors = new BitSet(graph.getN());
		Vertex ver = graph.getVertexByIndex(vertex);
		for (Edge e : ver.getEdges()) {
			neighbors.set(e.getOtherEnd(ver).getIndex());
		}

		// calculate outgoing edges till all neighbors are visited
		// - starting at the beginning
		int unvisitedPlacedNeighbors = state.getNumberOfPlacedNeighbors(vertex);
		for (Iterator<Integer> it = spine.iterator(); (it.hasNext() && (unvisitedPlacedNeighbors > 0));) {
			int v = it.next();
			int crossingEdges = state.getNumberOfUnplacedNeighbors(v);
			if (neighbors.get(v)) {
				unvisitedPlacedNeighbors--;
				crossingEdges--;
			}
			beginningCrossings += unvisitedPlacedNeighbors * crossingEdges;
		}

		// - starting at the end
		unvisitedPlacedNeighbors = state.getNumberOfPlacedNeighbors(vertex);
		for (Iterator<Integer> it = spine.descendingIterator(); it.hasNext()
				&& unvisitedPlacedNeighbors > 0;) {
			int v = it.next();
			int crossingEdges = state.getNumberOfUnplacedNeighbors(v);
			if (neighbors.get(v)) {
				unvisitedPlacedNeighbors--;
				crossingEdges--;
			}
			endCrossings += unvisitedPlacedNeighbors * crossingEdges;
		}

		return beginningCrossings < endCrossings ? VertexPlacement.BEGINNING : VertexPlacement.END;
	}

	private static VertexPlacement minimizeEdgeLength(int vertex, PlacementState state) {
		Deque<Integer> spine = state.getSpine();
		Graph graph = state.getGraph();
		BitSet neighbors = new BitSet(graph.getN());
		Vertex ver = graph.getVertexByIndex(vertex);
		for (Edge e : ver.getEdges()) {
			neighbors.set(e.getOtherEnd(ver).getIndex());
		}

		int beginningLength = 0;
		int unvisitedPlacedNeighbors = state.getNumberOfUnplacedNeighbors(vertex);
		for (Iterator<Integer> it = spine.iterator(); it.hasNext() && unvisitedPlacedNeighbors > 0;) {
			int v = it.next();
			beginningLength += unvisitedPlacedNeighbors;
			if (neighbors.get(v)) {
				unvisitedPlacedNeighbors--;
			}
		}

		int endLength = state.getNumberOfPlacedNeighbors(vertex) * (spine.size() + 1)
				- beginningLength;
		return beginningLength < endLength ? VertexPlacement.BEGINNING : VertexPlacement.END;
	}
}
