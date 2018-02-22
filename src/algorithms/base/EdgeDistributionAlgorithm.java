package algorithms.base;

import model.Embedding;

public interface EdgeDistributionAlgorithm {
	
	/**
	 * Computes an edge embedding for the given spine in {@code embedding}.
	 * The result is stored in {@code embedding}.
	 * @param embedding
	 */
	public void computeEdgeDistribution(Embedding embedding);	

}
