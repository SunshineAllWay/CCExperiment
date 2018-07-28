package org.apache.batik.svggen;
import org.apache.batik.svggen.SVGGeneratorContext.GraphicContextDefaults;
import java.awt.Dimension;
import java.awt.Font;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
public class SVGAccuracyTest extends AbstractTest
    implements SVGConstants{
    public static final String ERROR_CANNOT_GENERATE_SVG
        = "SVGAccuracyTest.error.cannot.generate.svg";
    public static final String ERROR_CANNOT_OPEN_REFERENCE_SVG_FILE
        = "SVGAccuracyTest.error.cannot.open.reference.svg.file";
    public static final String ERROR_ERROR_WHILE_COMPARING_FILES
        = "SVGAccuracyTest.error.while.comparing.files";
    public static final String ERROR_GENERATED_SVG_INACCURATE
        = "SVGAccuracyTest.error.generated.svg.inaccurate";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION
        = "SVGAccuracyTest.entry.key.error.description";
    public static final String ENTRY_KEY_LINE_NUMBER
        = "SVGAccuracyTest.entry.key.line.number";
    public static final String ENTRY_KEY_COLUMN_NUMBER
        = "SVGAccuracyTest.entry.key.column.number";
    public static final String ENTRY_KEY_COLUMN_EXPECTED_VALUE
        = "SVGAccuracyTest.entry.key.column.expected.value";
    public static final String ENTRY_KEY_COLUMN_FOUND_VALUE
        = "SVGAccuracyTest.entry.key.column.found.value";
    public static final String ENTRY_KEY_REFERENCE_LINE
        = "SVGAccuracyTest.entry.key.reference.line";
    public static final String ENTRY_KEY_NEW_LINE
        = "SVGAccuracyTest.entry.key.new.line";
    public static final Dimension CANVAS_SIZE
        = new Dimension(300, 400);
    private Painter painter;
    private URL refURL;
    private File saveSVG;
    public SVGAccuracyTest(Painter painter,
                           URL refURL){
        this.painter = painter;
        this.refURL  = refURL;
    }
    public File getSaveSVG(){
        return saveSVG;
    }
    public void setSaveSVG(File saveSVG){
        this.saveSVG = saveSVG;
    }
    public TestReport runImpl() throws Exception {
        DefaultTestReport report
            = new DefaultTestReport(this);
        SVGGraphics2D g2d = buildSVGGraphics2D();
        g2d.setSVGCanvasSize(CANVAS_SIZE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF-8");
        try{
            painter.paint(g2d);
            configureSVGGraphics2D(g2d);
            g2d.stream(osw);
            osw.flush();
            bos.flush();
            bos.close();
        }catch(Exception e){
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setErrorCode(ERROR_CANNOT_GENERATE_SVG);
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                                     Messages.formatMessage(ERROR_CANNOT_GENERATE_SVG,
                                                            new String[]{painter == null? "null" : painter.getClass().getName(),
                                                                         e.getClass().getName(),
                                                                         e.getMessage(),
                                                                         trace.toString() })) });
            report.setPassed(false);
            return report;
        }
        InputStream refStream = null;
        try {
            refStream =
                new BufferedInputStream(refURL.openStream());
        }catch(Exception e){
            report.setErrorCode(ERROR_CANNOT_OPEN_REFERENCE_SVG_FILE);
            report.setDescription( new TestReport.Entry[]{
                new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                          Messages.formatMessage(ERROR_CANNOT_OPEN_REFERENCE_SVG_FILE,
                                                 new Object[]{refURL != null? refURL.toExternalForm() : "null",
                                                              e.getMessage()})) });
            report.setPassed(false);
            save(bos.toByteArray());
            return report;
        }
        InputStream newStream = new ByteArrayInputStream(bos.toByteArray());
        boolean accurate = true;
        String refLine = null;
        String newLine = null;
        int ln = 1;
        try{
            BufferedReader refReader = new BufferedReader(new InputStreamReader(refStream));
            BufferedReader newReader = new BufferedReader(new InputStreamReader(newStream));
            while((refLine = refReader.readLine()) != null){
                newLine = newReader.readLine();
                if(newLine == null || !refLine.equals(newLine)){
                    accurate = false;
                    break;
                }
                ln++;
            }
            if(accurate){
                newLine = newReader.readLine();
                if(newLine != null){
                    accurate = false;
                }
            }
        } catch(IOException e) {
            report.setErrorCode(ERROR_ERROR_WHILE_COMPARING_FILES);
            report.setDescription(new TestReport.Entry[]{
                new TestReport.Entry(Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                                     Messages.formatMessage(ERROR_ERROR_WHILE_COMPARING_FILES,
                                                            new Object[]{refURL.toExternalForm(),
                                                                         e.getMessage()}))});
            report.setPassed(false);
            save(bos.toByteArray());
            return report;
        }
        if(!accurate){
            save(bos.toByteArray());
            int cn = computeColumnNumber(refLine, newLine);
            String expectedChar = "eol";
            if(cn >= 0 && refLine != null && refLine.length() > cn){
                expectedChar = (new Character(refLine.charAt(cn))).toString();
            }
            String foundChar = "null";
            if(cn >=0 && newLine != null && newLine.length() > cn){
                foundChar = (new Character(newLine.charAt(cn))).toString();
            }
            if(expectedChar.equals(" ")){
                expectedChar = "' '";
            }
            if(foundChar.equals(" ")){
                foundChar = "' '";
            }
            report.setErrorCode(ERROR_GENERATED_SVG_INACCURATE);
            report.addDescriptionEntry(Messages.formatMessage(ENTRY_KEY_LINE_NUMBER,null), new Integer(ln));
            report.addDescriptionEntry(Messages.formatMessage(ENTRY_KEY_COLUMN_NUMBER,null), new Integer(cn));
            report.addDescriptionEntry(Messages.formatMessage(ENTRY_KEY_COLUMN_EXPECTED_VALUE,null), expectedChar);
            report.addDescriptionEntry(Messages.formatMessage(ENTRY_KEY_COLUMN_FOUND_VALUE,null), foundChar);
            report.addDescriptionEntry(Messages.formatMessage(ENTRY_KEY_REFERENCE_LINE,null), refLine);
            report.addDescriptionEntry(Messages.formatMessage(ENTRY_KEY_NEW_LINE,null), newLine);
            report.setPassed(false);
        }
        else{
            report.setPassed(true);
        }
        return report;
    }
    public int computeColumnNumber(String aStr, String bStr){
        if(aStr == null || bStr == null){
            return -1;
        }
        int n = aStr.length();
        int i = -1;
        for(i=0; i<n; i++){
            char a = aStr.charAt(i);
            if(i < bStr.length()){
                char b = bStr.charAt(i);
                if(a != b){
                    break;
                }
            }
            else {
                break;
            }
        }
        return i;
    }
    protected void save(byte[] data) throws IOException{
        if(saveSVG == null){
            return;
        }
        FileOutputStream os = new FileOutputStream(saveSVG);
        os.write(data);
        os.close();
    }
    protected boolean byteCompare(InputStream refStream,
                                  InputStream newStream)
        throws IOException{
        int b = 0;
        int nb = 0;
        do {
            if (b == nb || nb != 13)
                b = refStream.read();
            nb = newStream.read();
        } while (b != -1 && nb != -1 && (b == nb || nb == 13));
        refStream.close();
        newStream.close();
        return (b == nb || nb == 13);
    }
    protected SVGGraphics2D buildSVGGraphics2D() {
        DOMImplementation impl = GenericDOMImplementation.getDOMImplementation();
        String namespaceURI = SVGConstants.SVG_NAMESPACE_URI;
        Document domFactory = impl.createDocument(namespaceURI, SVG_SVG_TAG, null);
        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(domFactory);
        GraphicContextDefaults defaults
            = new GraphicContextDefaults();
        defaults.font = new Font("Arial", Font.PLAIN, 12);
        ctx.setGraphicContextDefaults(defaults);
        ctx.setPrecision(12);
        return new SVGGraphics2D(ctx, false);
    }
    protected void configureSVGGraphics2D(SVGGraphics2D g2d) {}
}
