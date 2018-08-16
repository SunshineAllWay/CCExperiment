package edu.ucdavis.cacheca;

//@author Originally written in C++ by Zhaopeng; converted to Java by Christine

//Note: originally an inner class of Ngram.cpp

/**
 * Token and tis probability
 */
public class Word {
	public String mToken;
	public float mProb;

	public Word(){
		mProb = 0.0f;
	}

	public Word(String token, float prob){
		mProb = prob;
		mToken = token;
	}
}
