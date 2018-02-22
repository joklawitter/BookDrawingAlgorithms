package algorithms.heuristics.vertexorder;

import java.util.ArrayList;
import java.util.List;

import model.Edge;
import model.Embedding;
import model.Vertex;
import algorithms.heuristics.VertexOrderHeuristics;

/**
 * This class implements a {@link VertexOrderHeuristics} based on the
 * strategy used in the {@link ConnectivityAlgorithm} to pick vertices to place
 * next. It than places them such that the new position minimizes the crossings
 * of the edges of the new vertex with already fully placed edges.
 *
 * @author Jonathan Klawitter
 * @see ConnectivityAlgorithm
 * @see VertexOrderHeuristics
 */
public class GreedyConnectivityAlgorithm extends ConnectivityAlgorithm {

	public GreedyConnectivityAlgorithm() {
		super();
		// super is set with connectivity selector
	}

	@Override
	public void computeVertexOrder(Embedding embedding) {
		// initialize data structures
		PlacementState state = new PlacementState(embedding.getGraph());
		int n = embedding.getN();
		Vertex[] vertices = embedding.getGraph().getVertices();

		List<Edge> placedEdges = new ArrayList<Edge>();

		// place start vertex
		int startVertex = initialSelector.selectVertex(state);
		state.placeVertex(startVertex, VertexPlacement.END);
		List<Vertex> spine = new ArrayList<Vertex>(n);
		spine.add(vertices[startVertex]);

		// insert remaining vertices
		// i.e. 1) select vertex, 2) place at best position, 3) maintain state
		for (int i = 1; i < n; i++) {
			// 1) vertex is chosen by selection algorithm of super class
			int vertexIndex = selector.selectVertex(state);
			Vertex vertex = vertices[vertexIndex];

			// 2) find best position for this vertex
			// this means where it produces fewest crossings with already placed
			// edges
			int position = getBestPosition(spine, state, vertex, placedEdges);
			spine.add(position, vertex);

			// 3) maintain state
			// we store the edges of placed neighbors
			// these are needed to compute if next vertex introduces new
			// crossings with them
			addEdgesToPlacedNeighbours(state, vertex, placedEdges);
			// only to maintain state of selector!
			state.placeVertex(vertexIndex, VertexPlacement.BEGINNING);
		}

		// convert to array
		int[] spineArray = embedding.getSpine();
		for (int i = 0; i < spineArray.length; i++) {
			spineArray[i] = spine.get(i).getIndex();
		}

		embedding.setSpine(spineArray);
	}

	/**
	 * Finds the best position for the given vertex in the given spine in term
	 * of crossings its edges produce with already placed edges.
	 * 
	 * @param spine
	 *            the current spine
	 * @param state
	 *            needs if vertices are already placed
	 * @param vertex
	 *            {@link Vertex} to place
	 * @param placedEdges
	 *            the already placed {@link Edge}s
	 * @return the best position in terms of produced crossings
	 */
	private int getBestPosition(List<Vertex> spine, PlacementState state, Vertex vertex,
			List<Edge> placedEdges) {
		// we compute for each already placed edge for which position it would
		// produce crossings with the edges to place

		int[] crossingsAtPosition = new int[spine.size() + 1];

		List<Edge> edges = new ArrayList<Edge>();

		// pack edges of the new vertex to already placed vertices in the list
		addEdgesToPlacedNeighbours(state, vertex, edges);

		// compare all edges that get placed by adding vertex
		// with all edges that are already placed
		for (Edge xy : placedEdges) {
			int posX = spine.indexOf(xy.getStart());
			int posY = spine.indexOf(xy.getTarget());

			if (posX > posY) {
				int tmp = posX;
				posX = posY;
				posY = tmp;
			}

			for (Edge uv : edges) {
				int posU = spine.indexOf(uv.getOtherEnd(vertex));
				if (posU == posX || posU == posY) {
					continue;
				}
				// if u is outside of placed edge, inside is bad
				if (posU < posX || posU > posY) {
					for (int i = posX + 1; i <= posY; i++) {
						crossingsAtPosition[i]++;
					}
				} else {
					for (int i = 0; i <= posX; i++) {
						crossingsAtPosition[i]++;
					}
					for (int i = posY + 1; i < crossingsAtPosition.length; i++) {
						crossingsAtPosition[i]++;
					}
				}
			}
		}
		int min = Integer.MAX_VALUE;
		int minPosition = 0;
		for (int i = 0; i < crossingsAtPosition.length; i++) {
			if (crossingsAtPosition[i] <= min) {
				min = crossingsAtPosition[i];
				minPosition = i;
			}
		}

		return minPosition;
	}

	/**
	 * Adds the edges of the given vertex which connect it to already placed
	 * vertices (according to state) to the given list.
	 * 
	 * @param state
	 *            {@link PlacementState} describing which vertices are placed
	 *            yet
	 * @param vertex
	 *            {@link Vertex} which edges get added
	 * @param edges
	 *            list to add edges to
	 */
	public static void addEdgesToPlacedNeighbours(PlacementState state, Vertex vertex, List<Edge> edges) {
		for (Edge e : vertex.getEdges()) {
			if (state.isPlaced(e.getOtherEnd(vertex).getIndex())) {
				edges.add(e);
			}
		}
	}

}
