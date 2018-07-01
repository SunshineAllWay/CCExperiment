package tokenunit;

/**
 * @author HHeart
 * @param <K>: type of element in token
 */

public class Token<K> {
	public K mTokenELem;

	public Token(K ptokenElem) {
		this.mTokenELem = ptokenElem;
	}

	public int hashCode() {
		return (mTokenELem.hashCode());
	}

	public boolean equals(Token<K> tc) {
		return (mTokenELem == tc.mTokenELem);
	}

	public String toString() {
		return mTokenELem.toString();
	}
}