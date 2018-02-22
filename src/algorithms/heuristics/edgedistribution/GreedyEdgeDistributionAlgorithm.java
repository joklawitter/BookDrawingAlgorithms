package algorithms.heuristics.edgedistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.EdgeDistributionAlgorithm;

/**
 * This class implements several edge distribution heuristics which all belong
 * to the class of greedy algorithms. The general framework first computes an
 * edge order according to a user defined strategy. Then the edges are processed
 * in this order and each edge is put on the page with the least increase of
 * crossings.
 * 
 * @author Michael Wegener
 *
 */
public class GreedyEdgeDistributionAlgorithm implements EdgeDistributionAlgorithm {
	public static enum Order {
		ROW_MAJOR {
			@Override
			public Iterator<Edge> order(Embedding embedding) {
				return rowMajor(embedding);
			}

		},
		ROW_MAJOR_V {
			@Override
			public Iterator<Edge> order(Embedding embedding) {
				return rowMajorForOrderOnSpine(embedding);
			}

		},
		RANDOM {
			@Override
			public Iterator<Edge> order(Embedding embedding) {
				ArrayList<Edge> edges = new ArrayList<Edge>(Arrays.asList(embedding.getGraph()
						.getEdges()));
				Collections.shuffle(edges);
				return edges.iterator();
			}
		},
		E_LEN {
			@Override
			public Iterator<Edge> order(Embedding embedding) {
				return eLen(embedding);
			}
		},
		CEIL_FLOOR {
			@Override
			public Iterator<Edge> order(Embedding embedding) {
				return ceilFloor(embedding);
			}
		},
		CIRCULAR {
			@Override
			public Iterator<Edge> order(Embedding embedding) {
				return circular(embedding);
			}
		};

		public abstract Iterator<Edge> order(Embedding embedding);
	};

	private Order order;

	/**
	 * Creates an instance of the algorithm with the edge ordering strategy
	 * {@code order}.
	 * 
	 * @param order
	 *            The edge ordering strategy to use.
	 */
	public GreedyEdgeDistributionAlgorithm(Order order) {
		this.order = order;
	}
	
	public GreedyEdgeDistributionAlgorithm() {
		this(Order.E_LEN);
	}

	@Override
	/**
	 * Computes an edge distribution by ordering the edges according to the order strategy specified. 
	 * Then the edges are processed in this order and each edge is put on the page with the least increase of crossings.
	 */
	public void computeEdgeDistribution(Embedding embedding) {
		if (embedding.getK() == 1) {
			Arrays.fill(embedding.getDistribution(), 0);
			embedding.invalidateNumberOfCrossings();
			// nothing else to do here
			return;
		}
		
		Iterator<Edge> it = order.order(embedding);
		boolean[] alreadyPlaced = new boolean[embedding.getGraph().getM()];
		long totalCrossings = 0;

		while (it.hasNext()) {
			Edge e = it.next();
			int minCrossings = Integer.MAX_VALUE;
			int bestPage = 0;

			int[] crossingsOnPage = crossingsOnPages(alreadyPlaced, embedding, e);
			for (int i = 0; i < embedding.getK(); ++i) {
				if (crossingsOnPage[i] < minCrossings) {
					minCrossings = crossingsOnPage[i];
					bestPage = i;
				}
			}
			totalCrossings += minCrossings;
			embedding.getDistribution()[e.getIndex()] = bestPage;
			alreadyPlaced[e.getIndex()] = true;
		}
		embedding.setNumberOfCrossings(totalCrossings);
	}

	/**
	 * Calculates the number of crossings produced by placing the edge on each
	 * possible page. Only edges which are {@code alreadyPlaced} are inspected
	 * to calculate the number of crossings.
	 * 
	 * @param alreadyPlaced
	 *            Specifies for each edge if it has already been placed
	 * @param embedding
	 * @param edge
	 * @return An array of length maxPages where the value at position i is
	 *         interpreted as the number of crossings produced by {@code edge}
	 *         on page i if placed there.
	 */
	public static int[] crossingsOnPages(boolean[] alreadyPlaced, Embedding embedding, Edge edge) {
		int[] crossingsOnPage = new int[embedding.getK()];

		for (int i = 0; i < alreadyPlaced.length; ++i) {
			if (alreadyPlaced[i]) {
				if (embedding.canEdgesCross(edge, embedding.getGraph().getEdgeByIndex(i))) {
					crossingsOnPage[embedding.getPageOfEdge(i)]++;
				}
			}
		}

		return crossingsOnPage;
	}

	/**
	 * Computes a row-major ordering of the edges, i.e. first all edges (0,v)
	 * with v in V are added, then all edges (1,v) and so on.
	 * 
	 * @param embedding
	 */
	public static Iterator<Edge> rowMajor(Embedding embedding) {
		ArrayList<Edge> edges = new ArrayList<Edge>(embedding.getGraph().getM());
		for (Vertex v : embedding.getGraph().getVertices()) { // add edges in
																// row-major
																// order
			for (Edge e : v.getEdges()) {
				if (e.getStart().equals(v)) {
					edges.add(e);
				}
			}
		}

		return edges.iterator();
	}
	
	/**
	 * Computes the row-major ordering of the edges based on their real position on the spine.
	 * @param embedding the embedding that describes the vertex order
	 * @return an iterator for the row-major ordering
	 */
	public static Iterator<Edge> rowMajorForOrderOnSpine(Embedding embedding) {
		List<Edge> edges = new ArrayList<>(embedding.getGraph().getM());
		for (int i = 0; i < embedding.getGraph().getN(); i++) {
			Vertex v = embedding.getGraph().getVertexByIndex(embedding.getVertexAtPosition(i));
			List<Edge> outgoingEdges = new ArrayList<>(v.getDegree());
			for (Edge e : v.getEdges()) {
				Vertex other = e.getOtherEnd(v);
				if (embedding.getPositionOnSpine(other.getIndex()) > i) {
					outgoingEdges.add(e);
				}
			}
			Collections.sort(outgoingEdges, (e1, e2) -> compareBasedOnOtherVertex(embedding, v, e1, e2));
			edges.addAll(outgoingEdges);
		}
		
		return edges.iterator();
	}
	
	private static int compareBasedOnOtherVertex(Embedding embedding, Vertex v, Edge e1, Edge e2) {
		Vertex v1 = e1.getStart() == v ? e1.getTarget() : e1.getStart();
		Vertex v2 = e2.getStart() == v ? e2.getTarget() : e2.getStart();
		return Integer.compare(embedding.getPositionOnSpine(v1.getIndex()), 
				embedding.getPositionOnSpine(v2.getIndex()));		
	}

	/**
	 * Computes an ordering of the edges s.t. the edges appear in a decreasing
	 * order of their length. The length of an edge (u,v) is defined as
	 * |vertexOnSpine(u) - vertexOnSpine(v)|.
	 * 
	 * @param embedding
	 */
	public static Iterator<Edge> eLen(Embedding embedding) {
		ArrayList<Edge> edges = new ArrayList<Edge>(Arrays.asList(embedding.getGraph().getEdges()));
		Collections.shuffle(edges);
		Collections.sort(edges, new Comparator<Edge>() {
			@Override
			public int compare(Edge e1, Edge e2) {
				return Math.abs(embedding.getPositionOnSpine(e2.getStart().getIndex())
						- embedding.getPositionOnSpine(e2.getTarget().getIndex()))
						- Math.abs(embedding.getPositionOnSpine(e1.getStart().getIndex())
								- embedding.getPositionOnSpine(e1.getTarget().getIndex()));
			}

		});

		return edges.iterator();
	}

	/**
	 * Computes an ordering of the edges according to their length s.t. edges
	 * with medium sized lengths are ordered first.
	 * 
	 * @param embedding
	 */
	public static Iterator<Edge> ceilFloor(Embedding embedding) {
		if (embedding.getGraph().getN() == 1) {
			return Collections.emptyIterator();
		}
		
		ArrayList<ArrayList<Edge>> buckets = new ArrayList<ArrayList<Edge>>();
		for (int i = 0; i < embedding.getGraph().getN() - 1; ++i) {
			buckets.add(new ArrayList<Edge>());
		}

		for (Edge e : embedding.getGraph().getEdges()) {
			int length = Math.abs(embedding.getPositionOnSpine(e.getStart().getIndex())
					- embedding.getPositionOnSpine(e.getTarget().getIndex()));
			buckets.get(length - 1).add(e);
		}

		ArrayList<Edge> edges = new ArrayList<Edge>();
		Collections.shuffle(edges);
		int n = embedding.getGraph().getN();
		int midBucket = n / 2 - 1;
		edges.addAll(buckets.get(midBucket));
		for (int i = 1; i < n / 2; ++i) {
			edges.addAll(buckets.get(midBucket - i));
			edges.addAll(buckets.get(midBucket + i));
		}
		if (n % 2 != 0) {
			edges.addAll(buckets.get(n - 2));
		}

		return edges.iterator();
	}

	/**
	 * Computes an edge ordering by using the circular heuristic from [Sats13]
	 * 
	 * @param embedding
	 */
	public static Iterator<Edge> circular(Embedding embedding) {
		HashMap<Edge, Edge> graphEdges = new HashMap<Edge, Edge>();
		Graph G = embedding.getGraph();
		for (Edge e : G.getEdges()) {
			graphEdges.put(e, e);
		}

		int n = G.getN();
		int nCeil = (int) Math.ceil((double) n / 2.0);
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (int vId = 0; vId < nCeil; ++vId) {
			int v = vId;
			for (int i = 1; i < nCeil; ++i) {
				int u = (vId + i) % n;
				Edge vu = new Edge(v, (vId + i) % n);
				if (graphEdges.containsKey(vu)) {
					edges.add(graphEdges.get(vu));
					graphEdges.remove(vu);
				}

				v = (n + vId - i) % n;
				Edge uv = new Edge(u, v);
				if (graphEdges.containsKey(uv)) {
					edges.add(graphEdges.get(uv));
					graphEdges.remove(uv);
				}
			}

			if (n % 2 == 0) {
				Edge vu = new Edge(v, (vId + n / 2) % n);
				if (graphEdges.containsKey(vu)) {
					edges.add(graphEdges.get(vu));
					graphEdges.remove(vu);
				}
			}
		}

		return edges.iterator();
	}

}
