package algorithms.heuristics.vertexorder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import util.RandomUtil;
import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.VertexOrderAlgorithm;

/**
 * This class implements the random breadth-first search algorithm on a graph to
 * compute a vertex order for its embedding.
 *
 * @author Jonathan Klawitter
 */
public class RandomBFSAlgorithm implements VertexOrderAlgorithm {

	/**
	 * Generates a vertex order for the given {@link Embedding} using a BFS on
	 * the underlying graph.
	 * 
	 * @param embedding
	 *            the {@link Embedding} for which a vertex order is computed
	 */
	@Override
	public void computeVertexOrder(Embedding embedding) {
		Graph g = embedding.getGraph();

		int[] spine = new int[g.getN()];
		int[] vertexOnSpine = new int[g.getN()];
		Arrays.fill(vertexOnSpine, -1);
		
		int position = 0;
		boolean[] listed = new boolean[g.getN()];

		Random random = RandomUtil.getRandom();

		Queue<Integer> q = new LinkedList<Integer>();

		// get start vertex
		int startIndex = random.nextInt(g.getN());


		// while q not empty
		while (position < g.getN()) {
			// graph may not be connected -> find not visited vertex
			while (vertexOnSpine[startIndex] != -1) {
				startIndex = (startIndex + 1) % g.getN();
			}
			
			q.add(startIndex);
			while (!q.isEmpty()) {
				int nextIndex = q.remove();

				listed[nextIndex] = true;
				vertexOnSpine[nextIndex] = position;
				spine[position++] = nextIndex;

				// add neighbours to q
				Vertex[] neighbours = g.getVertexByIndex(nextIndex).getNeighbors();
				int[] permutation = RandomUtil.randomPermutation(neighbours.length);
				for (int i = 0; i < permutation.length; i++) {
					int current = neighbours[permutation[i]].getIndex();
					if (listed[current]) {
						continue;
					} else {
						listed[current] = true;
						q.add(current);
					}
				}
			}
		}

		// check
		if (position != g.getN()) {
			throw new AssertionError(
					"Not all vertices visited in RBFS. Graph may not be connected.");
		}

		embedding.setSpine(spine);
	}

}
