package algorithms.heuristics.edgedistribution;

import model.Edge;

public class Conflict {
	
	private final Edge edge1;
	
	private final Edge edge2;
	
	public Conflict(Edge edge1, Edge edge2) {
		if (edge1.compareTo(edge2) < 0) {
			this.edge1 = edge1;
			this.edge2 = edge2;
		} else {
			this.edge1 = edge2;
			this.edge2 = edge1;
		}
	}
	
	public Edge getFirstEdge() {
		return edge1;
	}

	public Edge getSecondEdge() {
		return edge2;
	}
	
	@Override
	public String toString() {
		return "Conflict (" + edge1.toString() + ", " + edge2.toString() + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + edge1.hashCode();
		result = prime * result + edge2.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Conflict other = (Conflict) obj;
		if (!edge1.equals(other.edge1))
			return false;
		if (!edge2.equals(other.edge2))
			return false;
		return true;
	}
}
