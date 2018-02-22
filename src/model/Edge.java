package model;

/**
 * An edge of a {@link Graph}.
 * 
 * @author Jonathan Klawitter
 */
public class Edge implements Comparable<Edge> {

	private final Vertex start;
	private final Vertex target;

	private int index = -1;

	public Edge(Vertex start, Vertex target) {
		super();
		if (start.getIndex() > target.getIndex()) {
			Vertex tmp = start;
			start = target;
			target = tmp;
		}
		this.start = start;
		this.target = target;
		
		start.addEdge(this);
		target.addEdge(this);
	}	
		
	public Edge(int startId, int endId) {
		this(new Vertex(startId), new Vertex(endId));
	}

	public Vertex getStart() {
		return start;
	}

	public Vertex getTarget() {
		return target;
	}

	/**
	 * Returns for one vertex of the edge the other side.
	 * @param currentVertex one vertex of the edge
	 * @return the other end of the edge
	 */
	public Vertex getOtherEnd(Vertex currentVertex) {
		if (currentVertex.equals(start)) {
			return getTarget();
		} else if (currentVertex.equals(target)) {
			return getStart();
		} else {
			throw new IllegalArgumentException("Vertex not part of this edge.");
		}
	}
	
	/**
	 * Returns the index in the embedding distribution array.
	 * 
	 * @return the index in the embedding distribution array.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set the index in the embedding distribution array.
	 * 
	 * @param index
	 *            in the embedding distribution array.
	 */
	public void setIndex(int index) {
		// can only be set once
		if ((this.index < 0) && (index >= 0)) {
			this.index = index;
		}
	}

	/**
	 * Returns true if index for start and source are the same.
	 * They don't have to be the same object instances.
	 * @return if indices of vertices are the same
	 */
	@Override
	public boolean equals(Object other) {
	    boolean result = false;
	    if (other instanceof Edge) {
	    	Edge that = (Edge) other;
	        result = (this.getStart().getIndex() == that.getStart().getIndex() 
	        		&& this.getTarget().getIndex() == that.getTarget().getIndex());
	    }
	    return result;
	}
	
	public int compareTo(Edge o) {
		int oStartIndex = o.getStart().getIndex();
		int oTargetIndex = o.getTarget().getIndex();
		int meStartIndex = this.getStart().getIndex();
		int meTargetIndex = this.getTarget().getIndex();
		if (oStartIndex < meStartIndex) {
			return 1;
		}
		if (oStartIndex > meStartIndex) {
			return -1;
		}
		// oStartIndex == meStartIndex
		if (oTargetIndex < meTargetIndex) {
			return 1;
		}
		if (oTargetIndex > meTargetIndex) {
			return -1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = result * 4003 + getStart().getIndex(); 
		result = result * 31 + getTarget().getIndex(); 
		return result;
	}

	@Override
	public String toString() {
		return "E[" + index + "](" + start.getIndex() + "->" + target.getIndex() + ")";
	}


}
