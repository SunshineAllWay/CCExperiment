package engine;

import iounit.CorpusImporter;
import model.BasicNGram;
import model.CacheModel;
import tokenunit.Token;
import tokenunit.Tokensequence;

import java.io.File;
import java.util.*;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.min;

public class CacheRunEngine implements CCRunEngine{
    public int type;                          //model type: 1 for program language, 0 for natural language
    public int maxN;                          //maximal parameter in gramArray
    private double gamma;                      //concentration parameter
    private CacheModel[] cacheModelArray;   //unigram, bigram, trigram or unigram ...ngram

    public ArrayList<String> corpusTokenStream;  //token stream in the corpus
    public ArrayList<String> cacheTokenStream;   //token stream in the cache files

    private ArrayList<File> cacheFileList;   //the list of cache files
    private File curFile;                    //current edited file

    /**
     * Construct an object of NLcacheRunEngine
     * @param ptype 1 for program language, 0 for natural language
     * @param n the length of gram
     * @param g concentration parameter
     */
    public CacheRunEngine(int ptype, int n, double g, File pCurFile) {
        this.type = ptype;
        this.maxN = n;
        this.gamma = g;
        cacheModelArray = new CacheModel[maxN];

        CorpusImporter corpusImporter = new CorpusImporter(type);
        corpusTokenStream = corpusImporter.importTrainingCorpusFromBase();
        cacheTokenStream = new ArrayList<>();

        cacheFileList = new ArrayList<>();
        curFile = pCurFile;

        for (int i = 0; i < maxN; i++) {
            cacheModelArray[i] =  new CacheModel(i + 1, type, gamma, cacheFileList, curFile);
        }
    }

    /**
     * Construct an object of NLcacheRunEngine, max length of gram is 3
     * @param ptype 1 for program language, 0 for natural language
     * @param g concentration parameter
     * @param pCurFile current editing file
     */
    public CacheRunEngine(int ptype, double g, File pCurFile) {
        this.type = ptype;
        this.maxN = 3;
        this.gamma = g;
        cacheModelArray = (CacheModel[]) new CacheModel[maxN];

        CorpusImporter corpusImporter = new CorpusImporter(type);
        corpusTokenStream = corpusImporter.importTrainingCorpusFromBase();
        cacheTokenStream = new ArrayList<>();

        for (int i = 0; i < maxN; i++) {
            cacheModelArray[i] =  new CacheModel(i + 1, type, gamma, cacheFileList, curFile);
        }

        cacheFileList = new ArrayList<>();
        curFile = pCurFile;
    }

    /**
     * Train the n-gram model component during the preparation phase
     */
    public void preAction() {
        System.out.println("Cache engine warms up");
        for (int i = 0; i < cacheModelArray.length; i++) {
            System.out.println("---------------------------------");
            System.out.print(Integer.toString(i + 1) + "-gram");
            System.out.println(" single pre-action beginning");
            cacheModelArray[i].getNgramModel().preAction(corpusTokenStream);
            System.out.print(Integer.toString(i + 1) + "-gram");
            System.out.println(" single pre-action finished");
            System.out.println("---------------------------------");
            System.out.println();
        }
        System.out.println("Cache engine is prepared");
        retrainCacheModel();
    }

    /**
     * Infer and recommend the post token for current file
     * @param seq specify the token sequence
     * @return the most likely post token of the sequence
     */
    public ArrayList<String> completePostToken(Tokensequence seq) {
        //retain the cache components
        retrainCacheModel();
        int length = seq.getSequence().size();
        Tokensequence nseq = new Tokensequence((ArrayList<String>)seq.getSequence().clone());
        int prefixLength = Math.min(length, maxN);

        ArrayList<String> candidatesList = new ArrayList<>();

        if (length == 0) {
            return candidatesList;
        }

        ArrayList<String> tailStream = new ArrayList<>();
        tailStream.addAll(seq.getSequence().subList(length - prefixLength, length));

        //get the candidates from 2-gram model
        ArrayList<String> miniTailStream = new ArrayList<>();
        miniTailStream.addAll(tailStream.subList(prefixLength - 1, prefixLength));
        Tokensequence lastSeq = new Tokensequence(miniTailStream);
        HashMap<String, Double> elemCntMap = new HashMap<>();

        if (cacheModelArray[1].getNgramModel().getModel().get(lastSeq) != null) {
            elemCntMap.putAll(cacheModelArray[1].getNgramModel().getModel().get(lastSeq));
        }
        if (cacheModelArray[1].getNcacheModel().getModel().get(lastSeq) != null) {
            elemCntMap.putAll(cacheModelArray[1].getNcacheModel().getModel().get(lastSeq));
        }

        if (elemCntMap.size() == 0) {
            return candidatesList;
        }

        //HashMap<String, Integer> elemCntMap = candiadates.get();
        Iterator<Map.Entry<String, Double>> it = elemCntMap.entrySet().iterator();
        double maxProb = 0.0;

        HashMap<String, Double> elemProbMap = new HashMap<>();
        while(it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            ArrayList<String> ls = (ArrayList<String>)nseq.getSequence().clone();
            ls.add(entry.getKey());
            double prob = calculateProbability(new Tokensequence(ls));
            elemProbMap.put(entry.getKey(), prob);
        }

        Set<Map.Entry<String, Double>> elemProbSet = elemProbMap.entrySet();


        while(!elemProbSet.isEmpty()) {
            double maxProbablity = 0.0;
            Tokensequence closestSequence = null;
            Map.Entry<String, Double> recordEntry = null;
            for (Map.Entry<String, Double> entry : elemProbSet) {
                if (entry.getValue() > maxProbablity) {
                    recordEntry = entry;
                    maxProbablity = entry.getValue();
                }
            }
            candidatesList.add(recordEntry.getKey());
            elemProbSet.remove(recordEntry);
        }

        return candidatesList;
    }


    /**
     * Infer and recommend the post token for current file
     * @return the most likely post token of nseq(can be null)
     */
    public ArrayList<String> completePostToken() {
        //retain the cache components
        retrainCacheModel();
        CorpusImporter corpusImporter = new CorpusImporter(type);
        ArrayList<String> currentFileTokenStream = corpusImporter.importCorpusFromSingleFile(curFile);
        int length = currentFileTokenStream.size();
        int prefixLength = Math.min(length, maxN);

        if (length == 0) {
            return new ArrayList<>();
        }

        ArrayList<String> tailStream = new ArrayList<>();
        tailStream.addAll(currentFileTokenStream.subList(length - prefixLength, length));
        ArrayList<String> candidatesList = completePostToken(new Tokensequence(tailStream));
        return candidatesList;
    }

    private double getProbInUnaryGram(String elem) {
        ArrayList<String> ls = new ArrayList<>();
        ls.add(elem);
        Tokensequence seq = new Tokensequence(ls);

        if (!getNgramArray()[0].getModel().containsKey(seq)) {
            return 1.0 / getNgramArray()[0].getModel().size();
        }

        double capturedCount = getNgramArray()[0].getModel().get(seq).get(null).doubleValue();
        int totalCount = 0;
        Iterator<Map.Entry<Tokensequence, HashMap<String, Double>>> it = getNgramArray()[0].getModel().entrySet().iterator();
        while(it.hasNext()) {
            totalCount += it.next().getValue().get(null);
        }

        return (capturedCount * 1.0 / totalCount);
    }

    /**
     * Estimate the probability of the sentence
     * @param nseq: Token sequence(sentence)
     * @return: The probability of the sentence
     */

    /** maxN = 3
     * using refine unit such as Lidstone Smoothing
     * P(a1 a2 a3 a4 ... ak ... a_(n-1), an)
     * = p(a1) * p(a2 | a1) * p(a3 | a1 a2) * ... * p(an | a_(n-2) a_(n-1))
     * = p(a1) * (p(a1 a2) / p(a1)) * (p(a1 a2 a3) / p(a1 a2)) * ... * p(a_(n-2) a_(n-1) a_n) / p(a_(n-2) a_(n-1))
     * = p(a1) * p(a1 a2) * p(a1 a2 a3) * p(a2 a3 a4) * ... * p(a_(n-2) a_(n-1) a_(n)) /
     *   p(a1) * p(a1 a2) * p(a2 a3) * p(a3 a4) * p(a4 a5) * ... * p(a_(n-2) a_(n-1))
     */
    public double calculateProbability(Tokensequence nseq) {
        int seqlength = nseq.length();
        double logprob = 0.0;

        ArrayList<String> nseqContent = nseq.getSequence();
        int i;
        int maxGramLength = min(maxN, seqlength);

        //TODO: probability of 1-gram, assume all tokens in the sequence appear in the training list
        logprob += log(getProbInUnaryGram(nseq.getSequence().get(0)));
        for (i = 1; i < maxGramLength; i++) {
            ArrayList<String> ls = new ArrayList<>();
            ls.addAll(nseqContent.subList(0, i));
            Tokensequence subTokenSeq = new Tokensequence(ls);
            logprob += log(cacheModelArray[i].getRelativeProbability(subTokenSeq, new Token(nseqContent.get(i))));
        }
        for (i = maxN; i < seqlength; i++) {
            ArrayList<String> ls = new ArrayList<>();
            ls.addAll(nseqContent.subList(i - maxN + 1, i));
            Tokensequence subTokenSeq = new Tokensequence(ls);
            logprob += log(cacheModelArray[maxN - 1].getRelativeProbability(subTokenSeq, new Token(nseqContent.get(i))));
        }

        double prob = exp(logprob);

        return prob;
    }

    /**
     * Reload the cache when handling the request to complete the post token of the sequence
     */
    public void reloadCacheContent() {
        CorpusImporter corpusImporter = new CorpusImporter(type);
        int fileNum = cacheFileList.size();
        cacheTokenStream = new ArrayList<>();

        for (int i = 0; i < fileNum; i++) {
            cacheTokenStream.addAll(corpusImporter.importCorpusFromSingleFile(cacheFileList.get(i)));
        }

        cacheTokenStream.addAll(corpusImporter.importCorpusFromSingleFile(curFile));
    }

    /**
     * Update the list of cache files after finishing edition of current file
     * @param newCurFile next file which will be edit, can be null
     */
    public void updateCacheList(File newCurFile) {
        cacheFileList.add(curFile);
        curFile = newCurFile;
    }

    public void addCacheFileList(File file) {
        cacheFileList.add(file);
    }

    public void retrainCacheModel() {
        reloadCacheContent();
        for (int i = 0; i < maxN; i++) {
            cacheModelArray[i].updateCacheTokenStream(cacheTokenStream);
            cacheModelArray[i].updateCacheComponent();
        }
    }

    /**
     * Run the cache model: preparation and load cache file
     */
    public void run() {
        preAction();
        reloadCacheContent();
        return;
    }

    /**
     * Return the array of cache models
     * @return the array of cache models
     */
    public CacheModel[] getCacheModelArray() {
        return this.cacheModelArray;
    }

    public BasicNGram[] getNgramArray(){
        BasicNGram[] retArray = new BasicNGram [maxN];
        for (int i = 0; i < this.maxN; i++) {
            retArray[i] = cacheModelArray[i].getNgramModel();
        }
        return retArray;
    }

    public File getCurFile() {
        return this.curFile;
    }

    public void setCurFile(File pCurFile) {
        this.curFile = pCurFile;
        reloadCacheContent();
    }
}
