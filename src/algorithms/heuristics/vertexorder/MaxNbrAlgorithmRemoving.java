package algorithms.heuristics.vertexorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.VertexOrderAlgorithm;

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
public class MaxNbrAlgorithmRemoving implements VertexOrderAlgorithm {

	private class IncreasingComparator implements Comparator<Vertex> {
		@Override
		public int compare(Vertex v1, Vertex v2) {
			return degree[v1.getIndex()] - degree[v2.getIndex()];
		}
	}
	
	private class DecreasingComparator implements Comparator<Vertex> {
		@Override
		public int compare(Vertex v1, Vertex v2) {
			return degree[v2.getIndex()] - degree[v1.getIndex()];
		}
	}
	
	private boolean[] visited;
	
	private int[] degree;

	@Override
	public void computeVertexOrder(Embedding embedding) {
		Graph g = embedding.getGraph();
		int n = g.getN();
		if (n <= 1) {
			return;
		}

		int[] vertexOnSpine = embedding.getVertexOnSpineArray();
		int[] spine = embedding.getSpine();
		Vertex[] vertices = g.getVertices();
		
		degree = new int[n];
		for (int i = 0; i < n; i++) {
			vertexOnSpine[spine[i]] = -1;
			degree[i] = vertices[i].getDegree();
		}		
		

		ArrayList<Vertex> verticesToHandle = new ArrayList<Vertex>(n);
		for (int i = 0; i < n; i++) {
			verticesToHandle.add(vertices[i]);
		}
		Collections.sort(verticesToHandle, new DecreasingComparator());
		
		
		IncreasingComparator incComparator = new IncreasingComparator();
		visited = new boolean[n];
		int idx = 0;
		while (!verticesToHandle.isEmpty()) {
			Vertex vd = verticesToHandle.remove(0);			
			int v = vd.getIndex();
			visited[v] = true;
			degree[v] = 0;

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
				verticesToHandle.remove(ud);
				visited[u] = true;
				degree[v] = 0;
				
				for (Vertex neighbour : ud.getNeighbors()) {
					degree[neighbour.getIndex()]--;
					degree[neighbour.getIndex()] = Math.max(degree[neighbour.getIndex()], 0);
				}
			}
			
			Collections.sort(verticesToHandle, new DecreasingComparator());
		}

		embedding.setVertexOnSpine(vertexOnSpine);
	}

}
