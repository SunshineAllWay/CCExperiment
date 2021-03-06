package org.apache.lucene.analysis.cn;
import java.io.Reader;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.standard.StandardAnalyzer; 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
@Deprecated
public final class ChineseAnalyzer extends ReusableAnalyzerBase {
    @Override
    protected TokenStreamComponents createComponents(String fieldName,
        Reader reader) {
      final Tokenizer source = new ChineseTokenizer(reader);
      return new TokenStreamComponents(source, new ChineseFilter(source));
    }
}