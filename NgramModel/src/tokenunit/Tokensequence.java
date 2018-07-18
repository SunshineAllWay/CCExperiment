package tokenunit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author HHeart
 */

public class Tokensequence {
	public int n;  //length of token sequence
	public ArrayList<String> sequence;
	
	private ArrayList<String> initSequence;
	private String lastToken;

	public Tokensequence(String[] strarr) {
		ArrayList<String> tokenList = new ArrayList<>();
		for (int i = 0; i < strarr.length; i++) {
			tokenList.add(strarr[i]);
		}
		n = tokenList.size();
		sequence = tokenList;
		splitTokenSeq();
	}
	public Tokensequence(ArrayList<String> tokenList) {
		n = tokenList.size();
		sequence = tokenList;
		splitTokenSeq();
	}

	public void splitTokenSeq() {
		if (n > 1) {
			ArrayList<String> tmpsequence = (ArrayList<String>) sequence.clone();
			String lastElem = tmpsequence.remove(n - 1);
			initSequence = tmpsequence;
			lastToken = lastElem;
		} else {
			initSequence = sequence;
			lastToken = null;
		}
	}

	public Tokensequence append(Token ptoken) {
		ArrayList<String> ls = sequence;
		ls.add(ptoken.mTokenElem);
		return (new Tokensequence(ls));
	}
	
	public int length() {
		return this.n;
	}
	
	public ArrayList<String> getSequence() {
		return this.sequence;
	}

	public ArrayList<String> getInitSequence() {
		return this.initSequence;
	}

	public String getLastToken() {
		return this.lastToken;
	}

	public Tokensequence subTokenSequence(int indexFrom, int indexTo) {
		List<String> tmpsequence = ((ArrayList<String>) sequence.clone()).subList(indexFrom, indexTo);
		ArrayList<String> ls = new ArrayList<>();
		ls.addAll(tmpsequence);
		return (new Tokensequence(ls));
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
		
		Tokensequence tokenseq = (Tokensequence) obj;
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
		Integer nInt = new Integer(n);
		String str = nInt.toString();
		int len = sequence.size();
		
		for (int i = 0; i < len; i++) {
			String s = sequence.get(i).toString();
			str = str.concat(s);
		}

		return str;
	}
}