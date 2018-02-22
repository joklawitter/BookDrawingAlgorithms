package model;

import java.util.List;

import util.GraphUtils;

/**
 * This class represents an object oriented implemented graph, containing
 * {@link Vertex vertices} and {@link Edge edges}.
 *
 * @author Jonathan Klawitter
 */
public class Graph {

	private final Vertex[] vertices;
	private final Edge[] edges;

	private final int n;
	private final int m;
	private String name;

	/**
	 * Constructor for a graph by given {@link Vertex} array.
	 * 
	 * @param vertices
	 *            vertex array
	 * @param n
	 *            number of vertices
	 * @param m
	 *            number of edges
	 */
	public Graph(final Vertex[] vertices, final int n, final int m) {
		this(vertices, n, m, "");
	}

	/**
	 * Constructor for a graph by given {@link Vertex} array.
	 * 
	 * @param vertices
	 *            vertex array
	 * @param n
	 *            number of vertices
	 * @param m
	 *            number of edges
	 * @param graphClass
	 *            the graph class
	 * @param name
	 *            the name of this graph
	 */
	public Graph(final Vertex[] vertices, final int n, final int m, String name) {
		this.name = name;
		if (vertices == null) {
			throw new IllegalArgumentException("Vertices are null.");
		}
		if ((n < 0) || (m < 0)) {
			throw new IllegalArgumentException("n or m to small.");
		}
		if (n != vertices.length) {
			throw new IllegalArgumentException("Vertices of wrong length");
		}

		this.vertices = vertices;
		this.n = n;
		this.m = m;

		// for (int i = 0; i < vertices.length; i++) {
		// vertices[i].
		// }

		this.edges = new Edge[m];

		int curPosInEdgeArray = 0;
		for (int i = 0; i < n; i++) {

			List<Edge> curEdges = vertices[i].getEdges();
			for (Edge e : curEdges) {
				if (e.getIndex() < 0) {
					edges[curPosInEdgeArray] = e;
					e.setIndex(curPosInEdgeArray);
					curPosInEdgeArray++;
				}
			}
		}

		if (curPosInEdgeArray != m) {
			throw new IllegalStateException("m " + m + ", count " + curPosInEdgeArray);
		}
	}

	/**
	 * Constructor for graph by given vertices and edges. Indices should match
	 * array positions.
	 * 
	 * @param vertices
	 * @param edges
	 * @param name
	 *            the name of this graph
	 */
	public Graph(final Vertex[] vertices, final Edge[] edges, String name) {
		this.name = name;
		this.vertices = vertices;
		this.edges = edges;
		this.n = vertices.length;
		this.m = edges.length;
	}

	/**
	 * Returns whether this graph is valid. This means that
	 * <ul>
	 * <li>vertices and edges are in correct amount</li>
	 * <li>indices of vertices and edges are correct</li>
	 * </ul>
	 * 
	 * @return whether this graph is valid
	 */
	public boolean isValid() {
		if (n != vertices.length) {
			return false;
		}

		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i].getIndex() != i) {
				return false;
			}
		}

		if (m != edges.length) {
			return false;
		}

		for (int i = 0; i < edges.length; i++) {
			if (edges[i].getIndex() != i) {
				return false;
			}
		}

		for (Edge edge : edges) {
			if (edge.getStart().getIndex() == edge.getTarget().getIndex()) {
				System.out.println("illegal edge: " + edge);
				return false;
			}
		}

		int sumDegree = 0;
		for (Vertex vertex : vertices) {
			if (!vertex.isValid()) {
				return false;
			}
			sumDegree += vertex.getDegree();
		}
		if (sumDegree != 2 * m) {
			System.out
					.println("Degree of vertices invalid. sum = " + sumDegree + ", 2m = " + 2 * m);
			return false;
		}

		return true;
	}

	// -- GETTERS --
	/**
	 * Returns the number of vertices.
	 * 
	 * @return the number of vertices
	 */
	public int getN() {
		return n;
	}

	/**
	 * Returns the number of edges.
	 * 
	 * @return the number of edges
	 */
	public int getM() {
		return m;
	}

	/**
	 * Returns the density of the graph, i.e. #edges / #possible edges.
	 * 
	 * @return the density of the graph in [0,1]
	 */
	public double getDensity() {
		int maxM = getN() * (getN() - 1) / 2;
		return ((double) getM()) / ((double) maxM);
	}

	/**
	 * Returns the degree of vertex at given index. Please note, it returns only
	 * the out degree of the DG. Use vertex.getDegree or getOODegree if vertex
	 * instance of Vertex.
	 * 
	 * @param vertexIndex
	 * @return the degree of vertex at given index.
	 */
	public int getDegreeOf(int vertexIndex) {
		if (vertexIndex >= 0 && vertexIndex < getN()) {
			return getVertexByIndex(vertexIndex).getDegree();
		}
		return -1;
	}

	// OO getter
	/**
	 * Returns the vertex with given index.
	 * 
	 * @return the {@link Vertex} with given index
	 */
	public Vertex getVertexByIndex(int vertexIndex) {
		return vertices[vertexIndex];
	}

	/**
	 * Returns the {@link Vertex vertices} of this graph.
	 * 
	 * @return the {@link Vertex vertices} of this graph
	 */
	public Vertex[] getVertices() {
		return vertices;
	}

	/**
	 * Returns the edge with given index.
	 * 
	 * @return the {@link Edge} with given index
	 */
	public Edge getEdgeByIndex(int edgeIndex) {
		return edges[edgeIndex];
	}

	/**
	 * Returns the {@link Edge edges} of this graph.
	 * 
	 * @return the {@link Edge edges} of this graph
	 */
	public Edge[] getEdges() {
		return edges;
	}

	/**
	 * Returns the name of this graph, which may be based on its
	 * {@link GraphClass}.
	 * 
	 * @return the name of this graph (might be the empty string)
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            new name of the graph
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		String str = "Graph: n = " + n + ", m = " + m + "\n";
		String vis = "";
		for (Vertex vertex : vertices) {
			vis += vertex.toString() + "\n";
		}

		return str + vis;
	}

	/**
	 * Returns a graph with the same structure but with new objects.
	 * 
	 * @return a copy of this graph
	 */
	public Graph copy() {
		Vertex[] newVertices = GraphUtils.getInitialVertexArray(n);

		for (Edge e : this.getEdges()) {
			new Edge(newVertices[e.getStart().getIndex()], newVertices[e.getTarget().getIndex()]);
		}

		return new Graph(newVertices, this.getN(), this.getM(), this.getName());
	}

}
