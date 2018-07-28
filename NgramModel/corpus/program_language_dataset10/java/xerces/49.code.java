package xni.parser;
import org.apache.xerces.parsers.AbstractSAXParser;
public class PSVIParser
    extends AbstractSAXParser {
    public PSVIParser() {
        super(new PSVIConfiguration());
    } 
} 
