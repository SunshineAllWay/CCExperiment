package xni.parser;
import org.apache.xerces.parsers.AbstractSAXParser;
public class CSVParser
    extends AbstractSAXParser {
    public CSVParser() {
        super(new CSVConfiguration());
    } 
} 
