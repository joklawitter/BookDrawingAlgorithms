package algorithms.heuristics;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import algorithms.base.PartialVertexOrderAlgorithm;
import algorithms.base.VertexOrderAlgorithm;
import algorithms.heuristics.vertexorder.MaxNbrAlgorithm;
import algorithms.heuristics.vertexorder.RandomDFSAlgorithm;
import algorithms.heuristics.vertexorder.SmallestDegreeDFSAlgorithm;


/**
 * This enum contains all vertex order heuristics.
 * The name of the algorithm can be retrieved by calling {@link #toString()}.
 * @author Matthias
 *
 */
public enum PartialVertexOrderHeuristics {
//	CON_NEXT_CROSSINGS(new ConnectivityAlgorithm(VertexSelector.NEXT, VertexPlacer.CROSSINGS)),
//	CON_NEXT_ELEN(new ConnectivityAlgorithm(VertexSelector.NEXT, VertexPlacer.ELEN)),
//	CON_NEXT_FIXED(new ConnectivityAlgorithm(VertexSelector.NEXT, VertexPlacer.FIXED)),
//	CON_NEXT_RANDOM(new ConnectivityAlgorithm(VertexSelector.NEXT, VertexPlacer.RANDOM)),
//	CON_CON_CROSSINGS(new ConnectivityAlgorithm(VertexSelector.CONNECTIVITY, VertexPlacer.CROSSINGS)),
//	CON_CON_ELEN(new ConnectivityAlgorithm(VertexSelector.CONNECTIVITY, VertexPlacer.ELEN)),
//	CON_CON_FIXED(new ConnectivityAlgorithm(VertexSelector.CONNECTIVITY, VertexPlacer.FIXED)),
//	CON_CON_RANDOM(new ConnectivityAlgorithm(VertexSelector.CONNECTIVITY, VertexPlacer.RANDOM)),
//	CON_INCON_CROSSINGS(new ConnectivityAlgorithm(VertexSelector.INCON, VertexPlacer.CROSSINGS)),
//	CON_INCON_ELEN(new ConnectivityAlgorithm(VertexSelector.INCON, VertexPlacer.ELEN)),
//	CON_INCON_FIXED(new ConnectivityAlgorithm(VertexSelector.INCON, VertexPlacer.FIXED)),
//	CON_INCON_RANDOM(new ConnectivityAlgorithm(VertexSelector.INCON, VertexPlacer.RANDOM)),
//	CON_OUTCON_CROSSINGS(new ConnectivityAlgorithm(VertexSelector.OUTCON, VertexPlacer.CROSSINGS)),
//	CON_OUTCON_ELEN(new ConnectivityAlgorithm(VertexSelector.OUTCON, VertexPlacer.ELEN)),
//	CON_OUTCON_FIXED(new ConnectivityAlgorithm(VertexSelector.OUTCON, VertexPlacer.FIXED)),
//	CON_OUTCON_RANDOM(new ConnectivityAlgorithm(VertexSelector.OUTCON, VertexPlacer.RANDOM)),
//	CON_RANDOM_CROSSINGS(new ConnectivityAlgorithm(VertexSelector.RANDOM, VertexPlacer.CROSSINGS)),
//	CON_RANDOM_ELEN(new ConnectivityAlgorithm(VertexSelector.RANDOM, VertexPlacer.ELEN)),
//	CON_RANDOM_FIXED(new ConnectivityAlgorithm(VertexSelector.RANDOM, VertexPlacer.FIXED)),
//	CON_RANDOM_RANDOM(new ConnectivityAlgorithm(VertexSelector.RANDOM, VertexPlacer.RANDOM)),
//	IDFS(new IdfsAlgorithm()),
	RDFS(new RandomDFSAlgorithm()),
	SMALLEST_DEGREE_DFS(new SmallestDegreeDFSAlgorithm()),
//	RBFS(new RandomBFSAlgorithm()),
	MAX_NBR(new MaxNbrAlgorithm())
//	RANDOM(new RandomVertexOrderAlgorithm())
	;
	
	private final PartialVertexOrderAlgorithm algorithm;
	
	private final String name;
	
	private PartialVertexOrderHeuristics(PartialVertexOrderAlgorithm algorithm) {
		this.algorithm = algorithm;
		name = this.name().toLowerCase().replace("_", "-");
	}
	
	public VertexOrderAlgorithm getAlgorithm() {
		return algorithm;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * A set of all heuristics in this enum.
	 */
	public static final Set<PartialVertexOrderHeuristics> ALL = Collections.unmodifiableSet(EnumSet.allOf(PartialVertexOrderHeuristics.class));
		

}
