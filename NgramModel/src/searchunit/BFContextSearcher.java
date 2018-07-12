package searchunit;

import engine.CCRunEngine;
import engine.NLcacheRunEngine;
import engine.NLngramRunEngine;
import model.BasicNGram;
import model.CacheModel;
import model.CacheNGram;
import tokenunit.Tokensequence;

import java.util.*;

public class BFContextSearcher<K> implements ContextSearcher<K> {
    CCRunEngine<K> CCEngine;    //code completion engine
    double stretchLowFactor;   //lower bound of length stretch of sequence
    double stretchHighFactor;  //high bound of length stretch of sequence

    /**
     * Construct an object of BFContextSearcher(context searcher based on BFS)
     * @param pCCEngine Code Completion Engine
     * @param factor1 lower bound of length stretch of sequence
     * @param factor2 high bound of length stretch of sequence
     */
    public BFContextSearcher(CCRunEngine<K> pCCEngine, double factor1, double factor2) {
        this.CCEngine = pCCEngine;
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

            int depth = 1;
            boolean flag = false;
            while (depth < maxDepth && !flag) {
                flag = true;
                HashSet<ArrayList<K>> newListSet = new HashSet<>();
                for (ArrayList<K> list : listSet) {
                    ArrayList<K> oplist = CCEngine.completePostToken(new Tokensequence<>(list));
                    if (oplist.size() == 0) {
                        continue;
                    }
                    flag = false;
                    ArrayList<K> nextTokenList = (ArrayList<K>)oplist.clone();
                    int remainNum = Math.min(2, nextTokenList.size());

                    for (int j = 0; j < remainNum; j++) {
                        ArrayList<K> newList = (ArrayList<K>)list.clone();
                        newList.add(nextTokenList.get(j));
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
        HashMap<Tokensequence<K>, Double> seqToSimilarityMap = new HashMap<>();
        for (int i = 0; i < similarSequenceList.size(); i++) {
            double similarity = calculateSequenceSimilarity(similarSequenceList.get(i), seq);
            seqToSimilarityMap.put(similarSequenceList.get(i), new Double(similarity));
        }

        similarSequenceList.clear();
        Set<Map.Entry<Tokensequence<K>, Double>> seqToSimilaritySet = seqToSimilarityMap.entrySet();

        while(!seqToSimilaritySet.isEmpty()) {
            double maxSimilarity = 0.0;
            Tokensequence<K> closestSequence = null;
            Map.Entry<Tokensequence<K>, Double> recordEntry = null;
            for (Map.Entry<Tokensequence<K>, Double> entry : seqToSimilaritySet) {
                if (entry.getValue() > maxSimilarity) {
                    recordEntry = entry;
                    maxSimilarity = entry.getValue();
                }
            }
            similarSequenceList.add(recordEntry.getKey());
            seqToSimilaritySet.remove(recordEntry);
        }

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
        ArrayList<K> list1 = seq1.getSequence();
        ArrayList<K> list2 = seq2.getSequence();
        int len1 = list1.size();
        int len2 = list2.size();
        int[][] dp = new int[len1][len2];

        dp[0][0] = (list1.get(0) == list2.get(0)) ? 1 : 0;
        for (int i = 0; i < len1; i++) {
            dp[i][0] = (list1.get(i) == list2.get(i)) ? 1 : dp[i - 1][0];
        }
        for (int i = 0; i < len2; i++) {
            dp[0][i] = (list1.get(i) == list2.get(i)) ? 1: dp[0][i - 1];
        }
        for (int i = 1; i < len1; i++) {
            for (int j = 1; j < len2; j++) {
                if (list1.get(i) == list2.get(j)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i][j - 1], dp[i - 1][j]);
                }
            }
        }

        int commonSubseqLength = dp[len1 - 1][len2 - 1];
        similarity = similarity * commonSubseqLength / len1;
        similarity = similarity * commonSubseqLength / len2;
        return similarity;
    }
}
