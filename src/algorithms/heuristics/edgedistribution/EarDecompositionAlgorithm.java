package algorithms.heuristics.edgedistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import algorithms.base.EdgeDistributionAlgorithm;
import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;
import util.ConflictGraphFactory;

public class EarDecompositionAlgorithm implements EdgeDistributionAlgorithm {

	@Override
	public void computeEdgeDistribution(Embedding embedding) {
		int[] distribution = embedding.getDistribution();
		if (embedding.getK() == 1) {
			Arrays.fill(distribution, 0);
			embedding.setDistribution(distribution);
			return;
		} else {
			Arrays.fill(distribution, -1);
		}

		// creating conflict graph O(m^2)
		Graph conflictGraph = ConflictGraphFactory.createConflictGraph(embedding);

		boolean[] visited = new boolean[conflictGraph.getN()];
		boolean[] treeNode = new boolean[conflictGraph.getN()];
		boolean[] edgeVisited = new boolean[conflictGraph.getM()];
		Direction[] direction = new Direction[conflictGraph.getM()];
		Edge[] currentDfsEdge = new Edge[conflictGraph.getN()];
		Edge[] parent = new Edge[conflictGraph.getN()];
		ArrayList<ArrayList<Edge>> dependencies = new ArrayList<ArrayList<Edge>>();
		for (int i = 0; i < conflictGraph.getM(); ++i) {
			dependencies.add(new ArrayList<Edge>());
		}

		ArrayList<ArrayList<Vertex>> paths = new ArrayList<ArrayList<Vertex>>();

		int connectedNodes = 0;
		for (int i = 0; i < conflictGraph.getN(); ++i) { // O(m)
			if (conflictGraph.getVertexByIndex(i).getDegree() > 0)
				connectedNodes++;
		}

		int numVisited = 0;
		while (numVisited < connectedNodes) { // < "O(m) * inner"
			// pick random unvisited edge (of conflict graph)
			Edge baseEdge = randomEdge(conflictGraph, visited); // O(m)
			Vertex start = baseEdge.getStart();
			Vertex end = baseEdge.getTarget();

			visited[start.getIndex()] = true;
			visited[end.getIndex()] = true;
			if (!treeNode[start.getIndex()]) {
				treeNode[start.getIndex()] = true;
				numVisited++;
			}
			if (!treeNode[end.getIndex()]) {
				treeNode[end.getIndex()] = true;
				numVisited++;
			}
			direction[baseEdge.getIndex()] = Direction.FORWARD;
			currentDfsEdge[start.getIndex()] = baseEdge;
			parent[end.getIndex()] = baseEdge;

			ArrayList<Vertex> path = new ArrayList<Vertex>();
			path.add(start);
			path.add(end);
			edgeVisited[baseEdge.getIndex()] = true;
			paths.add(path);
			numVisited += dfs(end, visited, edgeVisited, treeNode, direction, currentDfsEdge,
					parent, dependencies, paths); // O(m + m^2)

			// place path vertices on pages
			placePaths(paths, distribution, embedding.getK());
			paths.clear();
			visited = Arrays.copyOf(treeNode, visited.length);
			for (int i = 0; i < treeNode.length; ++i) { // O(m)
				if (treeNode[i]) {
					currentDfsEdge[i] = null;
				}
			}
			for (int i = 0; i < dependencies.size(); ++i) {
				dependencies.get(i).clear();
			}
		}

		// place isolated vertices randomly
		for (int i = 0; i < conflictGraph.getN(); ++i) { // O(m)
			if (conflictGraph.getVertexByIndex(i).getDegree() == 0) {
				distribution[i] = ThreadLocalRandom.current().nextInt(embedding.getK());
			}
		}

		embedding.setDistribution(distribution);
	}

	private int dfs(Vertex v, boolean[] visited, boolean[] edgeVisited, boolean[] treeNode,
			Direction[] direction, Edge[] currentDfsEdge, Edge[] parent,
			ArrayList<ArrayList<Edge>> dependencies, ArrayList<ArrayList<Vertex>> paths) {
		int numVisited = 0;

		// DFS O(m + m^2)
		for (Edge e : v.getEdges()) {
			Vertex w;

			// find next edge of DFS-tree // O(1)
			if (e.getStart().equals(v)) {
				w = e.getTarget();
				if (w.equals(getStartVertex(parent[v.getIndex()], direction)))
					continue; // do not consider the parent-child edge
				direction[e.getIndex()] = Direction.FORWARD;
			} else {
				w = e.getStart();
				if (w.equals(getStartVertex(parent[v.getIndex()], direction)))
					continue; // do not consider the parent-child edge
				direction[e.getIndex()] = Direction.BACKWARD;
			}

			// if not visited yet -> no ear -> proceed
			if (!visited[w.getIndex()]) {
				parent[w.getIndex()] = e;
				visited[w.getIndex()] = true;
				currentDfsEdge[v.getIndex()] = e;

				// ----- recursive call ------
				numVisited += dfs(w, visited, edgeVisited, treeNode, direction, currentDfsEdge,
						parent, dependencies, paths);

			} else { // ear closed

				if (currentDfsEdge[w.getIndex()] != null) {
					Edge wx = currentDfsEdge[w.getIndex()];
					dependencies.get(wx.getIndex()).add(e);
					Vertex x;
					if (wx.getStart().equals(w)) {
						x = wx.getTarget();
					} else {
						x = wx.getStart();
					}

					if (treeNode[x.getIndex()]) {
						numVisited += processEars(wx, treeNode, edgeVisited, direction, parent,
								dependencies, paths);
					}
				}
			}
		}

		return numVisited;
	}

	private void placePaths(ArrayList<ArrayList<Vertex>> paths, int[] distribution, int k) { // O(path*m)
		for (ArrayList<Vertex> path : paths) {
			// pick random page for start vertex
			int p = ThreadLocalRandom.current().nextInt(k);
			if (distribution[path.get(0).getIndex()] == -1) { // O(1)
				while (path.size() > 1 && distribution[path.get(1).getIndex()] == p) {
					p = (p + 1) % k;
				}

				distribution[path.get(0).getIndex()] = p;
			}

			// place internal start vertices
			for (int i = 1; i < path.size() - 1; ++i) { // O(path * m)
				if (distribution[path.get(i).getIndex()] == -1) {
					// O(m)
					distribution[path.get(i).getIndex()] = pickOptimalPage(path.get(i),
							distribution, k);
				} else {
					// System.out.println("internal vertex already placed");
					// throw new IllegalStateException("Internal vertex already
					// placed.");
				}
			}

			// place last vertex
			if (distribution[path.get(path.size() - 1).getIndex()] == -1) { // O(1)
				p = ThreadLocalRandom.current().nextInt(k);
				while (p == distribution[path.get(path.size() - 2).getIndex()]) {
					p = (p + 1) % k;
				}

				distribution[path.get(path.size() - 1).getIndex()] = p;
			}
		}
	}

	private int processEars(Edge wx, boolean[] treeNode, boolean[] edgeVisited,
			Direction[] direction, Edge[] parent, ArrayList<ArrayList<Edge>> dependencies,
			ArrayList<ArrayList<Vertex>> paths) {

		// (I think) walks on ears an

		int numVisited = 0;
		for (Edge vw : dependencies.get(wx.getIndex())) {
			Vertex w = getStartVertex(wx, direction);

			Vertex v = getStartVertex(vw, direction);
			ArrayList<Vertex> path = new ArrayList<Vertex>();
			ArrayList<Edge> treeEdges = new ArrayList<Edge>();
			path.add(w);
			path.add(v);
			if (!treeNode[v.getIndex()]) {
				treeNode[v.getIndex()] = true;
				numVisited++;
			}
			Edge parentEdge = parent[v.getIndex()];
			Vertex u = getStartVertex(parentEdge, direction);
			while (!treeNode[u.getIndex()]) {
				path.add(u);
				treeEdges.add(parentEdge);
				edgeVisited[parentEdge.getIndex()] = true;
				treeNode[u.getIndex()] = true;
				numVisited++;

				parentEdge = parent[u.getIndex()];
				u = getStartVertex(parentEdge, direction);
			}

			if (!edgeVisited[parentEdge.getIndex()]) {
				path.add(u);
				treeEdges.add(parentEdge);
				edgeVisited[parentEdge.getIndex()] = true;
				if (!treeNode[u.getIndex()]) {
					treeNode[u.getIndex()] = true;
					numVisited++;
				}
			}
			paths.add(path);

			for (Edge e : treeEdges) {
				numVisited += processEars(e, treeNode, edgeVisited, direction, parent, dependencies,
						paths);
			}
		}

		dependencies.get(wx.getIndex()).clear();
		return numVisited;
	}

	private int pickOptimalPage(Vertex v, int[] distribution, int k) { // O(m)
		int[] neighborsOnPage = new int[k];
		for (Vertex u : v.getNeighbors()) {
			if (distribution[u.getIndex()] != -1) {
				neighborsOnPage[distribution[u.getIndex()]]++;
			}
		}

		int minNeighbors = neighborsOnPage[0];
		int bestPage = 0;

		for (int i = 1; i < k; ++i) {
			if (neighborsOnPage[i] < minNeighbors) {
				bestPage = i;
				minNeighbors = neighborsOnPage[i];
			}
		}

		return bestPage;
	}

	// --- helper methods ---
	private Edge randomEdge(Graph conflictGraph, boolean[] vertexVisited) { // O(1)
		// pick random edge:
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int vertexId = random.nextInt(conflictGraph.getN());
		while (vertexVisited[vertexId]
				|| conflictGraph.getVertexByIndex(vertexId).getEdges().size() == 0) {
			vertexId = (vertexId + 1) % conflictGraph.getN();
		}

		Vertex v = conflictGraph.getVertexByIndex(vertexId);
		return v.getEdges().get(0);
	}

	private Vertex getStartVertex(Edge e, Direction[] direction) { // O(1)
		return direction[e.getIndex()] == Direction.FORWARD ? e.getStart() : e.getTarget();
	}

	private enum Direction {
		FORWARD, BACKWARD
	}

	@Deprecated
	int pickBestPage(Vertex v, Vertex pred, Vertex succ, int[] distribution, int k) { // O(1)
		if (k > 2) {
			int page = ThreadLocalRandom.current().nextInt(k);
			while (page == distribution[pred.getIndex()] || page == distribution[succ.getIndex()]) {
				page = (page + 1) % k;
			}

			return page;
		} else {
			if (distribution[succ.getIndex()] == -1) {
				return 1 - distribution[pred.getIndex()];
			}
			return ThreadLocalRandom.current().nextInt(k);
		}
	}
}
