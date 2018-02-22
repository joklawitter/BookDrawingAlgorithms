package algorithms.optimizer.greedy;

import model.Embedding;
import util.RandomUtil;

/**
 * Greedy algorithms to optimize the edge distribution. Provides methods to find
 * best page for one edge, all edge in an given or random order or to
 * exhaustively search for the best edge distribution greedily.
 * 
 * @author Jonathan Klawitter
 */
public class GreedyEdgeDistributionAlgorithms {

	/**
	 * Optimizes the edge distribution of the given embedding by moving edges to
	 * the best page terms of crossings. This is repeated till no improvement
	 * was possible.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @return the gain of the optimization
	 */
	public static long exhaustiveGreedyEdgeDistributionOptimisation(Embedding embedding) {
		int[] randomOrder = RandomUtil.randomPermutation(embedding.getM());
		return exhaustiveGreedyEdgeDistributionOptimisationWithOrder(embedding, randomOrder);
	}

	/**
	 * Optimizes the edge distribution of the given embedding by moving edges to
	 * the best page terms of crossings. This is repeated till no improvement
	 * was possible.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @param order
	 *            the order in which the edge are handled by edge index
	 * @return the gain of the optimization
	 */
	public static long exhaustiveGreedyEdgeDistributionOptimisationWithOrder(Embedding embedding,
			int[] order) {
		long roundGain = 0;
		long overallGain = 0;

		// 1. iterate as long as gain is made
		boolean gainedSomething = true;
		do {
			gainedSomething = false;

			// 2. iterate over positions/vertices:
			// if found better for one > move it > repeat for all
			roundGain = findBestPageForEdgesWithOrder(embedding, order);

			if (roundGain > 0) {
				gainedSomething = true;
				overallGain += roundGain;
			}

		} while (gainedSomething);

		// (3.) number of crossings is updated in called methods

		return overallGain;
	}

	/**
	 * Finds for all edges in random order the best page, i.e. for each edge the
	 * best page is searched once, moves them and returns the total gain. Runs
	 * in O(m^2 + m*k).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @return total gain
	 */
	public static long findBestPageForEdges(Embedding embedding) {
		int[] randomOrder = RandomUtil.randomPermutation(embedding.getM());
		return findBestPageForEdgesWithOrder(embedding, randomOrder);
	}

	/**
	 * Finds for all edges in given order the best page, i.e. for each edge the
	 * best page is searched once, moves them and returns the total gain. Runs
	 * in O(m^2 + m*k).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @param order
	 *            the order in which the edge are handled by edge index
	 * @return total gain
	 */
	public static long findBestPageForEdgesWithOrder(Embedding embedding, int[] order) {
		long gain = 0;

		for (int i = 0; i < embedding.getM(); i++) {
			int edgeIndex = order[i];

			// 3. find best page for this edge
			// 4. update gain
			gain += findBestPageForEdge(embedding, edgeIndex);
		}

		// number of crossings is updated in called methods

		return gain;
	}

	/**
	 * Finds the best page for the given edge (by index) and moves it there in
	 * O(m + k).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @param edgeIndex
	 *            the edge to be moved to best page
	 * @return the gain of moving the edge or 0, if no move was done
	 */
	public static long findBestPageForEdge(Embedding embedding, int edgeIndex) {
		long startX = embedding.getNumberOfCrossings();

		// count crossings per page
		int[] crossingsPerPage = new int[embedding.getK()];
		for (int secondEdgeIndex = 0; secondEdgeIndex < embedding.getM(); secondEdgeIndex++) {
			if (secondEdgeIndex == edgeIndex) {
				continue;
			}

			if (embedding.canEdgesCross(secondEdgeIndex, edgeIndex)) {
				crossingsPerPage[embedding.getPageOfEdge(secondEdgeIndex)]++;
			}
		}

		// store old number of crossings
		int oldNumCrossings = crossingsPerPage[embedding.getPageOfEdge(edgeIndex)];

		// find best page
		int bestPage = embedding.getPageOfEdge(edgeIndex);
		int bestNumCrossings = oldNumCrossings;
		for (int i = 0; i < crossingsPerPage.length; i++) {
			if (crossingsPerPage[i] < bestNumCrossings) {
				bestNumCrossings = crossingsPerPage[i];
				bestPage = i;
			}
		}

		// move edge to best page and return gain
		embedding.moveEdgeToPage(edgeIndex, bestPage);

		long gain = oldNumCrossings - bestNumCrossings;
		embedding.setNumberOfCrossings(startX - gain);

		return gain;
	}

}
