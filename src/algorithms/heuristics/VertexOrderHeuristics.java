package algorithms.heuristics;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import algorithms.base.VertexOrderAlgorithm;
import algorithms.heuristics.combinations.FullGreedyConnectivityAlgorithm;
import algorithms.heuristics.vertexorder.BFSTreeAlgorithm;
import algorithms.heuristics.vertexorder.ConnectivityAlgorithm;
import algorithms.heuristics.vertexorder.ConnectivityAlgorithm.VertexPlacer;
import algorithms.heuristics.vertexorder.ConnectivityAlgorithm.VertexSelector;
import algorithms.heuristics.vertexorder.GreedyConnectivityAlgorithm;
import algorithms.heuristics.vertexorder.HamiltonPathAlgorithm;
import algorithms.heuristics.vertexorder.IdfsAlgorithm;
import algorithms.heuristics.vertexorder.MaxNbrAlgorithm;
import algorithms.heuristics.vertexorder.RandomBFSAlgorithm;
import algorithms.heuristics.vertexorder.RandomDFSAlgorithm;
import algorithms.heuristics.vertexorder.RandomVertexOrderAlgorithm;
import algorithms.heuristics.vertexorder.SmallestDegreeDFSAlgorithm;

/**
 * This enum contains all vertex order heuristics. The name of the algorithm can
 * be retrieved by calling {@link #toString()}.
 * 
 * @author Matthias Wolf, Jonathan Klawitter
 *
 */
public enum VertexOrderHeuristics {
	CON_GREEDY(new GreedyConnectivityAlgorithm(), "conGreedy"), CON_GREEDY_FULL(
			new FullGreedyConnectivityAlgorithm(), "conGreedy+"), CON_CROSSINGS(
					new ConnectivityAlgorithm(VertexSelector.CONNECTIVITY, VertexPlacer.CROSSINGS),
					"conCro"), CON_NEXT_CROSSINGS(new ConnectivityAlgorithm(VertexSelector.NEXT,
							VertexPlacer.CROSSINGS)), CON_NEXT_ELEN(new ConnectivityAlgorithm(
									VertexSelector.NEXT,
									VertexPlacer.ELEN)), CON_NEXT_FIXED(new ConnectivityAlgorithm(
											VertexSelector.NEXT,
											VertexPlacer.FIXED)), CON_NEXT_RANDOM(
													new ConnectivityAlgorithm(VertexSelector.NEXT,
															VertexPlacer.RANDOM)), CON_CON_ELEN(
																	new ConnectivityAlgorithm(
																			VertexSelector.CONNECTIVITY,
																			VertexPlacer.ELEN)), CON_CON_FIXED(
																					new ConnectivityAlgorithm(
																							VertexSelector.CONNECTIVITY,
																							VertexPlacer.FIXED)), CON_CON_RANDOM(
																									new ConnectivityAlgorithm(
																											VertexSelector.CONNECTIVITY,
																											VertexPlacer.RANDOM)), CON_INCON_CROSSINGS(
																													new ConnectivityAlgorithm(
																															VertexSelector.INCON,
																															VertexPlacer.CROSSINGS)), CON_INCON_ELEN(
																																	new ConnectivityAlgorithm(
																																			VertexSelector.INCON,
																																			VertexPlacer.ELEN)), CON_INCON_FIXED(
																																					new ConnectivityAlgorithm(
																																							VertexSelector.INCON,
																																							VertexPlacer.FIXED)), CON_INCON_RANDOM(
																																									new ConnectivityAlgorithm(
																																											VertexSelector.INCON,
																																											VertexPlacer.RANDOM)), CON_OUTCON_CROSSINGS(
																																													new ConnectivityAlgorithm(
																																															VertexSelector.OUTCON,
																																															VertexPlacer.CROSSINGS)), CON_OUTCON_ELEN(
																																																	new ConnectivityAlgorithm(
																																																			VertexSelector.OUTCON,
																																																			VertexPlacer.ELEN)), CON_OUTCON_FIXED(
																																																					new ConnectivityAlgorithm(
																																																							VertexSelector.OUTCON,
																																																							VertexPlacer.FIXED)), CON_OUTCON_RANDOM(
																																																									new ConnectivityAlgorithm(
																																																											VertexSelector.OUTCON,
																																																											VertexPlacer.RANDOM)), CON_RANDOM_CROSSINGS(
																																																													new ConnectivityAlgorithm(
																																																															VertexSelector.RANDOM,
																																																															VertexPlacer.CROSSINGS)), CON_RANDOM_ELEN(
																																																																	new ConnectivityAlgorithm(
																																																																			VertexSelector.RANDOM,
																																																																			VertexPlacer.ELEN)), CON_RANDOM_FIXED(
																																																																					new ConnectivityAlgorithm(
																																																																							VertexSelector.RANDOM,
																																																																							VertexPlacer.FIXED)), CON_RANDOM_RANDOM(
																																																																									new ConnectivityAlgorithm(
																																																																											VertexSelector.RANDOM,
																																																																											VertexPlacer.RANDOM)), IDFS(
																																																																													new IdfsAlgorithm()), SMALLEST_DEGREE_DFS(
																																																																															new SmallestDegreeDFSAlgorithm(),
																																																																															"smlDgrDFS"), RDFS(
																																																																																	new RandomDFSAlgorithm(),
																																																																																	"randDFS"), BFS_Tree(
																																																																																			new BFSTreeAlgorithm(),
																																																																																			"treeBFS"), RBFS(
																																																																																					new RandomBFSAlgorithm(),
																																																																																					"randBFS"), MAX_NBR(
																																																																																							new MaxNbrAlgorithm(),
																																																																																							"maxNbr"), MAX_NBR_REMOVING(
																																																																																									new MaxNbrAlgorithm(),
																																																																																									"maxNbrR"), Hamilton(
																																																																																											new HamiltonPathAlgorithm(),
																																																																																											"hamilton"), RANDOM(
																																																																																													new RandomVertexOrderAlgorithm(),
																																																																																													"random"), DO_NOTHING(
																																																																																															x -> {
																																																																																															} ,
																																																																																															"-");

	private final VertexOrderAlgorithm algorithm;

	private final String name;

	private VertexOrderHeuristics(VertexOrderAlgorithm algorithm) {
		this.algorithm = algorithm;
		name = this.name().replace("_", "-");
	}

	private VertexOrderHeuristics(VertexOrderAlgorithm algorithm, String name) {
		this.algorithm = algorithm;
		this.name = name;
	}

	/**
	 * @return the algorithm used in this heuristic
	 */
	public VertexOrderAlgorithm getAlgorithm() {
		return algorithm;
	}

	@Override
	public String toString() {
		return name;
	}

	/** The VO heuristics for the full binary tree test. */
	public static final Set<VertexOrderHeuristics> TREE_TEST_VOH = Collections
			.unmodifiableSet(EnumSet.of(CON_CROSSINGS
	// CON_CON_ELEN
	// RBFS,
	// MAX_NBR,
	// MAX_NBR_REMOVING,
	// RANDOM
	// MEAN_ITERATION, MEDIAN_ITERATION
	));

	/** The VO heuristics we test. */
	public static final Set<VertexOrderHeuristics> MAIN_VOH = Collections.unmodifiableSet(EnumSet
			.of(CON_GREEDY, CON_GREEDY_FULL, CON_CROSSINGS, SMALLEST_DEGREE_DFS, RDFS, BFS_Tree
	// RBFS
	// MAX_NBR_REMOVING
	// Hamilton
	// MAX_NBR,
	// RANDOM
	// MEAN_ITERATION, MEDIAN_ITERATION
	));

	/** The VO heuristics we test. */
	public static final Set<VertexOrderHeuristics> PROMISING_VOH = Collections
			.unmodifiableSet(EnumSet.of(CON_CROSSINGS, CON_GREEDY, RDFS, SMALLEST_DEGREE_DFS, // RBFS,
					BFS_Tree
	// Hamilton, MAX_NBR,
	// RANDOM
	// MEAN_ITERATION, MEDIAN_ITERATION
	));

	/** A set of all heuristics. */
	public static final Set<VertexOrderHeuristics> ALL = Collections
			.unmodifiableSet(EnumSet.allOf(VertexOrderHeuristics.class));

	// used for quick checking some specific
	public static final Set<VertexOrderHeuristics> QUICK_TESTING = Collections
			.unmodifiableSet(EnumSet.of(CON_GREEDY, CON_GREEDY_FULL
	// CON_CROSSINGS
	// SMALLEST_DEGREE_DFS
	// RDFS
	// BFS_Tree
	// RBFS ,
	// MAX_NBR_REMOVING
	// Hamilton
	));
	public static final Set<VertexOrderHeuristics> QUICK_TESTING2 = Collections.unmodifiableSet(
			EnumSet.of(CON_GREEDY, CON_CROSSINGS, SMALLEST_DEGREE_DFS, RDFS, Hamilton, BFS_Tree
	// RBFS
	// MAX_NBR_REMOVING
	// MAX_NBR,
	));
	public static final Set<VertexOrderHeuristics> QUICK_TESTING3 = Collections
			.unmodifiableSet(EnumSet.of(DO_NOTHING
	// CON_GREEDY,
	// CON_CROSSINGS
	// SMALLEST_DEGREE_DFS
	// RDFS,
	// BFS_Tree,
	// RBFS,
	// MAX_NBR_REMOVING
	// Hamilton, MAX_NBR,
	));

	public static final Set<VertexOrderHeuristics> NONE = Collections
			.unmodifiableSet(EnumSet.of(DO_NOTHING));
}
