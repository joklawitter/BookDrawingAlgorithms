package algorithms.heuristics.combinations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import util.RandomUtil;
import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.FullEmbeddingAlgorithm;

/**
 * This class implements the random breadth-first search algorithm on a graph to
 * compute a vertex order and simultaneously an edge distribution for its embedding.
 *
 * @author Jonathan Klawitter
 */
public class FullRandomBFSAlgorithm implements FullEmbeddingAlgorithm {

	/**
	 * Generates a vertex order for the given {@link Embedding} using a BFS on
	 * the underlying graph and simultaneously distributed the edges.
	 * 
	 * @param embedding
	 *            the {@link Embedding} for which an embedding is computed
	 */
	@Override
	public void computeEmbedding(Embedding embedding) {
		if (embedding.getK() == 1) {
			throw new IllegalArgumentException("Algorithm is only for k > 1.");
		}

		Graph g = embedding.getGraph();
		int n = g.getN();

		int[] spine = new int[n];
		int[] vertexOnSpine = new int[n];
		Arrays.fill(vertexOnSpine, -1);
		ArrayList<Edge> alreadyPlacedEdges = new ArrayList<Edge>(g.getM());
		CombinedHeuristicsHelper.initDistribution(embedding.getDistribution());
		
		int position = 0;
		boolean[] listed = new boolean[n];
		boolean[] placed = new boolean[n];

		Random random = RandomUtil.getRandom();

		Queue<Integer> q = new LinkedList<Integer>();

		// get start vertex
		int startIndex = random.nextInt(n);


		// while q not empty
		while (position < n) {
			// graph may not be connected -> find not visited vertex
			while (vertexOnSpine[startIndex] != -1) {
				startIndex = (startIndex + 1) % n;
			}
			
			q.add(startIndex);
			while (!q.isEmpty()) {
				int nextIndex = q.remove();

				listed[nextIndex] = true;
				placed[nextIndex] = true;
				vertexOnSpine[nextIndex] = position;
				spine[position++] = nextIndex;

				// add neighbors to q
				Vertex vertex = g.getVertexByIndex(nextIndex);
				List<Edge> edges = vertex.getEdges();
				Collections.shuffle(edges);
				for (Edge edge : edges) {
					int current = edge.getOtherEnd(vertex).getIndex();
					if (!listed[current]) {
						listed[current] = true;
						q.add(current);
					} else if (placed[current]){
						CombinedHeuristicsHelper.placeEdgeOnBestPage(embedding, edge,
								alreadyPlacedEdges, embedding.getK());
						alreadyPlacedEdges.add(edge);
					}
				}
			}
		}

		// check
		if (position != n) {
			throw new AssertionError(
					"Not all vertices visited in RBFS. Graph may not be connected.");
		}
		if (alreadyPlacedEdges.size() != g.getM()) {
			System.out.println(alreadyPlacedEdges.size());
			System.out.println(embedding.toString());
			throw new IllegalStateException("Not all edges distributed.");
		}

		embedding.setSpine(spine);
	}

}
