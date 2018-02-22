package algorithms.crossingCalculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;

public class OpenEdgesCrossingCalculatorVS implements ICrossingCalculator {


	@Override
	public long calculateNumberOfCrossings(Embedding embedding) {
		int k = embedding.getK();
		Graph g = embedding.getGraph();
		int[] spine = embedding.getSpine();
		int[] positions = embedding.getVertexOnSpineArray();

		long crossings = 0;
		ArrayList<LinkedList<Edge>> openEdgesPerPage = new ArrayList<LinkedList<Edge>>(k);
		for (int i = 0; i < k; i++) {
			openEdgesPerPage.add(i, new LinkedList<Edge>());
		}

		for (int i = 0; i < spine.length; i++) {
			Vertex currentVertex = g.getVertexByIndex(spine[i]);			
			List<Edge> currentEdges = currentVertex.getEdges();
			final int position = i;
			Collections.sort(currentEdges, (x, y) -> embedding.compareEdgesSharingEndpoint(x, y, position));
			for (Edge currentEdge : currentVertex.getEdges()) {
				Vertex otherSide = currentEdge.getOtherEnd(currentVertex);
				int currentEdgePage = embedding.getPageOfEdge(currentEdge.getIndex());
				
				if (positions[otherSide.getIndex()] < i) {
					// this is the end of an edge, which crosses all open ones
					LinkedList<Edge> openCrossingCandidates = openEdgesPerPage.get(currentEdgePage);
					Iterator<Edge> iterator = openCrossingCandidates.descendingIterator();
					Edge edgeCandidate;
					while (iterator.hasNext()) {
						edgeCandidate = iterator.next();
						if (edgeCandidate.equals(currentEdge)) {
							iterator.remove();
							break;
						} else {//if (embedding.wouldEdgesCross(currentEdge, edgeCandidate)){
							crossings++;
						}
					}
				} else {
					openEdgesPerPage.get(currentEdgePage).add(currentEdge);
				}
			}
		}
		
		return crossings;
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
			Collections.sort(currentEdges, (x, y) -> embedding.compareEdgesSharingEndpoint(x, y, position));
			
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
						} else {//if (embedding.wouldEdgesCross(currentEdge, edgeCandidate)){
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
		return "OpenEdgesCrossingCalculatorVS";
	}
}
