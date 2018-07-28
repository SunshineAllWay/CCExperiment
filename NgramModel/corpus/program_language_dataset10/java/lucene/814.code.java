package org.apache.lucene.analysis.cn.smart;
import java.util.Collections;
import java.util.List;
import org.apache.lucene.analysis.cn.smart.hhmm.HHMMSegmenter;
import org.apache.lucene.analysis.cn.smart.hhmm.SegToken;
import org.apache.lucene.analysis.cn.smart.hhmm.SegTokenFilter;
class WordSegmenter {
  private HHMMSegmenter hhmmSegmenter = new HHMMSegmenter();
  private SegTokenFilter tokenFilter = new SegTokenFilter();
  public List<SegToken> segmentSentence(String sentence, int startOffset) {
    List<SegToken> segTokenList = hhmmSegmenter.process(sentence);
    List<SegToken> result = Collections.emptyList();
    if (segTokenList.size() > 2) 
      result = segTokenList.subList(1, segTokenList.size() - 1);
    for (SegToken st : result)
      convertSegToken(st, sentence, startOffset);
    return result;
  }
  public SegToken convertSegToken(SegToken st, String sentence,
      int sentenceStartOffset) {
    switch (st.wordType) {
      case WordType.STRING:
      case WordType.NUMBER:
      case WordType.FULLWIDTH_NUMBER:
      case WordType.FULLWIDTH_STRING:
        st.charArray = sentence.substring(st.startOffset, st.endOffset)
            .toCharArray();
        break;
      default:
        break;
    }
    st = tokenFilter.filter(st);
    st.startOffset += sentenceStartOffset;
    st.endOffset += sentenceStartOffset;
    return st;
  }
}
