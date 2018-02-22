package algorithms.optimizer.greedy;

import java.util.List;

import util.RandomUtil;
import model.Edge;
import model.Embedding;

/**
 * Greedy algorithms to optimize the vertex order. Provides methods to find best
 * position for one vertex, all vertices in an given or random order or to
 * exhaustively search for the best spine greedily.
 * 
 * @author Jonathan Klawitter
 */
public class GreedyVertexOrderAlgorithms {

	/**
	 * Optimizes the embedding by moving vertices to best positions in terms of
	 * crossings. This is repeated till no improvement was possible.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @return the gain of the optimization
	 */
	public static long exhaustiveGreedySpineOptimisation(Embedding embedding) {
		int[] randomOrder = RandomUtil.randomPermutation(embedding.getN());
		return exhaustiveGreedySpineOptimisationWithOrder(embedding, randomOrder);
	}

	/**
	 * Optimizes the embedding by moving vertices to best positions in terms of
	 * crossings. This is repeated till no improvement was possible.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @param order
	 *            the order in which the vertices are handled by vertex index
	 *            (not spine position)
	 * @return the gain of the optimization
	 */
	public static long exhaustiveGreedySpineOptimisationWithOrder(Embedding embedding, int[] order) {
		long startX = embedding.getNumberOfCrossings();

		long roundGain = 0;
		long overallGain = 0;

		// 1. iterate as long as gain is made
		boolean gainedSomething = true;
		do {
			gainedSomething = false;

			// 2. iterate over positions/vertices:
			// if found better for one > move it > repeat for all
			roundGain = findBestPositionForVerticesWithOrder(embedding, order);

			if (roundGain > 0) {
				gainedSomething = true;
				overallGain += roundGain;
			}

		} while (gainedSomething); // ends gaining while loop

		embedding.setNumberOfCrossings(startX - overallGain);

		return overallGain;
	}

	/**
	 * Finds for all vertices in random order the best position on the spine and
	 * moves it there, i.e. for each vertex the best position is searched once,
	 * and returns the total gain. Runs in O(n^2 * delta^2).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @return total gain
	 */
	public static long findBestPositionForVertices(Embedding embedding) {
		int[] randomOrder = RandomUtil.randomPermutation(embedding.getN());
		return findBestPositionForVerticesWithOrder(embedding, randomOrder);
	}

	/**
	 * Finds for all vertices in given order the best position on the spine and
	 * moves it there, i.e. for each vertex the best position is searched once,
	 * and returns the total gain. Runs in O(n^2 * delta^2).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @param order
	 *            the order in which the vertices are handled by vertex index
	 *            (not spine position)
	 * @return total gain
	 */
	public static long findBestPositionForVerticesWithOrder(Embedding embedding, int[] order) {
		long gain = 0;

		for (int i = 0; i < embedding.getN(); i++) {
			int startPos = embedding.getPositionOnSpine(order[i]);

			// 3. find best position for this vertex at position startPos
			// 4. update gain
			gain += findBestPositionForVertex(embedding, startPos);
		}

		return gain;
	}

	/**
	 * Finds the best position for the vertex at given start position on the
	 * spine and moves it there in O(n * delta^2).
	 * 
	 * @param embedding
	 *            the {@link Embedding} to work on
	 * @param startPositionOnSpine
	 *            position on the spine of the vertex which is moved to best
	 *            position in terms of crossings
	 * @return the gain of moving the vertex or 0, if no move was done
	 */
	public static long findBestPositionForVertex(Embedding embedding, int startPositionOnSpine) {
		int n = embedding.getN();
		int bestPos = startPositionOnSpine;
		long currentGain = 0;
		long gain = 0;

		// A.1 from its position on spine move it to right and find best
		for (int q = startPositionOnSpine + 1; q < n; q++) {
			currentGain += computeGainForSwapOfNeighbouringVerticesAt(q - 1, q, embedding);
			embedding.swapPositions(q - 1, q);

			if (currentGain > gain) {
				gain = currentGain;
				bestPos = q;
			}
		}
		// swap back
		for (int q = n - 1; q > startPositionOnSpine; q--) {
			embedding.swapPositions(q - 1, q);
		}
		currentGain = 0; // back to start position, gain is 0

		// A.2 from its position on spine move it to left and find best
		for (int q = startPositionOnSpine - 1; q >= 0; q--) {
			currentGain += computeGainForSwapOfNeighbouringVerticesAt(q, q + 1, embedding);
			embedding.swapPositions(q, q + 1);

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

		// B. swap to best found
		if (bestPos > startPositionOnSpine) {
			for (int q = startPositionOnSpine + 1; q <= bestPos; q++) {
				embedding.swapPositions(q - 1, q);
			}
		} else if (bestPos < startPositionOnSpine) {
			for (int q = startPositionOnSpine - 1; q >= bestPos; q--) {
				embedding.swapPositions(q, q + 1);
			}
		}

		return gain;
	}

	/**
	 * Calculates the gain for swapping the vertices at the given positions,
	 * where leftVPosition + 1 = rightVPostion. Does neither swap the vertices
	 * in the embedding nor invalidate the number of crossings.<br>
	 * 
	 * To investigate neighbors over boundary:<br>
	 * leftVPostions = n - 1 and rightVPosition = 0
	 * 
	 * @param leftVPosition
	 *            - rightVPosition - 1
	 * @param rightVPosition
	 *            - leftVPosition + 1
	 * @param emb
	 * @return the gain for swapping the vertices at the given positions
	 */
	public static long computeGainForSwapOfNeighbouringVerticesAt(int leftVPosition,
			int rightVPosition, Embedding emb) {
		if ((leftVPosition + 1 != rightVPosition)
				&& !((leftVPosition == emb.getN() - 1) && (rightVPosition == 0))) {
			// works only for neighbors
			throw new IllegalArgumentException(leftVPosition + " " + rightVPosition);
		}

		// 1. get edges
		// - get edges of left vertex
		int lVPosition = leftVPosition;
		int leftVIndex = emb.getVertexAtPosition(lVPosition);
		List<Edge> leftE = emb.getGraph().getVertexByIndex(leftVIndex).getEdges();

		// - get edges of right vertex
		int rVPosition = rightVPosition;
		int rightVIndex = emb.getVertexAtPosition(rVPosition);
		List<Edge> rightE = emb.getGraph().getVertexByIndex(rightVIndex).getEdges();

		long xBefore = 0; // before swap
		long xAfter = 0; // after swap

		// 2. count for all edges from left if they cross with cross with edges
		// on right
		leftEdgesLoop: for (Edge lEdge : leftE) {

			// - information of a left edge
			int lPage = emb.getPageOfEdge(lEdge.getIndex());
			int l2VIndex = (lEdge.getStart().getIndex() != leftVIndex) ? lEdge.getStart()
					.getIndex() : lEdge.getTarget().getIndex();
			int l2VPosition = emb.getPositionOnSpine(l2VIndex);

			if (l2VPosition == rVPosition) {
				// ignore edge because of incidence no crossing possible
				continue leftEdgesLoop;
			}

			rightEdgesLoop: for (Edge rEdge : rightE) {
				if ((rEdge == lEdge) || (emb.getPageOfEdge(rEdge.getIndex()) != lPage)) {
					continue rightEdgesLoop;
				}

				// - information of a right edge
				int r2VIndex = (rEdge.getStart().getIndex() != rightVIndex) ? rEdge.getStart()
						.getIndex() : rEdge.getTarget().getIndex();
				int r2VPosition = emb.getPositionOnSpine(r2VIndex);

				if ((r2VPosition == lVPosition) || (r2VPosition == l2VPosition)) {
					// ignore edge because of incidence no crossing possible
					continue rightEdgesLoop;
				}

				// 3. they either cross before or after swap
				// 3.1 left = n-1 and right = 0
				if ((leftVPosition == (emb.getN() - 1)) && (rightVPosition == 0)) {
					// l2 - l1 and // r1 - r2 always
					if (l2VPosition < r2VPosition) {
						// r1 - l2 - r2 - l1 << xBefore but no xAfter
						xBefore++;
					} else {
						// r1 - r2 - l2 - l1 << no xBefore but xAfter
						xAfter++;
					}
				} else { // 3.2 otherwise
					if (l2VPosition < lVPosition) { // l2 - l1
						if (r2VPosition < rVPosition) { // r2 - r1
							if (r2VPosition < l2VPosition) { // r2 - l2
								// r2 - l2 - l1 - r1 << no xBefore but xAfter
								xAfter++;
							} else { // l2 - r2
								// l2 - r2 - l1 - r1 << xBefore but no xAfter
								xBefore++;
							}
						} else { // l2 - l1 - r1 - r2 << no xBefore but xAfter
							xAfter++;
						}
					} else { // l1 - l2
						if (r2VPosition < rVPosition) { // r2 - r1
							// r2 - l1 - r1 - l2 << xBefore but no xAfter
							xBefore++;
						} else { // r1 - r2
							if (r2VPosition < l2VPosition) {
								// l1 - r1 - r2 - l2 << no xBefore but xAfter
								xAfter++;
							} else { // l1 - r1 - l2 - r2 << xBefore but no
										// xAfter
								xBefore++;
							}
						}
					}
				}

			}
		}

		return xBefore - xAfter;
	}
}
