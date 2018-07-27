package org.apache.lucene.search.spell;
public class NGramDistance implements StringDistance {
  private int n;
  public NGramDistance(int size) {
    this.n = size;
  }
  public NGramDistance() {
    this(2);
  }
  public float getDistance(String source, String target) {
    final int sl = source.length();
    final int tl = target.length();
    if (sl == 0 || tl == 0) {
      if (sl == tl) {
        return 1;
      }
      else {
        return 0;
      }
    }
    int cost = 0;
    if (sl < n || tl < n) {
      for (int i=0,ni=Math.min(sl,tl);i<ni;i++) {
        if (source.charAt(i) == target.charAt(i)) {
          cost++;
        }
      }
      return (float) cost/Math.max(sl, tl);
    }
    char[] sa = new char[sl+n-1];
    float p[]; 
    float d[]; 
    float _d[]; 
    for (int i=0;i<sa.length;i++) {
      if (i < n-1) {
        sa[i]=0; 
      }
      else {
        sa[i] = source.charAt(i-n+1);
      }
    }
    p = new float[sl+1]; 
    d = new float[sl+1]; 
    int i; 
    int j; 
    char[] t_j = new char[n]; 
    for (i = 0; i<=sl; i++) {
        p[i] = i;
    }
    for (j = 1; j<=tl; j++) {
        if (j < n) {
          for (int ti=0;ti<n-j;ti++) {
            t_j[ti]=0; 
          }
          for (int ti=n-j;ti<n;ti++) {
            t_j[ti]=target.charAt(ti-(n-j));
          }
        }
        else {
          t_j = target.substring(j-n, j).toCharArray();
        }
        d[0] = j;
        for (i=1; i<=sl; i++) {
            cost = 0;
            int tn=n;
            for (int ni=0;ni<n;ni++) {
              if (sa[i-1+ni] != t_j[ni]) {
                cost++;
              }
              else if (sa[i-1+ni] == 0) { 
                tn--;
              }
            }
            float ec = (float) cost/tn;
            d[i] = Math.min(Math.min(d[i-1]+1, p[i]+1),  p[i-1]+ec);
        }
        _d = p;
        p = d;
        d = _d;
    }
    return 1.0f - (p[sl] / Math.max(tl, sl));
  }
}
