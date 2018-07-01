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

interface NgramRunEngine<K> {
	public Optional<K> completePostToken(Tokensequence<K> nseq);
	public double calculateProbability(Tokensequence<K> nseq);
	public double calculateLikelihood(int n);
	public double calculatePerplexity(int n);
	public void evaluateModel();
	public void run();
}