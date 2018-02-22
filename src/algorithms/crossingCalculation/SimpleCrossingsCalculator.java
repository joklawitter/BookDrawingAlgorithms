package algorithms.crossingCalculation;

import model.Embedding;
import model.Graph;

public class SimpleCrossingsCalculator implements ICrossingCalculator {
	
	public SimpleCrossingsCalculator( ) {
	}

	/**
	 * Calculates the crossing number and stores it to crossings.
	 * 
	 * @return the crossing-number
	 */
	public long calculateNumberOfCrossings(Embedding embedding) {
		long sum = 0;
		Graph g = embedding.getGraph();
		int[] distribution = embedding.getDistribution();
		for (int i = 0; i < g.getM(); i++) {
			for (int j = i + 1; j < g.getM(); j++) {
				if (distribution[i] == distribution[j]) {
					sum += embedding.canEdgesCross(i, j) ? 1 : 0;
				}
			}
		}		
		
		embedding.setNumberOfCrossings(sum);
		return sum;
	}

	/**
	 * Calculates all crossings of page k. O(n^2)
	 * 
	 * @param pageIndex
	 *            the page
	 * @return the amount of crossings of page k
	 */
	public long calculateNumberOfCrossingsOnPage(Embedding e, int pageIndex) {
		long sum = 0;
		Graph g = e.getGraph();
		for (int i = 0; i < g.getM(); i++) {
			if (e.getDistribution()[i] == pageIndex) {
				for (int j = i + 1; j < g.getM(); j++) {
					if (e.getDistribution()[i] == e.getDistribution()[j]) {
						sum += e.canEdgesCross(i, j) ? 1 : 0;
					}
				}
			}
		}
		return sum;
	}
	
	/**
	 * Calculates the amount of crossings of edge e of his current page.
	 * 
	 * @param edgeIndex
	 *            the edge
	 * @return the amount of crossing of edge e at page k
	 */
	public int calcCrossingsOfEdge(Embedding embedding, int edgeIndex) {
		return calcCrossingsOfEdge(embedding, edgeIndex, embedding.getDistribution()[edgeIndex]);
	}
	
	/**
	 * Calculates the amount of crossings for edge e at page k.
	 * 
	 * @param e
	 *            the index of the edge
	 * @param k
	 *            the index of the page
	 * @return the amount of crossings.
	 */
	public int calcCrossingsOfEdge(Embedding em, int e, int k) {
		int sum = 0;
		for (int i = 0; i < em.getDistribution().length; i++) {
			if (em.getDistribution()[i] == k) {
				sum += em.canEdgesCross(e, i) ? 1 : 0;
			}
		}
		return sum;
	}
	
	/**
	 * Returns whether given edge e produces a crossings on its page.
	 * 
	 * @param edge
	 *            the edge tested for crossings
	 * @return returns whether given edge e produces a crossings on its page
	 */
	public boolean doesEdgeCross(Embedding em, int e) {		
		int k = em.getDistribution()[e];
		Graph g = em.getGraph();
		for (int i = 0; i < g.getM(); i++) {
			if (i != e && em.getDistribution()[i] == k && em.canEdgesCross(e, i))
				return true;
		}
		return false;
	}
	
	/**
	 * Calculates the gain of crossings moving vertex v to position p
	 * 
	 * @param v
	 *            the vertex
	 * @param p
	 *            the page
	 * @return
	 */
	@Deprecated
	public long gainOfMoveVertexToPosition(Embedding e, int v, int p) {
		// TODO CH - think of better solution
		// naiv:
		/*
		 * store actual crossing number and position move to position p
		 * calculate crossing number move to old position => O(n^2)
		 */
		long oldC = 0;
		if (!e.isNumberOfCrossingsValid())
			oldC = calculateNumberOfCrossings(e);
		else 
		  oldC = e.getNumberOfCrossings();
		
		int oldP = e.getPositionOnSpine(v);
		
		e.moveVertexTo(oldP, p);
		long newC = calculateNumberOfCrossings(e);
		e.moveVertexTo(p, oldP);
		e.setNumberOfCrossings(oldC);
//		Graph g = e.getGraph();		
//		int maxPages = e.getMaxPages(),
//				N = e.getGraph().getN();		
//		long sum = 0, crossings = e.getNumberOfCrossings();
//		for (int k = 0; k < maxPages; k++) {
//			for (int i = 0; i < N; i++) {
//				for (int j = 0; j < N; j++) {
//					int u = g.getTargetVertexOfEdge(i), __v = g.getSourceVertexOfEdge(i), x = g.getTargetVertexOfEdge(j), y = g.getSourceVertexOfEdge(j);
//
//					sum += wouldEdgesCross(e, u, __v, x, y, v, p) ? 1 : 0;
//				}
//			}
//		}
		return oldC - newC;
	}
	
	/**
	 * Calculates the gain of the amount of crossings if the edge e moves to
	 * page k
	 * 
	 * @param e
	 *            the edge
	 * @param k
	 *            the page
	 * @return the gain of amount of crossings
	 */
	public int gainOfMoveEdgeToPage(Embedding em, int e, int k) {
		int oldCrossings = calcCrossingsOfEdge(em, e);
		int newCrossings = calcCrossingsOfEdge(em, e, k);
		return oldCrossings - newCrossings;
	}

	/**
	 * Calculates the gain of crossings swapping the positions of vertex u and
	 * vertex v. Doesn't change the embedding.
	 * 
	 * @param u
	 *            the first vertex to swap
	 * @param v
	 *            the second vertex to swap
	 * @return the gain
	 */
	public long gainOfSwapVertices(Embedding e, int u, int v) {
		long oldC = e.isNumberOfCrossingsValid() ? e.getNumberOfCrossings() : calculateNumberOfCrossings(e);
		e.swapVertices(u, v);
		long newC = calculateNumberOfCrossings(e);
		e.swapVertices(v, u);
		e.setNumberOfCrossings(oldC);
		return oldC - newC;
	}
	
	/**
	 * Helper for function gainOfMoveVertexToPosition.
	 * 
	 * @param u
	 * @param v
	 * @param x
	 * @param y
	 * @param t
	 * @param p
	 * @return
	 */
	protected boolean wouldEdgesCross(Embedding e, int u, int v, int x, int y, int t, int p) {
		if (u == t || v == t) {
			if (p == x || p == y) {
				return Embedding.canEdgesCross(p - 1, v, x, y);
			} else {
				return Embedding.canEdgesCross(p, v, x, y);
			}
		} else if (v == t) {
			if (p == x || p == y) {
				return Embedding.canEdgesCross(u, p - 1, x, y);
			} else {
				return Embedding.canEdgesCross(u, v, x, y);
			}
		} else if (x == t) {
			if (p == x || p == y) {
				return Embedding.canEdgesCross(u, v, p - 1, y);
			} else {
				return Embedding.canEdgesCross(u, v, x, y);
			}
		} else if (y == t) {
			if (p == x || p == y) {
				return Embedding.canEdgesCross(u, v, x, p - 1);
			} else {
				return Embedding.canEdgesCross(u, v, x, y);
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "SimpleCrossingsCalculator";
	}
}
