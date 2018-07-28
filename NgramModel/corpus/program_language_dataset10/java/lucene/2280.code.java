package org.apache.solr.analysis;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
public class SynonymFilter extends TokenFilter {
  private final SynonymMap map;  
  private Iterator<AttributeSource> replacement;  
  public SynonymFilter(TokenStream in, SynonymMap map) {
    super(in);
    this.map = map;
    addAttribute(TermAttribute.class);
    addAttribute(PositionIncrementAttribute.class);
    addAttribute(OffsetAttribute.class);
    addAttribute(TypeAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    while (true) {
      if (replacement!=null && replacement.hasNext()) {
        copy(this, replacement.next());
        return true;
      }
      AttributeSource firstTok = nextTok();
      if (firstTok == null) return false;
      TermAttribute termAtt = (TermAttribute) firstTok.addAttribute(TermAttribute.class);
      SynonymMap result = map.submap!=null ? map.submap.get(termAtt.termBuffer(), 0, termAtt.termLength()) : null;
      if (result == null) {
        copy(this, firstTok);
        return true;
      }
      if (firstTok == this)
        firstTok = cloneAttributes();
      matched = new LinkedList<AttributeSource>();
      result = match(result);
      if (result==null) {
        copy(this, firstTok);
        return true;
      }
      ArrayList<AttributeSource> generated = new ArrayList<AttributeSource>(result.synonyms.length + matched.size() + 1);
      AttributeSource lastTok = matched.isEmpty() ? firstTok : matched.getLast();
      boolean includeOrig = result.includeOrig();
      AttributeSource origTok = includeOrig ? firstTok : null;
      PositionIncrementAttribute firstPosIncAtt = (PositionIncrementAttribute) firstTok.addAttribute(PositionIncrementAttribute.class);
      int origPos = firstPosIncAtt.getPositionIncrement();  
      int repPos=0; 
      int pos=0;  
      for (int i=0; i<result.synonyms.length; i++) {
        Token repTok = result.synonyms[i];
        AttributeSource newTok = firstTok.cloneAttributes();
        TermAttribute newTermAtt = (TermAttribute) newTok.addAttribute(TermAttribute.class);
        OffsetAttribute newOffsetAtt = (OffsetAttribute) newTok.addAttribute(OffsetAttribute.class);
        TypeAttribute newTypeAtt = (TypeAttribute) newTok.addAttribute(TypeAttribute.class);
        PositionIncrementAttribute newPosIncAtt = (PositionIncrementAttribute) newTok.addAttribute(PositionIncrementAttribute.class);
        OffsetAttribute lastOffsetAtt = (OffsetAttribute) lastTok.addAttribute(OffsetAttribute.class);
        newOffsetAtt.setOffset(newOffsetAtt.startOffset(), lastOffsetAtt.endOffset());
        newTermAtt.setTermBuffer(repTok.termBuffer(), 0, repTok.termLength());
        repPos += repTok.getPositionIncrement();
        if (i==0) repPos=origPos;  
        while (origTok != null && origPos <= repPos) {
          PositionIncrementAttribute origPosInc = (PositionIncrementAttribute) origTok.addAttribute(PositionIncrementAttribute.class);
          origPosInc.setPositionIncrement(origPos-pos);
          generated.add(origTok);
          pos += origPosInc.getPositionIncrement();
          origTok = matched.isEmpty() ? null : matched.removeFirst();
          if (origTok != null) {
            origPosInc = (PositionIncrementAttribute) origTok.addAttribute(PositionIncrementAttribute.class);
            origPos += origPosInc.getPositionIncrement();
          }
        }
        newPosIncAtt.setPositionIncrement(repPos - pos);
        generated.add(newTok);
        pos += newPosIncAtt.getPositionIncrement();
      }
      while (origTok!=null) {
        PositionIncrementAttribute origPosInc = (PositionIncrementAttribute) origTok.addAttribute(PositionIncrementAttribute.class);
        origPosInc.setPositionIncrement(origPos-pos);
        generated.add(origTok);
        pos += origPosInc.getPositionIncrement();
        origTok = matched.isEmpty() ? null : matched.removeFirst();
        if (origTok != null) {
          origPosInc = (PositionIncrementAttribute) origTok.addAttribute(PositionIncrementAttribute.class);
          origPos += origPosInc.getPositionIncrement();
        }
      }
      replacement = generated.iterator();
    }
  }
  private LinkedList<AttributeSource> buffer;
  private LinkedList<AttributeSource> matched;
  private AttributeSource nextTok() throws IOException {
    if (buffer!=null && !buffer.isEmpty()) {
      return buffer.removeFirst();
    } else {
      if (input.incrementToken()) {
        return this;
      } else
        return null;
    }
  }
  private void pushTok(AttributeSource t) {
    if (buffer==null) buffer=new LinkedList<AttributeSource>();
    buffer.addFirst(t);
  }
  private SynonymMap match(SynonymMap map) throws IOException {
    SynonymMap result = null;
    if (map.submap != null) {
      AttributeSource tok = nextTok();
      if (tok != null) {
        if (tok == this)
          tok = cloneAttributes();
        TermAttribute termAtt = (TermAttribute) tok.getAttribute(TermAttribute.class);
        SynonymMap subMap = map.submap.get(termAtt.termBuffer(), 0, termAtt.termLength());
        if (subMap != null) {
          result = match(subMap);
        }
        if (result != null) {
          matched.addFirst(tok);
        } else {
          pushTok(tok);
        }
      }
    }
    if (result==null && map.synonyms!=null) {
      result = map;
    }
    return result;
  }
  private void copy(AttributeSource target, AttributeSource source) {
    if (target == source)
      return;
    for (Iterator<AttributeImpl> sourceIt = source.getAttributeImplsIterator(), targetIt=target.getAttributeImplsIterator(); 
         sourceIt.hasNext();) { 
           sourceIt.next().copyTo(targetIt.next()); 
    } 
  }
  @Override
  public void reset() throws IOException {
    input.reset();
    replacement = null;
  }
}
