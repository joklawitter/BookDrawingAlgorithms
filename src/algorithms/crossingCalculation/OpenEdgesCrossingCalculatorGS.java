package algorithms.crossingCalculation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;

public class OpenEdgesCrossingCalculatorGS implements ICrossingCalculator {

	@Override
	public long calculateNumberOfCrossings(Embedding embedding) {
		// fixed values
		int k = embedding.getK();
		Graph g = embedding.getGraph();
		int n = g.getN();
		int[] spine = embedding.getSpine();
		if (g.getM() <= 0) {
			return 0;
		}
		Edge[] edges = initSortEdges(embedding, g);
		
		// variables
		long crossings = 0;
		ArrayList<LinkedList<Edge>> openEdgesPerPage = initOpenEdgesLists(k);
		HashMap<Integer, ArrayList<Edge>> edgesToProcessPerVertex = new HashMap<Integer, ArrayList<Edge>>(
				n);

		// counter E
		int e = 0;
		Edge currentForwardEdge = edges[e++];
		
		// counter V
		for (int v = 0; v < spine.length; v++) {
			Vertex currentVertex = g.getVertexByIndex(spine[v]);

			// edges which end at v/currentVertex
			ArrayList<Edge> incomingEdges = edgesToProcessPerVertex.get(currentVertex.getIndex());
			if (incomingEdges != null) {
				// shorter edges added later -> backwards through incoming edges
				for (int i = incomingEdges.size() - 1; i >= 0; i--) {

					// get current edge backwards from v 
					Edge currentBackwardEdge = incomingEdges.get(i);
					int currentEdgePage = embedding.getPageOfEdge(currentBackwardEdge);
					
					// iterate over edges open on this edges page
					LinkedList<Edge> openCrossingCandidates = openEdgesPerPage.get(currentEdgePage);
					Iterator<Edge> iterator = openCrossingCandidates.descendingIterator();
					Edge edgeCandidate;
					while (iterator.hasNext()) {
						edgeCandidate = iterator.next();
						
						// crossings with open edges till found edge itself 
						if (edgeCandidate.equals(currentBackwardEdge)) {
							iterator.remove();
							break;
						} else {
							crossings++;
//							if (!embedding.wouldEdgesCross(currentBackwardEdge, edgeCandidate)) {
//								embedding.printEdge(currentBackwardEdge);
//								embedding.printEdge(edgeCandidate);								
//							}
						}
					}
				}
				
				edgesToProcessPerVertex.remove(currentVertex.getIndex());
			}

			// edges which start at v/currentVertex
			while ((e <= edges.length)
					&& (embedding.getPositionOfSmallerEndpoint(currentForwardEdge) == v)) {
				Vertex otherSide = currentForwardEdge.getOtherEnd(currentVertex);
				
				// add edge to processing edges for target vertex
				ArrayList<Edge> targetEdges = edgesToProcessPerVertex.get(otherSide.getIndex());
				if (targetEdges == null) {
					targetEdges = new ArrayList<Edge>(n - v);
					edgesToProcessPerVertex.put(otherSide.getIndex(), targetEdges );
				}
				targetEdges.add(currentForwardEdge);
				
				// add edge to open edges of its page
				int currentEdgePage = embedding.getPageOfEdge(currentForwardEdge.getIndex());
				openEdgesPerPage.get(currentEdgePage).add(currentForwardEdge);
				
				// step forward
				if (e == edges.length ) {
					break;
				}
				currentForwardEdge = edges[e++];
			}

		}
		
		for (int i = 0; i < openEdgesPerPage.size(); i++) {
			if (openEdgesPerPage.get(i).size() != 0) {
				System.out.println(openEdgesPerPage.get(i).size() + " on page " + i);
			}
		}

		return crossings;
	}

	private Edge[] initSortEdges(Embedding embedding, Graph g) {
		Edge[] edges = new Edge[g.getM()];
		Edge[] edgesToCopy = g.getEdges();
		for (int i = 0; i < edges.length; i++) {
			edges[i] = edgesToCopy[i];
		}
		Arrays.sort(edges, (x, y) -> embedding.compareEdgesOutgoingAsEmbedded(x, y));
		return edges;
	}

	private ArrayList<LinkedList<Edge>> initOpenEdgesLists(int k) {
		ArrayList<LinkedList<Edge>> openEdgesPerPage = new ArrayList<LinkedList<Edge>>(k);
		for (int i = 0; i < k; i++) {
			openEdgesPerPage.add(i, new LinkedList<Edge>());
		}
		return openEdgesPerPage;
	}

	@Override
	public long calculateNumberOfCrossingsOnPage(Embedding embedding, int pageIndex) {
		Graph g = embedding.getGraph();
		int[] spine = embedding.getSpine();
		int[] positions = embedding.getVertexOnSpineArray();

		LinkedList<Edge> openEdgesOnPage = new LinkedList<Edge>();
		long crossings = 0;

		for (int i = 0; i < spine.length; i++) {
			Vertex currentVertex = g.getVertexByIndex(spine[i]);
			List<Edge> currentEdges = currentVertex.getEdges();
			final int position = i;
			Collections.sort(currentEdges,
					(x, y) -> embedding.compareEdgesSharingEndpoint(x, y, position));

			for (Edge currentEdge : currentVertex.getEdges().stream()
					.filter(x -> (embedding.getPageOfEdge(x.getIndex()) == pageIndex))
					.sorted((x, y) -> embedding.compareEdgesSharingEndpoint(x, y, position))
					.collect(Collectors.toList())) {

				Vertex otherSide = currentEdge.getOtherEnd(currentVertex);

				if (positions[otherSide.getIndex()] < i) {
					// this is the end of an edge, which crosses all open ones
					Iterator<Edge> iterator = openEdgesOnPage.descendingIterator();
					Edge edgeCandidate;
					while (iterator.hasNext()) {
						edgeCandidate = iterator.next();
						if (edgeCandidate.equals(currentEdge)) {
							iterator.remove();
							break;
						} else {// if (embedding.wouldEdgesCross(currentEdge,
								// edgeCandidate)){
							crossings++;
						}
					}
				} else {
					openEdgesOnPage.add(currentEdge);
				}
			}
		}

		return crossings;
	}

	@Override
	public String toString() {
		return "OpenEdgesCrossingCalculatorGS";
	}

}
