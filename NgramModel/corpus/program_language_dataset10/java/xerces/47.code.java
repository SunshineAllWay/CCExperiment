package xni.parser;
import org.apache.xerces.impl.XMLNamespaceBinder;
import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.parsers.StandardParserConfiguration;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
public class NonValidatingParserConfiguration 
    extends StandardParserConfiguration {
    protected XMLNamespaceBinder fNamespaceBinder;
    public NonValidatingParserConfiguration() {
        fNamespaceBinder = new XMLNamespaceBinder();
        addComponent(fNamespaceBinder);
    } 
    protected void configurePipeline() {
        fScanner.setDocumentHandler(fNamespaceBinder);
        fNamespaceBinder.setDocumentHandler(fDocumentHandler);
        fNamespaceBinder.setDocumentSource(fScanner);
    } 
    protected XMLDTDValidator createDTDValidator() {
        return null;
    } 
} 
