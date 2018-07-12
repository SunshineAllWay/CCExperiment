package app;

import engine.NLcacheRunEngine;
import iounit.CorpusImporter;
import searchunit.BFContextSearcher;
import tokenunit.Tokensequence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public class NLcacheRunApp<K> implements CCRunApp<K> {
    private NLcacheRunEngine<K> runEngine;

    public NLcacheRunApp(int maxN, double gamma, File curFile) {
        runEngine = new NLcacheRunEngine(maxN, gamma, curFile);
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

    public ArrayList<K> completePostToken() {
        //retain the cache components
        runEngine.retrainCacheModel();
        CorpusImporter<K> corpusImporter = new CorpusImporter<>(0);
        ArrayList<K> currentFileTokenStream = corpusImporter.importCorpusFromSingleFile(runEngine.getCurFile());
        int length = currentFileTokenStream.size();
        int prefixLength = Math.min(length, runEngine.maxN);

        if (length == 0) {
            return new ArrayList<>();
        }

        ArrayList<K> tailStream = new ArrayList<>();
        tailStream.addAll(currentFileTokenStream.subList(length - prefixLength, length));
        return completePostToken(new Tokensequence<>(tailStream));
    }
}
