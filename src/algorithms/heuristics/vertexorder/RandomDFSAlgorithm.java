package algorithms.heuristics.vertexorder;

import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.PartialVertexOrderAlgorithm;

import java.util.Stack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of the RDFS vertex ordering algorithm. The graph is traversed
 * in depth first order and vertices are ordered accordingly. The root vertex is
 * chosen uniformly at random as is the order in which the neighbors of the
 * current vertex are visited.
 * 
 * @author Michael Wegner, Matthias Wolf, Jonathan Klawitter
 * @see Satsangi, Dharna, Kamal Srivastava, and Gursaran Srivastava.
 *      "K-page crossing number minimization problem: An evaluation of heuristics and its solution using GESAKP."
 *      Memetic Computing 5.4 (2013): 255-274.
 *
 */
public class RandomDFSAlgorithm implements PartialVertexOrderAlgorithm {

	@Override
	public void computeVertexOrder(Embedding embedding) {
		computePartialVertexOrder(embedding, 0, embedding.getGraph().getN());
	}

	@Override
	public void computePartialVertexOrder(Embedding embedding, int keepUpToPosition) {
		computePartialVertexOrder(embedding, keepUpToPosition + 1, embedding.getGraph().getN());
	}
	
	@Override
	public void computePartialVertexOrder(Embedding embedding, int begin, int end) {
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

		// get start position and index
		ThreadLocalRandom rand = ThreadLocalRandom.current();
		int rootPosition;
		if (begin < end) {			
			rootPosition = begin + rand.nextInt(end - begin);
		} else {
			int length = n - begin + end;
			rootPosition = (begin + rand.nextInt(length)) % n;			
		}
		int rootIndex = spine[rootPosition];

		Stack<Integer> stack = new Stack<Integer>();
		boolean[] visited = new boolean[n];

		int idx = begin;
		// continue if not reached end / still after begin without flip
		while ((idx < end) || (end < begin && begin <= idx)) {
			// graph may not be connected -> find not visited vertex
			while (vertexOnSpine[rootIndex] != -1) {
				rootPosition = (rootPosition + 1) % n;
				rootIndex = spine[rootPosition];
			}

			stack.push(rootIndex);
			while (!stack.empty()) {
				int v = stack.pop();
				if (!visited[v]) {
					visited[v] = true;

					if (vertexOnSpine[v] == -1) {
						// only set new index if vertex belongs to the set of
						// vertices to order
						vertexOnSpine[v] = idx;
						idx = (idx + 1);
						if (begin > end) {
							idx = idx % n;
						}
					}

					ArrayList<Integer> neighbors = new ArrayList<Integer>();
					for (Vertex u : g.getVertexByIndex(v).getNeighbors()) {
						if (!visited[u.getIndex()]) {
							neighbors.add(u.getIndex());
						}
					}
					Collections.shuffle(neighbors);

					for (int u : neighbors) {
						stack.push(u);
					}
				}
			}
		}

		embedding.setVertexOnSpine(vertexOnSpine);

	}

}
