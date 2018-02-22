package algorithms.heuristics;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import algorithms.base.EdgeDistributionAlgorithm;
import algorithms.heuristics.edgedistribution.ConflictAlgorithm;
import algorithms.heuristics.edgedistribution.EarDecompositionAlgorithm;
import algorithms.heuristics.edgedistribution.GreedyEdgeDistributionAlgorithm;
import algorithms.heuristics.edgedistribution.RandomEdgeDistributionAlgorithm;
import algorithms.heuristics.edgedistribution.SlopeAlgorithm;

/**
 * Contains all edge distribution heuristics. To get the name of the heuristic
 * call {@link #toString()}.
 * 
 * @author Matthias Wolf, Jonathan Klawitter
 * @see VertexOrderHeuristics
 */
public enum EdgeDistributionHeuristics {
	CONFLICT(new ConflictAlgorithm(), "conflict"), SLOPE(new SlopeAlgorithm(),
			"slope"), CEIL_FLOOR(new GreedyEdgeDistributionAlgorithm(
					GreedyEdgeDistributionAlgorithm.Order.CEIL_FLOOR),
			"ceilFloor"), ELEN(
					new GreedyEdgeDistributionAlgorithm(
							GreedyEdgeDistributionAlgorithm.Order.E_LEN),
					"eLen"), CIRCULAR(
							new GreedyEdgeDistributionAlgorithm(
									GreedyEdgeDistributionAlgorithm.Order.CIRCULAR),
							"circ"), GREEDY_V(new GreedyEdgeDistributionAlgorithm(
									GreedyEdgeDistributionAlgorithm.Order.ROW_MAJOR_V)), GR_RAN(
											new GreedyEdgeDistributionAlgorithm(
													GreedyEdgeDistributionAlgorithm.Order.RANDOM)), GREEDY(
															new GreedyEdgeDistributionAlgorithm(
																	GreedyEdgeDistributionAlgorithm.Order.ROW_MAJOR)), EAR(
																			new EarDecompositionAlgorithm(),
																			"earDecomp"), RANDOM(
																					new RandomEdgeDistributionAlgorithm()), DO_NOTHING(
																							x -> {
																							} ,
																							"-");

	private final EdgeDistributionAlgorithm algorithm;

	private final String name;

	private EdgeDistributionHeuristics(EdgeDistributionAlgorithm algorithm) {
		this.algorithm = algorithm;
		name = this.name().replace("_", "-");
	}

	private EdgeDistributionHeuristics(EdgeDistributionAlgorithm algorithm, String name) {
		this.algorithm = algorithm;
		this.name = name;
	}

	public EdgeDistributionAlgorithm getAlgorithm() {
		return algorithm;
	}

	@Override
	public String toString() {
		return name;
	}

	/** A set of all edge distribution heuristics. */
	public static final Set<EdgeDistributionHeuristics> ALL = Collections
			.unmodifiableSet(EnumSet.allOf(EdgeDistributionHeuristics.class));

	public static final Set<EdgeDistributionHeuristics> USED = Collections
			.unmodifiableSet(EnumSet.of(EdgeDistributionHeuristics.CEIL_FLOOR,
					EdgeDistributionHeuristics.ELEN, EdgeDistributionHeuristics.CIRCULAR,
					EdgeDistributionHeuristics.EAR, EdgeDistributionHeuristics.SLOPE
	// EdgeDistributionHeuristics.CONFLICT
	));

	public static final Set<EdgeDistributionHeuristics> QUICK_TESTING = Collections.unmodifiableSet(
			EnumSet.of(EdgeDistributionHeuristics.CEIL_FLOOR, EdgeDistributionHeuristics.ELEN,
					EdgeDistributionHeuristics.CIRCULAR, EdgeDistributionHeuristics.EAR
	// EdgeDistributionHeuristics.SLOPE
	// EdgeDistributionHeuristics.CONFLICT
	));
	public static final Set<EdgeDistributionHeuristics> QUICK_TESTING2 = Collections
			.unmodifiableSet(EnumSet.of(EdgeDistributionHeuristics.SLOPE,
					EdgeDistributionHeuristics.ELEN, EdgeDistributionHeuristics.CEIL_FLOOR,
					EdgeDistributionHeuristics.CIRCULAR
	// EdgeDistributionHeuristics.EAR
	// EdgeDistributionHeuristics.CONFLICT
	));
	public static final Set<EdgeDistributionHeuristics> QUICK_TESTING3 = Collections
			.unmodifiableSet(EnumSet.of(
					// EdgeDistributionHeuristics.ELEN,
					// EdgeDistributionHeuristics.CEIL_FLOOR,
					// EdgeDistributionHeuristics.CIRCULAR,
					EdgeDistributionHeuristics.EAR));

	public static final Set<EdgeDistributionHeuristics> NONE = Collections
			.unmodifiableSet(EnumSet.of(EdgeDistributionHeuristics.DO_NOTHING));

}
