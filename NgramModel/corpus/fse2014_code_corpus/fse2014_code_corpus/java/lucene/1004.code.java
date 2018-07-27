package org.apache.lucene.search.highlight;
public class SimpleHTMLFormatter implements Formatter {
  private static final String DEFAULT_PRE_TAG = "<B>";
  private static final String DEFAULT_POST_TAG = "</B>";
	private String preTag;
	private String postTag;
	public SimpleHTMLFormatter(String preTag, String postTag) {
		this.preTag = preTag;
		this.postTag = postTag;
	}
	public SimpleHTMLFormatter() {
	  this(DEFAULT_PRE_TAG, DEFAULT_POST_TAG);
	}
	public String highlightTerm(String originalText, TokenGroup tokenGroup) {
	  if (tokenGroup.getTotalScore() <= 0) {
	    return originalText;
	  }
	  StringBuilder returnBuffer = new StringBuilder(preTag.length() + originalText.length() + postTag.length());
	  returnBuffer.append(preTag);
	  returnBuffer.append(originalText);
	  returnBuffer.append(postTag);
	  return returnBuffer.toString();
	}
}
