package model;

/**
 * @author HHeart
 * @param <K>: type of token in CacheNGram
 */

public class CacheNGram<K> extends BasicNGram<K> {
	public CacheNGram(int ngramN, int type) {
		super(ngramN, type);
	}
}