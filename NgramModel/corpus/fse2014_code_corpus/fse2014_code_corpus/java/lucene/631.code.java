package org.apache.lucene.analysis.cn;
import java.io.IOException;
import java.util.Arrays;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
@Deprecated
public final class ChineseFilter extends TokenFilter {
    public static final String[] STOP_WORDS = {
    "and", "are", "as", "at", "be", "but", "by",
    "for", "if", "in", "into", "is", "it",
    "no", "not", "of", "on", "or", "such",
    "that", "the", "their", "then", "there", "these",
    "they", "this", "to", "was", "will", "with"
    };
    private CharArraySet stopTable;
    private TermAttribute termAtt;
    public ChineseFilter(TokenStream in) {
        super(in);
        stopTable = new CharArraySet(Version.LUCENE_CURRENT, Arrays.asList(STOP_WORDS), false);
        termAtt = addAttribute(TermAttribute.class);
    }
    @Override
    public boolean incrementToken() throws IOException {
        while (input.incrementToken()) {
            char text[] = termAtt.termBuffer();
            int termLength = termAtt.termLength();
            if (!stopTable.contains(text, 0, termLength)) {
                switch (Character.getType(text[0])) {
                case Character.LOWERCASE_LETTER:
                case Character.UPPERCASE_LETTER:
                    if (termLength>1) {
                        return true;
                    }
                    break;
                case Character.OTHER_LETTER:
                    return true;
                }
            }
        }
        return false;
    }
}