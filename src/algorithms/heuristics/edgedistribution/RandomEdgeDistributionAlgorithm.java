package algorithms.heuristics.edgedistribution;

import java.util.Random;

import util.RandomUtil;
import model.Embedding;
import algorithms.base.EdgeDistributionAlgorithm;

/**
 * This class implements the algorithm generating a random edge distribution.
 *
 * @author Jonathan Klawitter
 */
public class RandomEdgeDistributionAlgorithm implements EdgeDistributionAlgorithm {

	/**
	 * Generates a random edge distribution for the given {@link Embedding}.
	 * 
	 * @param embedding
	 *            the {@link Embedding} for which an edge distribution is randomly
	 *            generated
	 */
	@Override
	public void computeEdgeDistribution(Embedding embedding) {
		computeEdgeDistribution(embedding, RandomUtil.getRandom());
	}

	/**
	 * Generates a random edge distribution for the given {@link Embedding}
	 * using a given {@link Random}.
	 * 
	 * @param embedding
	 *            the {@link Embedding} for which an edge distribution is
	 *            randomly generated
	 * @param rand
	 *            {@link Random} used as source of randomness
	 */
	public void computeEdgeDistribution(Embedding embedding, Random rand) {
		int[] distribution = embedding.getDistribution();
		for (int i = 0; i < distribution.length; i++) {
			distribution[i] = rand.nextInt(embedding.getK());
		}
	}

}
