package algorithms.heuristics.vertexorder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import util.RandomUtil;
import model.Embedding;
import algorithms.base.VertexOrderAlgorithm;

/**
 * This class implements the algorithm generating a random vertex order.
 *
 * @author Jonathan Klawitter
 */
public class RandomVertexOrderAlgorithm implements VertexOrderAlgorithm {

	/**
	 * Generates a random vertex order for the given {@link Embedding}.
	 * 
	 * @param embedding
	 *            the {@link Embedding} for which a vertex order is randomly
	 *            generated
	 */
	@Override
	public void computeVertexOrder(Embedding embedding) {
		embedding.setSpine(RandomUtil.randomPermutation(embedding.getN()));
	}

	/**
	 * Generates a random vertex order for the given {@link Embedding} using a
	 * given {@link Random}.
	 * 
	 * @param embedding
	 *            the {@link Embedding} for which a vertex order is randomly
	 *            generated
	 * @param rand
	 *            {@link Random} used as source of randomness
	 */
	public void computeVertexOrder(Embedding embedding, Random rand) {
		int[] spine = embedding.getSpine();
		Collections.shuffle(Arrays.asList(spine), rand);
		embedding.setSpine(spine);
	}

}
