package refineunit;

/**
 * @author Lamp
 * Lid Stone Smoothing
 */

public class LidstoneSmoothing {
	public double mlambda;
	public int mngramCount;
	
	/** Lidstone Smoothing
	 * @param lambda: non-negative
	 * @param ngramcount: total number of sequence in n-gram model
	 */
	public LidstoneSmoothing(double lambda, int ngramcount) {
		this.mlambda = lambda;
		this.mngramCount = ngramcount;
	}
	
	/**
	 * @param count1: total count of w1,w2,..,wn-1 (with length of n - 1)
	 * @param count2: total count of w1,w2,...,wn (with length of n)
	 * @return probability of sequence after smoothing
	 */
	public double probAfterSmoothing(int count1, int count2) {
		double a = count2 + mlambda;
		double b = count1 + mlambda * mngramCount;
		return (a / b);
	}
}