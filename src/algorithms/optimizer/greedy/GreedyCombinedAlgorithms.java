package algorithms.optimizer.greedy;

import model.Edge;
import model.Embedding;
import model.Vertex;
import util.RandomUtil;

/**
 * Greedy algorithms to optimize an embedding. Provides methods to find best
 * position for one vertex, all vertices in an given or random order or to
 * exhaustively search for the best spine greedily while always simultaneously
 * replacing incident edges of vertices under consideration.<br>
 * Thus it is a combination of {@link GreedyVertexOrderAlgorithms} and
 * {@link GreedyEdgeDistributionAlgorithms}.
 * 
 * @author Jonathan Klawitter
 * @see GreedyVertexOrderAlgorithms
 * @see GreedyEdgeDistributionAlgorithms
 */
public class GreedyCombinedAlgorithms {

	/**
	 * Optimizes the embedding by moving vertices to best positions while
	 * simultaneously replacing their edges in. This is repeated till no
	 * improvement was possible.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @return the gain of the optimization
	 */
	public static long exhaustiveGreedyOptimisation(Embedding emb) {
		long startX = emb.getNumberOfCrossings();

		long roundGain = 0;
		long overallGain = 0;

		// 1. iterate as long as gain is made
		boolean gainedSomething = true;
		do {
			gainedSomething = false;

			// 2. iterate over positions/vertices:
			// if found better for one > move it > repeat for all
			roundGain = optimiseAllVertices(emb);

			if (roundGain > 0) {
				gainedSomething = true;
				overallGain += roundGain;
			}

		} while (gainedSomething); // ends gaining while loop

		emb.setNumberOfCrossings(startX - overallGain);

		return overallGain;
	}

	/**
	 * Finds for all vertices in random order the best position on the spine
	 * while simultaneously replacing their edges and moves it there, i.e. for
	 * each vertex the best position is searched once and its incident edges
	 * replaced, and returns the total gain. Runs in O(n^2 m delta).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @return total gain
	 */
	public static long optimiseAllVertices(Embedding embedding) {
		int[] randomOrder = RandomUtil.randomPermutation(embedding.getN());
		return optimiseAllVerticesWithOrder(embedding, randomOrder);
	}

	/**
	 * Finds for all vertices in random order the best position on the spine
	 * while simultaneously replacing their edges and moves it there, i.e. for
	 * each vertex the best position is searched once and its incident edges
	 * replaced, and returns the total gain. Runs in O(n^2 m delta).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @param order
	 *            the order in which the vertices are handled by vertex index
	 *            (not spine position)
	 * @return total gain
	 */
	public static long optimiseAllVerticesWithOrder(Embedding embedding, int[] order) {
		long gain = 0;

		for (int i = 0; i < embedding.getN(); i++) {
			int startPos = embedding.getPositionOnSpine(order[i]);

			// 3. find best position for this vertex at position startPos
			// 4. update gain
			gain += optimisePositionOfVertex(embedding, startPos);
		}

		return gain;
	}

	/**
	 * Finds the best position for the vertex and its incident edges at given
	 * start position on the spine, moves it there and replaces the edges in
	 * O(n(m + k + delta^2)) = O(n m delta).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @param startPositionOnSpine
	 *            position on the spine of the vertex which is considered
	 * @return the gain of optimizing this vertex and its incident edges
	 */
	public static long optimisePositionOfVertex(Embedding embedding, int startPositionOnSpine) {
		int n = embedding.getN();
		Vertex currentVertex = embedding.getGraph().getVertexByIndex(
				embedding.getVertexAtPosition(startPositionOnSpine));
		int bestPos = startPositionOnSpine;
		long currentGain;
		long gain;
		
		// A. optimize incident edges at this position
		long initialGain = findBestPagesForIncidentEdges(embedding, currentVertex);
		gain = initialGain;
		currentGain = initialGain;

		// B.1 from its position on spine move it to right and find best
		for (int q = startPositionOnSpine + 1; q < n; q++) {
			currentGain += GreedyVertexOrderAlgorithms.computeGainForSwapOfNeighbouringVerticesAt(
					q - 1, q, embedding);
			embedding.swapPositions(q - 1, q);
			// also optimize edges at the new position
			currentGain += findBestPagesForIncidentEdges(embedding, currentVertex);

			if (currentGain > gain) {
				gain = currentGain;
				bestPos = q;
			}
		}
		// swap back
		for (int q = n - 1; q > startPositionOnSpine; q--) {
			embedding.swapPositions(q - 1, q);
		}
		// also optimize edges again for correct gain counting
		findBestPagesForIncidentEdges(embedding, currentVertex);
		currentGain = initialGain; // back to start position, gain is 0

		// B.2 from its position on spine move it to left and find best
		for (int q = startPositionOnSpine - 1; q >= 0; q--) {
			currentGain += GreedyVertexOrderAlgorithms.computeGainForSwapOfNeighbouringVerticesAt(
					q, q + 1, embedding);
			embedding.swapPositions(q, q + 1);
			// also optimize edges at the new position
			currentGain += findBestPagesForIncidentEdges(embedding, currentVertex);

			if (currentGain > gain) {
				gain = currentGain;
				bestPos = q;
			}
		}
		// swap back
		// (could be optimized with loops below, but wouldn't be as clear)
		for (int q = 0; q < startPositionOnSpine; q++) {
			embedding.swapPositions(q, q + 1);
		}

		// C. swap to best found
		if (bestPos > startPositionOnSpine) {
			for (int q = startPositionOnSpine + 1; q <= bestPos; q++) {
				embedding.swapPositions(q - 1, q);
			}
		} else if (bestPos < startPositionOnSpine) {
			for (int q = startPositionOnSpine - 1; q >= bestPos; q--) {
				embedding.swapPositions(q, q + 1);
			}
		}
		findBestPagesForIncidentEdges(embedding, currentVertex);

		return gain;
	}

	// O(delta(m + k))
	private static long findBestPagesForIncidentEdges(Embedding embedding, Vertex vertex) {
		long gain = 0;
		for (Edge edge : vertex.getEdges()) {
			gain += GreedyEdgeDistributionAlgorithms
					.findBestPageForEdge(embedding, edge.getIndex());
		}

		return gain;
	}
}
