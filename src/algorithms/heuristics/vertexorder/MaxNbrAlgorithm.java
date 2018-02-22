package algorithms.heuristics.vertexorder;

import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.PartialVertexOrderAlgorithm;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;

import util.RandomUtil;

/**
 * Implementation of the MaxNbr algorithm. As long as there are non-ordered
 * vertices left, the algorithm chooses the vertex v with the highest degree
 * among these vertices and orders it next. Then all non-ordered neighbors of v
 * are ordered in increasing order of their degree.
 * 
 * @author Michael Wegener
 * @see Satsangi, Dharna, Kamal Srivastava, and Gursaran Srivastava. "K-page
 *      crossing number minimization problem: An evaluation of heuristics and
 *      its solution using GESAKP." Memetic Computing 5.4 (2013): 255-274.
 */
public class MaxNbrAlgorithm implements PartialVertexOrderAlgorithm {

	private class IncreasingComparator implements Comparator<Vertex> {
		@Override
		public int compare(Vertex v1, Vertex v2) {
			return v1.getDegree() - v2.getDegree();
		}

	}

	@Override
	public void computeVertexOrder(Embedding embedding) {
		computePartialVertexOrder(embedding, -1);
	}

	@Override
	public void computePartialVertexOrder(Embedding embedding, int keepUpToPosition) {
		computePartialVertexOrder(embedding, keepUpToPosition + 1, embedding.getN());
	}

	@Override
	public void computePartialVertexOrder(Embedding embedding, int begin, int end) {
		if (begin + 1 >= end)
			return; // only one or no element left to order => nothing to do

		Graph g = embedding.getGraph();
		int n = g.getN();
		int[] vertexOnSpine = embedding.getVertexOnSpineArray();
		int[] spine = embedding.getSpine();

		if ((begin + 1 == end) || (begin == end)) {			
			return; // only one element to order => nothing to do
		} else if (begin < end) {
			// we only change the order of vertices spine[begin]..spine[end-1]
			for (int i = begin; i < end; ++i) {
				vertexOnSpine[spine[i]] = -1;
			}		
		} else {
			// we only change the order of vertices spine[begin]..spine[n-1]
			for (int i = begin; i < n; ++i) {
				vertexOnSpine[spine[i]] = -1;
			}		
			// and spine[0]..spine[end-1]
			for (int i = 0; i < end; ++i) {
				vertexOnSpine[spine[i]] = -1;
			}		
		}
		
		
		IncreasingComparator incComparator = new IncreasingComparator();
		PriorityQueue<Vertex> pq = new PriorityQueue<Vertex>(n, new Comparator<Vertex>() {
			@Override
			public int compare(Vertex v1, Vertex v2) {
				return v2.getDegree() - v1.getDegree();
			}
		});

		// add vertices to PQ in decreasing degree order
		Vertex[] vertices = g.getVertices();
		int[] permutation = RandomUtil.randomPermutation(n);
		for (int i = 0; i < n; i++) {
			pq.add(vertices[permutation[i]]);
		}

		boolean[] visited = new boolean[n];
		int idx = begin;
		while (!pq.isEmpty()) {
			Vertex vd = pq.poll();
			int v = vd.getIndex();
			visited[v] = true;

			// only set new index if v belongs to the set of vertices to order
			if (vertexOnSpine[v] == -1) {
				vertexOnSpine[v] = idx;
				idx = (idx + 1) % n; 
			}

			ArrayList<Vertex> neighbors = new ArrayList<Vertex>();
			for (Vertex u : vd.getNeighbors()) {
				// only add neighbors which were not visited yet
				if (!visited[u.getIndex()]) {
					neighbors.add(u);
				}
			}

			Collections.sort(neighbors, incComparator);

			for (Vertex ud : neighbors) {
				int u = ud.getIndex();
				// only set new index if vertex belongs to the set of vertices
				// to order
				if (vertexOnSpine[u] == -1) {
					vertexOnSpine[u] = idx;
					idx = (idx + 1) % n;
				}
				pq.remove(ud);
				visited[u] = true;
			}
		}

		embedding.setVertexOnSpine(vertexOnSpine);
	}

}
