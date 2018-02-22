package algorithms.heuristics.vertexorder;

import java.util.concurrent.ThreadLocalRandom;
import model.Embedding;
import algorithms.base.VertexOrderAlgorithm;

/**
 * Implementation of the IDFS algorithm. A random vertex v is chosen which appears at index i_v on the spine. 
 * The vertices which have a smaller spine index are left unchanged (DFS path from the former root node to v)
 * while the vertices which have a spine index >= i_v take part in an rdfs starting at vertex v.
 * 
 * @author Michael
 * @see Bansal, Richa, et al. "An evolutionary algorithm for the 2-page crossing number problem." Evolutionary Computation, 2008.
 *
 */
public class IdfsAlgorithm implements VertexOrderAlgorithm {

	@Override
	public void computeVertexOrder(Embedding embedding) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		int rootIdx = random.nextInt(embedding.getGraph().getN());
		
		RandomDFSAlgorithm rdfs = new RandomDFSAlgorithm();
		rdfs.computePartialVertexOrder(embedding, rootIdx-1);
	}
}
