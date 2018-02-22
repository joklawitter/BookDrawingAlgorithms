package algorithms.base;

import model.Embedding;

public interface VertexOrderAlgorithm {
	
	/**
	 * Computes a vertex order and stores the result in the given {@code embedding}.
	 * @param embedding
	 */
	public void computeVertexOrder(Embedding embedding);
	
}
