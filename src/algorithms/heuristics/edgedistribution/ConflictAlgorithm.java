package algorithms.heuristics.edgedistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import util.RandomUtil;
import model.Edge;
import model.Embedding;
import algorithms.base.EdgeDistributionAlgorithm;

/**
 * Determines all pairs of conflicting edges and places those edges on different
 * pages unless both edges are already placed
 * 
 * @author Matthias Wolf
 * @see Satsangi et al., K-page crossing number minimization problem: An
 *      evaluation of heuristics and its solution using GESAKP
 *
 */
public class ConflictAlgorithm implements EdgeDistributionAlgorithm {

	/**
	 * Computes an edge distribution that is based on calculating the
	 * conflicting edges.
	 */
	@Override
	public void computeEdgeDistribution(Embedding embedding) {
		if (embedding.getK() == 1) {
			System.out.print("!");
			Arrays.fill(embedding.getDistribution(), 0);
			embedding.invalidateNumberOfCrossings();
			// nothing else to do here
			return;
		}
		
		int numberOfPages = embedding.getK();
		System.out.print("k" + numberOfPages + "|");
		int[] distribution = embedding.getDistribution();

		Arrays.fill(distribution, -1);
		List<Conflict> conflicts = determineConflicts(embedding);
		Random random = ThreadLocalRandom.current();
		Collections.shuffle(conflicts, random);
		System.out.print("c" + conflicts.size() + "|");
		
		for (Conflict c : conflicts) {
			Edge e1 = c.getFirstEdge();
			Edge e2 = c.getSecondEdge();
			int page1 = distribution[e1.getIndex()];
			int page2 = distribution[e2.getIndex()];

			if (page1 == -1) {
				page1 = RandomUtil.randomIntUnequalTo(0, numberOfPages, page2, random);
				distribution[e1.getIndex()] = page1;
			}

			if (page2 == -1) {
				page2 = RandomUtil.randomIntUnequalTo(0, numberOfPages, page1, random);
				distribution[e2.getIndex()] = page2;
			}
		}

		for (int i = 0; i < distribution.length; i++) {
			if (distribution[i] == -1)
				distribution[i] = 0;
		}
		
		embedding.invalidateNumberOfCrossings();
		
	}

	/**
	 * Creates a list that contains all conflicts.
	 * 
	 * @param embedding
	 *            the embedding
	 * @return a list of all conflicts.
	 */
	static List<Conflict> determineConflicts(Embedding embedding) {
		List<Conflict> conflicts = new ArrayList<>();
		Edge[] edges = embedding.getGraph().getEdges();

		for (int i = 0; i < edges.length; i++) {
			Edge e1 = edges[i];
			for (int j = i + 1; j < edges.length; j++) {
				Edge e2 = edges[j];
				if (embedding.canEdgesCross(e1.getIndex(), e2.getIndex())) {
					conflicts.add(new Conflict(e1, e2));
				}
			}
		}

		return conflicts;
	}

}
