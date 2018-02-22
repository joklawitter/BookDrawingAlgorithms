package util;

import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;

/**
 * This factory class provides methods to create conflict graphs or their
 * complement for given {@link Embedding}s.<br>
 * A conflict graph C of an embedding over a graph G is a graph with V(C) = E(G)
 * and E(C) = { ef | e,f from E(G), e,f possibly cross in the embedding}.
 *
 * @author Jonathan Klawitter
 */
public class ConflictGraphFactory {

	public static Graph createConflictGraphComplement(Embedding baseEmbedding) {
		return createConflictGraph(baseEmbedding, true);
	}

	public static Graph createConflictGraph(Embedding baseEmbedding) {
		return createConflictGraph(baseEmbedding, false);
	}

	private static Graph createConflictGraph(Embedding baseEmbedding, boolean complement) {
		int m = baseEmbedding.getM();
		Vertex[] vertices = new Vertex[m];
		for (int e = 0; e < m; ++e) {
			vertices[e] = new Vertex(e);
		}

		int numEdges = 0;
		for (int e1 = 0; e1 < m; ++e1) {
			for (int e2 = e1 + 1; e2 < m; ++e2) {
				boolean cross = baseEmbedding.canEdgesCross(e1, e2);
				// short: cross ^ complement
				if ((cross && !complement) || (!cross && complement)) {
					new Edge(vertices[e1], vertices[e2]);
					numEdges++;
				}
			}
		}

		String name = "ConflictGraph";
		if (complement) {
			name += "Complement";
		}

		return new Graph(vertices, m, numEdges, name);
	}
}
