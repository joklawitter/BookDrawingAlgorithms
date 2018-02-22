package algorithms.heuristics.vertexorder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import util.GraphUtils;
import util.RandomUtil;
import model.Edge;
import model.Embedding;
import model.Graph;
import model.Problem;
import model.Vertex;
import algorithms.Embedder;
import algorithms.base.VertexOrderAlgorithm;
import algorithms.heuristics.VertexOrderHeuristics;

/**
 * This class implements the breadth-first search tree algorithm on a graph to
 * compute a vertex order for its embedding. It computes a BFS spanning tree and
 * then uses the vertex order for a crossing free circular drawing of this tree.
 *
 * @author Jonathan Klawitter
 */
public class BFSTreeAlgorithm implements VertexOrderAlgorithm {

	/**
	 * Generates a vertex order for the given {@link Embedding} using an
	 * embedding of a BFS spanning tree of the underlying graph.
	 * 
	 * @param embedding
	 *            the {@link Embedding} for which a vertex order is computed
	 */
	@Override
	public void computeVertexOrder(Embedding embedding) {
		Graph g = embedding.getGraph();
		int n = g.getN();
		
		Vertex[] treeVertices = GraphUtils.getInitialVertexArray(n);
		int treeM = 0;

		int[] vertexOnSpine = new int[n];
		Arrays.fill(vertexOnSpine, -1);

		int numberOfVisited = 0;
		boolean[] visited = new boolean[n];

		Random random = RandomUtil.getRandom();

		Queue<Integer> q = new LinkedList<Integer>();

		// get start vertex
		int startIndex = random.nextInt(n);

		// while q not empty
		while (numberOfVisited < n) {
			// graph may not be connected -> find not visited vertex
			while (visited[startIndex]) {
				startIndex = (startIndex + 1) % n;
			}

			q.add(startIndex);
			while (!q.isEmpty()) {
				int nextIndex = q.remove();

				visited[nextIndex] = true;
				numberOfVisited++;

				// add neighbors to q
				Vertex[] neighbours = g.getVertexByIndex(nextIndex).getNeighbors();
				int[] permutation = RandomUtil.randomPermutation(neighbours.length);
				for (int i = 0; i < permutation.length; i++) {
					int current = neighbours[permutation[i]].getIndex();
					if (visited[current]) {
						continue;
					} else {
						visited[current] = true;
						q.add(current);
						
						// add edge to tree
						new Edge(treeVertices[current], treeVertices[nextIndex]);
						treeM++;
					}
				}
			}
		}
		
		// we know have a BFS tree and compute an embedding of it
		Graph tree = new Graph(treeVertices, n, treeM);
		Embedding treeEmbedding = new Embedding(new Problem(tree, 1));
		Embedder.heuristicsEmbedding(treeEmbedding, VertexOrderHeuristics.SMALLEST_DEGREE_DFS, null);
		
		// the computed spine maps from position to vertex index
		// we used the same as in given graph -> just take this spine
		embedding.setSpine(treeEmbedding.getSpine());
	}

}
