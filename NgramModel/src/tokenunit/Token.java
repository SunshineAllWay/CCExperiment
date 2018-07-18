package tokenunit;

/**
 * @author HHeart
 */

public class Token {
	public String mTokenElem;

	public Token(String ptokenElem) {
		this.mTokenElem = ptokenElem;
	}

	public int hashCode() {
		return (mTokenElem.hashCode());
	}

	public boolean equals(Token tc) {
		return (mTokenElem == tc.mTokenElem);
	}
}