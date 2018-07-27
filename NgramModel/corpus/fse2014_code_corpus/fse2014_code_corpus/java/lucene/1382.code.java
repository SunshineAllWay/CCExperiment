package org.apache.lucene.xmlparser.builders;
import org.apache.lucene.search.DuplicateFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.xmlparser.DOMUtils;
import org.apache.lucene.xmlparser.FilterBuilder;
import org.apache.lucene.xmlparser.ParserException;
import org.w3c.dom.Element;
public class DuplicateFilterBuilder implements FilterBuilder {
	public Filter getFilter(Element e) throws ParserException {
        String fieldName=DOMUtils.getAttributeWithInheritanceOrFail(e,"fieldName");
		DuplicateFilter df=new DuplicateFilter(fieldName);
		String keepMode=DOMUtils.getAttribute(e,"keepMode","first");
		if(keepMode.equalsIgnoreCase("first"))
		{
			df.setKeepMode(DuplicateFilter.KM_USE_FIRST_OCCURRENCE);
		}
		else
			if(keepMode.equalsIgnoreCase("last"))
			{
				df.setKeepMode(DuplicateFilter.KM_USE_LAST_OCCURRENCE);
			}
			else
			{
				throw new ParserException("Illegal keepMode attribute in DuplicateFilter:"+keepMode);
			}
		String processingMode=DOMUtils.getAttribute(e,"processingMode","full");
		if(processingMode.equalsIgnoreCase("full"))
		{
			df.setProcessingMode(DuplicateFilter.PM_FULL_VALIDATION);
		}
		else
			if(processingMode.equalsIgnoreCase("fast"))
			{
				df.setProcessingMode(DuplicateFilter.PM_FAST_INVALIDATION);
			}
			else
			{
				throw new ParserException("Illegal processingMode attribute in DuplicateFilter:"+processingMode);
			}
		return df;
	}
}
