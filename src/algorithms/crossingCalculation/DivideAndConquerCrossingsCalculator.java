package algorithms.crossingCalculation;

import java.util.Arrays;

import model.Edge;
import model.Embedding;
import model.Graph;

/**
 * Provides methods that calculate the crossing number of an embedding.
 * 
 * <p>
 * <b>Algorithm overview: </b>
 * The calculation of the crossing number has three steps:
 * <ol>
 * <li>Group the edges that are on the same page and do the following two steps for each page:</li>
 * <li>For each edge e determine the number of edges that start between its two endpoints. This is an upper
 * bound for the number of crossings as each edge e' that crosses e has to start between its two endpoints, but
 * e' may lie completely between the endpoints of e. In this case the two edges do not cross.</li>
 * <li>These cases are subtracted in the third step. We use the following observation: The edges that do not cross
 * when drawn as arcs (like in the book embedding) are those edges that cross when the graph is drawn as a
 * bipartite graph. This crossing number can be calculated by a modified merge sort algorithm.</li>
 * </ol>
 * </p>
 *  
 * <p>
 * <b>Definition of the bipartite graph: </b>
 * We direct the edges from the endpoint with the smaller position of the spine to the one with the larger 
 * position. We obtain the bipartite graph by splitting each node in two such that the outgoing and incoming
 * edges are separated.  
 * </p>
 * 
 * @author Matthias Wolf
 *
 */
public class DivideAndConquerCrossingsCalculator implements ICrossingCalculator {
	
	public DivideAndConquerCrossingsCalculator () {
	}

	/**
	 * Calculates the number of crossings in an embedding.
	 * @param embedding the embedding
	 * @return the crossing number
	 */
	public long calculateNumberOfCrossings(Embedding embedding) {
		long sum = 0;
		
		Edge[][] groupedEdges = groupEdgesOnSamePage(embedding);
		
		for (Edge[] edges : groupedEdges) {
			sum += countCrossingsOnOnePage(edges, embedding);
		}
		
		embedding.setNumberOfCrossings(sum);
		
		return sum;
	}
	
	/**
	 * Calculates the number of crossings on the given page.
	 * @param embedding the embedding
	 * @param pageIndex the page
	 * @return the number of crossings on the page.
	 */
	public long calculateNumberOfCrossingsOnPage(Embedding embedding, int pageIndex) {
		Graph graph = embedding.getGraph();
		int[] edgeIndices = embedding.getAllEdgeIndicesAtPage(pageIndex);
		Edge[] edgesOnPage = new Edge[edgeIndices.length];
		for (int i = 0; i < edgesOnPage.length; i++) {
			edgesOnPage[i] = graph.getEdgeByIndex(edgeIndices[i]);
		}
		
		return countCrossingsOnOnePage(edgesOnPage, embedding);		
	}
	
	/**
	 * Groups the edges that are on the same page.
	 * @param embedding the embedding
	 * @return an array where each subarray at position i contains the edges the are on page i.
	 */
	private Edge[][] groupEdgesOnSamePage(Embedding embedding) {
		Edge[] edges = embedding.getGraph().getEdges();
		int[] edgeDistribution = embedding.getDistribution();
		
		int[] edgesOnPage = new int[embedding.getK()];
		for (int page : edgeDistribution) {
			edgesOnPage[page]++;
		}
		
		Edge[][] result = new Edge[embedding.getK()][];
		int[] indices = new int[embedding.getK()];
		for (int i = 0; i < embedding.getK(); i++) {
			result[i] = new Edge[edgesOnPage[i]];
		}
		
		for (Edge e : edges) {
			int page = embedding.getPageOfEdge(e.getIndex());
			int index = indices[page]++;
			result[page][index] = e;
		}
		
		return result;
	}
	
	/**
	 * Calculates the number of crossings of the given set of edges. Note that all edges must lie on the
	 * same page.
	 * @param edges the edges
	 * @param embedding the embedding
	 * @return the number of crossings.
	 */
	private long countCrossingsOnOnePage(Edge[] edges, Embedding embedding) {
		if (edges.length <= 1) return 0;
		
		Arrays.sort(edges, (x, y) -> embedding.compareEdges(x, y));
				
		// counts how many edges start at position i 
		long[] edgesStartingBefore = new long[embedding.getGraph().getN()];
		for (Edge e : edges) {
			edgesStartingBefore[embedding.getPositionOfSmallerEndpoint(e)]++;
		}
		// add up to how many start before and at this position
		for (int i = 1; i < edgesStartingBefore.length; i++) {
			edgesStartingBefore[i] += edgesStartingBefore[i-1];
		}
		
		// over count the crossings
		int maxVertexIndex = embedding.getN() - 1;
		long count = 0;
		for (Edge e : edges) {
			int start = embedding.getPositionOfSmallerEndpoint(e);
			int end = embedding.getPositionOfLargerEndpoint(e);
			
			if (start == end || start == maxVertexIndex) continue;
			count += edgesStartingBefore[end - 1] - edgesStartingBefore[start];
		}
		
		// subtract the not-crossings 
		count -= countBipartiteCrossings(edges, 0, edges.length, embedding,  new Edge[edges.length]);
		
		return count;
	}
	
	/**
	 * Counts the crossings of the graph if it was drawn as a bipartite graph.
	 * 
	 * <p>
	 * This method counts the crossings of the subsequence of the edges whose indices in 
	 * the {@code input} array lie in the interval {@code [startIndex, startIndex + length)}.
	 * </p>
	 * @param input the sorted array of edges
	 * @param startIndex the index of the first edge to consider in {@code input}
	 * @param length the length of the subsequence to calculate
	 * @param output the edges that are sorted
	 * @param embedding the embedding
	 * @return the number of crossings in the subsequence.
	 */
	private long countBipartiteCrossings(Edge[] input, int startIndex, int length, 
			Embedding embedding, Edge[] output) {
		assert length > 0;
		
		if (length == 1) {
			output[0] = input[startIndex];
			return 0;
		}
		
		int leftSize = length / 2;
		int rightSize = length -  leftSize;
		
		Edge[] leftChunk = new Edge[leftSize];
		Edge[] rightChunk = new Edge[rightSize];
		
		long leftCount = countBipartiteCrossings(input, startIndex, leftSize, embedding, leftChunk);
		long rightCount = countBipartiteCrossings(input, startIndex + leftSize, rightSize, embedding, rightChunk);
		
		//merge
		long count = leftCount + rightCount;
		int i = 0, j = 0;
		while(i < leftSize && j < rightSize) {
			Edge leftEdge = leftChunk[i];
			Edge rightEdge = rightChunk[j];
			int leftStart = embedding.getPositionOfSmallerEndpoint(leftEdge);
			int leftEnd = embedding.getPositionOfLargerEndpoint(leftEdge);
			int rightStart = embedding.getPositionOfSmallerEndpoint(rightEdge);
			int rightEnd = embedding.getPositionOfLargerEndpoint(rightEdge);
			
			if (leftEnd < rightEnd || (leftEnd == rightEnd && leftStart >= rightStart)) {
				//left edge is smaller
				output[i+j] = leftEdge;
				i++;
			} else {
				//right edge is smaller.
				output[i+j] = rightEdge;
				j++;
				count += leftSize - i;
			}
		}
		
		// merge remaining edges
		for (; i < leftSize; i++) {
			output[rightSize + i] = leftChunk[i];
		}
		
		for (; j < rightSize; j++) {
			output[leftSize + j] = rightChunk[j];
		}
		
		return count;
	}

	@Override
	public String toString() {
		return "DivideAndConquerCrossingsCalculator";
	}	
}
