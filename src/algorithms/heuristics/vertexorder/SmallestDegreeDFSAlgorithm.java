package algorithms.heuristics.vertexorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.PartialVertexOrderAlgorithm;

/**
 * Implementation of the DFS vertex ordering algorithm. The graph is traversed in depth first order
 * and vertices are ordered accordingly. The root vertex is chosen by degree (smallest) as is the order
 * in which the neighbours of the current vertex are visited.
 * 
 * @author Jonathan Klawitter, (based on Michael Wegner's RDFS)
 */
public class SmallestDegreeDFSAlgorithm implements PartialVertexOrderAlgorithm {

	@Override
	public void computeVertexOrder(Embedding embedding) {
		computePartialVertexOrder(embedding, 0, embedding.getGraph().getN());		
	}

	@Override
	public void computePartialVertexOrder(Embedding embedding, int keepUpToPosition) {
		computePartialVertexOrder(embedding, keepUpToPosition+1, embedding.getGraph().getN());		
	}

	@Override
	public void computePartialVertexOrder(Embedding embedding, int begin, int end) {
		Graph g = embedding.getGraph();
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
			for (int i = begin; i < g.getN(); ++i) {
				vertexOnSpine[spine[i]] = -1;
			}		
			// and spine[0]..spine[end-1]
			for (int i = 0; i < end; ++i) {
				vertexOnSpine[spine[i]] = -1;
			}		
	
		}

		int rootPosition = findPositionOfVertexWithSmallestDegree(embedding, begin, end);
		int rootIndex = spine[rootPosition];

		Stack<Integer> stack = new Stack<Integer>();
		boolean[] visited = new boolean[embedding.getN()];		

		int idx = begin;
		// continue if not reached end / still after begin without flip
		while ((idx < end) || (end < begin && begin <= idx)) {
			// graph may not be connected -> find not visited vertex
			while (vertexOnSpine[rootIndex] != -1) {
				rootPosition = (rootPosition + 1) % embedding.getN();
				rootIndex = spine[rootPosition];
			}
			
			// order connected component
			stack.push(rootIndex);
			while (!stack.empty()) {
				int v = stack.pop();
				if (!visited[v]) {
					visited[v] = true;

					// only set new index if vertex belongs to the set of vertices to order
					if (vertexOnSpine[v] == -1) { 					
						vertexOnSpine[v] = idx++;
						if (begin > end) {
							idx = idx % g.getN();
						}
					}

					ArrayList<Vertex> neighbors = new ArrayList<Vertex>();					
					for (Vertex u : g.getVertexByIndex(v).getNeighbors()) {
						if (!visited[u.getIndex()]) {
							neighbors.add(u);
						}
					}
					// sorts by degree descending, thus y and x are swapped
					Collections.sort(neighbors, (Vertex x, Vertex y) -> Integer.compare(y.getDegree(), x.getDegree()));

					for (Vertex u : neighbors) {					
						stack.push(u.getIndex());					
					}
				}
			}
		}

		embedding.setVertexOnSpine(vertexOnSpine);	

	}

	private int findPositionOfVertexWithSmallestDegree(Embedding embedding, int begin, int end) {
		Graph g = embedding.getGraph();
		int[] spine = embedding.getSpine();
		
		int smallestPosition = begin;
		int smallestDegree = Integer.MAX_VALUE;
		if (begin < end) {			
			for (int i = begin; i < end; i++) {
				int currentDegree = g.getDegreeOf(spine[i]);
				if (currentDegree < smallestDegree) {
					smallestDegree = currentDegree;
					smallestPosition = i;
				}
			}
		} else {
			for (int i = begin; i < embedding.getN(); i++) {
				int currentDegree = g.getDegreeOf(spine[i]);
				if (currentDegree < smallestDegree) {
					smallestDegree = currentDegree;
					smallestPosition = i;
				}
			}
			for (int i = 0; i < end; i++) {
				int currentDegree = g.getDegreeOf(spine[i]);
				if (currentDegree < smallestDegree) {
					smallestDegree = currentDegree;
					smallestPosition = i;
				}
			}
		}
		
		return smallestPosition;
	}

}
