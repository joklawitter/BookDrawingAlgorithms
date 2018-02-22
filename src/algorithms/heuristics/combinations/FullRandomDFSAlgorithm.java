package algorithms.heuristics.combinations;

import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.FullEmbeddingAlgorithm;

import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of the RDFS vertex order + edge distribution algorithm. The
 * graph is traversed in depth first order and vertices are ordered accordingly.
 * The root vertex is chosen uniformly at random as is the order in which the
 * neighbors of the current vertex are visited. Edges are distributed when first
 * visited.
 * 
 * @author Jonathan Klawitter, (based on Michael Wegner's RDFS) 
 */
public class FullRandomDFSAlgorithm implements FullEmbeddingAlgorithm {

	/**
	 * Generates a vertex order for the given {@link Embedding} using a DFS on
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
		int begin = 0;
		int n = g.getN();
		if (1 == n) {
			return; // only one element to order => nothing to do
		}

		int[] vertexOnSpine = embedding.getVertexOnSpineArray();
		int[] spine = embedding.getSpine();

		int[] distribution = embedding.getDistribution();
		CombinedHeuristicsHelper.initDistribution(distribution);
		ArrayList<Edge> alreadyPlacedEdges = new ArrayList<Edge>(g.getM());
		
		// we only change the order of vertices spine[begin]..spine[end-1]
		for (int i = 0; i < n; ++i) {
			vertexOnSpine[spine[i]] = -1;
		}

		// get start position and index
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		int rootPosition = rand.nextInt(n);;
		int rootIndex = spine[rootPosition];

		Stack<Integer> stack = new Stack<Integer>();
		boolean[] visitedVertices = new boolean[n];

		int idx = begin;
		// continue if not reached end / still after begin without flip
		while ((idx < n) || (n < begin && begin <= idx)) {
			// graph may not be connected -> find not visited vertex
			while (vertexOnSpine[rootIndex] != -1) {
				rootPosition = (rootPosition + 1) % n;
				rootIndex = spine[rootPosition];
			}

			stack.push(rootIndex);
			while (!stack.empty()) {
				int v = stack.pop();
				if (!visitedVertices[v]) {
					visitedVertices[v] = true;

					if (vertexOnSpine[v] == -1) {
						// only set new index if vertex belongs to the set of
						// vertices to order
						vertexOnSpine[v] = idx;
						idx = (idx + 1);
						if (begin > n) {
							idx = idx % n;
						}
					}

					ArrayList<Vertex> neighbors = new ArrayList<Vertex>();
					Vertex vertex = g.getVertexByIndex(v);
					List<Edge> edges = vertex.getEdges();
					Collections.shuffle(edges);

					for (Edge edge : edges) {
						Vertex u = edge.getOtherEnd(vertex);
						if (!visitedVertices[u.getIndex()]) {
							neighbors.add(u);
						} else {
							CombinedHeuristicsHelper.placeEdgeOnBestPage(embedding, edge,
									alreadyPlacedEdges, embedding.getK());
							alreadyPlacedEdges.add(edge);
						}
					}

					Collections.shuffle(neighbors);

					for (Vertex u : neighbors) {
						stack.push(u.getIndex());
					}
				}
			}
		}

		embedding.setVertexOnSpine(vertexOnSpine);

	}

}
