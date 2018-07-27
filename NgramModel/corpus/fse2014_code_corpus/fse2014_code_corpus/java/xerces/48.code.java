package xni.parser;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
import xni.PSVIWriter;
public class PSVIConfiguration extends XIncludeAwareParserConfiguration {
    protected PSVIWriter fPSVIWriter;
    public PSVIConfiguration() {
        this(null, null);
    } 
    public PSVIConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null);
    } 
    public PSVIConfiguration(SymbolTable symbolTable,
                                     XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public PSVIConfiguration(SymbolTable symbolTable,
                                    XMLGrammarPool grammarPool,
                                    XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);
        fPSVIWriter = createPSVIWriter();
        if (fPSVIWriter != null) {
            addCommonComponent(fPSVIWriter);
        }
    } 
    protected void configurePipeline() {
        super.configurePipeline();
        addPSVIWriterToPipeline();
    } 
    protected void configureXML11Pipeline() {
        super.configureXML11Pipeline();
        addPSVIWriterToPipeline();
    } 
    protected void addPSVIWriterToPipeline() {
        if (fSchemaValidator != null) {
            fSchemaValidator.setDocumentHandler(fPSVIWriter);
            fPSVIWriter.setDocumentSource(fSchemaValidator);
            fPSVIWriter.setDocumentHandler(fDocumentHandler);
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fPSVIWriter);
            }
        }
    } 
    protected PSVIWriter createPSVIWriter(){
        return new PSVIWriter();
    }
} 
