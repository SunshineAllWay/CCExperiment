package model;

import java.util.*;
import refineunit.LidstoneSmoothing;
import refineunit.SmoothingType;
import tokenunit.Token;
import tokenunit.Tokensequence;
import static refineunit.SmoothingType.*;


/**
 * @author HHeart
 * @param <K>: type of token in basic n-gram model
 */

public class BasicNGram<K> {
    public int n;              //n>=2, n = 2 in bigram; n = 3 in trigram
	public int modelType;      //0: natural language model;   1: programming language model
	private int seqNum;        //number of sequence
	private HashMap<Tokensequence<K>, HashMap<K, Integer>> seqCntModel;  //kernel model in n-gram
    public HashSet<K> dic;     //dictionary of token elements

	/**
	 * Construct an object of BasicNGram
	 * @param ngramN the length of gram in n-gram model
	 * @param type the type of n-gram model, type = 0 when the model process natural language, type = 1 for PL
	 */
	public BasicNGram(int ngramN, int type) {
		this.n = ngramN;
		this.modelType = type;
		this.seqNum = 0;
		this.seqCntModel = new HashMap<>();
		this.dic = new HashSet<>();
	}

	/**
	 * split the list of tokens into the list of sequences of tokens
	 * @param wholeTokenList the list of tokens
	 * @return the list of sequences of tokens with length n
	 */
	public ArrayList<Tokensequence<K>> splitWholeTokenList(ArrayList<K> wholeTokenList) {
		ArrayList<Tokensequence<K>> seqList = new ArrayList<>();
		int len = wholeTokenList.size();

		for (int i = 0; i < len - n + 1; i++) {
			ArrayList<K> nseq = new ArrayList<>();
			for (int j = 0; j < n; j++) {
				nseq.add(wholeTokenList.get(i + j));
				dic.add(wholeTokenList.get(i + j));
			}
			seqList.add(new Tokensequence<>(nseq));
		}
		return seqList;
	}

	//error prone
	/**
	 * train seqCntModel to calculate the probability of a given sequence
	 * @param tokenseqList the list of sequences of tokens extracted from corpus
	 */
	private void trainBasicNGramCntModel(ArrayList<Tokensequence<K>> tokenseqList) {
		int len = tokenseqList.size();

		for (int i = 0; i < len; i++) {
			Tokensequence<K> tmpTokenSeq = tokenseqList.get(i);
			Tokensequence<K> tmpTokenInitSeq = new Tokensequence<>(tmpTokenSeq.getInitSequence());
			K lastTokenElem = tmpTokenSeq.getLastToken();
            HashMap<K, Integer> tokenCntMap;

			if (seqCntModel.containsKey(tmpTokenInitSeq)) {
                tokenCntMap = seqCntModel.get(tmpTokenInitSeq);
                if (tokenCntMap.containsKey(lastTokenElem)) {
                    int cnt = tokenCntMap.get(lastTokenElem);
                    cnt++;
                    tokenCntMap.put(lastTokenElem, cnt);
                } else {
                    tokenCntMap.put(lastTokenElem, 1);
                }
            } else {
                tokenCntMap = new HashMap<>();
                tokenCntMap.put(lastTokenElem, 1);
                seqCntModel.put(tmpTokenInitSeq, tokenCntMap);
                seqNum++;
            }
		}
	}

	/**
	 * n-gram model preaction including segment the sequence of tokens in the corpus and training model
	 * @param wholeTokenList: the list of tokens in the corpus
	 */
	public void preAction(ArrayList<K> wholeTokenList) {
		//Step 1: Import Corpus, check whether n is matched or not
		System.out.println("Stream split begins");
		long importCorpusMoment1 = System.currentTimeMillis();
		ArrayList<Tokensequence<K>> corpusList = splitWholeTokenList(wholeTokenList);
		System.out.println("Stream split finished");
		long importCorpusMoment2 = System.currentTimeMillis();
		long importCorpusTime = importCorpusMoment2 - importCorpusMoment1;
		System.out.println("Time cost: " + String.valueOf(importCorpusTime) + " ms");
		
		int len = corpusList.size();
		if (len == 0) return;
		if (corpusList.get(0).n != this.n) {
			return;
		}
		
		//Step 2: Train Model
		System.out.println("Count Model Training begins");
		long trainMoment3 = System.currentTimeMillis();
		trainBasicNGramCntModel(corpusList);
		System.out.println("Count Model Training finished");
		long trainMoment4 = System.currentTimeMillis();
		long trainTime2 = trainMoment4 - trainMoment3;
		System.out.println("Time cost: " + String.valueOf(trainTime2) + " ms");
	}


    /**
     * return candidates corresponding to token sequence
     * @param tokenseq token sequence
     * @return candidates corresponding to token sequence
     */
	public Optional<HashMap<K, Integer>> getBasicNGramCandidates(Tokensequence<K> tokenseq) {
		if (seqCntModel.containsKey(tokenseq)) {
		    return Optional.of(seqCntModel.get(tokenseq));
        } else {
            return Optional.empty();
        }
	}

    /**
     * Calculate the relative probability of a given token being the post token of token sequence
     * @param nseq prefix token sequence
     * @param t last token
     * @return relative probability of t being the post token of nseq
     */
	public double getRelativeProbability(Tokensequence<K> nseq, Token<K> t) {
	    //TODO: Need to polish
		Optional<HashMap<K, Integer>> elemCollection = getBasicNGramCandidates(nseq);
		if (!elemCollection.isPresent()) {
			return (1.0 / dic.size());
		}

		HashMap<K, Integer> elemCntMap = elemCollection.get();
		Iterator<Map.Entry<K, Integer>> it = elemCntMap.entrySet().iterator();
		int totalCnt = 0;
		int captureCnt = 0;

		while(it.hasNext()) {
			Map.Entry<K, Integer> entry = it.next();
			totalCnt += entry.getValue();
			if (entry.getKey().equals(t.mTokenELem)) {
				captureCnt += entry.getValue();
			}
		}

		double relativeProb = smoothing(totalCnt, captureCnt, Lidstone);
		return relativeProb;
	}

    /**
     * return the probability after smooth operation
     * @param count1 total count
     * @param count2 captured count
     * @param type type of smooth, NONE for no smooth operation
     * @return the probability after smooth operation
     */
	public double smoothing(int count1, int count2, SmoothingType type) {
		switch (type) {
			case Lidstone:
				//TODO: Error prone
				LidstoneSmoothing sm = new LidstoneSmoothing(0.1, dic.size());
				return sm.probAfterSmoothing(count1, count2);

			default:
				return (1.0 * count2) / count1;
		}
	}

	/**
	 * get the map from token sequence to the set of tokencount
	 * @return seqCntModel
	 */
	public HashMap<Tokensequence<K>, HashMap<K, Integer>> getModel() {
		//get the model
		return this.seqCntModel;
	}

    /**
     * return the number of sequences
     * @return the number of sequences
     */
	public int getSeqNum() {
		return this.seqNum;
	}

	/**
	 * Return the number of sequence with the given prefix
	 * @param prefix given prefix
	 * @return the number of sequence with the given prefix
	 */
	public int getSeqWithSpecificPrefix(Tokensequence<K> prefix) {
		HashMap<K, Integer> cntmap = seqCntModel.get(prefix);
		if (cntmap == null) {
			return 0;
		}

		Iterator<Map.Entry<K, Integer>> it = cntmap.entrySet().iterator();
		int cnt = 0;
		while(it.hasNext()) {
			cnt += it.next().getValue();
		}

		return cnt;
	}
}