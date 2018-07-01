package tokenunit;

/**
 * @author HHeart
 * @param <K>: type of element in token
 */

public class Tokencount<K> {
    public K mTokenElem;
    public int mCount;

    public Tokencount(K ptokenElem, int pcount) {
        this.mTokenElem = ptokenElem;
        this.mCount = pcount;
    }

    public void addCount() {
        mCount++;
    }

    public int hashCode() {
        return (mTokenElem.hashCode());
    }

    public boolean equals(Tokencount<K> tc) {
        boolean cnteq = (mCount == tc.mCount);
        boolean tokeneq = (mTokenElem == tc.mTokenElem);

        return (cnteq && tokeneq);
    }

    public String toString() {
        return (mTokenElem.toString() + String.valueOf(mCount));
    }
}