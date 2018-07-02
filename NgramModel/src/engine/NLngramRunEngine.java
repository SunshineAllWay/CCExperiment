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
    private int maxN;                     //maximal parameter in gramArray
    public double testRatio;              //the ratio of test files in the corpus
	private BasicNGram<K> [] gramArray;   //unigram, bigram, trigram or unigram ...ngram
	private ArrayList<K> trainingTokenList;  //list of tokens in the corpus used for training
	private ArrayList<K> testingTokenList; //list of tokens in the corpus used for testing
    public ArrayList<Double> likelihood;        //likelihood of n-gram model
    public ArrayList<Double> perplexity;        //perplexity of n-gram model
	/**
	 * @param n: the max parameter of n-gram models
	 * @param type:  0:natural language model;   1: programming language model
	 */
	public NLngramRunEngine(int n, int type, double ratio) {
		maxN = n;
		gramArray = (BasicNGram<K>[]) new BasicNGram [n];
		testRatio = ratio;
		likelihood = new ArrayList<>();
        perplexity = new ArrayList<>();

		CorpusImporter<K> corpusImporter = new CorpusImporter<>(type);
		trainingTokenList = corpusImporter.importTrainingCorpusFromBase(testRatio);
		testingTokenList = corpusImporter.imporTestingCorpusFromBase(testRatio);

		for (int i = 0; i < n; i++) {
			this.gramArray[i] = new BasicNGram<>(i + 1, type);
		}
	}

	/**
	 * @param type:  0:natural language model;   1: programming language model
	 */
	public NLngramRunEngine(int type, double ratio) {
		maxN = 3;
		gramArray = (BasicNGram<K>[]) new BasicNGram [3];
		testRatio = ratio;
        likelihood = new ArrayList<>();
        perplexity = new ArrayList<>();

		CorpusImporter<K> corpusImporter = new CorpusImporter<>(type);
		trainingTokenList = corpusImporter.importTrainingCorpusFromBase(ratio);
		testingTokenList = corpusImporter.imporTestingCorpusFromBase(testRatio);

		for (int i = 0; i < 3; i++) {
			this.gramArray[i] = new BasicNGram<>(i + 1, type);
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
			System.out.println(" single preaction beginning");
			gramArray[i].preAction(trainingTokenList);
			System.out.print(Integer.toString(i + 1) + "-gram");
			System.out.println(" single preaction finished");
			System.out.println("---------------------------------");
			System.out.println();
		}
		System.out.println("N-gram engine for natural language is prepared");

		evaluateModel();
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

        //TODO: probability of 1-gram
		for (i = 0; i < maxGramLength; i++) {
			Tokensequence<K> subTokenSeq = new Tokensequence<>((K[])nseqContent.subList(0, i).toArray());
			logprob += log(gramArray[i].getRelativeProbability(subTokenSeq, new Token<>(nseqContent.get(i))));
		}
		for (i = maxN; i < seqlength; i++) {
			Tokensequence<K> subTokenSeq = new Tokensequence<>((K[])nseqContent.subList(i - maxN, i).toArray());
			logprob += log(gramArray[maxN - 1].getRelativeProbability(subTokenSeq, new Token<>(nseqContent.get(i))));
		}

		double prob = exp(logprob);
		return prob;
	}

	/**
	 * Infer and recommend the post token for current token sequence
	 * @param nseq: token sequence
	 * @return the most likely post token of nseq(can be null)
	 */
	public Optional<K> completePostToken(Tokensequence<K> nseq) {
		//get the candidates from 2-gram model
		int seqlength = nseq.length();

		//POLISH
		Optional<HashMap<K, Integer>> candiadates = gramArray[1].getBasicNGramCandidates(nseq.subTokenSequence(seqlength - 1, seqlength));

		if (!candiadates.isPresent()) {
			return Optional.empty();
		}

		HashMap<K, Integer> elemCntMap = candiadates.get();
        Iterator<Map.Entry<K, Integer>> it = elemCntMap.entrySet().iterator();
		double maxProb = 0.0;
		K retElem = null;

		//Need to polish, select the Tokencount with the maximal count in the set.
		while(it.hasNext()) {
            Map.Entry<K, Integer> entry = it.next();
			double prob = calculateProbability(nseq.append(new Token<>(entry.getKey())));
			if (prob > maxProb) {
				maxProb = prob;
				retElem = entry.getKey();
			}
		}

		System.out.print("maxProb: ");
		System.out.println(maxProb);
		if (retElem == null) {
			return Optional.empty();
		} else {
			return Optional.of(retElem);
		}
	}

	/**
	 * Calculate the likelihood of n-gram in the testing corpus
	 * @param n: n in n-gram model
	 * @return the likelihood of n-gram in the testing corpus
	 */
	public double calculateLikelihood(int n) {
	    if (n == 1) {
            return 1.0;
        }

		double likelihood = 0.0;
		int len = testingTokenList.size();

		for (int i = 1; i < len; i++) {
			int toIndex = i;
			int fromIndex = max(0, i - n + 1);
			ArrayList<K> seq = new ArrayList<>();
			for (int k = fromIndex; k < toIndex; k++) {
				seq.add(testingTokenList.get(fromIndex));
			}

			Tokensequence<K> tokenseq = new Tokensequence<>(seq);
			Token<K> t = new Token<K>(testingTokenList.get(toIndex));
			likelihood += log(gramArray[toIndex - fromIndex].getRelativeProbability(tokenseq, t));
		}
		return likelihood;
	}

	/**
	 * Calculate the perplexity of n-gram in the testing corpus
	 * @param n: n in n-gram model
	 * @return the perplexity of n-gram in the testing corpus
	 */
	public double calculatePerplexity(int n) {
		double likelihood = calculatePerplexity(n);
		double perplexity = calculatePerplexity(likelihood);
		return perplexity;
	}

    /**
     * Calculate the perplexity of n-gram in the testing corpus
     * @param likelihood: held-out likelihood in n-gram model
     * @return the perplexity of n-gram in the testing corpus
     */
	public double calculatePerplexity(double likelihood) {
        double perplexity = exp(-likelihood * log(2) / testingTokenList.size());
        return perplexity;
    }
}