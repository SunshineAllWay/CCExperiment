package tokenunit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author HHeart
 * @param <K>: type of element in token
 */

public class Tokensequence<K> {
	public int n;  //length of token sequence
	public ArrayList<K> sequence;
	
	private Optional<ArrayList<K>> initsequence;
	private Optional<K> lastoken;
	
	//If strArray.length is not equal to N in NGram model, it needs to report the failure
	public Tokensequence(K[] tokenArray) {
		int len = tokenArray.length;
		this.n = len;
		this.sequence = new ArrayList<>();  //need to guarantee the pre-post order
	
		for (int i = 0; i < len; i++) {
			this.sequence.add(tokenArray[i]);
		}
		
		splitTokenSeq();
	}

	public Tokensequence(ArrayList<K> tokenList) {
		n = tokenList.size();
		sequence = tokenList;
		splitTokenSeq();
	}

	public void splitTokenSeq() {
		if (n > 0) {
			ArrayList<K> tmpsequence = (ArrayList<K>) sequence.clone();
			K lastElem = tmpsequence.remove(n - 1);
			initsequence = Optional.of(tmpsequence);
			lastoken = Optional.of(lastElem);
		} else {
			initsequence = Optional.of(new ArrayList<>());
			lastoken = Optional.empty();
		}
	}

	public Tokensequence<K> append(Token ptoken) {
		ArrayList<K> ls = sequence;
		ls.add((K)ptoken.mTokenELem);
		return (new Tokensequence<>(ls));
	}
	
	public int length() {
		return this.n;
	}
	
	public ArrayList<K> getSequence() {
		return this.sequence;
	}

	public Optional<ArrayList<K>> getInitSequence() {
		return this.initsequence;
	}

	public Optional<K> getLastToken() {
		return this.lastoken;
	}

	public Tokensequence<K> subTokenSequence(int indexFrom, int indexTo) {
		List<K> tmpsequence = ((ArrayList<K>) sequence.clone()).subList(indexFrom, indexTo);
		K[] tmparr = (K[])tmpsequence.toArray();
		return (new Tokensequence<>(tmparr));
	}

	//POLISH
	public int hashCode() {
		int hashValue = 0;
		for (int i = 0; i < sequence.size(); i++) {
			hashValue += sequence.get(i).hashCode();
		}
		return hashValue;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof Tokensequence)) {
			return false;
		}
		
		Tokensequence<?> tokenseq = (Tokensequence<?>) obj;
		boolean b1 = (this.n == tokenseq.n);
		boolean b2 = true;
		
		if (this.n != tokenseq.n) {
			b2 = false;
		} else {
			for (int i = 0; i < n; i++) {
				if (!(this.sequence.get(i).equals(tokenseq.sequence.get(i)))) {
					b2 = false;
					break;
				}
			}
		}
		
		return (b1 && b2);
	}
	
	public String toString() {
		Integer ninteger = new Integer(n);
		String str = ninteger.toString();
		int len = sequence.size();
		
		for (int i = 0; i < len; i++) {
			String s = sequence.get(i).toString();
			str = str.concat(s);
		}

		return str;
	}
}