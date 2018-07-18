package app;

import engine.NgramRunEngine;
import searchunit.BFContextSearcher;
import tokenunit.Tokensequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NgramRunApp implements CCRunApp {
    private NgramRunEngine runEngine;

    public NgramRunApp(int type, int maxN) {
        runEngine = new NgramRunEngine(type, maxN, 1);
        runEngine.preAction();
    }

    public ArrayList<String> completePostToken(Tokensequence nseq) {
        ArrayList<String> tokenCandidatesList = runEngine.completePostToken(nseq);
        if (tokenCandidatesList.size() != 0) {
            return tokenCandidatesList;
        }

        BFContextSearcher fuzzySearcher = new BFContextSearcher(runEngine);
        ArrayList<Tokensequence> similarSequenceList = fuzzySearcher.getSimilarSequences(new Tokensequence((ArrayList<String>)nseq.getSequence().clone()));


        HashMap<String, Double> probMap = new HashMap<>();
        HashSet<String> elemSet = new HashSet<>();
        for (int i = 0; i < similarSequenceList.size(); i++) {
            ArrayList<String> ls = runEngine.completePostToken(similarSequenceList.get(i));
            if (ls.size() > 0) {
                if (elemSet.contains(ls.get(0))) {
                    continue;
                }
                elemSet.add(ls.get(0));
                tokenCandidatesList.add(ls.get(0));
                ArrayList<String> tmpList = (ArrayList<String>)nseq.getSequence().clone();
                tmpList.add(ls.get(0));
                Tokensequence reseq = new Tokensequence(tmpList);
                probMap.put(ls.get(0), new Double(runEngine.calculateProbability(reseq)));
            }
        }

        String[] tokenCandidatesArray = (String[])tokenCandidatesList.toArray();
        ArrayList<String> sortedTokenCandidatesList = new ArrayList<>();

        for (int i = 0; i < tokenCandidatesList.size(); i++) {
            double maxProb = probMap.get(tokenCandidatesArray[i]).doubleValue();
            String elem = tokenCandidatesArray[i];
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
            String tmpElem = tokenCandidatesArray[i];
            tokenCandidatesArray[i] = elem;
            tokenCandidatesArray[index] = tmpElem;
        }

        return tokenCandidatesList;
    }
}
