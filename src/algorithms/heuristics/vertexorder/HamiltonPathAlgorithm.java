package algorithms.heuristics.vertexorder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import model.Edge;
import model.Embedding;
import model.Graph;
import model.Vertex;
import algorithms.base.VertexOrderAlgorithm;

/**
 * Heuristic to compute a hamiltonian path in the graph (if present) and order
 * the vertices in the order they appear in the path. This is an implementation
 * of the algorithm of Angluin and Valiant. Note that the computation of a
 * hamiltonian path is NP-complete and therefore, there might be a hamiltonian
 * path present in the graph even if this heuristic can't compute it.
 * 
 * @see https://www.math.upenn.edu/~wilf/AlgoComp.pdf, p.121
 * @author Michael
 *
 */
public class HamiltonPathAlgorithm implements VertexOrderAlgorithm {

	@Override
	public void computeVertexOrder(Embedding embedding) {
		ArrayList<Vertex> path = findPath(embedding.getGraph());
		
		if (path.size() == embedding.getGraph().getN()) {
			int[] spine = embedding.getSpine();			
			for (int i = 0; i < path.size(); ++i) {
				spine[i] = path.get(i).getIndex();
			}
			embedding.setSpine(spine);
		} else {
			int[] spine = embedding.getSpine();
			HashSet<Integer> alreadyPlaced = new HashSet<Integer>();
			for (int i = 0; i < path.size(); ++i) {
				spine[i] = path.get(i).getIndex();
				alreadyPlaced.add(spine[i]);				
			}
			
			int idx = path.size();
			for (int i = 0; i < spine.length; ++i) {
				if (!alreadyPlaced.contains(i)) {
					spine[idx++] = i;
				}
			}		
			
			embedding.setSpine(spine);
			
			RandomDFSAlgorithm rdfs = new RandomDFSAlgorithm();
			rdfs.computePartialVertexOrder(embedding, path.size()-1);
		}		
	}
	
	private ArrayList<Vertex> findPath(Graph G) {
		ArrayList<Vertex> path = new ArrayList<Vertex>();
		if (G.getM() == 0) return path;
		
		boolean[] inPath = new boolean[G.getN()];
		boolean[] edgeVisited = new boolean[G.getM()];
		
		int numIterations = 0;
		
		// choose start vertex with degree > 0
		int startId = ThreadLocalRandom.current().nextInt(G.getN());
		while (G.getDegreeOf(startId) == 0) {
			startId = (startId + 1) % G.getN();
		}
		
		Vertex currentVertex = G.getVertexByIndex(startId);
		Vertex target = currentVertex;
		
		path.add(currentVertex);
		inPath[currentVertex.getIndex()] = true;
		do {			
			// choose an edge of currentVertex
			List<Edge> edges = currentVertex.getEdges();
			int e = ThreadLocalRandom.current().nextInt(currentVertex.getDegree());

			int edgeId = edges.get(e).getIndex();	
			for (int i = 1; i < currentVertex.getDegree() && edgeVisited[edgeId]; ++i) {
				e = (e+1) % currentVertex.getDegree();
				edgeId = edges.get(e).getIndex();
			}
			
			if (edgeVisited[edgeId]) {
				if (path.size() > G.getN() / 2 && numIterations < 2) {
					reversePathFrom(0, path);
					currentVertex = path.get(path.size()-1);
					numIterations++;
					continue;
				}
				
				return path; // no hamiltonian path found, the partial path is returned
			}
			
			edgeVisited[edgeId] = true;
			Edge edge = edges.get(e);
			Vertex nextVertex = edge.getStart().equals(currentVertex)? edge.getTarget() : edge.getStart();
			
			if (!nextVertex.equals(target)) {
				if (!inPath[nextVertex.getIndex()]) {
					currentVertex = nextVertex;
					path.add(currentVertex);
					inPath[currentVertex.getIndex()] = true;
				} else { // short-circuit
					int idx = path.size()-1;
					while (!path.get(idx).equals(nextVertex)) {
						idx--;
					}
					
					Vertex u = path.get(idx+1); // successor of nextVertex on path
					path.set(idx+1, currentVertex); // replace u with currentVertex
					path.remove(path.size()-1); // remove currentVertex from end of path
					reversePathFrom(idx+2, path); // reverse the rest of the path
					currentVertex = u;		
					path.add(currentVertex);
					inPath[currentVertex.getIndex()] = true;
				}
			} 			
		} while (path.size() < G.getN()); // every vertex except target has to be in the path
		
		return path;	
	}
	
	private void reversePathFrom(int idx, ArrayList<Vertex> path) {		
		for (int i = idx, count = 1; i < Math.floor((double) path.size() / 2.0); ++i, ++count) {
			Vertex temp = path.get(path.size()-count);
			path.set(path.size()-count, path.get(i));
			path.set(i, temp);			
		}
	}

}
