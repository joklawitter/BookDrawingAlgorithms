package algorithms.heuristics.combinations;

import java.util.ArrayList;
import java.util.List;

import model.Edge;
import model.Embedding;
import model.Vertex;
import algorithms.base.FullEmbeddingAlgorithm;
import algorithms.base.VertexOrderAlgorithm;
import algorithms.heuristics.FullEmbeddingHeuristics;
import algorithms.heuristics.vertexorder.ConnectivityAlgorithm;
import algorithms.heuristics.vertexorder.GreedyConnectivityAlgorithm;

/**
 * This class implements a {@link FullEmbeddingHeuristics} based on the strategy
 * used in the {@link GreedyConnectivityAlgorithm} to pick and place vertices.
 * Simultaneously it also distributes the edges.
 *
 * @author Jonathan Klawitter
 * @see ConnectivityAlgorithm
 * @see FullEmbeddingHeuristics
 */
public class FullGreedyConnectivityAlgorithm extends ConnectivityAlgorithm implements
		FullEmbeddingAlgorithm, VertexOrderAlgorithm {

	public FullGreedyConnectivityAlgorithm() {
		super();
		// super is set with connectivity selector
	}

	@Override
	public void computeVertexOrder(Embedding embedding) {
		this.computeEmbedding(embedding);
	}

	@Override
	public void computeEmbedding(Embedding embedding) {
		if (embedding.getK() == 1) {
			throw new IllegalArgumentException("Algorithm is only for k > 1.");
		}

		// initialize data structures
		PlacementState state = new PlacementState(embedding.getGraph());
		int n = embedding.getN();
		Vertex[] vertices = embedding.getGraph().getVertices();

		CombinedHeuristicsHelper.initDistribution(embedding.getDistribution());
		List<Edge> placedEdges = new ArrayList<Edge>(embedding.getM());

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

			// 2) find best position for this vertex and place its edges
			// this means where it produces fewest crossings with already placed
			// edges
			int position = getBestPosition(embedding, spine, state, vertex, placedEdges);
			spine.add(position, vertex);

			// 3) maintain state
			// we store the edges of placed neighbors
			// these are needed to compute if next vertex introduces new
			// crossings with them
			GreedyConnectivityAlgorithm.addEdgesToPlacedNeighbours(state, vertex, placedEdges);
			// only to maintain state of selector!
			state.placeVertex(vertexIndex, VertexPlacement.BEGINNING);
		}

		// convert to array
		int[] spineArray = embedding.getSpine();
		for (int i = 0; i < spineArray.length; i++) {
			spineArray[i] = spine.get(i).getIndex();
		}

		if (placedEdges.size() != embedding.getM()) {
			System.out.println(embedding.toString());
			throw new IllegalStateException("Not all edges distributed.");
		}

		embedding.setSpine(spineArray);
	}

	/**
	 * Finds the best position for the given vertex in the given spine in term
	 * of crossings its edges produce with already placed edges. Then it
	 * distributes the edges accordingly.
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
	private int getBestPosition(Embedding embedding, List<Vertex> spine, PlacementState state,
			Vertex vertex, List<Edge> placedEdges) {
		// we compute for each already placed edge for which position it would
		// produce crossings with the edges to place

		int[] crossingsAtPosition = new int[spine.size() + 1];

		List<Edge> edges = new ArrayList<Edge>();
		// pack edges of the new vertex to already placed vertices in this list
		GreedyConnectivityAlgorithm.addEdgesToPlacedNeighbours(state, vertex, edges);

		// compare all edges that get placed by adding vertex
		// with all edges that are already placed
		for (Edge uv : edges) {
			int[][] crossingsOfEdgeAtPosition = new int[embedding.getK()][spine.size() + 1];
			for (Edge xy : placedEdges) {
				int posX = spine.indexOf(xy.getStart());
				int posY = spine.indexOf(xy.getTarget());

				if (posX > posY) {
					int tmp = posX;
					posX = posY;
					posY = tmp;
				}

				int pageIndex = embedding.getPageOfEdge(xy);

				int posU = spine.indexOf(uv.getOtherEnd(vertex));
				if (posU == posX || posU == posY) {
					continue;
				}
				// if u is outside of placed edge, inside is bad
				// store this on the bad page
				if (posU < posX || posU > posY) {
					for (int i = posX + 1; i <= posY; i++) {
						crossingsOfEdgeAtPosition[pageIndex][i]++;
					}
				} else {
					for (int i = 0; i <= posX; i++) {
						crossingsOfEdgeAtPosition[pageIndex][i]++;
					}
					for (int i = posY + 1; i < crossingsOfEdgeAtPosition[0].length; i++) {
						crossingsOfEdgeAtPosition[pageIndex][i]++;
					}
				}
			}
			
			// per position store how many crossings it would cause
			for (int i = 0; i < crossingsAtPosition.length; i++) {
				int min = Integer.MAX_VALUE;
				for (int j = 0; j < crossingsOfEdgeAtPosition.length; j++) {
					if (crossingsOfEdgeAtPosition[j][i] < min) {
						min = crossingsOfEdgeAtPosition[j][i];
					}
				}
				crossingsAtPosition[i] += min;				
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


		// place edges
		for (Edge uv : edges) {
			int posU = spine.indexOf(uv.getOtherEnd(vertex));
			int posV = minPosition;
			if (posU >= posV) {
				// is or will be behind
				posU++;
			}

			int[] crossingsPerPage = new int[embedding.getK()];
			for (Edge xy : placedEdges) {
				int posX = spine.indexOf(xy.getStart());
				int posY = spine.indexOf(xy.getTarget());
				if (posX >= posV) {
					posX++;
				}
				if (posY >= posV) {
					posY++;
				}

				if (Embedding.canEdgesCross(posU, posV, posX, posY)) {
					crossingsPerPage[embedding.getPageOfEdge(xy)]++;
				}
			}

			min = Integer.MAX_VALUE;
			int minPage = 0;
			for (int i = 0; i < crossingsPerPage.length; i++) {
				if (crossingsPerPage[i] <= min) {
					min = crossingsPerPage[i];
					minPage = i;
				}
			}
			embedding.moveEdgeToPage(uv.getIndex(), minPage);
		}

		return minPosition;
	}

}
