package model;

import java.util.*;

import iounit.CorpusImporter;
import refineunit.LidstoneSmoothing;
import refineunit.SmoothingType;
import tokenunit.Token;
import tokenunit.Tokensequence;
import tokenunit.Tokencount;

import static java.lang.Integer.min;
import static refineunit.SmoothingType.Lidstone;

/**
 * @author HHeart
 * @param <K>: type of token in basic n-gram model
 */

public class BasicNGram<K> {
	public int n;              //n>=2, n = 2 in bigram; n = 3 in trigram
	public int modelType;      //0: natural language model;   1: programming language model
	private int seqNum;        //number of sequence
	private HashMap<Tokensequence<K>, HashSet<Tokencount<K>>> seqCntModel;  //kernel model in n-gram

	public BasicNGram(int ngramN, int type) {
		this.n = ngramN;
		this.modelType = type;
		this.seqNum = 0;
		this.seqCntModel = new HashMap<>();
	}

	/**
	 * split the list of tokens into the list of sequences of tokens
	 * @param wholeTokenList: the list of tokens
	 * @return the list of sequences of tokens with length n
	 */
	public ArrayList<Tokensequence<K>> splitWholeTokenList(ArrayList<K> wholeTokenList) {
		ArrayList<Tokensequence<K>> seqList = new ArrayList<>();
		int len = wholeTokenList.size();

		for (int i = 0; i < len; i++) {
			ArrayList<K> nseq = new ArrayList<K>();
			for (int j = 0; j < min(n, len - i); j++) {
				nseq.add(wholeTokenList.get(i + j));
			}
			seqList.add(new Tokensequence<K>(nseq));
		}
		return seqList;
	}

	//error prone
	/**
	 * train seqCntModel to calculate the probability of a given sequence
	 * @param tokenseqList: the list of sequences of tokens extracted from corpus
	 */
	private void trainBasicNGramCntModel(ArrayList<Tokensequence<K>> tokenseqList) {
		int len = tokenseqList.size();

		for (int i = 0; i < len; i++) {
			Tokensequence<K> tmptokenseq = tokenseqList.get(i);
			Tokensequence<K> tmptokeninitseq = new Tokensequence<K>(tmptokenseq.getInitSequence().get());
			Token<K> lastToken = new Token<K>(tmptokenseq.getLastToken().get());
			boolean flag = true;

			if (seqCntModel.containsKey(tmptokeninitseq)) {
				Iterator<Tokencount<K>> it = seqCntModel.get(tmptokeninitseq).iterator();
				while (it.hasNext()) {
					Tokencount<K> tc = it.next();
					if (tc.mTokenElem.equals(lastToken.mTokenELem)) {
						tc.addCount();
						flag = false;
						break;
					}
				}
				if (flag) {
					Tokencount<K> tc = new Tokencount<K>(lastToken.mTokenELem, 1);
					seqCntModel.get(tmptokeninitseq).add(tc);
				}
			} else {
				HashSet<Tokencount<K>> s = new HashSet<>();
				Tokencount<K> tc = new Tokencount<K>(lastToken.mTokenELem, 1);
				s.add(tc);
				seqCntModel.put(tmptokeninitseq, s);
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
	 * get the map from token sequence to the set of tokencount
	 * @return seqCntModel
	 */
	public HashMap<Tokensequence<K>, HashSet<Tokencount<K>>> getBasicNGramCntModel() {
		//get the model
		return this.seqCntModel;
	}

	public Optional<HashSet<Tokencount<K>>> getBasicNGramCandidates(Tokensequence<K> nseq) {
		//return set of candidates corresponding to nseq, which has the form of {Tokencount} 
		
		HashSet<Tokencount<K>> result = seqCntModel.get(nseq);
		
		if (result == null) {
			return Optional.empty();
		} else {
			return Optional.of(result);
		}
	}

	//without smoothing
	public double getRelativeProbability(Tokensequence<K> nseq, Token<K> t) {
		Optional<HashSet<Tokencount<K>>> tokenCntSetOrNull = getBasicNGramCandidates(nseq);
		if (!tokenCntSetOrNull.isPresent()) {
			return (1.0 / seqNum);
		}

		HashSet<Tokencount<K>> tokencntset = tokenCntSetOrNull.get();
		Iterator<Tokencount<K>> it = tokencntset.iterator();
		int totalCnt = 0;
		int captureCnt = 0;

		while(it.hasNext()) {
			Tokencount<K> tokencnt = it.next();
			totalCnt += tokencnt.mCount;
			if (tokencnt.mTokenElem.equals(t.mTokenELem)) {
				captureCnt += tokencnt.mCount;
			}
		}

		double relativeProb = smoothing(captureCnt, totalCnt, Lidstone);

		return relativeProb;
	}

	public double smoothing(int count1, int count2, SmoothingType type) {
		switch (type) {
			case Lidstone:
				LidstoneSmoothing sm = new LidstoneSmoothing(0.5, seqNum);
				return sm.probAfterSmoothing(count1, count2);

			default:
				return (1.0 * count1) / count2;
		}
	}
}