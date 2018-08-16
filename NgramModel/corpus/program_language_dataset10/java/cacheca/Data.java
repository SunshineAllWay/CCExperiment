package edu.ucdavis.cacheca;

import java.net.URL;

//Author: This class was originally written in C++ by Zhaopeng; converted to Java by Christine

/**
 * Parameter Configuration
 */
public class Data {

	//data wrapper class
	
	static public boolean USE_BACKOFF = true;

	static public int NGRAM_ORDER = 3;
	
	static public boolean USE_CACHE = true;
	static public int CACHE_ORDER = 10;
	static public int CACHE_MIN_ORDER = 3;
	static public boolean USE_FILE_CACHE = true;
	
	static public int BEAM_SIZE = 10;
		
	static public Ngram NGRAM;
	static public Cache CACHE;


	static public boolean Init(URL u, int ngram_order){
		NGRAM_ORDER = ngram_order;
		NGRAM = new Ngram(u, NGRAM_ORDER, BEAM_SIZE);
		CACHE = new Cache(CACHE_ORDER, CACHE_MIN_ORDER);
		return true;
	}
	
}
