package algorithms.heuristics.combinations;

import java.util.ArrayList;
import java.util.Arrays;

import model.Edge;
import model.Embedding;
import algorithms.base.FullEmbeddingAlgorithm;
import algorithms.base.VertexOrderAlgorithm;
import algorithms.heuristics.VertexOrderHeuristics;

/**
 * This class implements methods used by several {@link FullEmbeddingAlgorithm}
 * arising from combining greedy edge distribution with
 * {@link VertexOrderHeuristics}.
 *
 * @author Jonathan Klawitter
 * @see VertexOrderAlgorithm
 * @see VertexOrderHeuristics
 */
public class CombinedHeuristicsHelper {

	/**
	 * @param distribution
	 *            distribution array to initialize
	 */
	public static void initDistribution(int[] distribution) {
		Arrays.fill(distribution, -1);
	}

	public static void placeEdgeOnBestPage(Embedding embedding, Edge edgeToPlace,
			ArrayList<Edge> alreadyPlacedEdges, int k) {
		int[] crossingsPerPage = new int[k];
		
		for (Edge edge : alreadyPlacedEdges) {
			if (embedding.canEdgesCross(edgeToPlace, edge) ) {
				crossingsPerPage[embedding.getPageOfEdge(edge)]++;
			}
		}
		
		int minimum = Integer.MAX_VALUE;
		int bestPage = -1;
		for (int i = 0; i < crossingsPerPage.length; i++) {
			if (crossingsPerPage[i] < minimum) {
				minimum = crossingsPerPage[i];
				bestPage = i;
			}
		}
		
		embedding.moveEdgeToPage(edgeToPlace.getIndex(), bestPage);
	}
}
