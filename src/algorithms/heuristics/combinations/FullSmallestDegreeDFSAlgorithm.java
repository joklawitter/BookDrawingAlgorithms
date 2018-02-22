package algorithms.heuristics.combinations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.FullEmbeddingAlgorithm;

/**
 * Implementation of the DFS vertex order + edge distribution algorithm. The
 * graph is traversed in depth first order and vertices are ordered accordingly.
 * The root vertex is chosen by degree (smallest) as is the order in which the
 * neighbours of the current vertex are visited. Edges are distributed when
 * first visited.
 * 
 * @author Jonathan Klawitter, (based on Michael Wegner's RDFS)
 */
public class FullSmallestDegreeDFSAlgorithm implements FullEmbeddingAlgorithm {

	/**
	 * Generates a vertex order for the given {@link Embedding} using a smallest
	 * degree DFS on the underlying graph and simultaneously distributes the
	 * edges.
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
		if (1 == n) {
			return; // only one element to order => nothing to do
		}

		int[] vertexOnSpine = embedding.getVertexOnSpineArray();
		int[] spine = embedding.getSpine();
		ArrayList<Edge> alreadyPlacedEdges = new ArrayList<Edge>(g.getM());
		CombinedHeuristicsHelper.initDistribution(embedding.getDistribution());
		
		// we only change the order of vertices spine[begin]..spine[end-1]
		for (int i = 0; i < n; ++i) {
			vertexOnSpine[spine[i]] = -1;
		}

		// get start position and index
		int rootPosition = findPositionOfVertexWithSmallestDegree(embedding);
		int rootIndex = spine[rootPosition];

		Stack<Integer> stack = new Stack<Integer>();
		boolean[] visitedVertices = new boolean[g.getN()];
		
		int idx = 0;
		// continue if not reached end / still after begin without flip
		while (idx < n) {
			// graph may not be connected -> find not visited vertex
			while (vertexOnSpine[rootIndex] != -1) {
				rootPosition = (rootPosition + 1) % embedding.getN();
				rootIndex = spine[rootPosition];
			}

			// order connected component
			stack.push(rootIndex);
			while (!stack.empty()) {
				int v = stack.pop();
				if (!visitedVertices[v]) {
					visitedVertices[v] = true;

					// only set new index if vertex belongs to the set of
					// vertices to order
					if (vertexOnSpine[v] == -1) {
						vertexOnSpine[v] = idx++;
					}

					ArrayList<Vertex> neighbors = new ArrayList<Vertex>();
					Vertex vertex = g.getVertexByIndex(v);
					List<Edge> edges = vertex.getEdges();
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

					// sorts by degree descending, thus y and x are swapped
					Collections.sort(neighbors,
							(Vertex x, Vertex y) -> Integer.compare(y.getDegree(), x.getDegree()));

					// sorted descending -> smallest degree on top
					for (Vertex u : neighbors) {
						stack.push(u.getIndex());
					}
				}
			}
		}

		embedding.setVertexOnSpine(vertexOnSpine);

	}

	private int findPositionOfVertexWithSmallestDegree(Embedding embedding) {
		Graph g = embedding.getGraph();
		int[] spine = embedding.getSpine();

		int smallestPosition = 0;
		int smallestDegree = Integer.MAX_VALUE;

		for (int i = 0; i < g.getN(); i++) {
			int currentDegree = g.getDegreeOf(spine[i]);
			if (currentDegree < smallestDegree) {
				smallestDegree = currentDegree;
				smallestPosition = i;
			}
		}

		return smallestPosition;
	}

}
