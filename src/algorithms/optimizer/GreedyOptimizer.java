package algorithms.optimizer;

import util.RandomUtil;
import model.Embedding;
import algorithms.Optimizer;
import algorithms.optimizer.greedy.GreedyCombinedAlgorithms;

/**
 * Optimizer which uses greedy algorithms of {@link GreedyCombinedAlgorithms} to
 * optimize an embedding.
 * 
 * @author Jonathan Klawitter
 * @see TwoStepGreedyOptimizer (which uses greedy optimizer for VO and ED alternatingly)
 */
@SuppressWarnings("serial")
public class GreedyOptimizer extends Optimizer {

	// (60e9) = minute
	private final static long MAX_TIME = (long) (15 * 60 * 1000);

	protected Embedding embedding;

	protected boolean inTesting = false;

	public GreedyOptimizer(Embedding embedding) {
		this.embedding = embedding;
	}

	@Override
	protected void optimize() {
		long iterationGain = 1;
		long startC = embedding.getNumberOfCrossings();

		int[] vertexOrder = RandomUtil.randomPermutation(embedding.getN());

		if (inTesting) {
			System.out.println("> start: " + startC);
		}

		setLocalBestEmbedding(embedding);
		initialMonitoring();
		
		while (shouldIterate(iterationGain)) {
			iterationGain = GreedyCombinedAlgorithms.optimiseAllVerticesWithOrder(embedding,
					vertexOrder);

			if (inTesting) {
				System.out.println("--- round gain: " + iterationGain);
			}

			if (iterationGain > 0) {
				setLocalBestEmbedding(embedding);
			}
			
			iteration++;
			monitoreIteration();
		}

		super.endTime = System.nanoTime();

		if (inTesting) {
			System.out.println("> start: " + startC);
			System.out.println("> end:   " + embedding.getNumberOfCrossings());
			System.out.println("> best:  " + this.getLocalBestEmbedding().getNumberOfCrossings());
			System.out.println("> total gain: " + (startC - embedding.getNumberOfCrossings()));
		}

	}

	/**
	 * Returns whether the optimizer should do further iterations.
	 * 
	 * @return whether the optimizer should do further iterations
	 */
	protected boolean shouldIterate(long iterationGain) {
		// no progress
		if (iterationGain <= 0) {
			return false;
		}

		// best solution found
		if (embedding.getNumberOfCrossings() == targetNumberOfCrossings) {
			return false;
		} else if (embedding.getNumberOfCrossings() < targetNumberOfCrossings) {
			throw new IllegalStateException("To few crossings: " + embedding.getNumberOfCrossings()
					+ "<" + targetNumberOfCrossings + "\n" + embedding);
		}

		// stop if to much time elapsed
		if ((System.currentTimeMillis() - super.startTime) > MAX_TIME) {
			System.out.println("> stopping - time limit reached");
			return false;
		}

		return true;
	}
	
	@Override
	public String toString() {
		return "GreedyOptimizer";
	}

}
