package app;

import engine.NLngramRunEngine;
import searchunit.BFContextSearcher;
import tokenunit.Tokensequence;

import java.util.ArrayList;
import java.util.HashSet;

public class NLngramRunApp<K> implements CCRunApp<K> {
    private NLngramRunEngine<K> runEngine;

    public NLngramRunApp(int maxN) {
        runEngine = new NLngramRunEngine(maxN, 1);
        runEngine.preAction();
    }

    public ArrayList<K> completePostToken(Tokensequence<K> nseq) {
        BFContextSearcher<K> fuzzySearcher = new BFContextSearcher(runEngine);
        ArrayList<Tokensequence<K>> similarSequenceList = fuzzySearcher.getSimilarSequences(nseq);
        ArrayList<K> tokenCandidatesList = new ArrayList<>();
        HashSet<K> tokenCandidatesSet = new HashSet<>();

        for (int i = 0; i < similarSequenceList.size(); i++) {
            ArrayList<K> ls = runEngine.completePostToken(similarSequenceList.get(i));
            if (ls.size() > 0) {
                tokenCandidatesSet.add(ls.get(0));
            }
        }
        tokenCandidatesList.addAll(tokenCandidatesSet);
        return tokenCandidatesList;
    }
}
