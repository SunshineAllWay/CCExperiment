package model;

import tokenunit.Token;
import tokenunit.Tokensequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class CacheModel {
    public int n;              //n>=2, n = 2 in bigram; n = 3 in trigram
    public int modelType;      //0: natural language model;   1: programming language model
    public double gamma;       //concentration parameter

    private ArrayList<String> cacheTokenStream;  //token stream in the cache files
    private BasicNGram  ngramModel;  //n-gram model
    private CacheNGram ncacheModel;  //cache n-gram model


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
        this.ngramModel = new BasicNGram(n, modelType);
        this.ncacheModel = new CacheNGram(n, modelType);
    }

    /**
     * Train the ngramModel
     * @param corpusTokenStream token stream in the corpus
     */
    public void preAction(ArrayList<String> corpusTokenStream) {
        ngramModel.preAction(corpusTokenStream);
    }

    /**
     * update cacheTokenStream with token stream in cache files
     * @param newCacheTokenStream token stream in cache files
     */
    public void updateCacheTokenStream(ArrayList<String> newCacheTokenStream) {
        cacheTokenStream = (ArrayList<String>)newCacheTokenStream.clone();
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
    public double getRelativeProbability(Tokensequence nseq, Token t) {
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
    public BasicNGram getNgramModel() {
        return this.ngramModel;
    }

    /**
     * Return cache component
     * @return cache component
     */
    public CacheNGram getNcacheModel() {
        return this.ncacheModel;
    }
}
