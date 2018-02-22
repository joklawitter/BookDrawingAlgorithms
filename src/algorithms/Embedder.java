package algorithms;

import java.util.Random;

import model.Embedding;
import util.RandomUtil;
import algorithms.base.FullEmbeddingAlgorithm;
import algorithms.heuristics.EdgeDistributionHeuristics;
import algorithms.heuristics.FullEmbeddingHeuristics;
import algorithms.heuristics.VertexOrderHeuristics;
import algorithms.heuristics.edgedistribution.RandomEdgeDistributionAlgorithm;
import algorithms.heuristics.vertexorder.RandomVertexOrderAlgorithm;

/**
 * This class provides methods to simply use the embedding algorithms.
 *
 * @author Jonathan Klawitter
 */
public class Embedder {

	private Embedder() {
		// should not be instantiated
	}

	/**
	 * Returns a {@link FullEmbeddingAlgorithm} for given vertex orderer and
	 * edge distributor.
	 * 
	 * @param orderer
	 *            to order the vertices
	 * @param distributor
	 *            to distribute the edges
	 * @return a {@link FullEmbeddingAlgorithm} for given vertex orderer and
	 *         edge distributor
	 */
	public static FullEmbeddingAlgorithm getFullEmbeddingAlgorithm(VertexOrderHeuristics orderer,
			EdgeDistributionHeuristics distributor) {
		return embedding -> {
			orderer.getAlgorithm().computeVertexOrder(embedding);
			distributor.getAlgorithm().computeEdgeDistribution(embedding);
		};
	}

	/**
	 * Embeds the given embedding with the specified algorithms.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to be changed
	 * @param orderer
	 *            the vertex order heuristic to be used, ignored if null
	 * @param distributor
	 *            the edge distribution heuristic to be used, ignored if null
	 */
	public static void heuristicsEmbedding(Embedding embedding, VertexOrderHeuristics orderer,
			EdgeDistributionHeuristics distributor) {
		if (orderer != null) {
			orderer.getAlgorithm().computeVertexOrder(embedding);
		}
		if (distributor != null) {
			distributor.getAlgorithm().computeEdgeDistribution(embedding);
		}
	}
	
	/**
	 * Embeds the given embedding with the specified algorithms.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to be changed
	 * @param heuristic
	 *            the {@link FullEmbeddingHeuristics} to compute an embedding with
	 */
	public static void heuristicsEmbedding(Embedding embedding, FullEmbeddingHeuristics heuristic) {
		if (heuristic != null) {
			heuristic.getAlgorithm().computeEmbedding(embedding);
		}
	}

	
	/**
	 * Creates a random embedding for the given one.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to be changed
	 * @param rand
	 *            a {@link Random} or null (then a new Random is created)
	 */
	public static void randomEmbedding(Embedding embedding, Random rand) {
		if (rand == null) {
			rand = RandomUtil.getRandom();
		}
		new RandomVertexOrderAlgorithm().computeVertexOrder(embedding, rand);
		new RandomEdgeDistributionAlgorithm().computeEdgeDistribution(embedding, rand);
	}

	


	/**
	 * Creates a embedding with sat4j given a spine.
	 * 
	 * @param embedding
	 *            the {@link Embedding} to be changed
	 */
	public static void sat4jEdgeEmbedding(Embedding embedding) {
//		new PlanarEdgeDistributionWithSatAlgorithm().computeEmbedding(embedding);
	}
}
