package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.solr.analysis.BaseTokenFilterFactory;
public class HyphenatedWordsFilterFactory extends BaseTokenFilterFactory {
	public HyphenatedWordsFilter create(TokenStream input) {
		return new HyphenatedWordsFilter(input);
	}
}
