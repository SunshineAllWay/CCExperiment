package org.apache.solr.search.function;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.ValueSourceParser;
public class NvlValueSourceParser extends ValueSourceParser {
    private float nvlFloatValue = 0.0f;
    public ValueSource parse(FunctionQParser fp) throws ParseException {
	ValueSource source = fp.parseValueSource();
	final float nvl = fp.parseFloat();
	return new SimpleFloatFunction(source) {
	    protected String name() {
		return "nvl";
	    }
	    protected float func(int doc, DocValues vals) {
		float v = vals.floatVal(doc);
		if (v == nvlFloatValue) {
		    return nvl;
		} else {
		    return v;
		}
	    }
	};
    }
    public void init(NamedList args) {
	Float nvlFloatValueArg = (Float) args.get("nvlFloatValue");
	if (nvlFloatValueArg != null) {
	    this.nvlFloatValue = nvlFloatValueArg;
	}
    }
}