package algorithms.optimizer.simluatedAnnealing;

import java.util.Random;

import algorithms.Optimizer;
import algorithms.optimizer.greedy.GreedyEdgeDistributionAlgorithms;
import algorithms.optimizer.greedy.GreedyVertexOrderAlgorithms;
import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;

@SuppressWarnings("serial")
public class SimulatedAnnealingOptimizer extends Optimizer {

	public static final int MAX_ITERATIONS = 980;
	public static final int START_ITERATION = 0;
	public static final int ITERATION_FACTOR = 20;
	public static final double COOLING_LIMIT = 0.2;

	public final Embedding embedding;
	public final double initialTemperature;
	public final int numEdgeMoves;
	public final int numVertexSwaps;
	public final int numVertexMoves;
	public final int numVertexGreedyMoves;

	public SimulatedAnnealingOptimizer(Embedding embedding, double initialTemperature) {
		this.embedding = embedding;
		setLocalBestEmbedding(embedding);
		this.initialTemperature = initialTemperature;
		Graph graph = embedding.getGraph();
		this.numEdgeMoves = graph.getM();
		int n = graph.getN();
		this.numVertexSwaps = n * (int) Math.sqrt(n);
		this.numVertexMoves = n;
		this.numVertexGreedyMoves = n / 4 + 1;
	}

	@Override
	protected void optimize() {
		Random random = new Random();
		Graph graph = embedding.getGraph();
		int n = graph.getN();
		int m = graph.getM();
		int k = embedding.getK();

		long crossings = embedding.getNumberOfCrossings();

		for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
			// System.out.println(iteration + ":" +
			// getLocalBestEmbedding().getNumberOfCrossings());

			double temperature = initialTemperature
					+ (1 / Math.log(ITERATION_FACTOR) - 1 / Math.log(iteration + ITERATION_FACTOR))
							* (COOLING_LIMIT - initialTemperature) / (1 / Math.log(ITERATION_FACTOR)
									- 1 / Math.log(MAX_ITERATIONS + ITERATION_FACTOR));

			// 1) move edges
			for (int i = 0; i < numEdgeMoves; i++) {
				int edgeIndex = random.nextInt(m);
				Edge edge = graph.getEdgeByIndex(edgeIndex);
				long croDiff = -embedding.getNumberOfCrossings();

				int oldPage = embedding.getPageOfEdge(edge);
				int newPage = random.nextInt(k - 1);
				if (newPage >= oldPage) {
					newPage++;
				}
				embedding.moveEdgeToPage(edgeIndex, newPage);

				croDiff += embedding.calculateNumberOfCrossings();

				if ((croDiff > 0) && (random.nextDouble() >= Math.exp(-croDiff / temperature))) {
					embedding.moveEdgeToPage(edgeIndex, oldPage);
				} else {
					crossings += croDiff;
					setLocalBestEmbedding(embedding);
				}
			}

			// 2) swap vertices with neighbors
			for (int i = 0; i < numVertexSwaps; i++) {
				int vertexIndex = random.nextInt(n);
				long croDiff = -embedding.getNumberOfCrossings();

				int oldPosition = embedding.getPositionOnSpine(vertexIndex);
				long gainOfSwap = GreedyVertexOrderAlgorithms
						.computeGainForSwapOfNeighbouringVerticesAt(oldPosition,
								(oldPosition + 1) % n, embedding);

				if ((gainOfSwap >= 0) || (random.nextDouble() < Math.exp(-croDiff / temperature))) {
					int otherIndex = embedding.getVertexAtPosition((oldPosition + 1) % n);
					embedding.swapVertices(vertexIndex, otherIndex);
					crossings -= gainOfSwap;
					setLocalBestEmbedding(embedding);
				}
			}

			// 3) move vertices
			for (int i = 0; i < numVertexMoves; i++) {
				int vertexIndex = random.nextInt(n);
				Vertex vertex = graph.getVertexByIndex(vertexIndex);
				int oldPosition = embedding.getPositionOnSpine(vertexIndex);
				int newPosition = random.nextInt(n);
				if (oldPosition == newPosition) {
					continue;
				}

				long croDiff = -embedding.getNumberOfCrossings();

				int[] distribution = embedding.getDistribution();

				embedding.moveVertexTo(oldPosition, newPosition);
				for (Edge edge : vertex.getEdges()) {
					GreedyEdgeDistributionAlgorithms.findBestPageForEdge(embedding,
							edge.getIndex());
				}
				croDiff += embedding.calculateNumberOfCrossings();

				if ((croDiff > 0) && (random.nextDouble() >= Math.exp(-croDiff / temperature))) {
					// reverse changes
					embedding.moveVertexTo(newPosition, oldPosition);
					embedding.setDistribution(distribution);
				} else {
					crossings += croDiff;
					setLocalBestEmbedding(embedding);
				}
			}

			// 4) move vertices to best position
			for (int i = 0; i < numVertexGreedyMoves; i++) {
				int vertexIndex = random.nextInt(n);
				Vertex vertex = graph.getVertexByIndex(vertexIndex);
				int oldPosition = embedding.getPositionOnSpine(vertexIndex);
				int[] distribution = embedding.getDistribution();
				long croDiff = -embedding.getNumberOfCrossings();

				GreedyVertexOrderAlgorithms.findBestPositionForVertex(embedding, oldPosition);
				for (Edge edge : vertex.getEdges()) {
					GreedyEdgeDistributionAlgorithms.findBestPageForEdge(embedding,
							edge.getIndex());
				}
				croDiff += embedding.calculateNumberOfCrossings();

				if ((croDiff < 0) || (random.nextDouble() < Math.exp(-croDiff / temperature))) {
					// do change
					crossings += croDiff;
					setLocalBestEmbedding(embedding);
				} else {
					// reverse it
					embedding.moveVertexTo(embedding.getPositionOnSpine(vertexIndex), oldPosition);
					embedding.setDistribution(distribution);
				}
			}
		}
	}

}
