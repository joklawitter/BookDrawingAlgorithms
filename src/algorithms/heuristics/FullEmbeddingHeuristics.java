package algorithms.heuristics;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import algorithms.base.FullEmbeddingAlgorithm;
import algorithms.heuristics.combinations.FullGreedyConnectivityAlgorithm;
import algorithms.heuristics.combinations.FullRandomBFSAlgorithm;
import algorithms.heuristics.combinations.FullRandomDFSAlgorithm;
import algorithms.heuristics.combinations.FullSmallestDegreeDFSAlgorithm;

/**
 * This enum contains all full embedding heuristics. The name of the algorithm
 * can be retrieved by calling {@link #toString()}.
 * 
 * @author Jonathan Klawitter
 *
 */
public enum FullEmbeddingHeuristics {
	CON_GREEDY_FULL(new FullGreedyConnectivityAlgorithm(), "conGreedy+"), SMALLEST_DEGREE_DFS_FULL(
			new FullSmallestDegreeDFSAlgorithm(), "smlDgrDFS+"), RDFS_FULL(new FullRandomDFSAlgorithm(),
					"randDFS+"), RBFS_FULL(new FullRandomBFSAlgorithm(), "randBFS+"), DO_NOTHING(x -> {
					}, "-");

	private final FullEmbeddingAlgorithm algorithm;

	private final String name;

	private FullEmbeddingHeuristics(FullEmbeddingAlgorithm algorithm) {
		this.algorithm = algorithm;
		name = this.name().replace("_", "-");
	}

	private FullEmbeddingHeuristics(FullEmbeddingAlgorithm algorithm, String name) {
		this.algorithm = algorithm;
		this.name = name;
	}

	/**
	 * @return the algorithm used in this heuristic
	 */
	public FullEmbeddingAlgorithm getAlgorithm() {
		return algorithm;
	}

	@Override
	public String toString() {
		return name;
	}

	/** A set of all heuristics. */
	public static final Set<FullEmbeddingHeuristics> ALL = Collections
			.unmodifiableSet(EnumSet.allOf(FullEmbeddingHeuristics.class));

	// used for quick checking some specific
	public static final Set<FullEmbeddingHeuristics> MAIN = Collections.unmodifiableSet(EnumSet.of(CON_GREEDY_FULL
	// , SMALLEST_DEGREE_DFS_FULL, RDFS_FULL , RBFS_FULL
	));

	public static final Set<FullEmbeddingHeuristics> NONE = Collections.unmodifiableSet(EnumSet.of(DO_NOTHING));
}
