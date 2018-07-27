package org.apache.solr.analysis;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Locale;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.collation.CollationKeyFilter;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.util.plugin.ResourceLoaderAware;
public class CollationKeyFilterFactory extends BaseTokenFilterFactory implements ResourceLoaderAware {
  private Collator collator;
  public void inform(ResourceLoader loader) {
    String custom = args.get("custom");
    String language = args.get("language");
    String country = args.get("country");
    String variant = args.get("variant");
    String strength = args.get("strength");
    String decomposition = args.get("decomposition");
    if (custom == null && language == null)
      throw new SolrException(ErrorCode.SERVER_ERROR, "Either custom or language is required.");
    if (custom != null && 
        (language != null || country != null || variant != null))
      throw new SolrException(ErrorCode.SERVER_ERROR, "Cannot specify both language and custom. "
          + "To tailor rules for a built-in language, see the javadocs for RuleBasedCollator. "
          + "Then save the entire customized ruleset to a file, and use with the custom parameter");
    if (language != null) { 
      collator = createFromLocale(language, country, variant);
    } else { 
      collator = createFromRules(custom, loader);
    }
    if (strength != null) {
      if (strength.equalsIgnoreCase("primary"))
        collator.setStrength(Collator.PRIMARY);
      else if (strength.equalsIgnoreCase("secondary"))
        collator.setStrength(Collator.SECONDARY);
      else if (strength.equalsIgnoreCase("tertiary"))
        collator.setStrength(Collator.TERTIARY);
      else if (strength.equalsIgnoreCase("identical"))
        collator.setStrength(Collator.IDENTICAL);
      else
        throw new SolrException(ErrorCode.SERVER_ERROR, "Invalid strength: " + strength);
    }
    if (decomposition != null) {
      if (decomposition.equalsIgnoreCase("no"))
        collator.setDecomposition(Collator.NO_DECOMPOSITION);
      else if (decomposition.equalsIgnoreCase("canonical"))
        collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
      else if (decomposition.equalsIgnoreCase("full"))
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);
      else
        throw new SolrException(ErrorCode.SERVER_ERROR, "Invalid decomposition: " + decomposition);
    }
  }
  public TokenStream create(TokenStream input) {
    return new CollationKeyFilter(input, collator);
  }
  private Collator createFromLocale(String language, String country, String variant) {
    Locale locale;
    if (language != null && country == null && variant != null)
      throw new SolrException(ErrorCode.SERVER_ERROR, 
          "To specify variant, country is required");
    else if (language != null && country != null && variant != null)
      locale = new Locale(language, country, variant);
    else if (language != null && country != null)
      locale = new Locale(language, country);
    else 
      locale = new Locale(language);
    return Collator.getInstance(locale);
  }
  private Collator createFromRules(String fileName, ResourceLoader loader) {
    InputStream input = null;
    try {
     input = loader.openResource(fileName);
     String rules = IOUtils.toString(input, "UTF-8");
     return new RuleBasedCollator(rules);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    } finally {
      IOUtils.closeQuietly(input);
    }
  }
}
