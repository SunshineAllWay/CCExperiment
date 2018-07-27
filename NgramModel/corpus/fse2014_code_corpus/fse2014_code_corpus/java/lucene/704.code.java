package org.apache.lucene.analysis.ru;
import java.io.Reader;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.Tokenizer; 
import org.apache.lucene.analysis.LetterTokenizer; 
import org.apache.lucene.analysis.standard.StandardTokenizer; 
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
@Deprecated
public class RussianLetterTokenizer extends CharTokenizer
{    
    private static final int DIGIT_0 = '0';
    private static final int DIGIT_9 = '9';
    public RussianLetterTokenizer(Version matchVersion, Reader in) {
      super(matchVersion, in);
    }
    public RussianLetterTokenizer(Version matchVersion, AttributeSource source, Reader in) {
      super(matchVersion, source, in);
    }
    public RussianLetterTokenizer(Version matchVersion, AttributeFactory factory, Reader in) {
      super(matchVersion, factory, in);
    }
    @Deprecated
    public RussianLetterTokenizer(Reader in) {
      super(in);
    }
    @Deprecated
    public RussianLetterTokenizer(AttributeSource source, Reader in) {
      super(source, in);
    }
    @Deprecated
    public RussianLetterTokenizer(AttributeFactory factory, Reader in) {
      super(factory, in);
    }
    @Override
    protected boolean isTokenChar(int c) {
        return Character.isLetter(c) || (c >= DIGIT_0 && c <= DIGIT_9);
    }
}
