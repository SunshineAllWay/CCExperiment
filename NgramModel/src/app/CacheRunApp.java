package app;

import engine.CacheRunEngine;
import iounit.CorpusImporter;
import searchunit.BFContextSearcher;
import tokenunit.Tokensequence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CacheRunApp implements CCRunApp{
    private CacheRunEngine runEngine;

    public CacheRunApp(int type, int maxN, double gamma, File curFile) {
        runEngine = new CacheRunEngine(type, maxN, gamma, curFile);
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

        String[] tokenCandidatesArray = new String[tokenCandidatesList.size()];
        for (int i = 0; i < tokenCandidatesList.size(); i++) {
            tokenCandidatesArray[i] = tokenCandidatesList.get(i);
        }
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

    public ArrayList<String> completePostToken() {
        //retain the cache components

        runEngine.retrainCacheModel();
        int length = runEngine.cacheTokenStream.size();
        int prefixLength = Math.min(length, runEngine.maxN);

        if (length == 0) {
            return new ArrayList<>();
        }

        ArrayList<String> tailStream = new ArrayList<>();
        tailStream.addAll(runEngine.cacheTokenStream.subList(length - prefixLength, length));
        return completePostToken(new Tokensequence(tailStream));
    }

    public CacheRunEngine getRunEngine() {
        return runEngine;
    }
}
