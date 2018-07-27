package org.apache.lucene.search;
public class TopFieldDocs
extends TopDocs {
	public SortField[] fields;
        public TopFieldDocs (int totalHits, ScoreDoc[] scoreDocs, SortField[] fields, float maxScore) {
	  super (totalHits, scoreDocs, maxScore);
	  this.fields = fields;
	}
}