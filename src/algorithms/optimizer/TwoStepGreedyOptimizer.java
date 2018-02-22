package algorithms.optimizer;

import util.RandomUtil;
import model.Embedding;
import algorithms.optimizer.greedy.GreedyEdgeDistributionAlgorithms;
import algorithms.optimizer.greedy.GreedyVertexOrderAlgorithms;

/**
 * Optimizer which uses greedy algorithms of {@link GreedyVertexOrderAlgorithms}
 * and {@link GreedyEdgeDistributionAlgorithms} alternating to optimize an
 * embedding.
 *
 * @author Jonathan Klawitter
 * @see GreedyOptimizer (which uses the combined greedy optimizer)
 */
@SuppressWarnings("serial")
public class TwoStepGreedyOptimizer extends GreedyOptimizer {

	/** Optimize vertex order/edge distribution. */
	public final static boolean OPTIMIZE = true;

	/** Do NOT Optimize vertex order/edge distribution. */
	public final static boolean DONT_OPTIMIZE = false;

	/** Optimize vertex order/edge distribution exhaustively each round. */
	public final static boolean EXHAUSTIVE = true;

	/** Whether to optimize the vertex order. */
	private final boolean optimizeVertexOrder;

	/** Whether to optimize the edge distribution. */
	private final boolean optimizeEdgeDistribution;

	/** Whether to optimize the vertex order exhaustively. */
	private boolean optimizeVertexOrderExhaustively = false;

	/** Whether to optimize the edge distribution exhaustively. */
	private boolean optimizeEdgeDistributionExhaustively = false;

	/**
	 * Constructor with embedding to optimize and boolean saying what to
	 * optimize.
	 * 
	 * @param embedding
	 * @param optimizeVertexOrder
	 *            whether to optimize the vertex order
	 * @param optimizeEdgeDistribution
	 *            whether to optimize the edge distribution
	 */
	public TwoStepGreedyOptimizer(Embedding embedding, boolean optimizeVertexOrder,
			boolean optimizeEdgeDistribution) {
		super(embedding);
		this.optimizeVertexOrder = optimizeVertexOrder;
		this.optimizeEdgeDistribution = optimizeEdgeDistribution;
		setLocalBestEmbedding(embedding);
		;
	}

	@Override
	protected void optimize() {
		long spineGain = 0;
		long totalSpineGain = 0;

		long distributionGain = 0;
		long totalDistributionGain = 0;

		long iterationGain = 1;
		long currentC = embedding.getNumberOfCrossings();
		long oldC;
		long start = currentC;

		int[] vertexOrder = RandomUtil.randomPermutation(embedding.getN());
		int[] edgesOrder = RandomUtil.randomPermutation(embedding.getM());

		if (inTesting) {
			System.out.println("> start: " + currentC);
		}

		setLocalBestEmbedding(embedding);
		monitoreIteration();

		while (super.shouldIterate(iterationGain)) {

			oldC = currentC;

			// vertex order
			if (optimizeVertexOrder) {
				if (optimizeVertexOrderExhaustively) {
					spineGain = GreedyVertexOrderAlgorithms
							.exhaustiveGreedySpineOptimisationWithOrder(embedding, vertexOrder);
				} else {
					spineGain = GreedyVertexOrderAlgorithms.findBestPositionForVerticesWithOrder(
							embedding, vertexOrder);
				}
				totalSpineGain += spineGain;

				// if (inTesting) System.out.println("--- spine gain: " +
				// spineGain);
			}

			// edge distribution
			if (optimizeEdgeDistribution) {
				if (optimizeEdgeDistributionExhaustively) {
					distributionGain = GreedyEdgeDistributionAlgorithms
							.exhaustiveGreedyEdgeDistributionOptimisationWithOrder(embedding, edgesOrder);
				} else {				
					distributionGain = GreedyEdgeDistributionAlgorithms
							.findBestPageForEdgesWithOrder(embedding, edgesOrder);
				}
				totalDistributionGain += distributionGain;

				// if (inTesting) System.out.println("--- distribution gain: " +
				// distributionGain);

			}

			iterationGain = spineGain + distributionGain;
			spineGain = 0;
			distributionGain = 0;
			if (inTesting) {
				System.out.println("--- round gain: " + distributionGain + "(" + spineGain + "/"
						+ distributionGain + ")");
			}

			if (iterationGain > 0) {
				setLocalBestEmbedding(embedding);
			}

			currentC = embedding.getNumberOfCrossings();
			if (iterationGain != oldC - currentC) {
				throw new IllegalStateException("Miscounted gain.");
			}
			iteration++;
			monitoreIteration();
		}

		super.endTime = System.nanoTime();

		if (inTesting) {
			System.out.println("> start: " + start);
			System.out.println("> end:   " + embedding.getNumberOfCrossings());
			System.out.println("> best:  " + this.getLocalBestEmbedding().getNumberOfCrossings());
			System.out.println("> vertex gains: " + totalSpineGain);
			System.out.println("> edge gains:   " + totalDistributionGain);
		}
	}

	@Override
	public String toString() {
		return "TwoStepGreedyOptimizer(optVO" + getYesOrNo(optimizeVertexOrder) + "/optED"
				+ getYesOrNo(optimizeEdgeDistribution) + ")";
	}

	private String getYesOrNo(boolean bool) {
		return bool ? "+" : "-";
	}

	/**
	 * @param optimizeVertexOrderExhaustively
	 *            the optimizeVertexOrderExhaustively to set
	 */
	public TwoStepGreedyOptimizer setVOOExhaustively(boolean optimizeVertexOrderExhaustively) {
		this.optimizeVertexOrderExhaustively = optimizeVertexOrderExhaustively;
		return this;
	}

	/**
	 * @param optimizeEdgeDistributionExhaustively
	 *            the optimizeEdgeDistributionExhaustively to set
	 */
	public TwoStepGreedyOptimizer setEDOExhaustively(boolean optimizeEdgeDistributionExhaustively) {
		this.optimizeEdgeDistributionExhaustively = optimizeEdgeDistributionExhaustively;
		return this;
	}
}
