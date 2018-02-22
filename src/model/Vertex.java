package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A vertex of a {@link Graph}.
 * 
 * @author Jonathan Klawitter
 */
public class Vertex {
	
	private final int index;
	
	protected List<Edge> edges = new ArrayList<Edge>();
		
	public Vertex(int index) {
		this.index = index;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void addEdge(Edge edge) {
		if ((edge.getStart() != this) && (edge.getTarget() != this)) {
			throw new IllegalArgumentException("Edge added to wrong vertex.");
		}
		edges.add(edge);
	}

	public int getIndex() {
		return index;
	}
	
	public int getDegree() {
		return edges.size();
	}
	
	public Vertex[] getNeighbors() {
		Vertex[] neighbors = new Vertex[getDegree()];
		List<Edge> edges  = getEdges();
		for (int i = 0; i < edges.size(); ++i) {
			if (edges.get(i).getStart().equals(this)) {
				neighbors[i] = edges.get(i).getTarget();				
			} else {
				neighbors[i] = edges.get(i).getStart();				
			}
		}

		return neighbors;
	}
	
	@Override
	public String toString() {
		String str = "V<" + index + ">[dgr" + getDegree() + "]";
		for (Edge edge : edges) {
			str += edge.toString();
		}
		return str;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Vertex)) return false;
	    Vertex v = (Vertex) other;
	    
	    return this.index == v.index;
	}
	
	@Override
	public int hashCode() {
		return this.index;
	}
	
	public void removeNeighbour(Vertex toRemove) {
		for (Edge e : edges) {
			if (e.getOtherEnd(this) == toRemove) {
				edges.remove(e);
				return;
			}
		}
	}

	public boolean isValid() {
		for (Edge edge : edges) {
			Vertex other = edge.getOtherEnd(this);
			if (other == this || other.getIndex() == this.getIndex()) {
				System.out.println("Error, edge loop: " + this);
				return false;
			}
			
			for (Edge edge2 : edges) {
				if (edge == edge2) {
					continue;
				} else {
					if (edge2.getOtherEnd(this) == other) {
						System.out.println("Error, edge twice:"+ this);
						return false;
					} else if (edge2.getOtherEnd(this).getIndex() == other.getIndex()) {
						System.out.println("Error, edge twice:"+ this);
						return false;
					}
				}
			}
		}
		return true;
	}

	public void shuffleEdges() {
		Collections.shuffle(edges);
	}


}
