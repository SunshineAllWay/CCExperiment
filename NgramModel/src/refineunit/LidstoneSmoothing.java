package refineunit;

/**
 * @author Lamp
 * Lid Stone Smoothing
 */

public class LidstoneSmoothing {
	public double mlambda;
	public int mNDic;
	
	/** Lidstone Smoothing
	 * @param lambda: non-negative
	 * @param nDic: total number of sequence in n-gram model
	 */
	public LidstoneSmoothing(double lambda, int nDic) {
		this.mlambda = lambda;
		this.mNDic = nDic;
	}
	
	/**
	 * @param totalCnt: total count of w1,w2,..,wn-1 (with length of n - 1)
	 * @param captureCnt: total count of w1,w2,...,wn (with length of n)
	 * @return probability of sequence after smoothing
	 */
	public double probAfterSmoothing(int totalCnt, int captureCnt) {
		double a = captureCnt + mlambda;
		double b = totalCnt + mlambda * mNDic;
		return (a / b);
	}
}