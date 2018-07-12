package engine;

import java.util.Optional;
import tokenunit.Tokensequence;

/**
 * @author HHeart
 * 
 * n-gram model engine: 
 * ----inference token  
 * ----calculate probability of token sequences(sentence, phrase etc.)
 * 
 * @param <K>: type of element in n-gram model
 */

interface NgramRunEngine<K> extends CCRunEngine<K>{
	double calculateLikelihood(int n);
	double calculatePerplexity(int n);
	void evaluateModel();
}