package org.apache.solr.analysis;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.FloatEncoder;
import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.core.SolrResourceLoader;
public class TestDelimitedPayloadTokenFilterFactory extends TestCase {
  public void testEncoder() throws Exception {
    Map<String,String> args = new HashMap<String, String>();
    args.put(DelimitedPayloadTokenFilterFactory.ENCODER_ATTR, "float");
    DelimitedPayloadTokenFilterFactory factory = new DelimitedPayloadTokenFilterFactory();
    factory.init(args);
    ResourceLoader loader = new SolrResourceLoader(null, null);
    factory.inform(loader);
    TokenStream input = new WhitespaceTokenizer(new StringReader("the|0.1 quick|0.1 red|0.1"));
    DelimitedPayloadTokenFilter tf = factory.create(input);
    while (tf.incrementToken()){
      PayloadAttribute payAttr = (PayloadAttribute) tf.getAttribute(PayloadAttribute.class);
      assertTrue("payAttr is null and it shouldn't be", payAttr != null);
      byte[] payData = payAttr.getPayload().getData();
      assertTrue("payData is null and it shouldn't be", payData != null);
      assertTrue("payData is null and it shouldn't be", payData != null);
      float payFloat = PayloadHelper.decodeFloat(payData);
      assertTrue(payFloat + " does not equal: " + 0.1f, payFloat == 0.1f);
    }
  }
  public void testDelim() throws Exception {
    Map<String,String> args = new HashMap<String, String>();
    args.put(DelimitedPayloadTokenFilterFactory.ENCODER_ATTR, FloatEncoder.class.getName());
    args.put(DelimitedPayloadTokenFilterFactory.DELIMITER_ATTR, "*");
    DelimitedPayloadTokenFilterFactory factory = new DelimitedPayloadTokenFilterFactory();
    factory.init(args);
    ResourceLoader loader = new SolrResourceLoader(null, null);
    factory.inform(loader);
    TokenStream input = new WhitespaceTokenizer(new StringReader("the*0.1 quick*0.1 red*0.1"));
    DelimitedPayloadTokenFilter tf = factory.create(input);
    while (tf.incrementToken()){
      PayloadAttribute payAttr = (PayloadAttribute) tf.getAttribute(PayloadAttribute.class);
      assertTrue("payAttr is null and it shouldn't be", payAttr != null);
      byte[] payData = payAttr.getPayload().getData();
      assertTrue("payData is null and it shouldn't be", payData != null);
      float payFloat = PayloadHelper.decodeFloat(payData);
      assertTrue(payFloat + " does not equal: " + 0.1f, payFloat == 0.1f);
    }
  }
}
