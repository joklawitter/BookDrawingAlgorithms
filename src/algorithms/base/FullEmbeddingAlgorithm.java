package algorithms.base;

import model.Embedding;

public interface FullEmbeddingAlgorithm {

	/**
	 * Computes an embedding consisting of a spine and an edge embedding. 
	 * The result is stored in {@code embedding}
	 * @param embedding 
	 */
	public void computeEmbedding(Embedding embedding);
}
