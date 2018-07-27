package org.apache.solr.highlight;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
public class RegexFragmenter extends HighlightingPluginBase implements SolrFragmenter
{
  protected String defaultPatternRaw;
  protected Pattern defaultPattern;
  public void init(NamedList args) {
    super.init(args);
    defaultPatternRaw = LuceneRegexFragmenter.DEFAULT_PATTERN_RAW;
    if( defaults != null ) {
      defaultPatternRaw = defaults.get(HighlightParams.PATTERN, LuceneRegexFragmenter.DEFAULT_PATTERN_RAW);      
    }
    defaultPattern = Pattern.compile(defaultPatternRaw);
  }
  public Fragmenter getFragmenter(String fieldName, SolrParams params )
  { 
    numRequests++;        
    if( defaults != null ) {
      params = new DefaultSolrParams( params, defaults );
    }
    int fragsize  = params.getFieldInt(   fieldName, HighlightParams.FRAGSIZE,  LuceneRegexFragmenter.DEFAULT_FRAGMENT_SIZE );
    int increment = params.getFieldInt(   fieldName, HighlightParams.INCREMENT, LuceneRegexFragmenter.DEFAULT_INCREMENT_GAP );
    float slop    = params.getFieldFloat( fieldName, HighlightParams.SLOP,      LuceneRegexFragmenter.DEFAULT_SLOP );
    int maxchars  = params.getFieldInt(   fieldName, HighlightParams.MAX_RE_CHARS, LuceneRegexFragmenter.DEFAULT_MAX_ANALYZED_CHARS );
    String rawpat = params.getFieldParam( fieldName, HighlightParams.PATTERN,   LuceneRegexFragmenter.DEFAULT_PATTERN_RAW );
    Pattern p = rawpat == defaultPatternRaw ? defaultPattern : Pattern.compile(rawpat);
    if( fragsize <= 0 ) {
      return new NullFragmenter();
    }
    return new LuceneRegexFragmenter( fragsize, increment, slop, maxchars, p );
  }
  @Override
  public String getDescription() {
    return "RegexFragmenter (" + defaultPatternRaw + ")";
  }
  @Override
  public String getVersion() {
      return "$Revision: 801872 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: RegexFragmenter.java 801872 2009-08-07 03:21:06Z markrmiller $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/highlight/RegexFragmenter.java $";
  }
}
class LuceneRegexFragmenter implements Fragmenter
{
  public static final int DEFAULT_FRAGMENT_SIZE = 70;
  public static final int DEFAULT_INCREMENT_GAP = 50;
  public static final float DEFAULT_SLOP = 0.6f;
  public static final int DEFAULT_MAX_ANALYZED_CHARS = 10000;
  protected int targetFragChars;
  protected int incrementGapThreshold;
  protected float slop;
  protected int maxAnalyzedChars;
  protected Pattern textRE;
  protected int currentNumFrags;
  protected int currentOffset;
  protected int targetOffset;
  protected int[] hotspots;
  private PositionIncrementAttribute posIncAtt;
  private OffsetAttribute offsetAtt;
  public static final String 
    DEFAULT_PATTERN_RAW = "[-\\w ,\\n\"']{20,200}";
  public static final Pattern 
    DEFAULT_PATTERN = Pattern.compile(DEFAULT_PATTERN_RAW);
  public LuceneRegexFragmenter() {
    this(DEFAULT_FRAGMENT_SIZE, 
         DEFAULT_INCREMENT_GAP,
         DEFAULT_SLOP,
         DEFAULT_MAX_ANALYZED_CHARS);
  }
  public LuceneRegexFragmenter(int targetFragChars) {
    this(targetFragChars, 
         DEFAULT_INCREMENT_GAP,
         DEFAULT_SLOP,
         DEFAULT_MAX_ANALYZED_CHARS);
  }
  public LuceneRegexFragmenter(int targetFragChars, 
                               int incrementGapThreshold,
                               float slop,
                               int maxAnalyzedChars ) {
    this(targetFragChars, incrementGapThreshold, slop, maxAnalyzedChars,
         DEFAULT_PATTERN);
  }
  public LuceneRegexFragmenter(int targetFragChars, 
                               int incrementGapThreshold,
                               float slop,
                               int maxAnalyzedChars,
                               Pattern targetPattern) {
    this.targetFragChars = targetFragChars;
    this.incrementGapThreshold = incrementGapThreshold;    
    this.slop = slop;
    this.maxAnalyzedChars = maxAnalyzedChars;
    this.textRE = targetPattern;
  }
  public void start(String originalText, TokenStream tokenStream) {
    currentNumFrags = 1;
    currentOffset = 0;
    addHotSpots(originalText);
    posIncAtt = (PositionIncrementAttribute) tokenStream.getAttribute(PositionIncrementAttribute.class);
    offsetAtt = (OffsetAttribute) tokenStream.getAttribute(OffsetAttribute.class);
  }
  protected void addHotSpots(String text) {
    ArrayList<Integer> temphs = new ArrayList<Integer>(
                              text.length() / targetFragChars);
    Matcher match = textRE.matcher(text);
    int cur = 0;
    while(match.find() && cur < maxAnalyzedChars) {
      int start=match.start(), end=match.end();
      temphs.add(start);
      temphs.add(end);
      cur = end;
    }    
    hotspots = new int[temphs.size()];
    for(int i = 0; i < temphs.size(); i++) {
      hotspots[i] = temphs.get(i);
    }
    Arrays.sort(hotspots);
  }
  public boolean isNewFragment()
  {
    boolean isNewFrag = false;
    int minFragLen = (int)((1.0f - slop)*targetFragChars);
    int endOffset = offsetAtt.endOffset();
    if(posIncAtt.getPositionIncrement() > incrementGapThreshold) {
      isNewFrag = true;
    } else if(endOffset - currentOffset < minFragLen) {
      isNewFrag = false;
    } else if(targetOffset > 0) {
      isNewFrag = endOffset > targetOffset;
    } else {
      int minOffset = currentOffset + minFragLen;
      int maxOffset = (int)(currentOffset + (1.0f + slop)*targetFragChars);
      int hotIndex;
      hotIndex = Arrays.binarySearch(hotspots, endOffset);
      if(hotIndex < 0) hotIndex = -hotIndex;
      if(hotIndex >= hotspots.length) {
        targetOffset = currentOffset + targetFragChars;
      } else if(hotspots[hotIndex] > maxOffset) {
        targetOffset = currentOffset + targetFragChars;
      } else {
        int goal = hotspots[hotIndex];
        while(goal < minOffset && hotIndex < hotspots.length) {
          hotIndex++;
          goal = hotspots[hotIndex];
        }        
        targetOffset = goal <= maxOffset ? goal : currentOffset + targetFragChars;
      }
      isNewFrag = endOffset > targetOffset;
    }      
    if(isNewFrag) {
        currentNumFrags++;
        currentOffset = endOffset;
        targetOffset = -1;
    }
    return isNewFrag;
  }
}
