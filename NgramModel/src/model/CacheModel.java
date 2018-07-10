package model;

import tokenunit.Token;
import tokenunit.Tokensequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CacheModel<K> {
    public int n;              //n>=2, n = 2 in bigram; n = 3 in trigram
    public int modelType;      //0: natural language model;   1: programming language model
    public double gamma;       //concentration parameter

    private ArrayList<K> cacheTokenStream;  //token stream in the cache files
    private BasicNGram<K>  ngramModel;  //n-gram model
    private CacheNGram<K> ncacheModel;  //cache n-gram model


    /**
     * Construct an object of CacheModel
     * @param n the length of gram
     * @param modelType the type of model, type = 0 when the model process natural language, type = 1 for PL
     * @param gamma concentration parameter which is between 0 and infinity
     */
    public CacheModel(int n, int modelType, double gamma) {
        this.n = n;
        this.modelType = modelType;
        this.gamma = gamma;
        this.ngramModel = new BasicNGram<>(n, modelType);
        this.ncacheModel = new CacheNGram<>(n, modelType);
    }

    /**
     * Train the ngramModel
     * @param corpusTokenStream token stream in the corpus
     */
    public void preAction(ArrayList<K> corpusTokenStream) {
        ngramModel.preAction(corpusTokenStream);
    }

    /**
     * update cacheTokenStream with token stream in cache files
     * @param newCacheTokenStream token stream in cache files
     */
    public void updateCacheTokenStream(ArrayList<K> newCacheTokenStream) {
        cacheTokenStream = (ArrayList<K>)newCacheTokenStream.clone();
    }

    /**
     * update cache n-gram model with token stream in cache files
     */
    public void updateCacheComponent() {
        ncacheModel.preAction(cacheTokenStream);
    }

    /**
     * Calculate the relative probability of a given token being the post token of token sequence
     * @param nseq prefix token sequence
     * @param t last token
     * @return relative probability of t being the post token of nseq
     */
    public double getRelativeProbability(Tokensequence<K> nseq, Token<K> t) {
        double p1 = ngramModel.getRelativeProbability(nseq, t);
        double p2 = ncacheModel.getRelativeProbability(nseq, t);
        int h = ncacheModel.getSeqWithSpecificPrefix(nseq);

        if (h == 0) {
            return p1;
        }

        double p = (gamma * p1 + h * p2) / (gamma + h);
        return p;
    }

    /**
     * Return n-gram model component
     * @return n-gram model component
     */
    public BasicNGram<K> getNgramModel() {
        return this.ngramModel;
    }

    /**
     * Return cache component
     * @return cache component
     */
    public CacheNGram<K> getNcacheModel() {
        return this.ncacheModel;
    }
}
