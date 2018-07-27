package org.apache.lucene.search;
import org.apache.lucene.index.TermPositions;
import java.io.IOException;
import java.util.HashMap;
final class SloppyPhraseScorer extends PhraseScorer {
    private int slop;
    private PhrasePositions repeats[];
    private PhrasePositions tmpPos[]; 
    private boolean checkedRepeats;
    SloppyPhraseScorer(Weight weight, TermPositions[] tps, int[] offsets, Similarity similarity,
                       int slop, byte[] norms) {
        super(weight, tps, offsets, similarity, norms);
        this.slop = slop;
    }
    @Override
    protected final float phraseFreq() throws IOException {
        int end = initPhrasePositions();
        float freq = 0.0f;
        boolean done = (end<0);
        while (!done) {
            PhrasePositions pp = pq.pop();
            int start = pp.position;
            int next = pq.top().position;
            boolean tpsDiffer = true;
            for (int pos = start; pos <= next || !tpsDiffer; pos = pp.position) {
                if (pos<=next && tpsDiffer)
                    start = pos;                  
                if (!pp.nextPosition()) {
                    done = true;          
                    break;
                }
                PhrasePositions pp2 = null;
                tpsDiffer = !pp.repeats || (pp2 = termPositionsDiffer(pp))==null;
                if (pp2!=null && pp2!=pp) {
                  pp = flip(pp,pp2); 
                }
            }
            int matchLength = end - start;
            if (matchLength <= slop)
                freq += getSimilarity().sloppyFreq(matchLength); 
            if (pp.position > end)
                end = pp.position;
            pq.add(pp);               
        }
        return freq;
    }
    private PhrasePositions flip(PhrasePositions pp, PhrasePositions pp2) {
      int n=0;
      PhrasePositions pp3;
      while ((pp3=pq.pop()) != pp2) {
        tmpPos[n++] = pp3;
      }
      for (n--; n>=0; n--) {
        pq.insertWithOverflow(tmpPos[n]);
      }
      pq.add(pp);
      return pp2;
    }
    private int initPhrasePositions() throws IOException {
        int end = 0;
        if (checkedRepeats && repeats==null) {
            pq.clear();
            for (PhrasePositions pp = first; pp != null; pp = pp.next) {
                pp.firstPosition();
                if (pp.position > end)
                    end = pp.position;
                pq.add(pp);         
            }
            return end;
        }
        for (PhrasePositions pp = first; pp != null; pp = pp.next)
            pp.firstPosition();
        if (!checkedRepeats) {
            checkedRepeats = true;
            HashMap<PhrasePositions, Object> m = null;
            for (PhrasePositions pp = first; pp != null; pp = pp.next) {
                int tpPos = pp.position + pp.offset;
                for (PhrasePositions pp2 = pp.next; pp2 != null; pp2 = pp2.next) {
                    int tpPos2 = pp2.position + pp2.offset;
                    if (tpPos2 == tpPos) { 
                        if (m == null)
                            m = new HashMap<PhrasePositions, Object>();
                        pp.repeats = true;
                        pp2.repeats = true;
                        m.put(pp,null);
                        m.put(pp2,null);
                    }
                }
            }
            if (m!=null)
                repeats = m.keySet().toArray(new PhrasePositions[0]);
        }
        if (repeats!=null) {
            for (int i = 0; i < repeats.length; i++) {
                PhrasePositions pp = repeats[i];
                PhrasePositions pp2;
                while ((pp2 = termPositionsDiffer(pp)) != null) {
                  if (!pp2.nextPosition())  
                      return -1;           
                } 
            }
        }
        pq.clear();
        for (PhrasePositions pp = first; pp != null; pp = pp.next) {
            if (pp.position > end)
                end = pp.position;
            pq.add(pp);         
        }
        if (repeats!=null) {
          tmpPos = new PhrasePositions[pq.size()];
        }
        return end;
    }
    private PhrasePositions termPositionsDiffer(PhrasePositions pp) {
        int tpPos = pp.position + pp.offset;
        for (int i = 0; i < repeats.length; i++) {
            PhrasePositions pp2 = repeats[i];
            if (pp2 == pp)
                continue;
            int tpPos2 = pp2.position + pp2.offset;
            if (tpPos2 == tpPos)
                return pp.offset > pp2.offset ? pp : pp2; 
        }
        return null; 
    }
}
