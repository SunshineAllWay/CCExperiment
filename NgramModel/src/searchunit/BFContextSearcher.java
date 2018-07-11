package searchunit;

import model.BasicNGram;
import tokenunit.Tokensequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class BFContextSearcher<K> implements ContextSearcher<K> {
    BasicNGram<K> model;      //basic n-gram model
    double stretchLowFactor;   //lower bound of length stretch of sequence
    double stretchHighFactor; //high bound of length stretch of sequence

    /**
     * Construct an object of BFContextSearcher(context searcher based on BFS)
     * @param pmodel basic n-gram model
     * @param factor1 lower bound of length stretch of sequence
     * @param factor2 high bound of length stretch of sequence
     */
    public BFContextSearcher(BasicNGram<K> pmodel, double factor1, double factor2) {
        this.model = pmodel;
        this.stretchLowFactor = factor1;
        this.stretchHighFactor =  factor2;
    }

    /**
     * Retrieve the similar sequences compared to a given sequence
     * @param seq sequence input
     * @return similar sequences
     */
    public ArrayList<Tokensequence<K>> getSimilarSequences(Tokensequence<K> seq) {
        ArrayList<Tokensequence<K>> similarSequenceList = new ArrayList<>();
        ArrayList<K> ls = seq.getSequence();
        HashSet<ArrayList<K>> listSet = new HashSet<>();
        int minDepth = (int)(ls.size() * stretchLowFactor);
        int maxDepth = (int) (ls.size() * stretchHighFactor);

        for (int i = 0; i < ls.size(); i++) {
            K elem = ls.get(i);

            ArrayList<K> singleElemList  = new ArrayList<>();
            singleElemList.add(elem);
            listSet.add(singleElemList);

            int depth = 0;
            while (depth < maxDepth) {
                HashSet<ArrayList<K>> newListSet = new HashSet<>();
                for (ArrayList<K> list : listSet) {
                    Optional<HashMap<K, Integer>> opmap = model.getBasicNGramCandidates(new Tokensequence<>(list));
                    if (!opmap.isPresent()) {
                        continue;
                    }
                    HashMap<K, Integer> map = opmap.get();
                    HashSet<K> nextTokenSet = (HashSet<K>)map.keySet();

                    for (K nextToken : nextTokenSet) {
                        ArrayList<K> newList = new ArrayList<>();
                        newList = (ArrayList<K>)list.clone();
                        newList.add(nextToken);
                        newListSet.add(newList);
                    }
                }
                listSet =  newListSet;
                depth++;
            }
        }

        for (ArrayList<K> l : listSet) {
            if (l.size() > minDepth) {
                similarSequenceList.add(new Tokensequence<>(l));
            }
        }

        //Sort by the similarity
        return similarSequenceList;
    }

    /**
     * Calculate the similarity between two sequences
     * @param seq1 sequence 1
     * @param seq2 sequence 2
     * @return the similarity between seq1 and seq2
     */
    public double calculateSequenceSimilarity(Tokensequence<K> seq1, Tokensequence<K> seq2) {
        double similarity = 1.0;
        return similarity;
    }
}
