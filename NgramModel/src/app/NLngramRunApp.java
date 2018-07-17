package app;

import engine.NLngramRunEngine;
import searchunit.BFContextSearcher;
import tokenunit.Token;
import tokenunit.Tokensequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NLngramRunApp<K> implements CCRunApp<K> {
    private NLngramRunEngine<K> runEngine;

    public NLngramRunApp(int maxN) {
        runEngine = new NLngramRunEngine(maxN, 1);
        runEngine.preAction();
    }

    public ArrayList<K> completePostToken(Tokensequence<K> nseq) {
        ArrayList<K> tokenCandidatesList = new ArrayList<>();
        tokenCandidatesList = runEngine.completePostToken(nseq);
        if (tokenCandidatesList.size() != 0) {
            return tokenCandidatesList;
        }

        BFContextSearcher<K> fuzzySearcher = new BFContextSearcher(runEngine);
        ArrayList<Tokensequence<K>> similarSequenceList = fuzzySearcher.getSimilarSequences(new Tokensequence<>((ArrayList<K>)nseq.getSequence().clone()));


        HashMap<K, Double> probMap = new HashMap<>();
        HashSet<K> elemSet = new HashSet<>();
        for (int i = 0; i < similarSequenceList.size(); i++) {
            ArrayList<K> ls = runEngine.completePostToken(similarSequenceList.get(i));
            if (ls.size() > 0) {
                if (elemSet.contains(ls.get(0))) {
                    continue;
                }
                elemSet.add(ls.get(0));
                tokenCandidatesList.add(ls.get(0));
                ArrayList<K> tmpList = (ArrayList<K>)nseq.getSequence().clone();
                tmpList.add(ls.get(0));
                Tokensequence<K> reseq = new Tokensequence<>(tmpList);
                probMap.put(ls.get(0), new Double(runEngine.calculateProbability(reseq)));
            }
        }

        K[] tokenCandidatesArray = (K[])tokenCandidatesList.toArray();
        ArrayList<K> sortedTokenCandidatesList = new ArrayList<>();

        for (int i = 0; i < tokenCandidatesList.size(); i++) {
            double maxProb = probMap.get(tokenCandidatesArray[i]).doubleValue();
            K elem = tokenCandidatesArray[i];
            int index = 0;
            for (int j = 1; j < tokenCandidatesList.size(); j++) {
                double tmpProb = probMap.get(tokenCandidatesArray[j]).doubleValue();
                if (maxProb < tmpProb) {
                    elem = tokenCandidatesArray[j];
                    maxProb = tmpProb;
                    index = j;
                }
            }
            sortedTokenCandidatesList.add(elem);
            K tmpElem = tokenCandidatesArray[i];
            tokenCandidatesArray[i] = elem;
            tokenCandidatesArray[index] = tmpElem;
        }

        return tokenCandidatesList;
    }
}
