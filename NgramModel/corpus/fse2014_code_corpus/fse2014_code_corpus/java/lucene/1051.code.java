package org.apache.lucene.misc;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.index.FieldInvertState;
import java.util.Map;
import java.util.HashMap;
public class SweetSpotSimilarity extends DefaultSimilarity {
  private int ln_min = 1;
  private int ln_max = 1;
  private float ln_steep = 0.5f;
  private Map<String,Number> ln_maxs = new HashMap<String,Number>(7);
  private Map<String,Number> ln_mins = new HashMap<String,Number>(7);
  private Map<String,Float> ln_steeps = new HashMap<String,Float>(7);
  private Map<String,Boolean> ln_overlaps = new HashMap<String,Boolean>(7);
  private float tf_base = 0.0f;
  private float tf_min = 0.0f;
  private float tf_hyper_min = 0.0f;
  private float tf_hyper_max = 2.0f;
  private double tf_hyper_base = 1.3d;
  private float tf_hyper_xoffset = 10.0f;
  public SweetSpotSimilarity() {
    super();
  }
  public void setBaselineTfFactors(float base, float min) {
    tf_min = min;
    tf_base = base;
  }
  public void setHyperbolicTfFactors(float min, float max,
                                     double base, float xoffset) {
    tf_hyper_min = min;
    tf_hyper_max = max;
    tf_hyper_base = base;
    tf_hyper_xoffset = xoffset;
  }
  public void setLengthNormFactors(int min, int max, float steepness) {
    this.ln_min = min;
    this.ln_max = max;
    this.ln_steep = steepness;
  }
  public void setLengthNormFactors(String field, int min, int max,
                                   float steepness, boolean discountOverlaps) {
    ln_mins.put(field, Integer.valueOf(min));
    ln_maxs.put(field, Integer.valueOf(max));
    ln_steeps.put(field, Float.valueOf(steepness));
    ln_overlaps.put(field, new Boolean(discountOverlaps));
  }
  @Override
  public float computeNorm(String fieldName, FieldInvertState state) {
    final int numTokens;
    boolean overlaps = discountOverlaps;
    if (ln_overlaps.containsKey(fieldName)) {
      overlaps = ln_overlaps.get(fieldName).booleanValue();
    }
    if (overlaps)
      numTokens = state.getLength() - state.getNumOverlap();
    else
      numTokens = state.getLength();
    return state.getBoost() * lengthNorm(fieldName, numTokens);
  }
  @Override
  public float lengthNorm(String fieldName, int numTerms) {
    int l = ln_min;
    int h = ln_max;
    float s = ln_steep;
    if (ln_mins.containsKey(fieldName)) {
      l = ln_mins.get(fieldName).intValue();
    }
    if (ln_maxs.containsKey(fieldName)) {
      h = ln_maxs.get(fieldName).intValue();
    }
    if (ln_steeps.containsKey(fieldName)) {
      s = ln_steeps.get(fieldName).floatValue();
    }
    return (float)
      (1.0f /
       Math.sqrt
       (
        (
         s *
         (float)(Math.abs(numTerms - l) + Math.abs(numTerms - h) - (h-l))
         )
        + 1.0f
        )
       );
  }
  @Override
  public float tf(int freq) {
    return baselineTf(freq);
  }
  public float baselineTf(float freq) {
    if (0.0f == freq) return 0.0f;
    return (freq <= tf_min)
      ? tf_base
      : (float)Math.sqrt(freq + (tf_base * tf_base) - tf_min);
  }
  public float hyperbolicTf(float freq) {
    if (0.0f == freq) return 0.0f;
    final float min = tf_hyper_min;
    final float max = tf_hyper_max;
    final double base = tf_hyper_base;
    final float xoffset = tf_hyper_xoffset;
    final double x = (double)(freq - xoffset);
    final float result = min +
      (float)(
              (max-min) / 2.0f
              *
              (
               ( ( Math.pow(base,x) - Math.pow(base,-x) )
                 / ( Math.pow(base,x) + Math.pow(base,-x) )
                 )
               + 1.0d
               )
              );
    return Float.isNaN(result) ? max : result;
  }
}
