package algorithms;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

import model.Embedding;

@SuppressWarnings("serial")
public abstract class Optimizer extends RecursiveTask<Embedding> {

	/**
	 * Class to store solutions with additional data like the iteration and time
	 * it was found.
	 */
	public class SolutionInformation {

		/**
		 * Contains the actual solution
		 */
		private final Embedding embedding;

		/**
		 * Number of iterations it took to found the solution
		 */
		protected final int iteration;

		/**
		 * Start time of optimization
		 */
		private final long startTime;

		/**
		 * Time when the solution was found
		 */
		private final long time;

		public SolutionInformation(final Embedding embedding) {
			time = System.currentTimeMillis();
			iteration = Optimizer.this.iteration;
			startTime = Optimizer.this.startTime;
			this.embedding = new Embedding(embedding);

			// System.out.println(embedding.getNumberOfCrossings() +
			// " iteration(" + iteration
			// + ") new solution");
		}

		/**
		 * Returns the embedding representing the actual solution.
		 * 
		 * @return
		 */
		public Embedding getEmbedding() {
			return embedding;
		}

		/**
		 * Returns the number of iterations performed in the optimizer until
		 * this solution has been found.
		 * 
		 * @return
		 */
		public final int getNumberOfIterations() {
			return iteration;
		}

		public long getStartTime() {
			if (iteration == -1) {
				return Optimizer.this.startTime;
			}
			return startTime;
		}

		/**
		 * Returns the elapsed time measured from the start of the optimizer
		 * until this solution has been found.
		 * 
		 * @return
		 */
		public Duration getElapsedTime() {
			return Duration.ofMillis(time).minus(Duration.ofMillis(startTime));
		}

		public long getNumberOfCrossings() {
			return embedding.getNumberOfCrossings();
		}

		/**
		 * Returns {@code true} if and only if this solution is at least as good
		 * as the given embedding.
		 * 
		 * @param embedding
		 *            embedding to compare with this solution
		 * @return whether the this solution is at least as good as the given
		 *         embedding
		 */
		public boolean isAtLeastAsGoodAs(final Embedding embedding) {
			return getNumberOfCrossings() <= embedding.getNumberOfCrossings();
		}

		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			result.append("Solution: ");
			result.append("crossings: ");
			result.append(getNumberOfCrossings());
			if (iteration != -1) {
				result.append(", ");
				result.append("iteration: ");
				result.append(getNumberOfIterations());
				result.append(", ");
				result.append("elapsed time: ");
				result.append(getElapsedTime());
			} else {
				result.append(" (from initial population)");
			}
			return result.toString();
		}

	}

	/** Contains the best result so far. */
	private volatile SolutionInformation localBestSolution = null;

	/** The number of crossings to achieve or 0 if unknown. */
	protected long targetNumberOfCrossings = 0;

	/** Current number of iterations performed. */
	protected int iteration = 0;

	/** Start time of optimization. */
	protected long startTime;

	/** End time of optimization. */
	protected long endTime;

	/** Number of intervals after which to monitor result. */
	protected int monitoringIntervals = 1;

	/** Milliseconds after which to monitor result. */
	protected long monitoringTime = 1000;

	private long lastMonitoring = 0;

	private long lastSubmittedBestCrossing;

	/** Monitored number of crossings. */
	protected ArrayList<Long> monitoredCrossings = new ArrayList<Long>();

	/** Monitored elapsed time. */
	protected ArrayList<Long> monitoredTime = new ArrayList<Long>();

	/** Monitored iteration. */
	protected ArrayList<Long> monitoredIteration = new ArrayList<Long>();

	/** Monitored current best crossing. */
	protected ArrayList<Long> monitoredCurrentBest = new ArrayList<Long>();

	public Optimizer() {
		super();
		startTime = System.currentTimeMillis();
	}

	/**
	 * @param crossingsToReach
	 *            number of crossings to achieve
	 */
	public void setTargetNumberOfCrossings(long crossingsToReach) {
		this.targetNumberOfCrossings = crossingsToReach;
	}

	/**
	 * Returns the best result so far obtained by the optimizer.
	 * 
	 * If the local best embedding changes, a new instance is returned. Thus, an
	 * already returned instance does not reflect the changes. This behavior
	 * ensures thread safety.
	 * 
	 * @see #getLocalBestSolution()
	 * 
	 * @return
	 */
	public final Embedding getLocalBestEmbedding() {
		return localBestSolution.getEmbedding();
	}

	/**
	 * Returns the best result so far obtained by the optimizer.
	 * 
	 * In addition to {@link #getLocalBestEmbedding()} the returned object
	 * contains information about the time and the number of iterations it took
	 * to find the embedding.
	 * 
	 * If the local best embedding changes, a new instance is returned. Thus, an
	 * already returned instance does not reflect the changes. This behavior
	 * ensures thread safety.
	 * 
	 * @return
	 */
	public final SolutionInformation getLocalBestSolution() {
		return localBestSolution;
	}

	/**
	 * Return the number of iterations performed in the optimizer.
	 * 
	 * During execution the returned value might be below the actual value.
	 * 
	 * @return
	 */
	public final int getNumberOfInterations() {
		return iteration;
	}

	/**
	 * Returns the elapsed time for the whole optimization.
	 * 
	 * @return elapsed time for whole optimization
	 */
	public long getElapsedTime() {
		return endTime - startTime;
	}

	/**
	 * Returns the elapsed time for the whole optimization.
	 * 
	 * @return elapsed time for whole optimization
	 */
	public Duration getElapsedDuration() {
		return Duration.ofMillis(endTime).minus(Duration.ofMillis(startTime));
	}

	/**
	 * Returns the current elapsed time during optimization.
	 * 
	 * @return current elapsed time during optimization
	 */
	public Duration getCurrentElapsedTime() {
		return Duration.ofMillis(System.currentTimeMillis()).minus(Duration.ofMillis(startTime));
	}

	public ArrayList<Long> getMonitoredCrossings() {
		return monitoredCrossings;
	}

	public ArrayList<Long> getMonitoredTime() {
		return monitoredTime;
	}

	public ArrayList<Long> getMonitoredCurrentBest() {
		return monitoredCurrentBest;
	}

	public ArrayList<Long> getMonitoredIteration() {
		return monitoredIteration;
	}

	public void setMonitoringIntervals(int monitoringIntervals) {
		this.monitoringIntervals = monitoringIntervals;
	}

	public void setMonitoringTime(long monitoringTime) {
		this.monitoringTime = monitoringTime;
	}

	protected final void monitoreTime() {
		if (monitoringTime <= 0) {
			return;
		}

		long elapsed = System.currentTimeMillis() - startTime;

		if (elapsed - lastMonitoring >= monitoringTime) {
			do {
				setMonitoredValues();
			} while (elapsed - lastMonitoring >= monitoringTime);
		}

	}

	public void setMonitoredValues() {
		monitoredCrossings.add(localBestSolution.getNumberOfCrossings());
		monitoredTime.add(getCurrentElapsedTime().toMillis());
		monitoredCurrentBest.add(lastSubmittedBestCrossing);
		monitoredIteration.add((long) iteration);
		lastMonitoring += monitoringTime;
	}

	protected final void monitoreIteration() {
		if (monitoringIntervals <= 0) {
			return;
		}

		if (iteration % monitoringIntervals == 0) {
			setMonitoredValues();
		}
	}

	protected final void initialMonitoring() {
		monitoredCrossings.add(localBestSolution.getNumberOfCrossings());
		monitoredTime.add(getCurrentElapsedTime().toMillis());
		monitoredCurrentBest.add(localBestSolution.getNumberOfCrossings());
		monitoredIteration.add(0L);
	}

	public int getIterations() {
		return iteration;
	}

	/**
	 * Creates a deep copy of the given embedding and stores it as the current
	 * local best embedding.
	 * 
	 * A deep copy of the embedding is created to allow later modifications of
	 * the given embedding in the optimizer without affecting the local best
	 * embedding.
	 * 
	 * The instance previously stored in {@code localBestEmbedding} is not
	 * reused to ensure thread safety.
	 * 
	 * @param embedding
	 */
	protected final void setLocalBestEmbedding(Embedding embedding) {
		lastSubmittedBestCrossing = embedding.getNumberOfCrossings();

		if (localBestSolution != null && localBestSolution.isAtLeastAsGoodAs(embedding)) {
			// "New embedding (found in iteration {}) is not better than current
			// local best embedding (found in iteration {})",
			// iteration, localBestSolution.getNumberOfIterations());
			return;
		}
		localBestSolution = new SolutionInformation(embedding);
	}

	/**
	 * This method performs the actual optimization.
	 * 
	 * The implementation of this method should set the local best embedding at
	 * least at the end of the optimization.
	 * 
	 * @see #setLocalBestEmbedding
	 * 
	 */
	protected abstract void optimize();

	@Override
	public final Embedding compute() {
		iteration = 0;
		startTime = System.currentTimeMillis();
		optimize();
		endTime = System.currentTimeMillis();
		return localBestSolution.getEmbedding();
	}

	@Override
	public void reinitialize() {
		super.reinitialize();
		iteration = -1;
	}
}
