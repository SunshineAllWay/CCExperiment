package engine;

import iounit.CorpusImporter;
import model.BasicNGram;
import model.CacheModel;
import model.CacheNGram;
import tokenunit.Token;
import tokenunit.Tokensequence;

import java.io.File;
import java.util.*;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.min;

public class NLcacheRunEngine<K> implements CacheRunEngine<K>{
    private int maxN;                          //maximal parameter in gramArray
    private double gamma;                      //concentration parameter
    private CacheModel<K>[] cacheModelArray;   //unigram, bigram, trigram or unigram ...ngram

    private ArrayList<K> corpusTokenStream;  //token stream in the corpus
    private ArrayList<K> cacheTokenStream;   //token stream in the cache files

    private ArrayList<File> cacheFileList;   //the list of cache files
    private File curFile;                    //current edited file

    /**
     * Construct an object of NLcacheRunEngine
     * @param n the length of gram
     * @param g concentration parameter
     */
    public NLcacheRunEngine(int n, double g, File pCurFile) {
        this.maxN = n;
        this.gamma = g;
        cacheModelArray = (CacheModel<K>[]) new CacheModel[maxN];

        CorpusImporter<K> corpusImporter = new CorpusImporter<>(0);
        corpusTokenStream = corpusImporter.importTrainingCorpusFromBase(1);
        cacheTokenStream = new ArrayList<>();

        for (int i = 0; i < maxN; i++) {
            cacheModelArray[i] =  new CacheModel<>(i + 1, 0, gamma);
        }

        cacheFileList = new ArrayList<>();
        curFile = pCurFile;
    }

    /**
     * Construct an object of NLcacheRunEngine, max length of gram is 3
     * @param g concentration parameter
     */
    public NLcacheRunEngine(double g, File pCurFile) {
        this.maxN = 3;
        this.gamma = g;
        cacheModelArray = (CacheModel<K>[]) new CacheModel[maxN];

        CorpusImporter<K> corpusImporter = new CorpusImporter<>(0);
        corpusTokenStream = corpusImporter.importTrainingCorpusFromBase(1);
        cacheTokenStream = new ArrayList<>();

        for (int i = 0; i < maxN; i++) {
            cacheModelArray[i] =  new CacheModel<>(i + 1, 0, gamma);
        }

        cacheFileList = new ArrayList<>();
        curFile = pCurFile;
    }

    /**
     * Train the n-gram model component during the preparation phase
     */
    public void preAction() {
        System.out.println("Cache engine for natural language warms up");
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
        System.out.println("Cache engine for natural language is prepared");
    }


    /**
     * Infer and recommend the post token for current file
     * @return the most likely post token of nseq(can be null)
     */
    public Optional<K> completePostToken() {
        //retain the cache components
        retrainCacheModel();
        CorpusImporter<K> corpusImporter = new CorpusImporter<>(0);
        ArrayList<K> currentFileTokenStream = corpusImporter.importCorpusFromSingleFile(curFile);
        int length = currentFileTokenStream.size();
        int prefixLength = Math.min(length, maxN);

        if (length == 0) {
            return Optional.empty();
        }

        ArrayList<K> tailStream = (ArrayList<K>)currentFileTokenStream.subList(length - prefixLength, length);

        //get the candidates from 2-gram model
        ArrayList<K> miniTailStream = (ArrayList<K>)tailStream.subList(prefixLength - 1, prefixLength);
        Tokensequence<K> seq = new Tokensequence<>(miniTailStream);
        Tokensequence<K> lastSeq = new Tokensequence<>(miniTailStream);
        HashMap<K, Integer> elemCntMap = new HashMap<>();

        elemCntMap.putAll(cacheModelArray[1].getNgramModel().getModel().get(lastSeq));
        elemCntMap.putAll(cacheModelArray[1].getNcacheModel().getModel().get(lastSeq));

        if (elemCntMap.size() == 0) {
            return Optional.empty();
        }

        //HashMap<K, Integer> elemCntMap = candiadates.get();
        Iterator<Map.Entry<K, Integer>> it = elemCntMap.entrySet().iterator();
        double maxProb = 0.0;
        K retElem = null;

        //Need to polish, select the Tokencount with the maximal count in the set.
        while(it.hasNext()) {
            Map.Entry<K, Integer> entry = it.next();
            double prob = calculateProbability(seq.append(new Token<>(entry.getKey())));
            if (prob > maxProb) {
                maxProb = prob;
                retElem = entry.getKey();
            }
        }

        if (retElem == null) {
            return Optional.empty();
        } else {
            return Optional.of(retElem);
        }
    }


    /**
     * Estimate the probability of the sentence
     * @param nseq: Token sequence(sentence)
     * @return: The probability of the sentence
     */

    /** maxN = 3
     * using refineunit such as LidstoneSmoothing
     * P(a1 a2 a3 a4 ... ak ... a_(n-1), an)
     * = p(a1) * p(a2 | a1) * p(a3 | a1 a2) * ... * p(an | a_(n-2) a_(n-1))
     * = p(a1) * (p(a1 a2) / p(a1)) * (p(a1 a2 a3) / p(a1 a2)) * ... * p(a_(n-2) a_(n-1) a_n) / p(a_(n-2) a_(n-1))
     * = p(a1) * p(a1 a2) * p(a1 a2 a3) * p(a2 a3 a4) * ... * p(a_(n-2) a_(n-1) a_(n)) /
     *   p(a1) * p(a1 a2) * p(a2 a3) * p(a3 a4) * p(a4 a5) * ... * p(a_(n-2) a_(n-1))
     */
    public double calculateProbability(Tokensequence<K> nseq) {
        int seqlength = nseq.length();
        double logprob = 0.0;

        ArrayList<K> nseqContent = nseq.getSequence();
        int i;
        int maxGramLength = min(maxN, seqlength);

        //TODO: probability of 1-gram, assume all tokens in the sequence appear in the training list
        for (i = 1; i < maxGramLength; i++) {
            Tokensequence<K> subTokenSeq = new Tokensequence<>((K[])nseqContent.subList(0, i).toArray());
            logprob += log(cacheModelArray[i].getRelativeProbability(subTokenSeq, new Token<>(nseqContent.get(i))));
        }
        for (i = maxN; i < seqlength; i++) {
            Tokensequence<K> subTokenSeq = new Tokensequence<>((K[])nseqContent.subList(i - maxN + 1, i).toArray());
            logprob += log(cacheModelArray[maxN - 1].getRelativeProbability(subTokenSeq, new Token<>(nseqContent.get(i))));
        }

        double prob = exp(logprob);

        return prob;
    }

    /**
     * Reload the cache when handling the request to complete the post token of the sequence
     */
    public void reloadCacheContent() {
        CorpusImporter<K> corpusImporter = new CorpusImporter<>(0);
        int fileNum = cacheFileList.size();

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
}
