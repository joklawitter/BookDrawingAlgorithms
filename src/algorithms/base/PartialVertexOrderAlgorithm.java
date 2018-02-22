package algorithms.base;

import model.Embedding;

/**
 * Class implementing this interface should be able to compute a vertex order
 * only within a given range.
 *
 * @author Michael Wegner, Matthias Wolf, Jonathan Klawitter, 
 */
public interface PartialVertexOrderAlgorithm extends VertexOrderAlgorithm {

	/**
	 * Computes a partial vertex order based on the order of {@code embedding}.
	 * The vertices with spine indices smaller or equal to
	 * {@code keepUpToPosition} remain fixed.
	 * 
	 * @param embedding
	 * @param keepUpToPosition
	 */
	public void computePartialVertexOrder(Embedding embedding, int keepUpToPosition);

	/**
	 * Computes a partial vertex order based on the order of {@code embedding}.
	 * Only vertices with spine indices in the half open interval [begin..end)
	 * or intervals [0..end)[start..n).
	 * 
	 * @param embedding
	 * @param begin
	 * @param end
	 */
	public void computePartialVertexOrder(Embedding embedding, int begin, int end);

}
