package engine;
import java.util.*;

import iounit.CorpusImporter;
import model.BasicNGram;
import tokenunit.Token;
import tokenunit.Tokensequence;

import static java.lang.StrictMath.*;


/**
 * @author HHeart
 * 
 * n-gram natural language model engine:
 * ----inference token  
 * ----calculate probability of token sequences(sentence, phrase etc.)
 * 
 * @param <K>: type of element in n-gram model
 */

public class NLngramRunEngine<K> implements NgramRunEngine<K>{
    public int maxN;                     //maximal parameter in gramArray
    public double testRatio;              //the ratio of test files in the corpus
	private BasicNGram<K> [] gramArray;   //unigram, bigram, trigram or unigram ...ngram
	private ArrayList<K> trainingTokenList;  //list of tokens in the corpus used for training
	private ArrayList<K> testingTokenList;   //list of tokens in the corpus used for testing
    public ArrayList<Double> likelihood;        //likelihood of n-gram model
    public ArrayList<Double> perplexity;        //perplexity of n-gram model
	/**
	 * @param n the max parameter of n-gram models
	 * @param ratio  the radio of testing files
	 */
	public NLngramRunEngine(int n, double ratio) {
		maxN = n;
		gramArray = (BasicNGram<K>[]) new BasicNGram [n];
		testRatio = ratio;
		likelihood = new ArrayList<>();
        perplexity = new ArrayList<>();

		CorpusImporter<K> corpusImporter = new CorpusImporter<>(0);
		trainingTokenList = corpusImporter.importTrainingCorpusFromBase();

		for (int i = 0; i < n; i++) {
			this.gramArray[i] = new BasicNGram<>(i + 1, 0);
		}
	}

	/**
	 * @param ratio  the radio of testing files
	 */
	public NLngramRunEngine(double ratio) {
		maxN = 3;
		gramArray = (BasicNGram<K>[]) new BasicNGram [3];
		testRatio = ratio;
        likelihood = new ArrayList<>();
        perplexity = new ArrayList<>();

		CorpusImporter<K> corpusImporter = new CorpusImporter<>(0);
		trainingTokenList = corpusImporter.importTrainingCorpusFromBase();

		for (int i = 0; i < 3; i++) {
			this.gramArray[i] = new BasicNGram<>(i + 1, 0);
		}
	}

	/**
	 * N-gram model for natural language model pre action
	 * Import corpus and train n-gram models
	 */
	public void preAction() {
		System.out.println("N-gram engine for natural language warms up");
		for (int i = 0; i < gramArray.length; i++) {
			System.out.println("---------------------------------");
			System.out.print(Integer.toString(i + 1) + "-gram");
			System.out.println(" single pre-action beginning");
			gramArray[i].preAction(trainingTokenList);
			System.out.print(Integer.toString(i + 1) + "-gram");
			System.out.println(" single pre-action finished");
			System.out.println("---------------------------------");
			System.out.println();
		}
		System.out.println("N-gram engine for natural language is prepared");
	}

    /**
     * Evaluate n-gram model: calculate the likelihood and perplexity
     */
	public void evaluateModel() {
        for (int i = 0; i < maxN; i++) {
            likelihood.add(calculateLikelihood(i + 1));
            perplexity.add(calculatePerplexity(likelihood.get(i)));
        }
    }

	/**
	 * return the list of tokens containing in the corpus
	 * @return wholeTokenList
	 */
	public ArrayList<K> getTrainingTokenList() {
		return this.trainingTokenList;
	}

	/**
	 * Run natural language n-gram engine
	 */
	public void run() {
		preAction();
		return;
	}

	private double getProbInUnaryGram(K elem) {
		ArrayList<K> ls = new ArrayList<>();
		ls.add(elem);
		Tokensequence<K> seq = new Tokensequence<>(ls);

		if (!gramArray[0].getModel().containsKey(seq)) {
			return -1;
		}

		int capturedCount = gramArray[0].getModel().get(seq).get(null);
		int totalCount = 0;
		Iterator<Map.Entry<Tokensequence<K>, HashMap<K, Integer>>> it = gramArray[0].getModel().entrySet().iterator();
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
		logprob += log(getProbInUnaryGram(nseq.getSequence().get(0)));
		for (i = 1; i < maxGramLength; i++) {
			Tokensequence<K> subTokenSeq = new Tokensequence<>((K[])nseqContent.subList(0, i).toArray());
			logprob += log(gramArray[i].getRelativeProbability(subTokenSeq, new Token<>(nseqContent.get(i))));
		}
		for (i = maxN; i < seqlength; i++) {
			Tokensequence<K> subTokenSeq = new Tokensequence<>((K[])nseqContent.subList(i - maxN + 1, i).toArray());
			logprob += log(gramArray[maxN - 1].getRelativeProbability(subTokenSeq, new Token<>(nseqContent.get(i))));
		}

		double prob = exp(logprob);

		return prob;
	}

	/**
	 * Infer and recommend the post token for current token sequence
	 * @param seq: token sequence
	 * @return the most likely post token of nseq(can be null)
	 */
	public ArrayList<K> completePostToken(Tokensequence<K> seq) {
		//get the candidates from 2-gram model
		Tokensequence<K> nseq = new Tokensequence<>((ArrayList<K>)seq.getSequence().clone());
		ArrayList<K> candidatesList = new ArrayList<>();

		//POLISH
        Tokensequence<K> lastSeq = nseq.subTokenSequence(nseq.length() - 1, nseq.length());
        HashMap<K, Integer> elemCntMap = gramArray[1].getModel().get(lastSeq);

		if (elemCntMap == null) {
			return candidatesList;
		}

		//HashMap<K, Integer> elemCntMap = candiadates.get();
        Iterator<Map.Entry<K, Integer>> it = elemCntMap.entrySet().iterator();
		HashMap<K, Double> elemProbMap = new HashMap<>();

		//Need to polish, select the Tokencount with the maximal count in the set.
		while(it.hasNext()) {
            Map.Entry<K, Integer> entry = it.next();
            ArrayList<K> ls = (ArrayList<K>)nseq.getSequence().clone();
            ls.add(entry.getKey());
			double prob = calculateProbability(new Tokensequence<>(ls));
			elemProbMap.put(entry.getKey(), prob);
		}

		Set<Map.Entry<K, Double>> elemProbSet = elemProbMap.entrySet();

		//Sort by probability
		while(!elemProbSet.isEmpty()) {
			double maxProbablity = 0.0;
			Map.Entry<K, Double> recordEntry = null;
			for (Map.Entry<K, Double> entry : elemProbSet) {
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
	 * Calculate the likelihood of n-gram in the testing corpus
	 * @param n: n in n-gram model
	 * @return the likelihood of n-gram in the testing corpus
	 */
	public double calculateLikelihood(int n) {
        double likelihood = 0.0;
        int len = trainingTokenList.size();

        //Assume tokens in testing list all appeared in the training list, and it can't stand in many situations
	    if (n == 1) {
			int seqNum = gramArray[0].getSeqNum();
			for (int i = 0; i < len; i++) {
			    ArrayList<K> tokenseq = new ArrayList<>();
			    tokenseq.add(trainingTokenList.get(i));
			    HashMap<K, Integer> map = gramArray[0].getModel().get(new Tokensequence<>(tokenseq));
			    if (map == null) {
			    	continue;
				}
			    double prob =  getProbInUnaryGram(trainingTokenList.get(i));
                likelihood += log(prob);
            }
            return likelihood;
        }

		for (int i = 1; i < len; i++) {
			int toIndex = i;
			int fromIndex = max(0, i - n + 1);
			ArrayList<K> seq = new ArrayList<>();
			for (int k = fromIndex; k < toIndex; k++) {
				seq.add(trainingTokenList.get(k));
			}

			Tokensequence<K> tokenseq = new Tokensequence<>(seq);
			Token<K> t = new Token<K>(trainingTokenList.get(toIndex));
			double prob = gramArray[toIndex - fromIndex].getRelativeProbability(tokenseq, t);
			likelihood += log(prob);
		}
		return likelihood;
	}

	/**
	 * Calculate the perplexity of n-gram in the testing corpus
	 * @param n: n in n-gram model
	 * @return the perplexity of n-gram in the testing corpus
	 */
	public double calculatePerplexity(int n) {
		double likelihood = calculateLikelihood(n);
		double perplexity = calculatePerplexity(likelihood);
		return perplexity;
	}

    /**
     * Calculate the perplexity of n-gram in the testing corpus
     * @param likelihood: held-out likelihood in n-gram model
     * @return the perplexity of n-gram in the testing corpus
     */
	public double calculatePerplexity(double likelihood) {
        double perplexity = exp(-likelihood * log(2) / trainingTokenList.size());
        return perplexity;
    }

    /**
     * get post token information using backoff
     * @param tokenseq: prefix token sequence
     * @param i: model parameter plus 1, assume i is equal to the length of prefix token sequence
     * @return post token candidates and counts
     */
	public HashMap<K, Integer> getPostInfoBybackingOff(Tokensequence<K> tokenseq, int i) {
		if (i >= 0) {
			HashMap<K, Integer> map = gramArray[i].getModel().get(tokenseq);
			if (map != null) {
				return map;
			} else {
			    Tokensequence<K> tailTokenSeq = tokenseq.subTokenSequence(1, tokenseq.length());
				return getPostInfoBybackingOff(tailTokenSeq, i - 1);
			}
		}
		return null;
	}

	/**
	 * Return the array of basic n-gram models
	 * @return the array of basic n-gram models
	 */
	public BasicNGram<K>[] getNgramArray() {
		return this.gramArray;
	}
}