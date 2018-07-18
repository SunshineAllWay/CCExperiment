package model;

import java.util.*;
import refineunit.LidstoneSmoothing;
import refineunit.SmoothingType;
import tokenunit.Token;
import tokenunit.Tokensequence;
import static refineunit.SmoothingType.*;


/**
 * @author HHeart
 */

public class BasicNGram {
    public int n;              //n>=2, n = 2 in bigram; n = 3 in trigram
	public int modelType;      //0: natural language model;   1: programming language model
	private int seqNum;        //number of sequence
	private HashMap<Tokensequence, HashMap<String, Integer>> seqCntModel;  //kernel model in n-gram
    public HashSet<String> dic;     //dictionary of token elements

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
	public ArrayList<Tokensequence> splitWholeTokenList(ArrayList<String> wholeTokenList) {
		ArrayList<Tokensequence> seqList = new ArrayList<>();
		int len = wholeTokenList.size();

		for (int i = 0; i < len - n + 1; i++) {
			ArrayList<String> nseq = new ArrayList<>();
			for (int j = 0; j < n; j++) {
				nseq.add(wholeTokenList.get(i + j));
				dic.add(wholeTokenList.get(i + j));
			}
			seqList.add(new Tokensequence(nseq));
		}
		return seqList;
	}

	//error prone
	/**
	 * train seqCntModel to calculate the probability of a given sequence
	 * @param tokenseqList the list of sequences of tokens extracted from corpus
	 */
	private void trainBasicNGramCntModel(ArrayList<Tokensequence> tokenseqList) {
		int len = tokenseqList.size();

		for (int i = 0; i < len; i++) {
			Tokensequence tmpTokenSeq = tokenseqList.get(i);
			Tokensequence tmpTokenInitSeq = new Tokensequence(tmpTokenSeq.getInitSequence());
			String lastTokenElem = tmpTokenSeq.getLastToken();
            HashMap<String, Integer> tokenCntMap;

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
	public void preAction(ArrayList<String> wholeTokenList) {
		//Step 1: Import Corpus, check whether n is matched or not
		System.out.println("Stream split begins");
		long importCorpusMoment1 = System.currentTimeMillis();
		ArrayList<Tokensequence> corpusList = splitWholeTokenList(wholeTokenList);
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
	public Optional<HashMap<String, Integer>> getBasicNGramCandidates(Tokensequence tokenseq) {
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
	public double getRelativeProbability(Tokensequence nseq, Token t) {
	    //TODO: Need to polish
		Optional<HashMap<String, Integer>> elemCollection = getBasicNGramCandidates(nseq);
		if (!elemCollection.isPresent()) {
			return (1.0 / dic.size());
		}

		HashMap<String, Integer> elemCntMap = elemCollection.get();
		Iterator<Map.Entry<String, Integer>> it = elemCntMap.entrySet().iterator();
		int totalCnt = 0;
		int captureCnt = 0;

		while(it.hasNext()) {
			Map.Entry<String, Integer> entry = it.next();
			totalCnt += entry.getValue();
			if (entry.getKey().equals(t.mTokenElem)) {
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
	public HashMap<Tokensequence, HashMap<String, Integer>> getModel() {
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
	public int getSeqWithSpecificPrefix(Tokensequence prefix) {
		HashMap<String, Integer> cntmap = seqCntModel.get(prefix);
		if (cntmap == null) {
			return 0;
		}

		Iterator<Map.Entry<String, Integer>> it = cntmap.entrySet().iterator();
		int cnt = 0;
		while(it.hasNext()) {
			cnt += it.next().getValue();
		}

		return cnt;
	}
}