package algorithms.heuristics.edgedistribution;

import java.util.Arrays;

import algorithms.base.EdgeDistributionAlgorithm;
import model.Edge;
import model.Embedding;

/**
 * Assigns the edges to the pages such that edge with similar slope are on the
 * same page. The slope of an edge is determined by drawing the nodes on a
 * circle and then measuring the slope of the straight line between the two end
 * points of the edge.
 * 
 * @author Matthias Wolf, Jonathan Klawitter
 */
public class SlopeAlgorithm implements EdgeDistributionAlgorithm {

	private int[] angleToPageMap;

	@Override
	public void computeEdgeDistribution(Embedding embedding) {
		if (embedding.getK() == 1) {
			Arrays.fill(embedding.getDistribution(), 0);
			embedding.invalidateNumberOfCrossings();
			// nothing else to do here
			return;
		}

		initAngleToPageMap(embedding.getN(), embedding.getK());

		int[] distribution = embedding.getDistribution();
		// double sectionSize = Math.PI / embedding.getK();

		for (Edge edge : embedding.getGraph().getEdges()) {
			int angl = calculateAngleInt(embedding, edge);
			int page = getPageForAngle(angl);

			// double angle = calculateAngle(embedding, edge);
			// int page = (int) (angle / sectionSize);
			// if (page == embedding.getK()) {
			// page = 0;
			// }
			distribution[edge.getIndex()] = page;

			// System.out.println(edge + ": page = " + page + ", angle = " +
			// angle);
		}

		embedding.invalidateNumberOfCrossings();
	}

	private void initAngleToPageMap(int n, int k) {
		double epsilon = 0.000000001;
		angleToPageMap = new int[n];
		double sectionSize = ((double) n) / k + epsilon;
		double currentSize = sectionSize;
		int currentPage = 0;
		for (int i = 1; i <= angleToPageMap.length; i++) {
			if (i < currentSize) {
				angleToPageMap[i - 1] = currentPage;
			} else {
				angleToPageMap[i - 1] = currentPage + 1;
				currentPage++;
				currentSize += sectionSize;
			}
		}
	}

	private int getPageForAngle(int angle) {
		return angleToPageMap[angle - 1];
	}

	/**
	 * Calculates the angle of the edge for the vertex order given in
	 * {@code embedding}. The results will be between {@code 0} (inclusive) and
	 * {@code Math.PI} (exclusive).
	 * 
	 * @param embedding
	 *            the embedding describing the vertex order
	 * @param edge
	 *            the edge whose angle shall be calculated
	 * @return the angle of the edge to the positive x-axis.
	 * @author Matthias Wolf
	 */
	@Deprecated
	private static double calculateAngle(Embedding embedding, Edge edge) {
		double increment = 2 * Math.PI / embedding.getGraph().getN();
		int vPos = embedding.getPositionOnSpine(edge.getStart().getIndex());
		int wPos = embedding.getPositionOnSpine(edge.getTarget().getIndex());

		double dy = Math.sin(vPos * increment) - Math.sin(wPos * increment);
		double dx = Math.cos(vPos * increment) - Math.cos(wPos * increment);

		double angle = Math.atan2(dy, dx);
		if (angle >= Math.PI)
			angle -= Math.PI;
		return angle >= 0 ? angle : Math.PI + angle;
	}

	/**
	 * Calculates the angle of the edge for the vertex order given in
	 * {@code embedding}. The result will be between 1 and n.
	 * 
	 * @param embedding
	 *            the embedding describing the vertex order
	 * @param edge
	 *            the edge whose angle shall be calculated
	 * @return the angle of the given edge
	 * @author Jonathan Klawitter
	 */
	private static int calculateAngleInt(Embedding embedding, Edge edge) {
		int x = embedding.getPositionOfSmallerEndpoint(edge);
		int y = embedding.getPositionOfLargerEndpoint(edge);

		// long version:
		// int length = y - x;
		// int angle = x * 2 + length;

		// short version:
		int angle = x + y;
		return (angle > embedding.getN()) ? angle - embedding.getN() : angle;
	}

}
