package org.apache.lucene.search;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
class BooleanScorer2 extends Scorer {
  private final List<Scorer> requiredScorers;
  private final List<Scorer> optionalScorers;
  private final List<Scorer> prohibitedScorers;
  private class Coordinator {
    float[] coordFactors = null;
    int maxCoord = 0; 
    int nrMatchers; 
    void init() { 
      coordFactors = new float[maxCoord + 1];
      Similarity sim = getSimilarity();
      for (int i = 0; i <= maxCoord; i++) {
        coordFactors[i] = sim.coord(i, maxCoord);
      }
    }
  }
  private final Coordinator coordinator;
  private final Scorer countingSumScorer;
  private final int minNrShouldMatch;
  private int doc = -1;
  public BooleanScorer2(Similarity similarity, int minNrShouldMatch,
      List<Scorer> required, List<Scorer> prohibited, List<Scorer> optional) throws IOException {
    super(similarity);
    if (minNrShouldMatch < 0) {
      throw new IllegalArgumentException("Minimum number of optional scorers should not be negative");
    }
    coordinator = new Coordinator();
    this.minNrShouldMatch = minNrShouldMatch;
    optionalScorers = optional;
    coordinator.maxCoord += optional.size();
    requiredScorers = required;
    coordinator.maxCoord += required.size();
    prohibitedScorers = prohibited;
    coordinator.init();
    countingSumScorer = makeCountingSumScorer();
  }
  private class SingleMatchScorer extends Scorer {
    private Scorer scorer;
    private int lastScoredDoc = -1;
    private float lastDocScore = Float.NaN;
    SingleMatchScorer(Scorer scorer) {
      super(scorer.getSimilarity());
      this.scorer = scorer;
    }
    @Override
    public float score() throws IOException {
      int doc = docID();
      if (doc >= lastScoredDoc) {
        if (doc > lastScoredDoc) {
          lastDocScore = scorer.score();
          lastScoredDoc = doc;
        }
        coordinator.nrMatchers++;
      }
      return lastDocScore;
    }
    @Override
    public int docID() {
      return scorer.docID();
    }
    @Override
    public int nextDoc() throws IOException {
      return scorer.nextDoc();
    }
    @Override
    public int advance(int target) throws IOException {
      return scorer.advance(target);
    }
  }
  private Scorer countingDisjunctionSumScorer(final List<Scorer> scorers,
      int minNrShouldMatch) throws IOException {
    return new DisjunctionSumScorer(scorers, minNrShouldMatch) {
      private int lastScoredDoc = -1;
      private float lastDocScore = Float.NaN;
      @Override public float score() throws IOException {
        int doc = docID();
        if (doc >= lastScoredDoc) {
          if (doc > lastScoredDoc) {
            lastDocScore = super.score();
            lastScoredDoc = doc;
          }
          coordinator.nrMatchers += super.nrMatchers;
        }
        return lastDocScore;
      }
    };
  }
  private static final Similarity defaultSimilarity = Similarity.getDefault();
  private Scorer countingConjunctionSumScorer(List<Scorer> requiredScorers) throws IOException {
    final int requiredNrMatchers = requiredScorers.size();
    return new ConjunctionScorer(defaultSimilarity, requiredScorers) {
      private int lastScoredDoc = -1;
      private float lastDocScore = Float.NaN;
      @Override public float score() throws IOException {
        int doc = docID();
        if (doc >= lastScoredDoc) {
          if (doc > lastScoredDoc) {
            lastDocScore = super.score();
            lastScoredDoc = doc;
          }
          coordinator.nrMatchers += requiredNrMatchers;
        }
        return lastDocScore;
      }
    };
  }
  private Scorer dualConjunctionSumScorer(Scorer req1, Scorer req2) throws IOException { 
    return new ConjunctionScorer(defaultSimilarity, new Scorer[]{req1, req2});
  }
  private Scorer makeCountingSumScorer() throws IOException { 
    return (requiredScorers.size() == 0)
          ? makeCountingSumScorerNoReq()
          : makeCountingSumScorerSomeReq();
  }
  private Scorer makeCountingSumScorerNoReq() throws IOException { 
    int nrOptRequired = (minNrShouldMatch < 1) ? 1 : minNrShouldMatch;
    Scorer requiredCountingSumScorer;
    if (optionalScorers.size() > nrOptRequired)
      requiredCountingSumScorer = countingDisjunctionSumScorer(optionalScorers, nrOptRequired);
    else if (optionalScorers.size() == 1)
      requiredCountingSumScorer = new SingleMatchScorer(optionalScorers.get(0));
    else
      requiredCountingSumScorer = countingConjunctionSumScorer(optionalScorers);
    return addProhibitedScorers(requiredCountingSumScorer);
  }
  private Scorer makeCountingSumScorerSomeReq() throws IOException { 
    if (optionalScorers.size() == minNrShouldMatch) { 
      ArrayList<Scorer> allReq = new ArrayList<Scorer>(requiredScorers);
      allReq.addAll(optionalScorers);
      return addProhibitedScorers(countingConjunctionSumScorer(allReq));
    } else { 
      Scorer requiredCountingSumScorer =
            requiredScorers.size() == 1
            ? new SingleMatchScorer(requiredScorers.get(0))
            : countingConjunctionSumScorer(requiredScorers);
      if (minNrShouldMatch > 0) { 
        return addProhibitedScorers( 
                      dualConjunctionSumScorer( 
                              requiredCountingSumScorer,
                              countingDisjunctionSumScorer(
                                      optionalScorers,
                                      minNrShouldMatch)));
      } else { 
        return new ReqOptSumScorer(
                      addProhibitedScorers(requiredCountingSumScorer),
                      optionalScorers.size() == 1
                        ? new SingleMatchScorer(optionalScorers.get(0))
                        : countingDisjunctionSumScorer(optionalScorers, 1));
      }
    }
  }
  private Scorer addProhibitedScorers(Scorer requiredCountingSumScorer) throws IOException
  {
    return (prohibitedScorers.size() == 0)
          ? requiredCountingSumScorer 
          : new ReqExclScorer(requiredCountingSumScorer,
                              ((prohibitedScorers.size() == 1)
                                ? prohibitedScorers.get(0)
                                : new DisjunctionSumScorer(prohibitedScorers)));
  }
  @Override
  public void score(Collector collector) throws IOException {
    collector.setScorer(this);
    while ((doc = countingSumScorer.nextDoc()) != NO_MORE_DOCS) {
      collector.collect(doc);
    }
  }
  @Override
  protected boolean score(Collector collector, int max, int firstDocID) throws IOException {
    doc = firstDocID;
    collector.setScorer(this);
    while (doc < max) {
      collector.collect(doc);
      doc = countingSumScorer.nextDoc();
    }
    return doc != NO_MORE_DOCS;
  }
  @Override
  public int docID() {
    return doc;
  }
  @Override
  public int nextDoc() throws IOException {
    return doc = countingSumScorer.nextDoc();
  }
  @Override
  public float score() throws IOException {
    coordinator.nrMatchers = 0;
    float sum = countingSumScorer.score();
    return sum * coordinator.coordFactors[coordinator.nrMatchers];
  }
  @Override
  public int advance(int target) throws IOException {
    return doc = countingSumScorer.advance(target);
  }
}
