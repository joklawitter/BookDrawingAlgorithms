package algorithms.crossingCalculation;

import model.Embedding;

public interface ICrossingCalculator {

	public long calculateNumberOfCrossings(Embedding embedding);
	
	public long calculateNumberOfCrossingsOnPage(Embedding embedding, int pageIndex);
}
