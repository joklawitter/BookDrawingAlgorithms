# BookDrawingAlgorithms
Collection of algorithms for crossing minimisation in book drawings, as used for a paper.

## Algorithms
This repository contains several algorithms for crossing minimisation in book drawings.
- constructive heuristics, as described in [1] and [2], 
- local search heuristics (greedy and simulated annealing after [3](https://github.com/josefcibulka/book-embedder))
For code of other approaches (evolutionary, force-based, SAT-solver, MAX-SAT-solver) please contanct me.

The repository further contains different algorithms to compute the number of crossings in a book drawing (see [1]).

## Disclaimer
I have **not** implemented all of the algorithms, and have implemented and altered some only partially. A big portion of the code was created in a university group project.  Co-authors are listed in the respective files in the Javadoc header. 
I do **not** take responsibility for the algorithms correctness.

## References
- [1] J. Klawitter, *Algorithms for crossing minimisation in book drawings*, Master Thesis, 2016.
- [2] J. Klawitter, T. Mchedlidze, M. Nöllenburg, *Experimental Evaluation of Book Drawing Algorithms*, Proceedings GD17, 2017.
- [3] J. Cibulka, [*book-embedder*, repository](https://github.com/josefcibulka/book-embedder)
